package com.knightwin.music.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knightwin.music.data.media.ArtworkLoader
import com.knightwin.music.data.model.Album
import com.knightwin.music.data.model.AppSettings
import com.knightwin.music.data.model.GreeterMode
import com.knightwin.music.data.model.PlaybackUiState
import com.knightwin.music.data.model.Song
import com.knightwin.music.ui.components.ArtworkImage
import com.knightwin.music.ui.components.HeaderLogoAvatar
import com.knightwin.music.ui.components.PlayingEqualizerBars
import com.knightwin.music.ui.icons.KnightIcons
import com.knightwin.music.ui.theme.GlassVariant
import com.knightwin.music.ui.theme.KnightDialog
import com.knightwin.music.ui.theme.LiquidGlassSurface
import com.knightwin.music.ui.theme.LocalKnightExtras
import com.knightwin.music.ui.theme.verticalEdgeFade
import java.util.Calendar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeTabContent(
    songs: List<Song>,
    recentlyAddedSongs: List<Song>,
    albums: List<Album>,
    suggestedSongs: List<Song>,
    isScanning: Boolean,
    playbackState: PlaybackUiState,
    settings: AppSettings,
    randomizedGreeter: String,
    artworkLoader: ArtworkLoader,
    artCacheEpoch: Int,
    bottomPadding: PaddingValues,
    onPlaySongs: (List<Song>, Int, Boolean) -> Unit,
    onResumeOrPlayAll: () -> Unit,
    onAlbumClick: (Album) -> Unit,
    onMoreSongClick: (Song) -> Unit,
    onSeeAllRecent: () -> Unit,
    onSeeAllAlbums: () -> Unit,
    onRerollGreeter: () -> Unit,
    onRefreshSuggestions: () -> Unit,
    onSetGreeterMode: (GreeterMode) -> Unit,
    onSetCustomGreeterText: (String) -> Unit,
    onSetCustomLogoUri: (Uri) -> Unit,
    onResetCustomLogo: () -> Unit,
    onRescan: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val extras = LocalKnightExtras.current
    var showGreeterEditor by remember { mutableStateOf(false) }
    var showLogoDialog by remember { mutableStateOf(false) }

    val logoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onSetCustomLogoUri(uri)
        }
    }

    val timeGreeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..4 -> "Late night"
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else -> "Good night"
        }
    }

    val activeGreeterTitle = remember(settings.greeterMode, settings.customGreeterText, randomizedGreeter) {
        if (settings.greeterMode == GreeterMode.CUSTOM && settings.customGreeterText.isNotBlank()) {
            settings.customGreeterText
        } else {
            randomizedGreeter
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Sticky Home Top Header (stays still while scrolling)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .combinedClickable(
                        onClick = {
                            if (settings.greeterMode == GreeterMode.RANDOMIZED) {
                                onRerollGreeter()
                            } else {
                                showGreeterEditor = true
                            }
                        },
                        onLongClick = { showGreeterEditor = true }
                    )
            ) {
                Text(
                    text = timeGreeting,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(1.dp))
                AnimatedContent(
                    targetState = activeGreeterTitle,
                    transitionSpec = {
                        (fadeIn() + slideInVertically { it / 3 }) togetherWith
                            (fadeOut() + slideOutVertically { -it / 3 })
                    },
                    label = "greeterTitle"
                ) { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 31.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
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

            Spacer(modifier = Modifier.width(8.dp))

            HeaderLogoAvatar(
                customLogoPath = settings.customLogoPath,
                contentDescription = "Customize Header Picture",
                modifier = Modifier
                    .size(39.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { showLogoDialog = true }
            )
        }

        // Scrollable Home content with soft edge fade directly under sticky header (both LiquidUI & Material 3)
        if (isScanning && songs.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Scanning local audio library...", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else if (songs.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = KnightIcons.Songs,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No Local Music Found", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Copy MP3, FLAC, M4A, or OGG tracks to your phone's Music folder, then scan again.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRescan, shape = CircleShape) {
                        Text("Rescan Local Storage")
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = 14.dp,
                    bottom = bottomPadding.calculateBottomPadding() + 8.dp
                ),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalEdgeFade(topFade = 14.dp, bottomFade = 16.dp)
            ) {
                // Shuffle All & Play Action Pills
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (extras.liquidGlassEnabled) {
                    LiquidGlassSurface(
                        shape = CircleShape,
                        variant = GlassVariant.PROMINENT,
                        elevation = 6.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        onClick = { onPlaySongs(songs, 0, true) }
                    ) {
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = KnightIcons.Shuffle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Shuffle All",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    LiquidGlassSurface(
                        shape = CircleShape,
                        variant = GlassVariant.PILL,
                        elevation = 4.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        onClick = onResumeOrPlayAll
                    ) {
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (playbackState.isPlaying) KnightIcons.Pause else KnightIcons.Play,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (playbackState.isPlaying) "Pause" else "Play",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = { onPlaySongs(songs, 0, true) },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = KnightIcons.Shuffle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Shuffle All",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    OutlinedButton(
                        onClick = onResumeOrPlayAll,
                        shape = CircleShape,
                        border = BorderStroke(
                            1.dp,
                            if (extras.isOled) {
                                MaterialTheme.colorScheme.outlineVariant
                            } else {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f)
                            }
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (extras.isOled) Color.Black else MaterialTheme.colorScheme.surfaceContainerLow,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = if (playbackState.isPlaying) KnightIcons.Pause else KnightIcons.Play,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (playbackState.isPlaying) "Pause" else "Play",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Recently Added Carousel
        if (recentlyAddedSongs.isNotEmpty()) {
            item {
                HomeSectionHeader(
                    title = "Recently Added",
                    actionLabel = "See all",
                    onAction = onSeeAllRecent
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(recentlyAddedSongs, key = { _, song -> "recent_${song.id}" }) { index, song ->
                        if (extras.liquidGlassEnabled) {
                            LiquidGlassSurface(
                                shape = RoundedCornerShape(20.dp),
                                variant = GlassVariant.CARD,
                                elevation = 4.dp,
                                modifier = Modifier.width(148.dp),
                                onClick = { onPlaySongs(recentlyAddedSongs, index, false) },
                                onLongClick = { onMoreSongClick(song) }
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    ArtworkImage(
                                        albumId = song.albumId,
                                        artistName = song.artist,
                                        albumName = song.album,
                                        artUri = song.albumArtUri,
                                        sampleSongUri = song.contentUri,
                                        artworkLoader = artworkLoader,
                                        preferEmbedded = settings.preferEmbeddedArt,
                                        cacheEpoch = artCacheEpoch,
                                        shape = RoundedCornerShape(14.dp),
                                        targetSizePx = 300,
                                        iconSize = 30.dp,
                                        modifier = Modifier.size(132.dp)
                                    )
                                    Spacer(modifier = Modifier.height(7.dp))
                                    Text(
                                        text = song.title,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.5.sp
                                        ),
                                        color = if (song.id == playbackState.currentSong?.id) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = song.artist,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .width(138.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .combinedClickable(
                                        onClick = { onPlaySongs(recentlyAddedSongs, index, false) },
                                        onLongClick = { onMoreSongClick(song) }
                                    )
                            ) {
                                ArtworkImage(
                                    albumId = song.albumId,
                                    artistName = song.artist,
                                    albumName = song.album,
                                    artUri = song.albumArtUri,
                                    sampleSongUri = song.contentUri,
                                    artworkLoader = artworkLoader,
                                    preferEmbedded = settings.preferEmbeddedArt,
                                    cacheEpoch = artCacheEpoch,
                                    shape = RoundedCornerShape(16.dp),
                                    targetSizePx = 320,
                                    iconSize = 32.dp,
                                    modifier = Modifier.size(138.dp)
                                )
                                Spacer(modifier = Modifier.height(7.dp))
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.5.sp
                                    ),
                                    color = if (song.id == playbackState.currentSong?.id) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = song.artist,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Albums Carousel
        if (albums.isNotEmpty()) {
            item {
                HomeSectionHeader(
                    title = "Albums",
                    actionLabel = "See all",
                    onAction = onSeeAllAlbums
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(albums.take(16), key = { "home_album_${it.id}_${it.name}" }) { album ->
                        if (extras.liquidGlassEnabled) {
                            LiquidGlassSurface(
                                shape = RoundedCornerShape(22.dp),
                                variant = GlassVariant.CARD,
                                elevation = 4.dp,
                                modifier = Modifier.width(166.dp),
                                onClick = { onAlbumClick(album) }
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    ArtworkImage(
                                        albumId = album.id,
                                        artistName = album.artist,
                                        albumName = album.name,
                                        artUri = album.artUri,
                                        sampleSongUri = album.sampleSongUri,
                                        artworkLoader = artworkLoader,
                                        preferEmbedded = settings.preferEmbeddedArt,
                                        cacheEpoch = artCacheEpoch,
                                        shape = RoundedCornerShape(16.dp),
                                        targetSizePx = 340,
                                        iconSize = 34.dp,
                                        modifier = Modifier.size(150.dp)
                                    )
                                    Spacer(modifier = Modifier.height(7.dp))
                                    Text(
                                        text = album.name,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.5.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = album.artist,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .width(156.dp)
                                    .clickable { onAlbumClick(album) }
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
                                    shape = RoundedCornerShape(18.dp),
                                    targetSizePx = 360,
                                    iconSize = 36.dp,
                                    modifier = Modifier.size(156.dp)
                                )
                                Spacer(modifier = Modifier.height(7.dp))
                                Text(
                                    text = album.name,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.5.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = album.artist,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Songs You Might Like
        if (suggestedSongs.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 10.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Songs you might like",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (extras.liquidGlassEnabled) {
                        LiquidGlassSurface(
                            shape = CircleShape,
                            variant = GlassVariant.PILL,
                            elevation = 3.dp,
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .size(36.dp),
                            onClick = onRefreshSuggestions
                        ) {
                            Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = KnightIcons.Refresh,
                                    contentDescription = "Refresh suggestions",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    } else {
                        IconButton(onClick = onRefreshSuggestions) {
                            Icon(
                                imageVector = KnightIcons.Refresh,
                                contentDescription = "Refresh suggestions",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }
                }
            }

            if (extras.liquidGlassEnabled) {
                item(key = "suggested_group") {
                    LiquidGlassSurface(
                        shape = RoundedCornerShape(24.dp),
                        variant = GlassVariant.CARD,
                        elevation = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            suggestedSongs.forEachIndexed { index, song ->
                                val isCurrent = song.id == playbackState.currentSong?.id
                                val rowClickModifier = Modifier.combinedClickable(
                                    onClick = { onPlaySongs(suggestedSongs, index, false) },
                                    onLongClick = { onMoreSongClick(song) }
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .then(
                                            if (isCurrent) {
                                                Modifier
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(
                                                        MaterialTheme.colorScheme.primary.copy(
                                                            alpha = if (extras.isDark) 0.16f else 0.12f
                                                        )
                                                    )
                                            } else {
                                                Modifier
                                            }
                                        )
                                        .then(rowClickModifier)
                                ) {
                                    SuggestedSongRowInner(
                                        song = song,
                                        isCurrent = isCurrent,
                                        isPlaying = playbackState.isPlaying,
                                        artworkLoader = artworkLoader,
                                        preferEmbeddedArt = settings.preferEmbeddedArt,
                                        artCacheEpoch = artCacheEpoch,
                                        liquidMode = true,
                                        onPlay = { onPlaySongs(suggestedSongs, index, false) },
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                                if (index < suggestedSongs.lastIndex) {
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
                itemsIndexed(suggestedSongs, key = { idx, song -> "suggest_${song.id}_$idx" }) { index, song ->
                    val isCurrent = song.id == playbackState.currentSong?.id
                    val rowClickModifier = Modifier.combinedClickable(
                        onClick = { onPlaySongs(suggestedSongs, index, false) },
                        onLongClick = { onMoreSongClick(song) }
                    )
                    val bg = if (isCurrent) {
                        if (extras.isOled) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        } else {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
                        }
                    } else {
                        Color.Transparent
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bg)
                            .then(rowClickModifier)
                            .padding(start = 18.dp, end = 10.dp, top = 7.dp, bottom = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SuggestedSongRowInner(
                            song = song,
                            isCurrent = isCurrent,
                            isPlaying = playbackState.isPlaying,
                            artworkLoader = artworkLoader,
                            preferEmbeddedArt = settings.preferEmbeddedArt,
                            artCacheEpoch = artCacheEpoch,
                            liquidMode = false,
                            onPlay = { onPlaySongs(suggestedSongs, index, false) }
                        )
                    }
                }
            }
            }
            }
        }
    }

    if (showLogoDialog) {
        HeaderLogoDialog(
            customLogoPath = settings.customLogoPath,
            onChoosePicture = {
                logoPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onResetToDefault = {
                onResetCustomLogo()
            },
            onEditGreeter = {
                showLogoDialog = false
                showGreeterEditor = true
            },
            onDismiss = { showLogoDialog = false }
        )
    }

    if (showGreeterEditor) {
        HomeGreeterDialog(
            currentMode = settings.greeterMode,
            currentCustomText = settings.customGreeterText,
            currentRandomPreview = randomizedGreeter,
            onRerollPreview = onRerollGreeter,
            onSave = { mode, custom ->
                onSetGreeterMode(mode)
                onSetCustomGreeterText(custom)
                showGreeterEditor = false
            },
            onDismiss = { showGreeterEditor = false }
        )
    }
}

@Composable
private fun SuggestedSongRowInner(
    song: Song,
    isCurrent: Boolean,
    isPlaying: Boolean,
    artworkLoader: ArtworkLoader,
    preferEmbeddedArt: Boolean,
    artCacheEpoch: Int,
    liquidMode: Boolean,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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
        } else if (liquidMode) {
            LiquidGlassSurface(
                shape = CircleShape,
                variant = GlassVariant.PILL,
                elevation = 2.dp,
                modifier = Modifier.size(34.dp),
                onClick = onPlay
            ) {
                Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = KnightIcons.Play,
                        contentDescription = "Play ${song.title}",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else {
            IconButton(onClick = onPlay) {
                Icon(
                    imageVector = KnightIcons.Play,
                    contentDescription = "Play ${song.title}",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun HomeSectionHeader(
    title: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 10.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        TextButton(onClick = onAction) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun HomeGreeterDialog(
    currentMode: GreeterMode,
    currentCustomText: String,
    currentRandomPreview: String,
    onRerollPreview: () -> Unit,
    onSave: (GreeterMode, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMode by remember(currentMode) { mutableStateOf(currentMode) }
    var draftText by remember(currentCustomText) { mutableStateOf(currentCustomText) }

    KnightDialog(
        onDismissRequest = onDismiss,
        title = { Text("Home Greeter", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Choose a fresh randomized music persona each launch or write your own personal greeter.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { selectedMode = GreeterMode.RANDOMIZED }
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedMode == GreeterMode.RANDOMIZED,
                        onClick = { selectedMode = GreeterMode.RANDOMIZED }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Randomized", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(
                            text = "Preview: \"$currentRandomPreview\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onRerollPreview) {
                        Icon(
                            imageVector = KnightIcons.Refresh,
                            contentDescription = "Roll another",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { selectedMode = GreeterMode.CUSTOM }
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedMode == GreeterMode.CUSTOM,
                        onClick = { selectedMode = GreeterMode.CUSTOM }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("Write Your Own", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(
                            text = "Always display your custom title on Home",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (selectedMode == GreeterMode.CUSTOM) {
                    OutlinedTextField(
                        value = draftText,
                        onValueChange = { if (it.length <= 36) draftText = it },
                        label = { Text("Custom greeter") },
                        placeholder = { Text("e.g. KnightWin, Music Lover") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(selectedMode, draftText) },
                enabled = selectedMode == GreeterMode.RANDOMIZED || draftText.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun HeaderLogoDialog(
    customLogoPath: String,
    onChoosePicture: () -> Unit,
    onResetToDefault: () -> Unit,
    onEditGreeter: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    KnightDialog(
        onDismissRequest = onDismiss,
        title = { Text("Header Picture", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HeaderLogoAvatar(
                    customLogoPath = customLogoPath,
                    contentDescription = "Header Avatar Preview",
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .border(2.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
                Text(
                    text = if (customLogoPath.isNotBlank()) "Custom picture active" else "Using default KnightWin logo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onChoosePicture,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (customLogoPath.isNotBlank()) "Change Picture" else "Choose Picture")
                }
                if (customLogoPath.isNotBlank()) {
                    OutlinedButton(
                        onClick = onResetToDefault,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset to KnightWin Logo")
                    }
                }
                if (onEditGreeter != null) {
                    TextButton(onClick = onEditGreeter) {
                        Text("Edit Home Greeter")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}
