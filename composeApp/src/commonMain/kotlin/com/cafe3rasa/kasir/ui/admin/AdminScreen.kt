package com.cafe3rasa.kasir.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cafe3rasa.kasir.auth.UserAccount
import com.cafe3rasa.kasir.theme.KasirColors

/**
 * Panel Admin: Header di atas, Konten Tab dinamis, Navbar tetap di bawah.
 */
@Composable
fun AdminScreen(
    currentUser: UserAccount? = null,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier,
    store: AdminStore = remember { AdminStore() }
) {
    var tab by rememberSaveable { mutableStateOf(AdminTab.PRODUK) }

    BoxWithConstraints(modifier.fillMaxSize().background(KasirColors.Background)) {
        val compact = maxWidth < 840.dp
        Column(Modifier.fillMaxSize()) {
            AdminHeader(
                adminName = currentUser?.displayName ?: "Admin Maharewa",
                compact = compact
            )
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (tab) {
                    AdminTab.PRODUK -> ProdukScreen(store, compact)
                    AdminTab.LAPORAN -> LaporanScreen(store, compact)
                    AdminTab.MEJA -> MejaScreen(store, compact)
                }
            }
            AdminBottomNav(
                current = tab,
                onSelect = { tab = it },
                onLogout = onLogout,
                compact = compact
            )
        }
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun AdminScreenPreview() {
    AdminScreen()
}
