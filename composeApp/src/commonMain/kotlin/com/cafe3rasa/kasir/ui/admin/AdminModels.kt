package com.cafe3rasa.kasir.ui.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.cafe3rasa.kasir.data.ApiClient
import com.cafe3rasa.kasir.data.ApiException
import com.cafe3rasa.kasir.data.CategoryBody
import com.cafe3rasa.kasir.data.CategoryDto
import com.cafe3rasa.kasir.data.ImageUploadBody
import com.cafe3rasa.kasir.data.MenuUpdateBody
import com.cafe3rasa.kasir.data.ReviewsDto
import com.cafe3rasa.kasir.data.MenuBody
import com.cafe3rasa.kasir.data.MenuDto
import com.cafe3rasa.kasir.data.OrderDto
import com.cafe3rasa.kasir.data.ReportDto
import com.cafe3rasa.kasir.data.TableBody
import com.cafe3rasa.kasir.data.TableDto
import com.cafe3rasa.kasir.theme.KasirColors
import com.cafe3rasa.kasir.ui.components.invalidateRemoteImage
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.JsonElement
import org.jetbrains.compose.resources.DrawableResource
import pbl.composeapp.generated.resources.Res
import pbl.composeapp.generated.resources.bakmi_ayam
import pbl.composeapp.generated.resources.kopi_susu
import pbl.composeapp.generated.resources.nasi_campur
import pbl.composeapp.generated.resources.pisang_goreng
import pbl.composeapp.generated.resources.teh_tarik

data class AdminProduct(
    val id: String,
    val name: String,
    val category: String,
    val price: Long,
    val description: String,
    val image: DrawableResource? = null,
    val available: Boolean = true,
    val filters: List<String> = emptyList(), // Filter opsional: Best Seller, Terlaris, Rekomendasi, dll.
    val imagePath: String? = null            // path gambar di web pelanggan, mis. img/nasi-campur.jpg
)

data class DiningTable(
    val number: Int,
    val area: String,
    val capacity: Int,
    val status: TableStatus,
    val id: Int = 0                           // ID meja di database
)

enum class TableStatus(val label: String) {
    TERISI("TERISI"),
    KOSONG("KOSONG"),
    BARU("BARU")
}

enum class OrderStatus(val label: String) {
    SELESAI("Selesai"),
    DIPROSES("Diproses"),
    DIBATALKAN("Dibatalkan")
}

data class OrderLog(
    val id: String,
    val time: String,
    val table: String,
    val items: String,
    val total: Long,
    val payment: String,   // "Cash" atau "Transfer"
    val status: OrderStatus,
    val date: String = ""  // tanggal pesanan, mis. "25 Nov"
)

/** Format angka murni KMP ke "Rp 45.000" */
fun rupiah(value: Long): String {
    val s = value.toString()
    val sb = StringBuilder()
    var count = 0
    for (i in s.length - 1 downTo 0) {
        sb.append(s[i])
        count++
        if (count % 3 == 0 && i > 0) {
            sb.append('.')
        }
    }
    return "Rp " + sb.reverse().toString()
}

fun tableNo(n: Int): String = n.toString().padStart(2, '0')

fun categoryColor(category: String): Color = when (category) {
    "Makanan" -> KasirColors.PrimaryTeal
    "Minuman" -> KasirColors.AccentRust
    "Camilan" -> Color(0xFFA04028)
    else -> KasirColors.PrimaryDarkNavy
}

/** Periode laporan (dipakai LaporanScreen). */
enum class Period(val label: String) { HARIAN("Harian"), BULANAN("Bulanan"), TAHUNAN("Tahunan") }

/**
 * Data Panel Admin yang terhubung ke backend.
 * Semua perubahan dikirim ke server, lalu daftar dimuat ulang dari database.
 * Hasil / error ditampilkan lewat [message] (snackbar di AdminScreen).
 */
class AdminStore {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var started = false
    private var messageCounter = 0L

