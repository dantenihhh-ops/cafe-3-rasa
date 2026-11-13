package com.cafe3rasa.kasir

import androidx.compose.runtime.*
import com.cafe3rasa.kasir.auth.UserAccount
import com.cafe3rasa.kasir.auth.UserRole
import com.cafe3rasa.kasir.splash.TigaRasaSplash
import com.cafe3rasa.kasir.state.AdminPanelState
import com.cafe3rasa.kasir.theme.KasirCafeTheme
import com.cafe3rasa.kasir.ui.CashierDashboardScreen
import com.cafe3rasa.kasir.ui.admin.AdminLoginScreen
import com.cafe3rasa.kasir.ui.admin.AdminPanel
import com.cafe3rasa.kasir.ui.screens.LoginScreen

/** Halaman utama aplikasi. */
private enum class AppScreen {
    SPLASH,
    LOGIN,          // Login bersama (kasir & admin)
    KASIR,          // Dashboard kasir
    ADMIN_LOGIN,    // Login khusus admin dari dalam kasir
    ADMIN           // Panel admin
}

@Composable
fun App() {
    var screen by remember { mutableStateOf(AppScreen.SPLASH) }
    var currentUser by remember { mutableStateOf<UserAccount?>(null) }
    val admin = remember { AdminPanelState() }

    KasirCafeTheme {
        when (screen) {

            // ── 1. Splash ──────────────────────────────────────────────────────
            AppScreen.SPLASH -> TigaRasaSplash(
                onFinished = { screen = AppScreen.LOGIN }
            )

            // ── 2. Login (kasir & admin) ───────────────────────────────────────
            AppScreen.LOGIN -> LoginScreen(
                onLoginSuccess = { user ->
                    currentUser = user
                    screen = when (user.role) {
                        UserRole.KASIR -> AppScreen.KASIR
                        UserRole.ADMIN -> AppScreen.ADMIN
                    }
                }
            )

            // ── 3. Dashboard Kasir ─────────────────────────────────────────────
            AppScreen.KASIR -> CashierDashboardScreen(
                onOpenAdmin = { screen = AppScreen.ADMIN_LOGIN },
                onLogout = {
                    currentUser = null
                    screen = AppScreen.LOGIN
                }
            )

            // ── 4. Login Admin (dari dalam dashboard kasir) ────────────────────
            AppScreen.ADMIN_LOGIN -> AdminLoginScreen(
                state = admin,
                onBack = { screen = AppScreen.KASIR }
            )

            // ── 5. Panel Admin ─────────────────────────────────────────────────
            AppScreen.ADMIN -> AdminPanel(
                state = admin,
                onExit = {
                    admin.logout()
                    currentUser = null
                    screen = AppScreen.LOGIN
                }
            )
        }

        // Auto-navigasi ke panel admin setelah login admin berhasil
        LaunchedEffect(admin.isLoggedIn) {
            if (admin.isLoggedIn && screen == AppScreen.ADMIN_LOGIN) {
                screen = AppScreen.ADMIN
            }
        }
    }
}
