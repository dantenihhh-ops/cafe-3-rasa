package com.cafe3rasa.kasir.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cafe3rasa.kasir.model.*

enum class TableFilter(val label: String) {
    SEMUA("Semua (8)"),
    BARU_PESAN("Baru Pesan"),
    TERISI("Terisi"),
    PERLU_BILL("Perlu Bill")
}

enum class MobileNavigationTab(val label: String) {
    TABLE_MAP("Peta Meja"),
    ORDER_DETAIL("Pesanan Meja"),
    QUICK_MENU("Tambah Menu")
}

class CashierDashboardState {
    var selectedTableNumber by mutableStateOf("01")
    var selectedTableOccupied by mutableStateOf(true)
    var activeTableFilter by mutableStateOf(TableFilter.SEMUA)
    var activeMenuCategory by mutableStateOf(MenuCategory.SEMUA)
    var activeTagFilter by mutableStateOf<String?>("Semua")
    var menuSearchQuery by mutableStateOf("")
    val availableTags = listOf("Semua", "Best Seller", "Terlaris", "Rekomendasi")
    var paymentStatus by mutableStateOf(PaymentBillStatus.BELUM_BAYAR)
    var selectedPaymentMethod by mutableStateOf(PaymentMethod.BELUM_DIPILIH)
    var showPaymentMethodDialog by mutableStateOf(false)
    var mobileTab by mutableStateOf(MobileNavigationTab.ORDER_DETAIL)
    var showChangeTableDialog by mutableStateOf(false)

    val tables = mutableStateListOf(
        // Meja 01: Baru pesan dari e-menu (dot berkedip kuning)
        TableModel(1, "01", TableStatus.BARU_PESAN, "Pesanan Baru", "09:25 WIB", 2, orderSource = OrderSource.EMENU),
        // Meja 02: Terisi, belum bayar
        TableModel(2, "02", TableStatus.TERISI_BELUM_BAYAR, "Belum Bayar", "09:10 WIB", 17),
        // Meja 03: Kosong
        TableModel(3, "03", TableStatus.KOSONG, "", "09:25 WIB", 0),
        // Meja 04: Terisi, belum bayar
        TableModel(4, "04", TableStatus.TERISI_BELUM_BAYAR, "Belum Bayar", "08:55 WIB", 32),
        // Meja 05: Kosong
        TableModel(5, "05", TableStatus.KOSONG, "", "09:25 WIB", 0),
        // Meja 06: Lunas via QRIS
        TableModel(6, "06", TableStatus.TERISI_LUNAS, "Lunas", "08:45 WIB", 42, paymentMethod = PaymentMethod.QRIS),
        // Meja 07: Baru pesan dari kasir (dot berkedip kuning)
        TableModel(7, "07", TableStatus.BARU_PESAN, "Pesanan Baru", "09:38 WIB", 0, orderSource = OrderSource.KASIR),
        // Meja 08: Kosong
        TableModel(8, "08", TableStatus.KOSONG, "", "09:25 WIB", 0)
    )

    val requests = mutableStateListOf(
        TableRequest(
            id = "req_1",
            tableNumber = "Meja 04",
            badgeLabel = "Minta Bill Tunai",
            badgeType = RequestBadgeType.MINTA_BILL_TUNAI,
            timeAgo = "2 mnt lalu",
            note = "Minta struk split bill & siapkan kembalian Rp 50.000",
            primaryActionText = "Konfirmasi"
        ),
        TableRequest(
            id = "req_2",
            tableNumber = "Meja 02",
            badgeLabel = "Panggil Waiter",
            badgeType = RequestBadgeType.PANGGIL_WAITER,
            timeAgo = "Baru saja",
            note = "Minta sendok, garpu tambahan & sambal matah ekstra",
            primaryActionText = "Konfirmasi"
        ),
        TableRequest(
            id = "req_3",
            tableNumber = "Meja 05",
            badgeLabel = "Panggil Waiter",
            badgeType = RequestBadgeType.PANGGIL_WAITER,
            timeAgo = "4 mnt lalu",
            note = "Minta bantuan colokan listrik / stopkontak di area selasar",
            primaryActionText = "Konfirmasi"
        ),
        TableRequest(
            id = "req_4",
            tableNumber = "Meja 01",
            badgeLabel = "Peringatan Dapur",
            badgeType = RequestBadgeType.PERINGATAN_DAPUR,
            timeAgo = "16 mnt lalu",
            note = "Pesanan melebihi estimasi 15 menit — konfirmasi ke dapur",
            primaryActionText = "Konfirmasi",
            secondaryActionText = "Cek Dapur",
            isAlertDapur = true
        ),
        TableRequest(
            id = "req_5",
            tableNumber = "Meja 07",
            badgeLabel = "Tambah Pesanan",
            badgeType = RequestBadgeType.TAMBAH_PESANAN,
            timeAgo = "18 mnt lalu",
            note = "Minta menu fisik tambahan & rekomendasi kopi hangat",
            primaryActionText = "Konfirmasi"
        )
    )

