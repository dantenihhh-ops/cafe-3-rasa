package com.cafe3rasa.kasir.model

enum class TableStatus {
    KOSONG,              // Meja kosong, tidak ada tamu
    BARU_PESAN,          // Pesanan baru masuk dari e-menu, belum dikonfirmasi kasir (dot berkedip)
    TERISI_BELUM_BAYAR,  // Kasir sudah konfirmasi, tamu belum bayar (dot merah solid)
    TERISI_LUNAS         // Sudah bayar, siap dikosongkan (dot hijau)
}

data class TableModel(
    val id: Int,
    val number: String,
    val status: TableStatus,
    val subtitle: String = "",
    val activeSince: String = "09:25 WIB",
    val minutesAgo: Int = 0,
    val paymentMethod: PaymentMethod = PaymentMethod.BELUM_DIPILIH,
    val orderSource: OrderSource = OrderSource.KASIR
)

/** Sumber pesanan — dari kasir langsung atau dari e-menu website pelanggan */
enum class OrderSource(val label: String) {
    KASIR("Kasir"),
    EMENU("E-Menu")
}