    var isLoading by mutableStateOf(false)
        private set
    var loadError by mutableStateOf<String?>(null)
        private set
    var message by mutableStateOf<AdminMessage?>(null)
        private set

    // Kategori (nama) — urutan dari server; id disimpan terpisah
    val categories = mutableStateListOf<String>()
    private val categoryIdByName = mutableMapOf<String, String>()

    // Filter opsional yang dapat dipilih / ditambahkan oleh admin
    val availableFilters = mutableStateListOf("Best Seller", "Terlaris", "Rekomendasi", "Spesial")

    val products = mutableStateListOf<AdminProduct>()
    val tables = mutableStateListOf<DiningTable>()

    /** Laporan per periode + geseran (0 = sekarang, -1 = sebelumnya, ...) */
    val reports = mutableStateMapOf<Pair<Period, Int>, ReportDto>()
    var reportLoading by mutableStateOf(false)
        private set

    /** Rating & ulasan pelanggan (Pusat Informasi & Ulasan) */
    var reviews by mutableStateOf<ReviewsDto?>(null)
        private set
    var reviewsLoading by mutableStateOf(false)
        private set

    // ═══ Siklus hidup ════════════════════════════════════════════════════

    fun start() {
        if (started) return
        started = true
        refreshAll()
    }

    fun stop() = scope.cancel()

    fun refreshAll() {
        scope.launch {
            isLoading = true
            try {
                coroutineScope {
                    val c = async { loadCategories() }
                    val p = async { loadProducts() }
                    val t = async { loadTables() }
                    c.await(); p.await(); t.await()
                }
                loadError = null
            } catch (e: ApiException) {
                loadError = e.message
                notify(e.message)
            } catch (e: Exception) {
                loadError = e.message ?: "Gagal memuat data"
            } finally {
                isLoading = false
            }
        }
        loadReport(Period.HARIAN, 0)
    }

    // ═══ Pemuatan data ═══════════════════════════════════════════════════

    private suspend fun loadCategories() {
        val list: List<CategoryDto> = ApiClient.get("categories")
        categoryIdByName.clear()
        list.forEach { categoryIdByName[it.name] = it.id }
        categories.setAll(list.map { it.name })
    }

    private suspend fun loadProducts() {
        val list: List<MenuDto> = ApiClient.get("menu")
        products.setAll(list.map { it.toAdmin() })
        // Label filter dari produk yang sudah ada ikut ditampilkan sebagai pilihan
        list.flatMap { it.filters }.distinct().forEach { f ->
            if (availableFilters.none { it.equals(f, ignoreCase = true) }) availableFilters.add(f)
        }
    }

    private suspend fun loadTables() {
        val list: List<TableDto> = ApiClient.get("tables")
        tables.setAll(
            list.mapNotNull { t ->
                val n = t.number.toIntOrNull() ?: return@mapNotNull null
                DiningTable(
                    number = n,
                    area = t.area,
                    capacity = t.capacity,
                    status = when (t.status) {
                        "KOSONG" -> TableStatus.KOSONG
                        "BARU_PESAN" -> TableStatus.BARU
                        else -> TableStatus.TERISI
                    },
                    id = t.id
                )
            }.sortedBy { it.number }
        )
    }


    /** Muat laporan periode tertentu; [offset] 0 = periode berjalan, -1 = sebelumnya, dst. */
    fun loadReport(period: Period, offset: Int = 0) {
        scope.launch {
            reportLoading = true
            try {
                val r: ReportDto = ApiClient.get("reports/summary?period=${period.name}&offset=$offset")
                reports[period to offset] = r
            } catch (e: ApiException) {
                notify("Laporan: ${e.message}")
            } catch (_: Exception) {
            } finally {
                reportLoading = false
            }
        }
    }

    /** Log pesanan dari laporan (untuk tabel Log Pesanan & CSV) */
    fun orderLogsOf(report: ReportDto?): List<OrderLog> = report?.orders.orEmpty().map { it.toLog() }

