package com.cafe3rasa.kasir.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cafe3rasa.kasir.auth.UserAccount
import com.cafe3rasa.kasir.auth.UserRole
import com.cafe3rasa.kasir.auth.addCashierAccount
import com.cafe3rasa.kasir.auth.deleteCashierAccount
import com.cafe3rasa.kasir.auth.updateCashierAccount
import com.cafe3rasa.kasir.auth.userAccountsState
import com.cafe3rasa.kasir.theme.KasirColors

// ─── Dialog: Tambah Akun Kasir ────────────────────────────────────────────────

@Composable
private fun TambahAkunDialog(
    onDismiss: () -> Unit,
    onSaved: (errorMsg: String?) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = KasirColors.Surface,
        shape = RoundedCornerShape(12.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(32.dp).background(KasirColors.SelectedTableBg, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, null, tint = KasirColors.PrimaryTeal, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    "Tambah Akun Kasir",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = KasirColors.TextPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (errorMsg != null) {
                    Box(
                        Modifier.fillMaxWidth()
                            .background(Color(0xFFFFE5E5), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(errorMsg!!, fontSize = 12.5.sp, color = Color(0xFFB91C1C))
                    }
                }
                Column {
                    FormLabel("Username")
                    FormField(
                        value = username,
                        onValueChange = { username = it; errorMsg = null },
                        placeholder = "contoh: kasir3",
                        leadingIcon = Icons.Default.Person
                    )
                }
                Column {
                    FormLabel("Nama Kasir")
                    FormField(
                        value = displayName,
                        onValueChange = { displayName = it; errorMsg = null },
                        placeholder = "contoh: Siti Rahayu",
                        leadingIcon = Icons.Default.People
                    )
                }
                Column {
                    FormLabel("Password")
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMsg = null },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("min. 4 karakter", color = KasirColors.TextSubtle, fontSize = 13.5.sp) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(Icons.Default.Lock, null, tint = KasirColors.TextSubtle, modifier = Modifier.size(16.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = KasirColors.TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.5.sp, color = KasirColors.TextPrimary),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = KasirColors.Surface,
                            unfocusedContainerColor = KasirColors.Background,
                            focusedBorderColor = KasirColors.PrimaryTeal,
                            unfocusedBorderColor = KasirColors.BorderLight,
                            cursorColor = KasirColors.PrimaryTeal
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    addCashierAccount(username, displayName, password) { err ->
                        if (err != null) {
                            errorMsg = err
                        } else {
                            onSaved(null)
                            onDismiss()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = KasirColors.PrimaryTeal,
                    contentColor = KasirColors.TextWhite
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = androidx.compose.foundation.BorderStroke(1.dp, KasirColors.Border),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = KasirColors.TextPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Batal", fontSize = 13.sp)
            }
        }
    )
}

// ─── Dialog: Edit Akun Kasir ──────────────────────────────────────────────────

@Composable
private fun EditAkunDialog(
    account: UserAccount,
    onDismiss: () -> Unit
) {
    var displayName by remember { mutableStateOf(account.displayName) }
    var newPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = KasirColors.Surface,
        shape = RoundedCornerShape(12.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(32.dp).background(KasirColors.SelectedTableBg, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, null, tint = KasirColors.PrimaryTeal, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    "Edit Akun: @${account.username}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = KasirColors.TextPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (errorMsg != null) {
                    Box(
                        Modifier.fillMaxWidth()
                            .background(Color(0xFFFFE5E5), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(errorMsg!!, fontSize = 12.5.sp, color = Color(0xFFB91C1C))
                    }
                }
                Column {
                    FormLabel("Nama Kasir")
                    FormField(
                        value = displayName,
                        onValueChange = { displayName = it; errorMsg = null },
                        placeholder = "Nama lengkap kasir",
                        leadingIcon = Icons.Default.People
                    )
                }
                Column {
                    FormLabel("Password Baru (kosongkan jika tidak diganti)")
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it; errorMsg = null },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Biarkan kosong = tidak diubah", color = KasirColors.TextSubtle, fontSize = 13.sp) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(Icons.Default.Lock, null, tint = KasirColors.TextSubtle, modifier = Modifier.size(16.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = KasirColors.TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.5.sp, color = KasirColors.TextPrimary),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = KasirColors.Surface,
                            unfocusedContainerColor = KasirColors.Background,
                            focusedBorderColor = KasirColors.PrimaryTeal,
                            unfocusedBorderColor = KasirColors.BorderLight,
                            cursorColor = KasirColors.PrimaryTeal
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    updateCashierAccount(account.id, displayName, newPassword) { err ->
                        if (err != null) {
                            errorMsg = err
                        } else {
                            onDismiss()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = KasirColors.PrimaryTeal,
                    contentColor = KasirColors.TextWhite
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Update", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = androidx.compose.foundation.BorderStroke(1.dp, KasirColors.Border),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = KasirColors.TextPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Batal", fontSize = 13.sp)
            }
        }
    )
}

// ─── Dialog: Konfirmasi Hapus ─────────────────────────────────────────────────

@Composable
private fun HapusAkunDialog(
    account: UserAccount,
    onDismiss: () -> Unit
) {
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = KasirColors.Surface,
        shape = RoundedCornerShape(12.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(32.dp).background(Color(0xFFFFE5E5), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Delete, null, tint = Color(0xFFB91C1C), modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text("Hapus Akun Kasir", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = KasirColors.TextPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (errorMsg != null) {
                    Box(
                        Modifier.fillMaxWidth()
                            .background(Color(0xFFFFE5E5), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(errorMsg!!, fontSize = 12.5.sp, color = Color(0xFFB91C1C))
                    }
                }
                Text(
                    "Yakin ingin menghapus akun kasir berikut?",
                    fontSize = 13.5.sp,
                    color = KasirColors.TextMuted
                )
                Box(
                    Modifier.fillMaxWidth()
                        .background(KasirColors.Background, RoundedCornerShape(8.dp))
                        .border(1.dp, KasirColors.Border, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(account.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = KasirColors.TextPrimary)
                        Text("@${account.username}", fontSize = 12.5.sp, color = KasirColors.TextMuted)
                    }
                }
                Text("Aksi ini tidak dapat dibatalkan.", fontSize = 12.sp, color = Color(0xFFB91C1C))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    deleteCashierAccount(account.id) { err ->
                        if (err != null) {
                            errorMsg = err
                        } else {
                            onDismiss()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB91C1C),
                    contentColor = KasirColors.TextWhite
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Hapus", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = androidx.compose.foundation.BorderStroke(1.dp, KasirColors.Border),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = KasirColors.TextPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Batal", fontSize = 13.sp)
            }
        }
    )
}

// ─── Kartu Akun Kasir ─────────────────────────────────────────────────────────

@Composable
private fun KasirAccountCard(
    account: UserAccount,
    isAdmin: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isAdmin) KasirColors.SelectedTableBg else KasirColors.Surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isAdmin) KasirColors.PrimaryTeal else KasirColors.Border
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar inisial
            Box(
                Modifier
                    .size(42.dp)
                    .background(
                        if (isAdmin) KasirColors.PrimaryTeal else KasirColors.PrimaryDarkNavy,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    account.displayName.take(2).uppercase(),
                    color = KasirColors.TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        account.displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = KasirColors.TextPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    if (isAdmin) {
                        Pill("ADMIN", KasirColors.PrimaryTeal, KasirColors.TextWhite, fontSize = 10f)
                    } else {
                        Pill("KASIR", KasirColors.PrimaryDarkNavy, KasirColors.TextWhite, fontSize = 10f)
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    "@${account.username}",
                    fontSize = 12.5.sp,
                    color = KasirColors.TextMuted
                )
            }

            // Tombol Edit & Hapus (hanya untuk akun KASIR, admin dilindungi)
            if (!isAdmin) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedIconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(34.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, KasirColors.Border),
                        shape = RoundedCornerShape(8.dp),
                        colors = IconButtonDefaults.outlinedIconButtonColors(contentColor = KasirColors.PrimaryTeal)
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                    }
                    OutlinedIconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(34.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCCCC)),
                        shape = RoundedCornerShape(8.dp),
                        colors = IconButtonDefaults.outlinedIconButtonColors(contentColor = Color(0xFFB91C1C))
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                    }
                }
            } else {
                // Akun admin dilindungi — tampilkan badge
                Box(
                    Modifier
                        .background(KasirColors.SelectedTableBg, RoundedCornerShape(6.dp))
                        .border(1.dp, KasirColors.PrimaryTeal.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, null, tint = KasirColors.PrimaryTeal, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Dilindungi", fontSize = 11.sp, color = KasirColors.PrimaryTeal, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ─── Halaman Utama: Manajemen Akun ───────────────────────────────────────────

@Composable
fun AkunScreen(compact: Boolean = false) {
    // State untuk dialog
    var showTambah by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<UserAccount?>(null) }
    var hapusTarget by remember { mutableStateOf<UserAccount?>(null) }
    var snackMessage by remember { mutableStateOf<String?>(null) }

    // Daftar akun dimuat dari server (tabel users) saat halaman dibuka
    var loadError by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { loadError = com.cafe3rasa.kasir.auth.refreshAccounts() }
    LaunchedEffect(loadError) { loadError?.let { snackMessage = "Gagal memuat akun: $it" } }
    val accounts = userAccountsState

    if (showTambah) {
        TambahAkunDialog(
            onDismiss = { showTambah = false },
            onSaved = { snackMessage = "Akun kasir berhasil ditambahkan" }
        )
    }
    editTarget?.let { acc ->
        EditAkunDialog(account = acc, onDismiss = { editTarget = null })
    }
    hapusTarget?.let { acc ->
        HapusAkunDialog(account = acc, onDismiss = { hapusTarget = null })
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(KasirColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(if (compact) 16.dp else 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Header ───────────────────────────────────────────────────────
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PageTitle(
                title = "Manajemen Akun",
                subtitle = "${accounts.size} akun terdaftar · ${accounts.count { it.role == UserRole.KASIR }} kasir",
                compact = compact
            )
            Button(
                onClick = { showTambah = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = KasirColors.PrimaryTeal,
                    contentColor = KasirColors.TextWhite
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    if (compact) "Tambah" else "Tambah Akun Kasir",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        // ── Snackbar feedback ─────────────────────────────────────────────
        snackMessage?.let { msg ->
            Box(
                Modifier.fillMaxWidth()
                    .background(Color(0xFFE6FAF3), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF10B981), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✓ $msg", fontSize = 13.sp, color = Color(0xFF065F46), fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    TextButton(onClick = { snackMessage = null }) {
                        Text("Tutup", fontSize = 12.sp, color = Color(0xFF065F46))
                    }
                }
            }
        }

        // ── Info Panel ────────────────────────────────────────────────────
        AdminCard {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(36.dp).background(KasirColors.SelectedTableBg, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Settings, null, tint = KasirColors.PrimaryTeal, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text("Hak Akses", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = KasirColors.TextPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Akun Admin dapat mengelola produk, meja, laporan, dan akun kasir. " +
                        "Akun Kasir hanya dapat memproses transaksi dan mengubah ketersediaan menu.",
                        fontSize = 12.5.sp,
                        color = KasirColors.TextMuted,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // ── Daftar Akun ────────────────────────────────────────────────────
        AdminCard {
            CardTitle(
                text = "Daftar Akun",
                icon = Icons.Default.People,
                trailing = "${accounts.size} akun"
            )
            Spacer(Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Tampilkan Admin dulu (dilindungi), kemudian kasir
                accounts.sortedWith(compareBy { it.role != UserRole.ADMIN }).forEach { account ->
                    KasirAccountCard(
                        account = account,
                        isAdmin = account.role == UserRole.ADMIN,
                        onEdit = { editTarget = account },
                        onDelete = { hapusTarget = account }
                    )
                }
            }
        }
    }
}
