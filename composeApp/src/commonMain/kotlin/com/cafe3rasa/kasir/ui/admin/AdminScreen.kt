package com.cafe3rasa.kasir.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
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
 * - Header pojok kanan: Tombol Logout (konfirmasi) → kembali ke Login.
 * - Bottom nav: 4 tab (Produk, Laporan, Meja, Akun). Tidak ada tombol Keluar di nav.
 */
@Composable
fun AdminScreen(
    currentUser: UserAccount? = null,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier,
    store: AdminStore = remember { AdminStore() }
) {
    var tab by rememberSaveable { mutableStateOf(AdminTab.PRODUK) }
    var showInfoScreen by rememberSaveable { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }

    // Muat data dari server saat Panel Admin dibuka
    DisposableEffect(store) {
        store.start()
        onDispose { store.stop() }
    }
    // Pesan hasil aksi (berhasil / error dari server)
    LaunchedEffect(store.message) {
        store.message?.let { snackbar.showSnackbar(it.text) }
    }

    BoxWithConstraints(modifier.fillMaxSize().background(KasirColors.Background)) {
        val compact = maxWidth < 840.dp

        if (showInfoScreen) {
            // Halaman Informasi & Bantuan Admin (Hanya diakses oleh Admin)
            AdminInfoScreen(
                onBack = { showInfoScreen = false },
                compact = compact
            )
        } else {
            Column(Modifier.fillMaxSize()) {
                // Header: nama Tiga Rasa di kiri, tombol tanda tanya & logout di kanan
                AdminHeader(
                    adminName = currentUser?.displayName ?: "Admin Maharewa",
                    compact = compact,
                    onLogout = onLogout,
                    onOpenInfo = { showInfoScreen = true }
                )
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when (tab) {
                        AdminTab.PRODUK   -> ProdukScreen(store, compact)
                        AdminTab.LAPORAN  -> LaporanScreen(store, compact)
                        AdminTab.MEJA     -> MejaScreen(store, compact)
                        AdminTab.AKUN     -> AkunScreen(compact)
                    }
                }
                // Bottom nav: 4 tab navigasi
                AdminBottomNav(
                    current = tab,
                    onSelect = { tab = it },
                    compact = compact
                )
            }
        }
        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)
        )
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun AdminScreenPreview() {
    AdminScreen()
}
