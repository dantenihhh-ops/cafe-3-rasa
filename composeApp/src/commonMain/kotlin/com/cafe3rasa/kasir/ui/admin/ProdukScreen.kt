package com.cafe3rasa.kasir.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cafe3rasa.kasir.data.rememberImagePicker
import com.cafe3rasa.kasir.theme.KasirColors
import com.cafe3rasa.kasir.ui.components.RemoteImage
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.painterResource

@Composable
fun ProdukScreen(store: AdminStore, compact: Boolean = false) {
    var filter by rememberSaveable { mutableStateOf("Semua") }
    var search by rememberSaveable { mutableStateOf("") }

    // Pemilih foto: simpan id produk yang sedang diganti fotonya
    var photoTarget by remember { mutableStateOf<String?>(null) }
    val pickPhoto = rememberImagePicker { bytes, error ->
        val id = photoTarget
        photoTarget = null
        when {
            error != null -> store.notifyError(error)
            bytes != null && id != null -> store.uploadProductImage(id, bytes)
        }
    }
    val onPickPhoto: (String) -> Unit = { id -> photoTarget = id; pickPhoto() }

    // Produk yang sedang diedit (dialog Edit Produk)
    var editingId by remember { mutableStateOf<String?>(null) }
    val editing = editingId?.let { id -> store.products.firstOrNull { it.id == id } }
    if (editing != null) {
        EditProductDialog(
            store = store,
            product = editing,
            onPickPhoto = { onPickPhoto(editing.id) },
            onDismiss = { editingId = null }
        )
    }

    val visible = store.products.filter { p ->
        val matchCategoryOrTag = when {
            filter == "Semua" -> true
            filter.startsWith("tag:") -> p.filters.contains(filter.removePrefix("tag:"))
            else -> p.category == filter
        }
        val matchSearch = search.isBlank() || p.name.contains(search.trim(), ignoreCase = true)
        matchCategoryOrTag && matchSearch
    }
    val chips: @Composable () -> Unit = {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChoiceChip("Semua", filter == "Semua", { filter = "Semua" }, count = store.products.size)
            store.categories.forEach { c -> ChoiceChip(c, filter == c, { filter = c }, count = store.countIn(c)) }
            store.availableFilters.forEach { f ->
                val countWithFilter = store.products.count { it.filters.contains(f) }
                ChoiceChip("Tag: $f", filter == "tag:$f", { filter = "tag:$f" }, count = countWithFilter)
            }
        }
    }

    if (compact) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PageTitle("Produk & Kategori", "Produk baru langsung tampil di menu sesuai kategorinya.", compact = true)
            FormField(search, { search = it }, "Cari produk...", leadingIcon = Icons.Default.Search)
            chips()
            AdminCard(padding = 0.dp) {
                Box(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    CardTitle("Daftar Produk", Icons.Default.Restaurant, trailing = "${visible.size}/${store.products.size}")
                }
                HorizontalDivider(color = KasirColors.BorderLight)
                if (visible.isEmpty()) {
                    Text("Belum ada produk di kategori ini.", color = KasirColors.TextMuted, fontSize = 13.5.sp, modifier = Modifier.padding(24.dp))
                }
                visible.forEach { p ->
                    ProductRowCompact(p, onToggle = { store.toggleAvailability(p.id) }, onDelete = { store.removeProduct(p.id) }, onPickPhoto = { onPickPhoto(p.id) }, onEdit = { editingId = p.id })
                    HorizontalDivider(color = KasirColors.BorderLight)
                }
            }
            AddProductCard(store)
            CategoryCard(store, onRemoved = { if (filter == it) filter = "Semua" })
        }
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Box(Modifier.weight(1f)) {
                PageTitle("Produk & Kategori", "Produk yang ditambahkan langsung tampil di menu pelanggan dan kasir sesuai kategorinya.")
            }
            FormField(search, { search = it }, "Cari produk...", Modifier.width(280.dp), leadingIcon = Icons.Default.Search)
        }
        chips()
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            AdminCard(Modifier.weight(1f), padding = 0.dp) {
                Box(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
                    CardTitle("Daftar Produk", Icons.Default.Restaurant, trailing = "Menampilkan ${visible.size} dari ${store.products.size} produk")
                }
                HorizontalDivider(color = KasirColors.BorderLight)
                Row(
                    Modifier.fillMaxWidth().background(KasirColors.Background).padding(horizontal = 18.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HeaderCell("PRODUK", Modifier.weight(1f))
                    HeaderCell("KATEGORI", Modifier.width(100.dp))
                    HeaderCell("HARGA", Modifier.width(92.dp))
                    HeaderCell("STATUS", Modifier.width(120.dp))
                    HeaderCell("AKSI", Modifier.width(72.dp))
                }
                HorizontalDivider(color = KasirColors.BorderLight)
                if (visible.isEmpty()) {
                    Text("Belum ada produk di kategori ini.", color = KasirColors.TextMuted, fontSize = 13.5.sp, modifier = Modifier.padding(32.dp).align(Alignment.CenterHorizontally))
                }
                Column {
                    visible.forEach { p ->
                        ProductRow(p, onToggle = { store.toggleAvailability(p.id) }, onDelete = { store.removeProduct(p.id) }, onPickPhoto = { onPickPhoto(p.id) }, onEdit = { editingId = p.id })
                        HorizontalDivider(color = KasirColors.BorderLight)
                    }
                }
            }
            Column(
                Modifier.width(340.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AddProductCard(store)
                CategoryCard(store, onRemoved = { if (filter == it) filter = "Semua" })
            }
        }
    }
}

