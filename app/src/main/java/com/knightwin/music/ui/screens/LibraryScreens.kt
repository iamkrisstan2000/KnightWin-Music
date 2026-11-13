package com.knightwin.music.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knightwin.music.R
import com.knightwin.music.data.media.ArtworkLoader
import com.knightwin.music.data.model.Album
import com.knightwin.music.data.model.AlbumLayout
import com.knightwin.music.data.model.AppSettings
import com.knightwin.music.data.model.Artist
import com.knightwin.music.data.model.LibrarySubTab
import com.knightwin.music.data.model.Playlist
import com.knightwin.music.data.model.Song
import com.knightwin.music.data.model.SortOrder
import com.knightwin.music.ui.components.ArtworkImage
import com.knightwin.music.ui.components.PlayingEqualizerBars
import com.knightwin.music.ui.components.formatDuration
import com.knightwin.music.ui.icons.KnightIcons
import com.knightwin.music.ui.theme.GlassVariant
import com.knightwin.music.ui.theme.GroupedLiquidGlassItem
import com.knightwin.music.ui.theme.KnightDialog
import com.knightwin.music.ui.theme.LiquidGlassSurface
import com.knightwin.music.ui.theme.LocalKnightExtras
import com.knightwin.music.ui.theme.verticalEdgeFade
import kotlinx.coroutines.launch

sealed interface DetailDestination {
    data class AlbumDetail(val album: Album) : DetailDestination
    data class ArtistDetail(val artist: Artist) : DetailDestination
    data class SmartPlaylistDetail(val type: SmartPlaylistType) : DetailDestination
    data class UserPlaylistDetail(val playlistId: String) : DetailDestination
}

enum class SmartPlaylistType(val title: String, val subtitle: String) {
    FAVORITES("Favorites", "Tracks you've loved"),
    RECENTLY_PLAYED("Recently Played", "Your latest local spins"),
    MOST_PLAYED("Most Played", "Your top local rotation")
}

