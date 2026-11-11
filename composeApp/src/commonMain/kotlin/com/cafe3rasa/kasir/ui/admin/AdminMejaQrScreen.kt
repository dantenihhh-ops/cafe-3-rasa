package com.cafe3rasa.kasir.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import com.cafe3rasa.kasir.state.AdminPanelState
import com.cafe3rasa.kasir.state.TableArea
import com.cafe3rasa.kasir.theme.KasirColors
import com.cafe3rasa.kasir.theme.KasirTypography

/** 09 — Meja & QR Pemesanan. */
@Composable
fun AdminMejaQrScreen(state: AdminPanelState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 20.dp)
            .padding(bottom = 72.dp) // ruang footer
    ) {
        Text(
            text = "Meja & QR Pemesanan",
            style = KasirTypography.headlineLarge.copy(fontSize = 24.sp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Tambahkan meja baru sesuai kondisi di lokasi, lalu cetak QR-nya untuk ditempel di meja tersebut.",
            style = KasirTypography.bodyLarge.copy(fontSize = 13.5.sp),
            color = KasirColors.TextSecondary
        )

        Spacer(Modifier.height(24.dp))
        Row {
            StatCard("Total Meja", state.totalTables, Modifier.weight(1f))
            Spacer(Modifier.width(12.dp))
            StatCard("Area Utama", state.mainAreaTables, Modifier.weight(1f))
            Spacer(Modifier.width(12.dp))
            StatCard("Selasar & Lainnya", state.otherAreaTables, Modifier.weight(1f))
            Spacer(Modifier.width(12.dp))
            StatCard("Sedang Terisi", state.occupiedTables, Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TableListCard(state, Modifier.weight(1f))
            Column(Modifier.width(364.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                AddTableCard(state)
                QrPreviewCard(state)
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Meja & QR",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = KasirColors.PrimaryTeal,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun StatCard(label: String, value: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(KasirColors.Surface)
            .border(1.dp, KasirColors.Border, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(label, fontSize = 12.sp, color = KasirColors.TextSecondary)
        Spacer(Modifier.height(4.dp))
        Text(
            text = value.toString(),
            fontSize = 22.4.sp,
            fontWeight = FontWeight.Bold,
            color = KasirColors.TextPrimary
        )
    }
}

@Composable
private fun TableListCard(state: AdminPanelState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(KasirColors.Surface)
            .border(1.dp, KasirColors.Border, RoundedCornerShape(12.dp))
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.GridView,
                contentDescription = null,
                tint = KasirColors.TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Daftar Meja",
                fontSize = 15.1.sp,
                fontWeight = FontWeight.Bold,
                color = KasirColors.TextPrimary
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "Pilih kartu meja untuk melihat QR-nya",
                fontSize = 12.4.sp,
                color = KasirColors.TextMuted
            )
        }
        Spacer(Modifier.height(16.dp))

        // Grid 4 kolom
        state.tableRows.chunked(4).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(11.5.dp)) {
                rowItems.forEach { table ->
                    TableCard(
                        number = table.number,
                        area = table.area,
                        seats = table.seats,
                        isOccupied = table.isOccupied,
                        isSelected = state.selectedQrTable == table.number,
                        onClick = { state.selectedQrTable = table.number },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(4 - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(13.dp))
        }
    }
}

@Composable
private fun TableCard(
    number: String,
    area: TableArea,
    seats: Int,
    isOccupied: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(131.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) KasirColors.MintSoft else KasirColors.Surface)
            .border(
                if (isSelected) 1.5.dp else 1.dp,
                if (isSelected) KasirColors.PrimaryTeal else KasirColors.Border,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = number,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = KasirColors.TextPrimary
            )
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(if (isOccupied) KasirColors.AccentRust else KasirColors.NeutralEmptyBg)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (isOccupied) "TERISI" else "KOSONG",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isOccupied) Color.White else KasirColors.TextMuted
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(area.label, fontSize = 12.4.sp, color = KasirColors.TextSecondary)
        Spacer(Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.EventSeat,
                contentDescription = null,
                tint = KasirColors.InputPlaceholder,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(5.dp))
            Text("$seats kursi", fontSize = 12.1.sp, color = KasirColors.TextMuted)
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.QrCode2,
                contentDescription = null,
                tint = KasirColors.TextSecondary,
                modifier = Modifier.size(11.dp)
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = if (isSelected) "QR ditampilkan" else "Lihat QR",
                fontSize = 11.8.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) KasirColors.PrimaryTeal else KasirColors.TextSecondary
            )
        }
    }
}

