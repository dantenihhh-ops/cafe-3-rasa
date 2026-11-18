package com.cafe3rasa.kasir.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cafe3rasa.kasir.model.PaymentBillStatus
import com.cafe3rasa.kasir.model.PaymentMethod
import com.cafe3rasa.kasir.state.formatRupiah
import com.cafe3rasa.kasir.theme.KasirColors
import com.cafe3rasa.kasir.theme.KasirTypography

@Composable
fun PaymentStatusCard(
    tableNumber: String,
    currentStatus: PaymentBillStatus,
    currentMethod: PaymentMethod = PaymentMethod.BELUM_DIPILIH,
    onStatusChanged: (PaymentBillStatus) -> Unit,
    onMethodSelected: (PaymentMethod) -> Unit = {},
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
            // Header
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

            // Toggle: Belum Bayar / Lunas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Belum Bayar
                val isBelumBayar = currentStatus == PaymentBillStatus.BELUM_BAYAR
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isBelumBayar) KasirColors.PrimaryDarkNavy else KasirColors.Background)
                        .border(
                            1.dp,
                            if (isBelumBayar) KasirColors.PrimaryDarkNavy else KasirColors.BorderLight,
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

                // Lunas
                val isLunas = currentStatus == PaymentBillStatus.LUNAS
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isLunas) Color(0xFF16A34A) else KasirColors.Background)
                        .border(
                            1.dp,
                            if (isLunas) Color(0xFF16A34A) else KasirColors.BorderLight,
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

            // ── Metode Pembayaran (hanya muncul jika status = LUNAS) ──
            if (currentStatus == PaymentBillStatus.LUNAS) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(3.dp, 14.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(KasirColors.AccentRust)
                        )
                        Text(
                            text = "METODE PEMBAYARAN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = KasirColors.TextPrimary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PaymentMethodChip(
                            label = "Cash",
                            icon = Icons.Default.Payments,
                            isSelected = currentMethod == PaymentMethod.CASH,
                            onClick = { onMethodSelected(PaymentMethod.CASH) },
                            modifier = Modifier.weight(1f)
                        )
                        PaymentMethodChip(
                            label = "Transfer",
                            icon = Icons.Default.AccountBalance,
                            isSelected = currentMethod == PaymentMethod.TRANSFER,
                            onClick = { onMethodSelected(PaymentMethod.TRANSFER) },
                            modifier = Modifier.weight(1f)
                        )
                        PaymentMethodChip(
                            label = "QRIS",
                            icon = Icons.Default.QrCode,
                            isSelected = currentMethod == PaymentMethod.QRIS,
                            onClick = { onMethodSelected(PaymentMethod.QRIS) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Warning jika belum pilih metode
                    if (currentMethod == PaymentMethod.BELUM_DIPILIH) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(KasirColors.PeachSoft)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = KasirColors.AccentRust,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Pilih metode pembayaran untuk melengkapi transaksi",
                                fontSize = 10.5.sp,
                                color = KasirColors.AccentRust,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) KasirColors.PrimaryTeal else KasirColors.SurfaceLight
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) KasirColors.PrimaryTeal else KasirColors.BorderLight,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) KasirColors.TextWhite else KasirColors.TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) KasirColors.TextWhite else KasirColors.TextSecondary
            )
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
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val isCompact = maxWidth < 480.dp

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isCompact) 62.dp else 68.dp)
                .background(KasirColors.Surface)
                .border(1.dp, KasirColors.Border)
                .padding(horizontal = if (isCompact) 12.dp else 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Item count & Total
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = "$itemCount item • ${formatRupiah(totalRupiah)}",
                    style = KasirTypography.headlineMedium.copy(
                        fontSize = if (isCompact) 15.sp else 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = KasirColors.TextPrimary
                    )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Metode: ",
                        style = KasirTypography.bodySmall.copy(
                            fontSize = if (isCompact) 10.5.sp else 11.sp,
                            color = KasirColors.TextMuted
                        )
                    )
                    Text(
                        text = paymentMethod,
                        style = KasirTypography.bodySmall.copy(
                            fontSize = if (isCompact) 10.5.sp else 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = KasirColors.TextPrimary
                        )
                    )
                }
            }

            // Right: Konfirmasi Button
            Box(
                modifier = Modifier
                    .height(if (isCompact) 38.dp else 44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(KasirColors.AccentRust)
                    .clickable { onConfirm() }
                    .padding(horizontal = if (isCompact) 16.dp else 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Konfirmasi",
                    style = KasirTypography.titleMedium.copy(
                        fontSize = if (isCompact) 13.sp else 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = KasirColors.TextWhite
                    )
                )
            }
        }
    }
}
