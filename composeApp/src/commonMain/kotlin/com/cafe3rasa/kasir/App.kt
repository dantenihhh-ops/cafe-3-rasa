package com.cafe3rasa.kasir

import androidx.compose.runtime.*
import com.cafe3rasa.kasir.auth.AuthRepository
import com.cafe3rasa.kasir.auth.UserAccount
import com.cafe3rasa.kasir.auth.UserRole
import com.cafe3rasa.kasir.data.ApiClient
import com.cafe3rasa.kasir.splash.TigaRasaSplash
import com.cafe3rasa.kasir.state.AdminPanelState
import com.cafe3rasa.kasir.theme.KasirCafeTheme
import com.cafe3rasa.kasir.ui.CashierDashboardScreen
import com.cafe3rasa.kasir.ui.admin.AdminLoginScreen
import com.cafe3rasa.kasir.ui.admin.AdminScreen
import com.cafe3rasa.kasir.ui.screens.LoginScreen

/** Halaman utama aplikasi. */
private enum class AppScreen {
    SPLASH,
    LOGIN,          // Login bersama (kasir & admin)
    KASIR,          // Dashboard kasir
    ADMIN_LOGIN,    // Login khusus admin dari dalam kasir
    ADMIN           // Panel admin
}

private fun screenFor(user: UserAccount) = when (user.role) {
    UserRole.KASIR -> AppScreen.KASIR
    UserRole.ADMIN -> AppScreen.ADMIN
}

@Composable
fun App() {
    var screen by remember { mutableStateOf(AppScreen.SPLASH) }
    var currentUser by remember { mutableStateOf<UserAccount?>(null) }
    val admin = remember { AdminPanelState() }

    // Pemulihan sesi login tersimpan berjalan bersamaan dengan animasi splash
    var splashDone by remember { mutableStateOf(false) }
    var sessionChecked by remember { mutableStateOf(false) }
    var restoredUser by remember { mutableStateOf<UserAccount?>(null) }

    LaunchedEffect(Unit) {
        restoredUser = runCatching { AuthRepository.restoreSession() }.getOrNull()
        sessionChecked = true
    }

    // Setelah splash selesai & sesi sudah dicek -> langsung ke dashboard jika masih login
    LaunchedEffect(splashDone, sessionChecked) {
        if (splashDone && sessionChecked && screen == AppScreen.SPLASH) {
            val user = restoredUser
            if (user != null) {
                currentUser = user
                screen = screenFor(user)
            } else {
                screen = AppScreen.LOGIN
            }
        }
    }

    val logout: () -> Unit = {
        AuthRepository.logout()
        admin.logout()
        currentUser = null
        screen = AppScreen.LOGIN
    }

    // Jika server menolak token (sesi kadaluarsa) -> kembali ke layar login
    DisposableEffect(Unit) {
        ApiClient.onUnauthorized = {
            if (screen != AppScreen.LOGIN && screen != AppScreen.SPLASH) logout()
        }
        onDispose { ApiClient.onUnauthorized = null }
    }

    KasirCafeTheme {
        when (screen) {

            // ── 1. Splash ──────────────────────────────────────────────────────
            AppScreen.SPLASH -> TigaRasaSplash(
                onFinished = { splashDone = true }
            )

            // ── 2. Login (kasir & admin) ───────────────────────────────────────
            AppScreen.LOGIN -> LoginScreen(
                onLoginSuccess = { user ->
                    currentUser = user
                    screen = screenFor(user)
                }
            )

            // ── 3. Dashboard Kasir ─────────────────────────────────────────────
            AppScreen.KASIR -> CashierDashboardScreen(
                onOpenAdmin = { screen = AppScreen.ADMIN_LOGIN },
                onLogout = logout
            )

            // ── 4. Login Admin (dari dalam dashboard kasir) ────────────────────
            AppScreen.ADMIN_LOGIN -> AdminLoginScreen(
                state = admin,
                onBack = { screen = AppScreen.KASIR }
            )

            // ── 5. Panel Admin ─────────────────────────────────────────────────
            AppScreen.ADMIN -> AdminScreen(
                currentUser = currentUser,
                onLogout = logout
            )
        }

        // Auto-navigasi ke panel admin setelah login admin berhasil
        LaunchedEffect(admin.isLoggedIn) {
            if (admin.isLoggedIn && screen == AppScreen.ADMIN_LOGIN) {
                // Sesi sekarang milik akun admin yang baru login
                currentUser = AuthRepository.currentUser ?: currentUser
                screen = AppScreen.ADMIN
            }
        }
    }
}
