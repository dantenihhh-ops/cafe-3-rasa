package com.cafe3rasa.kasir.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cafe3rasa.kasir.data.ReviewsDto
import com.cafe3rasa.kasir.theme.KasirColors
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Halaman Informasi & Bantuan Admin:
 * Layout responsif menyesuaikan ukuran layar HP & Tablet tanpa whitespace berlebih.
 */
@Composable
fun AdminInfoScreen(
    store: AdminStore,
    onBack: () -> Unit,
    compact: Boolean = false
) {
    // Ambil rating & ulasan terbaru setiap halaman dibuka
    LaunchedEffect(Unit) { store.loadReviews() }
    val data = store.reviews
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KasirColors.Background)
    ) {
        // ── Top Bar Halaman Info ──
        Surface(
            color = KasirColors.Surface,
            border = BorderStroke(1.dp, KasirColors.Border)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = if (compact) 12.dp else 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onBack,
                    shape = RoundedCornerShape(8.dp),
                    color = KasirColors.SurfaceLight,
                    border = BorderStroke(1.dp, KasirColors.BorderLight),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke Dashboard",
                            tint = KasirColors.TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Pusat Informasi & Ulasan",
                        fontWeight = FontWeight.Bold,
                        fontSize = if (compact) 15.sp else 17.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = KasirColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Rating, komentar, CS, dan info pengembang",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = KasirColors.TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // ── Konten Scrollable (Responsif: Full Width di HP, Terpusat Rapi di Tablet/Desktop) ──
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(if (compact) 12.dp else 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
            // ── 1. Rating & Kepuasan Pelanggan (dari database) ──
            AdminCard(padding = if (compact) 14.dp else 18.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, null, tint = KasirColors.AccentRust, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Rating Pelanggan", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = KasirColors.TextPrimary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(KasirColors.MintSoft)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (data == null) "MEMUAT..." else "SKOR ${score(data.summary.avgRating)} / 5.0",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = KasirColors.PrimaryTeal
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            onClick = { store.loadReviews() },
                            shape = RoundedCornerShape(6.dp),
                            color = KasirColors.SurfaceLight,
                            border = BorderStroke(1.dp, KasirColors.BorderLight),
                            modifier = Modifier.size(26.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Refresh, "Muat ulang ulasan", tint = KasirColors.TextSecondary, modifier = Modifier.size(15.dp))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                if (data == null) {
                    Text(
                        if (store.reviewsLoading) "Memuat rating dari server..." else "Rating belum termuat. Ketuk tombol muat ulang.",
                        fontSize = 12.5.sp, color = KasirColors.TextMuted
                    )
                } else if (data.summary.totalReviews == 0) {
                    Text("Belum ada rating dari pelanggan. Rating diisi pelanggan di halaman Bantuan pada web menu.", fontSize = 12.5.sp, color = KasirColors.TextMuted)
                } else {
                    RatingOverview(data, compact)
                }
            }

            // ── 2. Komentar & Saran Pelanggan (dari database) ──
            AdminCard(padding = if (compact) 14.dp else 18.dp) {
                val withComment = data?.reviews.orEmpty().filter { !it.comment.isNullOrBlank() }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ChatBubbleOutline, null, tint = KasirColors.PrimaryTeal, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Komentar & Saran Pelanggan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = KasirColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(KasirColors.SurfaceLight)
                            .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${data?.summary?.withComment ?: 0} Komentar",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = KasirColors.TextMuted
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                if (withComment.isEmpty()) {
                    Text("Belum ada komentar dari pelanggan.", fontSize = 12.5.sp, color = KasirColors.TextMuted)
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    withComment.take(20).forEach { r ->
                        CustomerFeedbackItem(
                            meja = "Meja ${r.tableNumber}",
                            time = timeAgo(r.createdAt),
                            comment = r.comment.orEmpty(),
                            rating = "★".repeat(r.rating.coerceIn(0, 5)) + "☆".repeat((5 - r.rating).coerceIn(0, 5)),
                            aspects = r.aspects
                        )
                    }
                }
                if (withComment.size > 20) {
                    Spacer(Modifier.height(6.dp))
                    Text("Menampilkan 20 komentar terbaru.", fontSize = 11.sp, color = KasirColors.TextSubtle)
                }
            }

            // ── 3. Customer Service (CS) & Bantuan Operasional ──
            AdminCard(padding = if (compact) 14.dp else 18.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HeadsetMic,
                        contentDescription = null,
                        tint = KasirColors.PrimaryTeal,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Layanan Bantuan & CS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = KasirColors.TextPrimary
                    )
                }

                Spacer(Modifier.height(10.dp))

                if (compact) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CsContactCard(Icons.Default.Phone, "WhatsApp Hotline CS", "+62 821-3456-7890", "Aktif 08.00 - 23.00 WITA")
                        CsContactCard(Icons.Default.Email, "Email Dukungan", "support@tigarasa-cafe.id", "Respon tiket 1x24 jam")
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CsContactCard(Icons.Default.Phone, "WhatsApp Hotline CS", "+62 821-3456-7890", "Aktif 08.00 - 23.00 WITA", Modifier.weight(1f))
                        CsContactCard(Icons.Default.Email, "Email Dukungan", "support@tigarasa-cafe.id", "Respon tiket 1x24 jam", Modifier.weight(1f))
                    }
                }
            }

            // ── 4. Info & Deskripsi Aplikasi ──
            AdminCard(padding = if (compact) 14.dp else 18.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = KasirColors.PrimaryDarkNavy,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Info & Deskripsi Aplikasi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = KasirColors.TextPrimary
                    )
                }

                Spacer(Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(KasirColors.SurfaceLight)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Nama Sistem", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = KasirColors.TextPrimary)
                        Text("Tiga Rasa POS & E-Menu", fontSize = 12.sp, color = KasirColors.PrimaryTeal, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = KasirColors.BorderLight)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Versi Rilis", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = KasirColors.TextPrimary)
                        Text("v2.4.0 (2026)", fontSize = 12.sp, color = KasirColors.TextMuted)
                    }
                    HorizontalDivider(color = KasirColors.BorderLight)
                    Text(
                        text = "Sistem kasir pintar dan pemesanan digital E-Menu pelanggan berbasis QR Meja untuk Cafe 3 Rasa.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = KasirColors.TextMuted
                    )
                }
            }

            // ── 5. Nama Pencipta & Tim Pengembang (Menyesuaikan Ramping) ──
            AdminCard(padding = if (compact) 12.dp else 16.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = KasirColors.AccentRust,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Pencipta & Pengembang",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = KasirColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(KasirColors.PeachSoft)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PBL NO. 6",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = KasirColors.AccentRust
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(KasirColors.SurfaceLight)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (compact) {
                        // Di HP: Susun vertikal agar tidak sempit
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(KasirColors.Surface)
                                    .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(6.dp))
                                    .padding(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("Pencipta / Lead Dev", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = KasirColors.TextMuted)
                                    Text("Maharewa", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KasirColors.PrimaryTeal)
                                    Text("Full-Stack Mobile & Web", fontSize = 10.5.sp, color = KasirColors.TextMuted)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(KasirColors.Surface)
                                    .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(6.dp))
                                    .padding(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("Kategori Proyek", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = KasirColors.TextMuted)
                                    Text("PBL", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KasirColors.PrimaryDarkNavy)
                                    Text("Digitalisasi UMKM Resto", fontSize = 10.5.sp, color = KasirColors.TextMuted)
                                }
                            }
                        }
                    } else {
                        // Di Tablet / Layar Lebar: Berdampingan
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(KasirColors.Surface)
                                    .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(6.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text("Pencipta / Lead Developer", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KasirColors.TextMuted)
                                    Text("Maharewa", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = KasirColors.PrimaryTeal)
                                    Text("Full-Stack Mobile & Web Engineer", fontSize = 11.5.sp, color = KasirColors.TextMuted)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(KasirColors.Surface)
                                    .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(6.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text("Kategori Proyek", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KasirColors.TextMuted)
                                    Text("Project-Based Learning (PBL)", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = KasirColors.PrimaryDarkNavy)
                                    Text("Digitalisasi UMKM Restoran & POS", fontSize = 11.5.sp, color = KasirColors.TextMuted)
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = KasirColors.BorderLight, thickness = 0.5.dp)
                    Text(
                        text = "Hak Cipta © 2026 Tiga Rasa by Maharewa",
                        fontSize = 10.5.sp,
                        color = KasirColors.TextSubtle
                    )
                }
            }
        }
    }
}
}

