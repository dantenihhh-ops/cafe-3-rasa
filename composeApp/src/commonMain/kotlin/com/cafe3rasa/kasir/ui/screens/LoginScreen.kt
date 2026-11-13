package com.cafe3rasa.kasir.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cafe3rasa.kasir.auth.UserAccount
import com.cafe3rasa.kasir.auth.authenticate
import com.cafe3rasa.kasir.theme.KasirColors
import com.cafe3rasa.kasir.theme.KasirTypography
import org.jetbrains.compose.resources.imageResource
import pbl.composeapp.generated.resources.Res
import pbl.composeapp.generated.resources.logo_tiga_rasa
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale

// ─── Tambahan warna untuk halaman login ──────────────────────────────────────
private val ColorTealSoft  = Color(0xFFE7F2F1)
private val ColorCoralTint = Color(0xFFFDF0EE)
private val ColorCream     = Color(0xFFFFF9F4)

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
    val focusManager = LocalFocusManager.current
    val scrollState  = rememberScrollState()

    LaunchedEffect(Unit) { visible = true }

    val doLogin = {
        errorMsg = null
        when {
            username.isBlank() || password.isBlank() ->
                errorMsg = "Username dan password wajib diisi."
            else -> {
                isLoading = true
                val user = authenticate(username, password)
                isLoading = false
                if (user != null) onLoginSuccess(user)
                else errorMsg = "Username atau password salah."
            }
        }
    }

    // BoxWithConstraints untuk membaca ukuran layar aktual
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        val screenW = maxWidth
        val screenH = maxHeight
        val isCompact = screenW < 480.dp   // HP portrait

        // Ukuran lingkaran dekoratif sesuai layar
        val circleSize1 = (screenW * 0.75f).coerceAtMost(420.dp)
        val circleSize2 = (screenW * 0.90f).coerceAtMost(500.dp)
        val offset1     = (circleSize1 * 0.33f)
        val offset2     = (circleSize2 * 0.36f)

        // ── Dekorasi lingkaran ─────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(circleSize1)
                .offset(-offset1, -offset1)
                .background(ColorTealSoft, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(circleSize2)
                .align(Alignment.BottomEnd)
                .offset(offset2, offset2)
                .background(ColorCoralTint, CircleShape)
        )
        // Titik aksen – hanya tampil di layar lebih besar
        if (!isCompact) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .align(Alignment.TopEnd)
                    .offset((-screenW * 0.55f), screenH * 0.19f)
                    .background(KasirColors.PrimaryDarkNavy, CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.BottomStart)
                    .offset(screenW * 0.55f, -(screenH * 0.33f))
                    .background(KasirColors.AccentRust, CircleShape)
            )
        }

        // ── Kartu Login ────────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 4 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(
                        horizontal = if (isCompact) 16.dp else 32.dp,
                        vertical   = if (isCompact) 24.dp else 40.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Kartu form login
                Box(
                    modifier = Modifier
                        .widthIn(max = 460.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(if (isCompact) 20.dp else 24.dp))
                        .background(Color.White)
                        .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(if (isCompact) 20.dp else 24.dp))
                        .padding(
                            horizontal = if (isCompact) 20.dp else 32.dp,
                            vertical   = if (isCompact) 24.dp else 36.dp
                        )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        // Logo
                        val logo = imageResource(Res.drawable.logo_tiga_rasa)
                        Image(
                            bitmap = logo,
                            contentDescription = "Logo Tiga Rasa",
                            modifier = Modifier.size(if (isCompact) 72.dp else 100.dp),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(Modifier.height(if (isCompact) 14.dp else 18.dp))

                        Text(
                            "Selamat Datang",
                            style = KasirTypography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize   = if (isCompact) 20.sp else 24.sp,
                                color      = KasirColors.TextPrimary
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            "Masuk sebagai Admin atau Kasir",
                            style = KasirTypography.bodyMedium.copy(
                                fontSize = if (isCompact) 12.sp else 13.5.sp,
                                color    = KasirColors.TextMuted
                            ),
                            textAlign = TextAlign.Center,
                            modifier  = Modifier.fillMaxWidth().padding(top = 4.dp)
                        )

                        Spacer(Modifier.height(if (isCompact) 20.dp else 28.dp))

                        // Field Username
                        LoginFieldLabel("Username")
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it; errorMsg = null },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = {
                                Text("Masukkan username", color = KasirColors.TextSubtle,
                                    fontSize = if (isCompact) 13.sp else 14.sp)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = KasirColors.TextSubtle,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction    = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor    = KasirColors.PrimaryTeal,
                                unfocusedBorderColor  = KasirColors.BorderLight,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = KasirColors.Background,
                                cursorColor           = KasirColors.PrimaryTeal,
                                focusedTextColor      = KasirColors.TextPrimary,
                                unfocusedTextColor    = KasirColors.TextPrimary
                            )
                        )

                        Spacer(Modifier.height(if (isCompact) 12.dp else 16.dp))

                        // Field Password
                        LoginFieldLabel("Password")
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorMsg = null },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = {
                                Text("Masukkan password", color = KasirColors.TextSubtle,
                                    fontSize = if (isCompact) 13.sp else 14.sp)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = KasirColors.TextSubtle,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.Visibility
                                                  else Icons.Default.VisibilityOff,
                                    contentDescription = if (showPassword) "Sembunyikan" else "Tampilkan",
                                    tint = if (showPassword) KasirColors.PrimaryTeal else KasirColors.TextSubtle,
                                    modifier = Modifier
                                        .size(20.dp)
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
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor    = KasirColors.PrimaryTeal,
                                unfocusedBorderColor  = KasirColors.BorderLight,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = KasirColors.Background,
                                cursorColor           = KasirColors.PrimaryTeal,
                                focusedTextColor      = KasirColors.TextPrimary,
                                unfocusedTextColor    = KasirColors.TextPrimary
                            )
                        )

                        // Pesan Error
                        AnimatedVisibility(visible = errorMsg != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFDF0EE))
                                    .border(1.dp, KasirColors.AccentRust, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    errorMsg ?: "",
                                    style = KasirTypography.bodySmall.copy(
                                        color      = KasirColors.AccentRust,
                                        fontWeight = FontWeight.Medium,
                                        fontSize   = 13.sp
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(if (isCompact) 18.dp else 22.dp))

                        // Tombol Masuk
                        Button(
                            onClick  = { doLogin() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isCompact) 46.dp else 50.dp),
                            shape   = RoundedCornerShape(12.dp),
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
                                    "Masuk  →",
                                    fontWeight = FontWeight.Bold,
                                    fontSize   = if (isCompact) 14.sp else 15.sp
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(if (isCompact) 16.dp else 24.dp))

                // Panduan Akun Demo
                Box(
                    modifier = Modifier
                        .widthIn(max = 460.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ColorCream)
                        .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(12.dp))
                        .padding(if (isCompact) 14.dp else 16.dp)
                ) {
                    Column {
                        Text(
                            "Akun Demo",
                            style = KasirTypography.labelSmall.copy(
                                fontSize   = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color      = KasirColors.TextMuted
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        DemoAccountRow("Kasir", "kasir", "kasir123", KasirColors.PrimaryTeal, isCompact)
                        Spacer(Modifier.height(4.dp))
                        DemoAccountRow("Admin", "admin", "admin123", KasirColors.AccentRust, isCompact)
                    }
                }

                Spacer(Modifier.height(if (isCompact) 14.dp else 20.dp))
                Text(
                    "© 2026 Tiga Rasa by Maharewa",
                    style = KasirTypography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        color    = KasirColors.TextSubtle
                    )
                )
            }
        }
    }
}

