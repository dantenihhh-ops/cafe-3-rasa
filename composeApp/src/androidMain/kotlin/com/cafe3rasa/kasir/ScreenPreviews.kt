package com.cafe3rasa.kasir

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.cafe3rasa.kasir.splash.TigaRasaSplash
import com.cafe3rasa.kasir.theme.KasirCafeTheme
import com.cafe3rasa.kasir.ui.CashierDashboardScreen
import com.cafe3rasa.kasir.ui.admin.AdminScreen
import com.cafe3rasa.kasir.ui.screens.LoginScreen

@Preview(name = "1. Splash Screen", showBackground = true, widthDp = 800, heightDp = 600)
@Composable
fun PreviewSplashScreen() {
    KasirCafeTheme {
        TigaRasaSplash(onFinished = {})
    }
}

@Preview(name = "2. Login Screen (Tablet)", showBackground = true, widthDp = 1024, heightDp = 768)
@Composable
fun PreviewLoginScreenTablet() {
    KasirCafeTheme {
        LoginScreen(onLoginSuccess = {})
    }
}

@Preview(name = "3. Login Screen (HP)", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
fun PreviewLoginScreenPhone() {
    KasirCafeTheme {
        LoginScreen(onLoginSuccess = {})
    }
}

@Preview(name = "4. Dashboard Kasir (Tablet)", showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
fun PreviewCashierDashboardTablet() {
    KasirCafeTheme {
        CashierDashboardScreen()
    }
}

@Preview(name = "5. Dashboard Kasir (HP)", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
fun PreviewCashierDashboardPhone() {
    KasirCafeTheme {
        CashierDashboardScreen()
    }
}

@Preview(name = "6. Admin Panel (Tablet)", showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
fun PreviewAdminTablet() {
    KasirCafeTheme {
        AdminScreen()
    }
}

@Preview(name = "7. Admin Panel (HP)", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
fun PreviewAdminPhone() {
    KasirCafeTheme {
        AdminScreen()
    }
}
