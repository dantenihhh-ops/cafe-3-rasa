package com.cafe3rasa.kasir.model

enum class RequestBadgeType {
    MINTA_BILL_TUNAI,
    PANGGIL_WAITER,
    PERINGATAN_DAPUR,
    TAMBAH_PESANAN
}

data class TableRequest(
    val id: String,
    val tableNumber: String,
    val badgeLabel: String,
    val badgeType: RequestBadgeType,
    val timeAgo: String,
    val note: String,
    val primaryActionText: String = "Konfirmasi",
    val secondaryActionText: String = "Selesai",
    val isAlertDapur: Boolean = false
)
