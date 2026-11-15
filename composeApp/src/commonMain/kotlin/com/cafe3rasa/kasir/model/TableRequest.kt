package com.cafe3rasa.kasir.model

enum class RequestBadgeType {
    MINTA_BILL_TUNAI,
    PANGGIL_WAITER,
    PERINGATAN_DAPUR,
    TAMBAH_PESANAN
}

enum class RequestStatus(val label: String) {
    MENUNGGU("Menunggu"),
    DIKONFIRMASI("Staf Menuju Meja"),
    SELESAI("Selesai")
}

data class TableRequest(
    val id: String,
    val tableNumber: String,
    val badgeLabel: String,
    val badgeType: RequestBadgeType,
    val timeAgo: String,
    val note: String,
    val status: RequestStatus = RequestStatus.MENUNGGU,
    val responseMessage: String = "Staf segera menuju meja Anda",
    val primaryActionText: String = "Konfirmasi",
    val secondaryActionText: String = "Selesai",
    val isAlertDapur: Boolean = false
)
