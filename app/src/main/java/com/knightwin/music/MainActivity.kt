package com.knightwin.music

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knightwin.music.data.model.AlbumLayout
import com.knightwin.music.data.model.LibrarySubTab
import com.knightwin.music.data.model.MainTab
import com.knightwin.music.data.model.Song
import com.knightwin.music.data.model.SortOrder
import com.knightwin.music.data.model.ThemeMode
import com.knightwin.music.ui.MainViewModel
import com.knightwin.music.ui.components.AddToPlaylistDialog
import com.knightwin.music.ui.components.FloatingMiniPlayer
import com.knightwin.music.ui.components.KnightBottomNavBar
import com.knightwin.music.ui.components.SongDetailsDialog
import com.knightwin.music.ui.components.SongOptionsBottomSheet
import com.knightwin.music.ui.screens.AudioPermissionScreen
import com.knightwin.music.ui.screens.DetailDestination
import com.knightwin.music.ui.screens.DetailScreenContent
import com.knightwin.music.ui.screens.FirstRunArtConsentDialog
import com.knightwin.music.ui.screens.HomeTabContent
import com.knightwin.music.ui.screens.LibraryTabContent
import com.knightwin.music.ui.screens.NowPlayingScreen
import com.knightwin.music.ui.screens.OneUiSettingsScreen
import com.knightwin.music.ui.screens.SearchTabContent
import com.knightwin.music.ui.theme.KnightWinTheme
import com.knightwin.music.ui.theme.LiquidAppBackdrop
import com.knightwin.music.ui.theme.LocalKnightExtras
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val isDark = when (settings.themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            LaunchedEffect(isDark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        AndroidColor.TRANSPARENT,
                        AndroidColor.TRANSPARENT
                    ) { isDark },
                    navigationBarStyle = SystemBarStyle.auto(
                        AndroidColor.TRANSPARENT,
                        AndroidColor.TRANSPARENT
                    ) { isDark }
                )
            }

            KnightWinTheme(settings = settings) {
                KnightWinRoot(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun KnightWinRoot(viewModel: MainViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val artists by viewModel.artists.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val recentlyPlayedIds by viewModel.recentlyPlayedIds.collectAsStateWithLifecycle()
    val playCounts by viewModel.playCounts.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val hasAudioPermission by viewModel.hasAudioPermission.collectAsStateWithLifecycle()
    val libraryScannedOnce by viewModel.libraryScannedOnce.collectAsStateWithLifecycle()
    val artRefreshState by viewModel.artRefreshState.collectAsStateWithLifecycle()
    val artCacheEpoch by viewModel.artCacheEpoch.collectAsStateWithLifecycle()
    val artCacheSizeBytes by viewModel.artCacheSizeBytes.collectAsStateWithLifecycle()
    val recentlyAddedSongs by viewModel.recentlyAddedSongs.collectAsStateWithLifecycle()
    val suggestedSongs by viewModel.suggestedSongs.collectAsStateWithLifecycle()
    val randomizedGreeter by viewModel.randomizedGreeter.collectAsStateWithLifecycle()

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    var deniedOnce by rememberSaveable { mutableStateOf(false) }

    val audioLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) deniedOnce = true
        viewModel.onAudioPermissionResult(granted)
        if (granted) {
            val hasNotif = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasNotif) {
                notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
                viewModel.onAudioPermissionResult(granted)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.onAudioPermissionResult(granted)
    }

    if (!hasAudioPermission) {
        AudioPermissionScreen(
            showSettingsFallback = deniedOnce,
            onRequestPermission = { audioLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO) },
            onOpenSystemSettings = {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null)
                )
                context.startActivity(intent)
            }
        )
        return
    }

    var currentTab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    var librarySubTab by rememberSaveable { mutableStateOf(LibrarySubTab.SONGS) }
    var detailDestination by remember { mutableStateOf<DetailDestination?>(null) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showNowPlaying by rememberSaveable { mutableStateOf(false) }

    var sheetSong by remember { mutableStateOf<Pair<Song, String?>?>(null) }
    var playlistDialogSong by remember { mutableStateOf<Song?>(null) }
    var detailsDialogSong by remember { mutableStateOf<Song?>(null) }

    BackHandler(enabled = showNowPlaying) { showNowPlaying = false }
    BackHandler(enabled = !showNowPlaying && showSettings) {
        if (!artRefreshState.isRunning) viewModel.clearArtFetchStatus()
        showSettings = false
    }
    BackHandler(enabled = !showNowPlaying && !showSettings && detailDestination != null) { detailDestination = null }
    BackHandler(enabled = !showNowPlaying && !showSettings && detailDestination == null && currentTab != MainTab.HOME) {
        currentTab = MainTab.HOME
    }

    fun showToast(msg: String) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(msg)
        }
    }

    val extras = LocalKnightExtras.current

    LiquidAppBackdrop(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (artRefreshState.isRunning && !showSettings) {
                        Surface(
                            color = if (extras.isOled) Color.Black else MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                Text(
                                    text = "Fetching album art (${artRefreshState.processed}/${artRefreshState.total})...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (extras.isOled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                LinearProgressIndicator(
                                    progress = {
                                        if (artRefreshState.total > 0) {
                                            (artRefreshState.processed.toFloat() / artRefreshState.total.toFloat()).coerceIn(0f, 1f)
                                        } else 0f
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    FloatingMiniPlayer(
                        playbackState = playbackState,
                        artworkLoader = viewModel.artworkLoader,
                        preferEmbeddedArt = settings.preferEmbeddedArt,
                        artCacheEpoch = artCacheEpoch,
                        nowPlayingStyle = settings.nowPlayingStyle,
                        playbackSpeed = settings.playbackSpeed,
                        onTogglePlayPause = { viewModel.playbackManager.togglePlayPause() },
                        onSkipNext = { viewModel.playbackManager.skipNext() },
                        onSkipPrevious = { viewModel.playbackManager.skipPrevious() },
                        onExpand = { showNowPlaying = true }
                    )

                    KnightBottomNavBar(
                        currentTab = currentTab,
                        onSelectTab = { tab ->
                            detailDestination = null
                            currentTab = tab
                        }
                    )
                }
            }
        ) { innerPadding ->
            val listBottomPadding = PaddingValues(bottom = 8.dp)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .clipToBounds()
            ) {
                val activeDetail = detailDestination
                if (activeDetail != null) {
                    DetailScreenContent(
                        destination = activeDetail,
                        allSongs = songs,
                        allAlbums = albums,
                        playlists = playlists,
                        favoriteIds = favoriteIds,
                        recentlyPlayedIds = recentlyPlayedIds,
                        playCounts = playCounts,
                        currentSongId = playbackState.currentSong?.id,
                        isPlaying = playbackState.isPlaying,
                        settings = settings,
                        artworkLoader = viewModel.artworkLoader,
                        artCacheEpoch = artCacheEpoch,
                        bottomPadding = listBottomPadding,
                        onBack = { detailDestination = null },
                        onPlaySongs = { list, idx, shuffle -> viewModel.playbackManager.playSongs(list, idx, shuffle) },
                        onMoreSongClick = { song, playlistId -> sheetSong = song to playlistId },
                        onAlbumClick = { album -> detailDestination = DetailDestination.AlbumDetail(album) }
                    )
                } else {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "tabSwitch"
                    ) { tab ->
                        when (tab) {
                            MainTab.HOME -> HomeTabContent(
                                songs = songs,
                                recentlyAddedSongs = recentlyAddedSongs,
                                albums = albums,
                                suggestedSongs = suggestedSongs,
                                isScanning = isScanning,
                                playbackState = playbackState,
                                settings = settings,
                                randomizedGreeter = randomizedGreeter,
                                artworkLoader = viewModel.artworkLoader,
                                artCacheEpoch = artCacheEpoch,
                                bottomPadding = listBottomPadding,
                                onPlaySongs = { list, idx, shuffle -> viewModel.playbackManager.playSongs(list, idx, shuffle) },
                                onResumeOrPlayAll = {
                                    if (playbackState.currentSong != null) {
                                        viewModel.playbackManager.togglePlayPause()
                                    } else if (songs.isNotEmpty()) {
                                        viewModel.playbackManager.playSongs(songs, 0, false)
                                    }
                                },
                                onAlbumClick = { album -> detailDestination = DetailDestination.AlbumDetail(album) },
                                onMoreSongClick = { song -> sheetSong = song to null },
                                onSeeAllRecent = {
                                    viewModel.setSortOrder(SortOrder.DATE_ADDED)
                                    librarySubTab = LibrarySubTab.SONGS
                                    currentTab = MainTab.LIBRARY
                                },
                                onSeeAllAlbums = {
                                    librarySubTab = LibrarySubTab.ALBUMS
                                    currentTab = MainTab.LIBRARY
                                },
                                onRerollGreeter = { viewModel.rerollGreeter() },
                                onRefreshSuggestions = { viewModel.refreshSuggestions() },
                                onSetGreeterMode = { viewModel.setGreeterMode(it) },
                                onSetCustomGreeterText = { viewModel.setCustomGreeterText(it) },
                                onSetCustomLogoUri = { viewModel.setCustomHeaderLogoFromUri(it); showToast("Header picture updated") },
                                onResetCustomLogo = { viewModel.clearCustomHeaderLogo(); showToast("Reset to KnightWin logo") },
                                onRescan = { viewModel.rescanLibrary(); showToast("Scanning local audio...") },
                                onOpenSettings = { showSettings = true }
                            )
                            MainTab.LIBRARY -> LibraryTabContent(
                                activeSubTab = librarySubTab,
                                onSelectSubTab = { librarySubTab = it },
                                songs = songs,
                                albums = albums,
                                artists = artists,
                                playlists = playlists,
                                favoriteCount = songs.count { favoriteIds.contains(it.id) },
                                recentCount = recentlyPlayedIds.count { id -> songs.any { it.id == id } },
                                mostPlayedCount = playCounts.count { (id, c) -> c > 0 && songs.any { it.id == id } },
                                isScanning = isScanning,
                                currentSongId = playbackState.currentSong?.id,
                                isPlaying = playbackState.isPlaying,
                                settings = settings,
                                artworkLoader = viewModel.artworkLoader,
                                artCacheEpoch = artCacheEpoch,
                                bottomPadding = listBottomPadding,
                                onPlaySongs = { list, idx, shuffle -> viewModel.playbackManager.playSongs(list, idx, shuffle) },
                                onChangeSortOrder = { viewModel.setSortOrder(it) },
                                onToggleAlbumLayout = {
                                    val next = if (settings.albumLayout == AlbumLayout.GRID) AlbumLayout.LIST else AlbumLayout.GRID
                                    viewModel.setAlbumLayout(next)
                                },
                                onToggleArtistLayout = {
                                    val next = if (settings.artistLayout == AlbumLayout.GRID) AlbumLayout.LIST else AlbumLayout.GRID
                                    viewModel.setArtistLayout(next)
                                },
                                onAlbumClick = { album -> detailDestination = DetailDestination.AlbumDetail(album) },
                                onArtistClick = { artist -> detailDestination = DetailDestination.ArtistDetail(artist) },
                                onOpenSmartPlaylist = { type -> detailDestination = DetailDestination.SmartPlaylistDetail(type) },
                                onOpenPlaylist = { pl -> detailDestination = DetailDestination.UserPlaylistDetail(pl.id) },
                                onCreatePlaylist = { name -> viewModel.createPlaylist(name); showToast("Created \"$name\"") },
                                onRenamePlaylist = { id, newName -> viewModel.renamePlaylist(id, newName) },
                                onDeletePlaylist = { id -> viewModel.deletePlaylist(id); showToast("Playlist deleted") },
                                onRescan = { viewModel.rescanLibrary(); showToast("Scanning local audio...") },
                                onMoreSongClick = { song -> sheetSong = song to null },
                                onOpenSettings = { showSettings = true }
                            )
                            MainTab.SEARCH -> SearchTabContent(
                                query = searchQuery,
                                songs = songs,
                                albums = albums,
                                artists = artists,
                                recentSearches = recentSearches,
                                currentSongId = playbackState.currentSong?.id,
                                isPlaying = playbackState.isPlaying,
                                settings = settings,
                                artworkLoader = viewModel.artworkLoader,
                                artCacheEpoch = artCacheEpoch,
                                bottomPadding = listBottomPadding,
                                onQueryChange = { viewModel.setSearchQuery(it) },
                                onCommitSearch = { viewModel.commitSearchHistory(it) },
                                onClearHistory = { viewModel.clearRecentSearches() },
                                onPlaySongs = { list, idx, shuffle -> viewModel.playbackManager.playSongs(list, idx, shuffle) },
                                onAlbumClick = { album -> detailDestination = DetailDestination.AlbumDetail(album) },
                                onArtistClick = { artist -> detailDestination = DetailDestination.ArtistDetail(artist) },
                                onMoreSongClick = { song -> sheetSong = song to null },
                                onOpenSettings = { showSettings = true }
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showSettings,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut()
        ) {
            OneUiSettingsScreen(
                settings = settings,
                songCount = songs.size,
                albumCount = albums.size,
                audioSessionId = playbackState.audioSessionId,
                artRefreshState = artRefreshState,
                artCacheSizeBytes = artCacheSizeBytes,
                randomizedGreeter = randomizedGreeter,
                onBack = {
                    if (!artRefreshState.isRunning) viewModel.clearArtFetchStatus()
                    showSettings = false
                },
                onSetThemeMode = { viewModel.setThemeMode(it) },
                onSetOledBlack = { viewModel.setOledBlack(it) },
                onSetAccentTheme = { viewModel.setAccentTheme(it) },
                onSetGreeterMode = { viewModel.setGreeterMode(it) },
                onSetCustomGreeterText = { viewModel.setCustomGreeterText(it) },
                onRerollGreeter = { viewModel.rerollGreeter() },
                onSetCustomLogoUri = { viewModel.setCustomHeaderLogoFromUri(it); showToast("Header picture updated") },
                onResetCustomLogo = { viewModel.clearCustomHeaderLogo(); showToast("Reset to KnightWin logo") },
                onSetNowPlayingStyle = { viewModel.setNowPlayingStyle(it) },
                onSetAlbumLayout = { viewModel.setAlbumLayout(it) },
                onSetArtistLayout = { viewModel.setArtistLayout(it) },
                onSetLiquidGlass = { viewModel.setLiquidGlassAccents(it) },
                onSetSortOrder = { viewModel.setSortOrder(it) },
                onSetMinDuration = { viewModel.setMinDurationFilter(it) },
                onSetCrossfadeMs = { viewModel.setCrossfadeMs(it) },
                onSetPlaybackSpeed = { viewModel.setPlaybackSpeed(it) },
                onSetReverbPreset = { viewModel.setReverbPreset(it) },
                onSetEqualizerPreset = { viewModel.setEqualizerPreset(it) },
                onSetEqualizerCustomBands = { viewModel.setEqualizerCustomBands(it) },
                onSetKeepScreenOn = { viewModel.setKeepScreenOn(it) },
                onSetPreferEmbeddedArt = { viewModel.setPreferEmbeddedArt(it) },
                onRescanLibrary = { viewModel.rescanLibrary(); showToast("Local library rescanned") },
                onFetchMissingArt = { viewModel.fetchAlbumArt(overwriteExisting = false) },
                onRefreshAllArt = { viewModel.fetchAlbumArt(overwriteExisting = true) },
                onCancelArtFetch = { viewModel.cancelArtFetch() },
                onClearArtStatus = { viewModel.clearArtFetchStatus() },
                onClearDownloadedArt = { viewModel.clearDownloadedArtCache(); showToast("Cleared downloaded album art cache") },
                onDismissBanner = { viewModel.dismissBanner() }
            )
        }

        AnimatedVisibility(
            visible = showNowPlaying && playbackState.currentSong != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            val activeSongId = playbackState.currentSong?.id
            NowPlayingScreen(
                playbackState = playbackState,
                settings = settings,
                isFavorite = activeSongId != null && favoriteIds.contains(activeSongId),
                artworkLoader = viewModel.artworkLoader,
                artCacheEpoch = artCacheEpoch,
                onDismiss = { showNowPlaying = false },
                onTogglePlayPause = { viewModel.playbackManager.togglePlayPause() },
                onSkipNext = { viewModel.playbackManager.skipNext() },
                onSkipPrevious = { viewModel.playbackManager.skipPrevious() },
                onSeekTo = { viewModel.playbackManager.seekTo(it) },
                onToggleShuffle = { viewModel.playbackManager.toggleShuffle() },
                onCycleRepeat = { viewModel.playbackManager.cycleRepeatMode() },
                onToggleFavorite = { activeSongId?.let { viewModel.toggleFavorite(it) } },
                onAddToPlaylist = { song -> playlistDialogSong = song },
                onJumpToQueueIndex = { viewModel.playbackManager.jumpToQueueIndex(it) },
                onRemoveFromQueue = { viewModel.playbackManager.removeFromQueue(it) },
                onClearQueue = { viewModel.playbackManager.clearUpcomingQueue() },
                onSetSleepTimerMinutes = { viewModel.playbackManager.setSleepTimerMinutes(it) },
                onSetSleepEndOfTrack = { viewModel.playbackManager.setSleepTimerEndOfTrack() },
                onCancelSleepTimer = { viewModel.playbackManager.cancelSleepTimer() }
            )
        }
    }

    if (!settings.firstRunArtPromptShown && libraryScannedOnce && !isScanning && albums.isNotEmpty()) {
        FirstRunArtConsentDialog(
            albumCount = albums.size,
            onFetchOnce = { viewModel.acceptFirstRunArtFetch() },
            onStayOffline = { viewModel.declineFirstRunArtFetch() }
        )
    }

    sheetSong?.let { (song, playlistId) ->
        SongOptionsBottomSheet(
            song = song,
            isFavorite = favoriteIds.contains(song.id),
            artworkLoader = viewModel.artworkLoader,
            preferEmbeddedArt = settings.preferEmbeddedArt,
            artCacheEpoch = artCacheEpoch,
            onDismiss = { sheetSong = null },
            onPlayNext = { viewModel.playbackManager.playNext(song); showToast("Playing next: ${song.title}") },
            onAddToQueue = { viewModel.playbackManager.addToQueue(song); showToast("Added to queue") },
            onToggleFavorite = { viewModel.toggleFavorite(song.id) },
            onAddToPlaylist = { playlistDialogSong = song },
            onShowDetails = { detailsDialogSong = song },
            extraRemoveFromPlaylistLabel = if (playlistId != null) "Remove from Playlist" else null,
            onExtraRemoveFromPlaylist = if (playlistId != null) {
                { viewModel.removeSongFromPlaylist(playlistId, song.id); showToast("Removed from playlist") }
            } else null
        )
    }

    playlistDialogSong?.let { song ->
        AddToPlaylistDialog(
            song = song,
            playlists = playlists,
            onDismiss = { playlistDialogSong = null },
            onSelectPlaylist = { pl -> viewModel.addSongToPlaylist(pl.id, song.id); showToast("Added to \"${pl.name}\"") },
            onCreateNewPlaylist = { name -> viewModel.createPlaylist(name, listOf(song.id)); showToast("Created \"$name\" and added song") }
        )
    }

    detailsDialogSong?.let { song ->
        SongDetailsDialog(song = song, onDismiss = { detailsDialogSong = null })
    }
}
