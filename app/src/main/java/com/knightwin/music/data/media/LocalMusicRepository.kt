package com.knightwin.music.data.media

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import com.knightwin.music.data.model.Album
import com.knightwin.music.data.model.Artist
import com.knightwin.music.data.model.Song
import com.knightwin.music.data.model.SortOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

class LocalMusicRepository(private val context: Context) {

    private val onlineArtDir: File by lazy {
        File(context.filesDir, "album_art_online").apply { mkdirs() }
    }

    private val embeddedArtDir: File by lazy {
        File(context.filesDir, "album_art_embedded").apply { mkdirs() }
    }

    suspend fun scanSongs(minDurationMs: Long): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.RELATIVE_PATH,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.BUCKET_DISPLAY_NAME
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        val albumArtBaseUri = Uri.parse("content://media/external/audio/albumart")

        runCatching {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val durationCol = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val trackCol = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)
                val yearCol = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)
                val dateAddedCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
                val sizeCol = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
                val mimeCol = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
                val relPathCol = cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)
                val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val bucketCol = cursor.getColumnIndex(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    if (idCol < 0) continue
                    val id = cursor.getLong(idCol)
                    val duration = if (durationCol >= 0) cursor.getLong(durationCol) else 0L
                    if (duration <= 0L || duration < minDurationMs) continue

                    val title = if (titleCol >= 0) cursor.getString(titleCol).orEmpty().ifBlank { "Unknown Title" } else "Unknown Title"
                    val rawArtist = if (artistCol >= 0) cursor.getString(artistCol).orEmpty() else ""
                    val artist = if (rawArtist.isBlank() || rawArtist == "<unknown>") "Unknown Artist" else rawArtist
                    val rawAlbum = if (albumCol >= 0) cursor.getString(albumCol).orEmpty() else ""
                    val album = if (rawAlbum.isBlank() || rawAlbum == "<unknown>") "Unknown Album" else rawAlbum
                    val albumId = if (albumIdCol >= 0) cursor.getLong(albumIdCol) else 0L
                    val track = if (trackCol >= 0) cursor.getInt(trackCol) else 0
                    val year = if (yearCol >= 0) cursor.getInt(yearCol) else 0
                    val dateAdded = if (dateAddedCol >= 0) cursor.getLong(dateAddedCol) else 0L
                    val size = if (sizeCol >= 0) cursor.getLong(sizeCol) else 0L
                    val mime = if (mimeCol >= 0) cursor.getString(mimeCol).orEmpty().ifBlank { "audio/*" } else "audio/*"
                    val relPath = if (relPathCol >= 0) cursor.getString(relPathCol).orEmpty() else ""
                    val dataPath = if (dataCol >= 0) cursor.getString(dataCol).orEmpty() else ""
                    val bucket = if (bucketCol >= 0) cursor.getString(bucketCol).orEmpty() else ""

                    val folder = when {
                        bucket.isNotBlank() -> bucket
                        relPath.isNotBlank() -> relPath.trimEnd('/').substringAfterLast('/', relPath)
                        dataPath.isNotBlank() -> File(dataPath).parentFile?.name.orEmpty().ifBlank { "Music" }
                        else -> "Music"
                    }

                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val artUri = ContentUris.withAppendedId(albumArtBaseUri, albumId)

                    songs.add(
                        Song(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            albumId = albumId,
                            durationMs = duration,
                            trackNumber = track % 1000,
                            year = year,
                            dateAddedSec = dateAdded,
                            sizeBytes = size,
                            mimeType = mime,
                            contentUri = contentUri,
                            albumArtUri = artUri,
                            folderName = folder,
                            filePath = dataPath
                        )
                    )
                }
            }
        }
        songs
    }

    fun sortSongs(songs: List<Song>, order: SortOrder): List<Song> {
        return when (order) {
            SortOrder.TITLE -> songs.sortedBy { it.title.lowercase() }
            SortOrder.ARTIST -> songs.sortedWith(compareBy<Song> { it.artist.lowercase() }.thenBy { it.album.lowercase() }.thenBy { it.trackNumber })
            SortOrder.ALBUM -> songs.sortedWith(compareBy<Song> { it.album.lowercase() }.thenBy { it.trackNumber }.thenBy { it.title.lowercase() })
            SortOrder.DATE_ADDED -> songs.sortedByDescending { it.dateAddedSec }
            SortOrder.DURATION -> songs.sortedByDescending { it.durationMs }
        }
    }

    fun buildAlbums(songs: List<Song>): List<Album> {
        return songs.groupBy { it.albumId to it.album }
            .values
            .mapNotNull { group ->
                val first = group.firstOrNull() ?: return@mapNotNull null
                Album(
                    id = first.albumId,
                    name = first.album,
                    artist = group.groupBy { it.artist }.maxByOrNull { it.value.size }?.key ?: first.artist,
                    year = group.maxOfOrNull { it.year } ?: 0,
                    songCount = group.size,
                    artUri = first.albumArtUri,
                    sampleSongUri = first.contentUri,
                    totalDurationMs = group.sumOf { it.durationMs }
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    fun buildArtists(songs: List<Song>): List<Artist> {
        return songs.groupBy { it.artist.trim().lowercase() }
            .values
            .mapNotNull { group ->
                val first = group.firstOrNull() ?: return@mapNotNull null
                val albums = group.distinctBy { it.albumId }.size
                Artist(
                    id = first.artist.trim().lowercase(),
                    name = first.artist,
                    albumCount = albums,
                    songCount = group.size,
                    artUri = first.albumArtUri,
                    sampleSongUri = first.contentUri,
                    sampleAlbumName = first.album
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    fun albumHashKey(artistName: String, albumName: String): String {
        val raw = "${artistName.trim().lowercase()}|${albumName.trim().lowercase()}"
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        return digest.take(12).joinToString("") { "%02x".format(it) }
    }

    fun getOnlineArtFile(artistName: String, albumName: String): File {
        return File(onlineArtDir, "${albumHashKey(artistName, albumName)}.jpg")
    }

    fun getEmbeddedArtFile(albumId: Long): File {
        return File(embeddedArtDir, "emb_${albumId}.jpg")
    }

    fun hasLocalArtAvailable(album: Album): Boolean {
        val embFile = getEmbeddedArtFile(album.id)
        if (embFile.exists() && embFile.length() > 0L) return true

        val mediaStoreExists = runCatching {
            context.contentResolver.openFileDescriptor(album.artUri, "r")?.use { true } ?: false
        }.getOrDefault(false)
        if (mediaStoreExists) return true

        return runCatching {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, album.sampleSongUri)
                retriever.embeddedPicture != null
            } finally {
                retriever.release()
            }
        }.getOrDefault(false)
    }

    fun getOnlineArtCacheSizeBytes(): Long {
        return onlineArtDir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    fun clearOnlineArtCache() {
        onlineArtDir.listFiles()?.forEach { it.delete() }
    }
}
