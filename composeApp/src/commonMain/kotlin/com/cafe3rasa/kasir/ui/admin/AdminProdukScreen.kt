package com.cafe3rasa.kasir.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cafe3rasa.kasir.model.MenuCategory
import com.cafe3rasa.kasir.model.MenuItemModel
import com.cafe3rasa.kasir.state.AdminPanelState
import com.cafe3rasa.kasir.state.formatRupiah
import com.cafe3rasa.kasir.theme.KasirColors
import com.cafe3rasa.kasir.theme.KasirTypography

/** 07 — Produk & Kategori. */
@Composable
fun AdminProdukScreen(state: AdminPanelState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 20.dp)
            .padding(bottom = 72.dp) // ruang footer
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Produk & Kategori",
                    style = KasirTypography.headlineLarge.copy(fontSize = 24.sp)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Produk yang ditambahkan langsung tampil di menu pelanggan dan kasir sesuai kategorinya.",
                    style = KasirTypography.bodyLarge.copy(fontSize = 13.5.sp),
                    color = KasirColors.TextSecondary
                )
            }
            SearchBox(
                value = state.productSearch,
                onValueChange = { state.productSearch = it },
                placeholder = "Cari produk...",
                modifier = Modifier
                    .padding(top = 13.dp)
                    .width(281.dp)
                    .height(42.dp)
            )
        }

        Spacer(Modifier.height(28.dp))
        CategoryChips(state)

        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ProductTableCard(state, Modifier.weight(1f))
            Column(Modifier.width(340.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AddProductCard(state)
                CategoryCard(state)
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Produk & Kategori",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = KasirColors.PrimaryTeal,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun CategoryChips(state: AdminPanelState) {
    val chips = listOf(MenuCategory.SEMUA, MenuCategory.MAKANAN, MenuCategory.MINUMAN, MenuCategory.CAMILAN)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        chips.forEach { category ->
            val isActive = state.productCategoryFilter == category
            val isFirst = category == MenuCategory.SEMUA
            Row(
                modifier = Modifier
                    .height(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isActive) KasirColors.PrimaryTeal else KasirColors.Surface)
                    .border(
                        1.dp,
                        if (isActive) KasirColors.PrimaryTeal else KasirColors.Border,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { state.productCategoryFilter = category }
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = category.displayName,
                    fontSize = 12.9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isActive) Color.White else KasirColors.TextSecondary
                )
                Box(
                    modifier = Modifier
                        .width(if (isFirst) 24.dp else 26.dp)
                        .height(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isActive) Color.White.copy(alpha = 0.22f)
                            else KasirColors.NeutralEmptyBg
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.countFor(category).toString(),
                        fontSize = 11.7.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) Color.White else KasirColors.TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductTableCard(state: AdminPanelState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(KasirColors.Surface)
            .border(1.dp, KasirColors.Border, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
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
                text = "Daftar Produk",
                fontSize = 15.1.sp,
                fontWeight = FontWeight.Bold,
                color = KasirColors.TextPrimary
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "Menampilkan ${state.filteredProducts.size} dari ${state.products.size} produk",
                fontSize = 12.5.sp,
                color = KasirColors.TextMuted
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(KasirColors.LogTableBg)
                .padding(horizontal = 18.dp, vertical = 11.dp)
        ) {
            TableHeader("PRODUK", 1.6f)
            TableHeader("KATEGORI", 0.75f)
            TableHeader("HARGA", 0.75f)
            TableHeader("STATUS", 0.8f)
            TableHeader("AKSI", 0.5f)
        }

        state.filteredProducts.forEachIndexed { index, product ->
            if (index > 0) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(1.dp)
                        .background(KasirColors.TableRowDivider)
                )
            }
            ProductRow(state, product)
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun RowScope.TableHeader(text: String, weight: Float) {
    Text(
        text = text,
        fontSize = 11.4.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.4.sp,
        color = KasirColors.TextMuted,
        modifier = Modifier.weight(weight)
    )
}

@Composable
private fun ProductRow(state: AdminPanelState, product: MenuItemModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(Modifier.weight(1.6f), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(43.4.dp, 44.3.dp)
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
                    color = KasirColors.TextPrimary
                )
                Text(
                    text = product.description,
                    fontSize = 12.sp,
                    color = KasirColors.TextMuted
                )
            }
        }
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
        Text(
            text = formatRupiah(product.price),
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = KasirColors.TextPrimary,
            modifier = Modifier.weight(0.75f)
        )
        Row(Modifier.weight(0.8f), verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = product.isAvailable,
                onCheckedChange = { state.toggleProductAvailability(product.id) },
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
        }
        Row(Modifier.weight(0.5f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .size(30.dp, 29.7.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(KasirColors.Surface)
                    .border(1.dp, KasirColors.Border, RoundedCornerShape(7.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit ${product.name}",
                    tint = KasirColors.TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(29.7.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(KasirColors.PeachSoft)
                    .clickable { state.deleteProduct(product.id) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Hapus ${product.name}",
                    tint = KasirColors.AccentRust,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun AddProductCard(state: AdminPanelState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(KasirColors.Surface)
            .border(1.dp, KasirColors.Border, RoundedCornerShape(12.dp))
            .padding(19.dp)
    ) {
        AdminSectionTitle("Tambah Produk Baru", Icons.Default.AddCircleOutline)
        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(KasirColors.LogTableBg)
                .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.UploadFile,
                    contentDescription = null,
                    tint = KasirColors.PrimaryTeal,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Unggah foto produk",
                    fontSize = 13.1.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KasirColors.TextPrimary
                )
                Text(
                    text = "JPG / PNG, maks. 2 MB",
                    fontSize = 11.4.sp,
                    color = KasirColors.TextMuted
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        AdminFieldLabel("Nama Produk")
        Spacer(Modifier.height(6.dp))
        AdminTextField(
            value = state.draftName,
            onValueChange = { state.draftName = it },
            placeholder = "contoh: Es Kopi Pandan"
        )

        Spacer(Modifier.height(14.dp))
        AdminFieldLabel("Kategori")
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.categories.forEach { category ->
                val isActive = state.draftCategory == category
                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isActive) KasirColors.MintSoft else KasirColors.Surface)
                        .border(
                            if (isActive) 1.2.dp else 1.dp,
                            if (isActive) KasirColors.PrimaryTeal else KasirColors.Border,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { state.draftCategory = category }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.displayName,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isActive) KasirColors.PrimaryTeal else KasirColors.TextSecondary
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        AdminFieldLabel("Harga (Rp)")
        Spacer(Modifier.height(6.dp))
        AdminTextField(
            value = state.draftPrice,
            onValueChange = { state.draftPrice = it.filter(Char::isDigit) },
            placeholder = "25000"
        )

        Spacer(Modifier.height(14.dp))
        AdminFieldLabel("Deskripsi Singkat")
        Spacer(Modifier.height(6.dp))
        AdminTextField(
            value = state.draftDescription,
            onValueChange = { state.draftDescription = it },
            placeholder = "Bahan atau rasa utama produk",
            height = 60.dp
        )

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            AdminTextButton(
                text = "Reset",
                onClick = { state.resetDraft() },
                modifier = Modifier
                    .width(95.dp)
                    .height(43.dp)
            )
            AdminTextButton(
                text = "+  Simpan Produk",
                onClick = { state.saveDraftProduct() },
                modifier = Modifier
                    .weight(1f)
                    .height(43.dp),
                background = KasirColors.PrimaryTeal,
                border = null,
                textColor = Color.White
            )
        }
    }
}

@Composable
private fun CategoryCard(state: AdminPanelState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(KasirColors.Surface)
            .border(1.dp, KasirColors.Border, RoundedCornerShape(12.dp))
            .padding(19.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Layers,
                contentDescription = null,
                tint = KasirColors.PrimaryTeal,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Kategori",
                fontSize = 15.2.sp,
                fontWeight = FontWeight.Bold,
                color = KasirColors.TextPrimary
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${state.categories.size} kategori",
                fontSize = 12.6.sp,
                color = KasirColors.TextMuted
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) {
                AdminTextField(
                    value = state.newCategoryName,
                    onValueChange = { state.newCategoryName = it },
                    placeholder = "Nama kategori baru",
                    height = 37.dp
                )
            }
            AdminTextButton(
                text = "+ Tambah",
                onClick = { state.addCategory() },
                modifier = Modifier
                    .width(83.6.dp)
                    .height(37.3.dp),
                background = KasirColors.PrimaryDarkNavy,
                border = null,
                textColor = Color.White,
                fontSize = 12.8.sp
            )
        }

        Spacer(Modifier.height(14.dp))
        val dots = listOf(KasirColors.PrimaryTeal, KasirColors.AccentRust, Color(0xFFB4543C))
        state.categories.forEachIndexed { index, category ->
            if (index > 0) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(KasirColors.TableRowDivider)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.85.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(dots[index % dots.size])
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = category.displayName,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KasirColors.TextPrimary
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${state.products.count { it.category == category }} produk",
                    fontSize = 12.sp,
                    color = KasirColors.TextMuted
                )
                Spacer(Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(26.dp, 24.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(KasirColors.NeutralEmptyBg)
                        .clickable { state.deleteCategory(category) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Hapus kategori ${category.displayName}",
                        tint = KasirColors.InputPlaceholder,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Kategori yang masih berisi produk tidak bisa dihapus.",
            fontSize = 11.5.sp,
            color = KasirColors.InputPlaceholder
        )
    }
}

@Composable
fun AdminSectionTitle(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = KasirColors.PrimaryTeal,
            modifier = Modifier.size(15.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.9.sp,
            fontWeight = FontWeight.Bold,
            color = KasirColors.TextPrimary
        )
    }
}

@Composable
fun AdminFieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.5.sp,
        fontWeight = FontWeight.SemiBold,
        color = KasirColors.TextSecondary
    )
}

@Composable
fun AdminTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    height: androidx.compose.ui.unit.Dp = 39.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(KasirColors.LogTableBg)
            .border(1.dp, KasirColors.Border, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                fontSize = 13.sp,
                color = KasirColors.TextSubtle
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = height <= 40.dp,
            textStyle = TextStyle(fontSize = 13.sp, color = KasirColors.TextPrimary),
            cursorBrush = SolidColor(KasirColors.PrimaryTeal),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SearchBox(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(KasirColors.Surface)
            .border(1.dp, KasirColors.Border, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = KasirColors.InputPlaceholder,
            modifier = Modifier.size(17.dp)
        )
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) {
                Text(placeholder, fontSize = 13.sp, color = KasirColors.InputPlaceholder)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(fontSize = 13.sp, color = KasirColors.TextPrimary),
                cursorBrush = SolidColor(KasirColors.PrimaryTeal),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

internal fun categoryColor(category: MenuCategory): Color = when (category) {
    MenuCategory.MAKANAN -> KasirColors.PrimaryTeal
    MenuCategory.MINUMAN -> KasirColors.AccentRust
    MenuCategory.CAMILAN -> Color(0xFFB4543C)
    MenuCategory.SEMUA -> KasirColors.TextMuted
}