@Composable
private fun ProductRowCompact(p: AdminProduct, onToggle: () -> Unit, onDelete: () -> Unit, onPickPhoto: () -> Unit, onEdit: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (p.image != null) {
                Image(painterResource(p.image), p.name, contentScale = ContentScale.Crop, modifier = Modifier.size(52.dp).clip(RoundedCornerShape(8.dp)).clickable { onPickPhoto() })
            } else if (p.imagePath?.startsWith("uploads/") == true) {
                RemoteImage(p.imagePath, p.name, modifier = Modifier.size(52.dp).clip(RoundedCornerShape(8.dp)).clickable { onPickPhoto() })
            } else {
                Box(
                    Modifier.size(52.dp).clip(RoundedCornerShape(8.dp)).background(KasirColors.NeutralEmptyBg).border(1.dp, KasirColors.BorderLight, RoundedCornerShape(8.dp)).clickable { onPickPhoto() },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.AddAPhoto, "Tambah foto", tint = KasirColors.TextSubtle, modifier = Modifier.size(18.dp)) }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Pill(p.category.uppercase(), categoryColor(p.category), Color.White, 10f, 4.dp)
                    p.filters.forEach { tag ->
                        Pill(tag.uppercase(), KasirColors.PrimaryDarkNavy, Color.White, 9f, 3.dp)
                    }
                }
                Text(p.name, fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = KasirColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(p.description, fontSize = 12.sp, color = KasirColors.TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(rupiah(p.price), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = KasirColors.TextPrimary, modifier = Modifier.weight(1f))
            Switch(
                checked = p.available,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = KasirColors.PrimaryTeal,
                    uncheckedTrackColor = KasirColors.SwitchInactive,
                    uncheckedThumbColor = Color.White
                )
            )
            Spacer(Modifier.width(6.dp))
            Text(
                if (p.available) "Tersedia" else "Habis",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (p.available) KasirColors.PrimaryTeal else KasirColors.AccentRust,
                modifier = Modifier.width(56.dp)
            )
            SmallIconButton(Icons.Default.Edit, "Edit ${p.name}", KasirColors.TextSecondary, KasirColors.Surface, KasirColors.BorderLight, onEdit)
            Spacer(Modifier.width(6.dp))
            SmallIconButton(Icons.Default.Delete, "Hapus ${p.name}", KasirColors.AccentRust, KasirColors.Background, KasirColors.BorderLight, onDelete)
        }
    }
}

@Composable
private fun HeaderCell(text: String, modifier: Modifier) {
    Text(text, modifier, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, letterSpacing = 0.5.sp, color = KasirColors.TextMuted)
}

