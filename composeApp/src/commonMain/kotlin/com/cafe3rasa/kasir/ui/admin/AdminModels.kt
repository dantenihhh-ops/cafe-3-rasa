package com.cafe3rasa.kasir.ui.admin

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import com.cafe3rasa.kasir.theme.KasirColors
import org.jetbrains.compose.resources.DrawableResource
import pbl.composeapp.generated.resources.Res
import pbl.composeapp.generated.resources.bakmi_ayam
import pbl.composeapp.generated.resources.kopi_susu
import pbl.composeapp.generated.resources.nasi_campur
import pbl.composeapp.generated.resources.pisang_goreng
import pbl.composeapp.generated.resources.teh_tarik

data class AdminProduct(
    val id: Int,
    val name: String,
    val category: String,
    val price: Long,
    val description: String,
    val image: DrawableResource? = null,
    val available: Boolean = true
)

data class DiningTable(
    val number: Int,
    val area: String,
    val capacity: Int,
    val status: TableStatus
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
    val status: OrderStatus
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

/**
 * Penyimpanan data state di memori untuk Admin Panel.
 */
class AdminStore {
    val categories = mutableStateListOf("Makanan", "Minuman", "Camilan")

    val products = mutableStateListOf(
        AdminProduct(1, "Nasi Campur 3 Rasa Spesial", "Makanan", 45_000, "Ayam rempah Madura, cumi hitam pedas, sambal matah", Res.drawable.nasi_campur),
        AdminProduct(2, "Bakmi Ayam Jamur Maharewa", "Makanan", 32_000, "Mie kenyal, kuah kaldu ayam kampung & pangsit", Res.drawable.bakmi_ayam),
        AdminProduct(3, "Kopi Susu Gula Aren 3 Rasa", "Minuman", 22_000, "Espresso, susu segar, gula aren organik", Res.drawable.kopi_susu),
        AdminProduct(4, "Teh Tarik Rempah Hangat", "Minuman", 16_000, "Teh hitam, kapulaga, cengkeh & susu kental manis", Res.drawable.teh_tarik),
        AdminProduct(5, "Pisang Goreng Wijen Karamel", "Camilan", 18_000, "Pisang kepok renyah, wijen & karamel gula aren", Res.drawable.pisang_goreng),
        AdminProduct(6, "Kentang Goreng Rasa Keju", "Camilan", 20_000, "Saus tartar & sambal", null, available = false)
    )
    private var nextProductId = 7

    val tables = mutableStateListOf(
        DiningTable(1, "Area Utama", 4, TableStatus.TERISI),
        DiningTable(2, "Area Utama", 4, TableStatus.TERISI),
        DiningTable(3, "Area Utama", 2, TableStatus.KOSONG),
        DiningTable(4, "Area Utama", 4, TableStatus.TERISI),
        DiningTable(5, "Area Utama", 6, TableStatus.KOSONG),
        DiningTable(6, "Area Utama", 4, TableStatus.TERISI),
        DiningTable(7, "Selasar", 2, TableStatus.KOSONG),
        DiningTable(8, "Selasar", 4, TableStatus.KOSONG)
    )

    val orders = listOf(
        OrderLog("#TR-04-8921", "12:40", "Meja 04", "Nasi Campur 3 Rasa Spesial, Kopi Susu Gula Aren, Pisang Goreng Wijen", 93_500, "Cash", OrderStatus.DIPROSES),
        OrderLog("#TR-01-8920", "12:31", "Meja 01", "Nasi Campur 3 Rasa Spesial (2), Pisang Goreng Wijen Karamel", 118_800, "Cash", OrderStatus.DIPROSES),
        OrderLog("#TR-06-8919", "12:18", "Meja 06", "Bakmi Ayam Jamur Maharewa, Teh Tarik Rempah Hangat", 52_800, "Transfer", OrderStatus.SELESAI),
        OrderLog("#TR-02-8918", "12:05", "Meja 02", "Kopi Susu Gula Aren 3 Rasa (3)", 72_600, "Cash", OrderStatus.SELESAI),
        OrderLog("#TR-07-8917", "11:52", "Meja 07", "Teh Tarik Rempah Hangat (2), Kentang Goreng Rasa Keju", 57_200, "Transfer", OrderStatus.SELESAI),
        OrderLog("#TR-03-8916", "11:40", "Meja 03", "Bakmi Ayam Jamur Maharewa", 35_200, "Cash", OrderStatus.DIBATALKAN),
        OrderLog("#TR-05-8915", "11:22", "Meja 05", "Nasi Campur 3 Rasa Spesial, Kopi Susu Gula Aren 3 Rasa", 73_700, "Transfer", OrderStatus.SELESAI)
    )

    fun countIn(category: String) = products.count { it.category == category }

    fun addProduct(name: String, category: String, price: Long, description: String) {
        products.add(0, AdminProduct(nextProductId++, name, category, price, description.ifBlank { "Produk baru" }))
    }

    fun toggleAvailability(id: Int) {
        val i = products.indexOfFirst { it.id == id }
        if (i >= 0) products[i] = products[i].copy(available = !products[i].available)
    }

    fun removeProduct(id: Int) { products.removeAll { it.id == id } }

    /** @return false bila nama kategori kosong/sudah ada. */
    fun addCategory(name: String): Boolean {
        val clean = name.trim()
        if (clean.isEmpty() || categories.any { it.equals(clean, ignoreCase = true) }) return false
        categories.add(clean)
        return true
    }

    /** Kategori yang masih berisi produk tidak bisa dihapus. */
    fun removeCategory(name: String) {
        if (countIn(name) == 0) categories.remove(name)
    }

    /** @return pesan error, atau null bila berhasil. */
    fun addTable(number: Int, area: String, capacity: Int): String? {
        if (number <= 0) return "Isi nomor meja terlebih dulu."
        if (tables.any { it.number == number }) return "Meja ${tableNo(number)} sudah terdaftar."
        tables.add(DiningTable(number, area, capacity, TableStatus.BARU))
        tables.sortBy { it.number }
        return null
    }
}
