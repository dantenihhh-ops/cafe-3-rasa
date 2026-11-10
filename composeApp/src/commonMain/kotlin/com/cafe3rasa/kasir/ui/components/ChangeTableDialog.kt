package com.cafe3rasa.kasir.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cafe3rasa.kasir.model.TableModel
import com.cafe3rasa.kasir.theme.KasirColors
import com.cafe3rasa.kasir.theme.KasirTypography

@Composable
fun ChangeTableDialog(
    currentTableNumber: String,
    emptyTables: List<TableModel>,
    onTableSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(KasirColors.Surface)
                .border(1.dp, KasirColors.Border, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(KasirColors.SelectedTableBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = KasirColors.PrimaryTeal,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Ganti Meja",
                                style = KasirTypography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = KasirColors.TextPrimary
                                )
                            )
                            Text(
                                text = "Pindahkan Meja $currentTableNumber ke meja kosong",
                                style = KasirTypography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    color = KasirColors.TextMuted
                                )
                            )
                        }
                    }

                    // Close Button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(KasirColors.NeutralEmptyBg)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = KasirColors.TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(KasirColors.BorderLight)
                )

                // List / Grid of Empty Tables
                if (emptyTables.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(KasirColors.Background)
                            .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(10.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = KasirColors.TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Tidak ada meja kosong yang tersedia.",
                                style = KasirTypography.bodyMedium.copy(
                                    color = KasirColors.TextMuted,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                } else {
                    Text(
                        text = "PILIH MEJA TUJUAN (STATUS KOSONG / BELUM AKTIF):",
                        style = KasirTypography.labelSmall.copy(
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = KasirColors.TextMuted
                        )
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.heightIn(max = 240.dp)
                    ) {
                        items(emptyTables) { table ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(KasirColors.Background)
                                    .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(10.dp))
                                    .clickable { onTableSelected(table.number) }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EventSeat,
                                            contentDescription = null,
                                            tint = KasirColors.PrimaryTeal,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "Meja ${table.number}",
                                                style = KasirTypography.labelLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = KasirColors.TextPrimary
                                                )
                                            )
                                            Text(
                                                text = "Kosong / Tersedia",
                                                style = KasirTypography.bodySmall.copy(
                                                    fontSize = 10.5.sp,
                                                    color = KasirColors.PrimaryTeal
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Footer Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tombol Batal
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(KasirColors.Background)
                            .border(1.dp, KasirColors.Border, RoundedCornerShape(8.dp))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Batal",
                            style = KasirTypography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = KasirColors.TextSecondary
                            )
                        )
                    }

                    // Tombol Pilih Meja Kosong Pertama (jika ada)
                    if (emptyTables.isNotEmpty()) {
                        val firstEmpty = emptyTables.first()
                        Box(
                            modifier = Modifier
                                .weight(1.5f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(KasirColors.PrimaryTeal)
                                .clickable { onTableSelected(firstEmpty.number) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ke Meja ${firstEmpty.number}",
                                style = KasirTypography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = KasirColors.TextWhite
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