@Composable
fun MainLibraryHeader(
    title: String,
    subtitle: String,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extras = LocalKnightExtras.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 18.dp, end = 14.dp, top = 12.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.knightwin_emblem),
            contentDescription = "KnightWin Music",
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        if (extras.liquidGlassEnabled) {
            LiquidGlassSurface(
                shape = CircleShape,
                variant = GlassVariant.PILL,
                elevation = 4.dp,
                modifier = Modifier.size(40.dp),
                onClick = onOpenSettings
            ) {
                Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = KnightIcons.SettingsGear,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }
        } else {
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = KnightIcons.SettingsGear,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun LibraryTabContent(
    activeSubTab: LibrarySubTab,
    onSelectSubTab: (LibrarySubTab) -> Unit,
    songs: List<Song>,
    albums: List<Album>,
    artists: List<Artist>,
    playlists: List<Playlist>,
    favoriteCount: Int,
    recentCount: Int,
    mostPlayedCount: Int,
    isScanning: Boolean,
    currentSongId: Long?,
    isPlaying: Boolean,
    settings: AppSettings,
    artworkLoader: ArtworkLoader,
    artCacheEpoch: Int,
    bottomPadding: PaddingValues,
    onPlaySongs: (List<Song>, Int, Boolean) -> Unit,
    onChangeSortOrder: (SortOrder) -> Unit,
    onToggleAlbumLayout: () -> Unit,
    onToggleArtistLayout: () -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onOpenSmartPlaylist: (SmartPlaylistType) -> Unit,
    onOpenPlaylist: (Playlist) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onRenamePlaylist: (String, String) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onRescan: () -> Unit,
    onMoreSongClick: (Song) -> Unit,
    onOpenSettings: () -> Unit
) {
    val subTabs = LibrarySubTab.entries
    val pagerState = rememberPagerState(
        initialPage = subTabs.indexOf(activeSubTab).coerceAtLeast(0),
        pageCount = { subTabs.size }
    )
    val scope = rememberCoroutineScope()

    LaunchedEffect(activeSubTab) {
        val target = subTabs.indexOf(activeSubTab).coerceAtLeast(0)
        if (pagerState.currentPage != target) {
            pagerState.animateScrollToPage(target)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            val tab = subTabs[page]
            if (tab != activeSubTab) onSelectSubTab(tab)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MainLibraryHeader(
            title = "Library",
            subtitle = "${songs.size} ${if (songs.size == 1) "track" else "tracks"} • ${albums.size} ${if (albums.size == 1) "album" else "albums"} • Offline",
            onOpenSettings = onOpenSettings
        )

        val extras = LocalKnightExtras.current
        if (extras.liquidGlassEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                LiquidGlassSurface(
                    shape = RoundedCornerShape(30.dp),
                    variant = GlassVariant.DOCK,
                    elevation = 10.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LibrarySubTabPillRow(
                        subTabs = subTabs,
                        activeSubTab = activeSubTab,
                        liquidMode = true,
                        onSelectTab = { index, subTab ->
                            onSelectSubTab(subTab)
                            scope.launch { pagerState.animateScrollToPage(index) }
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp)
                    )
                }
            }
        } else {
            LibrarySubTabPillRow(
                subTabs = subTabs,
                activeSubTab = activeSubTab,
                liquidMode = false,
                onSelectTab = { index, subTab ->
                    onSelectSubTab(subTab)
                    scope.launch { pagerState.animateScrollToPage(index) }
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (subTabs[page]) {
                LibrarySubTab.SONGS -> SongsTabContent(
                    songs = songs,
                    isScanning = isScanning,
                    currentSongId = currentSongId,
                    isPlaying = isPlaying,
                    settings = settings,
                    artworkLoader = artworkLoader,
                    artCacheEpoch = artCacheEpoch,
                    bottomPadding = bottomPadding,
                    onPlaySongs = onPlaySongs,
                    onChangeSortOrder = onChangeSortOrder,
                    onRescan = onRescan,
                    onMoreSongClick = onMoreSongClick
                )
                LibrarySubTab.ALBUMS -> AlbumsTabContent(
                    albums = albums,
                    settings = settings,
                    artworkLoader = artworkLoader,
                    artCacheEpoch = artCacheEpoch,
                    bottomPadding = bottomPadding,
                    onToggleAlbumLayout = onToggleAlbumLayout,
                    onAlbumClick = onAlbumClick,
                    onRescan = onRescan
                )
                LibrarySubTab.ARTISTS -> ArtistsTabContent(
                    artists = artists,
                    settings = settings,
                    artworkLoader = artworkLoader,
                    artCacheEpoch = artCacheEpoch,
                    bottomPadding = bottomPadding,
                    onToggleArtistLayout = onToggleArtistLayout,
                    onArtistClick = onArtistClick,
                    onRescan = onRescan
                )
                LibrarySubTab.PLAYLISTS -> PlaylistsTabContent(
                    playlists = playlists,
                    favoriteCount = favoriteCount,
                    recentCount = recentCount,
                    mostPlayedCount = mostPlayedCount,
                    bottomPadding = bottomPadding,
                    onOpenSmartPlaylist = onOpenSmartPlaylist,
                    onOpenPlaylist = onOpenPlaylist,
                    onCreatePlaylist = onCreatePlaylist,
                    onRenamePlaylist = onRenamePlaylist,
                    onDeletePlaylist = onDeletePlaylist
                )
            }
        }
    }
}

@Composable
private fun LibrarySubTabPillRow(
    subTabs: List<LibrarySubTab>,
    activeSubTab: LibrarySubTab,
    liquidMode: Boolean,
    onSelectTab: (Int, LibrarySubTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        subTabs.forEachIndexed { index, subTab ->
            val selected = subTab == activeSubTab
            val icon = when (subTab) {
                LibrarySubTab.SONGS -> KnightIcons.Songs
                LibrarySubTab.ALBUMS -> KnightIcons.Albums
                LibrarySubTab.ARTISTS -> KnightIcons.Artists
                LibrarySubTab.PLAYLISTS -> KnightIcons.Playlists
            }
            val tabWeight = when (subTab) {
                LibrarySubTab.SONGS -> 0.90f
                LibrarySubTab.ALBUMS -> 0.98f
                LibrarySubTab.ARTISTS -> 0.96f
                LibrarySubTab.PLAYLISTS -> 1.16f
            }

            if (liquidMode && selected) {
                LiquidGlassSurface(
                    shape = CircleShape,
                    variant = GlassVariant.PROMINENT,
                    elevation = 6.dp,
                    modifier = Modifier
                        .weight(tabWeight)
                        .height(40.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelectTab(index, subTab) }
                ) {
                    SubTabPillContent(
                        icon = icon,
                        label = subTab.label,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        bold = true,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else {
                val pillColor by animateColorAsState(
                    targetValue = if (!liquidMode && selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    animationSpec = tween(200),
                    label = "subTabPillBg"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (!liquidMode && selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(200),
                    label = "subTabPillContent"
                )
                Box(
                    modifier = Modifier
                        .weight(tabWeight)
                        .height(40.dp)
                        .clip(CircleShape)
                        .background(pillColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelectTab(index, subTab) },
                    contentAlignment = Alignment.Center
                ) {
                    SubTabPillContent(
                        icon = icon,
                        label = subTab.label,
                        contentColor = contentColor,
                        bold = !liquidMode && selected
                    )
                }
            }
        }
    }
}

@Composable
private fun SubTabPillContent(
    icon: ImageVector,
    label: String,
    contentColor: Color,
    bold: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 12.5.sp,
                fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium
            ),
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SongsTabContent(
    songs: List<Song>,
    isScanning: Boolean,
    currentSongId: Long?,
    isPlaying: Boolean,
    settings: AppSettings,
    artworkLoader: ArtworkLoader,
    artCacheEpoch: Int,
    bottomPadding: PaddingValues,
    onPlaySongs: (List<Song>, Int, Boolean) -> Unit,
    onChangeSortOrder: (SortOrder) -> Unit,
    onRescan: () -> Unit,
    onMoreSongClick: (Song) -> Unit
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {

        if (isScanning && songs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Scanning local audio library...", style = MaterialTheme.typography.bodyMedium)
                }
            }
            return
        }

        if (songs.isEmpty()) {
            EmptyLibraryState(onRescan = onRescan)
            return
        }

        val extras = LocalKnightExtras.current
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                if (extras.liquidGlassEnabled) {
                    LiquidGlassSurface(
                        shape = CircleShape,
                        variant = GlassVariant.PILL,
                        elevation = 3.dp,
                        modifier = Modifier.height(32.dp),
                        onClick = { sortMenuExpanded = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = KnightIcons.SortArrows,
                                contentDescription = "Sort by",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = settings.sortOrder.label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }
                } else {
                    val sortBorderColor = if (extras.isOled) {
                        MaterialTheme.colorScheme.outlineVariant
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)
                    }
                    val sortBgColor = if (extras.isOled) {
                        Color.Black
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    }
                    Row(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(CircleShape)
                            .background(sortBgColor)
                            .border(0.85.dp, sortBorderColor, CircleShape)
                            .clickable { sortMenuExpanded = true }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = KnightIcons.SortArrows,
                            contentDescription = "Sort by",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = settings.sortOrder.label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }
                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false },
                    containerColor = if (extras.isOled) Color.Black else MaterialTheme.colorScheme.surfaceContainer,
                    border = if (extras.isOled) BorderStroke(0.85.dp, MaterialTheme.colorScheme.outlineVariant) else null
                ) {
                    SortOrder.entries.forEach { order ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = order.label,
                                    fontWeight = if (settings.sortOrder == order) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            trailingIcon = if (settings.sortOrder == order) {
                                { Icon(imageVector = KnightIcons.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
                            } else null,
                            onClick = {
                                onChangeSortOrder(order)
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            }

            if (extras.liquidGlassEnabled) {
                LiquidGlassSurface(
                    shape = CircleShape,
                    variant = GlassVariant.PROMINENT,
                    elevation = 5.dp,
                    modifier = Modifier.size(34.dp),
                    onClick = { onPlaySongs(songs, 0, true) }
                ) {
                    Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = KnightIcons.Shuffle,
                            contentDescription = "Shuffle All",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .then(
                            if (extras.isOled) {
                                Modifier.border(0.85.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            } else {
                                Modifier
                            }
                        )
                        .clickable { onPlaySongs(songs, 0, true) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = KnightIcons.Shuffle,
                        contentDescription = "Shuffle All",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        if (extras.liquidGlassEnabled) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .padding(top = 4.dp, bottom = bottomPadding.calculateBottomPadding()),
                contentAlignment = Alignment.TopCenter
            ) {
                LiquidGlassSurface(
                    shape = RoundedCornerShape(24.dp),
                    variant = GlassVariant.CARD,
                    elevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(top = 8.dp, bottom = 10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalEdgeFade(topFade = 8.dp, bottomFade = 10.dp)
                    ) {
                        itemsIndexed(songs, key = { _, item -> item.id }) { index, song ->
                            SongRowItem(
                                song = song,
                                isCurrent = song.id == currentSongId,
                                isPlaying = isPlaying && song.id == currentSongId,
                                artworkLoader = artworkLoader,
                                preferEmbeddedArt = settings.preferEmbeddedArt,
                                artCacheEpoch = artCacheEpoch,
                                inGroupedBox = true,
                                onClick = { onPlaySongs(songs, index, false) },
                                onMoreClick = { onMoreSongClick(song) }
                            )
                            if (index < songs.lastIndex) {
                                HorizontalDivider(
                                    thickness = 0.6.dp,
                                    color = extras.oneUiDivider,
                                    modifier = Modifier.padding(start = 74.dp, end = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = 8.dp,
                    bottom = bottomPadding.calculateBottomPadding() + 8.dp
                ),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalEdgeFade(topFade = 10.dp, bottomFade = 16.dp)
            ) {
                itemsIndexed(songs, key = { _, item -> item.id }) { index, song ->
                    SongRowItem(
                        song = song,
                        isCurrent = song.id == currentSongId,
                        isPlaying = isPlaying && song.id == currentSongId,
                        artworkLoader = artworkLoader,
                        preferEmbeddedArt = settings.preferEmbeddedArt,
                        artCacheEpoch = artCacheEpoch,
                        onClick = { onPlaySongs(songs, index, false) },
                        onMoreClick = { onMoreSongClick(song) }
                    )
                }
            }
        }
    }
}

@Composable
fun AlbumsTabContent(
    albums: List<Album>,
    settings: AppSettings,
    artworkLoader: ArtworkLoader,
    artCacheEpoch: Int,
    bottomPadding: PaddingValues,
    onToggleAlbumLayout: () -> Unit,
    onAlbumClick: (Album) -> Unit,
    onRescan: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (albums.isEmpty()) {
            EmptyLibraryState(onRescan = onRescan)
            return
        }

        val extras = LocalKnightExtras.current
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            if (extras.liquidGlassEnabled) {
                LiquidGlassSurface(
                    shape = CircleShape,
                    variant = GlassVariant.PILL,
                    elevation = 3.dp,
                    modifier = Modifier.height(34.dp),
                    onClick = onToggleAlbumLayout
                ) {
                    Row(
                        modifier = Modifier.align(Alignment.Center).padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = KnightIcons.Albums, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (settings.albumLayout == AlbumLayout.GRID) "Grid View" else "List View",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.5.sp)
                        )
                    }
                }
            } else {
                FilterChip(
                    selected = settings.albumLayout == AlbumLayout.GRID,
                    onClick = onToggleAlbumLayout,
                    label = { Text(if (settings.albumLayout == AlbumLayout.GRID) "Grid View" else "List View") },
                    leadingIcon = { Icon(imageVector = KnightIcons.Albums, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
        }
        if (settings.albumLayout == AlbumLayout.GRID) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = 14.dp,
                    end = 14.dp,
                    top = 8.dp,
                    bottom = bottomPadding.calculateBottomPadding() + 8.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalEdgeFade(topFade = 10.dp, bottomFade = 16.dp)
            ) {
                items(albums, key = { "${it.id}_${it.name}" }) { album ->
                    if (extras.liquidGlassEnabled) {
                        LiquidGlassSurface(
                            shape = RoundedCornerShape(20.dp),
                            variant = GlassVariant.CARD,
                            elevation = 4.dp,
                            onClick = { onAlbumClick(album) }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                ArtworkImage(
                                    albumId = album.id,
                                    artistName = album.artist,
                                    albumName = album.name,
                                    artUri = album.artUri,
                                    sampleSongUri = album.sampleSongUri,
                                    artworkLoader = artworkLoader,
                                    preferEmbedded = settings.preferEmbeddedArt,
                                    cacheEpoch = artCacheEpoch,
                                    shape = RoundedCornerShape(14.dp),
                                    targetSizePx = 360,
                                    iconSize = 36.dp,
                                    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(album.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    text = "${album.artist} • ${album.songCount} ${if (album.songCount == 1) "song" else "songs"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    } else {
                        Card(
                            onClick = { onAlbumClick(album) },
                            shape = RoundedCornerShape(18.dp),
                            border = if (extras.isOled) BorderStroke(0.85.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                ArtworkImage(
                                    albumId = album.id,
                                    artistName = album.artist,
                                    albumName = album.name,
                                    artUri = album.artUri,
                                    sampleSongUri = album.sampleSongUri,
                                    artworkLoader = artworkLoader,
                                    preferEmbedded = settings.preferEmbeddedArt,
                                    cacheEpoch = artCacheEpoch,
                                    shape = RoundedCornerShape(14.dp),
                                    targetSizePx = 360,
                                    iconSize = 36.dp,
                                    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(album.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    text = "${album.artist} • ${album.songCount} ${if (album.songCount == 1) "song" else "songs"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        } else if (extras.liquidGlassEnabled) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .padding(top = 4.dp, bottom = bottomPadding.calculateBottomPadding()),
                contentAlignment = Alignment.TopCenter
            ) {
                LiquidGlassSurface(
                    shape = RoundedCornerShape(24.dp),
                    variant = GlassVariant.CARD,
                    elevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(top = 8.dp, bottom = 10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalEdgeFade(topFade = 8.dp, bottomFade = 10.dp)
                    ) {
                        itemsIndexed(albums, key = { _, a -> "${a.id}_${a.name}" }) { index, album ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAlbumClick(album) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ArtworkImage(
                                    albumId = album.id,
                                    artistName = album.artist,
                                    albumName = album.name,
                                    artUri = album.artUri,
                                    sampleSongUri = album.sampleSongUri,
                                    artworkLoader = artworkLoader,
                                    preferEmbedded = settings.preferEmbeddedArt,
                                    cacheEpoch = artCacheEpoch,
                                    shape = RoundedCornerShape(12.dp),
                                    targetSizePx = 180,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(album.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        text = "${album.artist} • ${album.songCount} ${if (album.songCount == 1) "track" else "tracks"} • ${formatDuration(album.totalDurationMs)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            if (index < albums.lastIndex) {
                                HorizontalDivider(
                                    thickness = 0.6.dp,
                                    color = extras.oneUiDivider,
                                    modifier = Modifier.padding(start = 82.dp, end = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = 8.dp,
                    bottom = bottomPadding.calculateBottomPadding() + 8.dp
                ),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalEdgeFade(topFade = 10.dp, bottomFade = 16.dp)
            ) {
                items(albums, key = { "${it.id}_${it.name}" }) { album ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAlbumClick(album) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ArtworkImage(
                            albumId = album.id,
                            artistName = album.artist,
                            albumName = album.name,
                            artUri = album.artUri,
                            sampleSongUri = album.sampleSongUri,
                            artworkLoader = artworkLoader,
                            preferEmbedded = settings.preferEmbeddedArt,
                            cacheEpoch = artCacheEpoch,
                            shape = RoundedCornerShape(12.dp),
                            targetSizePx = 180,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(album.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                text = "${album.artist} • ${album.songCount} ${if (album.songCount == 1) "track" else "tracks"} • ${formatDuration(album.totalDurationMs)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArtistsTabContent(
    artists: List<Artist>,
    settings: AppSettings,
    artworkLoader: ArtworkLoader,
    artCacheEpoch: Int,
    bottomPadding: PaddingValues,
    onToggleArtistLayout: () -> Unit,
    onArtistClick: (Artist) -> Unit,
    onRescan: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (artists.isEmpty()) {
            EmptyLibraryState(onRescan = onRescan)
            return
        }

        val extras = LocalKnightExtras.current
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            if (extras.liquidGlassEnabled) {
                LiquidGlassSurface(
                    shape = CircleShape,
                    variant = GlassVariant.PILL,
                    elevation = 3.dp,
                    modifier = Modifier.height(34.dp),
                    onClick = onToggleArtistLayout
                ) {
                    Row(
                        modifier = Modifier.align(Alignment.Center).padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = KnightIcons.Artists, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (settings.artistLayout == AlbumLayout.GRID) "Grid View" else "List View",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.5.sp)
                        )
                    }
                }
            } else {
                FilterChip(
                    selected = settings.artistLayout == AlbumLayout.GRID,
                    onClick = onToggleArtistLayout,
                    label = { Text(if (settings.artistLayout == AlbumLayout.GRID) "Grid View" else "List View") },
                    leadingIcon = { Icon(imageVector = KnightIcons.Artists, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
        }

        if (settings.artistLayout == AlbumLayout.GRID) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = 14.dp,
                    end = 14.dp,
                    top = 8.dp,
                    bottom = bottomPadding.calculateBottomPadding() + 8.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalEdgeFade(topFade = 10.dp, bottomFade = 16.dp)
            ) {
                items(artists, key = { it.id }) { artist ->
                    val artistSubtitle = "${artist.albumCount} ${if (artist.albumCount == 1) "album" else "albums"} • ${artist.songCount} ${if (artist.songCount == 1) "song" else "songs"}"
                    if (extras.liquidGlassEnabled) {
                        LiquidGlassSurface(
                            shape = RoundedCornerShape(20.dp),
                            variant = GlassVariant.CARD,
                            elevation = 4.dp,
                            onClick = { onArtistClick(artist) }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                ArtworkImage(
                                    albumId = artist.artUri.lastPathSegment?.toLongOrNull() ?: 0L,
                                    artistName = artist.name,
                                    albumName = artist.sampleAlbumName,
                                    artUri = artist.artUri,
                                    sampleSongUri = artist.sampleSongUri,
                                    artworkLoader = artworkLoader,
                                    preferEmbedded = settings.preferEmbeddedArt,
                                    cacheEpoch = artCacheEpoch,
                                    shape = CircleShape,
                                    targetSizePx = 320,
                                    iconSize = 36.dp,
                                    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = artist.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = artistSubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    } else {
                        Card(
                            onClick = { onArtistClick(artist) },
                            shape = RoundedCornerShape(18.dp),
                            border = if (extras.isOled) BorderStroke(0.85.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                ArtworkImage(
                                    albumId = artist.artUri.lastPathSegment?.toLongOrNull() ?: 0L,
                                    artistName = artist.name,
                                    albumName = artist.sampleAlbumName,
                                    artUri = artist.artUri,
                                    sampleSongUri = artist.sampleSongUri,
                                    artworkLoader = artworkLoader,
                                    preferEmbedded = settings.preferEmbeddedArt,
                                    cacheEpoch = artCacheEpoch,
                                    shape = CircleShape,
                                    targetSizePx = 320,
                                    iconSize = 36.dp,
                                    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = artist.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = artistSubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        } else if (extras.liquidGlassEnabled) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .padding(top = 4.dp, bottom = bottomPadding.calculateBottomPadding()),
                contentAlignment = Alignment.TopCenter
            ) {
                LiquidGlassSurface(
                    shape = RoundedCornerShape(24.dp),
                    variant = GlassVariant.CARD,
                    elevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(top = 8.dp, bottom = 10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalEdgeFade(topFade = 8.dp, bottomFade = 10.dp)
                    ) {
                        itemsIndexed(artists, key = { _, a -> a.id }) { index, artist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onArtistClick(artist) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ArtworkImage(
                                    albumId = artist.artUri.lastPathSegment?.toLongOrNull() ?: 0L,
                                    artistName = artist.name,
                                    albumName = artist.sampleAlbumName,
                                    artUri = artist.artUri,
                                    sampleSongUri = artist.sampleSongUri,
                                    artworkLoader = artworkLoader,
                                    preferEmbedded = settings.preferEmbeddedArt,
                                    cacheEpoch = artCacheEpoch,
                                    shape = CircleShape,
                                    targetSizePx = 160,
                                    iconSize = 22.dp,
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(artist.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        text = "${artist.albumCount} ${if (artist.albumCount == 1) "album" else "albums"} • ${artist.songCount} ${if (artist.songCount == 1) "song" else "songs"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (index < artists.lastIndex) {
                                HorizontalDivider(
                                    thickness = 0.6.dp,
                                    color = extras.oneUiDivider,
                                    modifier = Modifier.padding(start = 80.dp, end = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = 8.dp,
                    bottom = bottomPadding.calculateBottomPadding() + 8.dp
                ),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalEdgeFade(topFade = 10.dp, bottomFade = 16.dp)
            ) {
                items(artists, key = { it.id }) { artist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onArtistClick(artist) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ArtworkImage(
                            albumId = artist.artUri.lastPathSegment?.toLongOrNull() ?: 0L,
                            artistName = artist.name,
                            albumName = artist.sampleAlbumName,
                            artUri = artist.artUri,
                            sampleSongUri = artist.sampleSongUri,
                            artworkLoader = artworkLoader,
                            preferEmbedded = settings.preferEmbeddedArt,
                            cacheEpoch = artCacheEpoch,
                            shape = CircleShape,
                            targetSizePx = 160,
                            iconSize = 22.dp,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(artist.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                text = "${artist.albumCount} ${if (artist.albumCount == 1) "album" else "albums"} • ${artist.songCount} ${if (artist.songCount == 1) "song" else "songs"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistsTabContent(
    playlists: List<Playlist>,
    favoriteCount: Int,
    recentCount: Int,
    mostPlayedCount: Int,
    bottomPadding: PaddingValues,
    onOpenSmartPlaylist: (SmartPlaylistType) -> Unit,
    onOpenPlaylist: (Playlist) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onRenamePlaylist: (String, String) -> Unit,
    onDeletePlaylist: (String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<Playlist?>(null) }

    val extras = LocalKnightExtras.current
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(top = 8.dp, bottom = bottomPadding.calculateBottomPadding() + 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .verticalEdgeFade(topFade = 10.dp, bottomFade = 16.dp)
        ) {
            item {
                Text(
                    text = "Smart Collections",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
            item {
                LiquidGlassSurface(
                    shape = RoundedCornerShape(24.dp),
                    variant = GlassVariant.CARD,
                    elevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SmartPlaylistRow(
                            title = "Favorites",
                            subtitle = "$favoriteCount ${if (favoriteCount == 1) "song" else "songs"}",
                            icon = KnightIcons.HeartFilled,
                            onClick = { onOpenSmartPlaylist(SmartPlaylistType.FAVORITES) }
                        )
                        HorizontalDivider(
                            thickness = 0.6.dp,
                            color = extras.oneUiDivider,
                            modifier = Modifier.padding(start = 74.dp, end = 16.dp)
                        )
                        SmartPlaylistRow(
                            title = "Recently Played",
                            subtitle = "$recentCount recent ${if (recentCount == 1) "track" else "tracks"}",
                            icon = KnightIcons.VinylDisc,
                            onClick = { onOpenSmartPlaylist(SmartPlaylistType.RECENTLY_PLAYED) }
                        )
                        HorizontalDivider(
                            thickness = 0.6.dp,
                            color = extras.oneUiDivider,
                            modifier = Modifier.padding(start = 74.dp, end = 16.dp)
                        )
                        SmartPlaylistRow(
                            title = "Most Played",
                            subtitle = "$mostPlayedCount played ${if (mostPlayedCount == 1) "track" else "tracks"}",
                            icon = KnightIcons.Sparkles,
                            onClick = { onOpenSmartPlaylist(SmartPlaylistType.MOST_PLAYED) }
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Your Playlists", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    FilledTonalButton(onClick = { showCreateDialog = true }, shape = RoundedCornerShape(14.dp)) {
                        Icon(imageVector = KnightIcons.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New")
                    }
                }
            }

            if (playlists.isEmpty()) {
                item {
                    LiquidGlassSurface(
                        shape = RoundedCornerShape(24.dp),
                        variant = GlassVariant.CARD,
                        elevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = KnightIcons.Playlists, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No custom playlists yet", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "Tap New or use a song's menu to organize your offline mixes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                item {
                    LiquidGlassSurface(
                        shape = RoundedCornerShape(24.dp),
                        variant = GlassVariant.CARD,
                        elevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            playlists.forEachIndexed { index, pl ->
                                var menuOpen by remember { mutableStateOf(false) }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onOpenPlaylist(pl) }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = KnightIcons.Playlists, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(22.dp))
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(pl.name, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            text = "${pl.songIds.size} ${if (pl.songIds.size == 1) "song" else "songs"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Box {
                                        IconButton(onClick = { menuOpen = true }) {
                                            Icon(imageVector = KnightIcons.MoreDots, contentDescription = "Playlist options", modifier = Modifier.size(20.dp))
                                        }
                                        DropdownMenu(
                                            expanded = menuOpen,
                                            onDismissRequest = { menuOpen = false },
                                            containerColor = if (extras.isOled) Color.Black else MaterialTheme.colorScheme.surfaceContainer,
                                            border = if (extras.isOled) BorderStroke(0.85.dp, MaterialTheme.colorScheme.outlineVariant) else null
                                        ) {
                                            DropdownMenuItem(text = { Text("Rename") }, onClick = { menuOpen = false; renameTarget = pl })
                                            DropdownMenuItem(text = { Text("Delete", color = MaterialTheme.colorScheme.error) }, onClick = { menuOpen = false; onDeletePlaylist(pl.id) })
                                        }
                                    }
                                }
                                if (index < playlists.lastIndex) {
                                    HorizontalDivider(
                                        thickness = 0.6.dp,
                                        color = extras.oneUiDivider,
                                        modifier = Modifier.padding(start = 74.dp, end = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        PlaylistNameDialog("New Playlist", "", "Create", { showCreateDialog = false }) {
            onCreatePlaylist(it)
            showCreateDialog = false
        }
    }
    renameTarget?.let { pl ->
        PlaylistNameDialog("Rename Playlist", pl.name, "Save", { renameTarget = null }) {
            onRenamePlaylist(pl.id, it)
            renameTarget = null
        }
    }
}

@Composable
private fun SmartPlaylistRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private enum class SearchScope(val label: String) {
    ALL("All"), SONGS("Songs"), ALBUMS("Albums"), ARTISTS("Artists")
}

@Composable
fun SearchTabContent(
    query: String,
    songs: List<Song>,
    albums: List<Album>,
    artists: List<Artist>,
    recentSearches: List<String>,
    currentSongId: Long?,
    isPlaying: Boolean,
    settings: AppSettings,
    artworkLoader: ArtworkLoader,
    artCacheEpoch: Int,
    bottomPadding: PaddingValues,
    onQueryChange: (String) -> Unit,
    onCommitSearch: (String) -> Unit,
    onClearHistory: () -> Unit,
    onPlaySongs: (List<Song>, Int, Boolean) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onMoreSongClick: (Song) -> Unit,
    onOpenSettings: () -> Unit
) {
    var scopeFilter by remember { mutableStateOf(SearchScope.ALL) }
    val trimmed = query.trim()

    val matchedSongs = remember(trimmed, songs) {
        if (trimmed.isEmpty()) emptyList()
        else songs.filter {
            it.title.contains(trimmed, ignoreCase = true) ||
                it.artist.contains(trimmed, ignoreCase = true) ||
                it.album.contains(trimmed, ignoreCase = true)
        }
    }
    val matchedAlbums = remember(trimmed, albums) {
        if (trimmed.isEmpty()) emptyList()
        else albums.filter {
            it.name.contains(trimmed, ignoreCase = true) ||
                it.artist.contains(trimmed, ignoreCase = true)
        }
    }
    val matchedArtists = remember(trimmed, artists) {
        if (trimmed.isEmpty()) emptyList()
        else artists.filter { it.name.contains(trimmed, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MainLibraryHeader(
            title = "Search",
            subtitle = "Instant offline search across your device",
            onOpenSettings = onOpenSettings
        )

        LiquidGlassSurface(
            shape = CircleShape,
            variant = GlassVariant.PILL,
            elevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = KnightIcons.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text("Songs, albums, artists...", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onCommitSearch(query) }),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = KnightIcons.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        if (trimmed.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 12.dp)) {
                if (recentSearches.isNotEmpty()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Recent Searches", style = MaterialTheme.typography.titleSmall)
                        TextButton(onClick = onClearHistory) { Text("Clear") }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        recentSearches.forEach { term ->
                            SuggestionChip(onClick = { onQueryChange(term) }, label = { Text(term) })
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(36.dp))
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = KnightIcons.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f), modifier = Modifier.size(44.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Search Local Music", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Everything is searched locally on your phone — zero network needed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            return
        }

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SearchScope.entries.forEach { scope ->
                FilterChip(selected = scopeFilter == scope, onClick = { scopeFilter = scope }, label = { Text(scope.label) })
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(top = 8.dp, bottom = bottomPadding.calculateBottomPadding() + 8.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalEdgeFade(topFade = 10.dp, bottomFade = 16.dp)
        ) {
            val showAlbums = (scopeFilter == SearchScope.ALL || scopeFilter == SearchScope.ALBUMS) && matchedAlbums.isNotEmpty()
            val showArtists = (scopeFilter == SearchScope.ALL || scopeFilter == SearchScope.ARTISTS) && matchedArtists.isNotEmpty()
            val showSongs = (scopeFilter == SearchScope.ALL || scopeFilter == SearchScope.SONGS) && matchedSongs.isNotEmpty()

            if (showAlbums) {
                item {
                    Text("Albums", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp))
                }
                item {
                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(matchedAlbums, key = { "${it.id}_${it.name}" }) { album ->
                            Column(modifier = Modifier.width(126.dp).clickable { onCommitSearch(trimmed); onAlbumClick(album) }) {
                                ArtworkImage(
                                    albumId = album.id,
                                    artistName = album.artist,
                                    albumName = album.name,
                                    artUri = album.artUri,
                                    sampleSongUri = album.sampleSongUri,
                                    artworkLoader = artworkLoader,
                                    preferEmbedded = settings.preferEmbeddedArt,
                                    cacheEpoch = artCacheEpoch,
                                    shape = RoundedCornerShape(14.dp),
                                    targetSizePx = 260,
                                    modifier = Modifier.size(126.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(album.name, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(album.artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (showArtists) {
                item {
                    Text("Artists", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp))
                }
                itemsIndexed(matchedArtists, key = { _, a -> a.id }) { idx, artist ->
                    val extras = LocalKnightExtras.current
                    if (extras.liquidGlassEnabled) {
                        GroupedLiquidGlassItem(
                            isFirst = idx == 0,
                            isLast = idx == matchedArtists.lastIndex,
                            dividerStartPadding = 70.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onCommitSearch(trimmed); onArtistClick(artist) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ArtworkImage(
                                    albumId = artist.artUri.lastPathSegment?.toLongOrNull() ?: 0L,
                                    artistName = artist.name,
                                    albumName = artist.sampleAlbumName,
                                    artUri = artist.artUri,
                                    sampleSongUri = artist.sampleSongUri,
                                    artworkLoader = artworkLoader,
                                    preferEmbedded = settings.preferEmbeddedArt,
                                    cacheEpoch = artCacheEpoch,
                                    shape = CircleShape,
                                    targetSizePx = 140,
                                    modifier = Modifier.size(46.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(artist.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                                    Text("${artist.songCount} ${if (artist.songCount == 1) "song" else "songs"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onCommitSearch(trimmed); onArtistClick(artist) }.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ArtworkImage(
                                albumId = artist.artUri.lastPathSegment?.toLongOrNull() ?: 0L,
                                artistName = artist.name,
                                albumName = artist.sampleAlbumName,
                                artUri = artist.artUri,
                                sampleSongUri = artist.sampleSongUri,
                                artworkLoader = artworkLoader,
                                preferEmbedded = settings.preferEmbeddedArt,
                                cacheEpoch = artCacheEpoch,
                                shape = CircleShape,
                                targetSizePx = 140,
                                modifier = Modifier.size(46.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(artist.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                                Text("${artist.songCount} ${if (artist.songCount == 1) "song" else "songs"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(6.dp)) }
            }

            if (showSongs) {
                item {
                    Text("Songs", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp))
                }
                itemsIndexed(matchedSongs, key = { _, s -> s.id }) { idx, song ->
                    val extras = LocalKnightExtras.current
                    if (extras.liquidGlassEnabled) {
                        GroupedLiquidGlassItem(
                            isFirst = idx == 0,
                            isLast = idx == matchedSongs.lastIndex,
                            dividerStartPadding = 74.dp
                        ) {
                            SongRowItem(
                                song = song,
                                isCurrent = song.id == currentSongId,
                                isPlaying = isPlaying && song.id == currentSongId,
                                artworkLoader = artworkLoader,
                                preferEmbeddedArt = settings.preferEmbeddedArt,
                                artCacheEpoch = artCacheEpoch,
                                inGroupedBox = true,
                                onClick = { onCommitSearch(trimmed); onPlaySongs(matchedSongs, idx, false) },
                                onMoreClick = { onMoreSongClick(song) }
                            )
                        }
                    } else {
                        SongRowItem(
                            song = song,
                            isCurrent = song.id == currentSongId,
                            isPlaying = isPlaying && song.id == currentSongId,
                            artworkLoader = artworkLoader,
                            preferEmbeddedArt = settings.preferEmbeddedArt,
                            artCacheEpoch = artCacheEpoch,
                            onClick = { onCommitSearch(trimmed); onPlaySongs(matchedSongs, idx, false) },
                            onMoreClick = { onMoreSongClick(song) }
                        )
                    }
                }
            }

            if (!showAlbums && !showArtists && !showSongs) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("No matches for \"$trimmed\"", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailScreenContent(
    destination: DetailDestination,
    allSongs: List<Song>,
    allAlbums: List<Album>,
    playlists: List<Playlist>,
    favoriteIds: Set<Long>,
    recentlyPlayedIds: List<Long>,
    playCounts: Map<Long, Int>,
    currentSongId: Long?,
    isPlaying: Boolean,
    settings: AppSettings,
    artworkLoader: ArtworkLoader,
    artCacheEpoch: Int,
    bottomPadding: PaddingValues,
    onBack: () -> Unit,
    onPlaySongs: (List<Song>, Int, Boolean) -> Unit,
    onMoreSongClick: (Song, String?) -> Unit,
    onAlbumClick: (Album) -> Unit
) {
    BackHandler(onBack = onBack)
    val songsById = remember(allSongs) { allSongs.associateBy { it.id } }

    val (title, subtitle, detailSongs) = when (destination) {
        is DetailDestination.AlbumDetail -> {
            val list = allSongs
                .filter { it.albumId == destination.album.id && it.album == destination.album.name }
                .sortedWith(compareBy<Song> { it.trackNumber }.thenBy { it.title })
            Triple(destination.album.name, "${destination.album.artist} • ${list.size} tracks", list)
        }
        is DetailDestination.ArtistDetail -> {
            val list = allSongs.filter { it.artist.equals(destination.artist.name, ignoreCase = true) }
            Triple(destination.artist.name, "${destination.artist.albumCount} albums • ${list.size} songs", list)
        }
        is DetailDestination.SmartPlaylistDetail -> {
            val list = when (destination.type) {
                SmartPlaylistType.FAVORITES -> allSongs.filter { favoriteIds.contains(it.id) }
                SmartPlaylistType.RECENTLY_PLAYED -> recentlyPlayedIds.mapNotNull { songsById[it] }
                SmartPlaylistType.MOST_PLAYED -> allSongs.filter { (playCounts[it.id] ?: 0) > 0 }.sortedByDescending { playCounts[it.id] ?: 0 }
            }
            Triple(destination.type.title, "${list.size} ${if (list.size == 1) "song" else "songs"}", list)
        }
        is DetailDestination.UserPlaylistDetail -> {
            val pl = playlists.find { it.id == destination.playlistId }
            val list = pl?.songIds?.mapNotNull { songsById[it] } ?: emptyList()
            Triple(pl?.name ?: "Playlist", "${list.size} ${if (list.size == 1) "song" else "songs"}", list)
        }
    }

    val removablePlaylistId = (destination as? DetailDestination.UserPlaylistDetail)?.playlistId

    Column(modifier = Modifier.fillMaxSize()) {
        val extras = LocalKnightExtras.current
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (extras.liquidGlassEnabled) {
                LiquidGlassSurface(
                    shape = CircleShape,
                    variant = GlassVariant.PILL,
                    elevation = 4.dp,
                    modifier = Modifier.size(40.dp),
                    onClick = onBack
                ) {
                    Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                        Icon(imageVector = KnightIcons.BackChevron, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
            } else {
                IconButton(onClick = onBack) {
                    Icon(imageVector = KnightIcons.BackChevron, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(top = 8.dp, bottom = bottomPadding.calculateBottomPadding() + 8.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalEdgeFade(topFade = 10.dp, bottomFade = 16.dp)
        ) {
            if (destination is DetailDestination.AlbumDetail) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ArtworkImage(
                            albumId = destination.album.id,
                            artistName = destination.album.artist,
                            albumName = destination.album.name,
                            artUri = destination.album.artUri,
                            sampleSongUri = destination.album.sampleSongUri,
                            artworkLoader = artworkLoader,
                            preferEmbedded = settings.preferEmbeddedArt,
                            cacheEpoch = artCacheEpoch,
                            shape = RoundedCornerShape(22.dp),
                            targetSizePx = 480,
                            iconSize = 52.dp,
                            modifier = Modifier.size(210.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }

            if (destination is DetailDestination.ArtistDetail) {
                val artistAlbums = allAlbums.filter { it.artist.equals(destination.artist.name, ignoreCase = true) }
                if (artistAlbums.isNotEmpty()) {
                    item {
                        Text("Albums", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp))
                        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(artistAlbums, key = { "${it.id}_${it.name}" }) { album ->
                                Column(modifier = Modifier.width(126.dp).clickable { onAlbumClick(album) }) {
                                    ArtworkImage(
                                        albumId = album.id,
                                        artistName = album.artist,
                                        albumName = album.name,
                                        artUri = album.artUri,
                                        sampleSongUri = album.sampleSongUri,
                                        artworkLoader = artworkLoader,
                                        preferEmbedded = settings.preferEmbeddedArt,
                                        cacheEpoch = artCacheEpoch,
                                        shape = RoundedCornerShape(14.dp),
                                        targetSizePx = 240,
                                        modifier = Modifier.size(126.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(album.name, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            if (detailSongs.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (extras.liquidGlassEnabled) {
                            LiquidGlassSurface(
                                shape = CircleShape,
                                variant = GlassVariant.PROMINENT,
                                elevation = 5.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                onClick = { onPlaySongs(detailSongs, 0, false) }
                            ) {
                                Row(modifier = Modifier.align(Alignment.Center), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = KnightIcons.Play, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Play All", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                            LiquidGlassSurface(
                                shape = CircleShape,
                                variant = GlassVariant.PILL,
                                elevation = 4.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                onClick = { onPlaySongs(detailSongs, 0, true) }
                            ) {
                                Row(modifier = Modifier.align(Alignment.Center), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = KnightIcons.Shuffle, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Shuffle", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        } else {
                            Button(onClick = { onPlaySongs(detailSongs, 0, false) }, shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f)) {
                                Icon(imageVector = KnightIcons.Play, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Play All")
                            }
                            FilledTonalButton(onClick = { onPlaySongs(detailSongs, 0, true) }, shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f)) {
                                Icon(imageVector = KnightIcons.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Shuffle")
                            }
                        }
                    }
                }
                itemsIndexed(detailSongs, key = { idx, s -> "${s.id}_$idx" }) { index, song ->
                    if (extras.liquidGlassEnabled) {
                        GroupedLiquidGlassItem(
                            isFirst = index == 0,
                            isLast = index == detailSongs.lastIndex,
                            dividerStartPadding = 74.dp
                        ) {
                            SongRowItem(
                                song = song,
                                isCurrent = song.id == currentSongId,
                                isPlaying = isPlaying && song.id == currentSongId,
                                artworkLoader = artworkLoader,
                                preferEmbeddedArt = settings.preferEmbeddedArt,
                                artCacheEpoch = artCacheEpoch,
                                inGroupedBox = true,
                                onClick = { onPlaySongs(detailSongs, index, false) },
                                onMoreClick = { onMoreSongClick(song, removablePlaylistId) }
                            )
                        }
                    } else {
                        SongRowItem(
                            song = song,
                            isCurrent = song.id == currentSongId,
                            isPlaying = isPlaying && song.id == currentSongId,
                            artworkLoader = artworkLoader,
                            preferEmbeddedArt = settings.preferEmbeddedArt,
                            artCacheEpoch = artCacheEpoch,
                            onClick = { onPlaySongs(detailSongs, index, false) },
                            onMoreClick = { onMoreSongClick(song, removablePlaylistId) }
                        )
                    }
                }
            } else {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text("No songs in this collection yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun SongRowItem(
    song: Song,
    isCurrent: Boolean,
    isPlaying: Boolean,
    artworkLoader: ArtworkLoader,
    preferEmbeddedArt: Boolean,
    artCacheEpoch: Int,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    inGroupedBox: Boolean = false
) {
    val extras = LocalKnightExtras.current
    val rowContent: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = if (inGroupedBox || extras.liquidGlassEnabled) 12.dp else 16.dp,
                    end = if (inGroupedBox || extras.liquidGlassEnabled) 4.dp else 6.dp,
                    top = 8.dp,
                    bottom = 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ArtworkImage(
                albumId = song.albumId,
                artistName = song.artist,
                albumName = song.album,
                artUri = song.albumArtUri,
                sampleSongUri = song.contentUri,
                artworkLoader = artworkLoader,
                preferEmbedded = preferEmbeddedArt,
                cacheEpoch = artCacheEpoch,
                shape = RoundedCornerShape(12.dp),
                targetSizePx = 150,
                iconSize = 20.dp,
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${song.artist} • ${song.album}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (isCurrent) {
                PlayingEqualizerBars(isPlaying = isPlaying)
                Spacer(modifier = Modifier.width(6.dp))
            } else {
                Text(formatDuration(song.durationMs), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onMoreClick) {
                Icon(imageVector = KnightIcons.MoreDots, contentDescription = "Song options", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        }
    }

    if (inGroupedBox) {
        val currentPillBg = if (isCurrent) {
            MaterialTheme.colorScheme.primary.copy(alpha = if (extras.isDark) 0.16f else 0.12f)
        } else {
            Color.Transparent
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isCurrent) {
                        Modifier
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(currentPillBg)
                    } else {
                        Modifier
                    }
                )
                .clickable(onClick = onClick)
        ) {
            rowContent()
        }
    } else if (extras.liquidGlassEnabled) {
        LiquidGlassSurface(
            shape = RoundedCornerShape(18.dp),
            variant = if (isCurrent) GlassVariant.PILL else GlassVariant.CARD,
            elevation = if (isCurrent) 5.dp else 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 3.5.dp),
            onClick = onClick
        ) {
            rowContent()
        }
    } else {
        val bg = if (isCurrent) {
            if (extras.isOled) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            }
        } else {
            Color.Transparent
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bg)
                .clickable(onClick = onClick)
        ) {
            rowContent()
        }
    }
}

@Composable
private fun EmptyLibraryState(onRescan: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = KnightIcons.Songs, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp))
        Spacer(modifier = Modifier.height(14.dp))
        Text("No Local Music Found", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Copy MP3, FLAC, M4A, or OGG tracks to your phone's Music folder, then scan again.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(18.dp))
        Button(onClick = onRescan, shape = RoundedCornerShape(16.dp)) { Text("Rescan Local Storage") }
    }
}

@Composable
private fun PlaylistNameDialog(
    title: String,
    initialName: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialName) }
    KnightDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Playlist name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text.trim()) }, enabled = text.isNotBlank()) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
