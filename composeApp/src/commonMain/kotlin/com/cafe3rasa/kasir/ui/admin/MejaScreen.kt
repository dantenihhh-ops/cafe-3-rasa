package com.cafe3rasa.kasir.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cafe3rasa.kasir.theme.KasirColors
import org.jetbrains.compose.resources.painterResource
import pbl.composeapp.generated.resources.Res
import pbl.composeapp.generated.resources.logo_tiga_rasa

private const val ORDER_URL = "cafe3rasa.id/order?meja="

@Composable
fun MejaScreen(store: AdminStore, compact: Boolean = false) {
    var selected by rememberSaveable { mutableIntStateOf(4) }
    val sel = store.tables.firstOrNull { it.number == selected } ?: store.tables.first()
    val perRow = if (compact) 2 else 4

    val stats: @Composable () -> Unit = {
        val items = listOf(
            "Total Meja" to store.tables.size,
            "Area Utama" to store.tables.count { it.area == "Area Utama" },
            "Selasar & Lainnya" to store.tables.count { it.area != "Area Utama" },
            "Sedang Terisi" to store.tables.count { it.status == TableStatus.TERISI }
        )
        (if (compact) items.chunked(2) else listOf(items)).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (label, value) -> Stat(label, value, Modifier.weight(1f)) }
            }
        }
    }
    val tableList: @Composable () -> Unit = {
        AdminCard(padding = if (compact) 14.dp else 18.dp) {
            CardTitle(
                "Daftar Meja",
                Icons.Default.People,
                trailing = if (compact) "Ketuk untuk lihat QR" else "Pilih kartu meja untuk melihat QR-nya"
            )
            Spacer(Modifier.height(14.dp))
            store.tables.toList().chunked(perRow).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { t ->
                        TableCard(t, t.number == sel.number, Modifier.weight(1f)) { selected = t.number }
                    }
                    repeat(perRow - row.size) { Spacer(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }

    if (compact) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PageTitle("Meja & QR Pemesanan", "Tambah meja baru lalu cetak QR-nya untuk ditempel di meja.", compact = true)
            stats()
            AddTableCard(store, onAdded = { selected = it })
            QrPreviewCard(sel)
            tableList()
        }
        return
    }

    Row(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PageTitle("Meja & QR Pemesanan", "Tambahkan meja baru sesuai kondisi di lokasi, lalu cetak QR-nya untuk ditempel di meja tersebut.")
            stats()
            tableList()
        }
        Column(
            Modifier
                .width(360.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AddTableCard(store, onAdded = { selected = it })
            QrPreviewCard(sel)
        }
    }
}

@Composable
private fun Stat(label: String, value: Int, modifier: Modifier) {
    AdminCard(modifier, padding = 14.dp) {
        Text(label, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = KasirColors.TextMuted)
        Spacer(Modifier.height(4.dp))
        Text("$value", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = KasirColors.TextPrimary)
    }
}

@Composable
private fun TableCard(t: DiningTable, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val (sBg, sFg) = when (t.status) {
        TableStatus.TERISI -> KasirColors.AccentRust to Color.White
        TableStatus.BARU -> KasirColors.PrimaryDarkNavy to Color.White
        TableStatus.KOSONG -> KasirColors.NeutralEmptyBg to KasirColors.TextMuted
    }
    Column(
        modifier
            .height(132.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) KasirColors.SelectedTableBg else KasirColors.Surface)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) KasirColors.PrimaryTeal else KasirColors.BorderLight, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(tableNo(t.number), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (selected) KasirColors.PrimaryTeal else KasirColors.TextPrimary, modifier = Modifier.weight(1f))
            Pill(t.status.label, sBg, sFg, 9.5f, 4.dp)
        }
        Text(t.area, fontWeight = FontWeight.Medium, fontSize = 12.5.sp, color = KasirColors.TextSecondary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.People, null, tint = KasirColors.TextMuted, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text("${t.capacity} kursi", fontSize = 12.sp, color = KasirColors.TextMuted)
        }
        Spacer(Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.QrCode, null, tint = KasirColors.PrimaryTeal, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (selected) "QR ditampilkan" else "Lihat QR", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = KasirColors.PrimaryTeal)
        }
    }
}

