package com.cafe3rasa.kasir.data

import java.util.prefs.Preferences

private val prefs: Preferences by lazy { Preferences.userRoot().node("cafe3rasa-kasir") }

actual fun storageGet(key: String): String? = prefs.get(key, null)

actual fun storagePut(key: String, value: String?) {
    if (value == null) prefs.remove(key) else prefs.put(key, value)
    runCatching { prefs.flush() }
}

// Aplikasi desktop biasanya berjalan di laptop yang sama dengan backend
actual val defaultServerUrl: String = "http://localhost:5000"
