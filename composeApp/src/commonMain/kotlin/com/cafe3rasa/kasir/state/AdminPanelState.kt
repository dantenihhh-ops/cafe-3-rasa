package com.cafe3rasa.kasir.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cafe3rasa.kasir.model.*

/** Halaman panel admin (mockup 07-09) + login (05). */
enum class AdminTab(val label: String) {
    PRODUK("Produk & Kategori"),
    LOG_STATISTIK("Log & Statistik"),
    MEJA_QR("Meja & QR"),
    KELUAR("Keluar")
}

/** Periode grafik di halaman Log & Statistik (08). */
enum class StatisticsPeriod(val label: String) {
    HARIAN("Harian"),
    BULANAN("Bulanan"),
    TAHUNAN("Tahunan")
}

enum class LogStatusFilter(val label: String) {
    SEMUA("Semua"),
    SELESAI("Selesai"),
    DIPROSES("Diproses"),
    DIBATALKAN("Dibatalkan")
}

enum class TableArea(val label: String) {
    AREA_UTAMA("Area Utama"),
    SELASAR("Selasar"),
    LANTAI_2("Lantai 2")
}

data class AdminTableRow(
    val number: String,
    val area: TableArea,
    val seats: Int,
    val isOccupied: Boolean
)

/** Bar chart "Pendapatan per Jam" (08). */
data class HourlyRevenue(val hour: String, val revenue: Long)

class AdminPanelState {
    var isLoggedIn by mutableStateOf(false)
    var activeTab by mutableStateOf(AdminTab.PRODUK)

    // ---------- 05 Login ----------
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var isPasswordVisible by mutableStateOf(false)
    var rememberMe by mutableStateOf(true)
    var loginError by mutableStateOf<String?>(null)

    // ---------- 07 Produk ----------
    var productSearch by mutableStateOf("")
    var productCategoryFilter by mutableStateOf(MenuCategory.SEMUA)

    var draftName by mutableStateOf("")
    var draftPrice by mutableStateOf("")
    var draftCategory by mutableStateOf(MenuCategory.MAKANAN)
    var draftDescription by mutableStateOf("")
    var newCategoryName by mutableStateOf("")

    val categories = mutableStateListOf(MenuCategory.MAKANAN, MenuCategory.MINUMAN, MenuCategory.CAMILAN)

    val products = mutableStateListOf(
        MenuItemModel(
            "menu_1", "Nasi Campur 3 Rasa Spesial", MenuCategory.MAKANAN, 45000, "nasi_campur",
            "Ayam rempah Madura, cumi hitam pedas, sambal matah"
        ),
        MenuItemModel(
            "menu_2", "Bakmi Ayam Jamur Maharewa", MenuCategory.MAKANAN, 32000, "bakmi_ayam",
            "Mie kenyal, kuah kaldu ayam kampung & pangsit"
        ),
        MenuItemModel(
            "menu_3", "Kopi Susu Gula Aren 3 Rasa", MenuCategory.MINUMAN, 22000, "kopi_susu",
            "Espresso, susu segar, gula aren organik"
        ),
        MenuItemModel(
            "menu_4", "Teh Tarik Rempah Hangat", MenuCategory.MINUMAN, 16000, "teh_tarik",
            "Teh hitam, kapulaga, cengkeh & susu kental manis"
        ),
        MenuItemModel(
            "menu_5", "Pisang Goreng Wijen Karamel", MenuCategory.CAMILAN, 18000, "pisang_goreng",
            "Pisang kepok renyah, wijen & karamel gula aren"
        ),
        MenuItemModel(
            "menu_6", "Kentang Goreng Rasa Keju", MenuCategory.CAMILAN, 20000, null,
            "Saus tartar & sambal", isAvailable = false
        )
    )

    val filteredProducts: List<MenuItemModel>
        get() = products.filter { p ->
            (productCategoryFilter == MenuCategory.SEMUA || p.category == productCategoryFilter) &&
                (productSearch.isBlank() || p.name.contains(productSearch, ignoreCase = true))
        }

