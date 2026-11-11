package com.cafe3rasa.kasir.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cafe3rasa.kasir.state.AdminPanelState
import com.cafe3rasa.kasir.theme.KasirColors
import com.cafe3rasa.kasir.theme.KasirTypography

/**
 * 05 — Login panel admin (mockup: 1280x1070).
 * Background putih dengan dekorasi mint/peach, arch cream di tengah, kartu putih 438x624.
 */
@Composable
fun AdminLoginScreen(state: AdminPanelState, onBack: () -> Unit = {}) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(KasirColors.Surface)
    ) {
        // Kanvas desain 1280x1070 — diskalakan agar SELURUH layar login muat di jendela.
        val designWidth = 1280.dp
        val designHeight = 1070.dp
        val scaleByWidth = maxWidth.value / designWidth.value
        val scaleByHeight = maxHeight.value / designHeight.value
        val scale = minOf(1f, scaleByWidth, scaleByHeight)
        val density = LocalDensity.current

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.requiredSize(designWidth * scale, designHeight * scale)
            ) {
                CompositionLocalProvider(
                    LocalDensity provides Density(density.density * scale, density.fontScale)
                ) {
                    LoginCanvas(state, onBack)
                }
            }
        }
    }
}

@Composable
private fun LoginCanvas(state: AdminPanelState, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KasirColors.Surface)
    ) {
        // Dekorasi latar
        Box(
            Modifier
                .offset(x = (-161).dp, y = (-181).dp)
                .size(521.dp)
                .clip(CircleShape)
                .background(KasirColors.CreamTint)
        )
        Box(
            Modifier
                .offset(x = 842.dp, y = 671.dp)
                .size(615.dp)
                .clip(CircleShape)
                .background(KasirColors.PeachSoft)
        )
        Box(
            Modifier
                .offset(x = 249.7.dp, y = 119.7.dp)
                .size(75.6.dp)
                .border(2.dp, KasirColors.PrimaryTeal, CircleShape)
        )
        Box(
            Modifier
                .offset(x = 1057.dp, y = 140.dp)
                .size(12.dp)
                .clip(CircleShape)
                .background(KasirColors.PrimaryDarkNavy)
        )
        Box(
            Modifier
                .offset(x = 221.dp, y = 823.dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(KasirColors.AccentRust)
        )

        // Arch cream
        Box(
            Modifier
                .offset(x = 341.dp, y = 85.dp)
                .size(600.dp, 860.dp)
                .clip(RoundedCornerShape(topStart = 300.dp, topEnd = 300.dp, bottomEnd = 26.dp, bottomStart = 26.dp))
                .background(KasirColors.CreamArch)
        )

        // Kartu login
        Column(
            modifier = Modifier
                .offset(x = 421.dp, y = 205.dp)
                .size(438.dp, 624.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(KasirColors.Surface)
                .border(1.dp, KasirColors.Border, RoundedCornerShape(14.dp))
                .padding(horizontal = 40.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BrandLockup()
            Spacer(Modifier.height(26.dp))
            Text(
                text = "Selamat Datang",
                style = KasirTypography.headlineLarge.copy(fontSize = 25.6.sp),
                modifier = Modifier.fillMaxWidth(),
                color = KasirColors.TextPrimary
            )
            Text(
                text = "Masuk untuk melanjutkan.",
                style = KasirTypography.bodyLarge.copy(fontSize = 14.sp),
                modifier = Modifier.fillMaxWidth(),
                color = KasirColors.TextSecondary
            )

            Spacer(Modifier.height(26.dp))
            FieldLabel("Username")
            Spacer(Modifier.height(9.dp))
            LoginField(
                value = state.username,
                onValueChange = { state.username = it },
                placeholder = "Masukkan username",
                leading = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = KasirColors.InputPlaceholder,
                        modifier = Modifier.size(17.dp)
                    )
                }
            )

            Spacer(Modifier.height(19.dp))
            FieldLabel("Password")
            Spacer(Modifier.height(9.dp))
            LoginField(
                value = state.password,
                onValueChange = { state.password = it },
                placeholder = "Masukkan password",
                isPassword = !state.isPasswordVisible,
                leading = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = KasirColors.InputPlaceholder,
                        modifier = Modifier.size(16.dp)
                    )
                },
                trailing = {
                    Icon(
                        imageVector = if (state.isPasswordVisible) Icons.Default.VisibilityOff
                        else Icons.Default.Visibility,
                        contentDescription = "Tampilkan password",
                        tint = KasirColors.TextSecondary,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { state.isPasswordVisible = !state.isPasswordVisible }
                    )
                }
            )

            Spacer(Modifier.height(17.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = state.rememberMe,
                    onCheckedChange = { state.rememberMe = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = KasirColors.PrimaryTeal,
                        uncheckedColor = KasirColors.BorderLight
                    ),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    text = "Ingat saya",
                    fontSize = 13.4.sp,
                    color = KasirColors.TextSecondary
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Lupa password?",
                    fontSize = 13.4.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KasirColors.AccentRust
                )
            }

            state.loginError?.let { error ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = error,
                    fontSize = 12.5.sp,
                    color = KasirColors.AccentRust,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(49.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(KasirColors.PrimaryTeal)
                    .clickable { state.login() }
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Masuk",
                    fontSize = 15.2.sp,
                    fontWeight = FontWeight.Bold,
                    color = KasirColors.TextWhite
                )
                Spacer(Modifier.width(10.dp))
                Text("→", fontSize = 15.sp, color = KasirColors.TextWhite)
            }
        }

        Text(
            text = "© 2026 Tiga Rasa by Maharewa",
            fontSize = 12.sp,
            color = KasirColors.TextMuted,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 555.dp, y = 849.dp)
        )

        Text(
            text = "← Kembali ke Kasir",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = KasirColors.TextSecondary,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 28.dp, y = 24.dp)
                .clickable { onBack() }
        )
    }
}

@Composable
private fun BrandLockup() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(KasirColors.PrimaryDarkNavy),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "3R",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = KasirColors.TextWhite
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = "TIGA RASA",
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 3.sp,
            color = KasirColors.PrimaryDarkNavy
        )
        Text(
            text = "by Maharewa",
            fontSize = 11.sp,
            letterSpacing = 1.2.sp,
            color = KasirColors.TextMuted
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.4.sp,
        fontWeight = FontWeight.SemiBold,
        color = KasirColors.TextPrimary,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun LoginField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leading: @Composable () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
    isPassword: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(KasirColors.LogTableBg)
            .border(1.dp, KasirColors.Border, RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leading()
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    fontSize = 14.sp,
                    color = KasirColors.InputPlaceholder
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp, color = KasirColors.TextPrimary),
                cursorBrush = SolidColor(KasirColors.PrimaryTeal),
                visualTransformation =
                    if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                modifier = Modifier.fillMaxWidth()
            )
        }
        trailing?.invoke()
    }
}
