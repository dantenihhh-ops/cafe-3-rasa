package com.cafe3rasa.kasir.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cafe3rasa.kasir.model.*

enum class TableFilter(val label: String) {
    SEMUA("Semua (8)"),
    TERISI("Terisi (3)"),
    PERLU_BILL("Perlu Bill (2)")
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
    var paymentStatus by mutableStateOf(PaymentBillStatus.BELUM_BAYAR)
    var mobileTab by mutableStateOf(MobileNavigationTab.ORDER_DETAIL)
    var showChangeTableDialog by mutableStateOf(false)

    val tables = mutableStateListOf(
        TableModel(1, "01", TableStatus.DIPILIH, "Pesanan Baru", "09:25 WIB", 16),
        TableModel(2, "02", TableStatus.TERISI_BELUM_BAYAR, "BELUM BAYAR"),
        TableModel(3, "03", TableStatus.KOSONG, "Tersedia"),
        TableModel(4, "04", TableStatus.TERISI_BELUM_BAYAR, "BELUM BAYAR"),
        TableModel(5, "05", TableStatus.KOSONG, "Tersedia"),
        TableModel(6, "06", TableStatus.TERISI_LUNAS, "LUNAS"),
        TableModel(7, "07", TableStatus.KOSONG, "Tersedia"),
        TableModel(8, "08", TableStatus.KOSONG, "Tersedia")
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
        MenuItemModel("menu_1", "Nasi Campur 3 Rasa", MenuCategory.MAKANAN, 45000, "nasi_campur"),
        MenuItemModel("menu_2", "Bakmi Ayam Jamur Maharewa", MenuCategory.MAKANAN, 32000, "bakmi_ayam"),
        MenuItemModel("menu_3", "Kopi Susu Gula Aren 3 Rasa", MenuCategory.MINUMAN, 22000, "kopi_susu"),
        MenuItemModel("menu_4", "Pisang Goreng Wijen Karamel", MenuCategory.CAMILAN, 18000, "pisang_goreng"),
        MenuItemModel("menu_5", "Teh Tarik Rempah", MenuCategory.MINUMAN, 16000, null)
    )

    val totalItemCount: Int
        get() = orderItems.sumOf { it.quantity }

    val totalRupiah: Long
        get() = orderItems.sumOf { it.subtotal }

    val filteredTables: List<TableModel>
        get() = when (activeTableFilter) {
            TableFilter.SEMUA -> tables
            TableFilter.TERISI -> tables.filter { it.status != TableStatus.KOSONG }
            TableFilter.PERLU_BILL -> tables.filter { it.status == TableStatus.TERISI_BELUM_BAYAR }
        }

    val filteredMenu: List<MenuItemModel>
        get() = if (activeMenuCategory == MenuCategory.SEMUA) {
            menuCatalog
        } else {
            menuCatalog.filter { it.category == activeMenuCategory }
        }

    val emptyTables: List<TableModel>
        get() = tables.filter { it.status == TableStatus.KOSONG }

    fun selectTable(number: String) {
        selectedTableNumber = number
        val table = tables.firstOrNull { it.number == number }
        selectedTableOccupied = table?.status != TableStatus.KOSONG
    }

    fun toggleOccupancy() {
        selectedTableOccupied = !selectedTableOccupied
        val index = tables.indexOfFirst { it.number == selectedTableNumber }
        if (index != -1) {
            tables[index] = tables[index].copy(
                status = if (selectedTableOccupied) TableStatus.DIPILIH else TableStatus.KOSONG,
                subtitle = if (selectedTableOccupied) "Pesanan Aktif" else "Tersedia",
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
            // Meja lama dikosongkan karena tamu pindah meja
            if (oldIndex != -1) {
                tables[oldIndex] = tables[oldIndex].copy(
                    status = TableStatus.KOSONG,
                    subtitle = "Tersedia",
                    activeSince = "09:25 WIB",
                    minutesAgo = 0
                )
            }

            // Meja baru diaktifkan dengan pesanan yang dipindahkan
            tables[newIndex] = tables[newIndex].copy(
                status = TableStatus.DIPILIH,
                subtitle = "Pesanan Aktif",
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
                subtitle = "Tersedia",
                activeSince = "09:25 WIB",
                minutesAgo = 0
            )
        }
        orderItems.clear()
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
