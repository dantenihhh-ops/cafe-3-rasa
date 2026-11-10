package com.cafe3rasa.kasir.model

enum class TableStatus {
    DIPILIH,
    TERISI_BELUM_BAYAR,
    KOSONG,
    TERISI_LUNAS
}

data class TableModel(
    val id: Int,
    val number: String,
    val status: TableStatus,
    val subtitle: String,
    val activeSince: String = "09:25 WIB",
    val minutesAgo: Int = 16
)
