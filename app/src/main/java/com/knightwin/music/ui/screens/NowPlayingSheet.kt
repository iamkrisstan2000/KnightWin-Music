package com.knightwin.music.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import com.knightwin.music.data.media.ArtworkLoader
import com.knightwin.music.data.model.AppSettings
import com.knightwin.music.data.model.NowPlayingStyle
import com.knightwin.music.data.model.PlaybackUiState
import com.knightwin.music.data.model.Song
import com.knightwin.music.playback.AudioBeatMonitor
import com.knightwin.music.ui.components.ArtworkImage
import com.knightwin.music.ui.components.PlayingEqualizerBars
import com.knightwin.music.ui.components.formatDuration
import com.knightwin.music.ui.icons.KnightIcons
import com.knightwin.music.ui.theme.GlassVariant
import com.knightwin.music.ui.theme.KnightDialog
import com.knightwin.music.ui.theme.KnightModalSheet
import com.knightwin.music.ui.theme.LiquidGlassSurface
import com.knightwin.music.ui.theme.LocalKnightExtras
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NowPlayingScreen(
    playbackState: PlaybackUiState,
    settings: AppSettings,
    isFavorite: Boolean,
    artworkLoader: ArtworkLoader,
    artCacheEpoch: Int,
    onDismiss: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onJumpToQueueIndex: (Int) -> Unit,
    onRemoveFromQueue: (Int) -> Unit,
    onClearQueue: () -> Unit,
    onSetSleepTimerMinutes: (Int) -> Unit,
    onSetSleepEndOfTrack: () -> Unit,
    onCancelSleepTimer: () -> Unit
) {
    val song = playbackState.currentSong ?: return
    val extras = LocalKnightExtras.current
    val view = LocalView.current

    if (settings.keepScreenOn) {
        DisposableEffect(Unit) {
            view.keepScreenOn = true
            onDispose { view.keepScreenOn = false }
        }
    }

    var dominantColor by remember(song.albumId) { mutableStateOf(Color(0xFF27B1FE)) }
    val animatedDominant by animateColorAsState(
        targetValue = dominantColor,
        animationSpec = tween(600),
        label = "domColor"
    )

    var showQueueSheet by remember { mutableStateOf(false) }
    var showSleepDialog by remember { mutableStateOf(false) }

    val baseBg = if (extras.isOled) Color.Black else MaterialTheme.colorScheme.background
    val washAccent = if (extras.liquidGlassEnabled) {
        lerp(MaterialTheme.colorScheme.primary, animatedDominant, 0.55f)
    } else {
        animatedDominant
    }
    val bgModifier = if (extras.isOled) {
        Modifier.background(Color.Black)
    } else {
        val washTop = washAccent.copy(alpha = if (extras.isDark) 0.22f else 0.22f)
        val washMid = washAccent.copy(alpha = if (extras.isDark) 0.08f else 0.09f)
        Modifier.background(Brush.verticalGradient(colors = listOf(washTop, washMid, baseBg)))
    }

    val config = LocalConfiguration.current
    val isLandscape = config.screenWidthDp > config.screenHeightDp

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = baseBg,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(bgModifier)
                .statusBarsPadding()
                .navigationBarsPadding()
                .displayCutoutPadding()
        ) {
            val topBar: @Composable () -> Unit = {
                val pillSize = if (isLandscape) 36.dp else 40.dp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (extras.liquidGlassEnabled) {
                        LiquidGlassSurface(
                            shape = CircleShape,
                            variant = GlassVariant.PILL,
                            tintColor = washAccent,
                            elevation = 4.dp,
                            modifier = Modifier.size(pillSize),
                            onClick = onDismiss
                        ) {
                            Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = KnightIcons.ChevronDown,
                                    contentDescription = "Minimize",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(pillSize)) {
                            Icon(
                                imageVector = KnightIcons.ChevronDown,
                                contentDescription = "Minimize",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    if (isLandscape) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = "NOW PLAYING",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.2.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "  •  ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Text(
                                text = song.album,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "NOW PLAYING",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.3.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = song.album,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    val timerActive = playbackState.sleepTimerRemainingMs > 0L || playbackState.sleepTimerEndOfTrack
                    if (extras.liquidGlassEnabled) {
                        LiquidGlassSurface(
                            shape = CircleShape,
                            variant = if (timerActive) GlassVariant.PROMINENT else GlassVariant.PILL,
                            tintColor = if (timerActive) MaterialTheme.colorScheme.primary else washAccent,
                            elevation = 4.dp,
                            modifier = Modifier.size(pillSize),
                            onClick = { showSleepDialog = true }
                        ) {
                            Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = KnightIcons.SleepMoon,
                                    contentDescription = "Sleep Timer",
                                    tint = if (timerActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    } else {
                        IconButton(onClick = { showSleepDialog = true }, modifier = Modifier.size(pillSize)) {
                            Icon(
                                imageVector = KnightIcons.SleepMoon,
                                contentDescription = "Sleep Timer",
                                tint = if (timerActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }
                }
            }

            val titleAndActions: @Composable () -> Unit = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = if (isLandscape) {
                                MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            } else {
                                MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                            },
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee()
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${song.artist} — ${song.album}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) KnightIcons.HeartFilled else KnightIcons.HeartOutline,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(23.dp)
                        )
                    }

                    IconButton(onClick = { onAddToPlaylist(song) }) {
                        Icon(
                            imageVector = KnightIcons.Playlists,
                            contentDescription = "Add to Playlist",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            val seekbarSection: @Composable () -> Unit = {
                val duration = playbackState.durationMs.coerceAtLeast(1L)
                var isSeeking by remember { mutableStateOf(false) }
                var seekFraction by remember { mutableFloatStateOf(0f) }

                val currentFraction = if (isSeeking) {
                    seekFraction
                } else {
                    (playbackState.positionMs.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = currentFraction,
                        onValueChange = {
                            isSeeking = true
                            seekFraction = it
                        },
                        onValueChangeFinished = {
                            isSeeking = false
                            onSeekTo((seekFraction * duration).toLong())
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val shownPos = if (isSeeking) (seekFraction * duration).toLong() else playbackState.positionMs
                        Text(
                            text = formatDuration(shownPos),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (playbackState.sleepTimerRemainingMs > 0L) {
                            Text(
                                text = "Sleep in ${formatDuration(playbackState.sleepTimerRemainingMs)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else if (playbackState.sleepTimerEndOfTrack) {
                            Text(
                                text = "Stops after track",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = formatDuration(duration),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            val playerControlsCapsule: @Composable () -> Unit = {
                val playBtnSize = if (isLandscape) 54.dp else 66.dp
                val playIconSize = if (isLandscape) 26.dp else 32.dp
                val skipBtnSize = if (isLandscape) 42.dp else 48.dp
                val playerControlsRow: @Composable () -> Unit = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 10.dp,
                                vertical = if (isLandscape) 6.dp else 10.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = onToggleShuffle) {
                            Icon(
                                imageVector = KnightIcons.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (playbackState.shuffleEnabled) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(21.dp)
                            )
                        }

                        IconButton(onClick = onSkipPrevious, modifier = Modifier.size(skipBtnSize)) {
                            Icon(
                                imageVector = KnightIcons.SkipPrevious,
                                contentDescription = "Previous",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        if (extras.liquidGlassEnabled) {
                            LiquidGlassSurface(
                                shape = CircleShape,
                                variant = GlassVariant.PROMINENT,
                                tintColor = MaterialTheme.colorScheme.primary,
                                elevation = 6.dp,
                                modifier = Modifier.size(playBtnSize),
                                onClick = onTogglePlayPause
                            ) {
                                Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (playbackState.isPlaying) KnightIcons.Pause else KnightIcons.Play,
                                        contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(playIconSize)
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp,
                                modifier = Modifier
                                    .size(playBtnSize)
                                    .clip(CircleShape)
                                    .clickable(onClick = onTogglePlayPause)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (playbackState.isPlaying) KnightIcons.Pause else KnightIcons.Play,
                                        contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(playIconSize)
                                    )
                                }
                            }
                        }

                        IconButton(onClick = onSkipNext, modifier = Modifier.size(skipBtnSize)) {
                            Icon(
                                imageVector = KnightIcons.SkipNext,
                                contentDescription = "Next",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        val repeatIcon = if (playbackState.repeatMode == Player.REPEAT_MODE_ONE) {
                            KnightIcons.RepeatOne
                        } else {
                            KnightIcons.Repeat
                        }
                        val repeatActive = playbackState.repeatMode != Player.REPEAT_MODE_OFF
                        IconButton(onClick = onCycleRepeat) {
                            Icon(
                                imageVector = repeatIcon,
                                contentDescription = "Repeat",
                                tint = if (repeatActive) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }
                }

                if (extras.liquidGlassEnabled) {
                    LiquidGlassSurface(
                        shape = RoundedCornerShape(36.dp),
                        variant = GlassVariant.DOCK,
                        tintColor = washAccent,
                        elevation = 14.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        playerControlsRow()
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(36.dp),
                        color = if (extras.isOled) Color.Black else MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        border = null,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        playerControlsRow()
                    }
                }
            }

            val shuffleMessages = remember {
                listOf(
                    "Shuffling Music",
                    "Randomizing Songs",
                    "Guess what's next!",
                    "Generating next song...",
                    "Shuffling, please wait..."
                )
            }
            val shuffleMessage = remember(playbackState.shuffleEnabled, playbackState.currentSong?.id) {
                shuffleMessages.random()
            }

            val nextSong: Song? = remember(
                playbackState.shuffleEnabled,
                playbackState.repeatMode,
                playbackState.queue,
                playbackState.currentQueueIndex,
                playbackState.currentSong
            ) {
                if (playbackState.shuffleEnabled || playbackState.queue.isEmpty()) {
                    null
                } else when (playbackState.repeatMode) {
                    Player.REPEAT_MODE_ONE -> playbackState.currentSong
                    Player.REPEAT_MODE_ALL -> {
                        val idx = playbackState.currentQueueIndex
                        if (idx >= 0) playbackState.queue[(idx + 1) % playbackState.queue.size] else playbackState.queue.firstOrNull()
                    }
                    else -> playbackState.queue.getOrNull(playbackState.currentQueueIndex + 1)
                }
            }

            val queueCardContent: @Composable () -> Unit = {
                val thumbSize = if (isLandscape) 36.dp else 42.dp
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 14.dp,
                            vertical = if (isLandscape) 7.dp else 10.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when {
                        playbackState.shuffleEnabled -> {
                            Box(
                                modifier = Modifier
                                    .size(thumbSize)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = KnightIcons.Shuffle,
                                    contentDescription = "Shuffle On",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        nextSong != null -> {
                            ArtworkImage(
                                albumId = nextSong.albumId,
                                artistName = nextSong.artist,
                                albumName = nextSong.album,
                                artUri = nextSong.albumArtUri,
                                sampleSongUri = nextSong.contentUri,
                                artworkLoader = artworkLoader,
                                preferEmbedded = settings.preferEmbeddedArt,
                                cacheEpoch = artCacheEpoch,
                                shape = RoundedCornerShape(10.dp),
                                targetSizePx = 128,
                                iconSize = 16.dp,
                                modifier = Modifier.size(thumbSize)
                            )
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .size(thumbSize)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = KnightIcons.Queue,
                                    contentDescription = "Queue",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        val eyebrow = when {
                            playbackState.shuffleEnabled -> "SHUFFLE ON"
                            playbackState.repeatMode == Player.REPEAT_MODE_ONE -> "REPEATING TRACK"
                            nextSong != null -> "UP NEXT"
                            else -> "QUEUE"
                        }
                        val mainLine = when {
                            playbackState.shuffleEnabled -> shuffleMessage
                            nextSong != null -> nextSong.title
                            else -> "End of Queue"
                        }
                        val subLine: String? = when {
                            playbackState.shuffleEnabled -> null
                            nextSong != null -> nextSong.artist
                            else -> "Tap to view queue"
                        }

                        Text(
                            text = eyebrow,
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 0.9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                        Text(
                            text = mainLine,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (subLine != null && !isLandscape) {
                            Text(
                                text = subLine,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = KnightIcons.Queue,
                            contentDescription = "Open Queue",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${playbackState.queue.size}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            val upNextQueueCard: @Composable () -> Unit = {
                val cardShape = RoundedCornerShape(if (isLandscape) 20.dp else 24.dp)
                if (extras.liquidGlassEnabled) {
                    LiquidGlassSurface(
                        shape = cardShape,
                        variant = GlassVariant.CARD,
                        tintColor = washAccent,
                        elevation = 6.dp,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { showQueueSheet = true }
                    ) {
                        queueCardContent()
                    }
                } else {
                    Surface(
                        shape = cardShape,
                        color = if (extras.isOled) Color.Black else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        border = null,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(cardShape)
                            .clickable { showQueueSheet = true }
                    ) {
                        queueCardContent()
                    }
                }
            }

            if (isLandscape) {
                // Landscape / Tablet: Artwork style on the left, song info + seekbar + controls + queue on the right
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    topBar()
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(0.45f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            NowPlayingArtworkCenterpiece(
                                song = song,
                                isPlaying = playbackState.isPlaying,
                                style = settings.nowPlayingStyle,
                                dominantColor = animatedDominant,
                                artworkLoader = artworkLoader,
                                preferEmbeddedArt = settings.preferEmbeddedArt,
                                artCacheEpoch = artCacheEpoch,
                                onDominantColor = { dominantColor = it },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(0.55f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Center
                        ) {
                            titleAndActions()
                            Spacer(modifier = Modifier.height(6.dp))
                            seekbarSection()
                            Spacer(modifier = Modifier.height(10.dp))
                            playerControlsCapsule()
                            Spacer(modifier = Modifier.height(10.dp))
                            upNextQueueCard()
                        }
                    }
                }
            } else {
                // Portrait: Classic stacked vertical centerpiece layout (height-safe on compact phones too)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    topBar()

                    Spacer(modifier = Modifier.weight(0.35f))

                    Box(
                        modifier = Modifier
                            .weight(4.5f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        NowPlayingArtworkCenterpiece(
                            song = song,
                            isPlaying = playbackState.isPlaying,
                            style = settings.nowPlayingStyle,
                            dominantColor = animatedDominant,
                            artworkLoader = artworkLoader,
                            preferEmbeddedArt = settings.preferEmbeddedArt,
                            artCacheEpoch = artCacheEpoch,
                            onDominantColor = { dominantColor = it },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.weight(0.45f))

                    titleAndActions()

                    Spacer(modifier = Modifier.height(14.dp))

                    seekbarSection()

                    Spacer(modifier = Modifier.height(18.dp))

                    playerControlsCapsule()

                    Spacer(modifier = Modifier.height(14.dp))

                    upNextQueueCard()
                }
            }
        }
    }

    if (showQueueSheet) {
        KnightModalSheet(onDismissRequest = { showQueueSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Queue (${playbackState.queue.size})", style = MaterialTheme.typography.titleLarge)
                        if (playbackState.shuffleEnabled) {
                            Text(
                                text = "Shuffling Music",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (playbackState.queue.size > 1) {
                        TextButton(onClick = onClearQueue) { Text("Clear Upcoming") }
                    }
                }
                HorizontalDivider()
                LazyColumn(modifier = Modifier.height(380.dp)) {
                    itemsIndexed(playbackState.queue) { index, qSong ->
                        val isCurrent = index == playbackState.currentQueueIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onJumpToQueueIndex(index) }
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ArtworkImage(
                                albumId = qSong.albumId,
                                artistName = qSong.artist,
                                albumName = qSong.album,
                                artUri = qSong.albumArtUri,
                                sampleSongUri = qSong.contentUri,
                                artworkLoader = artworkLoader,
                                preferEmbedded = settings.preferEmbeddedArt,
                                cacheEpoch = artCacheEpoch,
                                targetSizePx = 140,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = qSong.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = qSong.artist,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (isCurrent) {
                                PlayingEqualizerBars(isPlaying = playbackState.isPlaying)
                            } else {
                                IconButton(onClick = { onRemoveFromQueue(index) }) {
                                    Icon(
                                        imageVector = KnightIcons.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSleepDialog) {
        KnightDialog(
            onDismissRequest = { showSleepDialog = false },
            title = { Text("Sleep Timer") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(15, 30, 45, 60).forEach { mins ->
                        FilledTonalButton(
                            onClick = {
                                onSetSleepTimerMinutes(mins)
                                showSleepDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("$mins minutes") }
                    }
                    FilledTonalButton(
                        onClick = {
                            onSetSleepEndOfTrack()
                            showSleepDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("End of current track") }
                    if (playbackState.sleepTimerRemainingMs > 0L || playbackState.sleepTimerEndOfTrack) {
                        TextButton(
                            onClick = {
                                onCancelSleepTimer()
                                showSleepDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Turn Off Timer", color = MaterialTheme.colorScheme.error) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showSleepDialog = false }) { Text("Close") } }
        )
    }
}

@Composable
private fun NowPlayingArtworkCenterpiece(
    song: Song,
    isPlaying: Boolean,
    style: NowPlayingStyle,
    dominantColor: Color,
    artworkLoader: ArtworkLoader,
    preferEmbeddedArt: Boolean,
    artCacheEpoch: Int,
    onDominantColor: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val artScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.92f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 260f),
        label = "artScale"
    )

    val latestPlaying by rememberUpdatedState(isPlaying)
    val infinite = rememberInfiniteTransition(label = "npAnim")
    var discAngle by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(style) {
        if (style == NowPlayingStyle.SPINNING_DISC || style == NowPlayingStyle.CLASSIC_ART) {
            val maxSpeed = if (style == NowPlayingStyle.CLASSIC_ART) 34f else 26f
            var velocity = if (latestPlaying) maxSpeed else 0f
            var lastNanos = withFrameNanos { it }
            while (isActive) {
                if (!latestPlaying && velocity <= 0.05f) {
                    velocity = 0f
                    snapshotFlow { latestPlaying }.first { it }
                    lastNanos = withFrameNanos { it }
                }
                withFrameNanos { now ->
                    val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                    lastNanos = now
                    velocity = if (latestPlaying) {
                        (velocity + dt * 48f).coerceAtMost(maxSpeed)
                    } else {
                        (velocity - dt * 20f).coerceAtLeast(0f)
                    }
                    discAngle = (discAngle + dt * velocity) % 360f
                }
            }
        }
    }
    val vinylSlide by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.35f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "vinylCaseSlide"
    )
    val glowPulse by infinite.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(1300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glowPulse"
    )
    val rawBeatPulse by AudioBeatMonitor.beatPulse.collectAsStateWithLifecycle()
    val smoothBeat by animateFloatAsState(
        targetValue = if (isPlaying) rawBeatPulse else 0f,
        animationSpec = spring(dampingRatio = 0.60f, stiffness = 460f),
        label = "artworkBeatPulse"
    )

    when (style) {
        NowPlayingStyle.SPINNING_DISC -> {
            val extras = LocalKnightExtras.current
            val accentColor = MaterialTheme.colorScheme.primary
            val secondaryGlow = lerp(accentColor, dominantColor, 0.36f)
            val spindleBg = if (extras.isOled) {
                Color.Black
            } else {
                lerp(MaterialTheme.colorScheme.background, accentColor, 0.14f)
            }

            BoxWithConstraints(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                val discSide = minOf(maxWidth * 0.94f, maxHeight * 0.96f).coerceAtLeast(1.dp)
                Box(
                    modifier = Modifier.size(discSide),
                    contentAlignment = Alignment.Center
                ) {
                    // Diffused accented glow behind the Spinning CD pulsating to the live audio beat
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val baseBreath = if (isPlaying) (glowPulse - 0.88f) / 0.24f else 0f // 0..1 gentle fallback breath
                        val pulseStrength = (smoothBeat * 0.85f + baseBreath * 0.15f).coerceIn(0f, 1f)
                        val outerRadius = (size.minDimension * 0.5f) * (0.94f + 0.16f * pulseStrength)
                        val innerRadius = (size.minDimension * 0.5f) * (0.82f + 0.12f * smoothBeat)

                        val outerAlpha = if (isPlaying) {
                            (0.32f + 0.42f * pulseStrength).coerceIn(0f, 0.80f)
                        } else {
                            0.18f
                        }
                        val haloAlpha = if (isPlaying) {
                            (0.24f + 0.48f * smoothBeat).coerceIn(0f, 0.76f)
                        } else {
                            0.12f
                        }

                        // Wide diffused outer accent aura
                        drawCircle(
                            brush = Brush.radialGradient(
                                0.0f to secondaryGlow.copy(alpha = outerAlpha * 0.65f),
                                0.46f to accentColor.copy(alpha = outerAlpha),
                                0.72f to secondaryGlow.copy(alpha = outerAlpha * 0.42f),
                                0.90f to accentColor.copy(alpha = outerAlpha * 0.12f),
                                1.0f to Color.Transparent,
                                center = center,
                                radius = outerRadius
                            ),
                            radius = outerRadius,
                            center = center
                        )

                        // Punchy beat-reactive inner accent halo just outside the CD edge
                        drawCircle(
                            brush = Brush.radialGradient(
                                0.48f to accentColor.copy(alpha = haloAlpha * 0.82f),
                                0.74f to accentColor.copy(alpha = haloAlpha * 0.38f),
                                0.94f to secondaryGlow.copy(alpha = haloAlpha * 0.10f),
                                1.0f to Color.Transparent,
                                center = center,
                                radius = innerRadius
                            ),
                            radius = innerRadius,
                            center = center
                        )
                    }

                    // Spinning CD Disc: Full-bleed album art printed across the CD face
                    Box(
                        modifier = Modifier
                            .fillMaxSize(0.84f)
                            .scale(artScale * (1f + smoothBeat * 0.022f))
                            .shadow(18.dp, CircleShape)
                            .clip(CircleShape)
                            .border(2.dp, Color.White.copy(alpha = 0.48f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(discAngle)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
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
                                shape = CircleShape,
                                targetSizePx = 560,
                                iconSize = 54.dp,
                                onDominantColor = onDominantColor,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Subtle rotating iridescent polycarbonate prism sheen
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    brush = Brush.sweepGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0xFF7DF9FF).copy(alpha = 0.13f),
                                            Color.White.copy(alpha = 0.16f),
                                            Color(0xFFD6A8FF).copy(alpha = 0.12f),
                                            Color.Transparent,
                                            Color(0xFFFFE08A).copy(alpha = 0.12f),
                                            Color.White.copy(alpha = 0.15f),
                                            Color(0xFF8CFFD4).copy(alpha = 0.11f),
                                            Color.Transparent
                                        )
                                    )
                                )
                            }
                        }

                        // Stationary overhead glossy specular reflection & CD optical edge rings
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val rMax = size.minDimension / 2f
                            drawCircle(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.18f),
                                        Color.Transparent,
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.10f)
                                    )
                                )
                            )
                            // Outer polycarbonate rim bevel & subtle optical track rings
                            drawCircle(
                                color = Color.White.copy(alpha = 0.36f),
                                radius = rMax * 0.975f,
                                style = Stroke(width = 1.6.dp.toPx())
                            )
                            drawCircle(
                                color = Color.Black.copy(alpha = 0.18f),
                                radius = rMax * 0.955f,
                                style = Stroke(width = 1.dp.toPx())
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.24f),
                                radius = rMax * 0.34f,
                                style = Stroke(width = 1.dp.toPx())
                            )
                        }

                        // CD Transparent Polycarbonate Clamping Hub & Stacking Ring
                        Box(
                            modifier = Modifier
                                .fillMaxSize(0.305f)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFE8EEF8).copy(alpha = 0.42f),
                                            Color.White.copy(alpha = 0.56f),
                                            Color(0xFFB8C4D8).copy(alpha = 0.38f)
                                        )
                                    )
                                )
                                .border(1.4.dp, Color.White.copy(alpha = 0.72f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            // Inner raised CD stacking ring
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(0.72f)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFFCFD8E6).copy(alpha = 0.50f),
                                                Color.White.copy(alpha = 0.70f)
                                            )
                                        )
                                    )
                                    .border(1.2.dp, Color.Black.copy(alpha = 0.22f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                // Punched-out center CD spindle hole
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize(0.50f)
                                        .clip(CircleShape)
                                        .background(spindleBg)
                                        .border(1.6.dp, Color.White.copy(alpha = 0.75f), CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }

        NowPlayingStyle.CLASSIC_ART -> {
            // Vintage LP Cardboard Case on the left + Spinning Vinyl peeking out the right sleeve pocket
            BoxWithConstraints(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                val boxW = minOf(maxWidth * 0.96f, maxHeight * 1.24f).coerceAtLeast(1.dp)
                val boxH = (boxW / 1.28f).coerceAtLeast(1.dp)
                BoxWithConstraints(
                    modifier = Modifier.size(boxW, boxH),
                    contentAlignment = Alignment.CenterStart
                ) {
                    val caseSize = minOf(maxHeight * 0.92f, maxWidth * 0.66f)
                    val discSize = caseSize * 0.90f
                    val peekFraction = 0.25f + 0.21f * vinylSlide
                    val maxGroupWidth = caseSize + discSize * 0.46f
                    val sleeveLeft = ((maxWidth - maxGroupWidth) / 2f).coerceAtLeast(0.dp)
                    val discLeft = sleeveLeft + caseSize - discSize + (discSize * peekFraction)

                    // Spinning Vinyl Disc (tucked behind the right opening of the sleeve)
                    Box(
                        modifier = Modifier
                            .offset(x = discLeft)
                            .size(discSize)
                            .rotate(discAngle)
                            .shadow(12.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    colors = listOf(
                                        Color(0xFF0C0D12),
                                        Color(0xFF232734),
                                        Color(0xFF090A0E),
                                        Color(0xFF2A2F3E),
                                        Color(0xFF0C0D12)
                                    )
                                )
                            )
                            .border(1.5.dp, Color.White.copy(alpha = 0.14f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val rMax = size.minDimension / 2f
                            for (i in 1..8) {
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.055f),
                                    radius = rMax * (0.48f + i * 0.058f),
                                    style = Stroke(width = 1.1f)
                                )
                            }
                        }

                        ArtworkImage(
                            albumId = song.albumId,
                            artistName = song.artist,
                            albumName = song.album,
                            artUri = song.albumArtUri,
                            sampleSongUri = song.contentUri,
                            artworkLoader = artworkLoader,
                            preferEmbedded = preferEmbeddedArt,
                            cacheEpoch = artCacheEpoch,
                            shape = CircleShape,
                            targetSizePx = 240,
                            iconSize = 24.dp,
                            modifier = Modifier
                                .fillMaxSize(0.40f)
                                .border(2.dp, Color(0xFF14161D), CircleShape)
                        )

                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF12141A))
                                .border(1.5.dp, Color.White.copy(alpha = 0.50f), CircleShape)
                        )
                    }

                    // Vintage Vinyl Case / Cardboard LP Sleeve in foreground
                    Box(
                        modifier = Modifier
                            .offset(x = sleeveLeft)
                            .size(caseSize)
                            .shadow(16.dp, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
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
                            shape = RoundedCornerShape(8.dp),
                            targetSizePx = 520,
                            iconSize = 58.dp,
                            onDominantColor = onDominantColor,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Subtle vintage LP sleeve spine crease & worn ring impression
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = Color(0xFFF5EBD6).copy(alpha = 0.06f),
                                radius = size.minDimension * 0.44f,
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }

                        // Left spine groove
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(start = 7.dp)
                                .width(2.5.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.28f),
                                            Color(0xFFF5EBD6).copy(alpha = 0.14f)
                                        )
                                    )
                                )
                        )

                        // Right sleeve pocket shadow lip
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .width(6.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.36f)
                                        )
                                    )
                                )
                        )

                        // Vintage cardboard edge border
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(1.dp, Color(0xFFE8DEC8).copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        )
                    }
                }
            }
        }

        NowPlayingStyle.BEAT_GLOW -> {
            val accentColor = MaterialTheme.colorScheme.primary
            val secondaryGlow = lerp(accentColor, dominantColor, 0.36f)
            val baseBreath = if (isPlaying) (glowPulse - 0.88f) / 0.24f else 0f
            val pulseStrength = (smoothBeat * 0.85f + baseBreath * 0.15f).coerceIn(0f, 1f)
            val outerScale = if (isPlaying) 0.96f + 0.14f * pulseStrength else 0.90f
            val innerScale = if (isPlaying) 0.96f + 0.10f * smoothBeat else 0.92f
            val outerAlpha = if (isPlaying) (0.34f + 0.42f * pulseStrength).coerceIn(0f, 0.82f) else 0.20f
            val haloAlpha = if (isPlaying) (0.28f + 0.48f * smoothBeat).coerceIn(0f, 0.78f) else 0.14f

            BoxWithConstraints(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                val glowSide = minOf(maxWidth * 0.94f, maxHeight * 0.96f).coerceAtLeast(1.dp)
                Box(
                    modifier = Modifier.size(glowSide),
                    contentAlignment = Alignment.Center
                ) {
                    // Wide diffused square outer accent aura pulsating to the beat
                    Box(
                        modifier = Modifier
                            .fillMaxSize(0.78f)
                            .scale(outerScale)
                            .blur(38.dp, BlurredEdgeTreatment.Unbounded)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        accentColor.copy(alpha = outerAlpha),
                                        secondaryGlow.copy(alpha = outerAlpha * 0.92f),
                                        accentColor.copy(alpha = outerAlpha * 0.85f)
                                    )
                                ),
                                shape = RoundedCornerShape(34.dp)
                            )
                    )

                    // Punchy beat-reactive square inner accent rim glow hugging the cover edges
                    Box(
                        modifier = Modifier
                            .fillMaxSize(0.78f)
                            .scale(innerScale)
                            .blur(18.dp, BlurredEdgeTreatment.Unbounded)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        accentColor.copy(alpha = haloAlpha),
                                        secondaryGlow.copy(alpha = haloAlpha * 0.80f)
                                    )
                                ),
                                shape = RoundedCornerShape(30.dp)
                            )
                    )

                    // Elevated square cover framed by luminous glowing rim
                    ArtworkImage(
                        albumId = song.albumId,
                        artistName = song.artist,
                        albumName = song.album,
                        artUri = song.albumArtUri,
                        sampleSongUri = song.contentUri,
                        artworkLoader = artworkLoader,
                        preferEmbedded = preferEmbeddedArt,
                        cacheEpoch = artCacheEpoch,
                        shape = RoundedCornerShape(26.dp),
                        targetSizePx = 520,
                        iconSize = 64.dp,
                        onDominantColor = onDominantColor,
                        modifier = Modifier
                            .fillMaxSize(0.78f)
                            .scale(artScale * (1f + smoothBeat * 0.022f))
                            .shadow(
                                elevation = 20.dp,
                                shape = RoundedCornerShape(26.dp),
                                ambientColor = accentColor,
                                spotColor = secondaryGlow
                            )
                            .border(
                                width = 1.6.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        accentColor.copy(alpha = 0.85f),
                                        Color.White.copy(alpha = 0.45f),
                                        secondaryGlow.copy(alpha = 0.80f)
                                    )
                                ),
                                shape = RoundedCornerShape(26.dp)
                            )
                    )
                }
            }
        }
    }
}
