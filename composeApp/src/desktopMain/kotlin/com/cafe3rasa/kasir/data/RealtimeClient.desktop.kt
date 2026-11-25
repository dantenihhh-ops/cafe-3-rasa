package com.cafe3rasa.kasir.data

import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

actual class RealtimeClient actual constructor() {

    private var socket: Socket? = null

    actual fun connect(
        serverUrl: String,
        token: String?,
        onEvent: (event: String, tableNumber: String?) -> Unit,
        onStatus: (connected: Boolean) -> Unit
    ) {
        disconnect()
        if (serverUrl.isBlank()) return

        val options = IO.Options().apply {
            reconnection = true
            reconnectionDelay = 2_000
            reconnectionDelayMax = 10_000
            timeout = 10_000
        }

        val s = try {
            IO.socket(serverUrl, options)
        } catch (e: Exception) {
            onStatus(false)
            return
        }

        s.on(Socket.EVENT_CONNECT) {
            // Masuk ke room kasir (server memeriksa token login)
            s.emit("join_cashier", JSONObject().put("token", token ?: ""))
            onStatus(true)
        }
        s.on(Socket.EVENT_DISCONNECT) { onStatus(false) }
        s.on(Socket.EVENT_CONNECT_ERROR) { onStatus(false) }

        CASHIER_EVENTS.forEach { event ->
            s.on(event) { args ->
                val payload = args.firstOrNull() as? JSONObject
                val table = payload?.optString("tableNumber")?.takeIf { it.isNotBlank() }
                onEvent(event, table)
            }
        }

        socket = s
        s.connect()
    }

    actual fun disconnect() {
        socket?.let {
            it.off()
            it.disconnect()
            it.close()
        }
        socket = null
    }
}