    /** Muat rating & ulasan pelanggan dari database */
    fun loadReviews() {
        scope.launch {
            reviewsLoading = true
            try {
                reviews = ApiClient.get<ReviewsDto>("reviews")
            } catch (e: ApiException) {
                notify("Ulasan: ${e.message}")
            } catch (_: Exception) {
                notify("Ulasan gagal dimuat, periksa koneksi")
            } finally {
                reviewsLoading = false
            }
        }
    }

    // ═══ Produk ══════════════════════════════════════════════════════════

    fun countIn(category: String) = products.count { it.category == category }

    fun addProduct(
        name: String,
        category: String,
        price: Long,
        description: String,
        filters: List<String> = emptyList(),
        imageBytes: ByteArray? = null
    ) {
        val categoryId = categoryIdByName[category] ?: return notify("Kategori \"$category\" tidak ditemukan")
        runAction("Produk \"$name\" ditambahkan") {
            val created = ApiClient.post<MenuDto, MenuBody>(
                "menu",
                MenuBody(
                    name = name,
                    categoryId = categoryId,
                    price = price,
                    description = description,
                    filters = filters
                )
            )
            // Foto yang dipilih sebelum menyimpan langsung diunggah ke produk baru
            if (imageBytes != null) uploadImageNow(created.id, imageBytes)
            loadProducts()
            loadCategories()
        }
    }

    /** Ubah nama, kategori, harga, deskripsi, dan filter produk */
    fun updateProduct(id: String, name: String, category: String, price: Long, description: String, filters: List<String>) {
        val categoryId = categoryIdByName[category] ?: return notify("Kategori \"$category\" tidak ditemukan")
        runAction("Produk \"$name\" diperbarui") {
            ApiClient.patch<MenuDto, MenuUpdateBody>(
                "menu/$id",
                MenuUpdateBody(name = name, categoryId = categoryId, price = price, description = description, filters = filters)
            )
            loadProducts()
        }
    }

    fun addCustomFilter(filterName: String): Boolean {
        val clean = filterName.trim()
        if (clean.isBlank() || availableFilters.any { it.equals(clean, ignoreCase = true) }) return false
        availableFilters.add(clean)
        return true
    }

    fun toggleAvailability(id: String) {
        runAction(null) {
            ApiClient.patchEmpty<MenuDto>("menu/$id/toggle")
            loadProducts()
        }
    }

    fun removeProduct(id: String) {
        val name = products.firstOrNull { it.id == id }?.name ?: "Produk"
        runAction("\"$name\" dihapus") {
            ApiClient.delete<JsonElement>("menu/$id")
            loadProducts()
            loadCategories()
        }
    }

