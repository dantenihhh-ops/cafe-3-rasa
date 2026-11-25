package com.cafe3rasa.kasir.data

import androidx.compose.runtime.Composable

/**
 * Menyimpan file (mis. laporan CSV) ke perangkat.
 * Android: dialog "Simpan ke..." (pilih folder Download, Drive, dll).
 * Desktop: dialog simpan file.
 * Fungsi yang dikembalikan menerima (namaFile, isi); [onResult] menerima pesan hasil.
 */
@Composable
expect fun rememberFileSaver(mimeType: String, onResult: (message: String) -> Unit): (fileName: String, content: ByteArray) -> Unit