    fun countFor(category: MenuCategory): Int =
        if (category == MenuCategory.SEMUA) products.size else products.count { it.category == category }

    fun login() {
        if (username.isBlank() || password.isBlank()) {
            loginError = "Username dan password wajib diisi."
            return
        }
        loginError = null
        isLoggedIn = true
    }

    fun logout() {
        activeTab = AdminTab.PRODUK
        isLoggedIn = false
        username = ""
        password = ""
        loginError = null
    }

    fun toggleProductAvailability(id: String) {
        val i = products.indexOfFirst { it.id == id }
        if (i != -1) products[i] = products[i].copy(isAvailable = !products[i].isAvailable)
    }

    fun deleteProduct(id: String) {
        products.removeAll { it.id == id }
    }

    fun saveDraftProduct() {
        val price = draftPrice.filter { it.isDigit() }.toLongOrNull() ?: return
        if (draftName.isBlank()) return
        products.add(
            MenuItemModel(
                id = "menu_${System.currentTimeMillis()}",
                name = draftName.trim(),
                category = draftCategory,
                price = price,
                description = draftDescription.trim()
            )
        )
        resetDraft()
    }

    fun resetDraft() {
        draftName = ""
        draftPrice = ""
        draftCategory = MenuCategory.MAKANAN
        draftDescription = ""
    }

