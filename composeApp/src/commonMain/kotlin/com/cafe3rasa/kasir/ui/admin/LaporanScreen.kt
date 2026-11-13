package com.cafe3rasa.kasir.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cafe3rasa.kasir.theme.KasirColors
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToLong

private val CoralTintSoft = Color(0xFFF9E6E0)

enum class Period(val label: String) { HARIAN("Harian"), BULANAN("Bulanan"), TAHUNAN("Tahunan") }

private data class PeriodData(
    val rangeLabel: String,
    val chartTitle: String,
    val peakLabel: String,
    val labels: List<String>,
    val values: List<Long>,
    val labelEvery: Int,
    val avgPerOrder: Long,
    val delta: String
)

private fun periodData(p: Period): PeriodData = when (p) {
    Period.HARIAN -> PeriodData(
        "Hari Ini", "Pendapatan per Jam", "Jam",
        (8..21).map { "${it.toString().padStart(2, '0')}:00" },
        listOf(86, 142, 118, 204, 388, 342, 156, 132, 174, 226, 298, 356, 248, 112).map { it * 1_000L },
        1, 42_600, "+8,4% dibanding kemarin"
    )
    Period.BULANAN -> PeriodData(
        "Bulan Ini", "Pendapatan per Tanggal", "Tanggal",
        (1..31).map { "$it" },
        (1..31).map { i ->
            val weekend = (i + 3) % 7 >= 5
            (if (weekend) 3_050_000L else 2_080_000L) + ((i * 37) % 11) * 42_000L
        },
        5, 42_450, "+5,1% dibanding bulan lalu"
    )
    Period.TAHUNAN -> PeriodData(
        "Tahun Ini", "Pendapatan per Bulan", "Bulan",
        listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov"),
        listOf(58.2, 54.6, 61.3, 57.9, 63.4, 66.1, 70.8, 68.5, 65.2, 72.4, 11.9).map { (it * 1_000_000).roundToLong() },
        1, 42_300, "+12,7% dibanding tahun lalu"
    )
}

private fun shortRupiah(v: Long): String =
    if (v >= 1_000_000) {
        val jt = v / 1_000_000
        val sisa = (v % 1_000_000) / 100_000
        if (sisa > 0) "$jt,$sisa jt" else "$jt jt"
    } else {
        "${v / 1000} rb"
    }

private fun formatNumber(v: Long): String {
    val s = v.toString()
    val sb = StringBuilder()
    var count = 0
    for (i in s.length - 1 downTo 0) {
        sb.append(s[i])
        count++
        if (count % 3 == 0 && i > 0) {
            sb.append('.')
        }
    }
    return sb.reverse().toString()
}

