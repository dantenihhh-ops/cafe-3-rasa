package com.cafe3rasa.kasir.data

/**
 * Koneksi real-time (Socket.io) ke backend.
 * Diimplementasikan per platform (Android & Desktop memakai io.socket:socket.io-client).
 *
 * @param onEvent  dipanggil saat server mengirim event (nama event, nomor meja jika ada)
 * @param onStatus dipanggil saat status koneksi berubah (true = terhubung)
 */
expect class RealtimeClient() {
    fun connect(
        serverUrl: String,
        token: String?,
        onEvent: (event: String, tableNumber: String?) -> Unit,
        onStatus: (connected: Boolean) -> Unit
    )

    fun disconnect()
}

/** Event dari backend yang membuat data dashboard kasir perlu dimuat ulang */
val CASHIER_EVENTS = listOf(
    "new_order",
    "order_updated",
    "call_waiter",
    "request_updated",
    "table_status_changed",
    "tables_changed",
    "table_cleared",
    "table_moved",
    "menu_changed"
)
