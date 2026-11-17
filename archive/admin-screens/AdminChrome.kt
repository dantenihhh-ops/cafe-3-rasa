package com.cafe3rasa.kasir.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cafe3rasa.kasir.state.AdminTab
import com.cafe3rasa.kasir.theme.KasirColors
import com.cafe3rasa.kasir.theme.KasirTypography

/**
 * Header panel admin — padanan `dhead()` di mockup 07/08/09 (mockup 05-09).
 * Tinggi 55dp, logo kiri, pill "ADMIN PANEL", jam, divider, avatar.
 */
@Composable
fun AdminHeader(dateText: String = "Rabu, 04 Nov 2026") {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .background(KasirColors.Surface)
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp, 30.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(KasirColors.PrimaryDarkNavy),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "3R",
                    color = KasirColors.TextWhite,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Cafe 3 Rasa",
                style = KasirTypography.headlineMedium.copy(
                    fontSize = 19.5.sp,
                    color = KasirColors.TextPrimary
                )
            )
            Spacer(Modifier.width(24.dp))
            Box(
                modifier = Modifier
                    .height(23.7.dp)
                    .clip(CircleShape)
                    .background(KasirColors.PrimaryDarkNavy)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ADMIN PANEL",
                    color = KasirColors.TextWhite,
                    fontSize = 11.2.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.weight(1f))

            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = KasirColors.TextMuted,
                modifier = Modifier.size(17.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(dateText, style = KasirTypography.bodyMedium.copy(fontSize = 13.3.sp))
            Spacer(Modifier.width(16.dp))
            Text(
                text = "10:24 AM",
                style = KasirTypography.bodyMedium.copy(fontSize = 13.2.sp)
            )
            Spacer(Modifier.width(24.dp))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(28.4.dp)
                    .background(KasirColors.Border)
            )
            Spacer(Modifier.width(14.dp))
            Box(
                modifier = Modifier
                    .size(34.dp, 33.3.dp)
                    .clip(CircleShape)
                    .background(KasirColors.MintSoft),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AM",
                    fontSize = 11.7.sp,
                    fontWeight = FontWeight.Bold,
                    color = KasirColors.PrimaryTeal
                )
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = "Admin Maharewa",
                    fontSize = 13.6.sp,
                    fontWeight = FontWeight.Bold,
                    color = KasirColors.TextPrimary
                )
                Text(
                    text = "Administrator",
                    fontSize = 11.7.sp,
                    color = KasirColors.TextMuted
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(KasirColors.Border)
        )
    }
}

/**
 * Footer navigasi admin — padanan `dfoot()` di mockup 07/08/09.
 * Empat item: Produk & Kategori, Log & Statistik, Meja & QR, Keluar.
 */
@Composable
fun AdminFooterNav(
    active: AdminTab,
    onTabSelected: (AdminTab) -> Unit
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(KasirColors.Border)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(KasirColors.Surface)
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val items = listOf(
                Triple(AdminTab.PRODUK, Icons.Default.RestaurantMenu, "Produk & Kategori"),
                Triple(AdminTab.LOG_STATISTIK, Icons.Default.ShowChart, "Log & Statistik"),
                Triple(AdminTab.MEJA_QR, Icons.Default.GridView, "Meja & QR"),
                Triple(AdminTab.KELUAR, Icons.Default.Logout, "Keluar")
            )
            items.forEachIndexed { index, (tab, icon, label) ->
                val isActive = tab == active
                val tint = when {
                    tab == AdminTab.KELUAR -> KasirColors.AccentRust
                    isActive -> KasirColors.PrimaryTeal
                    else -> KasirColors.TextMuted
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onTabSelected(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .width(96.dp)
                                .height(3.dp)
                                .background(KasirColors.PrimaryTeal)
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = tint,
                        modifier = Modifier.size(21.dp)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        color = tint
                    )
                }
                if (index == items.lastIndex) Unit
            }
        }
    }
}

/** Tombol dasar yang dipakai berulang di panel admin. */
@Composable
fun AdminTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    background: Color = KasirColors.Surface,
    border: Color? = KasirColors.Border,
    textColor: Color = KasirColors.TextPrimary,
    fontSize: TextUnit = 13.sp,
    shape: RoundedCornerShape = RoundedCornerShape(10.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(background)
            .border(if (border != null) 1.dp else 0.dp, border ?: Color.Transparent, shape)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            maxLines = 1
        )
    }
}