@Composable
fun LaporanScreen(store: AdminStore, compact: Boolean = false) {
    var period by rememberSaveable { mutableStateOf(Period.HARIAN) }
    var statusFilter by rememberSaveable { mutableStateOf("Semua") }
    var query by rememberSaveable { mutableStateOf("") }

    val d = periodData(period)
    val total = d.values.sum()
    val orders = (total / d.avgPerOrder)
    val maxV = d.values.max()
    val peakIdx = d.values.indexOf(maxV)
    val step = 10.0.pow(floor(log10(maxV.toDouble())))
    val top = (ceil(maxV / step) * step).toLong()
    val gap = if (compact) 12.dp else 16.dp

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = if (compact) 16.dp else 28.dp, vertical = if (compact) 16.dp else 24.dp),
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {
        // Judul + tombol unduh
        val downloadButton: @Composable (Modifier) -> Unit = { m ->
            Button(
                onClick = { },
                shape = RoundedCornerShape(9.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KasirColors.AccentRust),
                modifier = m.height(40.dp)
            ) {
                Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Unduh Laporan (CSV)", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Color.White)
            }
        }
        if (compact) {
            PageTitle("Log & Statistik", "Pendapatan, jumlah pesanan, dan menu terlaris.", compact = true)
            downloadButton(Modifier.fillMaxWidth())
        } else {
            Row(verticalAlignment = Alignment.Bottom) {
                Box(Modifier.weight(1f)) {
                    PageTitle("Log & Statistik Pesanan", "Pantau pendapatan, jumlah pesanan, dan menu terlaris per hari, bulan, atau tahun.")
                }
                downloadButton(Modifier)
            }
        }

        // Filter periode + label rentang
        val periodSwitch: @Composable (Modifier) -> Unit = { m ->
            Row(
                m
                    .clip(RoundedCornerShape(10.dp))
                    .background(KasirColors.Surface)
                    .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(10.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Period.entries.forEach { p ->
                    val on = p == period
                    Text(
                        p.label,
                        color = if (on) Color.White else KasirColors.TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        textAlign = TextAlign.Center,
                        modifier = (if (compact) Modifier.weight(1f) else Modifier)
                            .clip(RoundedCornerShape(7.dp))
                            .background(if (on) KasirColors.PrimaryTeal else Color.Transparent)
                            .clickable { period = p }
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                    )
                }
            }
        }
        val rangeNav: @Composable () -> Unit = {
            SmallIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Periode sebelumnya", KasirColors.TextSecondary, KasirColors.Surface, KasirColors.BorderLight) {}
            Spacer(Modifier.width(8.dp))
            Row(
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(KasirColors.Surface)
                    .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CalendarToday, null, tint = KasirColors.PrimaryTeal, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(8.dp))
                Text(d.rangeLabel, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = KasirColors.TextPrimary)
            }
            Spacer(Modifier.width(8.dp))
            SmallIconButton(Icons.AutoMirrored.Filled.ArrowForward, "Periode berikutnya", KasirColors.TextSecondary, KasirColors.Surface, KasirColors.BorderLight) {}
        }
        if (compact) {
            periodSwitch(Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) { rangeNav() }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                periodSwitch(Modifier)
                Spacer(Modifier.weight(1f))
                rangeNav()
            }
        }

        // KPI
        val kpis: List<@Composable (Modifier) -> Unit> = listOf(
            { m -> KpiCard("Total Pendapatan", rupiah(total), d.delta, Icons.Default.Download, KasirColors.PrimaryTeal, KasirColors.SelectedTableBg, KasirColors.PrimaryTeal, m) },
            { m -> KpiCard("Jumlah Pesanan", formatNumber(orders), "pesanan selesai", Icons.Default.Restaurant, KasirColors.AccentRust, CoralTintSoft, KasirColors.TextMuted, m) },
            { m -> KpiCard("Rata-rata per Pesanan", rupiah(d.avgPerOrder), "nilai transaksi", Icons.Default.BarChart, KasirColors.PrimaryTeal, KasirColors.SelectedTableBg, KasirColors.TextMuted, m) },
            { m -> KpiCard("Puncak ${d.peakLabel}", d.labels[peakIdx], rupiah(maxV), Icons.Default.BarChart, KasirColors.AccentRust, CoralTintSoft, KasirColors.AccentRust, m) }
        )
        (if (compact) kpis.chunked(2) else listOf(kpis)).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 14.dp)) {
                row.forEach { it(Modifier.weight(1f)) }
            }
        }

        // Grafik + menu terlaris
        val chartCard: @Composable (Modifier) -> Unit = { m ->
            AdminCard(m) {
                if (compact) {
                    Text(d.chartTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = KasirColors.TextPrimary)
                    Spacer(Modifier.height(6.dp))
                    Row { Legend(KasirColors.PrimaryTeal, "Pendapatan"); Spacer(Modifier.width(14.dp)); Legend(KasirColors.AccentRust, "Puncak") }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(d.chartTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = KasirColors.TextPrimary, modifier = Modifier.weight(1f))
                        Legend(KasirColors.PrimaryTeal, "Pendapatan"); Spacer(Modifier.width(14.dp)); Legend(KasirColors.AccentRust, "Puncak")
                    }
                }
                Spacer(Modifier.height(14.dp))
                BarChart(d, top, peakIdx, compact)
            }
        }
        val topCard: @Composable (Modifier) -> Unit = { m ->
            AdminCard(m) {
                Row {
                    Text("Menu Terlaris", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = KasirColors.TextPrimary, modifier = Modifier.weight(1f))
                    Text("porsi terjual", fontSize = 12.sp, color = KasirColors.TextMuted)
                }
                Spacer(Modifier.height(12.dp))
                val share = listOf(0.30, 0.27, 0.19, 0.14, 0.11)
                val names = listOf("Nasi Campur 3 Rasa Spesial", "Kopi Susu Gula Aren 3 Rasa", "Bakmi Ayam Jamur Maharewa", "Teh Tarik Rempah Hangat", "Pisang Goreng Wijen Karamel")
                val portions = orders * 2.1
                names.forEachIndexed { i, name ->
                    val color = if (i == 0) KasirColors.AccentRust else KasirColors.PrimaryTeal
                    Row {
                        Text("${i + 1}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color)
                        Spacer(Modifier.width(8.dp))
                        Text(name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = KasirColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        Text(formatNumber((portions * share[i] * 0.55).roundToLong()), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KasirColors.TextPrimary)
                    }
                    Spacer(Modifier.height(6.dp))
                    Box(Modifier.fillMaxWidth().height(6.dp).background(Color(0xFFEEF2F6), RoundedCornerShape(3.dp))) {
                        Box(Modifier.fillMaxWidth((share[i] / share[0]).toFloat()).height(6.dp).background(color, RoundedCornerShape(3.dp)))
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
        if (compact) {
            chartCard(Modifier.fillMaxWidth())
            topCard(Modifier.fillMaxWidth())
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                chartCard(Modifier.weight(1f))
                topCard(Modifier.width(300.dp))
            }
        }

        // Log pesanan
        val filtered = store.orders.filter {
            (statusFilter == "Semua" || it.status.label == statusFilter) &&
                (query.isBlank() || it.id.contains(query.trim(), true) || it.table.contains(query.trim(), true))
        }
        val datePrefix = when (period) { Period.HARIAN -> ""; Period.BULANAN -> "31 Okt, "; Period.TAHUNAN -> "04 Nov, " }
        val statusChips: @Composable () -> Unit = {
            listOf("Semua", "Selesai", "Diproses", "Dibatalkan").forEach { s ->
                ChoiceChip(s, statusFilter == s, { statusFilter = s }, activeBg = KasirColors.AccentRust)
                Spacer(Modifier.width(6.dp))
            }
        }

        AdminCard(padding = 0.dp) {
            if (compact) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CardTitle("Log Pesanan", Icons.Default.Restaurant)
                    Row(Modifier.horizontalScroll(rememberScrollState())) { statusChips() }
                    FormField(query, { query = it }, "Cari no. pesanan / meja", leadingIcon = Icons.Default.Search)
                }
            } else {
                Row(Modifier.padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) { CardTitle("Log Pesanan", Icons.Default.Restaurant) }
                    statusChips()
                    Spacer(Modifier.width(4.dp))
                    FormField(query, { query = it }, "Cari no. pesanan / meja", Modifier.width(240.dp), leadingIcon = Icons.Default.Search)
                }
            }
            HorizontalDivider(color = KasirColors.BorderLight)
            if (!compact) {
                Row(Modifier.fillMaxWidth().background(KasirColors.Background).padding(horizontal = 18.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("NO. PESANAN" to 120, "WAKTU" to 130, "MEJA" to 70).forEach { (t, w) -> LogHead(t, Modifier.width(w.dp)) }
                    LogHead("ITEM", Modifier.weight(1f))
                    listOf("TOTAL" to 96, "PEMBAYARAN" to 100, "STATUS" to 104).forEach { (t, w) -> LogHead(t, Modifier.width(w.dp)) }
                }
                HorizontalDivider(color = KasirColors.BorderLight)
            }
            if (filtered.isEmpty()) {
                Text("Tidak ada pesanan yang cocok.", color = KasirColors.TextMuted, fontSize = 13.5.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(28.dp))
            }
            filtered.forEach { o ->
                if (compact) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(o.id, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = KasirColors.TextPrimary, modifier = Modifier.weight(1f))
                            StatusBadge(o.status)
                        }
                        Text("${o.table} · $datePrefix${o.time} WIB · ${o.payment}", fontSize = 12.sp, color = KasirColors.TextMuted)
                        Text(o.items, fontSize = 13.sp, color = KasirColors.TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(rupiah(o.total), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = KasirColors.TextPrimary)
                    }
                } else {
                    Row(Modifier.fillMaxWidth().height(46.dp).padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(o.id, Modifier.width(120.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KasirColors.TextPrimary)
                        Text("$datePrefix${o.time} WIB", Modifier.width(130.dp), fontSize = 13.sp, color = KasirColors.TextSecondary)
                        Text(o.table, Modifier.width(70.dp), fontWeight = FontWeight.Medium, fontSize = 13.sp, color = KasirColors.TextPrimary)
                        Text(o.items, Modifier.weight(1f), fontSize = 13.sp, color = KasirColors.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(rupiah(o.total), Modifier.width(96.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KasirColors.TextPrimary)
                        Text(o.payment, Modifier.width(100.dp), fontSize = 13.sp, color = KasirColors.TextSecondary)
                        Box(Modifier.width(104.dp)) { StatusBadge(o.status) }
                    }
                }
                HorizontalDivider(color = KasirColors.BorderLight)
            }
            Text(
                "Menampilkan ${filtered.size} pesanan terbaru dari ${formatNumber(orders)} pesanan",
                fontSize = 12.5.sp, color = KasirColors.TextMuted, modifier = Modifier.padding(horizontal = if (compact) 16.dp else 18.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun KpiCard(label: String, value: String, note: String, icon: ImageVector, color: Color, tint: Color, noteColor: Color, modifier: Modifier) {
    AdminCard(modifier, padding = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontWeight = FontWeight.Medium, fontSize = 12.5.sp, color = KasirColors.TextMuted, modifier = Modifier.weight(1f))
            Box(Modifier.size(32.dp).background(tint, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(17.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = KasirColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        Text(note, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = noteColor)
    }
}

@Composable
private fun Legend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(color, RoundedCornerShape(3.dp)))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 12.sp, color = KasirColors.TextMuted)
    }
}

@Composable
private fun BarChart(d: PeriodData, top: Long, peakIdx: Int, compact: Boolean = false) {
    val gap = when { d.values.size > 20 -> if (compact) 2.dp else 3.dp; compact -> 4.dp; else -> 8.dp }
    val every = if (compact) maxOf(d.labelEvery, if (d.values.size > 20) 10 else 3) else d.labelEvery
    Row(Modifier.fillMaxWidth().height(if (compact) 180.dp else 220.dp)) {
        Column(Modifier.width(44.dp).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.End) {
            listOf(4, 3, 2, 1, 0).forEach { k -> Text(if (k == 0) "0" else shortRupiah(top * k / 4), fontSize = 11.sp, color = KasirColors.TextMuted) }
        }
        Spacer(Modifier.width(10.dp))
        Row(
            Modifier.weight(1f).fillMaxHeight().padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalAlignment = Alignment.Bottom
        ) {
            d.values.forEachIndexed { i, v ->
                Box(
                    Modifier.weight(1f).fillMaxHeight((v.toFloat() / top).coerceAtLeast(0.01f))
                        .background(if (i == peakIdx) KasirColors.AccentRust else KasirColors.PrimaryTeal, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                )
            }
        }
    }
    HorizontalDivider(color = KasirColors.BorderLight, modifier = Modifier.padding(start = 54.dp))
    Row(Modifier.fillMaxWidth().padding(start = 54.dp, top = 6.dp), horizontalArrangement = Arrangement.spacedBy(gap)) {
        d.labels.forEachIndexed { i, l ->
            val show = every == 1 || i == 0 || (i + 1) % every == 0
            Text(if (show) l else "", Modifier.weight(1f), fontSize = 10.sp, color = KasirColors.TextMuted, textAlign = TextAlign.Center, maxLines = 1, softWrap = false, overflow = TextOverflow.Visible)
        }
    }
}

@Composable
private fun LogHead(text: String, modifier: Modifier) {
    Text(text, modifier, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, letterSpacing = 0.5.sp, color = KasirColors.TextMuted)
}

@Composable
private fun StatusBadge(status: OrderStatus) {
    val (bg, fg) = when (status) {
        OrderStatus.SELESAI -> KasirColors.SelectedTableBg to KasirColors.PrimaryTeal
        OrderStatus.DIPROSES -> CoralTintSoft to KasirColors.AccentRust
        OrderStatus.DIBATALKAN -> KasirColors.NeutralEmptyBg to KasirColors.TextMuted
    }
    Row(Modifier.background(bg, RoundedCornerShape(999.dp)).padding(horizontal = 8.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).background(fg, CircleShape))
        Spacer(Modifier.width(5.dp))
        Text(status.label, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = fg)
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun LaporanScreenPreview() {
    LaporanScreen(store = AdminStore())
}
