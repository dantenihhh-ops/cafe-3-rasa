package com.cafe3rasa.kasir.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
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

        Spacer(modifier = Modifier.height(10.dp))

        // Legenda warna status meja
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Abu-abu = Kosong
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(KasirColors.NeutralEmptyBg))
                Text("Kosong", fontSize = 10.sp, color = KasirColors.TextSubtle, fontWeight = FontWeight.Medium)
            }
            // Kuning = Baru Pesan (Berkedip)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                Text("Baru Pesan", fontSize = 10.sp, color = Color(0xFFD97706), fontWeight = FontWeight.SemiBold)
            }
            // Merah = Belum Bayar
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(KasirColors.AccentRust))
                Text("Belum Bayar", fontSize = 10.sp, color = KasirColors.TextMuted, fontWeight = FontWeight.Medium)
            }
            // Hijau = Lunas
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF22C55E)))
                Text("Lunas", fontSize = 10.sp, color = KasirColors.TextMuted, fontWeight = FontWeight.Medium)
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

        // Active Table Selection Pill — tanpa icon centang
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(KasirColors.PrimaryTeal)
                .clickable { onTableSelected(selectedTableNumber) },
            contentAlignment = Alignment.Center
        ) {
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

@Composable
private fun TableItemCard(
    table: TableModel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Background berdasarkan status
    val cardBg = when {
        isSelected -> KasirColors.SelectedTableBg
        table.status == TableStatus.KOSONG -> KasirColors.NeutralEmptyBg
        else -> KasirColors.Surface
    }
    val borderColor = when {
        isSelected -> KasirColors.PrimaryTeal
        table.status == TableStatus.BARU_PESAN -> Color(0xFFF59E0B)  // Kuning — perlu perhatian
        table.status == TableStatus.KOSONG -> KasirColors.NeutralEmptyBg
        else -> KasirColors.BorderLight
    }
    val borderWidth = when {
        isSelected -> 2.dp
        table.status == TableStatus.BARU_PESAN -> 1.5.dp
        else -> 1.dp
    }

    // Warna dot status
    val dotColor: Color? = when {
        table.status == TableStatus.BARU_PESAN -> Color(0xFFF59E0B)       // Kuning berkedip
        table.status == TableStatus.TERISI_BELUM_BAYAR -> KasirColors.AccentRust  // Merah solid
        table.status == TableStatus.TERISI_LUNAS -> Color(0xFF22C55E)      // Hijau solid
        isSelected -> KasirColors.PrimaryTeal
        else -> null
    }
    val isDotBlinking = table.status == TableStatus.BARU_PESAN

    // Warna nomor meja
    val numberColor = when {
        isSelected -> KasirColors.PrimaryTeal
        table.status == TableStatus.KOSONG -> KasirColors.TextSubtle
        table.status == TableStatus.BARU_PESAN -> Color(0xFFF59E0B)
        else -> KasirColors.TextPrimary
    }

    Box(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(cardBg)
            .border(borderWidth, borderColor, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        // Label "MEJA" kecil + nomor besar
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            Text(
                text = "MEJA",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (table.status == TableStatus.KOSONG && !isSelected)
                    KasirColors.TextSubtle
                else numberColor.copy(alpha = 0.7f),
                letterSpacing = 0.5.sp
            )
            Text(
                text = table.number,
                style = KasirTypography.titleLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = numberColor
                )
            )
        }

        // Dot indicator di pojok kanan atas — berkedip jika BARU_PESAN
        if (dotColor != null) {
            if (isDotBlinking) {
                BlinkingDot(color = dotColor, modifier = Modifier.align(Alignment.TopEnd))
            } else {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                        .align(Alignment.TopEnd)
                )
            }
        }
    }
}

/** Dot berkedip dengan animasi pulse untuk status BARU_PESAN */
@Composable
private fun BlinkingDot(color: Color, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotScale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    Box(
        modifier = modifier
            .size(14.dp),
        contentAlignment = Alignment.Center
    ) {
        // Lingkaran luar (halo)
        Box(
            modifier = Modifier
                .size(14.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha * 0.3f))
        )
        // Dot inti
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha))
        )
    }
}
