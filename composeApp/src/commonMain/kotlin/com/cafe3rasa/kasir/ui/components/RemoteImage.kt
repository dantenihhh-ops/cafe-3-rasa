package com.cafe3rasa.kasir.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.cafe3rasa.kasir.data.ApiClient
import com.cafe3rasa.kasir.theme.KasirColors
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import io.ktor.http.isSuccess
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap

/** Cache sederhana di memori agar gambar tidak diunduh berulang kali */
private val imageCache = mutableMapOf<String, ImageBitmap>()

/** Ubah path relatif server (mis. "uploads/menu/a.jpg") menjadi URL lengkap */
fun serverAssetUrl(pathOrUrl: String): String =
    if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) pathOrUrl
    else ApiClient.serverUrl.trimEnd('/') + "/" + pathOrUrl.trimStart('/')

/**
 * Menampilkan gambar dari server backend (foto menu upload, QR meja).
 * Selama memuat / gagal, menampilkan kotak abu-abu berikon gambar.
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun RemoteImage(
    pathOrUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val url = remember(pathOrUrl) { serverAssetUrl(pathOrUrl) }
    var bitmap by remember(url) { mutableStateOf(imageCache[url]) }

    LaunchedEffect(url) {
        if (bitmap != null) return@LaunchedEffect
        bitmap = runCatching {
            val response = ApiClient.http.get(url)
            if (!response.status.isSuccess()) return@runCatching null
            response.readRawBytes().decodeToImageBitmap()
        }.getOrNull()?.also { imageCache[url] = it }
    }

    val current = bitmap
    if (current != null) {
        Image(current, contentDescription, modifier = modifier, contentScale = contentScale)
    } else {
        Box(modifier.background(KasirColors.NeutralEmptyBg), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Image, null, tint = KasirColors.TextSubtle, modifier = Modifier.size(18.dp))
        }
    }
}

/** Hapus cache gambar tertentu (dipanggil setelah foto diganti) */
fun invalidateRemoteImage(pathOrUrl: String) {
    imageCache.remove(serverAssetUrl(pathOrUrl))
}
