package com.cafe3rasa.kasir.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cafe3rasa.kasir.theme.KasirColors
import com.cafe3rasa.kasir.theme.KasirTypography

@Composable
fun CashierHeader(
    isCompact: Boolean = false,
    activeTableCount: Int = 24,
    currentTimeText: String = "09:41 WIB",
    onNotificationClick: () -> Unit = {},
    onAdminClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(KasirColors.Surface)
            .border(width = 1.dp, color = KasirColors.Border)
            .padding(horizontal = if (isCompact) 12.dp else 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Logo & Brand Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Brand Logo Mark
            Box(
                modifier = Modifier
                    .size(30.dp, 32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(KasirColors.PrimaryDarkNavy),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "3R",
                    color = KasirColors.TextWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = (-0.5).sp
                )
            }

            Text(
                text = "Cafe 3 Rasa",
                style = KasirTypography.headlineMedium.copy(
                    fontSize = if (isCompact) 17.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = KasirColors.TextPrimary
                )
            )
        }

        // Center: POS Aktif Badge & Clock (Hidden on very compact if space tight)
        if (!isCompact) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // POS AKTIF Pill
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .clip(CircleShape)
                        .background(KasirColors.PrimaryDarkNavy)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "POS AKTIF",
                        color = KasirColors.TextWhite,
                        style = KasirTypography.labelLarge.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Clock
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Jam",
                        tint = KasirColors.TextMuted,
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = currentTimeText,
                        style = KasirTypography.bodyMedium.copy(
                            fontSize = 14.5.sp,
                            color = KasirColors.TextMuted
                        )
                    )
                }
            }
        }

        // Right: Table Count Pill, Bell, Settings
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 24 Meja Aktif Badge
            Row(
                modifier = Modifier
                    .height(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(KasirColors.Background)
                    .border(width = 1.dp, color = KasirColors.BorderLight, shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(KasirColors.PrimaryDarkNavy)
                )
                Text(
                    text = "$activeTableCount Meja",
                    style = KasirTypography.labelLarge.copy(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = KasirColors.TextPrimary
                    )
                )
            }

            // Notification Button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(KasirColors.Surface)
                    .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(8.dp))
                    .clickable { onNotificationClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifikasi",
                    tint = KasirColors.TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Vertical Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(26.dp)
                    .background(KasirColors.Border)
            )

            // Panel Admin Settings Button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(KasirColors.Surface)
                    .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(8.dp))
                    .clickable { onAdminClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Panel Admin",
                    tint = KasirColors.TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
