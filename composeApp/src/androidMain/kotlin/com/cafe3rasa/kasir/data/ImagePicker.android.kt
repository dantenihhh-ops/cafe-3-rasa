package com.cafe3rasa.kasir.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream

/**
 * Android: pilih dari galeri, lalu diperkecil (sisi terpanjang maks 1280 px)
 * dan dikompres ke JPEG agar di bawah 2 MB.
 */
@Composable
actual fun rememberImagePicker(onPicked: (bytes: ByteArray?, error: String?) -> Unit): () -> Unit {
    val context = LocalContext.current
    val callback = rememberUpdatedState(onPicked)

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult // dibatalkan
        val result = runCatching {
            val original = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                ?: error("Foto tidak bisa dibaca")
            val maxSide = 1280
            val scale = minOf(1f, maxSide.toFloat() / maxOf(original.width, original.height))
            val bitmap = if (scale < 1f) {
                Bitmap.createScaledBitmap(original, (original.width * scale).toInt(), (original.height * scale).toInt(), true)
            } else original

            var quality = 85
            var bytes: ByteArray
            do {
                bytes = ByteArrayOutputStream().use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                    out.toByteArray()
                }
                quality -= 15
            } while (bytes.size > MAX_IMAGE_BYTES && quality > 20)
            if (bytes.size > MAX_IMAGE_BYTES) error("Foto terlalu besar (maks 2 MB)")
            bytes
        }
        result.fold(
            onSuccess = { callback.value(it, null) },
            onFailure = { callback.value(null, it.message ?: "Gagal membaca foto") }
        )
    }
    return { launcher.launch("image/*") }
}
