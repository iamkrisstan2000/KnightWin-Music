package com.knightwin.music.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knightwin.music.data.media.ArtworkLoader
import com.knightwin.music.data.model.MainTab
import com.knightwin.music.data.model.NowPlayingStyle
import com.knightwin.music.data.model.PlaybackUiState
import com.knightwin.music.ui.icons.KnightIcons
import com.knightwin.music.ui.theme.GlassVariant
import com.knightwin.music.ui.theme.LiquidGlassSurface
import com.knightwin.music.ui.theme.LocalKnightExtras
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sin

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FloatingMiniPlayer(
    playbackState: PlaybackUiState,
    artworkLoader: ArtworkLoader,
    preferEmbeddedArt: Boolean,
    artCacheEpoch: Int,
    nowPlayingStyle: NowPlayingStyle,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
    playbackSpeed: Float = 1.0f
) {
    val song = playbackState.currentSong
    val extras = LocalKnightExtras.current

    AnimatedVisibility(
        visible = song != null,
        enter = slideInVertically { it / 2 } + fadeIn(),
        exit = slideOutVertically { it / 2 } + fadeOut(),
        modifier = modifier
    ) {
        if (song != null) {
            var dragSum by remember { mutableFloatStateOf(0f) }
            val rawProgress = if (playbackState.durationMs > 0L) {
                (playbackState.positionMs.toFloat() / playbackState.durationMs.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            }

            // Unbroken single-coroutine phase-locked progress loop (zero jumps on 250ms ticks)
            val latestRawProgress by rememberUpdatedState(rawProgress)
            val latestIsPlaying by rememberUpdatedState(playbackState.isPlaying)
            val latestDurationMs by rememberUpdatedState(playbackState.durationMs)
            val latestSpeed by rememberUpdatedState(playbackSpeed)

            var displayProgress by remember(song.id) { mutableFloatStateOf(rawProgress) }

            LaunchedEffect(song.id) {
                var lastNanos = withFrameNanos { it }
                while (isActive) {
                    withFrameNanos { now ->
                        val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                        lastNanos = now

                        val target = latestRawProgress
                        val diff = target - displayProgress

                        displayProgress = if (!latestIsPlaying) {
                            val k = 1f - exp(-12f * dt)
                            (displayProgress + diff * k).coerceIn(0f, 1f)
                        } else if (abs(diff) > 0.035f) {
                            val k = 1f - exp(-14f * dt)
                            (displayProgress + diff * k).coerceIn(0f, 1f)
                        } else {
                            val durSec = (latestDurationMs.coerceAtLeast(1000L)) / 1000f
                            val rate = latestSpeed.coerceIn(0.5f, 2f) / durSec
                            val advanced = displayProgress + rate * dt
                            val nudge = (target - advanced) * (dt * 2.5f).coerceAtMost(1f)
                            (advanced + nudge).coerceIn(0f, 1f)
                        }
                    }
                }
            }

            var spinDeg by remember { mutableFloatStateOf(0f) }
            val useSpin = nowPlayingStyle == NowPlayingStyle.SPINNING_DISC && playbackState.isPlaying
            LaunchedEffect(useSpin) {
                if (useSpin) {
                    var lastNanos = withFrameNanos { it }
                    while (isActive) {
                        withFrameNanos { now ->
                            val dt = (now - lastNanos) / 1_000_000_000f
                            lastNanos = now
                            spinDeg = (spinDeg + dt * 36f) % 360f
                        }
                    }
                }
            }

            LiquidGlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .pointerInput(song.id) {
                        detectHorizontalDragGestures(
                            onDragStart = { dragSum = 0f },
                            onHorizontalDrag = { _, dragAmount -> dragSum += dragAmount },
                            onDragEnd = {
                                if (dragSum < -70f) onSkipNext()
                                else if (dragSum > 70f) onSkipPrevious()
                                dragSum = 0f
                            }
                        )
                    },
                shape = RoundedCornerShape(24.dp),
                variant = GlassVariant.DOCK,
                elevation = 10.dp,
                onClick = onExpand
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    MiniPlayerLiquidFill(
                        progress = displayProgress,
                        isPlaying = playbackState.isPlaying,
                        liquidMode = extras.liquidGlassEnabled,
                        modifier = Modifier.matchParentSize()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp, end = 8.dp, top = 9.dp, bottom = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val artShape = if (nowPlayingStyle == NowPlayingStyle.SPINNING_DISC) {
                            CircleShape
                        } else {
                            RoundedCornerShape(12.dp)
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
                            shape = artShape,
                            targetSizePx = 160,
                            iconSize = 20.dp,
                            modifier = Modifier
                                .size(44.dp)
                                .then(if (nowPlayingStyle == NowPlayingStyle.SPINNING_DISC) Modifier.rotate(spinDeg) else Modifier)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                modifier = Modifier.basicMarquee()
                            )
                            Text(
                                text = song.artist,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (extras.liquidGlassEnabled) {
                            LiquidGlassSurface(
                                shape = CircleShape,
                                variant = GlassVariant.PROMINENT,
                                elevation = 4.dp,
                                modifier = Modifier.size(36.dp),
                                onClick = onTogglePlayPause
                            ) {
                                Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (playbackState.isPlaying) KnightIcons.Pause else KnightIcons.Play,
                                        contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        } else {
                            IconButton(
                                onClick = onTogglePlayPause,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = if (playbackState.isPlaying) KnightIcons.Pause else KnightIcons.Play,
                                    contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onSkipNext,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = KnightIcons.SkipNext,
                                contentDescription = "Next",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniPlayerLiquidFill(
    progress: Float,
    isPlaying: Boolean,
    liquidMode: Boolean,
    modifier: Modifier = Modifier
) {
    val extras = LocalKnightExtras.current
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary

    var phase1 by remember { mutableFloatStateOf(0f) }
    var phase2 by remember { mutableFloatStateOf(0f) }
    var phaseBack by remember { mutableFloatStateOf(0f) }
    var causticPhase by remember { mutableFloatStateOf(0f) }

    val flowFactor by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = tween(durationMillis = 480),
        label = "liquidFlowFactor"
    )
    val latestFlowFactor by rememberUpdatedState(flowFactor)

    LaunchedEffect(liquidMode) {
        if (!liquidMode) return@LaunchedEffect
        var lastNanos = withFrameNanos { it }
        val twoPi = (2.0 * PI).toFloat()
        while (isActive) {
            withFrameNanos { now ->
                val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                lastNanos = now
                val f = latestFlowFactor
                if (f > 0.001f) {
                    val effDt = dt * f
                    phase1 = (phase1 + effDt * 1.65f) % twoPi
                    phase2 = (phase2 + effDt * 2.28f) % twoPi
                    phaseBack = (phaseBack + effDt * 1.38f) % twoPi
                    causticPhase = (causticPhase + effDt * 0.23f) % 1f
                }
            }
        }
    }

    val backPath = remember { Path() }
    val mainPath = remember { Path() }
    val meniscusPath = remember { Path() }

    Canvas(modifier = modifier) {
        val p = progress.coerceIn(0f, 1f)
        if (p <= 0.002f) return@Canvas
        val w = size.width
        val h = size.height
        if (w <= 1f || h <= 1f) return@Canvas

        if (!liquidMode) {
            val fillAlpha = when {
                extras.isOled -> 0.24f
                extras.isDark -> 0.20f
                else -> 0.14f
            }
            if (p >= 0.996f) {
                drawRect(color = primary.copy(alpha = fillAlpha))
            } else {
                val fillX = (w * p).coerceIn(1f, w)
                val featherPx = minOf(16.dp.toPx(), fillX * 0.45f)
                val solidEnd = ((fillX - featherPx) / fillX).coerceIn(0f, 0.98f)
                drawRect(
                    brush = Brush.horizontalGradient(
                        0f to primary.copy(alpha = fillAlpha),
                        solidEnd to primary.copy(alpha = fillAlpha * 0.92f),
                        1f to Color.Transparent,
                        startX = 0f,
                        endX = fillX
                    ),
                    topLeft = Offset.Zero,
                    size = Size(fillX, h)
                )
            }
            return@Canvas
        }

        // LiquidUI: seamlessly looped multi-wave liquid slowly flowing until the end
        val fillX = w * p
        val edgeFactor = (p / 0.05f).coerceIn(0f, 1f) * ((1f - p) / 0.045f).coerceIn(0f, 1f)
        val ampMain = 5.5.dp.toPx() * edgeFactor
        val ampBack = 4.5.dp.toPx() * edgeFactor
        val backLead = 4.dp.toPx() * edgeFactor
        val step = 4f

        backPath.reset()
        mainPath.reset()
        meniscusPath.reset()

        backPath.moveTo(0f, 0f)
        mainPath.moveTo(0f, 0f)

        var y = 0f
        while (y <= h) {
            val bx = (fillX + backLead + sin(y * 0.055f - phaseBack) * ampBack).coerceIn(0f, w)
            val wave = (sin(y * 0.068f + phase1) * 0.70f + sin(y * 0.115f - phase2) * 0.30f) * ampMain
            val fx = (fillX + wave).coerceIn(0f, w)

            backPath.lineTo(bx, y)
            mainPath.lineTo(fx, y)
            if (y == 0f) meniscusPath.moveTo(fx, y) else meniscusPath.lineTo(fx, y)

            if (y >= h) break
            y = (y + step).coerceAtMost(h)
        }

        backPath.lineTo(0f, h)
        backPath.close()
        mainPath.lineTo(0f, h)
        mainPath.close()

        val backColor = lerp(primary, secondary, 0.45f).copy(
            alpha = if (extras.isDark) 0.14f else 0.10f
        )
        drawPath(path = backPath, color = backColor)

        val gradEndX = (fillX + ampMain).coerceAtLeast(1f)
        val mainBrush = Brush.horizontalGradient(
            0f to primary.copy(alpha = if (extras.isOled) 0.22f else if (extras.isDark) 0.20f else 0.14f),
            0.68f to lerp(primary, tertiary, 0.28f).copy(alpha = if (extras.isOled) 0.30f else if (extras.isDark) 0.28f else 0.20f),
            1f to lerp(primary, Color.White, 0.18f).copy(alpha = if (extras.isOled) 0.38f else if (extras.isDark) 0.36f else 0.26f),
            startX = 0f,
            endX = gradEndX
        )
        drawPath(path = mainPath, brush = mainBrush)

        clipPath(mainPath) {
            // Drifting translucent caustic shimmer inside the liquid
            val bandHalf = w * 0.24f
            val center = -bandHalf + causticPhase * (w + bandHalf * 2f)
            drawRect(
                brush = Brush.linearGradient(
                    0f to Color.Transparent,
                    0.5f to Color.White.copy(alpha = if (extras.isDark) 0.10f else 0.22f),
                    1f to Color.Transparent,
                    start = Offset(center - bandHalf, 0f),
                    end = Offset(center + bandHalf, h)
                )
            )
            // Soft top inner glass refraction highlight
            drawRect(
                brush = Brush.verticalGradient(
                    0f to Color.White.copy(alpha = if (extras.isDark) 0.12f else 0.24f),
                    0.45f to Color.Transparent
                )
            )
        }

        if (edgeFactor > 0.04f) {
            drawPath(
                path = meniscusPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.42f * edgeFactor),
                        primary.copy(alpha = 0.62f * edgeFactor),
                        Color.White.copy(alpha = 0.28f * edgeFactor)
                    )
                ),
                style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun KnightBottomNavBar(
    currentTab: MainTab,
    onSelectTab: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val extras = LocalKnightExtras.current
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    val swipeModifier = Modifier.pointerInput(currentTab) {
        detectHorizontalDragGestures(
            onDragStart = { dragAccumulator = 0f },
            onHorizontalDrag = { _, dragAmount -> dragAccumulator += dragAmount },
            onDragEnd = {
                val tabs = MainTab.entries
                val idx = tabs.indexOf(currentTab)
                if (dragAccumulator > 65f && idx < tabs.lastIndex) {
                    onSelectTab(tabs[idx + 1])
                } else if (dragAccumulator < -65f && idx > 0) {
                    onSelectTab(tabs[idx - 1])
                }
                dragAccumulator = 0f
            }
        )
    }

    if (extras.liquidGlassEnabled) {
        // LiquidUI ON: Floating frosted glass capsule dock
        Box(
            modifier = modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .then(swipeModifier)
        ) {
            LiquidGlassSurface(
                shape = RoundedCornerShape(30.dp),
                variant = GlassVariant.DOCK,
                elevation = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                BottomNavPillRow(
                    currentTab = currentTab,
                    onSelectTab = onSelectTab,
                    liquidMode = true,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                )
            }
        }
    } else {
        // LiquidUI OFF: Pure Material 3 docked bar with soft fade edge (no hard line)
        val navContainerColor = when {
            extras.isOled -> Color.Black
            extras.isDark -> MaterialTheme.colorScheme.surfaceContainerLow
            else -> MaterialTheme.colorScheme.surfaceContainerLowest
        }
        Column(
            modifier = modifier
                .fillMaxWidth()
                .then(swipeModifier)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                navContainerColor.copy(alpha = 0.55f),
                                navContainerColor
                            )
                        )
                    )
            )
            Surface(
                color = navContainerColor,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                BottomNavPillRow(
                    currentTab = currentTab,
                    onSelectTab = onSelectTab,
                    liquidMode = false,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun BottomNavPillRow(
    currentTab: MainTab,
    onSelectTab: (MainTab) -> Unit,
    liquidMode: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MainTab.entries.forEach { tab ->
            val selected = tab == currentTab
            val icon = when (tab) {
                MainTab.HOME -> if (selected) KnightIcons.HomeFilled else KnightIcons.HomeOutline
                MainTab.LIBRARY -> KnightIcons.Library
                MainTab.SEARCH -> KnightIcons.Search
            }

            if (liquidMode && selected) {
                LiquidGlassSurface(
                    shape = CircleShape,
                    variant = GlassVariant.PROMINENT,
                    elevation = 6.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelectTab(tab) }
                ) {
                    NavPillContent(
                        icon = icon,
                        label = tab.label,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        bold = true,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else {
                val pillColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    animationSpec = tween(200),
                    label = "navPillBg"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(200),
                    label = "navPillContent"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(CircleShape)
                        .background(pillColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelectTab(tab) },
                    contentAlignment = Alignment.Center
                ) {
                    NavPillContent(
                        icon = icon,
                        label = tab.label,
                        contentColor = contentColor,
                        bold = selected
                    )
                }
            }
        }
    }
}

@Composable
private fun NavPillContent(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    contentColor: Color,
    bold: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.padding(horizontal = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(19.dp)
        )
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 13.5.sp,
                fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium
            ),
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
