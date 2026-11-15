package com.cafe3rasa.kasir.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cafe3rasa.kasir.theme.KasirColors
import com.cafe3rasa.kasir.theme.KasirTypography
import org.jetbrains.compose.resources.painterResource
import pbl.composeapp.generated.resources.Res
import pbl.composeapp.generated.resources.logo_tiga_rasa

enum class AdminTab(val label: String, val shortLabel: String, val icon: ImageVector) {
    PRODUK("Produk & Kategori", "Produk", Icons.Default.Restaurant),
    LAPORAN("Log & Statistik", "Laporan", Icons.Default.BarChart),
    MEJA("Meja & QR", "Meja & QR", Icons.Default.QrCode),
    AKUN("Setting Akun", "Akun", Icons.Default.People)
}

const val COMPACT_MAX_WIDTH_DP = 840

@Composable
fun AdminHeader(
    adminName: String = "Admin Maharewa",
    compact: Boolean = false,
    onLogout: () -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = KasirColors.Surface,
            title = {
                Text(
                    "Konfirmasi Logout",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = KasirColors.TextPrimary
                )
            },
            text = {
                Text(
                    "Apakah Anda yakin ingin keluar dari Admin Panel dan kembali ke halaman Login?",
                    fontSize = 13.5.sp,
                    color = KasirColors.TextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KasirColors.AccentRust,
                        contentColor = KasirColors.TextWhite
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Keluar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    border = BorderStroke(1.dp, KasirColors.Border),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = KasirColors.TextPrimary
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Batal", fontSize = 13.sp)
                }
            }
        )
    }

    Surface(
        color = KasirColors.Surface,
        border = BorderStroke(1.dp, KasirColors.Border)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = if (compact) 12.dp else 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(Res.drawable.logo_tiga_rasa),
                contentDescription = "Logo",
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "Tiga Rasa",
                fontWeight = FontWeight.Bold,
                fontSize = if (compact) 17.sp else 20.sp,
                color = KasirColors.TextPrimary
            )
            Spacer(Modifier.width(8.dp))
            Pill("ADMIN", KasirColors.PrimaryDarkNavy, KasirColors.TextWhite)

            Spacer(Modifier.weight(1f))

            if (!compact) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = KasirColors.TextMuted,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Hari Ini, Operasional Aktif", fontSize = 13.sp, color = KasirColors.TextMuted)
                Spacer(Modifier.width(14.dp))
                Box(Modifier.width(1.dp).height(24.dp).background(KasirColors.Border))
                Spacer(Modifier.width(14.dp))
            }

            // Profil Pojok Kanan Diubah Menjadi Tombol Logout Menuju Login
            Surface(
                onClick = { showLogoutDialog = true },
                color = KasirColors.SurfaceLight,
                border = BorderStroke(1.dp, KasirColors.AccentRust),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Row(
                    Modifier.padding(horizontal = if (compact) 8.dp else 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(22.dp)
                            .background(KasirColors.AccentRust, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            adminName.take(2).uppercase(),
                            color = KasirColors.TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp
                        )
                    }
                    if (!compact) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            adminName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = KasirColors.TextPrimary
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.width(1.dp).height(16.dp).background(KasirColors.Border))
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Logout",
                        tint = KasirColors.AccentRust,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Keluar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = KasirColors.AccentRust
                    )
                }
            }
        }
    }
}

@Composable
fun AdminBottomNav(
    current: AdminTab,
    onSelect: (AdminTab) -> Unit,
    compact: Boolean = false
) {
    Surface(
        color = KasirColors.Surface,
        border = BorderStroke(1.dp, KasirColors.Border)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(if (compact) 54.dp else 66.dp)
                .padding(horizontal = if (compact) 4.dp else 24.dp)
        ) {
            AdminTab.entries.forEach { tab ->
                val active = tab == current
                NavItem(
                    label = if (compact) tab.shortLabel else tab.label,
                    icon = tab.icon,
                    color = if (active) KasirColors.PrimaryTeal else KasirColors.TextMuted,
                    active = active,
                    modifier = Modifier.weight(1f),
                    compact = compact,
                    onClick = { onSelect(tab) }
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    icon: ImageVector,
    color: Color,
    active: Boolean,
    modifier: Modifier,
    compact: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (active) {
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .width(56.dp)
                    .height(3.dp)
                    .background(KasirColors.PrimaryTeal, RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(5.dp))
            Text(
                label,
                color = color,
                fontSize = if (compact) 11.5.sp else 13.sp,
                maxLines = 1,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun AdminCard(
    modifier: Modifier = Modifier,
    padding: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = KasirColors.Surface,
        border = BorderStroke(1.dp, KasirColors.Border)
    ) {
        Column(Modifier.padding(padding), content = content)
    }
}

@Composable
fun CardTitle(
    text: String,
    icon: ImageVector,
    iconTint: Color = KasirColors.PrimaryTeal,
    trailing: String? = null
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = KasirColors.TextPrimary,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) {
            Text(trailing, fontSize = 12.5.sp, color = KasirColors.TextMuted)
        }
    }
}

@Composable
fun Pill(text: String, bg: Color, fg: Color, fontSize: Float = 11f, radius: Dp = 999.dp) {
    Text(
        text,
        color = fg,
        fontWeight = FontWeight.Bold,
        fontSize = fontSize.sp,
        letterSpacing = 0.3.sp,
        modifier = Modifier
            .background(bg, RoundedCornerShape(radius))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
fun ChoiceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    activeBg: Color = KasirColors.PrimaryTeal,
    activeFg: Color = KasirColors.TextWhite,
    count: Int? = null,
    soft: Boolean = false
) {
    val bg = when {
        !selected -> KasirColors.Surface
        soft -> KasirColors.SelectedTableBg
        else -> activeBg
    }
    val fg = when {
        !selected -> KasirColors.TextSecondary
        soft -> KasirColors.PrimaryTeal
        else -> activeFg
    }
    val border = if (selected) (if (soft) KasirColors.PrimaryTeal else activeBg) else KasirColors.BorderLight

    Row(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, color = fg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        if (count != null) {
            Spacer(Modifier.width(8.dp))
            Text(
                "$count",
                color = if (selected && !soft) KasirColors.TextPrimary else fg,
                fontSize = 11.5.sp,
                modifier = Modifier
                    .background(
                        if (selected && !soft) KasirColors.Background else KasirColors.NeutralEmptyBg,
                        RoundedCornerShape(999.dp)
                    )
                    .padding(horizontal = 7.dp, vertical = 1.dp)
            )
        }
    }
}

@Composable
fun FormLabel(text: String) {
    Text(text, fontWeight = FontWeight.Medium, fontSize = 12.5.sp, color = KasirColors.TextSecondary)
    Spacer(Modifier.height(6.dp))
}

@Composable
fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    singleLine: Boolean = true,
    numeric: Boolean = false,
    minLines: Int = 1,
    leadingIcon: ImageVector? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = singleLine,
        minLines = minLines,
        placeholder = { Text(placeholder, color = KasirColors.TextSubtle, fontSize = 13.5.sp) },
        leadingIcon = leadingIcon?.let {
            { Icon(it, null, tint = KasirColors.TextSubtle, modifier = Modifier.size(16.dp)) }
        },
        keyboardOptions = if (numeric) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
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

@Composable
fun PageTitle(title: String, subtitle: String, compact: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            title,
            fontWeight = FontWeight.Bold,
            fontSize = if (compact) 20.sp else 24.sp,
            color = KasirColors.TextPrimary
        )
        Text(subtitle, fontSize = 13.5.sp, color = KasirColors.TextMuted)
    }
}
