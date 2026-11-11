package com.cafe3rasa.kasir

import androidx.compose.runtime.*
import com.cafe3rasa.kasir.splash.TigaRasaSplash
import com.cafe3rasa.kasir.state.AdminPanelState
import com.cafe3rasa.kasir.theme.KasirCafeTheme
import com.cafe3rasa.kasir.ui.CashierDashboardScreen
import com.cafe3rasa.kasir.ui.admin.AdminLoginScreen
import com.cafe3rasa.kasir.ui.admin.AdminPanel

/** Halaman utama aplikasi. */
private enum class AppScreen { KASIR, LOGIN, ADMIN }

@Composable
fun App() {
    var showSplash by remember { mutableStateOf(true) }
    var screen by remember { mutableStateOf(AppScreen.KASIR) }
    val admin = remember { AdminPanelState() }

    // Login berhasil -> masuk panel admin
    LaunchedEffect(admin.isLoggedIn, screen) {
        if (screen == AppScreen.LOGIN && admin.isLoggedIn) screen = AppScreen.ADMIN
    }

    KasirCafeTheme {
        when {
            showSplash -> TigaRasaSplash(onFinished = { showSplash = false })

            // POS kasir (mockup 06). Tombol ⚙ di header membuka panel admin.
            screen == AppScreen.KASIR -> CashierDashboardScreen(
                onOpenAdmin = { screen = AppScreen.LOGIN }
            )

            screen == AppScreen.LOGIN -> AdminLoginScreen(
                state = admin,
                onBack = { screen = AppScreen.KASIR }
            )

            else -> AdminPanel(
                state = admin,
                onExit = {
                    admin.logout()
                    screen = AppScreen.KASIR
                }
            )
        }
    }
}
