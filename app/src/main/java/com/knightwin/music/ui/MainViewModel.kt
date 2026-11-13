package com.knightwin.music.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.knightwin.music.KnightWinApp
import com.knightwin.music.data.model.AccentTheme
import com.knightwin.music.data.model.Album
import com.knightwin.music.data.model.AlbumLayout
import com.knightwin.music.data.model.AppSettings
import com.knightwin.music.data.model.ArtRefreshState
import com.knightwin.music.data.model.Artist
import com.knightwin.music.data.model.EqualizerPreset
import com.knightwin.music.data.model.GreeterCatalog
import com.knightwin.music.data.model.GreeterMode
import com.knightwin.music.data.model.MinDurationFilter
import com.knightwin.music.data.model.NowPlayingStyle
import com.knightwin.music.data.model.PlaybackUiState
import com.knightwin.music.data.model.Playlist
import com.knightwin.music.data.model.ReverbPreset
import com.knightwin.music.data.model.Song
import com.knightwin.music.data.model.SortOrder
import com.knightwin.music.data.model.ThemeMode
import com.knightwin.music.playback.PlaybackManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as KnightWinApp
    val settingsRepo = app.settingsRepository
    private val musicRepo = app.localMusicRepository
    val artworkLoader = app.artworkLoader
    private val artFetcher = app.albumArtFetcher

    val playbackManager = PlaybackManager(application, viewModelScope, settingsRepo)

    val settings: StateFlow<AppSettings> = settingsRepo.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppSettings()
    )

    val favoriteIds: StateFlow<Set<Long>> = settingsRepo.favoriteIdsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptySet()
    )

    val playlists: StateFlow<List<Playlist>> = settingsRepo.playlistsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val recentlyPlayedIds: StateFlow<List<Long>> = settingsRepo.recentlyPlayedFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val playCounts: StateFlow<Map<Long, Int>> = settingsRepo.playCountsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyMap()
    )

    val recentSearches: StateFlow<List<String>> = settingsRepo.recentSearchesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val playbackState: StateFlow<PlaybackUiState> = playbackManager.state
    val artRefreshState: StateFlow<ArtRefreshState> = artFetcher.state
    val artCacheEpoch: StateFlow<Int> = artworkLoader.cacheEpoch

    private val _rawSongs = MutableStateFlow<List<Song>>(emptyList())
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _hasAudioPermission = MutableStateFlow(false)
    val hasAudioPermission: StateFlow<Boolean> = _hasAudioPermission.asStateFlow()

    private val _libraryScannedOnce = MutableStateFlow(false)
    val libraryScannedOnce: StateFlow<Boolean> = _libraryScannedOnce.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _artCacheSizeBytes = MutableStateFlow(0L)
    val artCacheSizeBytes: StateFlow<Long> = _artCacheSizeBytes.asStateFlow()

    private val _randomizedGreeter = MutableStateFlow(GreeterCatalog.titles.random())
    val randomizedGreeter: StateFlow<String> = _randomizedGreeter.asStateFlow()

    private val _suggestionSeed = MutableStateFlow(System.currentTimeMillis())

    val songs: StateFlow<List<Song>> = combine(_rawSongs, settings) { list, s ->
        musicRepo.sortSongs(list, s.sortOrder)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val recentlyAddedSongs: StateFlow<List<Song>> = _rawSongs.combine(settings) { list, _ ->
        list.sortedByDescending { it.dateAddedSec }.take(14)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val suggestedSongs: StateFlow<List<Song>> = combine(_rawSongs, _suggestionSeed) { list, seed ->
        if (list.isEmpty()) emptyList() else list.shuffled(Random(seed)).take(8)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val albums: StateFlow<List<Album>> = combine(_rawSongs, settings) { list, _ ->
        musicRepo.buildAlbums(list)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val artists: StateFlow<List<Artist>> = combine(_rawSongs, settings) { list, _ ->
        musicRepo.buildArtists(list)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        playbackManager.connect()
        refreshCacheSize()

        viewModelScope.launch {
            settings.collect { s ->
                playbackManager.setCrossfadeMs(s.crossfadeMs)
            }
        }
    }

    fun onAudioPermissionResult(granted: Boolean) {
        _hasAudioPermission.value = granted
        if (granted && !_libraryScannedOnce.value) {
            rescanLibrary()
        }
    }

    fun rescanLibrary() {
        if (!_hasAudioPermission.value) return
        viewModelScope.launch {
            _isScanning.value = true
            val minDuration = settings.value.minDurationFilter.durationMs
            val scanned = musicRepo.scanSongs(minDuration)
            _rawSongs.value = scanned
            playbackManager.updateLibraryLookup(scanned)
            _libraryScannedOnce.value = true
            _isScanning.value = false
            refreshCacheSize()
        }
    }

    fun refreshCacheSize() {
        viewModelScope.launch(Dispatchers.IO) {
            _artCacheSizeBytes.value = musicRepo.getOnlineArtCacheSizeBytes()
        }
    }

    fun acceptFirstRunArtFetch() {
        viewModelScope.launch {
            settingsRepo.setFirstRunArtPromptShown(true)
            fetchAlbumArt(overwriteExisting = false)
        }
    }

    fun declineFirstRunArtFetch() {
        viewModelScope.launch {
            settingsRepo.setFirstRunArtPromptShown(true)
        }
    }

    fun fetchAlbumArt(overwriteExisting: Boolean) {
        viewModelScope.launch {
            artFetcher.fetchAlbumArt(
                albums = albums.value,
                overwriteExisting = overwriteExisting,
                onFinished = {
                    settingsRepo.setLastArtSyncEpochMs(System.currentTimeMillis())
                    refreshCacheSize()
                }
            )
        }
    }

    fun cancelArtFetch() {
        artFetcher.cancel()
    }

    fun clearArtFetchStatus() {
        artFetcher.clearStatus()
    }

    fun clearDownloadedArtCache() {
        viewModelScope.launch(Dispatchers.IO) {
            musicRepo.clearOnlineArtCache()
            artworkLoader.notifyArtUpdated()
            refreshCacheSize()
        }
    }

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { settingsRepo.setThemeMode(mode) }
    fun setOledBlack(enabled: Boolean) = viewModelScope.launch { settingsRepo.setOledBlack(enabled) }
    fun setAccentTheme(theme: AccentTheme) = viewModelScope.launch { settingsRepo.setAccentTheme(theme) }
    fun setNowPlayingStyle(style: NowPlayingStyle) = viewModelScope.launch { settingsRepo.setNowPlayingStyle(style) }
    fun setAlbumLayout(layout: AlbumLayout) = viewModelScope.launch { settingsRepo.setAlbumLayout(layout) }
    fun setArtistLayout(layout: AlbumLayout) = viewModelScope.launch { settingsRepo.setArtistLayout(layout) }
    fun setLiquidGlassAccents(enabled: Boolean) = viewModelScope.launch { settingsRepo.setLiquidGlassAccents(enabled) }
    fun setGreeterMode(mode: GreeterMode) = viewModelScope.launch { settingsRepo.setGreeterMode(mode) }
    fun setCustomGreeterText(text: String) = viewModelScope.launch { settingsRepo.setCustomGreeterText(text) }
    fun rerollGreeter() {
        val current = _randomizedGreeter.value
        val candidates = GreeterCatalog.titles.filter { it != current }
        _randomizedGreeter.value = (if (candidates.isNotEmpty()) candidates else GreeterCatalog.titles).random()
    }
    fun refreshSuggestions() {
        _suggestionSeed.value = _suggestionSeed.value + 1L + Random.nextLong(1, 10_000)
    }
    fun setSortOrder(order: SortOrder) = viewModelScope.launch { settingsRepo.setSortOrder(order) }
    fun setMinDurationFilter(filter: MinDurationFilter) {
        viewModelScope.launch {
            settingsRepo.setMinDurationFilter(filter)
            rescanLibrary()
        }
    }
    fun setCrossfadeMs(ms: Int) = viewModelScope.launch { settingsRepo.setCrossfadeMs(ms) }
    fun setPlaybackSpeed(speed: Float) = viewModelScope.launch { settingsRepo.setPlaybackSpeed(speed) }
    fun setReverbPreset(preset: ReverbPreset) = viewModelScope.launch { settingsRepo.setReverbPreset(preset) }
    fun setEqualizerPreset(preset: EqualizerPreset) = viewModelScope.launch { settingsRepo.setEqualizerPreset(preset) }
    fun setEqualizerCustomBands(bands: List<Float>) = viewModelScope.launch { settingsRepo.setEqualizerCustomBands(bands) }
    fun setCustomHeaderLogoFromUri(uri: Uri) {
        viewModelScope.launch {
            val savedPath = withContext(Dispatchers.IO) {
                runCatching {
                    val source = ImageDecoder.createSource(app.contentResolver, uri)
                    val decoded = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        decoder.isMutableRequired = false
                        val w = info.size.width.coerceAtLeast(1)
                        val h = info.size.height.coerceAtLeast(1)
                        val maxSide = maxOf(w, h)
                        if (maxSide > 1024) {
                            val scale = 1024f / maxSide.toFloat()
                            decoder.setTargetSize(
                                (w * scale).toInt().coerceAtLeast(1),
                                (h * scale).toInt().coerceAtLeast(1)
                            )
                        }
                    }
                    val side = minOf(decoded.width, decoded.height).coerceAtLeast(1)
                    val left = ((decoded.width - side) / 2).coerceAtLeast(0)
                    val top = ((decoded.height - side) / 2).coerceAtLeast(0)
                    val cropped = if (decoded.width != side || decoded.height != side) {
                        Bitmap.createBitmap(decoded, left, top, side, side)
                    } else {
                        decoded
                    }
                    val finalBmp = if (side > 512) {
                        Bitmap.createScaledBitmap(cropped, 512, 512, true)
                    } else {
                        cropped
                    }
                    app.filesDir.listFiles { file ->
                        file.name.startsWith("custom_header_logo_") && file.name.endsWith(".png")
                    }?.forEach { it.delete() }

                    val outFile = File(app.filesDir, "custom_header_logo_${System.currentTimeMillis()}.png")
                    FileOutputStream(outFile).use { fos ->
                        finalBmp.compress(Bitmap.CompressFormat.PNG, 95, fos)
                    }
                    if (finalBmp !== cropped) finalBmp.recycle()
                    if (cropped !== decoded) cropped.recycle()
                    decoded.recycle()
                    outFile.absolutePath
                }.getOrNull()
            }
            if (!savedPath.isNullOrBlank()) {
                settingsRepo.setCustomLogoPath(savedPath)
            }
        }
    }

    fun clearCustomHeaderLogo() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                app.filesDir.listFiles { file ->
                    file.name.startsWith("custom_header_logo_") && file.name.endsWith(".png")
                }?.forEach { it.delete() }
            }
            settingsRepo.setCustomLogoPath("")
        }
    }
    fun setKeepScreenOn(enabled: Boolean) = viewModelScope.launch { settingsRepo.setKeepScreenOn(enabled) }
    fun setPreferEmbeddedArt(prefer: Boolean) {
        viewModelScope.launch {
            settingsRepo.setPreferEmbeddedArt(prefer)
            artworkLoader.invalidateMemoryOnly()
        }
    }
    fun dismissBanner() = viewModelScope.launch { settingsRepo.setBannerDismissed(true) }

    fun toggleFavorite(songId: Long) = viewModelScope.launch { settingsRepo.toggleFavorite(songId) }

    fun createPlaylist(name: String, initialSongIds: List<Long> = emptyList()) {
        viewModelScope.launch { settingsRepo.createPlaylist(name, initialSongIds) }
    }

    fun renamePlaylist(playlistId: String, newName: String) {
        viewModelScope.launch { settingsRepo.renamePlaylist(playlistId, newName) }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch { settingsRepo.deletePlaylist(playlistId) }
    }

    fun addSongToPlaylist(playlistId: String, songId: Long) {
        viewModelScope.launch { settingsRepo.addSongToPlaylist(playlistId, songId) }
    }

    fun removeSongFromPlaylist(playlistId: String, songId: Long) {
        viewModelScope.launch { settingsRepo.removeSongFromPlaylist(playlistId, songId) }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun commitSearchHistory(query: String) {
        viewModelScope.launch { settingsRepo.addRecentSearch(query) }
    }

    fun clearRecentSearches() {
        viewModelScope.launch { settingsRepo.clearRecentSearches() }
    }

    override fun onCleared() {
        playbackManager.release()
        super.onCleared()
    }
}
