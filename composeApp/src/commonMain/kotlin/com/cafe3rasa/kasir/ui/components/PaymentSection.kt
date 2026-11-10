package com.cafe3rasa.kasir.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cafe3rasa.kasir.model.PaymentBillStatus
import com.cafe3rasa.kasir.state.formatRupiah
import com.cafe3rasa.kasir.theme.KasirColors
import com.cafe3rasa.kasir.theme.KasirTypography

@Composable
fun PaymentStatusCard(
    tableNumber: String,
    currentStatus: PaymentBillStatus,
    onStatusChanged: (PaymentBillStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(KasirColors.Surface)
            .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Title & Description
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Payment,
                    contentDescription = null,
                    tint = KasirColors.PrimaryTeal,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "STATUS PEMBAYARAN",
                    style = KasirTypography.titleSmall.copy(
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = KasirColors.TextPrimary
                    )
                )
            }

            Text(
                text = "Tentukan status tagihan untuk Meja $tableNumber",
                style = KasirTypography.bodySmall.copy(
                    fontSize = 11.5.sp,
                    color = KasirColors.TextMuted
                )
            )

            // Responsive Toggle Buttons: [Belum Bayar] and [Lunas] full-width split
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Belum Bayar Button
                val isBelumBayar = currentStatus == PaymentBillStatus.BELUM_BAYAR
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isBelumBayar) KasirColors.PrimaryTeal else KasirColors.Background)
                        .border(
                            1.dp,
                            if (isBelumBayar) KasirColors.PrimaryTeal else KasirColors.BorderLight,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onStatusChanged(PaymentBillStatus.BELUM_BAYAR) }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = if (isBelumBayar) KasirColors.TextWhite else KasirColors.TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Belum Bayar",
                            style = KasirTypography.labelLarge.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isBelumBayar) KasirColors.TextWhite else KasirColors.TextSecondary
                            )
                        )
                    }
                }

                // Lunas Button
                val isLunas = currentStatus == PaymentBillStatus.LUNAS
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isLunas) KasirColors.AccentRust else KasirColors.Background)
                        .border(
                            1.dp,
                            if (isLunas) KasirColors.AccentRust else KasirColors.BorderLight,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onStatusChanged(PaymentBillStatus.LUNAS) }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isLunas) KasirColors.TextWhite else KasirColors.TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Lunas",
                            style = KasirTypography.labelLarge.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLunas) KasirColors.TextWhite else KasirColors.TextSecondary
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentSummaryBar(
    itemCount: Int,
    totalRupiah: Long,
    paymentMethod: String = "TUNAI (CASH)",
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .background(KasirColors.Surface)
            .border(1.dp, KasirColors.Border)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Item count & Total
        Column(
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = "$itemCount item • ${formatRupiah(totalRupiah)}",
                style = KasirTypography.headlineMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = KasirColors.TextPrimary
                )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Metode Bayar: ",
                    style = KasirTypography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = KasirColors.TextMuted
                    )
                )
                Text(
                    text = paymentMethod,
                    style = KasirTypography.bodySmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = KasirColors.TextPrimary
                    )
                )
            }
        }

        // Right: Konfirmasi Button
        Box(
            modifier = Modifier
                .height(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(KasirColors.AccentRust)
                .clickable { onConfirm() }
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Konfirmasi",
                style = KasirTypography.titleMedium.copy(
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = KasirColors.TextWhite
                )
            )
        }
    }
}
