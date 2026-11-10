package com.cafe3rasa.kasir.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cafe3rasa.kasir.model.TableModel
import com.cafe3rasa.kasir.model.TableStatus
import com.cafe3rasa.kasir.state.TableFilter
import com.cafe3rasa.kasir.theme.KasirColors
import com.cafe3rasa.kasir.theme.KasirTypography

@Composable
fun TableMapSection(
    tables: List<TableModel>,
    selectedTableNumber: String,
    activeFilter: TableFilter,
    onFilterSelected: (TableFilter) -> Unit,
    onTableSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(KasirColors.Surface)
    ) {
        // Title & Area
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PETA MEJA",
                style = KasirTypography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = KasirColors.TextPrimary
                )
            )
            Text(
                text = "Area Utama & Selasar",
                style = KasirTypography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = KasirColors.TextMuted
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TableFilter.values().forEach { filter ->
                val isSelected = filter == activeFilter
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(if (isSelected) KasirColors.TextPrimary else KasirColors.Surface)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) KasirColors.TextPrimary else KasirColors.BorderLight,
                            shape = RoundedCornerShape(7.dp)
                        )
                        .clickable { onFilterSelected(filter) }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter.label,
                        style = KasirTypography.labelLarge.copy(
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) KasirColors.TextWhite else KasirColors.TextSecondary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Table Grid (4 columns, 2 rows for 8 tables)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val chunked = tables.chunked(4)
            chunked.forEach { rowTables ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowTables.forEach { table ->
                        val isSelected = table.number == selectedTableNumber
                        TableItemCard(
                            table = table,
                            isSelected = isSelected,
                            onClick = { onTableSelected(table.number) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Fill remaining slots if any
                    val remaining = 4 - rowTables.size
                    for (i in 0 until remaining) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Capacity & Occupancy
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kapasitas: ${tables.size} Meja Aktif",
                style = KasirTypography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = KasirColors.TextSubtle
                )
            )
            Text(
                text = "Okupansi: 38%",
                style = KasirTypography.bodySmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = KasirColors.TextPrimary
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Active Table Selection Pill Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(KasirColors.PrimaryTeal)
                .clickable { onTableSelected(selectedTableNumber) },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = KasirColors.TextWhite,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Meja $selectedTableNumber Dipilih",
                    style = KasirTypography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = KasirColors.TextWhite
                    )
                )
            }
        }
    }
}

@Composable
private fun TableItemCard(
    table: TableModel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isSelected) KasirColors.SelectedTableBg else KasirColors.Surface
    val borderColor = if (isSelected) KasirColors.PrimaryTeal else KasirColors.BorderLight
    val borderWidth = if (isSelected) 1.5.dp else 1.dp

    Box(
        modifier = modifier
            .height(86.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(cardBg)
            .border(borderWidth, borderColor, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Table Number + Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = table.number,
                    style = KasirTypography.titleLarge.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) KasirColors.PrimaryTeal else KasirColors.TextPrimary
                    )
                )

                val badgeBg = when {
                    isSelected -> KasirColors.PrimaryTeal
                    table.status == TableStatus.TERISI_BELUM_BAYAR || table.status == TableStatus.TERISI_LUNAS -> KasirColors.AccentRust
                    else -> KasirColors.NeutralEmptyBg
                }
                val badgeText = when {
                    isSelected -> "DIPILIH"
                    table.status == TableStatus.TERISI_BELUM_BAYAR || table.status == TableStatus.TERISI_LUNAS -> "TERISI"
                    else -> "KOSONG"
                }
                val badgeTextColor = when {
                    isSelected || table.status != TableStatus.KOSONG -> KasirColors.TextWhite
                    else -> KasirColors.TextMuted
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(badgeBg)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = KasirTypography.labelSmall.copy(
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor
                        )
                    )
                }
            }

            // Bottom Subtitle / Tag
            when {
                isSelected -> {
                    Text(
                        text = "Pesanan Baru",
                        style = KasirTypography.bodySmall.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = KasirColors.PrimaryTeal,
                            lineHeight = 13.sp
                        )
                    )
                }
                table.status == TableStatus.TERISI_BELUM_BAYAR -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(3.dp))
                            .background(KasirColors.PrimaryDarkNavy)
                            .padding(vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "BELUM BAYAR",
                            style = KasirTypography.labelSmall.copy(
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = KasirColors.TextWhite
                            )
                        )
                    }
                }
                table.status == TableStatus.TERISI_LUNAS -> {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clip(RoundedCornerShape(3.dp))
                            .background(KasirColors.PaidBadgeBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LUNAS",
                            style = KasirTypography.labelSmall.copy(
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = KasirColors.PaidBadgeText
                            )
                        )
                    }
                }
                else -> {
                    Text(
                        text = "Tersedia",
                        style = KasirTypography.bodySmall.copy(
                            fontSize = 11.5.sp,
                            color = KasirColors.TextSubtle
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
