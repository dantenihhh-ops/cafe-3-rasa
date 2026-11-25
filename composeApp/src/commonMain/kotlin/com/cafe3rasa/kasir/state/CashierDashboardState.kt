package com.cafe3rasa.kasir.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cafe3rasa.kasir.data.AddItemsBody
import com.cafe3rasa.kasir.data.ApiClient
import com.cafe3rasa.kasir.data.ApiException
import com.cafe3rasa.kasir.data.KasirOrderBody
import com.cafe3rasa.kasir.data.MenuDto
import com.cafe3rasa.kasir.data.MoveTableBody
import com.cafe3rasa.kasir.data.NewItemBody
import com.cafe3rasa.kasir.data.OrderDto
import com.cafe3rasa.kasir.data.PaymentBody
import com.cafe3rasa.kasir.data.RealtimeClient
import com.cafe3rasa.kasir.data.RespondBody
import com.cafe3rasa.kasir.data.ServiceRequestDto
import com.cafe3rasa.kasir.data.TableDto
import com.cafe3rasa.kasir.data.TableStatusBody
import com.cafe3rasa.kasir.data.UpdateItemBody
import com.cafe3rasa.kasir.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

enum class TableFilter(val label: String) {
    SEMUA("Semua"),
    BARU_PESAN("Baru Pesan"),
    TERISI("Terisi"),
    PERLU_BILL("Perlu Bill")
}

enum class MobileNavigationTab(val label: String) {
    TABLE_MAP("Peta Meja"),
    ORDER_DETAIL("Pesanan Meja"),
    QUICK_MENU("Tambah Menu")
}

/** Pesan singkat untuk ditampilkan di snackbar (id unik agar pesan yang sama tetap muncul lagi) */
data class UiMessage(val id: Long, val text: String)

/**
 * State dashboard kasir yang terhubung ke backend.
 *
 * - Data meja, pesanan, permintaan & menu diambil dari server (REST API)
 * - Perubahan dari web pelanggan masuk real-time lewat Socket.io
 * - Setiap aksi kasir dikirim ke server, lalu data dimuat ulang
 */
class CashierDashboardState {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val realtime = RealtimeClient()
    private var pollingJob: Job? = null
    private var refreshJob: Job? = null
    private var started = false
    private var messageCounter = 0L

    // ─── Status koneksi & pemuatan ────────────────────────────────────────
    var isRefreshing by mutableStateOf(false)
        private set
    var isLoadedOnce by mutableStateOf(false)
        private set
    var isRealtimeConnected by mutableStateOf(false)
        private set
    var loadError by mutableStateOf<String?>(null)
        private set
    var isBusy by mutableStateOf(false)
        private set
    var message by mutableStateOf<UiMessage?>(null)
        private set

    // ─── Pilihan & filter UI ──────────────────────────────────────────────
    var selectedTableNumber by mutableStateOf("01")
    var selectedTableOccupied by mutableStateOf(false)
    var activeTableFilter by mutableStateOf(TableFilter.SEMUA)
    var activeMenuCategory by mutableStateOf(MenuCategory.SEMUA)
    var activeTagFilter by mutableStateOf<String?>("Semua")
    var menuSearchQuery by mutableStateOf("")
    val availableTags = listOf("Semua", "Best Seller", "Terlaris", "Rekomendasi")
    var paymentStatus by mutableStateOf(PaymentBillStatus.BELUM_BAYAR)
    var selectedPaymentMethod by mutableStateOf(PaymentMethod.BELUM_DIPILIH)
    var showPaymentMethodDialog by mutableStateOf(false)
    var mobileTab by mutableStateOf(MobileNavigationTab.TABLE_MAP)
    var showChangeTableDialog by mutableStateOf(false)

    // ─── Data dari server ─────────────────────────────────────────────────
    val tables = mutableStateListOf<TableModel>()
    val requests = mutableStateListOf<TableRequest>()
    val orderItems = mutableStateListOf<OrderItemModel>()
    val menuCatalog = mutableStateListOf<MenuItemModel>()

    private var tableDtos: List<TableDto> = emptyList()

    /** Pesanan aktif meja yang sedang dipilih (null jika meja kosong) */
    val selectedOrder: OrderDto?
        get() = tableDtos.firstOrNull { it.number == selectedTableNumber }?.currentOrder

    // ─── Turunan ──────────────────────────────────────────────────────────
    val totalItemCount: Int
        get() = orderItems.sumOf { it.quantity }