    val orderItems = mutableStateListOf(
        OrderItemModel(
            id = "ord_1",
            name = "Nasi Campur 3 Rasa Spesial",
            category = MenuCategory.MAKANAN,
            unitPrice = 45000,
            quantity = 2,
            note = "Tidak pedas, sambal matah dipisah",
            imageResName = "nasi_campur"
        ),
        OrderItemModel(
            id = "ord_2",
            name = "Pisang Goreng Wijen Karamel",
            category = MenuCategory.CAMILAN,
            unitPrice = 18000,
            quantity = 1,
            note = "Wijen agak banyak, karamel dipisah",
            imageResName = "pisang_goreng"
        )
    )

    val menuCatalog = listOf(
        MenuItemModel("menu_1", "Nasi Campur 3 Rasa", MenuCategory.MAKANAN, 45000, "nasi_campur", filters = listOf("Terlaris", "Best Seller")),
        MenuItemModel("menu_2", "Bakmi Ayam Jamur Maharewa", MenuCategory.MAKANAN, 32000, "bakmi_ayam", filters = listOf("Rekomendasi", "Best Seller")),
        MenuItemModel("menu_3", "Kopi Susu Gula Aren 3 Rasa", MenuCategory.MINUMAN, 22000, "kopi_susu", filters = listOf("Best Seller", "Terlaris")),
        MenuItemModel("menu_4", "Pisang Goreng Wijen Karamel", MenuCategory.CAMILAN, 18000, "pisang_goreng", filters = listOf("Best Seller")),
        MenuItemModel("menu_5", "Teh Tarik Rempah", MenuCategory.MINUMAN, 16000, null, filters = listOf("Rekomendasi"))
    )

    val totalItemCount: Int
        get() = orderItems.sumOf { it.quantity }

    val totalRupiah: Long
        get() = orderItems.sumOf { it.subtotal }

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
        get() = tables.filter { it.status == TableStatus.KOSONG }

    fun selectTable(number: String) {
        selectedTableNumber = number
        val table = tables.firstOrNull { it.number == number }
        selectedTableOccupied = table?.status != TableStatus.KOSONG
        // Sync payment method state
        selectedPaymentMethod = table?.paymentMethod ?: PaymentMethod.BELUM_DIPILIH
        paymentStatus = when (table?.status) {
            TableStatus.TERISI_LUNAS -> PaymentBillStatus.LUNAS
            else -> PaymentBillStatus.BELUM_BAYAR
        }
    }

    /** Kasir konfirmasi pesanan baru → ubah dari BARU_PESAN ke TERISI_BELUM_BAYAR */
    fun confirmNewOrder(tableNumber: String) {
        val index = tables.indexOfFirst { it.number == tableNumber }
        if (index != -1 && tables[index].status == TableStatus.BARU_PESAN) {
            tables[index] = tables[index].copy(status = TableStatus.TERISI_BELUM_BAYAR)
            if (selectedTableNumber == tableNumber) {
                selectedTableOccupied = true
                paymentStatus = PaymentBillStatus.BELUM_BAYAR
            }
        }
    }