@Composable
private fun RatingRowCompact(
    label: String,
    score: String,
    percentage: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(KasirColors.SurfaceLight)
            .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = KasirColors.TextPrimary)
            Text("Kepuasan $percentage", fontSize = 10.5.sp, color = KasirColors.TextMuted)
        }
        Text(score, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun RatingStatBadge(
    label: String,
    score: String,
    percentage: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(KasirColors.SurfaceLight)
            .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(label, fontSize = 11.sp, color = KasirColors.TextMuted, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(3.dp))
            Text(score, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
            Text("Kepuasan $percentage", fontSize = 10.5.sp, color = KasirColors.TextSecondary)
        }
    }
}

@Composable
private fun CustomerFeedbackItem(
    meja: String,
    time: String,
    comment: String,
    rating: String,
    aspects: List<String> = emptyList()
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(KasirColors.SurfaceLight)
            .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(6.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(KasirColors.PrimaryDarkNavy)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(meja, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(rating, fontSize = 11.5.sp, color = KasirColors.AccentRust, fontWeight = FontWeight.Bold)
                }
                Text(time, fontSize = 10.5.sp, color = KasirColors.TextSubtle)
            }
            Text(
                text = "\"$comment\"",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = KasirColors.TextPrimary,
                fontFamily = FontFamily.SansSerif
            )
            if (aspects.isNotEmpty()) {
                Text("Puas dengan: ${aspects.joinToString(", ")}", fontSize = 10.5.sp, color = KasirColors.TextMuted)
            }
        }
    }
}