@Composable
private fun ProductRow(p: AdminProduct, onToggle: () -> Unit, onDelete: () -> Unit, onPickPhoto: () -> Unit, onEdit: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            if (p.image != null) {
                Image(painterResource(p.image), p.name, contentScale = ContentScale.Crop, modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).clickable { onPickPhoto() })
            } else if (p.imagePath?.startsWith("uploads/") == true) {
                RemoteImage(p.imagePath, p.name, modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).clickable { onPickPhoto() })
            } else {
                Box(
                    Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).background(KasirColors.NeutralEmptyBg).border(1.dp, KasirColors.BorderLight, RoundedCornerShape(8.dp)).clickable { onPickPhoto() },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.AddAPhoto, "Tambah foto", tint = KasirColors.TextSubtle, modifier = Modifier.size(18.dp)) }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(p.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = KasirColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    p.filters.forEach { tag ->
                        Pill(tag.uppercase(), KasirColors.PrimaryDarkNavy, Color.White, 9.5f, 3.dp)
                    }
                }
                Text(p.description, fontSize = 12.sp, color = KasirColors.TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Box(Modifier.width(100.dp)) { Pill(p.category.uppercase(), categoryColor(p.category), Color.White, 10.5f, 4.dp) }
        Text(rupiah(p.price), Modifier.width(92.dp), fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = KasirColors.TextPrimary)
        Row(Modifier.width(120.dp), verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = p.available,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = KasirColors.PrimaryTeal,
                    uncheckedTrackColor = KasirColors.SwitchInactive,
                    uncheckedThumbColor = Color.White
                )
            )
            Spacer(Modifier.width(8.dp))
            Text(if (p.available) "Tersedia" else "Habis", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (p.available) KasirColors.PrimaryTeal else KasirColors.AccentRust)
        }
        Row(Modifier.width(72.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SmallIconButton(Icons.Default.Edit, "Edit ${p.name}", KasirColors.TextSecondary, KasirColors.Surface, KasirColors.BorderLight, onEdit)
            SmallIconButton(Icons.Default.Delete, "Hapus ${p.name}", KasirColors.AccentRust, KasirColors.Background, KasirColors.BorderLight, onDelete)
        }
    }
}

@Composable
fun SmallIconButton(icon: ImageVector, desc: String, tint: Color, bg: Color, border: Color, onClick: () -> Unit) {
    Box(
        Modifier.size(30.dp).clip(RoundedCornerShape(7.dp)).background(bg).border(1.dp, border, RoundedCornerShape(7.dp)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icon, desc, tint = tint, modifier = Modifier.size(14.dp)) }
}