    /** Upload / ganti foto produk (bytes JPEG/PNG/WEBP maks 2 MB) */
    fun uploadProductImage(id: String, bytes: ByteArray) {
        val product = products.firstOrNull { it.id == id } ?: return
        runAction("Foto \"${product.name}\" diperbarui") {
            uploadImageNow(id, bytes)
            product.imagePath?.let { invalidateRemoteImage(it) }
            loadProducts()
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private suspend fun uploadImageNow(id: String, bytes: ByteArray) {
        ApiClient.post<MenuDto, ImageUploadBody>("menu/$id/image", ImageUploadBody(Base64.encode(bytes)))
    }

    fun notifyError(text: String) = notify(text)

    // ═══ Kategori ════════════════════════════════════════════════════════

    /** @return false bila nama kategori kosong/sudah ada (cek lokal); hasil server lewat [message]. */
    fun addCategory(name: String): Boolean {
        val clean = name.trim()
        if (clean.isEmpty() || categories.any { it.equals(clean, ignoreCase = true) }) return false
        runAction("Kategori \"$clean\" ditambahkan") {
            ApiClient.post<CategoryDto, CategoryBody>("categories", CategoryBody(clean))
            loadCategories()
        }
        return true
    }

    /** Kategori yang masih berisi produk tidak bisa dihapus (dicek juga oleh server). */
    fun removeCategory(name: String) {
        if (countIn(name) > 0) return notify("Kategori \"$name\" masih berisi produk")
        val id = categoryIdByName[name] ?: return
        runAction("Kategori \"$name\" dihapus") {
            ApiClient.delete<JsonElement>("categories/$id")
            loadCategories()
        }
    }

    // ═══ Meja ════════════════════════════════════════════════════════════

    /** @return pesan error validasi, atau null bila permintaan dikirim ke server. */
    fun addTable(number: Int, area: String, capacity: Int): String? {
        if (number <= 0) return "Isi nomor meja terlebih dulu."
        if (tables.any { it.number == number }) return "Meja ${tableNo(number)} sudah terdaftar."
        runAction("Meja ${tableNo(number)} ditambahkan") {
            ApiClient.post<TableDto, TableBody>("tables", TableBody(tableNo(number), area, capacity))
            loadTables()
        }
        return null
    }

    fun removeTable(number: Int) {
        val t = tables.firstOrNull { it.number == number } ?: return
        runAction("Meja ${tableNo(number)} dihapus") {
            ApiClient.delete<JsonElement>("tables/${t.id}")
            loadTables()
        }
    }

    // ═══ Helper ══════════════════════════════════════════════════════════

    private fun runAction(successMessage: String?, block: suspend () -> Unit) {
        scope.launch {
            try {
                block()
                successMessage?.let { notify(it) }
            } catch (e: ApiException) {
                notify(e.message)
            } catch (e: Exception) {
                notify("Gagal: ${e.message ?: "terjadi kesalahan"}")
            }
        }
    }

    private fun notify(text: String) {
        messageCounter++
        message = AdminMessage(messageCounter, text)
    }

    private fun <T> MutableList<T>.setAll(items: List<T>) {
        if (this == items) return
        clear()
        addAll(items)
    }

    private fun MenuDto.toAdmin() = AdminProduct(
        id = id,
        name = name,
        category = categoryName,
        price = price,
        description = description,
        image = if (image?.startsWith("uploads/") == true) null else drawableFor(image),
        available = isAvailable,
        filters = filters,
        imagePath = image
    )

    fun OrderDto.toLog(): OrderLog {
        val created = createdAt?.let { runCatching { Instant.parse(it) }.getOrNull() }
            ?.toLocalDateTime(TimeZone.of("Asia/Makassar"))
        val time = created?.let { "${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}" } ?: "-"
        val date = created?.let { "${it.dayOfMonth} ${MONTHS[it.monthNumber - 1]}" } ?: ""
        val itemsText = items.joinToString(", ") { if (it.quantity > 1) "${it.name} (${it.quantity})" else it.name }
        val statusLog = when {
            status == "DIBATALKAN" -> OrderStatus.DIBATALKAN
            status == "SELESAI" || paymentBillStatus == "LUNAS" -> OrderStatus.SELESAI
            else -> OrderStatus.DIPROSES
        }
        return OrderLog(
            id = "#$orderNumber",
            time = time,
            table = "Meja $tableNumber",
            items = itemsText,
            total = total,
            payment = when (paymentMethod) {
                "CASH" -> "Cash"
                "TRANSFER" -> "Transfer"
                else -> "-"
            },
            status = statusLog,
            date = date
        )
    }

    companion object {
        private val MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")

        /** "img/nasi-campur.jpg" → gambar bawaan aplikasi */
        fun drawableFor(path: String?): DrawableResource? =
            when (path?.substringAfterLast('/')?.substringBeforeLast('.')?.lowercase()) {
                "nasi-campur" -> Res.drawable.nasi_campur
                "kopi-susu" -> Res.drawable.kopi_susu
                "pisang-goreng" -> Res.drawable.pisang_goreng
                "bakmi", "bakmi-ayam" -> Res.drawable.bakmi_ayam
                "teh-tarik" -> Res.drawable.teh_tarik
                else -> null
            }
    }
}

/** Pesan snackbar Panel Admin (id unik agar pesan sama tetap muncul lagi) */
data class AdminMessage(val id: Long, val text: String)
