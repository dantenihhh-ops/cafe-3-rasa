package com.cafe3rasa.kasir.ui.admin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cafe3rasa.kasir.model.OrderStatus
import com.cafe3rasa.kasir.state.AdminPanelState
import com.cafe3rasa.kasir.state.LogStatusFilter
import com.cafe3rasa.kasir.state.StatisticsPeriod
import com.cafe3rasa.kasir.state.formatRupiah
import com.cafe3rasa.kasir.theme.KasirColors
import com.cafe3rasa.kasir.theme.KasirTypography

/** 08 — Log & Statistik Pesanan. */
@Composable
fun AdminLogScreen(state: AdminPanelState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 20.dp)
            .padding(bottom = 72.dp) // ruang footer
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Log & Statistik Pesanan",
                    style = KasirTypography.headlineLarge.copy(fontSize = 24.sp)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Pantau pendapatan, jumlah pesanan, dan menu terlaris per hari, bulan, atau tahun.",
                    style = KasirTypography.bodyLarge.copy(fontSize = 13.5.sp),
                    color = KasirColors.TextSecondary
                )
            }
            Row(
                modifier = Modifier
                    .padding(top = 13.dp)
                    .height(39.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(KasirColors.AccentRust)
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Unduh Laporan (CSV)",
                    fontSize = 13.4.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(Modifier.height(26.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Segmented Harian / Bulanan / Tahunan
            Row(
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(KasirColors.Surface)
                    .border(1.dp, KasirColors.Border, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StatisticsPeriod.entries.forEach { period ->
                    val isActive = state.statisticsPeriod == period
                    Box(
                        modifier = Modifier
                            .height(31.7.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isActive) KasirColors.PrimaryTeal else Color.Transparent)
                            .clickable { state.statisticsPeriod = period }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = period.label,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isActive) Color.White else KasirColors.TextSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            DateStepButton(Icons.Default.ChevronLeft) { state.shiftPeriod(-1) }
            Spacer(Modifier.width(8.dp))
            Row(
                modifier = Modifier
                    .width(165.dp)
                    .height(31.7.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(KasirColors.Surface)
                    .border(1.dp, KasirColors.Border, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = KasirColors.PrimaryTeal,
                    modifier = Modifier.size(13.5.dp)
                )
                Text(
                    text = state.periodLabel,
                    fontSize = 13.4.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KasirColors.TextPrimary
                )
            }
            Spacer(Modifier.width(8.dp))
            DateStepButton(Icons.Default.ChevronRight) { state.shiftPeriod(1) }
        }

        Spacer(Modifier.height(14.dp))
        KpiRow(state)

        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
            RevenueChartCard(state, Modifier.weight(1.78f))
            TopMenuCard(state, Modifier.weight(0.72f))
        }

        Spacer(Modifier.height(18.dp))
        LogTableCard(state)
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Log & Statistik",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = KasirColors.PrimaryTeal,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun DateStepButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(33.5.dp, 31.7.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(KasirColors.Surface)
            .border(1.dp, KasirColors.Border, RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = KasirColors.TextSecondary,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun KpiRow(state: AdminPanelState) {
    val average = if (state.orderCount == 0) 0L else state.totalRevenue / state.orderCount
    val kpis = listOf(
        Triple("Total Pendapatan", formatRupiah(state.totalRevenue), "+8,4% dibanding kemarin"),
        Triple("Jumlah Pesanan", state.orderCount.toString(), "pesanan selesai"),
        Triple("Rata-rata per Pesanan", formatRupiah(average), "nilai transaksi"),
        Triple("Puncak Jam", state.peakHour, "Rp 388.000")
    )
    val iconSets = listOf(
        Icons.Default.Payments to (KasirColors.MintSoft to KasirColors.PrimaryTeal),
        Icons.Default.ReceiptLong to (KasirColors.PeachSoft to Color(0xFFB4543C)),
        Icons.Default.TrendingUp to (KasirColors.MintSoft to KasirColors.PrimaryTeal),
        Icons.Default.BarChart to (KasirColors.PeachSoft to Color(0xFFB4543C))
    )
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        kpis.forEachIndexed { index, (title, value, caption) ->
            val (icon, tintPair) = iconSets[index]
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(KasirColors.Surface)
                    .border(1.dp, KasirColors.Border, RoundedCornerShape(12.dp))
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 12.5.sp,
                        color = KasirColors.TextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(31.4.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(tintPair.first),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = tintPair.second,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = value,
                    fontSize = 21.8.sp,
                    fontWeight = FontWeight.Bold,
                    color = KasirColors.TextPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = caption,
                    fontSize = 12.sp,
                    color = KasirColors.TextMuted
                )
            }
        }
    }
}

@Composable
private fun RevenueChartCard(state: AdminPanelState, modifier: Modifier = Modifier) {
    val bars = state.hourlyRevenue
    val peakIndex = bars.indices.maxByOrNull { bars[it].revenue } ?: 0
    val maxRevenue = bars.maxOf { it.revenue }.toFloat()

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(KasirColors.Surface)
            .border(1.dp, KasirColors.Border, RoundedCornerShape(12.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Pendapatan per Jam",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = KasirColors.TextPrimary
            )
            Spacer(Modifier.weight(1f))
            LegendDot(KasirColors.PrimaryTeal, "Pendapatan")
            Spacer(Modifier.width(14.dp))
            LegendDot(KasirColors.AccentRust, "Puncak")
        }
        Spacer(Modifier.height(16.dp))

        Row {
            // Sumbu Y
            Column(
                modifier = Modifier
                    .width(38.dp)
                    .height(230.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                listOf("400 rb", "300 rb", "200 rb", "100 rb", "0").forEach {
                    Text(it, fontSize = 11.sp, color = KasirColors.TextMuted)
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                ) {
                    val gridColor = KasirColors.ChartGrid
                    val dash = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    // 4 garis grid + garis dasar
                    for (i in 0 until 4) {
                        val y = size.height * i / 4.2f
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f,
                            pathEffect = dash
                        )
                    }
                    val baseY = size.height - 4f
                    drawLine(
                        color = KasirColors.BorderLight,
                        start = Offset(0f, baseY),
                        end = Offset(size.width, baseY),
                        strokeWidth = 1f
                    )

                    val slot = size.width / bars.size
                    val barWidth = slot * 0.78f
                    bars.forEachIndexed { i, bar ->
                        val h = (bar.revenue / maxRevenue) * (size.height - 20f)
                        val left = i * slot + (slot - barWidth) / 2f
                        drawRoundRect(
                            color = if (i == peakIndex) KasirColors.AccentRust else KasirColors.PrimaryTeal,
                            topLeft = Offset(left, baseY - h),
                            size = Size(barWidth, h),
                            cornerRadius = CornerRadius(3f, 3f)
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row {
                    bars.forEach { bar ->
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                text = bar.hour,
                                fontSize = 10.5.sp,
                                color = KasirColors.TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 12.sp, color = KasirColors.TextMuted)
    }
}

@Composable
private fun TopMenuCard(state: AdminPanelState, modifier: Modifier = Modifier) {
    val maxSold = state.topMenus.maxOfOrNull { it.second } ?: 1
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(KasirColors.Surface)
            .border(1.dp, KasirColors.Border, RoundedCornerShape(12.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Menu Terlaris",
                fontSize = 15.1.sp,
                fontWeight = FontWeight.Bold,
                color = KasirColors.TextPrimary
            )
            Spacer(Modifier.weight(1f))
            Text("porsi terjual", fontSize = 12.sp, color = KasirColors.TextMuted)
        }
        Spacer(Modifier.height(14.dp))
        state.topMenus.forEachIndexed { index, (name, sold) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${index + 1}",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = KasirColors.TextMuted,
                    modifier = Modifier.width(18.dp)
                )
                Text(
                    text = name,
                    fontSize = 13.2.sp,
                    color = KasirColors.TextPrimary,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = sold.toString(),
                    fontSize = 13.2.sp,
                    fontWeight = FontWeight.Bold,
                    color = KasirColors.TextPrimary
                )
            }
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(KasirColors.ChartTrack)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(sold.toFloat() / maxSold)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (index == 0) KasirColors.AccentRust else KasirColors.PrimaryTeal)
                )
            }
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
private fun LogTableCard(state: AdminPanelState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(KasirColors.Surface)
            .border(1.dp, KasirColors.Border, RoundedCornerShape(12.dp))
            .padding(vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = null,
                tint = KasirColors.TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Log Pesanan",
                fontSize = 14.9.sp,
                fontWeight = FontWeight.Bold,
                color = KasirColors.TextPrimary
            )
            Spacer(Modifier.width(16.dp))
            LogStatusFilter.entries.forEach { filter ->
                val isActive = state.logStatusFilter == filter
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isActive) KasirColors.AccentRust else KasirColors.Surface)
                        .border(
                            1.dp,
                            if (isActive) KasirColors.AccentRust else KasirColors.Border,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { state.logStatusFilter = filter }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter.label,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isActive) Color.White else KasirColors.TextSecondary
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            SearchBox(
                value = state.logSearch,
                onValueChange = { state.logSearch = it },
                placeholder = "Cari no. pesanan / meja",
                modifier = Modifier
                    .width(236.dp)
                    .height(32.dp)
            )
        }

        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(KasirColors.LogTableBg)
                .padding(horizontal = 18.dp, vertical = 11.dp)
        ) {
            ColumnHeader("NO. PESANAN", 1.15f)
            ColumnHeader("WAKTU", 0.85f)
            ColumnHeader("MEJA", 0.6f)
            ColumnHeader("ITEM", 3.2f)
            ColumnHeader("TOTAL", 0.75f)
            ColumnHeader("PEMBAYARAN", 0.9f)
            ColumnHeader("STATUS", 0.75f)
        }

        state.filteredLog.forEachIndexed { index, entry ->
            if (index > 0) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(1.dp)
                        .background(KasirColors.TableRowDivider)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(47.dp)
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.id,
                    fontSize = 13.2.sp,
                    fontWeight = FontWeight.Bold,
                    color = KasirColors.PrimaryTeal,
                    modifier = Modifier.weight(1.15f)
                )
                Text(
                    entry.time,
                    fontSize = 12.8.sp,
                    color = KasirColors.TextSecondary,
                    modifier = Modifier.weight(0.85f)
                )
                Text(
                    "Meja ${entry.tableNumber}",
                    fontSize = 13.sp,
                    color = KasirColors.TextPrimary,
                    modifier = Modifier.weight(0.6f)
                )
                Text(
                    entry.itemSummary,
                    fontSize = 12.8.sp,
                    color = KasirColors.TextSecondary,
                    maxLines = 1,
                    modifier = Modifier.weight(3.2f)
                )
                Text(
                    formatRupiah(entry.total),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KasirColors.TextPrimary,
                    modifier = Modifier.weight(0.75f)
                )
                Text(
                    entry.paymentMethod.label,
                    fontSize = 12.8.sp,
                    color = KasirColors.TextSecondary,
                    modifier = Modifier.weight(0.9f)
                )
                StatusPill(entry.status)
            }
        }

        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Menampilkan ${state.filteredLog.size} pesanan terbaru dari ${state.orderCount} pesanan",
                fontSize = 12.5.sp,
                color = KasirColors.TextMuted
            )
            Spacer(Modifier.weight(1f))
            AdminTextButton(
                text = "Sebelumnya",
                onClick = {},
                modifier = Modifier
                    .width(108.dp)
                    .height(34.dp),
                shape = RoundedCornerShape(8.dp),
                fontSize = 12.sp
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(24.dp, 34.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(KasirColors.PrimaryTeal),
                contentAlignment = Alignment.Center
            ) {
                Text("1", fontSize = 12.6.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            AdminTextButton(
                text = "2",
                onClick = {},
                modifier = Modifier
                    .size(24.dp, 34.dp),
                shape = RoundedCornerShape(7.dp)
            )
            Spacer(Modifier.width(8.dp))
            AdminTextButton(
                text = "Berikutnya",
                onClick = {},
                modifier = Modifier
                    .width(112.dp)
                    .height(34.dp),
                shape = RoundedCornerShape(8.dp),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun RowScope.ColumnHeader(text: String, weight: Float) {
    Text(
        text = text,
        fontSize = 11.6.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.3.sp,
        color = KasirColors.TextMuted,
        modifier = Modifier.weight(weight)
    )
}

@Composable
private fun RowScope.StatusPill(status: OrderStatus) {
    val (bg, fg) = when (status) {
        OrderStatus.SELESAI -> KasirColors.MintSoft to KasirColors.PrimaryTeal
        OrderStatus.DIPROSES -> KasirColors.PeachSoft to KasirColors.AccentRust
        OrderStatus.DIBATALKAN -> KasirColors.NeutralEmptyBg to KasirColors.TextMuted
    }
    Row(
        modifier = Modifier
            .weight(0.75f)
            .wrapContentWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(fg)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = status.label,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = fg
        )
    }
}
