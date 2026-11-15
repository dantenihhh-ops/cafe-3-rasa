package com.cafe3rasa.kasir.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cafe3rasa.kasir.model.RequestBadgeType
import com.cafe3rasa.kasir.model.RequestStatus
import com.cafe3rasa.kasir.model.TableRequest
import com.cafe3rasa.kasir.theme.KasirColors
import com.cafe3rasa.kasir.theme.KasirTypography

// ─── Dialog Popup Notepad Timbal Balik Kasir ─────────────────────────────────

@Composable
private fun KasirResponseDialog(
    request: TableRequest,
    onDismiss: () -> Unit,
    onSendResponse: (String) -> Unit
) {
    var responseText by remember { mutableStateOf("Mohon ditunggu kak, sedang kami siapkan") }

    val quickReplies = listOf(
        "Mohon ditunggu kak",
        "Baik kak, kami akan segera mengantarkannya",
        "Sedang disiapkan staf ya kak",
        "Bill sedang dicetak, staf segera ke meja"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = KasirColors.Surface,
        shape = RoundedCornerShape(12.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(KasirColors.SelectedTableBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint = KasirColors.PrimaryTeal,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Konfirmasi Permintaan",
                        style = KasirTypography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = KasirColors.TextPrimary
                        )
                    )
                    Text(
                        text = "Kirim pesan informasi ke ${request.tableNumber}",
                        style = KasirTypography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = KasirColors.TextMuted
                        )
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Info Permintaan Konsumen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(KasirColors.Background)
                        .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Permintaan ${request.tableNumber}:",
                            style = KasirTypography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = KasirColors.TextMuted
                            )
                        )
                        Text(
                            text = "\"${request.note}\"",
                            style = KasirTypography.bodySmall.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = KasirColors.TextPrimary,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }

                // Template Balasan Cepat
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Pilih respon cepat atau ketik sendiri:",
                        style = KasirTypography.labelSmall.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = KasirColors.TextSecondary
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickReplies.take(2).forEach { reply ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(KasirColors.Surface)
                                    .border(1.dp, KasirColors.Border, RoundedCornerShape(6.dp))
                                    .clickable { responseText = reply }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = reply,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = KasirColors.TextPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickReplies.drop(2).forEach { reply ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(KasirColors.Surface)
                                    .border(1.dp, KasirColors.Border, RoundedCornerShape(6.dp))
                                    .clickable { responseText = reply }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = reply,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = KasirColors.TextPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Papan Notepad Balasan Kasir
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Papan Catatan Balasan ke Pelanggan:",
                        style = KasirTypography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = KasirColors.TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = responseText,
                        onValueChange = { responseText = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3,
                        placeholder = {
                            Text(
                                "Tulis informasi balasan untuk pelanggan...",
                                fontSize = 13.sp,
                                color = KasirColors.TextSubtle
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 13.sp,
                            color = KasirColors.TextPrimary
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = KasirColors.Surface,
                            unfocusedContainerColor = KasirColors.Background,
                            focusedBorderColor = KasirColors.PrimaryTeal,
                            unfocusedBorderColor = KasirColors.Border,
                            cursorColor = KasirColors.PrimaryTeal
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalMsg = responseText.trim().ifBlank { "Mohon ditunggu kak, sedang kami siapkan" }
                    onSendResponse(finalMsg)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = KasirColors.PrimaryTeal,
                    contentColor = KasirColors.TextWhite
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Kirim & Konfirmasi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = androidx.compose.foundation.BorderStroke(1.dp, KasirColors.Border),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = KasirColors.TextPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Batal", fontSize = 13.sp)
            }
        }
    )
}

// ─── Section Permintaan Meja ──────────────────────────────────────────────────

@Composable
fun TableRequestSection(
    requests: List<TableRequest>,
    onRequestConfirm: (String, String) -> Unit = { _, _ -> },
    onRequestDismiss: (String) -> Unit = {},
    onRequestHandled: (String) -> Unit = onRequestDismiss,
    modifier: Modifier = Modifier
) {
    val pendingCount = requests.count { it.status == RequestStatus.MENUNGGU }
    val inProgressCount = requests.count { it.status == RequestStatus.DIKONFIRMASI }

    // State untuk popup notepad balasan kasir
    var activeConfirmTarget by remember { mutableStateOf<TableRequest?>(null) }

    if (activeConfirmTarget != null) {
        KasirResponseDialog(
            request = activeConfirmTarget!!,
            onDismiss = { activeConfirmTarget = null },
            onSendResponse = { replyMsg ->
                onRequestConfirm(activeConfirmTarget!!.id, replyMsg)
                activeConfirmTarget = null
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(KasirColors.Surface)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = if (pendingCount > 0) KasirColors.AccentRust else KasirColors.PrimaryTeal,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "PERMINTAAN MEJA",
                    style = KasirTypography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = KasirColors.TextPrimary
                    )
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (pendingCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(KasirColors.AccentRust)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$pendingCount Perlu Respon",
                            style = KasirTypography.labelSmall.copy(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = KasirColors.TextWhite
                            )
                        )
                    }
                }
                if (inProgressCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(KasirColors.PrimaryTeal)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$inProgressCount Sedang Ditangani",
                            style = KasirTypography.labelSmall.copy(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = KasirColors.TextWhite
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Request list
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (requests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(KasirColors.Background)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada permintaan dari meja pelanggan",
                        fontSize = 12.sp,
                        color = KasirColors.TextMuted
                    )
                }
            } else {
                requests.forEach { request ->
                    TableRequestCard(
                        request = request,
                        onConfirmClick = {
                            if (request.status == RequestStatus.MENUNGGU) {
                                activeConfirmTarget = request
                            } else {
                                onRequestDismiss(request.id)
                            }
                        },
                        onDismiss = { onRequestDismiss(request.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TableRequestCard(
    request: TableRequest,
    onConfirmClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConfirmed = request.status == RequestStatus.DIKONFIRMASI

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isConfirmed) KasirColors.SelectedTableBg else KasirColors.Surface)
            .border(
                1.dp,
                if (isConfirmed) KasirColors.PrimaryTeal else KasirColors.BorderLight,
                RoundedCornerShape(8.dp)
            )
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Card Header: Table + Badge + Status + Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = request.tableNumber,
                        style = KasirTypography.titleSmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = KasirColors.TextPrimary
                        )
                    )

                    val badgeBg = when (request.badgeType) {
                        RequestBadgeType.MINTA_BILL_TUNAI, RequestBadgeType.PERINGATAN_DAPUR -> KasirColors.AccentRust
                        RequestBadgeType.PANGGIL_WAITER, RequestBadgeType.TAMBAH_PESANAN -> KasirColors.PrimaryDarkNavy
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(badgeBg)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = request.badgeLabel,
                            style = KasirTypography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = KasirColors.TextWhite
                            )
                        )
                    }

                    // Status Pill
                    if (isConfirmed) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(KasirColors.PrimaryTeal)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "STAF MENUJU MEJA",
                                style = KasirTypography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KasirColors.TextWhite
                                )
                            )
                        }
                    }
                }

                Text(
                    text = request.timeAgo,
                    style = KasirTypography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = KasirColors.TextMuted
                    )
                )
            }

            // Note Box (Pesan dari Konsumen)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(KasirColors.Background)
                    .border(1.dp, KasirColors.BorderNote, RoundedCornerShape(6.dp))
                    .padding(horizontal = 9.dp, vertical = 7.dp)
            ) {
                Row {
                    Text(
                        text = "Permintaan: ",
                        style = KasirTypography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = KasirColors.TextMuted
                        )
                    )
                    Text(
                        text = "\"${request.note}\"",
                        style = KasirTypography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = KasirColors.TextPrimary,
                            lineHeight = 17.sp
                        )
                    )
                }
            }

            // Timbal Balik Kasir (Jika sudah dikonfirmasi)
            if (isConfirmed) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(KasirColors.Surface)
                        .border(1.dp, KasirColors.PrimaryTeal, RoundedCornerShape(6.dp))
                        .padding(horizontal = 9.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = KasirColors.PrimaryTeal,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Timbal Balik Kasir: ",
                            style = KasirTypography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = KasirColors.PrimaryTeal
                            )
                        )
                        Text(
                            text = "\"${request.responseMessage}\"",
                            style = KasirTypography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                color = KasirColors.TextPrimary
                            )
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secondary Action Button (Tutup / Abaikan)
                Box(
                    modifier = Modifier
                        .height(26.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(KasirColors.Surface)
                        .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(5.dp))
                        .clickable { onDismiss() }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isConfirmed) "Tutup" else "Abaikan",
                        style = KasirTypography.labelSmall.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = KasirColors.TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Primary Action Button: "Konfirmasi" (saat menunggu) atau "Tandai Selesai" (saat dikonfirmasi)
                val btnColor = if (isConfirmed) {
                    KasirColors.PrimaryDarkNavy
                } else if (request.isAlertDapur || request.badgeType == RequestBadgeType.MINTA_BILL_TUNAI) {
                    KasirColors.AccentRust
                } else {
                    KasirColors.PrimaryTeal
                }

                val btnText = if (isConfirmed) "Tandai Selesai" else "Konfirmasi"

                Box(
                    modifier = Modifier
                        .height(26.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(btnColor)
                        .clickable { onConfirmClick() }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = btnText,
                        style = KasirTypography.labelSmall.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = KasirColors.TextWhite
                        )
                    )
                }
            }
        }
    }
}