@Composable
private fun AddTableCard(store: AdminStore, onAdded: (Int) -> Unit) {
    val nextNo = (store.tables.maxOfOrNull { it.number } ?: 0) + 1
    var no by rememberSaveable { mutableStateOf(tableNo(nextNo)) }
    var area by rememberSaveable { mutableStateOf("Area Utama") }
    var cap by rememberSaveable { mutableIntStateOf(4) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    AdminCard {
        CardTitle("Tambah Meja Baru", Icons.Default.Add)
        Spacer(Modifier.height(12.dp))
        FormLabel("Nomor Meja")
        FormField(no, { v -> no = v.filter { it.isDigit() }.take(3); error = null }, "contoh: 09", numeric = true)
        Spacer(Modifier.height(12.dp))
        FormLabel("Area")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Area Utama", "Selasar", "Lantai 2").forEach { a -> ChoiceChip(a, area == a, { area = a }, soft = true) }
        }
        Spacer(Modifier.height(12.dp))
        FormLabel("Kapasitas Kursi")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(2, 4, 6, 8).forEach { n ->
                Box(Modifier.weight(1f)) { ChoiceChip("$n", cap == n, { cap = n }, soft = true) }
            }
        }
        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error!!, fontSize = 12.sp, color = KasirColors.AccentRust)
        }
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = {
                val n = no.toIntOrNull() ?: 0
                error = store.addTable(n, area, cap)
                if (error == null) {
                    onAdded(n)
                    no = tableNo((store.tables.maxOfOrNull { it.number } ?: n) + 1)
                }
            },
            shape = RoundedCornerShape(9.dp),
            colors = ButtonDefaults.buttonColors(containerColor = KasirColors.PrimaryTeal),
            modifier = Modifier.fillMaxWidth().height(44.dp)
        ) {
            Icon(Icons.Default.QrCode, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Tambah Meja & Generate QR", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
        }
    }
}

@Composable
private fun QrPreviewCard(t: DiningTable) {
    val no = tableNo(t.number)
    AdminCard {
        Row {
            Text("Preview Cetak QR", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = KasirColors.TextPrimary, modifier = Modifier.weight(1f))
            Text("Ukuran A6", fontSize = 12.sp, color = KasirColors.TextMuted)
        }
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(KasirColors.Background)
                .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(12.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                Modifier
                    .width(236.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(KasirColors.Surface)
                    .border(2.dp, KasirColors.PrimaryDarkNavy, RoundedCornerShape(12.dp))
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(painterResource(Res.drawable.logo_tiga_rasa), null, modifier = Modifier.size(54.dp))
                Text(
                    "MEJA $no",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    modifier = Modifier.background(KasirColors.PrimaryDarkNavy, RoundedCornerShape(999.dp)).padding(horizontal = 12.dp, vertical = 3.dp)
                )
                QrCode(seed = t.number * 7919 + 17, modifier = Modifier.size(150.dp).semantics { contentDescription = "Kode QR untuk Meja $no" })
                Text("Scan untuk lihat menu & pesan", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = KasirColors.TextPrimary)
                Text("$ORDER_URL$no", fontSize = 10.5.sp, color = KasirColors.TextMuted)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Meja $no · ${t.area} · ${t.capacity} kursi. QR ini membuka menu dan mengunci pesanan ke meja $no.", fontSize = 12.sp, color = KasirColors.TextMuted)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = { },
                shape = RoundedCornerShape(9.dp),
                border = BorderStroke(1.dp, KasirColors.BorderLight),
                modifier = Modifier.weight(1f).height(40.dp)
            ) {
                Icon(Icons.Default.Download, null, tint = KasirColors.TextPrimary, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text("Unduh PNG", color = KasirColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Button(
                onClick = { },
                shape = RoundedCornerShape(9.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KasirColors.AccentRust),
                modifier = Modifier.weight(1f).height(40.dp)
            ) {
                Icon(Icons.Default.Print, null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text("Cetak QR", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun QrCode(seed: Int, modifier: Modifier = Modifier) {
    val n = 25
    val cells = remember(seed) { qrPattern(seed, n) }
    Canvas(modifier.background(Color.White).padding(6.dp)) {
        val cell = size.minDimension / n
        for (r in 0 until n) {
            for (c in 0 until n) {
                if (cells[r * n + c]) {
                    drawRect(Color(0xFF111827), Offset(c * cell, r * cell), Size(cell + 0.5f, cell + 0.5f))
                }
            }
        }
    }
}

private fun qrPattern(seed: Int, n: Int): BooleanArray {
    var x = (seed.toLong() * 2654435761L) and 0xFFFFFFFFL
    fun rnd(): Double {
        x = x xor ((x shl 13) and 0xFFFFFFFFL)
        x = x xor (x ushr 17)
        x = x xor ((x shl 5) and 0xFFFFFFFFL)
        return x.toDouble() / 4294967296.0
    }
    fun finder(r: Int, c: Int): Boolean? {
        if (r < 0 || c < 0 || r > 7 || c > 7) return null
        if (r == 7 || c == 7) return false
        if (r == 0 || r == 6 || c == 0 || c == 6) return true
        return r in 2..4 && c in 2..4
    }
    return BooleanArray(n * n) { i ->
        val r = i / n
        val c = i % n
        finder(r, c) ?: finder(r, n - 1 - c) ?: finder(n - 1 - r, c)
            ?: if (r in 16..20 && c in 16..20) {
                val rr = r - 16
                val cc = c - 16
                rr == 0 || rr == 4 || cc == 0 || cc == 4 || (rr == 2 && cc == 2)
            } else if (r == 6) c % 2 == 0
            else if (c == 6) r % 2 == 0
            else rnd() < 0.48
    }
}
