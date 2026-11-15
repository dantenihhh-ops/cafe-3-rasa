package com.cafe3rasa.kasir.auth

import androidx.compose.runtime.mutableStateListOf

// ─── User Roles ───────────────────────────────────────────────────────────────

enum class UserRole { KASIR, ADMIN }

// ─── User Data Model ──────────────────────────────────────────────────────────

data class UserAccount(
    val id: String,
    val username: String,
    val password: String,
    val displayName: String,
    val role: UserRole
)

// ─── State Akun Pengguna (Reaktif di Memori) ──────────────────────────────────
// Menyimpan akun pengguna aktif. Dapat ditambah, diubah, atau dihapus oleh Admin.

val userAccountsState = mutableStateListOf(
    UserAccount(
        id        = "usr_kasir_01",
        username  = "kasir",
        password  = "kasir123",
        displayName = "Budi Santoso",
        role      = UserRole.KASIR
    ),
    UserAccount(
        id        = "usr_kasir_02",
        username  = "kasir2",
        password  = "kasir123",
        displayName = "Dewi Rahayu",
        role      = UserRole.KASIR
    ),
    UserAccount(
        id        = "usr_admin_01",
        username  = "admin",
        password  = "admin123",
        displayName = "Pak Maharewa",
        role      = UserRole.ADMIN
    )
)

// Alias backward-compatibility untuk referensi yang masih membaca DUMMY_USERS
val DUMMY_USERS: List<UserAccount> get() = userAccountsState

// ─── Auth Logic ──────────────────────────────────────────────────────────────

/**
 * Validasi login dari state akun pengguna.
 * Mengembalikan [UserAccount] jika username & password cocok, atau null jika gagal.
 */
fun authenticate(username: String, password: String): UserAccount? {
    val cleanUsername = username.trim()
    return userAccountsState.firstOrNull {
        it.username.equals(cleanUsername, ignoreCase = true) && it.password == password
    }
}

// ─── CRUD Akun Kasir (Hanya untuk Admin) ───────────────────────────────────────

/**
 * Menambahkan akun kasir baru.
 * @return Pesan error jika validasi gagal, atau null jika berhasil.
 */
fun addCashierAccount(username: String, displayName: String, password: String): String? {
    val cleanUser = username.trim().lowercase()
    val cleanName = displayName.trim()
    val cleanPass = password.trim()

    if (cleanUser.isEmpty()) return "Username tidak boleh kosong"
    if (cleanUser.contains(" ")) return "Username tidak boleh mengandung spasi"
    if (cleanName.isEmpty()) return "Nama kasir tidak boleh kosong"
    if (cleanPass.isEmpty()) return "Password tidak boleh kosong"
    if (cleanPass.length < 4) return "Password minimal 4 karakter"
    if (userAccountsState.any { it.username.equals(cleanUser, ignoreCase = true) }) {
        return "Username '$cleanUser' sudah digunakan"
    }

    userAccountsState.add(
        UserAccount(
            id = "usr_kasir_${System.currentTimeMillis()}",
            username = cleanUser,
            password = cleanPass,
            displayName = cleanName,
            role = UserRole.KASIR
        )
    )
    return null
}

/**
 * Memperbarui nama atau password akun kasir.
 * @return Pesan error jika validasi gagal, atau null jika berhasil.
 */
fun updateCashierAccount(id: String, displayName: String, password: String): String? {
    val idx = userAccountsState.indexOfFirst { it.id == id }
    if (idx < 0) return "Akun kasir tidak ditemukan"

    val existing = userAccountsState[idx]
    val cleanName = displayName.trim()
    val cleanPass = password.trim()

    if (cleanName.isEmpty()) return "Nama kasir tidak boleh kosong"
    if (cleanPass.isNotEmpty() && cleanPass.length < 4) return "Password baru minimal 4 karakter"

    userAccountsState[idx] = existing.copy(
        displayName = cleanName,
        password = if (cleanPass.isNotEmpty()) cleanPass else existing.password
    )
    return null
}

/**
 * Menghapus akun kasir. Akun admin diproteksi agar tidak bisa terhapus.
 * @return Pesan error jika gagal, atau null jika berhasil.
 */
fun deleteCashierAccount(id: String): String? {
    val user = userAccountsState.firstOrNull { it.id == id } ?: return "Akun tidak ditemukan"
    if (user.role == UserRole.ADMIN) {
        return "Akun Administrator tidak dapat dihapus demi keamanan sistem"
    }
    userAccountsState.remove(user)
    return null
}
