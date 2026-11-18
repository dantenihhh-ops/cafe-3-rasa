package com.cafe3rasa.kasir.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cafe3rasa.kasir.model.MenuCategory
import com.cafe3rasa.kasir.model.MenuItemModel
import com.cafe3rasa.kasir.state.formatRupiah
import com.cafe3rasa.kasir.theme.KasirColors
import com.cafe3rasa.kasir.theme.KasirTypography

@Composable
fun QuickMenuSection(
    menuItems: List<MenuItemModel>,
    activeCategory: MenuCategory,
    activeTagFilter: String? = "Semua",
    availableTags: List<String> = listOf("Semua", "Best Seller", "Terlaris", "Rekomendasi"),
    searchQuery: String = "",
    onCategorySelected: (MenuCategory) -> Unit,
    onTagFilterSelected: (String) -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onAddItem: (MenuItemModel) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val isCompactWidth = maxWidth < 600.dp
        val categoryScrollState = rememberScrollState()
        val tagScrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(KasirColors.Background),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Title & Subtitle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "MENU",
                        style = KasirTypography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = KasirColors.TextPrimary
                        )
                    )
                    Text(
                        text = "(${menuItems.size} menu)",
                        style = KasirTypography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = KasirColors.TextMuted
                        )
                    )
                }
            }

            // Search Bar Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(KasirColors.Surface)
                    .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Cari",
                    tint = KasirColors.TextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Cari menu makanan, minuman, camilan...",
                            fontSize = 12.sp,
                            color = KasirColors.TextMuted
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 12.5.sp,
                            color = KasirColors.TextPrimary,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(KasirColors.PrimaryTeal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (searchQuery.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Hapus",
                        tint = KasirColors.TextMuted,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onSearchQueryChange("") }
                    )
                }
            }

            // Category Chips Row (Makanan, Minuman, Camilan)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(categoryScrollState),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MenuCategory.values().forEach { category ->
                    val isSelected = category == activeCategory
                    val chipBg = if (isSelected) KasirColors.AccentRust else KasirColors.Surface
                    val borderColor = if (isSelected) KasirColors.AccentRust else KasirColors.BorderLight
                    val textColor = if (isSelected) KasirColors.TextWhite else KasirColors.TextSecondary

                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(chipBg)
                            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
                            .clickable { onCategorySelected(category) }
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.displayName,
                            style = KasirTypography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = textColor
                            )
                        )
                    }
                }
            }

            // Filter Tag Opsional Row (Best Seller, Terlaris, Rekomendasi)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(tagScrollState),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                availableTags.forEach { tag ->
                    val isSelected = (tag == activeTagFilter) || (activeTagFilter == null && tag == "Semua")
                    val chipBg = if (isSelected) KasirColors.PrimaryTeal else KasirColors.SurfaceLight
                    val borderColor = if (isSelected) KasirColors.PrimaryTeal else KasirColors.BorderLight
                    val textColor = if (isSelected) KasirColors.TextWhite else KasirColors.TextMuted

                    Box(
                        modifier = Modifier
                            .height(26.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(chipBg)
                            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
                            .clickable { onTagFilterSelected(tag) }
                            .padding(horizontal = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (tag == "Semua") "Semua Label" else tag,
                            style = KasirTypography.labelSmall.copy(
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = textColor
                            )
                        )
                    }
                }
            }

            // Empty State jika pencarian tidak ditemukan
            if (menuItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(KasirColors.Surface)
                        .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Menu tidak ditemukan untuk filter ini.",
                        fontSize = 12.sp,
                        color = KasirColors.TextMuted
                    )
                }
            }

            // Menu Items Grid (1 column on compact mobile, 2 columns on wide screens)
            if (isCompactWidth) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    menuItems.forEach { item ->
                        QuickMenuCard(
                            item = item,
                            onAdd = { onAddItem(item) }
                        )
                    }
                }
            } else {
                val chunked = menuItems.chunked(2)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    chunked.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { item ->
                                QuickMenuCard(
                                    item = item,
                                    onAdd = { onAddItem(item) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickMenuCard(
    item: MenuItemModel,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(KasirColors.Surface)
            .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(KasirColors.NeutralEmptyBg)
                    .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.category == MenuCategory.MINUMAN) Icons.Default.LocalCafe else Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = if (item.category == MenuCategory.CAMILAN || item.category == MenuCategory.MINUMAN) KasirColors.AccentRust else KasirColors.PrimaryTeal,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Name, Tags & Price
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val badgeBg = if (item.category == MenuCategory.MAKANAN) KasirColors.PrimaryTeal else KasirColors.AccentRust
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(badgeBg)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = item.category.name,
                            style = KasirTypography.labelSmall.copy(
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = KasirColors.TextWhite
                            )
                        )
                    }

                    // Tampilkan badge filter opsional (seperti Best Seller / Terlaris) jika ada
                    item.filters.firstOrNull()?.let { filterTag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(KasirColors.SurfaceLight)
                                .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = filterTag,
                                style = KasirTypography.labelSmall.copy(
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KasirColors.PrimaryDarkNavy
                                )
                            )
                        }
                    }

                    Text(
                        text = item.name,
                        style = KasirTypography.titleSmall.copy(
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = KasirColors.TextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = formatRupiah(item.price),
                    style = KasirTypography.titleSmall.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = KasirColors.PrimaryTeal
                    )
                )
            }

            // Add button (+)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(KasirColors.PrimaryTeal)
                    .clickable { onAdd() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah ${item.name}",
                    tint = KasirColors.TextWhite,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
