package com.cafe3rasa.kasir.auth

import androidx.compose.runtime.mutableStateListOf
import com.cafe3rasa.kasir.data.ApiClient
import com.cafe3rasa.kasir.data.ApiException
import com.cafe3rasa.kasir.data.UserCreateBody
import com.cafe3rasa.kasir.data.UserDto
import com.cafe3rasa.kasir.data.UserUpdateBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement

// ─── User Roles ───────────────────────────────────────────────────────────────

enum class UserRole { KASIR, ADMIN }

// ─── User Data Model ──────────────────────────────────────────────────────────

data class UserAccount(
    val id: String,
    val username: String,
    val password: String,        // selalu kosong: password tidak pernah dikirim server ke aplikasi
    val displayName: String,
    val role: UserRole,
    val isActive: Boolean = true
)

// ─── Daftar Akun (dimuat dari server, khusus Admin) ───────────────────────────

/** Daftar akun dari tabel `users` di database. Diisi oleh [refreshAccounts]. */
val userAccountsState = mutableStateListOf<UserAccount>()

private val accountScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

private fun UserDto.toAccount() = UserAccount(
    id = id,
    username = username,
    password = "",
    displayName = fullName,
    role = if (role.equals("ADMIN", ignoreCase = true)) UserRole.ADMIN else UserRole.KASIR,
    isActive = isActive
)

/** Muat ulang daftar akun dari server. @return pesan error, atau null jika berhasil. */
suspend fun refreshAccounts(): String? = try {
    val list: List<UserDto> = ApiClient.get("users")
    userAccountsState.clear()
    userAccountsState.addAll(list.map { it.toAccount() })
    null
} catch (e: ApiException) {
    e.message
}

/**
 * Jalankan aksi akun di latar belakang lalu kembalikan hasilnya ke UI lewat [onResult]
 * (null = berhasil, selain itu pesan error).
 */
private fun launchAccountAction(onResult: (String?) -> Unit, block: suspend () -> Unit) {
    accountScope.launch {
        val error = try {
            block()
            refreshAccounts()
            null
        } catch (e: ApiException) {
            e.message
        } catch (e: Exception) {
            e.message ?: "Terjadi kesalahan"
        }
        onResult(error)
    }
}

// ─── CRUD Akun Kasir (Hanya untuk Admin) ───────────────────────────────────────

/** Menambahkan akun kasir baru di server. Hasil dikirim ke [onResult]. */
fun addCashierAccount(username: String, displayName: String, password: String, onResult: (String?) -> Unit) {
    val cleanUser = username.trim().lowercase()
    val cleanName = displayName.trim()
    val cleanPass = password.trim()

    // Validasi cepat di aplikasi (server tetap memvalidasi ulang)
    val localError = when {
        cleanUser.isEmpty() -> "Username tidak boleh kosong"
        cleanUser.contains(" ") -> "Username tidak boleh mengandung spasi"
        cleanUser.length < 3 -> "Username minimal 3 karakter"
        cleanName.isEmpty() -> "Nama kasir tidak boleh kosong"
        cleanPass.length < 6 -> "Password minimal 6 karakter"
        else -> null
    }
    if (localError != null) return onResult(localError)

    launchAccountAction(onResult) {
        ApiClient.post<UserDto, UserCreateBody>("users", UserCreateBody(cleanUser, cleanName, cleanPass))
    }
}

/** Memperbarui nama dan/atau password akun kasir. Password kosong = tidak diubah. */
fun updateCashierAccount(id: String, displayName: String, password: String, onResult: (String?) -> Unit) {
    val cleanName = displayName.trim()
    val cleanPass = password.trim()

    val localError = when {
        cleanName.isEmpty() -> "Nama kasir tidak boleh kosong"
        cleanPass.isNotEmpty() && cleanPass.length < 6 -> "Password baru minimal 6 karakter"
        else -> null
    }
    if (localError != null) return onResult(localError)

    launchAccountAction(onResult) {
        ApiClient.patch<UserDto, UserUpdateBody>(
            "users/$id",
            UserUpdateBody(fullName = cleanName, password = cleanPass.ifEmpty { null })
        )
    }
}

/** Menghapus akun kasir. Akun admin dilindungi (juga dicek server). */
fun deleteCashierAccount(id: String, onResult: (String?) -> Unit) {
    val user = userAccountsState.firstOrNull { it.id == id } ?: return onResult("Akun tidak ditemukan")
    if (user.role == UserRole.ADMIN) {
        return onResult("Akun Administrator tidak dapat dihapus demi keamanan sistem")
    }
    launchAccountAction(onResult) {
        ApiClient.delete<JsonElement>("users/$id")
    }
}
