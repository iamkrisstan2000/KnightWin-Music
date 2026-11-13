package com.knightwin.music.data.media

import android.content.Context
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.knightwin.music.data.model.Album
import com.knightwin.music.data.model.ArtRefreshState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class AlbumArtFetcher(
    private val context: Context,
    private val repo: LocalMusicRepository,
    private val artworkLoader: ArtworkLoader
) {
    private val _state = MutableStateFlow(ArtRefreshState())
    val state: StateFlow<ArtRefreshState> = _state.asStateFlow()

    private var activeJob: Job? = null

    fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val net = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(net) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun fetchAlbumArt(
        albums: List<Album>,
        overwriteExisting: Boolean,
        onFinished: suspend (fetchedCount: Int) -> Unit = {}
    ) {
        if (_state.value.isRunning) return

        val eligibleAlbums = albums.filter {
            it.name.isNotBlank() &&
                !it.name.equals("Unknown Album", ignoreCase = true) &&
                !it.name.equals("<unknown>", ignoreCase = true)
        }

        if (eligibleAlbums.isEmpty()) {
            _state.value = ArtRefreshState(
                isRunning = false,
                completed = true,
                statusMessage = "No tagged albums found in your local library."
            )
            return
        }

        if (!isNetworkAvailable()) {
            _state.value = ArtRefreshState(
                isRunning = false,
                completed = true,
                error = "No internet connection. KnightWin Music stays offline until you're connected and tap Refresh."
            )
            return
        }

        var fetchedCount = 0
        var skippedCount = 0

        coroutineScope {
            activeJob = coroutineContext[Job]
            _state.value = ArtRefreshState(
                isRunning = true,
                processed = 0,
                total = eligibleAlbums.size,
                fetched = 0,
                skipped = 0,
                statusMessage = "Checking album artwork..."
            )

            withContext(Dispatchers.IO) {
                eligibleAlbums.forEachIndexed { index, album ->
                    ensureActive()

                    _state.value = _state.value.copy(
                        processed = index,
                        currentAlbum = "${album.name} — ${album.artist}",
                        statusMessage = "Checking ${index + 1} of ${eligibleAlbums.size}..."
                    )

                    val onlineFile = repo.getOnlineArtFile(album.artist, album.name)
                    val shouldSkip = if (overwriteExisting) {
                        false
                    } else {
                        (onlineFile.exists() && onlineFile.length() > 0L) || repo.hasLocalArtAvailable(album)
                    }

                    if (shouldSkip) {
                        skippedCount++
                    } else {
                        val success = downloadCoverForAlbum(album.artist, album.name, onlineFile)
                        if (success) {
                            fetchedCount++
                            artworkLoader.notifyArtUpdated()
                        } else {
                            skippedCount++
                        }
                        delay(180L)
                    }

                    _state.value = _state.value.copy(
                        processed = index + 1,
                        fetched = fetchedCount,
                        skipped = skippedCount
                    )
                }
            }

            if (fetchedCount > 0) {
                artworkLoader.notifyArtUpdated()
            }
            onFinished(fetchedCount)
            _state.value = ArtRefreshState(
                isRunning = false,
                processed = eligibleAlbums.size,
                total = eligibleAlbums.size,
                fetched = fetchedCount,
                skipped = skippedCount,
                completed = true,
                statusMessage = if (fetchedCount > 0) {
                    "Downloaded $fetchedCount album ${if (fetchedCount == 1) "cover" else "covers"}. Returning to offline mode."
                } else {
                    "All ${eligibleAlbums.size} albums already have artwork or no new matches found."
                }
            )
        }
    }

    fun cancel() {
        activeJob?.cancel()
        activeJob = null
        _state.value = ArtRefreshState(
            isRunning = false,
            completed = true,
            statusMessage = "Album art fetch cancelled. App is offline."
        )
    }

    fun clearStatus() {
        if (!_state.value.isRunning) {
            _state.value = ArtRefreshState()
        }
    }

    private fun downloadCoverForAlbum(artist: String, album: String, targetFile: File): Boolean {
        val queries = buildList {
            val cleanArtist = if (artist.equals("Unknown Artist", ignoreCase = true)) "" else artist
            if (cleanArtist.isNotBlank()) add("$cleanArtist $album")
            add(album)
        }

        for (query in queries) {
            val artworkUrl = queryItunesArtworkUrl(query) ?: continue
            val bytes = downloadBytes(artworkUrl) ?: continue
            if (isValidImage(bytes)) {
                val tmp = File(targetFile.parentFile, "${targetFile.name}.tmp")
                tmp.writeBytes(bytes)
                if (tmp.renameTo(targetFile)) {
                    return true
                }
            }
        }
        return false
    }

    private fun queryItunesArtworkUrl(term: String): String? {
        return runCatching {
            val encoded = URLEncoder.encode(term.trim(), "UTF-8")
            val url = URL("https://itunes.apple.com/search?term=$encoded&entity=album&limit=1")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 6000
                readTimeout = 8000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "KnightWinMusic/1.0 (Android)")
            }
            try {
                if (conn.responseCode !in 200..299) return null
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(body)
                val results = json.optJSONArray("results") ?: return null
                if (results.length() == 0) return null
                val rawUrl = results.getJSONObject(0).optString("artworkUrl100", "")
                if (rawUrl.isBlank()) return null
                rawUrl.replace("100x100bb", "600x600bb")
            } finally {
                conn.disconnect()
            }
        }.getOrNull()
    }

    private fun downloadBytes(urlString: String): ByteArray? {
        if (!urlString.startsWith("https://", ignoreCase = true)) return null
        return runCatching {
            val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
                connectTimeout = 6000
                readTimeout = 8000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "KnightWinMusic/1.0 (Android)")
            }
            try {
                if (conn.responseCode !in 200..299) return null
                val bytes = conn.inputStream.readBytes()
                if (bytes.size in 512..4_500_000) bytes else null
            } finally {
                conn.disconnect()
            }
        }.getOrNull()
    }

    private fun isValidImage(bytes: ByteArray): Boolean {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
        return opts.outWidth > 0 && opts.outHeight > 0
    }
}
