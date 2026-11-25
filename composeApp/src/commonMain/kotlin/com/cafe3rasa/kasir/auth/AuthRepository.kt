package com.cafe3rasa.kasir.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cafe3rasa.kasir.data.ApiClient
import com.cafe3rasa.kasir.data.ApiException
import com.cafe3rasa.kasir.data.LoginRequest
import com.cafe3rasa.kasir.data.LoginResponse
import com.cafe3rasa.kasir.data.UserDto
import com.cafe3rasa.kasir.data.storageGet
import com.cafe3rasa.kasir.data.storagePut

/**
 * Login ke backend & penyimpanan sesi.
 *
 * Sesi (token + data user) disimpan di penyimpanan lokal
 * (SharedPreferences di Android) sehingga kasir tidak perlu login ulang
 * setiap kali aplikasi dibuka.
 */
object AuthRepository {

    private const val KEY_USER = "auth_user"

    /** User yang sedang login (null jika belum login) */
    var currentUser by mutableStateOf<UserAccount?>(null)
        private set

    /** Login ke server. Melempar [ApiException] berisi pesan yang siap ditampilkan. */
    suspend fun login(username: String, password: String): UserAccount {
        val result: LoginResponse = ApiClient.post(
            "auth/login",
            LoginRequest(username.trim(), password)
        )
        ApiClient.token = result.token
        storagePut(KEY_USER, ApiClient.json.encodeToString(UserDto.serializer(), result.user))
        val user = result.user.toAccount()
        currentUser = user
        return user
    }

    /**
     * Pulihkan sesi tersimpan saat aplikasi dibuka.
     * - Token masih valid  -> kembalikan user (langsung masuk dashboard)
     * - Token ditolak (401) -> hapus sesi, kembalikan null (ke layar login)
     * - Server tidak bisa dihubungi -> tetap pakai data user tersimpan
     */
    suspend fun restoreSession(): UserAccount? {
        val token = runCatching { ApiClient.token }.getOrNull()
        val cached = runCatching {
            storageGet(KEY_USER)?.let { ApiClient.json.decodeFromString(UserDto.serializer(), it) }
        }.getOrNull()

        if (token.isNullOrBlank() || cached == null) {
            clearSession()
            return null
        }

        return try {
            val fresh: UserDto = ApiClient.get("auth/me")
            storagePut(KEY_USER, ApiClient.json.encodeToString(UserDto.serializer(), fresh))
            fresh.toAccount().also { currentUser = it }
        } catch (e: ApiException) {
            if (e.isNetworkError) {
                cached.toAccount().also { currentUser = it }
            } else {
                clearSession()
                null
            }
        }
    }

    fun logout() {
        clearSession()
    }

    private fun clearSession() {
        ApiClient.token = null
        storagePut(KEY_USER, null)
        currentUser = null
    }

    private fun UserDto.toAccount() = UserAccount(
        id = id,
        username = username,
        password = "",                 // password tidak pernah disimpan di aplikasi
        displayName = fullName,
        role = if (role.equals("ADMIN", ignoreCase = true)) UserRole.ADMIN else UserRole.KASIR
    )
}