    val totalRupiah: Long
        get() = selectedOrder?.total ?: orderItems.sumOf { it.subtotal }

    val filteredTables: List<TableModel>
        get() = when (activeTableFilter) {
            TableFilter.SEMUA -> tables
            TableFilter.BARU_PESAN -> tables.filter { it.status == TableStatus.BARU_PESAN }
            TableFilter.TERISI -> tables.filter {
                it.status == TableStatus.TERISI_BELUM_BAYAR || it.status == TableStatus.TERISI_LUNAS
            }
            TableFilter.PERLU_BILL -> tables.filter { it.status == TableStatus.TERISI_BELUM_BAYAR }
        }

    val filteredMenu: List<MenuItemModel>
        get() = menuCatalog.filter { item ->
            val matchCategory = activeMenuCategory == MenuCategory.SEMUA || item.category == activeMenuCategory
            val matchTag = activeTagFilter.isNullOrBlank() || activeTagFilter == "Semua" || item.filters.contains(activeTagFilter)
            val matchSearch = menuSearchQuery.isBlank() || item.name.contains(menuSearchQuery, ignoreCase = true)
            matchCategory && matchTag && matchSearch
        }

    val emptyTables: List<TableModel>
        get() = tables.filter { it.status == TableStatus.KOSONG && it.number != selectedTableNumber }

    // ═══════════════════════════════════════════════════════════════════════
    //  SIKLUS HIDUP
    // ═══════════════════════════════════════════════════════════════════════

    /** Dipanggil saat dashboard tampil: muat data + sambungkan real-time */
    fun start() {
        if (started) return
        started = true
        refresh(showIndicator = false)

        realtime.connect(
            serverUrl = ApiClient.serverUrl,
            token = ApiClient.token,
            onEvent = { event, tableNumber ->
                scope.launch {
                    when (event) {
                        "new_order" -> notify("Pesanan baru masuk dari Meja ${tableNumber ?: "-"}")
                        "call_waiter" -> notify("Meja ${tableNumber ?: "-"} memanggil bantuan")
                    }
                    scheduleRefresh()
                }
            },
            onStatus = { connected ->
                scope.launch {
                    val wasConnected = isRealtimeConnected
                    isRealtimeConnected = connected
                    // Setelah tersambung ulang, ambil data terbaru yang mungkin terlewat
                    if (connected && !wasConnected && isLoadedOnce) scheduleRefresh()
                }
            }
        )

        // Cadangan: muat ulang berkala saat koneksi real-time terputus
        pollingJob = scope.launch {
            while (isActive) {
                delay(15_000)
                if (!isRealtimeConnected) refresh(showIndicator = false)
            }
        }
    }