@Composable
private fun AddProductCard(store: AdminStore) {
    var name by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(store.categories.firstOrNull() ?: "") }
    var price by rememberSaveable { mutableStateOf("") }
    var desc by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedFilters = remember { mutableStateListOf<String>() }
    var newFilterName by rememberSaveable { mutableStateOf("") }
    // Foto yang dipilih sebelum produk disimpan (diunggah setelah produk dibuat)
    var photo by remember { mutableStateOf<ByteArray?>(null) }
    val pickPhoto = rememberImagePicker { bytes, err ->
        if (err != null) store.notifyError(err) else if (bytes != null) photo = bytes
    }

    AdminCard {
        CardTitle("Tambah Produk Baru", Icons.Default.Add)
        Spacer(Modifier.height(12.dp))
        PhotoPickBox(photo, onPick = pickPhoto, onClear = { photo = null })
        Spacer(Modifier.height(12.dp))
        FormLabel("Nama Produk (Wajib)")
        FormField(name, { name = it; error = null }, "contoh: Es Kopi Pandan")
        Spacer(Modifier.height(12.dp))
        FormLabel("Kategori (Wajib)")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            store.categories.forEach { c -> ChoiceChip(c, category == c, { category = c; error = null }, soft = true) }
        }
        Spacer(Modifier.height(12.dp))

        // ── Filter Kurasi Opsional (Best Seller, Terlaris, Rekomendasi, dll.) ──
        Row(verticalAlignment = Alignment.CenterVertically) {
            FormLabel("Filter Khusus (Opsional)")
            Spacer(Modifier.weight(1f))
            Text("Tidak wajib diisi", fontSize = 11.sp, color = KasirColors.TextMuted)
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            store.availableFilters.forEach { f ->
                val isSelected = selectedFilters.contains(f)
                ChoiceChip(
                    text = f,
                    selected = isSelected,
                    onClick = {
                        if (isSelected) selectedFilters.remove(f) else selectedFilters.add(f)
                    },
                    soft = true
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FormField(
                value = newFilterName,
                onValueChange = { newFilterName = it },
                placeholder = "+ Tambah filter baru...",
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (newFilterName.isNotBlank()) {
                        val clean = newFilterName.trim()
                        if (store.addCustomFilter(clean)) {
                            selectedFilters.add(clean)
                        }
                        newFilterName = ""
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KasirColors.SurfaceLight),
                border = BorderStroke(1.dp, KasirColors.BorderLight),
                modifier = Modifier.height(44.dp)
            ) {
                Text("Tambah", fontSize = 12.sp, color = KasirColors.PrimaryTeal, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(12.dp))
        FormLabel("Harga (Rp) (Wajib)")
        FormField(price, { v -> price = v.filter { it.isDigit() }.take(9); error = null }, "25000", numeric = true)
        Spacer(Modifier.height(12.dp))
        FormLabel("Deskripsi Singkat")
        FormField(desc, { desc = it }, "Bahan atau rasa utama produk", singleLine = false, minLines = 2)
        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error!!, fontSize = 12.sp, color = KasirColors.AccentRust)
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = { name = ""; price = ""; desc = ""; selectedFilters.clear(); newFilterName = ""; error = null; photo = null },
                shape = RoundedCornerShape(9.dp),
                border = BorderStroke(1.dp, KasirColors.BorderLight),
                modifier = Modifier.width(96.dp).height(42.dp)
            ) { Text("Reset", color = KasirColors.TextSecondary, fontWeight = FontWeight.Bold) }
            Button(
                onClick = {
                    val p = price.toLongOrNull() ?: 0L
                    error = when {
                        name.isBlank() -> "Nama produk wajib diisi."
                        category !in store.categories -> "Pilih kategori terlebih dulu."
                        p <= 0 -> "Masukkan harga yang valid."
                        else -> null
                    }
                    if (error == null) {
                        store.addProduct(name.trim(), category, p, desc.trim(), filters = selectedFilters.toList(), imageBytes = photo)
                        name = ""; price = ""; desc = ""; photo = null
                        selectedFilters.clear()
                        newFilterName = ""
                    }
                },
                shape = RoundedCornerShape(9.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KasirColors.PrimaryTeal),
                modifier = Modifier.weight(1f).height(42.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text("Simpan Produk", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun CategoryCard(store: AdminStore, onRemoved: (String) -> Unit) {
    var newCat by rememberSaveable { mutableStateOf("") }
    AdminCard {
        CardTitle("Kategori", Icons.Default.BarChart, trailing = "${store.categories.size} kategori")
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FormField(newCat, { newCat = it }, "Nama kategori baru", Modifier.weight(1f))
            Button(
                onClick = { if (store.addCategory(newCat)) newCat = "" else newCat = "" },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KasirColors.PrimaryDarkNavy),
                modifier = Modifier.height(48.dp)
            ) { Text("+ Tambah", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White) }
        }
        Spacer(Modifier.height(8.dp))
        store.categories.toList().forEach { name ->
            val n = store.countIn(name)
            HorizontalDivider(color = KasirColors.BorderLight)
            Row(Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).background(categoryColor(name), RoundedCornerShape(3.dp)))
                Spacer(Modifier.width(10.dp))
                Text(name, fontWeight = FontWeight.Medium, fontSize = 13.5.sp, color = KasirColors.TextPrimary, modifier = Modifier.weight(1f))
                Text("$n produk", fontSize = 12.sp, color = KasirColors.TextMuted)
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = { store.removeCategory(name); if (n == 0) onRemoved(name) },
                    enabled = n == 0,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        "Hapus kategori $name",
                        tint = if (n == 0) KasirColors.AccentRust else KasirColors.BorderLight,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("Kategori yang masih berisi produk tidak bisa dihapus.", fontSize = 11.5.sp, color = KasirColors.TextMuted)
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun ProdukScreenPreview() {
    ProdukScreen(store = AdminStore())
}

/** Kotak pilih foto untuk produk baru, menampilkan pratinjau setelah dipilih */
@OptIn(ExperimentalResourceApi::class)
@Composable
private fun PhotoPickBox(photo: ByteArray?, onPick: () -> Unit, onClear: () -> Unit) {
    val preview = remember(photo) { photo?.let { runCatching { it.decodeToImageBitmap() }.getOrNull() } }
    Row(
        Modifier.fillMaxWidth().height(92.dp).clip(RoundedCornerShape(10.dp)).background(KasirColors.Background)
            .border(1.5.dp, KasirColors.BorderLight, RoundedCornerShape(10.dp)).clickable(onClick = onPick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (preview == null) Arrangement.Center else Arrangement.Start
    ) {
        if (preview == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CloudUpload, null, tint = KasirColors.PrimaryTeal, modifier = Modifier.size(22.dp))
                Spacer(Modifier.height(4.dp))
                Text("Unggah foto produk", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KasirColors.PrimaryTeal)
                Text("JPG / PNG / WEBP, maks. 2 MB", fontSize = 11.5.sp, color = KasirColors.TextMuted)
            }
        } else {
            Image(preview, "Foto produk baru", contentScale = ContentScale.Crop, modifier = Modifier.size(68.dp).clip(RoundedCornerShape(8.dp)))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Foto siap diunggah", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KasirColors.TextPrimary)
                Text("${(photo!!.size + 1023) / 1024} KB · ketuk untuk ganti", fontSize = 11.5.sp, color = KasirColors.TextMuted)
            }
            SmallIconButton(Icons.Default.Close, "Hapus foto", KasirColors.AccentRust, KasirColors.Surface, KasirColors.BorderLight, onClear)
        }
    }
}

/** Dialog untuk mengubah nama, kategori, harga, deskripsi, filter, dan foto produk */
@Composable
private fun EditProductDialog(store: AdminStore, product: AdminProduct, onPickPhoto: () -> Unit, onDismiss: () -> Unit) {
    var name by remember(product.id) { mutableStateOf(product.name) }
    var category by remember(product.id) { mutableStateOf(product.category) }
    var price by remember(product.id) { mutableStateOf(product.price.toString()) }
    var desc by remember(product.id) { mutableStateOf(product.description) }
    val filters = remember(product.id) { mutableStateListOf<String>().apply { addAll(product.filters) } }
    var error by remember(product.id) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = KasirColors.Surface,
        title = { Text("Edit Produk", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = KasirColors.TextPrimary) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Foto saat ini + tombol ganti
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val m = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp))
                    when {
                        product.image != null -> Image(painterResource(product.image), product.name, contentScale = ContentScale.Crop, modifier = m)
                        product.imagePath?.startsWith("uploads/") == true -> RemoteImage(product.imagePath, product.name, modifier = m)
                        else -> Box(m.background(KasirColors.NeutralEmptyBg), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Image, null, tint = KasirColors.TextSubtle)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = onPickPhoto,
                        shape = RoundedCornerShape(9.dp),
                        border = BorderStroke(1.dp, KasirColors.BorderLight)
                    ) {
                        Icon(Icons.Default.AddAPhoto, null, tint = KasirColors.PrimaryTeal, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Ganti Foto", color = KasirColors.PrimaryTeal, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
                FormLabel("Nama Produk")
                FormField(name, { name = it; error = null }, "Nama produk")
                FormLabel("Kategori")
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    store.categories.forEach { c -> ChoiceChip(c, category == c, { category = c; error = null }, soft = true) }
                }
                FormLabel("Harga (Rp)")
                FormField(price, { v -> price = v.filter { it.isDigit() }.take(9); error = null }, "25000", numeric = true)
                FormLabel("Deskripsi Singkat")
                FormField(desc, { desc = it }, "Bahan atau rasa utama produk", singleLine = false, minLines = 2)
                FormLabel("Filter Khusus (Opsional)")
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    store.availableFilters.forEach { f ->
                        val on = filters.contains(f)
                        ChoiceChip(f, on, { if (on) filters.remove(f) else filters.add(f) }, soft = true)
                    }
                }
                error?.let { Text(it, fontSize = 12.sp, color = KasirColors.AccentRust) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = price.toLongOrNull() ?: 0L
                    error = when {
                        name.trim().length < 2 -> "Nama produk minimal 2 karakter."
                        category !in store.categories -> "Pilih kategori terlebih dulu."
                        p <= 0 -> "Masukkan harga yang valid."
                        else -> null
                    }
                    if (error == null) {
                        store.updateProduct(product.id, name.trim(), category, p, desc.trim(), filters.toList())
                        onDismiss()
                    }
                },
                shape = RoundedCornerShape(9.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KasirColors.PrimaryTeal)
            ) { Text("Simpan Perubahan", fontWeight = FontWeight.Bold, color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = KasirColors.TextSecondary, fontWeight = FontWeight.Bold) }
        }
    )
}