    fun addCategory() {
        val name = newCategoryName.trim()
        if (name.isBlank()) return
        val parsed = MenuCategory.entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) }
        if (parsed != null && parsed !in categories) categories.add(parsed)
        newCategoryName = ""
    }

    fun deleteCategory(category: MenuCategory) {
        // Kategori yang masih berisi produk tidak bisa dihapus (lihat catatan di mockup 07).
        if (products.none { it.category == category }) categories.remove(category)
    }

    // ---------- 08 Log & Statistik ----------
    var statisticsPeriod by mutableStateOf(StatisticsPeriod.HARIAN)
    var logStatusFilter by mutableStateOf(LogStatusFilter.SEMUA)
    var logSearch by mutableStateOf("")

    var periodLabel by mutableStateOf("Rabu, 04 Nov 2026")

    fun shiftPeriod(by: Int) {
        // Placeholder sampai data riil tersedia: geser label tanggal saja.
        periodLabel = when (by) {
            -1 -> "Selasa, 03 Nov 2026"
            1 -> "Kamis, 05 Nov 2026"
            else -> periodLabel
        }
    }

    val logEntries = mutableStateListOf(
        OrderLogEntry("#TR-04-8921", "12:40 WIB", "04", "Nasi Campur 3 Rasa Spesial, Kopi Susu Gula Aren, Pisang Goreng Wijen", 93500, PaymentMethod.CASH, OrderStatus.DIPROSES),
        OrderLogEntry("#TR-01-8920", "12:31 WIB", "01", "Nasi Campur 3 Rasa Spesial (2), Pisang Goreng Wijen Karamel", 118800, PaymentMethod.CASH, OrderStatus.DIPROSES),
        OrderLogEntry("#TR-06-8919", "12:18 WIB", "06", "Bakmi Ayam Jamur Maharewa, Teh Tarik Rempah Hangat", 52800, PaymentMethod.TRANSFER, OrderStatus.SELESAI),
        OrderLogEntry("#TR-02-8918", "12:05 WIB", "02", "Kopi Susu Gula Aren 3 Rasa (3)", 72600, PaymentMethod.CASH, OrderStatus.SELESAI),
        OrderLogEntry("#TR-07-8917", "11:52 WIB", "07", "Teh Tarik Rempah Hangat (2), Kentang Goreng Rasa Keju", 57200, PaymentMethod.TRANSFER, OrderStatus.SELESAI),
        OrderLogEntry("#TR-03-8916", "11:40 WIB", "03", "Bakmi Ayam Jamur Maharewa", 35200, PaymentMethod.CASH, OrderStatus.DIBATALKAN),
        OrderLogEntry("#TR-05-8915", "11:22 WIB", "05", "Nasi Campur 3 Rasa Spesial, Kopi Susu Gula Aren 3 Rasa", 73700, PaymentMethod.TRANSFER, OrderStatus.SELESAI)
    )

    val filteredLog: List<OrderLogEntry>
        get() = logEntries.filter { e ->
            (logStatusFilter == LogStatusFilter.SEMUA || e.status.label == logStatusFilter.label) &&
                (logSearch.isBlank() ||
                    e.id.contains(logSearch, ignoreCase = true) ||
                    e.tableNumber.contains(logSearch))
        }

    val totalRevenue: Long get() = 2_982_000
    val orderCount: Int get() = 70
    val peakHour: String get() = "Pukul 12:00"

    /** Tinggi bar relatif (px pada skala desain 1280) dari pendapatan per jam. */
    val hourlyRevenue = listOf(
        HourlyRevenue("08:00", 47_300), HourlyRevenue("09:00", 78_300),
        HourlyRevenue("10:00", 65_000), HourlyRevenue("11:00", 112_000),
        HourlyRevenue("12:00", 212_700), HourlyRevenue("13:00", 187_700),
        HourlyRevenue("14:00", 85_300), HourlyRevenue("15:00", 73_000),
        HourlyRevenue("16:00", 95_300), HourlyRevenue("17:00", 124_700),
        HourlyRevenue("18:00", 163_700), HourlyRevenue("19:00", 195_700),
        HourlyRevenue("20:00", 136_000), HourlyRevenue("21:00", 61_300)
    )

    val topMenus = listOf(
        "Nasi Campur 3 Rasa Spesial" to 24,
        "Kopi Susu Gula Aren 3 Rasa" to 22,
        "Bakmi Ayam Jamur Maharewa" to 15,
        "Teh Tarik Rempah Hangat" to 11,
        "Pisang Goreng Wijen Karamel" to 9
    )

    // ---------- 09 Meja & QR ----------
    var tableAreaFilter by mutableStateOf(TableArea.AREA_UTAMA)
    var newTableNumber by mutableStateOf("09")
    var newTableSeats by mutableStateOf(4)
    var selectedQrTable by mutableStateOf("04")
    /** Area pilihan di form Tambah Meja (terpisah dari filter daftar). */
    var newTableArea by mutableStateOf(TableArea.AREA_UTAMA)

    val tableRows = mutableStateListOf(
        AdminTableRow("01", TableArea.AREA_UTAMA, 4, true),
        AdminTableRow("02", TableArea.AREA_UTAMA, 4, true),
        AdminTableRow("03", TableArea.AREA_UTAMA, 2, false),
        AdminTableRow("04", TableArea.AREA_UTAMA, 4, true),
        AdminTableRow("05", TableArea.AREA_UTAMA, 6, false),
        AdminTableRow("06", TableArea.AREA_UTAMA, 4, true),
        AdminTableRow("07", TableArea.SELASAR, 2, false),
        AdminTableRow("08", TableArea.SELASAR, 4, false)
    )

    val totalTables: Int get() = tableRows.size
    val mainAreaTables: Int get() = tableRows.count { it.area == TableArea.AREA_UTAMA }
    val otherAreaTables: Int get() = tableRows.count { it.area != TableArea.AREA_UTAMA }
    val occupiedTables: Int get() = tableRows.count { it.isOccupied }

    val selectedQrRow: AdminTableRow
        get() = tableRows.firstOrNull { it.number == selectedQrTable } ?: tableRows.first()

    fun addTable() {
        val number = newTableNumber.trim().padStart(2, '0')
        if (number.isBlank() || tableRows.any { it.number == number }) return
        tableRows.add(AdminTableRow(number, newTableArea, newTableSeats, false))
        selectedQrTable = number
        newTableNumber = (tableRows.size + 1).toString().padStart(2, '0')
    }
}
