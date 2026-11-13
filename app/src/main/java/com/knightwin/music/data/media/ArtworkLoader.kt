package com.knightwin.music.data.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap

data class ArtworkResult(
    val bitmap: Bitmap,
    val dominantColorArgb: Int
)

class ArtworkLoader(
    private val context: Context,
    private val repo: LocalMusicRepository
) {
    private val maxKb = 18 * 1024
    private val memoryCache = object : LruCache<String, ArtworkResult>(maxKb) {
        override fun sizeOf(key: String, value: ArtworkResult): Int {
            return (value.bitmap.allocationByteCount / 1024).coerceAtLeast(1)
        }
    }

    private val missingKeys = ConcurrentHashMap<String, Boolean>()

    private val _cacheEpoch = MutableStateFlow(0)
    val cacheEpoch: StateFlow<Int> = _cacheEpoch.asStateFlow()

    fun notifyArtUpdated() {
        missingKeys.clear()
        memoryCache.evictAll()
        _cacheEpoch.value = _cacheEpoch.value + 1
    }

    fun invalidateMemoryOnly() {
        missingKeys.clear()
        memoryCache.evictAll()
        _cacheEpoch.value = _cacheEpoch.value + 1
    }

    suspend fun loadArtwork(
        albumId: Long,
        artistName: String,
        albumName: String,
        artUri: Uri,
        sampleSongUri: Uri,
        preferEmbedded: Boolean,
        targetSizePx: Int = 420
    ): ArtworkResult? {
        val bucketSize = if (targetSizePx <= 200) 200 else 512
        val cacheKey = "${albumId}_${repo.albumHashKey(artistName, albumName)}_${preferEmbedded}_$bucketSize"

        memoryCache.get(cacheKey)?.let { return it }
        if (missingKeys.containsKey(cacheKey)) return null

        val loaded = withContext(Dispatchers.IO) {
            val bitmap = if (preferEmbedded) {
                loadLocalEmbedded(albumId, artUri, sampleSongUri, bucketSize)
                    ?: loadOnlineCached(artistName, albumName, bucketSize)
            } else {
                loadOnlineCached(artistName, albumName, bucketSize)
                    ?: loadLocalEmbedded(albumId, artUri, sampleSongUri, bucketSize)
            }
            bitmap?.let {
                ArtworkResult(
                    bitmap = it,
                    dominantColorArgb = computeDominantColor(it)
                )
            }
        }

        if (loaded != null) {
            memoryCache.put(cacheKey, loaded)
        } else {
            missingKeys[cacheKey] = true
        }
        return loaded
    }

    private fun loadOnlineCached(artistName: String, albumName: String, maxPx: Int): Bitmap? {
        val file = repo.getOnlineArtFile(artistName, albumName)
        if (!file.exists() || file.length() <= 0L) return null
        return decodeFileSampled(file, maxPx)
    }

    private fun loadLocalEmbedded(
        albumId: Long,
        artUri: Uri,
        sampleSongUri: Uri,
        maxPx: Int
    ): Bitmap? {
        val embFile = repo.getEmbeddedArtFile(albumId)
        if (embFile.exists() && embFile.length() > 0L) {
            decodeFileSampled(embFile, maxPx)?.let { return it }
        }

        val mediaBitmap = runCatching {
            context.contentResolver.openInputStream(artUri)?.use { stream ->
                val bytes = stream.readBytes()
                if (bytes.isNotEmpty()) decodeBytesSampled(bytes, maxPx) else null
            }
        }.getOrNull()
        if (mediaBitmap != null) {
            saveThumbnailQuietly(embFile, mediaBitmap)
            return mediaBitmap
        }

        val embeddedBitmap = runCatching {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, sampleSongUri)
                val bytes = retriever.embeddedPicture
                if (bytes != null && bytes.isNotEmpty()) decodeBytesSampled(bytes, maxPx) else null
            } finally {
                retriever.release()
            }
        }.getOrNull()
        if (embeddedBitmap != null) {
            saveThumbnailQuietly(embFile, embeddedBitmap)
            return embeddedBitmap
        }

        return null
    }

    private fun saveThumbnailQuietly(target: File, bitmap: Bitmap) {
        runCatching {
            val tmp = File(target.parentFile, "${target.name}.tmp")
            FileOutputStream(tmp).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 88, out)
            }
            tmp.renameTo(target)
        }
    }

    private fun decodeFileSampled(file: File, maxPx: Int): Bitmap? {
        return runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
            val opts = BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxPx)
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            BitmapFactory.decodeFile(file.absolutePath, opts)
        }.getOrNull()
    }

    private fun decodeBytesSampled(bytes: ByteArray, maxPx: Int): Bitmap? {
        return runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
            val opts = BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxPx)
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
        }.getOrNull()
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxPx: Int): Int {
        var inSampleSize = 1
        while ((width / inSampleSize) > maxPx * 1.5f || (height / inSampleSize) > maxPx * 1.5f) {
            inSampleSize *= 2
        }
        return inSampleSize
    }

    private fun computeDominantColor(bitmap: Bitmap): Int {
        return runCatching {
            val tiny = Bitmap.createScaledBitmap(bitmap, 8, 8, true)
            var rSum = 0L
            var gSum = 0L
            var bSum = 0L
            var weightSum = 0L
            for (y in 0 until 8) {
                for (x in 0 until 8) {
                    val px = tiny.getPixel(x, y)
                    val r = (px shr 16) and 0xFF
                    val g = (px shr 8) and 0xFF
                    val b = px and 0xFF
                    val maxC = maxOf(r, g, b)
                    val minC = minOf(r, g, b)
                    val satWeight = (maxC - minC) + 16
                    rSum += r * satWeight
                    gSum += g * satWeight
                    bSum += b * satWeight
                    weightSum += satWeight
                }
            }
            if (tiny !== bitmap) tiny.recycle()
            if (weightSum <= 0L) return 0xFF27B1FE.toInt()
            val r = (rSum / weightSum).toInt().coerceIn(0, 255)
            val g = (gSum / weightSum).toInt().coerceIn(0, 255)
            val b = (bSum / weightSum).toInt().coerceIn(0, 255)
            (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        }.getOrDefault(0xFF27B1FE.toInt())
    }
}
