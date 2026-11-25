package com.cafe3rasa.kasir.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cafe3rasa.kasir.auth.AuthRepository
import com.cafe3rasa.kasir.auth.UserAccount
import com.cafe3rasa.kasir.data.ApiClient
import com.cafe3rasa.kasir.data.ApiException
import com.cafe3rasa.kasir.theme.KasirColors
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.imageResource
import pbl.composeapp.generated.resources.Res
import pbl.composeapp.generated.resources.logo_tiga_rasa

@Composable
fun LoginScreen(
    onLoginSuccess: (UserAccount) -> Unit = {}
) {
    var username     by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var errorMsg     by remember { mutableStateOf<String?>(null) }
    var isLoading    by remember { mutableStateOf(false) }
    var visible      by remember { mutableStateOf(false) }

    // Alamat server backend (tersimpan di HP, cukup diatur sekali)
    var serverUrl        by remember { mutableStateOf(ApiClient.serverUrl) }
    var showServerDialog by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val scrollState  = rememberScrollState()
    val scope        = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        visible = true
        // Pertama kali dibuka di HP: alamat server belum ada -> langsung minta diatur
        if (serverUrl.isBlank()) showServerDialog = true
    }

    val doLogin: () -> Unit = doLogin@{
        if (isLoading) return@doLogin
        errorMsg = null
        when {
            serverUrl.isBlank() -> {
                errorMsg = "Atur alamat server terlebih dahulu."
                showServerDialog = true
            }
            username.isBlank() || password.isBlank() ->
                errorMsg = "Username dan password wajib diisi."
            else -> {
                isLoading = true
                focusManager.clearFocus()
                scope.launch {
                    try {
                        val user = AuthRepository.login(username.trim(), password)
                        onLoginSuccess(user)
                    } catch (e: ApiException) {
                        errorMsg = if (e.isUnauthorized) "Username atau password salah." else e.message
                    } catch (e: Exception) {
                        errorMsg = "Terjadi kesalahan: ${e.message ?: "tidak diketahui"}"
                    } finally {
                        isLoading = false
                    }
                }
            }
        }
    }

    if (showServerDialog) {
        ServerSettingsDialog(
            initialUrl = serverUrl,
            onDismiss = { showServerDialog = false },
            onSave = { newUrl ->
                ApiClient.serverUrl = newUrl
                serverUrl = ApiClient.serverUrl
                showServerDialog = false
                errorMsg = null
            }
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(KasirColors.Background)
    ) {
        val screenW = maxWidth
        val isCompact = screenW < 480.dp

        // ── 1. Background Gelombang Organik Sesuai Tema Proyek ────────────────
        LoginBackgroundWaves(
            modifier = Modifier.fillMaxSize()
        )

        // ── 2. Konten Form Utama ─────────────────────────────────────────────
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 6 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(
                        start  = if (isCompact) 20.dp else 36.dp,
                        end    = if (isCompact) 20.dp else 36.dp,
                        top    = if (isCompact) 16.dp else 24.dp,
                        bottom = if (isCompact) 36.dp else 52.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // ── Logo Asli Tiga Rasa (Sedikit Diperbesar & Digeser ke Atas) ──
                val logo = imageResource(Res.drawable.logo_tiga_rasa)
                Image(
                    bitmap = logo,
                    contentDescription = "Logo Tiga Rasa",
                    modifier = Modifier.size(if (isCompact) 136.dp else 164.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.height(14.dp))

                // ── Judul & Subtitle Sesuai Tema Proyek ──
                Text(
                    text = "Selamat Datang",
                    fontSize = if (isCompact) 26.sp else 30.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    color = KasirColors.PrimaryDarkNavy,
                    letterSpacing = (-0.5).sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Masuk untuk melanjutkan ke Cafe 3 Rasa",
                    fontSize = if (isCompact) 13.5.sp else 14.5.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.SansSerif,
                    color = KasirColors.TextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(if (isCompact) 18.dp else 24.dp))

                // ── 3. Kartu Login Putih Solid (Height Ditambah Lebih Lega & Simetris) ──
                Box(
                    modifier = Modifier
                        .widthIn(max = 420.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(if (isCompact) 26.dp else 28.dp))
                        .background(KasirColors.Surface)
                        .border(
                            width = 1.dp,
                            color = KasirColors.BorderLight,
                            shape = RoundedCornerShape(if (isCompact) 26.dp else 28.dp)
                        )
                        .padding(
                            horizontal = if (isCompact) 22.dp else 28.dp,
                            vertical   = if (isCompact) 32.dp else 38.dp
                        )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Input Username (Wajib Menggunakan FontFamily.SansSerif)
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it; errorMsg = null },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            singleLine = true,
                            textStyle = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize   = 14.5.sp,
                                fontWeight = FontWeight.Normal,
                                color      = KasirColors.TextPrimary
                            ),
                            placeholder = {
                                Text(
                                    "Masukkan username",
                                    color = KasirColors.TextSubtle,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.SansSerif
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = KasirColors.PrimaryTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction    = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor      = KasirColors.PrimaryTeal,
                                unfocusedBorderColor    = KasirColors.BorderLight,
                                focusedContainerColor   = KasirColors.Surface,
                                unfocusedContainerColor = KasirColors.SurfaceLight,
                                cursorColor             = KasirColors.PrimaryTeal,
                                focusedTextColor        = KasirColors.TextPrimary,
                                unfocusedTextColor      = KasirColors.TextPrimary
                            )
                        )

                        Spacer(Modifier.height(16.dp))

                        // Input Password (Wajib Menggunakan FontFamily.SansSerif)
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorMsg = null },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            singleLine = true,
                            textStyle = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize   = 14.5.sp,
                                fontWeight = FontWeight.Normal,
                                color      = KasirColors.TextPrimary
                            ),
                            placeholder = {
                                Text(
                                    "Masukkan password",
                                    color = KasirColors.TextSubtle,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.SansSerif
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = KasirColors.PrimaryTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.Visibility
                                                  else Icons.Default.VisibilityOff,
                                    contentDescription = if (showPassword) "Sembunyikan" else "Tampilkan",
                                    tint = KasirColors.TextSubtle,
                                    modifier = Modifier
                                        .size(21.dp)
                                        .clickable { showPassword = !showPassword }
                                )
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None
                                                   else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction    = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { doLogin() }),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor      = KasirColors.PrimaryTeal,
                                unfocusedBorderColor    = KasirColors.BorderLight,
                                focusedContainerColor   = KasirColors.Surface,
                                unfocusedContainerColor = KasirColors.SurfaceLight,
                                cursorColor             = KasirColors.PrimaryTeal,
                                focusedTextColor        = KasirColors.TextPrimary,
                                unfocusedTextColor      = KasirColors.TextPrimary
                            )
                        )

                        // Pesan Error Aksen Tema
                        AnimatedVisibility(visible = errorMsg != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(KasirColors.PeachSoft)
                                    .border(1.dp, KasirColors.AccentRust, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = errorMsg ?: "",
                                    color = KasirColors.AccentRust,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.SansSerif
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // Tombol Utama "Masuk"
                        Button(
                            onClick  = { doLogin() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape   = RoundedCornerShape(16.dp),
                            enabled = !isLoading,
                            colors  = ButtonDefaults.buttonColors(
                                containerColor         = KasirColors.PrimaryTeal,
                                contentColor           = Color.White,
                                disabledContainerColor = KasirColors.PrimaryTeal
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(20.dp),
                                    color       = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Masuk",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // ── Info & pengaturan alamat server ──
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showServerDialog = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = KasirColors.TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (serverUrl.isBlank()) "Atur Server"
                               else "Server: ${serverUrl.removePrefix("http://").removePrefix("https://")}",
                        fontSize = 12.5.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = if (serverUrl.isBlank()) KasirColors.AccentRust else KasirColors.TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Dialog pengaturan alamat server backend.
 * Contoh isi: 192.168.43.120:5000 (IP laptop yang menjalankan "npm run dev").
 */
@Composable
private fun ServerSettingsDialog(
    initialUrl: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var input     by remember { mutableStateOf(initialUrl.removePrefix("http://")) }
    var testing   by remember { mutableStateOf(false) }
    var resultMsg by remember { mutableStateOf<String?>(null) }
    var resultOk  by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = KasirColors.Surface,
        icon = {
            Icon(Icons.Default.Wifi, contentDescription = null, tint = KasirColors.PrimaryTeal)
        },
        title = {
            Text(
                "Alamat Server",
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = KasirColors.PrimaryDarkNavy
            )
        },
        text = {
            Column {
                Text(
                    "Isi dengan IP laptop yang menjalankan server (npm run dev). " +
                        "Cek IP lewat CMD di laptop: ipconfig → IPv4 Address.",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = KasirColors.TextMuted
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it.trim(); resultMsg = null },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("192.168.43.120:5000", color = KasirColors.TextSubtle) },
                    textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.5.sp, color = KasirColors.TextPrimary),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = KasirColors.PrimaryTeal,
                        unfocusedBorderColor = KasirColors.BorderLight,
                        cursorColor          = KasirColors.PrimaryTeal
                    )
                )
                if (resultMsg != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = (if (resultOk) "✓ " else "✕ ") + resultMsg,
                        fontSize = 12.5.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium,
                        color = if (resultOk) KasirColors.PrimaryTeal else KasirColors.AccentRust
                    )
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        testing = true
                        resultMsg = null
                        scope.launch {
                            try {
                                resultMsg = ApiClient.checkHealth(input)
                                resultOk = true
                            } catch (e: ApiException) {
                                resultMsg = e.message
                                resultOk = false
                            } finally {
                                testing = false
                            }
                        }
                    },
                    enabled = !testing && input.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (testing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = KasirColors.PrimaryTeal,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Tes Koneksi", color = KasirColors.PrimaryTeal, fontFamily = FontFamily.SansSerif)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(input) },
                enabled = input.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KasirColors.PrimaryTeal)
            ) {
                Text("Simpan", color = Color.White, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = KasirColors.TextMuted, fontFamily = FontFamily.SansSerif)
            }
        }
    )
}

