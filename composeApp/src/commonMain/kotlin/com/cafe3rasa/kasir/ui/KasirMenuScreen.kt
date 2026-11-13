package com.cafe3rasa.kasir.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.BasicTextField
import com.cafe3rasa.kasir.model.MenuCategory
import com.cafe3rasa.kasir.model.MenuItemModel
import com.cafe3rasa.kasir.state.AdminPanelState
import com.cafe3rasa.kasir.state.formatRupiah
import com.cafe3rasa.kasir.theme.KasirColors
import com.cafe3rasa.kasir.theme.KasirTypography
import com.cafe3rasa.kasir.ui.admin.categoryColor
import com.cafe3rasa.kasir.ui.components.CashierHeader

/**
 * Halaman Menu untuk Kasir.
 * Kasir hanya bisa mengaktifkan / menonaktifkan produk (tersedia / tidak tersedia).
 * CRUD produk & kategori tetap hak admin.
 */
@Composable
fun KasirMenuScreen(
    state: AdminPanelState = remember { AdminPanelState() },
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var search by remember { mutableStateOf("") }
    var categoryFilter by remember { mutableStateOf(MenuCategory.SEMUA) }

    val filtered = state.products.filter { product ->
        val matchCategory = categoryFilter == MenuCategory.SEMUA || product.category == categoryFilter
        val matchSearch = search.isBlank() || product.name.contains(search, ignoreCase = true)
        matchCategory && matchSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KasirColors.Background)
    ) {
        // ── Header (pakai CashierHeader agar settings dropdown konsisten) ────────
        CashierHeader(
            currentTimeText = "",
            onLogout = onLogout,
            onDashboard = onBack,     // Dashboard = kembali ke dashboard kasir
            onOpenMenu = {},          // Sudah di halaman Menu, tidak perlu buka lagi
        )

        // ── Konten ──────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Baris: Search + Filter Kategori
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Search box
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(KasirColors.Surface)
                        .border(1.dp, KasirColors.Border, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = KasirColors.InputPlaceholder,
                        modifier = Modifier.size(16.dp)
                    )
                    Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (search.isEmpty()) {
                            Text("Cari produk...", fontSize = 13.sp, color = KasirColors.InputPlaceholder)
                        }
                        BasicTextField(
                            value = search,
                            onValueChange = { search = it },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 13.sp, color = KasirColors.TextPrimary),
                            cursorBrush = SolidColor(KasirColors.PrimaryTeal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Filter chip kategori
                val chips = listOf(MenuCategory.SEMUA, MenuCategory.MAKANAN, MenuCategory.MINUMAN, MenuCategory.CAMILAN)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    chips.forEach { cat ->
                        val isActive = categoryFilter == cat
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isActive) KasirColors.PrimaryDarkNavy else KasirColors.Surface)
                                .border(
                                    1.dp,
                                    if (isActive) KasirColors.PrimaryDarkNavy else KasirColors.Border,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { categoryFilter = cat }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat.displayName,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isActive) Color.White else KasirColors.TextSecondary
                            )
                        }
                    }
                }
            }

            // Tabel produk
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(KasirColors.Surface)
                    .border(1.dp, KasirColors.Border, RoundedCornerShape(12.dp))
            ) {
                // Header tabel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KasirColors.LogTableBg)
                        .padding(horizontal = 20.dp, vertical = 11.dp)
                ) {
                    Text("PRODUK", fontSize = 11.4.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp, color = KasirColors.TextMuted, modifier = Modifier.weight(1.6f))
                    Text("KATEGORI", fontSize = 11.4.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp, color = KasirColors.TextMuted, modifier = Modifier.weight(0.75f))
                    Text("HARGA", fontSize = 11.4.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp, color = KasirColors.TextMuted, modifier = Modifier.weight(0.75f))
                    Text("TERSEDIA", fontSize = 11.4.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp, color = KasirColors.TextMuted, modifier = Modifier.weight(0.6f))
                }

                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada produk ditemukan.",
                            fontSize = 13.sp,
                            color = KasirColors.TextMuted
                        )
                    }
                } else {
                    filtered.forEachIndexed { index, product ->
                        if (index > 0) {
                            Box(
                                Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                                    .height(1.dp).background(KasirColors.TableRowDivider)
                            )
                        }
                        KasirMenuRow(
                            product = product,
                            onToggle = { state.toggleProductAvailability(product.id) }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
            }

            // Keterangan bawah
            Text(
                text = "Nonaktifkan menu jika sedang habis atau tidak tersedia hari ini. Perubahan hanya berlaku untuk sesi kasir.",
                fontSize = 11.5.sp,
                color = KasirColors.InputPlaceholder,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun KasirMenuRow(
    product: MenuItemModel,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Kolom: Produk (gambar + nama + deskripsi)
        Row(Modifier.weight(1.6f), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(43.dp, 44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(KasirColors.NeutralEmptyBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = KasirColors.TextSubtle,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = product.name,
                    fontSize = 13.9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (product.isAvailable) KasirColors.TextPrimary else KasirColors.TextMuted
                )
                Text(
                    text = product.description,
                    fontSize = 12.sp,
                    color = KasirColors.TextMuted
                )
            }
        }

        // Kolom: Kategori
        Box(Modifier.weight(0.75f)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(categoryColor(product.category))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = product.category.displayName.uppercase(),
                    fontSize = 10.7.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Kolom: Harga
        Text(
            text = formatRupiah(product.price),
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = KasirColors.TextPrimary,
            modifier = Modifier.weight(0.75f)
        )

        // Kolom: Toggle Tersedia
        Row(
            Modifier.weight(0.6f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Switch(
                checked = product.isAvailable,
                onCheckedChange = { onToggle() },
                modifier = Modifier
                    .width(36.dp)
                    .height(19.dp),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = KasirColors.PrimaryTeal,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = KasirColors.DisabledToggle,
                    uncheckedBorderColor = Color.Transparent
                )
            )
            Text(
                text = if (product.isAvailable) "Aktif" else "Nonaktif",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = if (product.isAvailable) KasirColors.PrimaryTeal else KasirColors.TextMuted
            )
        }
    }
}


// ─── Preview Langsung ─────────────────────────────────────────────────────────

@org.jetbrains.compose.ui.tooling.preview.Preview
@androidx.compose.runtime.Composable
private fun PreviewKasirMenuTablet() {
    com.cafe3rasa.kasir.theme.KasirCafeTheme {
        KasirMenuScreen()
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@androidx.compose.runtime.Composable
private fun PreviewKasirMenuHP() {
    com.cafe3rasa.kasir.theme.KasirCafeTheme {
        KasirMenuScreen()
    }
}
