package com.cafe3rasa.kasir.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cafe3rasa.kasir.model.RequestBadgeType
import com.cafe3rasa.kasir.model.TableRequest
import com.cafe3rasa.kasir.theme.KasirColors
import com.cafe3rasa.kasir.theme.KasirTypography

@Composable
fun TableRequestSection(
    requests: List<TableRequest>,
    onRequestHandled: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(KasirColors.Surface)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = KasirColors.AccentRust,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "PERMINTAAN MEJA",
                    style = KasirTypography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = KasirColors.TextPrimary
                    )
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(KasirColors.AccentRust)
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${requests.size} Perlu Konfirmasi",
                    style = KasirTypography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = KasirColors.TextWhite
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Request list
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            requests.forEach { request ->
                TableRequestCard(
                    request = request,
                    onPrimaryAction = { onRequestHandled(request.id) },
                    onSecondaryAction = { onRequestHandled(request.id) }
                )
            }
        }
    }
}

@Composable
private fun TableRequestCard(
    request: TableRequest,
    onPrimaryAction: () -> Unit,
    onSecondaryAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(KasirColors.Surface)
            .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Card Header: Table + Badge + Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = request.tableNumber,
                        style = KasirTypography.titleSmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = KasirColors.TextPrimary
                        )
                    )

                    val badgeBg = when (request.badgeType) {
                        RequestBadgeType.MINTA_BILL_TUNAI, RequestBadgeType.PERINGATAN_DAPUR -> KasirColors.AccentRust
                        RequestBadgeType.PANGGIL_WAITER, RequestBadgeType.TAMBAH_PESANAN -> KasirColors.PrimaryDarkNavy
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(badgeBg)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = request.badgeLabel,
                            style = KasirTypography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = KasirColors.TextWhite
                            )
                        )
                    }
                }

                Text(
                    text = request.timeAgo,
                    style = KasirTypography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = KasirColors.TextMuted
                    )
                )
            }

            // Note Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(KasirColors.Background)
                    .border(1.dp, KasirColors.BorderNote, RoundedCornerShape(6.dp))
                    .padding(horizontal = 9.dp, vertical = 7.dp)
            ) {
                Row {
                    Text(
                        text = "Catatan: ",
                        style = KasirTypography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = KasirColors.TextMuted
                        )
                    )
                    Text(
                        text = "\"${request.note}\"",
                        style = KasirTypography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = KasirColors.TextPrimary,
                            lineHeight = 17.sp
                        )
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secondary Action Button (Selesai / Cek Dapur)
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(KasirColors.Surface)
                        .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(5.dp))
                        .clickable { onSecondaryAction() }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = request.secondaryActionText,
                        style = KasirTypography.labelSmall.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = KasirColors.TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Primary Action Button (Konfirmasi)
                val btnColor = if (request.isAlertDapur || request.badgeType == RequestBadgeType.MINTA_BILL_TUNAI) {
                    KasirColors.AccentRust
                } else {
                    KasirColors.PrimaryTeal
                }

                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(btnColor)
                        .clickable { onPrimaryAction() }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = request.primaryActionText,
                        style = KasirTypography.labelSmall.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = KasirColors.TextWhite
                        )
                    )
                }
            }
        }
    }
}