@Composable
private fun CsContactCard(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(KasirColors.SurfaceLight)
            .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(6.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = KasirColors.PrimaryTeal, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text(title, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = KasirColors.TextPrimary)
            }
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KasirColors.PrimaryTeal)
            Text(subtitle, fontSize = 10.5.sp, color = KasirColors.TextMuted)
        }
    }
}

// ─── Ringkasan rating ────────────────────────────────────────────────────────

private val ASPECT_COLORS = listOf(KasirColors.PrimaryTeal, KasirColors.PrimaryDarkNavy, KasirColors.AccentRust, KasirColors.PrimaryTeal)

/** 4.6 → "4.6" */
private fun score(v: Double): String {
    val tenths = kotlin.math.round(v * 10).toInt()
    return "${tenths / 10}.${tenths % 10}"
}

@Composable
private fun RatingOverview(data: ReviewsDto, compact: Boolean) {
    val total = data.summary.totalReviews
    Text(
        "Dari $total rating pelanggan. Persentase = pelanggan yang menyebut aspek tersebut memuaskan; skor = rata-rata bintang mereka.",
        fontSize = 11.sp, color = KasirColors.TextMuted, lineHeight = 15.sp
    )
    Spacer(Modifier.height(10.dp))

    // Per aspek
    if (compact) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            data.aspects.forEachIndexed { i, a ->
                RatingRowCompact(a.name, if (a.count > 0) "${score(a.avgRating)} / 5.0" else "-", "${a.percent}% (${a.count})", ASPECT_COLORS[i % ASPECT_COLORS.size])
            }
        }
    } else {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            data.aspects.forEachIndexed { i, a ->
                RatingStatBadge(a.name, if (a.count > 0) "${score(a.avgRating)} / 5.0" else "-", "${a.percent}% (${a.count})", ASPECT_COLORS[i % ASPECT_COLORS.size], Modifier.weight(1f))
            }
        }
    }

    // Sebaran bintang
    Spacer(Modifier.height(12.dp))
    val maxCount = (data.distribution.maxOfOrNull { it.count } ?: 1).coerceAtLeast(1)
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        data.distribution.forEach { d ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${d.stars} ★", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = KasirColors.AccentRust, modifier = Modifier.width(34.dp))
                Box(Modifier.weight(1f).height(7.dp).clip(RoundedCornerShape(4.dp)).background(KasirColors.NeutralEmptyBg)) {
                    if (d.count > 0) {
                        Box(Modifier.fillMaxWidth(d.count.toFloat() / maxCount).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(KasirColors.AccentRust))
                    }
                }
                Text("${d.count}", fontSize = 11.5.sp, color = KasirColors.TextMuted, modifier = Modifier.width(36.dp).padding(start = 8.dp))
            }
        }
    }
}

/** "2026-11-26T01:20:00Z" → "5 mnt lalu" / "3 jam lalu" / "24 Nov, 13:05" (WITA) */
private fun timeAgo(iso: String?): String {
    val t = iso?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: return ""
    val minutes = (Clock.System.now() - t).inWholeMinutes
    return when {
        minutes < 1 -> "Baru saja"
        minutes < 60 -> "$minutes mnt lalu"
        minutes < 24 * 60 -> "${minutes / 60} jam lalu"
        else -> {
            val l = t.toLocalDateTime(TimeZone.of("Asia/Makassar"))
            val months = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")
            "${l.dayOfMonth} ${months[l.monthNumber - 1]}, ${l.hour.toString().padStart(2, '0')}:${l.minute.toString().padStart(2, '0')}"
        }
    }
}
