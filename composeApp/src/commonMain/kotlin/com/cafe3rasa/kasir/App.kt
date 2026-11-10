package com.cafe3rasa.kasir

import androidx.compose.runtime.*
import com.cafe3rasa.kasir.splash.TigaRasaSplash
import com.cafe3rasa.kasir.theme.KasirCafeTheme
import com.cafe3rasa.kasir.ui.CashierDashboardScreen

@Composable
fun App() {
    var showSplash by remember { mutableStateOf(true) }
    KasirCafeTheme {
        if (showSplash) {
            TigaRasaSplash(onFinished = { showSplash = false })
        } else {
            CashierDashboardScreen()
        }
    }
}