@Composable
private fun LoginFieldLabel(text: String) {
    Text(
        text,
        style = KasirTypography.labelMedium.copy(
            fontSize   = 13.sp,
            fontWeight = FontWeight.Medium,
            color      = KasirColors.TextSecondary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

@Composable
private fun DemoAccountRow(
    roleLabel: String,
    username:  String,
    password:  String,
    roleColor: Color,
    isCompact: Boolean = false
) {
    if (isCompact) {
        // Di HP: susun vertikal agar tidak kepotong
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(roleColor)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    roleLabel,
                    style = KasirTypography.labelSmall.copy(
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                )
            }
            Text(
                "user: $username  ·  pass: $password",
                style = KasirTypography.bodySmall.copy(
                    fontSize = 11.5.sp,
                    color    = KasirColors.TextSecondary
                )
            )
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(roleColor)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    roleLabel,
                    style = KasirTypography.labelSmall.copy(
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                )
            }
            Text(
                "Username: $username",
                style = KasirTypography.bodySmall.copy(
                    fontSize = 12.sp,
                    color    = KasirColors.TextSecondary
                )
            )
            Text("·", color = KasirColors.TextSubtle)
            Text(
                "Password: $password",
                style = KasirTypography.bodySmall.copy(
                    fontSize = 12.sp,
                    color    = KasirColors.TextSecondary
                )
            )
        }
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun LoginScreenPreview() {
    LoginScreen()
}
