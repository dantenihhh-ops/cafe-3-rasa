package com.cafe3rasa.kasir.data

import androidx.compose.runtime.Composable

/**
 * Pemilih foto dari galeri (Android) / file (Desktop).
 * Mengembalikan fungsi untuk membuka pemilih. Hasil dikirim ke [onPicked]:
 * bytes gambar (JPEG/PNG/WEBP, maks 2 MB) atau pesan error.
 */
@Composable
expect fun rememberImagePicker(onPicked: (bytes: ByteArray?, error: String?) -> Unit): () -> Unit

/** Batas ukuran foto sesuai aturan hosting */
const val MAX_IMAGE_BYTES = 2 * 1024 * 1024