/**
 * Background Gelombang Organik Menggunakan Palet Solid Resmi Tema Proyek:
 * - KasirColors.PrimaryTeal (#318380)
 * - KasirColors.MintSoft (#E8F2F1)
 * - KasirColors.CreamTint (#DDEDEC)
 */
@Composable
private fun LoginBackgroundWaves(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // ── Pojok Kiri Atas: Gelombang Berlapis Tema Resmi ──
        // Lapisan 1: Paling Luar (CreamTint tema resmi #DDEDEC)
        val pathTopOuter = Path().apply {
            moveTo(0f, 0f)
            lineTo(w * 0.65f, 0f)
            cubicTo(
                w * 0.50f, h * 0.12f,
                w * 0.15f, h * 0.22f,
                0f, h * 0.28f
            )
            close()
        }
        drawPath(pathTopOuter, color = KasirColors.CreamTint)

        // Lapisan 2: Tengah (MintSoft tema resmi #E8F2F1)
        val pathTopMid = Path().apply {
            moveTo(0f, 0f)
            lineTo(w * 0.48f, 0f)
            cubicTo(
                w * 0.38f, h * 0.09f,
                w * 0.10f, h * 0.16f,
                0f, h * 0.22f
            )
            close()
        }
        drawPath(pathTopMid, color = KasirColors.MintSoft)

        // Lapisan 3: Hijau Utama Tema (PrimaryTeal #318380)
        val pathTopTeal = Path().apply {
            moveTo(0f, 0f)
            lineTo(w * 0.38f, 0f)
            cubicTo(
                w * 0.28f, h * 0.07f,
                w * 0.06f, h * 0.12f,
                0f, h * 0.17f
            )
            close()
        }
        drawPath(pathTopTeal, color = KasirColors.PrimaryTeal)

        // ── Pojok Kanan Bawah: Gelombang Berlapis Tema Resmi ──
        // Lapisan 1: Paling Luar (CreamTint tema resmi #DDEDEC)
        val pathBottomOuter = Path().apply {
            moveTo(w, h)
            lineTo(w * 0.25f, h)
            cubicTo(
                w * 0.45f, h * 0.88f,
                w * 0.75f, h * 0.78f,
                w, h * 0.70f
            )
            close()
        }
        drawPath(pathBottomOuter, color = KasirColors.CreamTint)

        // Lapisan 2: Tengah (MintSoft tema resmi #E8F2F1)
        val pathBottomMid = Path().apply {
            moveTo(w, h)
            lineTo(w * 0.40f, h)
            cubicTo(
                w * 0.58f, h * 0.90f,
                w * 0.82f, h * 0.83f,
                w, h * 0.78f
            )
            close()
        }
        drawPath(pathBottomMid, color = KasirColors.MintSoft)

        // Lapisan 3: Hijau Utama Tema (PrimaryTeal #318380)
        val pathBottomTeal = Path().apply {
            moveTo(w, h)
            lineTo(w * 0.58f, h)
            cubicTo(
                w * 0.70f, h * 0.93f,
                w * 0.88f, h * 0.88f,
                w, h * 0.84f
            )
            close()
        }
        drawPath(pathBottomTeal, color = KasirColors.PrimaryTeal)
    }
}
