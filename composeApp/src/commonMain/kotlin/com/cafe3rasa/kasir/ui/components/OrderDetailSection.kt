package com.cafe3rasa.kasir.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cafe3rasa.kasir.model.MenuCategory
import com.cafe3rasa.kasir.model.OrderItemModel
import com.cafe3rasa.kasir.state.formatRupiah
import com.cafe3rasa.kasir.theme.KasirColors
import com.cafe3rasa.kasir.theme.KasirTypography

@Composable
fun OrderDetailSection(
    tableNumber: String,
    isOccupied: Boolean,
    orderItems: List<OrderItemModel>,
    onToggleOccupancy: () -> Unit,
    onClearTable: () -> Unit,
    onChangeTable: () -> Unit,
    onIncrementQty: (String) -> Unit,
    onDecrementQty: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(KasirColors.Background),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Table Active Header
        SelectedTableHeader(
            tableNumber = tableNumber,
            isOccupied = isOccupied,
            onToggleOccupancy = onToggleOccupancy,
            onClearTable = onClearTable,
            onChangeTable = onChangeTable
        )

        // Order List Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                    contentDescription = null,
                    tint = KasirColors.PrimaryTeal,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "DAFTAR PESANAN MEJA $tableNumber",
                    style = KasirTypography.titleMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = KasirColors.TextPrimary
                    )
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(KasirColors.PrimaryDarkNavy)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                val totalQty = orderItems.sumOf { it.quantity }
                Text(
                    text = "$totalQty Item Terpilih",
                    style = KasirTypography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = KasirColors.TextWhite
                    )
                )
            }
        }

        // Order Item Cards
        if (orderItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(KasirColors.Surface)
                    .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada pesanan untuk meja ini.",
                    style = KasirTypography.bodyMedium.copy(color = KasirColors.TextMuted)
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                orderItems.forEach { item ->
                    OrderItemCard(
                        item = item,
                        onIncrement = { onIncrementQty(item.id) },
                        onDecrement = { onDecrementQty(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedTableHeader(
    tableNumber: String,
    isOccupied: Boolean,
    onToggleOccupancy: () -> Unit,
    onClearTable: () -> Unit,
    onChangeTable: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(KasirColors.Surface)
            .border(1.dp, KasirColors.Border, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: Table Label & Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Meja $tableNumber",
                    style = KasirTypography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = KasirColors.TextPrimary
                    )
                )

                // Occupancy Badge (TERISI / KOSONG)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isOccupied) KasirColors.AccentRust else KasirColors.PrimaryTeal)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isOccupied) "TERISI" else "KOSONG",
                        style = KasirTypography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = KasirColors.TextWhite
                        )
                    )
                }

                if (isOccupied) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(KasirColors.SelectedTableBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Pesanan Aktif",
                            style = KasirTypography.labelSmall.copy(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = KasirColors.PrimaryDarkNavy
                            )
                        )
                    }
                }
            }

            // Row 2: Subtitle Time info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = KasirColors.PrimaryTeal,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "Aktif sejak: ",
                    style = KasirTypography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        color = KasirColors.TextMuted
                    )
                )
                Text(
                    text = "09:25 WIB",
                    style = KasirTypography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = KasirColors.TextPrimary
                    )
                )
                Text(
                    text = "| 16 menit lalu",
                    style = KasirTypography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        color = KasirColors.TextMuted
                    )
                )
            }

            // Divider subtle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(KasirColors.BorderLight)
            )

            // Row 3: Action Controls (Status Okupansi Switch + Kosongkan Meja Button)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Switch Okupansi
                Row(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(KasirColors.Background)
                        .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column {
                        Text(
                            text = "STATUS OKUPANSI",
                            style = KasirTypography.labelSmall.copy(
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = KasirColors.TextMuted
                            )
                        )
                        Text(
                            text = if (isOccupied) "Terisi (Aktif)" else "Kosong",
                            style = KasirTypography.labelLarge.copy(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOccupied) KasirColors.AccentRust else KasirColors.PrimaryTeal
                            )
                        )
                    }

                    // Interactive Switch (Spring animated knob)
                    val knobOffset by animateDpAsState(
                        targetValue = if (isOccupied) 20.dp else 2.dp,
                        animationSpec = spring(stiffness = 300f, dampingRatio = 0.75f)
                    )

                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .height(24.dp)
                            .clip(CircleShape)
                            .background(if (isOccupied) KasirColors.PrimaryTeal else KasirColors.SwitchInactive)
                            .clickable { onToggleOccupancy() }
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .offset(x = knobOffset)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(KasirColors.Surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = if (isOccupied) KasirColors.PrimaryTeal else KasirColors.SwitchInactive,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }

                // Ganti Meja Button (Berganti ke meja yang masih kosong/belum aktif)
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(KasirColors.Surface)
                        .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(8.dp))
                        .clickable { onChangeTable() }
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Ganti Meja",
                            tint = KasirColors.PrimaryTeal,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Ganti Meja",
                            style = KasirTypography.labelLarge.copy(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = KasirColors.TextPrimary
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderItemCard(
    item: OrderItemModel,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(KasirColors.Surface)
            .border(1.5.dp, KasirColors.PrimaryTeal, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: Item Thumbnail + Title & Category + Unit Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Item Image Thumbnail
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(KasirColors.Background)
                        .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.category == MenuCategory.MINUMAN) Icons.Default.LocalCafe else Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = if (item.category == MenuCategory.CAMILAN) KasirColors.AccentRust else KasirColors.PrimaryTeal,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Name, Category & Price
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val badgeBg = if (item.category == MenuCategory.CAMILAN) KasirColors.AccentRust else KasirColors.PrimaryTeal
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(badgeBg)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = item.category.name,
                                style = KasirTypography.labelSmall.copy(
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KasirColors.TextWhite
                                )
                            )
                        }

                        Text(
                            text = item.name,
                            style = KasirTypography.titleMedium.copy(
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = KasirColors.TextPrimary
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "${formatRupiah(item.unitPrice)} / porsi",
                        style = KasirTypography.bodySmall.copy(
                            fontSize = 11.5.sp,
                            color = KasirColors.TextMuted
                        )
                    )
                }
            }

            // Row 2: Subtotal on Left + Stepper on Right (Guaranteed NO wrapping!)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(KasirColors.Background)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Subtotal
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Subtotal:",
                        style = KasirTypography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = KasirColors.TextMuted
                        )
                    )
                    Text(
                        text = formatRupiah(item.subtotal),
                        style = KasirTypography.titleMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = KasirColors.TextPrimary
                        )
                    )
                }

                // Stepper: [-] [qty] [+]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(KasirColors.Surface)
                            .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(6.dp))
                            .clickable { onDecrement() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "-",
                            style = KasirTypography.titleLarge.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = KasirColors.TextPrimary
                            )
                        )
                    }

                    Text(
                        text = item.quantity.toString(),
                        style = KasirTypography.titleMedium.copy(
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = KasirColors.TextPrimary
                        )
                    )

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(KasirColors.PrimaryTeal)
                            .clickable { onIncrement() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            style = KasirTypography.titleLarge.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = KasirColors.TextWhite
                            )
                        )
                    }
                }
            }

            // Row 3: Note Bar
            if (!item.note.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(KasirColors.Background)
                        .border(1.dp, KasirColors.BorderNote, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            tint = KasirColors.AccentRust,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Catatan: \"${item.note}\"",
                            style = KasirTypography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                color = KasirColors.TextSecondary
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(22.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(KasirColors.Surface)
                            .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(4.dp))
                            .clickable { }
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = KasirColors.TextPrimary,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = "Ubah",
                                style = KasirTypography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KasirColors.TextPrimary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
