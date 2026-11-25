package com.cafe3rasa.kasir.data

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.serializer

/** Error dari server atau jaringan, dengan pesan yang siap ditampilkan ke pengguna. */
class ApiException(
    override val message: String,
    val statusCode: Int = 0
) : Exception(message) {
    /** true jika gagal karena tidak bisa terhubung (bukan ditolak server) */
    val isNetworkError: Boolean get() = statusCode == 0
    val isUnauthorized: Boolean get() = statusCode == 401
}

/**
 * Klien REST API ke backend Cafe 3 Rasa (Express + Supabase).
 *
 * Semua response backend berbentuk:
 *   sukses -> { "success": true,  "data": ... }
 *   gagal  -> { "success": false, "message": "..." }
 */
object ApiClient {

    private const val KEY_SERVER_URL = "server_url"
    private const val KEY_TOKEN = "auth_token"

    val json = Json {
        ignoreUnknownKeys = true   // field tambahan dari server diabaikan
        explicitNulls = false
        coerceInputValues = true
        isLenient = true
    }

    val http = HttpClient {
        expectSuccess = false
        install(ContentNegotiation) { json(json) }
        install(HttpTimeout) {
            connectTimeoutMillis = 8_000
            requestTimeoutMillis = 25_000   // server Supabase di luar negeri, beri waktu cukup
            socketTimeoutMillis = 25_000
        }
    }

    // ─── Alamat server ────────────────────────────────────────────────────

    /** Alamat server tersimpan, mis. "http://192.168.43.120:5000" (kosong jika belum diatur) */
    var serverUrl: String
        get() = storageGet(KEY_SERVER_URL) ?: defaultServerUrl
        set(value) = storagePut(KEY_SERVER_URL, normalizeServerUrl(value).ifBlank { null })

    val isServerConfigured: Boolean get() = serverUrl.isNotBlank()

    /**
     * Rapikan input alamat server:
     *   "192.168.43.120"              -> "http://192.168.43.120:5000"   (laptop di jaringan lokal)
     *   "192.168.43.120:5000/api/"    -> "http://192.168.43.120:5000"
     *   "kelompok7.tkj3a.my.id"       -> "https://kelompok7.tkj3a.my.id" (hosting, tanpa port)
     */
    fun normalizeServerUrl(input: String): String {
        var url = input.trim().trimEnd('/')
        if (url.isBlank()) return ""
        if (url.endsWith("/api")) url = url.removeSuffix("/api").trimEnd('/')

        val hasScheme = url.startsWith("http://") || url.startsWith("https://")
        val hostPart = url.substringAfter("://").substringBefore('/')
        val hostOnly = hostPart.substringBefore(':')
        val isLocal = hostOnly == "localhost" ||
                hostOnly.all { it.isDigit() || it == '.' }      // alamat IP, mis. 10.173.179.55

        if (!hasScheme) url = if (isLocal) "http://$url" else "https://$url"
        // Port 5000 hanya ditambahkan untuk server lokal (laptop). Domain hosting memakai port standar.
        if (isLocal && !hostPart.contains(":")) url = "$url:5000"
        return url
    }

    // ─── Token login ──────────────────────────────────────────────────────

    var token: String?
        get() = storageGet(KEY_TOKEN)
        set(value) = storagePut(KEY_TOKEN, value)

    /** Dipanggil saat server menolak token (401), mis. untuk kembali ke layar login */
    var onUnauthorized: (() -> Unit)? = null

    // ─── Request helper ───────────────────────────────────────────────────

    fun url(path: String): String {
        val base = serverUrl.trimEnd('/')
        if (base.isBlank()) throw ApiException("Alamat server belum diatur. Ketuk \"Atur Server\" di layar login.")
        return "$base/api/${path.trimStart('/')}"
    }

    suspend inline fun <reified T> get(path: String): T =
        send(HttpMethod.Get, path) {}

    suspend inline fun <reified T, reified B : Any> post(path: String, body: B): T =
        send(HttpMethod.Post, path) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

    suspend inline fun <reified T> postEmpty(path: String): T =
        send(HttpMethod.Post, path) {}

