package com.cafe3rasa.kasir.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cafe3rasa.kasir.state.AdminPanelState
import com.cafe3rasa.kasir.state.AdminTab
import com.cafe3rasa.kasir.theme.KasirColors

/** Kerangka panel admin: header + isi halaman aktif (scroll) + footer navigasi. */
@Composable
fun AdminPanel(state: AdminPanelState, onExit: () -> Unit = {}) {
    Surface(modifier = Modifier.fillMaxSize(), color = KasirColors.Background) {
        Column(Modifier.fillMaxSize()) {
            AdminHeader()
            // Area scroll: mengisi sisa tinggi antara header dan footer.
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    when (state.activeTab) {
                        AdminTab.PRODUK -> AdminProdukScreen(state)
                        AdminTab.LOG_STATISTIK -> AdminLogScreen(state)
                        AdminTab.MEJA_QR -> AdminMejaQrScreen(state)
                        AdminTab.KELUAR -> Unit
                    }
                }
            }
            AdminFooterNav(active = state.activeTab) { tab ->
                if (tab == AdminTab.KELUAR) onExit() else state.activeTab = tab
            }
        }
    }
}


// ─── Preview Langsung ─────────────────────────────────────────────────────────

@org.jetbrains.compose.ui.tooling.preview.Preview
@androidx.compose.runtime.Composable
private fun PreviewAdminPanelTablet() {
    com.cafe3rasa.kasir.theme.KasirCafeTheme {
        AdminPanel(state = com.cafe3rasa.kasir.state.AdminPanelState())
    }
}