    /** Dipanggil saat dashboard ditutup (logout / pindah layar) */
    fun stop() {
        realtime.disconnect()
        scope.cancel()
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  MEMUAT DATA
    // ═══════════════════════════════════════════════════════════════════════

    /** Tarik-untuk-refresh & muat ulang manual */
    fun refresh(showIndicator: Boolean = true) {
        if (refreshJob?.isActive == true) return
        refreshJob = scope.launch {
            if (showIndicator) isRefreshing = true
            try {
                loadAll()
                loadError = null
            } catch (e: ApiException) {
                loadError = e.message
                if (showIndicator || !isLoadedOnce) notify(e.message)
            } catch (e: Exception) {
                loadError = e.message ?: "Gagal memuat data"
            } finally {
                isRefreshing = false
            }
        }
    }

    /** Gabungkan beberapa event real-time yang datang beruntun menjadi satu kali muat ulang */
    private fun scheduleRefresh() {
        scope.launch {
            delay(300)
            refresh(showIndicator = false)
        }
    }

    private suspend fun loadAll() {
        val tableList: List<TableDto> = ApiClient.get("tables")
        val requestList: List<ServiceRequestDto> = ApiClient.get("requests")
        if (menuCatalog.isEmpty() || !isLoadedOnce) loadMenu()

        tableDtos = tableList
        tables.setAll(tableList.map { it.toModel() })
        requests.setAll(requestList.map { it.toModel() })

        // Pilih meja pertama yang perlu perhatian saat pertama kali dibuka
        if (!isLoadedOnce) {
            val firstActive = tableList.firstOrNull { it.status == "BARU_PESAN" }
                ?: tableList.firstOrNull { it.status != "KOSONG" }
                ?: tableList.firstOrNull()
            firstActive?.let { selectedTableNumber = it.number }
        }
        isLoadedOnce = true
        syncSelectedTable()
    }

    private suspend fun loadMenu() {
        val menus: List<MenuDto> = ApiClient.get("menu")
        menuCatalog.setAll(menus.map { it.toModel() })
    }

    /** Samakan panel detail pesanan dengan meja yang sedang dipilih */
    private fun syncSelectedTable() {
        val dto = tableDtos.firstOrNull { it.number == selectedTableNumber }
        val order = dto?.currentOrder
        selectedTableOccupied = dto != null && dto.status != "KOSONG"
        orderItems.setAll(order?.items?.map { it.toModel() } ?: emptyList())
        paymentStatus = if (order?.paymentBillStatus == "LUNAS") PaymentBillStatus.LUNAS else PaymentBillStatus.BELUM_BAYAR
        selectedPaymentMethod = order?.paymentMethod.toPaymentMethod()
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  AKSI KASIR (dipanggil dari UI)
    // ═══════════════════════════════════════════════════════════════════════

    fun selectTable(number: String) {
        selectedTableNumber = number
        syncSelectedTable()
    }

    /** Konfirmasi pesanan baru → dikirim ke dapur, meja jadi "Belum Bayar" */
    fun confirmNewOrder(tableNumber: String) {
        val order = tableDtos.firstOrNull { it.number == tableNumber }?.currentOrder
            ?: return notify("Meja $tableNumber tidak punya pesanan untuk dikonfirmasi")
        runAction("Pesanan Meja $tableNumber dikonfirmasi & dikirim ke dapur") {
            ApiClient.patchEmpty<OrderDto>("orders/${order.id}/confirm")
        }
    }

    /** Tandai pesanan meja terpilih sudah disajikan / diantar (terpisah dari status bayar) */
    fun markOrderServed() {
        val order = selectedOrder ?: return notify("Meja $selectedTableNumber belum punya pesanan")
        runAction("Pesanan Meja $selectedTableNumber ditandai sudah disajikan") {
            ApiClient.patchEmpty<OrderDto>("orders/${order.id}/serve")
        }
    }

    /** Ubah status bayar (Lunas / Belum Bayar) dengan metode Cash / Transfer */
    fun updatePaymentStatus(newStatus: PaymentBillStatus, method: PaymentMethod = PaymentMethod.BELUM_DIPILIH) {
        val order = selectedOrder ?: return notify("Meja $selectedTableNumber belum punya pesanan")
        val finalMethod = if (method == PaymentMethod.BELUM_DIPILIH) PaymentMethod.CASH else method
        if (newStatus == PaymentBillStatus.LUNAS && order.status == "MENUNGGU_KONFIRMASI") {
            return notify("Konfirmasi pesanan terlebih dahulu sebelum menandai lunas")
        }
        val label = if (newStatus == PaymentBillStatus.LUNAS) "Lunas (${finalMethod.label})" else "Belum Bayar"
        runAction("Meja $selectedTableNumber: $label") {
            ApiClient.patch<OrderDto, PaymentBody>(
                "orders/${order.id}/payment",
                PaymentBody(paymentMethod = finalMethod.name, paymentBillStatus = newStatus.name)
            )
        }
    }

    /** Switch okupansi manual: tamu duduk tanpa pesanan ↔ meja kosong */
    fun toggleOccupancy() {
        val table = tableDtos.firstOrNull { it.number == selectedTableNumber } ?: return
        val target = if (table.status == "KOSONG") "TERISI_BELUM_BAYAR" else "KOSONG"
        runAction(if (target == "KOSONG") "Meja ${table.number} dikosongkan" else "Meja ${table.number} ditandai terisi") {
            ApiClient.patch<TableDto, TableStatusBody>("tables/${table.id}/status", TableStatusBody(target))
        }
    }

    /** Ganti meja: pindahkan pesanan ke meja kosong */
    fun switchTableTo(targetTableNumber: String) {
        val from = tableDtos.firstOrNull { it.number == selectedTableNumber } ?: return
        val to = tableDtos.firstOrNull { it.number == targetTableNumber } ?: return
        showChangeTableDialog = false
        runAction("Pesanan dipindahkan ke Meja $targetTableNumber") {
            ApiClient.post<kotlinx.serialization.json.JsonElement, MoveTableBody>(
                "tables/${from.id}/move",
                MoveTableBody(to.id)
            )
            selectedTableNumber = targetTableNumber
        }
    }

    /** Kosongkan meja (server menolak jika belum lunas) */
    fun clearTable() {
        val table = tableDtos.firstOrNull { it.number == selectedTableNumber } ?: return
        runAction("Meja ${table.number} telah dikosongkan") {
            ApiClient.postEmpty<TableDto>("tables/${table.id}/clear")
        }
    }

    fun incrementQuantity(itemId: String) = changeQuantity(itemId, +1)

    fun decrementQuantity(itemId: String) = changeQuantity(itemId, -1)

    private fun changeQuantity(itemId: String, delta: Int) {
        val order = selectedOrder ?: return
        val item = order.items.firstOrNull { it.id == itemId } ?: return
        val newQty = item.quantity + delta
        if (newQty <= 0 && order.items.size == 1) {
            return notify("Item terakhir tidak bisa dihapus. Kosongkan/batalkan meja bila perlu.")
        }
        runAction(null) {
            ApiClient.patch<OrderDto, UpdateItemBody>(
                "orders/${order.id}/items/$itemId",
                UpdateItemBody(newQty.coerceAtLeast(0))
            )
        }
    }

    /** Tambah menu ke pesanan meja terpilih (membuat pesanan baru jika meja belum punya) */
    fun addMenuItemToOrder(menuItem: MenuItemModel) {
        if (!menuItem.isAvailable) return notify("\"${menuItem.name}\" sedang habis")
        val order = selectedOrder
        val item = NewItemBody(menuItemId = menuItem.id, quantity = 1)
        runAction("${menuItem.name} ditambahkan ke Meja $selectedTableNumber") {
            if (order != null) {
                ApiClient.post<OrderDto, AddItemsBody>("orders/${order.id}/items", AddItemsBody(listOf(item)))
            } else {
                ApiClient.post<OrderDto, KasirOrderBody>(
                    "orders/kasir",
                    KasirOrderBody(tableNumber = selectedTableNumber, items = listOf(item))
                )
            }
        }
    }

    /** Konfirmasi permintaan bantuan & kirim balasan ke layar pelanggan */
    fun confirmRequest(requestId: String, responseMsg: String = "Staf sedang menuju meja Anda") {
        val req = requests.firstOrNull { it.id == requestId } ?: return
        // Permintaan yang sudah dikonfirmasi -> tombol utama berarti "Selesai"
        if (req.status == RequestStatus.DIKONFIRMASI) return dismissRequest(requestId)
        runAction("Balasan dikirim ke ${req.tableNumber}") {
            ApiClient.patch<ServiceRequestDto, RespondBody>(
                "requests/$requestId/respond",
                RespondBody(responseMsg.ifBlank { "Staf sedang menuju meja Anda" })
            )
        }
    }

    /** Tandai permintaan selesai (hilang dari daftar) */
    fun dismissRequest(requestId: String) {
        runAction(null) {
            ApiClient.patchEmpty<ServiceRequestDto>("requests/$requestId/done")
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  HELPER
    // ═══════════════════════════════════════════════════════════════════════

    /** Jalankan aksi ke server, tampilkan pesan, lalu muat ulang data */
    private fun runAction(successMessage: String?, block: suspend () -> Unit) {
        if (isBusy) return
        scope.launch {
            isBusy = true
            try {
                block()
                successMessage?.let { notify(it) }
                loadAll()
            } catch (e: ApiException) {
                notify(e.message)
                runCatching { loadAll() }
            } catch (e: Exception) {
                notify("Gagal: ${e.message ?: "terjadi kesalahan"}")
            } finally {
                isBusy = false
            }
        }
    }

    private fun notify(text: String) {
        messageCounter++
        message = UiMessage(messageCounter, text)
    }

    private fun <T> MutableList<T>.setAll(items: List<T>) {
        clear()
        addAll(items)
    }

    // ─── Konversi data server → model UI ──────────────────────────────────

    private fun TableDto.toModel(): TableModel {
        val tableStatus = runCatching { TableStatus.valueOf(status) }.getOrDefault(TableStatus.KOSONG)
        val since = activeSince?.let { parseInstant(it) }
        val order = currentOrder
        return TableModel(
            id = id,
            number = number,
            status = tableStatus,
            subtitle = when (tableStatus) {
                TableStatus.KOSONG -> ""
                TableStatus.BARU_PESAN -> "Pesanan Baru"
                TableStatus.TERISI_BELUM_BAYAR -> "Belum Bayar"
                TableStatus.TERISI_LUNAS -> "Lunas"
            },
            activeSince = since?.let { formatClock(it) } ?: "-",
            minutesAgo = since?.let { minutesSince(it) } ?: 0,
            paymentMethod = order?.paymentMethod.toPaymentMethod(),
            orderSource = if (order?.orderSource == "KASIR") OrderSource.KASIR else OrderSource.EMENU
        )
    }

    private fun ServiceRequestDto.toModel(): TableRequest {
        val (label, badge) = when (type) {
            "MINTA_BILL" -> "Minta Bill" to RequestBadgeType.MINTA_BILL_TUNAI
            "MINTA_ALAT_MAKAN" -> "Alat Makan" to RequestBadgeType.PANGGIL_WAITER
            "PANGGIL_PELAYAN" -> "Panggil Waiter" to RequestBadgeType.PANGGIL_WAITER
            else -> "Permintaan" to RequestBadgeType.TAMBAH_PESANAN
        }
        val confirmed = status == "DIKONFIRMASI"
        return TableRequest(
            id = id,
            tableNumber = "Meja $tableNumber",
            badgeLabel = label,
            badgeType = badge,
            timeAgo = createdAt?.let { parseInstant(it) }?.let { timeAgoText(it) } ?: "",
            note = message ?: "-",
            status = if (confirmed) RequestStatus.DIKONFIRMASI else RequestStatus.MENUNGGU,
            responseMessage = responseMessage ?: "Staf segera menuju meja Anda",
            primaryActionText = if (confirmed) "Selesai" else "Konfirmasi",
            secondaryActionText = if (confirmed) "Tutup" else "Selesai"
        )
    }

    private fun MenuDto.toModel() = MenuItemModel(
        id = id,
        name = name,
        category = categoryName.toMenuCategory(),
        price = price,
        imageResName = imageResFor(image),
        description = description,
        isAvailable = isAvailable,
        filters = filters
    )

    private fun com.cafe3rasa.kasir.data.OrderItemDto.toModel(): OrderItemModel {
        val menu = menuCatalog.firstOrNull { it.id == menuItemId }
        return OrderItemModel(
            id = id,
            name = name,
            category = categoryName.toMenuCategory(),
            unitPrice = unitPrice,
            quantity = quantity,
            note = note,
            imageResName = menu?.imageResName
        )
    }

    private fun String?.toPaymentMethod(): PaymentMethod = when (this) {
        "CASH" -> PaymentMethod.CASH
        "TRANSFER" -> PaymentMethod.TRANSFER
        else -> PaymentMethod.BELUM_DIPILIH
    }

    private fun String.toMenuCategory(): MenuCategory = when (lowercase()) {
        "makanan" -> MenuCategory.MAKANAN
        "minuman" -> MenuCategory.MINUMAN
        "camilan" -> MenuCategory.CAMILAN
        else -> MenuCategory.MAKANAN
    }

    /** "img/nasi-campur.jpg" (path web) → "nasi_campur" (nama gambar di aplikasi) */
    private fun imageResFor(image: String?): String? {
        val file = image?.substringAfterLast('/')?.substringBeforeLast('.')?.lowercase() ?: return null
        return when (file) {
            "nasi-campur" -> "nasi_campur"
            "kopi-susu" -> "kopi_susu"
            "pisang-goreng" -> "pisang_goreng"
            "bakmi", "bakmi-ayam" -> "bakmi_ayam"
            "teh-tarik" -> "teh_tarik"
            else -> file.replace('-', '_')
        }
    }

    private fun parseInstant(iso: String): Instant? = runCatching { Instant.parse(iso) }.getOrNull()

    private fun formatClock(instant: Instant): String {
        val t = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${t.hour.toString().padStart(2, '0')}:${t.minute.toString().padStart(2, '0')}"
    }

    private fun minutesSince(instant: Instant): Int =
        ((Clock.System.now() - instant).inWholeMinutes).toInt().coerceAtLeast(0)

    private fun timeAgoText(instant: Instant): String {
        val m = minutesSince(instant)
        return when {
            m < 1 -> "Baru saja"
            m < 60 -> "$m mnt lalu"
            else -> "${m / 60} jam lalu"
        }
    }
}

fun formatRupiah(amount: Long): String {
    val str = amount.toString()
    val formatted = StringBuilder()
    var count = 0
    for (i in str.length - 1 downTo 0) {
        formatted.append(str[i])
        count++
        if (count % 3 == 0 && i > 0) {
            formatted.append('.')
        }
    }
    return "Rp ${formatted.reverse()}"
}