    suspend inline fun <reified T, reified B : Any> patch(path: String, body: B): T =
        send(HttpMethod.Patch, path) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

    suspend inline fun <reified T> patchEmpty(path: String): T =
        send(HttpMethod.Patch, path) {}

    suspend inline fun <reified T> delete(path: String): T =
        send(HttpMethod.Delete, path) {}

    suspend inline fun <reified T> send(
        method: HttpMethod,
        path: String,
        crossinline block: HttpRequestBuilder.() -> Unit
    ): T {
        val target = url(path)
        val response: HttpResponse = try {
            http.request(target) {
                this.method = method
                token?.let { bearerAuth(it) }
                block()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: ApiException) {
            throw e
        } catch (e: Exception) {
            throw ApiException(networkErrorMessage(e))
        }
        val data = unwrap(response)
        return json.decodeFromJsonElement(serializer<T>(), data)
    }

    /** Ambil isi "data" dari response, atau lempar ApiException berisi pesan dari server */
    suspend fun unwrap(response: HttpResponse): JsonElement {
        val text = runCatching { response.bodyAsText() }.getOrDefault("")
        val obj: JsonObject? = runCatching { json.parseToJsonElement(text).jsonObject }.getOrNull()
        val success = obj?.get("success")?.jsonPrimitive?.booleanOrNull ?: response.status.isSuccess()

        if (!response.status.isSuccess() || !success) {
            val code = response.status.value
            val message = obj?.get("message")?.jsonPrimitive?.contentOrNull
                ?: "Server mengembalikan error ($code)"
            if (code == 401) onUnauthorized?.invoke()
            throw ApiException(message, code)
        }
        return obj?.get("data") ?: JsonNull
    }

    fun networkErrorMessage(e: Throwable): String {
        val raw = (e.message ?: e::class.simpleName ?: "").lowercase()
        // Server di hosting (domain) vs server di laptop (IP lokal) butuh saran yang berbeda
        val isLocal = Regex("""^https?://(\d{1,3}(\.\d{1,3}){3}|localhost)(:\d+)?""").containsMatchIn(serverUrl)
        val timeout = "timeout" in raw || "timed out" in raw
        return when {
            "cleartext" in raw ->
                "Android memblokir koneksi http. Pastikan AndroidManifest berisi usesCleartextTraffic=\"true\"."
            !isLocal && timeout ->
                "Server hosting lambat merespons ($serverUrl). Coba lagi beberapa detik; jika baru di-restart, tunggu sebentar."
            !isLocal ->
                "Tidak dapat terhubung ke server hosting ($serverUrl). Periksa koneksi internet HP, " +
                        "lalu buka $serverUrl/api/health di browser untuk memastikan server menyala."
            timeout ->
                "Server tidak merespons ($serverUrl). Pastikan backend menyala dan HP satu jaringan dengan laptop."
            else ->
                "Tidak dapat terhubung ke server ($serverUrl). Pastikan backend menyala (npm run dev), " +
                        "HP & laptop di jaringan yang sama, dan alamat IP benar."
        }
    }

    // ─── Health check (untuk tombol "Tes Koneksi") ────────────────────────

    /**
     * Cek apakah server & database bisa dihubungi.
     * @return pesan sukses; melempar [ApiException] jika gagal.
     */
    suspend fun checkHealth(serverInput: String): String {
        val base = normalizeServerUrl(serverInput)
        if (base.isBlank()) throw ApiException("Isi alamat server terlebih dahulu")
        val response = try {
            http.request("$base/api/health") { method = HttpMethod.Get }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw ApiException(
                "Tidak dapat terhubung ke $base. Pastikan backend menyala dan HP satu jaringan dengan laptop."
            )
        }
        val obj = runCatching { json.parseToJsonElement(response.bodyAsText()).jsonObject }.getOrNull()
        val database = obj?.get("database")?.jsonPrimitive?.contentOrNull
        return when {
            obj == null -> throw ApiException("$base bukan server Cafe 3 Rasa (respons tidak dikenali)")
            database == "connected" -> "Terhubung ke server & database"
            else -> throw ApiException("Server menyala, tetapi database tidak terhubung. Cek .env di laptop.")
        }
    }
}