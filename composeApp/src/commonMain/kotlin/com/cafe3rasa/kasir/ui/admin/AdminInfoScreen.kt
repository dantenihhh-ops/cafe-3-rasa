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
import com.cafe3rasa.kasir.theme.KasirColors

/**
 * Halaman Informasi & Bantuan Admin:
 * Layout responsif menyesuaikan ukuran layar HP & Tablet tanpa whitespace berlebih.
 */
@Composable
fun AdminInfoScreen(
    onBack: () -> Unit,
    compact: Boolean = false
) {
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
                        text = "Informasi & Bantuan",
                        fontWeight = FontWeight.Bold,
                        fontSize = if (compact) 15.sp else 17.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = KasirColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Rating, saran pelanggan, CS, dan info pengembang",
                        fontSize = 11.5.sp,
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
            // ── 1. Rating & Kepuasan Pelanggan ──
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
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = KasirColors.AccentRust,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Rating Pelanggan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = KasirColors.TextPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(KasirColors.MintSoft)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "SKOR 4.8 / 5.0",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = KasirColors.PrimaryTeal
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Breakdown Rating Responsif
                if (compact) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        RatingRowCompact("Rasa Makanan", "4.9 / 5.0", "98%", KasirColors.PrimaryTeal)
                        RatingRowCompact("Kecepatan Layanan", "4.7 / 5.0", "94%", KasirColors.PrimaryDarkNavy)
                        RatingRowCompact("Kemudahan E-Menu", "4.9 / 5.0", "99%", KasirColors.AccentRust)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RatingStatBadge("Rasa Makanan", "4.9 / 5.0", "98%", KasirColors.PrimaryTeal, Modifier.weight(1f))
                        RatingStatBadge("Kecepatan Layanan", "4.7 / 5.0", "94%", KasirColors.PrimaryDarkNavy, Modifier.weight(1f))
                        RatingStatBadge("Kemudahan E-Menu", "4.9 / 5.0", "99%", KasirColors.AccentRust, Modifier.weight(1f))
                    }
                }
            }

            // ── 2. Komentar & Saran Customer ──
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
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = KasirColors.PrimaryTeal,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Komentar & Saran Pelanggan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = KasirColors.TextPrimary
                        )
                    }
                    Text(
                        text = "4 Ulasan",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = KasirColors.TextMuted
                    )
                }

                Spacer(Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CustomerFeedbackItem(
                        meja = "Meja 04",
                        time = "10 mnt lalu",
                        comment = "Tombol minta bantuan kasir sangat responsif dan membantu! Kasir ramah sekali saat mengantar pesanan.",
                        rating = "★★★★★"
                    )
                    CustomerFeedbackItem(
                        meja = "Meja 02",
                        time = "35 mnt lalu",
                        comment = "Nasi Campur 3 Rasa bumbu Madura-nya juara! Harap pertahankan porsi dan racikan sambal matahnya.",
                        rating = "★★★★★"
                    )
                    CustomerFeedbackItem(
                        meja = "Meja 07",
                        time = "1 jam lalu",
                        comment = "Tampilan katalog di HP enteng dan responsif. Filter Best Seller sangat memudahkan memilih menu favorit.",
                        rating = "★★★★★"
                    )
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
                        text = "Sistem kasir pintar dan pemesanan digital E-Menu pelanggan berbasis QR Meja untuk Café 3 Rasa.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = KasirColors.TextMuted
                    )
                }
            }

            // ── 5. Nama Pencipta & Tim Pengembang (Menyesuaikan Ramping) ──
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
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = KasirColors.AccentRust,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Nama Pencipta & Pengembang",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = KasirColors.TextPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(KasirColors.PeachSoft)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PBL NO. 6",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            color = KasirColors.AccentRust
                        )
                    }
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
                    if (compact) {
                        // Di HP: Susun vertikal agar tidak sempit
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(KasirColors.Surface)
                                .border(1.dp, KasirColors.BorderLight, RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("Pencipta / Lead Developer", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KasirColors.TextMuted)
                                Text("Inaravedev & Maharewa", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = KasirColors.PrimaryTeal)
                                Text("Full-Stack Mobile & Web Engineer", fontSize = 11.5.sp, color = KasirColors.TextMuted)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
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
                                    Text("Inaravedev & Maharewa", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = KasirColors.PrimaryTeal)
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

                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Hak Cipta © 2026 Tiga Rasa by Maharewa & Inaravedev",
                        fontSize = 11.sp,
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
    rating: String
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
