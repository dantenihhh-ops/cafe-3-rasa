package com.cafe3rasa.kasir.auth

// ─── User Roles ───────────────────────────────────────────────────────────────

enum class UserRole { KASIR, ADMIN }

// ─── User Data Model ──────────────────────────────────────────────────────────

data class UserAccount(
    val id: String,
    val username: String,
    val password: String,   // Data dummy sementara — belum hash/enkripsi
    val displayName: String,
    val role: UserRole
)

// ─── Dummy User Database ─────────────────────────────────────────────────────
// Nanti diganti dengan autentikasi Firebase / API server.

val DUMMY_USERS = listOf(
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

// ─── Auth Logic ──────────────────────────────────────────────────────────────

/**
 * Validasi login dari data dummy.
 * Mengembalikan [UserAccount] jika berhasil, atau null jika gagal.
 */
fun authenticate(username: String, password: String): UserAccount? {
    return DUMMY_USERS.firstOrNull {
        it.username.equals(username.trim(), ignoreCase = true) && it.password == password
    }
}
