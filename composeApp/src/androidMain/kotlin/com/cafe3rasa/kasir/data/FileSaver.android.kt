package com.cafe3rasa.kasir.data

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberFileSaver(mimeType: String, onResult: (message: String) -> Unit): (fileName: String, content: ByteArray) -> Unit {
    val context = LocalContext.current
    val callback = rememberUpdatedState(onResult)
    val pending = remember { mutableStateOf<ByteArray?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(mimeType)) { uri ->
        val bytes = pending.value
        pending.value = null
        if (uri == null || bytes == null) return@rememberLauncherForActivityResult // dibatalkan
        runCatching {
            context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) } ?: error("Tidak bisa menulis file")
        }.fold(
            onSuccess = { callback.value("Laporan berhasil disimpan") },
            onFailure = { callback.value("Gagal menyimpan: ${it.message}") }
        )
    }
    return { fileName, content ->
        pending.value = content
        launcher.launch(fileName)
    }
}
