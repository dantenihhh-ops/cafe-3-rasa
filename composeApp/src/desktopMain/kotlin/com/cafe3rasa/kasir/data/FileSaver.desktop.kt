package com.cafe3rasa.kasir.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
actual fun rememberFileSaver(mimeType: String, onResult: (message: String) -> Unit): (fileName: String, content: ByteArray) -> Unit {
    val callback = rememberUpdatedState(onResult)
    return { fileName, content ->
        val dialog = FileDialog(null as Frame?, "Simpan Laporan", FileDialog.SAVE).apply {
            file = fileName
            isVisible = true
        }
        val name = dialog.file
        if (name != null) {
            runCatching { File(dialog.directory, name).writeBytes(content) }.fold(
                onSuccess = { callback.value("Laporan disimpan: $name") },
                onFailure = { callback.value("Gagal menyimpan: ${it.message}") }
            )
        }
    }
}