    /** Kasir mengubah status pembayaran meja */
    fun updatePaymentStatus(newStatus: PaymentBillStatus, method: PaymentMethod = PaymentMethod.BELUM_DIPILIH) {
        paymentStatus = newStatus
        val index = tables.indexOfFirst { it.number == selectedTableNumber }
        if (index != -1) {
            val newTableStatus = when (newStatus) {
                PaymentBillStatus.LUNAS -> TableStatus.TERISI_LUNAS
                PaymentBillStatus.BELUM_BAYAR -> TableStatus.TERISI_BELUM_BAYAR
            }
            tables[index] = tables[index].copy(
                status = newTableStatus,
                paymentMethod = if (newStatus == PaymentBillStatus.LUNAS) method else PaymentMethod.BELUM_DIPILIH
            )
            if (newStatus == PaymentBillStatus.LUNAS) {
                selectedPaymentMethod = method
            }
        }
    }

    fun toggleOccupancy() {
        selectedTableOccupied = !selectedTableOccupied
        val index = tables.indexOfFirst { it.number == selectedTableNumber }
        if (index != -1) {
            tables[index] = tables[index].copy(
                status = if (selectedTableOccupied) TableStatus.TERISI_BELUM_BAYAR else TableStatus.KOSONG,
                activeSince = if (selectedTableOccupied) "09:40 WIB" else "09:25 WIB",
                minutesAgo = if (selectedTableOccupied) 1 else 0
            )
        }
    }

    fun switchTableTo(targetTableNumber: String) {
        val oldNumber = selectedTableNumber
        val oldIndex = tables.indexOfFirst { it.number == oldNumber }
        val newIndex = tables.indexOfFirst { it.number == targetTableNumber }

        if (newIndex != -1) {
            if (oldIndex != -1) {
                tables[oldIndex] = tables[oldIndex].copy(
                    status = TableStatus.KOSONG,
                    activeSince = "09:25 WIB",
                    minutesAgo = 0
                )
            }
            tables[newIndex] = tables[newIndex].copy(
                status = TableStatus.TERISI_BELUM_BAYAR,
                activeSince = "09:40 WIB",
                minutesAgo = 1
            )
            selectedTableNumber = targetTableNumber
            selectedTableOccupied = true
            showChangeTableDialog = false
        }
    }

    fun clearTable() {
        selectedTableOccupied = false
        val index = tables.indexOfFirst { it.number == selectedTableNumber }
        if (index != -1) {
            tables[index] = tables[index].copy(
                status = TableStatus.KOSONG,
                activeSince = "09:25 WIB",
                minutesAgo = 0,
                paymentMethod = PaymentMethod.BELUM_DIPILIH
            )
        }
        orderItems.clear()
        paymentStatus = PaymentBillStatus.BELUM_BAYAR
        selectedPaymentMethod = PaymentMethod.BELUM_DIPILIH
    }

    fun incrementQuantity(itemId: String) {
        val index = orderItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            val item = orderItems[index]
            orderItems[index] = item.copy(quantity = item.quantity + 1)
        }
    }

    fun decrementQuantity(itemId: String) {
        val index = orderItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            val item = orderItems[index]
            if (item.quantity > 1) {
                orderItems[index] = item.copy(quantity = item.quantity - 1)
            } else {
                orderItems.removeAt(index)
            }
        }
    }

    fun addMenuItemToOrder(menuItem: MenuItemModel) {
        val existingIndex = orderItems.indexOfFirst { it.name.startsWith(menuItem.name.take(15)) }
        if (existingIndex != -1) {
            val existing = orderItems[existingIndex]
            orderItems[existingIndex] = existing.copy(quantity = existing.quantity + 1)
        } else {
            orderItems.add(
                OrderItemModel(
                    id = "ord_${System.currentTimeMillis()}_${menuItem.id}",
                    name = menuItem.name,
                    category = menuItem.category,
                    unitPrice = menuItem.price,
                    quantity = 1,
                    note = null,
                    imageResName = menuItem.imageResName
                )
            )
        }
    }

    fun confirmRequest(requestId: String, responseMsg: String = "Staf sedang menuju meja Anda") {
        val idx = requests.indexOfFirst { it.id == requestId }
        if (idx >= 0) {
            val req = requests[idx]
            requests[idx] = req.copy(
                status = RequestStatus.DIKONFIRMASI,
                responseMessage = responseMsg,
                primaryActionText = "Selesai",
                secondaryActionText = "Tutup"
            )
        }
    }

    fun dismissRequest(requestId: String) {
        requests.removeAll { it.id == requestId }
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