@Composable
private fun AddTableCard(state: AdminPanelState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(KasirColors.Surface)
            .border(1.dp, KasirColors.Border, RoundedCornerShape(12.dp))
            .padding(18.dp)
    ) {
        AdminSectionTitle("Tambah Meja Baru", Icons.Default.AddCircleOutline)
        Spacer(Modifier.height(14.dp))

        AdminFieldLabel("Nomor Meja")
        Spacer(Modifier.height(6.dp))
        AdminTextField(
            value = state.newTableNumber,
            onValueChange = { state.newTableNumber = it.filter(Char::isDigit).take(2) },
            placeholder = "09",
            height = 40.dp
        )

        Spacer(Modifier.height(14.dp))
        AdminFieldLabel("Area")
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TableArea.entries.forEach { area ->
                val isActive = state.newTableArea == area
                Box(
                    modifier = Modifier
                        .height(28.3.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isActive) KasirColors.MintSoft else KasirColors.Surface)
                        .border(
                            if (isActive) 1.2.dp else 1.dp,
                            if (isActive) KasirColors.PrimaryTeal else KasirColors.Border,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { state.newTableArea = area }
                        .padding(horizontal = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = area.label,
                        fontSize = 12.4.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isActive) KasirColors.PrimaryTeal else KasirColors.TextSecondary
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        AdminFieldLabel("Kapasitas Kursi")
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(5.5.dp)) {
            listOf(2, 4, 6, 8).forEach { seats ->
                val isActive = state.newTableSeats == seats
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(28.3.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isActive) KasirColors.MintSoft else KasirColors.Surface)
                        .border(
                            if (isActive) 1.2.dp else 1.dp,
                            if (isActive) KasirColors.PrimaryTeal else KasirColors.Border,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { state.newTableSeats = seats },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = seats.toString(),
                        fontSize = 13.2.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isActive) KasirColors.PrimaryTeal else KasirColors.TextSecondary
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.5.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(KasirColors.PrimaryTeal)
                .clickable { state.addTable() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.QrCode2,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(17.dp)
            )
            Spacer(Modifier.width(9.dp))
            Text(
                text = "Tambah Meja & Generate QR",
                fontSize = 13.8.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun QrPreviewCard(state: AdminPanelState) {
    val table = state.selectedQrRow
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(KasirColors.Surface)
            .border(1.dp, KasirColors.Border, RoundedCornerShape(12.dp))
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Preview Cetak QR",
                fontSize = 14.8.sp,
                fontWeight = FontWeight.Bold,
                color = KasirColors.TextPrimary
            )
            Spacer(Modifier.weight(1f))
            Text("Ukuran A6", fontSize = 11.8.sp, color = KasirColors.TextMuted)
        }
        Spacer(Modifier.height(14.dp))

        // Area preview abu-abu
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(KasirColors.LogTableBg),
            contentAlignment = Alignment.Center
        ) {
            // Kartu A6
            Column(
                modifier = Modifier
                    .size(236.dp, 308.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(KasirColors.Surface)
                    .border(1.6.dp, KasirColors.PrimaryDarkNavy, RoundedCornerShape(8.dp))
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(19.dp, 22.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(KasirColors.PrimaryDarkNavy)
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = "Cafe 3 Rasa",
                        fontSize = 14.7.sp,
                        fontWeight = FontWeight.Bold,
                        color = KasirColors.PrimaryDarkNavy
                    )
                }
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(KasirColors.PrimaryDarkNavy)
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "MEJA ${table.number}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                Spacer(Modifier.height(14.dp))
                QrCode(
                    seed = "https://cafe3rasa.id/order?meja=${table.number}",
                    modifier = Modifier.size(149.dp)
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Scan untuk lihat menu & pesan",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KasirColors.TextPrimary
                )
                Text(
                    text = "cafe3rasa.id/order?meja=${table.number}",
                    fontSize = 10.5.sp,
                    color = KasirColors.TextMuted
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        Text(
            text = "Meja ${table.number} • ${table.area.label} • ${table.seats} kursi. QR ini membuka menu dan " +
                "mengunci pesanan ke meja ${table.number}.",
            fontSize = 11.8.sp,
            lineHeight = 16.sp,
            color = KasirColors.TextSecondary
        )

        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            AdminTextButton(
                text = "Unduh PNG",
                onClick = {},
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(KasirColors.AccentRust)
                    .clickable { },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Print,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(7.dp))
                Text("Cetak QR", fontSize = 12.7.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

/**
 * QR "dekoratif" deterministik — bukan encoder QR sungguhan.
 * TODO: ganti dengan generator QR (mis. zxing / Firebase Dynamic Link) saat backend siap.
 */
@Composable
fun QrCode(seed: String, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.background(Color.White)) {
        val modules = 25
        val cell = size.minDimension / modules
        val dark = Color(0xFF101C36)

        fun hash(x: Int, y: Int): Int {
            var h = x * 374761393 + y * 668265263
            for (c in seed) h = h * 31 + c.code
            h = (h xor (h shr 13)) * 1274126177
            return (h xor (h shr 16)) and 0x7fffffff
        }

        fun finder(cx: Int, cy: Int) {
            for (fy in 0 until 7) for (fx in 0 until 7) {
                val edge = fx == 0 || fx == 6 || fy == 0 || fy == 6
                val core = fx in 2..4 && fy in 2..4
                if (edge || core) {
                    drawRect(
                        color = dark,
                        topLeft = Offset((cx + fx) * cell, (cy + fy) * cell),
                        size = Size(cell, cell)
                    )
                }
            }
        }

        // Timing pattern
        for (i in 8 until modules - 8) {
            if (i % 2 == 0) {
                drawRect(dark, Offset(i * cell, 6 * cell), Size(cell, cell))
                drawRect(dark, Offset(6 * cell, i * cell), Size(cell, cell))
            }
        }

        // Data modules (hindari area finder)
        for (y in 0 until modules) for (x in 0 until modules) {
            val inFinder = (x < 8 && y < 8) || (x >= modules - 8 && y < 8) || (x < 8 && y >= modules - 8)
            if (inFinder) continue
            if (hash(x, y) % 100 < 47) {
                drawRect(dark, Offset(x * cell, y * cell), Size(cell, cell))
            }
        }

        finder(0, 0)
        finder(modules - 7, 0)
        finder(0, modules - 7)
    }
}
