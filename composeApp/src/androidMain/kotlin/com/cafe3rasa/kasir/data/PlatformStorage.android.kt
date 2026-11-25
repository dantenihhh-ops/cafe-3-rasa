package com.cafe3rasa.kasir.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Pemegang SharedPreferences. Wajib di-init sekali di MainActivity.onCreate:
 *     AndroidStorage.init(applicationContext)
 */
object AndroidStorage {
    private const val PREFS_NAME = "cafe3rasa_session"
    internal var prefs: SharedPreferences? = null
        private set

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }
}

actual fun storageGet(key: String): String? = AndroidStorage.prefs?.getString(key, null)

actual fun storagePut(key: String, value: String?) {
    val editor = AndroidStorage.prefs?.edit() ?: return
    if (value == null) editor.remove(key) else editor.putString(key, value)
    editor.apply()
}

// Di HP asli alamat laptop berbeda-beda (tergantung hotspot/WiFi),
// sehingga dikosongkan dan wajib diatur sekali dari layar login.
actual val defaultServerUrl: String = ""
