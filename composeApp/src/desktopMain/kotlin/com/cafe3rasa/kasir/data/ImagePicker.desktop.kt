package com.cafe3rasa.kasir.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

/** Desktop: pilih file gambar (jpg, png, webp) maksimal 2 MB */
@Composable
actual fun rememberImagePicker(onPicked: (bytes: ByteArray?, error: String?) -> Unit): () -> Unit {
    val callback = rememberUpdatedState(onPicked)
    return {
        val dialog = FileDialog(null as Frame?, "Pilih Foto Menu", FileDialog.LOAD).apply {
            setFilenameFilter { _, name -> name.lowercase().let { it.endsWith(".jpg") || it.endsWith(".jpeg") || it.endsWith(".png") || it.endsWith(".webp") } }
            isVisible = true
        }
        val name = dialog.file
        if (name != null) {
            val file = File(dialog.directory, name)
            when {
                file.length() > MAX_IMAGE_BYTES -> callback.value(null, "Ukuran foto maksimal 2 MB")
                else -> runCatching { file.readBytes() }.fold(
                    onSuccess = { callback.value(it, null) },
                    onFailure = { callback.value(null, it.message ?: "Gagal membaca file") }
                )
            }
        }
    }
}
