package com.knightwin.music.ui.screens

import android.content.Intent
import android.media.audiofx.AudioEffect
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knightwin.music.R
import com.knightwin.music.data.model.AccentTheme
import com.knightwin.music.data.model.AlbumLayout
import com.knightwin.music.data.model.AppSettings
import com.knightwin.music.data.model.ArtRefreshState
import com.knightwin.music.data.model.EqualizerPreset
import com.knightwin.music.data.model.GreeterMode
import com.knightwin.music.data.model.MinDurationFilter
import com.knightwin.music.data.model.NowPlayingStyle
import com.knightwin.music.data.model.ReverbPreset
import com.knightwin.music.data.model.SortOrder
import com.knightwin.music.data.model.ThemeMode
import com.knightwin.music.ui.components.HeaderLogoAvatar
import com.knightwin.music.ui.components.formatBytes
import com.knightwin.music.ui.components.formatSyncDate
import com.knightwin.music.ui.icons.KnightIcons
import com.knightwin.music.ui.theme.GlassVariant
import com.knightwin.music.ui.theme.KnightDialog
import com.knightwin.music.ui.theme.KnightModalSheet
import com.knightwin.music.ui.theme.LiquidAppBackdrop
import com.knightwin.music.ui.theme.LiquidGlassSurface
import com.knightwin.music.ui.theme.LocalKnightExtras
import com.knightwin.music.ui.theme.verticalEdgeFade
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

private enum class SettingsDialog {
    THEME_MODE,
    UI_STYLE,
    ACCENT_COLOR,
    HOME_GREETER,
    HEADER_LOGO,
    NOW_PLAYING_STYLE,
    ALBUM_LAYOUT,
    ARTIST_LAYOUT,
    SORT_ORDER,
    MIN_DURATION,
    CROSSFADE,
    PLAYBACK_SPEED,
    REVERB_PRESET,
    EQUALIZER,
    PRIVACY_OFFLINE,
    ABOUT_APP
}

private enum class SettingsCategory(
    val title: String,
    val shortLabel: String,
    val icon: ImageVector,
    val badgeTopColor: Color,
    val badgeBottomColor: Color
) {
    APPEARANCE(
        title = "Appearance & Theme",
        shortLabel = "Appearance",
        icon = KnightIcons.Palette,
        badgeTopColor = Color(0xFF4F8CFF),
        badgeBottomColor = Color(0xFF2563EB)
    ),
    AUDIO(
        title = "Audio & Playback",
        shortLabel = "Audio",
        icon = KnightIcons.Equalizer,
        badgeTopColor = Color(0xFFFB923C),
        badgeBottomColor = Color(0xFFEF4444)
    ),
    LIBRARY(
        title = "Local Library",
        shortLabel = "Library",
        icon = KnightIcons.FolderScan,
        badgeTopColor = Color(0xFF2DD4BF),
        badgeBottomColor = Color(0xFF0D9488)
    ),
    ARTWORK(
        title = "Album Art & Sync",
        shortLabel = "Album Art",
        icon = KnightIcons.ArtSync,
        badgeTopColor = Color(0xFF818CF8),
        badgeBottomColor = Color(0xFF4F46E5)
    ),
    ABOUT(
        title = "Privacy & About",
        shortLabel = "About",
        icon = KnightIcons.InfoCircle,
        badgeTopColor = Color(0xFF4ADE80),
        badgeBottomColor = Color(0xFF16A34A)
    )
}

/**
 * Adaptive Settings Screen:
 *  - Material UI: Samsung OneUI 8 Portrait + OneUI Tablet Two-Pane Split in Landscape
 *  - LiquidUI: Apple iOS 26/27 Liquid Glass Portrait + iPadOS Floating Sidebar Split in Landscape
 */
@Composable
fun OneUiSettingsScreen(
    settings: AppSettings,
    songCount: Int,
    albumCount: Int,
    audioSessionId: Int,
    artRefreshState: ArtRefreshState,
    artCacheSizeBytes: Long,
    randomizedGreeter: String,
    onBack: () -> Unit,
    onSetThemeMode: (ThemeMode) -> Unit,
    onSetOledBlack: (Boolean) -> Unit,
    onSetAccentTheme: (AccentTheme) -> Unit,
    onSetGreeterMode: (GreeterMode) -> Unit,
    onSetCustomGreeterText: (String) -> Unit,
    onRerollGreeter: () -> Unit,
    onSetCustomLogoUri: (Uri) -> Unit,
    onResetCustomLogo: () -> Unit,
    onSetNowPlayingStyle: (NowPlayingStyle) -> Unit,
    onSetAlbumLayout: (AlbumLayout) -> Unit,
    onSetArtistLayout: (AlbumLayout) -> Unit,
    onSetLiquidGlass: (Boolean) -> Unit,
    onSetSortOrder: (SortOrder) -> Unit,
    onSetMinDuration: (MinDurationFilter) -> Unit,
    onSetCrossfadeMs: (Int) -> Unit,
    onSetPlaybackSpeed: (Float) -> Unit,
    onSetReverbPreset: (ReverbPreset) -> Unit,
    onSetEqualizerPreset: (EqualizerPreset) -> Unit,
    onSetEqualizerCustomBands: (List<Float>) -> Unit,
    onSetKeepScreenOn: (Boolean) -> Unit,
    onSetPreferEmbeddedArt: (Boolean) -> Unit,
    onRescanLibrary: () -> Unit,
    onFetchMissingArt: () -> Unit,
    onRefreshAllArt: () -> Unit,
    onCancelArtFetch: () -> Unit,
    onClearArtStatus: () -> Unit,
    onClearDownloadedArt: () -> Unit,
    onDismissBanner: () -> Unit
) {
    BackHandler(onBack = onBack)

    val extras = LocalKnightExtras.current
    val context = LocalContext.current
    val config = LocalConfiguration.current
    val isLandscape = config.screenWidthDp > config.screenHeightDp

    val listState = rememberLazyListState()
    val detailListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val searchFocusRequester = remember { FocusRequester() }
    val logoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onSetCustomLogoUri(uri)
        }
    }

    val isCollapsed by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 140
        }
    }

    var selectedCategory by remember { mutableStateOf(SettingsCategory.APPEARANCE) }
    var searchQuery by remember { mutableStateOf("") }
    var activeDialog by remember { mutableStateOf<SettingsDialog?>(null) }
    val q = searchQuery.trim()
    fun matches(vararg keywords: String): Boolean {
        if (q.isEmpty()) return true
        return keywords.any { it.contains(q, ignoreCase = true) }
    }

    val primary = MaterialTheme.colorScheme.primary
    val showGroup1 = matches("appearance", "theme", "dark", "light", "oled", "amoled", "true-black", "black", "accent", "color", "dynamic", "matcha", "dessert", "greeter", "home", "welcome", "name", "random", "custom", "picture", "logo", "photo", "avatar", "now playing", "vinyl", "cd", "disc", "glow", "cover", "liquidui", "liquid ui", "liquid", "frosted", "glass", "material ui", "material", "m3", "ui style", "layout", "album", "artist", "artists", "grid")
    val showGroup2 = matches("audio", "playback", "crossfade", "speed", "tempo", "reverb", "room", "hall", "echo", "equalizer", "eq", "sound", "preset", "jazz", "techno", "blues", "pop", "rock", "opera", "screen", "keep screen", "awake")
    val showGroup3 = matches("library", "rescan", "scan", "refresh", "sort", "order", "title", "artist", "filter", "duration", "short", "ringtones", "ignore")
    val showGroup4 = matches("album art", "art", "cover", "fetch", "missing", "refresh", "redownload", "cache", "storage", "delete", "embedded", "prefer", "local", "offline", "online", "sync")
    val showGroup5 = matches("privacy", "offline", "network", "guarantee", "about", "knightwin", "owner", "license", "gpl", "version", "credits", "android")

    val greeterSummary = if (settings.greeterMode == GreeterMode.CUSTOM && settings.customGreeterText.isNotBlank()) {
        "Custom: \"${settings.customGreeterText}\""
    } else {
        "Randomized ($randomizedGreeter)"
    }
    val iosGreeterValue = if (settings.greeterMode == GreeterMode.CUSTOM && settings.customGreeterText.isNotBlank()) {
        settings.customGreeterText
    } else {
        "Randomized"
    }

    val showArtSuggestion = q.isEmpty() && !settings.bannerDismissed && settings.lastArtSyncEpochMs == 0L && !artRefreshState.isRunning
    val showArtProgress = artRefreshState.isRunning || artRefreshState.statusMessage.isNotBlank() || artRefreshState.error != null

    // Shared OneUI 8 section emitter (used in both Material Portrait & Material Tablet Landscape)
    fun LazyListScope.emitOneUiCategory(category: SettingsCategory) {
        when (category) {
            SettingsCategory.APPEARANCE -> if (showGroup1) {
                item(key = "oneui_g1") {
                    OneUiSectionLabel("Appearance & Theme")
                    OneUiCard {
                        var hasPrev = false
                        if (matches("theme", "dark", "light", "system")) {
                            OneUiValueRow(
                                icon = KnightIcons.Sparkles,
                                title = "Theme Mode",
                                value = settings.themeMode.label,
                                onClick = { activeDialog = SettingsDialog.THEME_MODE }
                            )
                            hasPrev = true
                        }
                        if (matches("oled", "amoled", "true-black", "pure black", "dark")) {
                            if (hasPrev) OneUiDivider()
                            OneUiSwitchRow(
                                icon = KnightIcons.SleepMoon,
                                title = "OLED mode",
                                subtitle = "Enable OLED True-Black features. needs an AMOLED display to see full effect.",
                                checked = settings.oledBlack,
                                onCheckedChange = onSetOledBlack
                            )
                            hasPrev = true
                        }
                        if (matches("accent", "color", "dynamic", "matcha", "dessert", "material you", "theme")) {
                            if (hasPrev) OneUiDivider()
                            OneUiValueRow(
                                icon = KnightIcons.Palette,
                                title = "Accent Color",
                                value = settings.accentTheme.label,
                                onClick = { activeDialog = SettingsDialog.ACCENT_COLOR }
                            )
                            hasPrev = true
                        }
                        if (matches("now playing", "vinyl", "cd", "disc", "cover", "glow")) {
                            if (hasPrev) OneUiDivider()
                            OneUiValueRow(
                                icon = KnightIcons.VinylDisc,
                                title = "Now Playing Artwork Style",
                                value = settings.nowPlayingStyle.label,
                                onClick = { activeDialog = SettingsDialog.NOW_PLAYING_STYLE }
                            )
                            hasPrev = true
                        }
                        if (matches("album", "layout", "grid", "list")) {
                            if (hasPrev) OneUiDivider()
                            OneUiValueRow(
                                icon = KnightIcons.Albums,
                                title = "Albums Layout",
                                value = settings.albumLayout.label,
                                onClick = { activeDialog = SettingsDialog.ALBUM_LAYOUT }
                            )
                            hasPrev = true
                        }
                        if (matches("artist", "artists", "layout", "grid", "list", "2-column")) {
                            if (hasPrev) OneUiDivider()
                            OneUiValueRow(
                                icon = KnightIcons.Artists,
                                title = "Artists Layout",
                                value = settings.artistLayout.label,
                                onClick = { activeDialog = SettingsDialog.ARTIST_LAYOUT }
                            )
                            hasPrev = true
                        }
                        if (matches("greeter", "home", "welcome", "name", "random", "custom")) {
                            if (hasPrev) OneUiDivider()
                            OneUiValueRow(
                                icon = KnightIcons.HomeFilled,
                                title = "Home Greeter",
                                value = greeterSummary,
                                onClick = { activeDialog = SettingsDialog.HOME_GREETER }
                            )
                            hasPrev = true
                        }
                        if (matches("picture", "logo", "photo", "avatar", "home", "header", "custom")) {
                            if (hasPrev) OneUiDivider()
                            OneUiValueRow(
                                icon = KnightIcons.Artists,
                                title = "Header Picture",
                                value = if (settings.customLogoPath.isNotBlank()) "Custom picture" else "KnightWin Logo",
                                onClick = { activeDialog = SettingsDialog.HEADER_LOGO }
                            )
                            hasPrev = true
                        }
                        if (matches("liquidui", "liquid ui", "liquid glass", "liquid", "frosted", "glass", "material ui", "material", "m3", "ui style")) {
                            if (hasPrev) OneUiDivider()
                            OneUiValueRow(
                                icon = KnightIcons.Sparkles,
                                title = "UI Style",
                                value = if (settings.liquidGlassAccents) "Liquid UI" else "Material UI",
                                onClick = { activeDialog = SettingsDialog.UI_STYLE }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
            SettingsCategory.AUDIO -> if (showGroup2) {
                item(key = "oneui_g2") {
                    OneUiSectionLabel("Audio & Playback")
                    OneUiCard {
                        var hasPrev = false
                        if (matches("equalizer", "eq", "sound", "preset", "jazz", "techno", "blues", "pop", "rock", "opera")) {
                            OneUiValueRow(
                                icon = KnightIcons.Equalizer,
                                title = "Equalizer",
                                value = settings.eqPreset.label,
                                onClick = { activeDialog = SettingsDialog.EQUALIZER }
                            )
                            hasPrev = true
                        }
                        if (matches("reverb", "room", "hall", "echo", "audio")) {
                            if (hasPrev) OneUiDivider()
                            OneUiValueRow(
                                icon = KnightIcons.Sparkles,
                                title = "Room Reverb",
                                value = settings.reverbPreset.label,
                                onClick = { activeDialog = SettingsDialog.REVERB_PRESET }
                            )
                            hasPrev = true
                        }
                        if (matches("crossfade", "fade", "transition")) {
                            if (hasPrev) OneUiDivider()
                            OneUiValueRow(
                                icon = KnightIcons.Songs,
                                title = "Track Crossfade",
                                value = if (settings.crossfadeMs == 0) "Off" else "${settings.crossfadeMs / 1000f} seconds",
                                onClick = { activeDialog = SettingsDialog.CROSSFADE }
                            )
                            hasPrev = true
                        }
                        if (matches("speed", "tempo", "playback")) {
                            if (hasPrev) OneUiDivider()
                            OneUiValueRow(
                                icon = KnightIcons.SpeedDial,
                                title = "Playback Speed",
                                value = String.format(Locale.US, "%.2fx", settings.playbackSpeed),
                                onClick = { activeDialog = SettingsDialog.PLAYBACK_SPEED }
                            )
                            hasPrev = true
                        }
                        if (matches("keep screen", "awake", "screen on")) {
                            if (hasPrev) OneUiDivider()
                            OneUiSwitchRow(
                                icon = KnightIcons.InfoCircle,
                                title = "Keep Screen On",
                                subtitle = "Prevent sleep while viewing Now Playing",
                                checked = settings.keepScreenOn,
                                onCheckedChange = onSetKeepScreenOn
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
            SettingsCategory.LIBRARY -> if (showGroup3) {
                item(key = "oneui_g3") {
                    OneUiSectionLabel("Local Library")
                    OneUiCard {
                        var hasPrev = false
                        if (matches("rescan", "scan", "library", "refresh")) {
                            OneUiValueRow(
                                icon = KnightIcons.FolderScan,
                                title = "Rescan Device Audio",
                                value = "$songCount tracks • $albumCount albums indexed on device",
                                onClick = onRescanLibrary
                            )
                            hasPrev = true
                        }
                        if (matches("sort", "order", "title", "artist")) {
                            if (hasPrev) OneUiDivider()
                            OneUiValueRow(
                                icon = KnightIcons.SortArrows,
                                title = "Default Song Sort Order",
                                value = settings.sortOrder.label,
                                onClick = { activeDialog = SettingsDialog.SORT_ORDER }
                            )
                            hasPrev = true
                        }
                        if (matches("filter", "duration", "short", "ringtones", "ignore")) {
                            if (hasPrev) OneUiDivider()
                            OneUiValueRow(
                                icon = KnightIcons.Close,
                                title = "Filter Short Audio Clips",
                                value = settings.minDurationFilter.label,
                                onClick = { activeDialog = SettingsDialog.MIN_DURATION }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
            SettingsCategory.ARTWORK -> if (showGroup4) {
                item(key = "oneui_g4") {
                    OneUiSectionLabel("Album Art & Offline Sync")
                    OneUiCard {
                        var hasPrev = false
                        if (matches("fetch", "missing", "album art", "cover", "sync")) {
                            OneUiValueRow(
                                icon = KnightIcons.ArtSync,
                                title = "Fetch Missing Album Art",
                                value = "Last sync: ${formatSyncDate(settings.lastArtSyncEpochMs)}",
                                onClick = onFetchMissingArt
                            )
                            hasPrev = true
                        }
                        if (matches("refresh all", "album art", "redownload", "cover")) {
                            if (hasPrev) OneUiDivider()
                            OneUiValueRow(
                                icon = KnightIcons.ArtSync,
                                title = "Refresh All Album Art",
                                value = "Re-check online artwork for all $albumCount albums",
                                onClick = onRefreshAllArt
                            )
                            hasPrev = true
                        }
                        if (matches("embedded", "prefer", "local", "art")) {
                            if (hasPrev) OneUiDivider()
                            OneUiSwitchRow(
                                icon = KnightIcons.Albums,
                                title = "Prefer Embedded File Art",
                                subtitle = "Use audio file tags before downloaded covers",
                                checked = settings.preferEmbeddedArt,
                                onCheckedChange = onSetPreferEmbeddedArt
                            )
                            hasPrev = true
                        }
                        if (matches("clear", "cache", "storage", "delete", "art")) {
                            if (hasPrev) OneUiDivider()
                            OneUiValueRow(
                                icon = KnightIcons.Trash,
                                title = "Clear Downloaded Art Cache",
                                value = "${formatBytes(artCacheSizeBytes)} cached on device",
                                onClick = onClearDownloadedArt
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
            SettingsCategory.ABOUT -> if (showGroup5) {
                item(key = "oneui_g5") {
                    OneUiSectionLabel("Privacy & About")
                    OneUiCard {
                        var hasPrev = false
                        if (matches("privacy", "offline", "network", "guarantee")) {
                            OneUiValueRow(
                                icon = KnightIcons.OfflineShield,
                                title = "Offline Privacy Guarantee",
                                value = "Zero telemetry • Network used only for manual art fetch",
                                onClick = { activeDialog = SettingsDialog.PRIVACY_OFFLINE }
                            )
                            hasPrev = true
                        }
                        if (matches("about", "knightwin", "owner", "license", "gpl", "version", "credits")) {
                            if (hasPrev) OneUiDivider()
                            OneUiValueRow(
                                icon = KnightIcons.InfoCircle,
                                title = "About KnightWin Music",
                                value = "Version 1.0.0 • © 2026 KnightWin • GPL-3.0",
                                onClick = { activeDialog = SettingsDialog.ABOUT_APP }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
        }
    }

    // Shared iOS 26/27 Liquid Glass section emitter (used in LiquidUI Portrait & iPadOS Split-View Landscape)
    fun LazyListScope.emitIosCategory(category: SettingsCategory) {
        when (category) {
            SettingsCategory.APPEARANCE -> if (showGroup1) {
                item(key = "ios_g1") {
                    IosSectionLabel("Appearance & Theme")
                    IosGlassGroup {
                        var hasPrev = false
                        if (matches("theme", "dark", "light", "system")) {
                            IosValueRow(
                                icon = KnightIcons.Sparkles,
                                badgeTop = Color(0xFF3B82F6),
                                badgeBottom = Color(0xFF1D4ED8),
                                title = "Theme Mode",
                                value = settings.themeMode.label,
                                onClick = { activeDialog = SettingsDialog.THEME_MODE }
                            )
                            hasPrev = true
                        }
                        if (matches("oled", "amoled", "true-black", "pure black", "dark")) {
                            if (hasPrev) IosDivider()
                            IosSwitchRow(
                                icon = KnightIcons.SleepMoon,
                                badgeTop = Color(0xFF475569),
                                badgeBottom = Color(0xFF1E293B),
                                title = "OLED Mode",
                                subtitle = "True-black AMOLED background",
                                checked = settings.oledBlack,
                                onCheckedChange = onSetOledBlack
                            )
                            hasPrev = true
                        }
                        if (matches("accent", "color", "dynamic", "matcha", "dessert", "material you", "theme")) {
                            if (hasPrev) IosDivider()
                            IosValueRow(
                                icon = KnightIcons.Palette,
                                badgeTop = Color(0xFFEC4899),
                                badgeBottom = Color(0xFF9333EA),
                                title = "Accent Color",
                                value = settings.accentTheme.label,
                                onClick = { activeDialog = SettingsDialog.ACCENT_COLOR }
                            )
                            hasPrev = true
                        }
                        if (matches("now playing", "vinyl", "cd", "disc", "cover", "glow")) {
                            if (hasPrev) IosDivider()
                            IosValueRow(
                                icon = KnightIcons.VinylDisc,
                                badgeTop = Color(0xFFFF5A79),
                                badgeBottom = Color(0xFFE11D48),
                                title = "Now Playing Artwork",
                                value = settings.nowPlayingStyle.label,
                                onClick = { activeDialog = SettingsDialog.NOW_PLAYING_STYLE }
                            )
                            hasPrev = true
                        }
                        if (matches("album", "layout", "grid", "list")) {
                            if (hasPrev) IosDivider()
                            IosValueRow(
                                icon = KnightIcons.Albums,
                                badgeTop = Color(0xFF38BDF8),
                                badgeBottom = Color(0xFF0284C7),
                                title = "Albums Layout",
                                value = settings.albumLayout.label,
                                onClick = { activeDialog = SettingsDialog.ALBUM_LAYOUT }
                            )
                            hasPrev = true
                        }
                        if (matches("artist", "artists", "layout", "grid", "list", "2-column")) {
                            if (hasPrev) IosDivider()
                            IosValueRow(
                                icon = KnightIcons.Artists,
                                badgeTop = Color(0xFF06B6D4),
                                badgeBottom = Color(0xFF0E7490),
                                title = "Artists Layout",
                                value = settings.artistLayout.label,
                                onClick = { activeDialog = SettingsDialog.ARTIST_LAYOUT }
                            )
                            hasPrev = true
                        }
                        if (matches("greeter", "home", "welcome", "name", "random", "custom")) {
                            if (hasPrev) IosDivider()
                            IosValueRow(
                                icon = KnightIcons.HomeFilled,
                                badgeTop = Color(0xFF34D399),
                                badgeBottom = Color(0xFF059669),
                                title = "Home Greeter",
                                value = iosGreeterValue,
                                onClick = { activeDialog = SettingsDialog.HOME_GREETER }
                            )
                            hasPrev = true
                        }
                        if (matches("picture", "logo", "photo", "avatar", "home", "header", "custom")) {
                            if (hasPrev) IosDivider()
                            IosValueRow(
                                icon = KnightIcons.Artists,
                                badgeTop = Color(0xFF22C55E),
                                badgeBottom = Color(0xFF15803D),
                                title = "Header Picture",
                                value = if (settings.customLogoPath.isNotBlank()) "Custom" else "Default",
                                onClick = { activeDialog = SettingsDialog.HEADER_LOGO }
                            )
                            hasPrev = true
                        }
                        if (matches("liquidui", "liquid ui", "liquid glass", "liquid", "frosted", "glass", "material ui", "material", "m3", "ui style")) {
                            if (hasPrev) IosDivider()
                            IosValueRow(
                                icon = KnightIcons.Sparkles,
                                badgeTop = lerp(primary, Color.White, 0.12f),
                                badgeBottom = lerp(primary, Color(0xFF6366F1), 0.35f),
                                title = "UI Style",
                                value = if (settings.liquidGlassAccents) "Liquid UI" else "Material UI",
                                onClick = { activeDialog = SettingsDialog.UI_STYLE }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
            SettingsCategory.AUDIO -> if (showGroup2) {
                item(key = "ios_g2") {
                    IosSectionLabel("Audio & Playback")
                    IosGlassGroup {
                        var hasPrev = false
                        if (matches("equalizer", "eq", "sound", "preset", "jazz", "techno", "blues", "pop", "rock", "opera")) {
                            IosValueRow(
                                icon = KnightIcons.Equalizer,
                                badgeTop = Color(0xFFFB923C),
                                badgeBottom = Color(0xFFEF4444),
                                title = "Equalizer",
                                value = settings.eqPreset.label,
                                onClick = { activeDialog = SettingsDialog.EQUALIZER }
                            )
                            hasPrev = true
                        }
                        if (matches("reverb", "room", "hall", "echo", "audio")) {
                            if (hasPrev) IosDivider()
                            IosValueRow(
                                icon = KnightIcons.Sparkles,
                                badgeTop = Color(0xFFF43F5E),
                                badgeBottom = Color(0xFFBE123C),
                                title = "Room Reverb",
                                value = settings.reverbPreset.label,
                                onClick = { activeDialog = SettingsDialog.REVERB_PRESET }
                            )
                            hasPrev = true
                        }
                        if (matches("crossfade", "fade", "transition")) {
                            if (hasPrev) IosDivider()
                            IosValueRow(
                                icon = KnightIcons.Songs,
                                badgeTop = Color(0xFFFBBF24),
                                badgeBottom = Color(0xFFD97706),
                                title = "Track Crossfade",
                                value = if (settings.crossfadeMs == 0) "Off" else "${settings.crossfadeMs / 1000f}s",
                                onClick = { activeDialog = SettingsDialog.CROSSFADE }
                            )
                            hasPrev = true
                        }
                        if (matches("speed", "tempo", "playback")) {
                            if (hasPrev) IosDivider()
                            IosValueRow(
                                icon = KnightIcons.SpeedDial,
                                badgeTop = Color(0xFFF59E0B),
                                badgeBottom = Color(0xFFB45309),
                                title = "Playback Speed",
                                value = String.format(Locale.US, "%.2fx", settings.playbackSpeed),
                                onClick = { activeDialog = SettingsDialog.PLAYBACK_SPEED }
                            )
                            hasPrev = true
                        }
                        if (matches("keep screen", "awake", "screen on")) {
                            if (hasPrev) IosDivider()
                            IosSwitchRow(
                                icon = KnightIcons.InfoCircle,
                                badgeTop = Color(0xFF22D3EE),
                                badgeBottom = Color(0xFF0D9488),
                                title = "Keep Screen On",
                                subtitle = "Stay awake in Now Playing",
                                checked = settings.keepScreenOn,
                                onCheckedChange = onSetKeepScreenOn
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
            SettingsCategory.LIBRARY -> if (showGroup3) {
                item(key = "ios_g3") {
                    IosSectionLabel("Local Library")
                    IosGlassGroup {
                        var hasPrev = false
                        if (matches("rescan", "scan", "library", "refresh")) {
                            IosValueRow(
                                icon = KnightIcons.FolderScan,
                                badgeTop = Color(0xFF2DD4BF),
                                badgeBottom = Color(0xFF0F766E),
                                title = "Rescan Device Audio",
                                value = "$songCount tracks",
                                onClick = onRescanLibrary
                            )
                            hasPrev = true
                        }
                        if (matches("sort", "order", "title", "artist")) {
                            if (hasPrev) IosDivider()
                            IosValueRow(
                                icon = KnightIcons.SortArrows,
                                badgeTop = Color(0xFF14B8A6),
                                badgeBottom = Color(0xFF0E7490),
                                title = "Default Sort Order",
                                value = settings.sortOrder.label,
                                onClick = { activeDialog = SettingsDialog.SORT_ORDER }
                            )
                            hasPrev = true
                        }
                        if (matches("filter", "duration", "short", "ringtones", "ignore")) {
                            if (hasPrev) IosDivider()
                            IosValueRow(
                                icon = KnightIcons.Close,
                                badgeTop = Color(0xFF64748B),
                                badgeBottom = Color(0xFF334155),
                                title = "Filter Short Clips",
                                value = settings.minDurationFilter.label,
                                onClick = { activeDialog = SettingsDialog.MIN_DURATION }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
            SettingsCategory.ARTWORK -> if (showGroup4) {
                item(key = "ios_g4") {
                    IosSectionLabel("Album Art & Offline Sync")
                    IosGlassGroup {
                        var hasPrev = false
                        if (matches("fetch", "missing", "album art", "cover", "sync")) {
                            IosValueRow(
                                icon = KnightIcons.ArtSync,
                                badgeTop = Color(0xFF818CF8),
                                badgeBottom = Color(0xFF4F46E5),
                                title = "Fetch Missing Art",
                                value = formatSyncDate(settings.lastArtSyncEpochMs),
                                onClick = onFetchMissingArt
                            )
                            hasPrev = true
                        }
                        if (matches("refresh all", "album art", "redownload", "cover")) {
                            if (hasPrev) IosDivider()
                            IosValueRow(
                                icon = KnightIcons.ArtSync,
                                badgeTop = Color(0xFFA78BFA),
                                badgeBottom = Color(0xFF6D28D9),
                                title = "Refresh All Album Art",
                                value = "$albumCount albums",
                                onClick = onRefreshAllArt
                            )
                            hasPrev = true
                        }
                        if (matches("embedded", "prefer", "local", "art")) {
                            if (hasPrev) IosDivider()
                            IosSwitchRow(
                                icon = KnightIcons.Albums,
                                badgeTop = Color(0xFF60A5FA),
                                badgeBottom = Color(0xFF2563EB),
                                title = "Prefer Embedded File Art",
                                subtitle = "Audio tags over downloaded art",
                                checked = settings.preferEmbeddedArt,
                                onCheckedChange = onSetPreferEmbeddedArt
                            )
                            hasPrev = true
                        }
                        if (matches("clear", "cache", "storage", "delete", "art")) {
                            if (hasPrev) IosDivider()
                            IosValueRow(
                                icon = KnightIcons.Trash,
                                badgeTop = Color(0xFFF87171),
                                badgeBottom = Color(0xFFDC2626),
                                title = "Clear Art Cache",
                                value = formatBytes(artCacheSizeBytes),
                                onClick = onClearDownloadedArt
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
            SettingsCategory.ABOUT -> if (showGroup5) {
                item(key = "ios_g5") {
                    IosSectionLabel("Privacy & About")
                    IosGlassGroup {
                        var hasPrev = false
                        if (matches("privacy", "offline", "network", "guarantee")) {
                            IosValueRow(
                                icon = KnightIcons.OfflineShield,
                                badgeTop = Color(0xFF4ADE80),
                                badgeBottom = Color(0xFF16A34A),
                                title = "Offline Privacy",
                                value = "Zero telemetry",
                                onClick = { activeDialog = SettingsDialog.PRIVACY_OFFLINE }
                            )
                            hasPrev = true
                        }
                        if (matches("about", "knightwin", "owner", "license", "gpl", "version", "credits")) {
                            if (hasPrev) IosDivider()
                            IosValueRow(
                                icon = KnightIcons.InfoCircle,
                                badgeTop = Color(0xFF94A3B8),
                                badgeBottom = Color(0xFF475569),
                                title = "About KnightWin Music",
                                value = "v1.0.0 • KnightWin",
                                onClick = { activeDialog = SettingsDialog.ABOUT_APP }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
        }
    }

    LiquidAppBackdrop(
        modifier = Modifier.fillMaxSize(),
        useOneUiCanvas = true
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

            if (extras.liquidGlassEnabled && isLandscape) {
                // =====================================================================
                // LIQUID UI — iPadOS 26 / 27 FLOATING LIQUID GLASS SPLIT VIEW (LANDSCAPE)
                // =====================================================================
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .displayCutoutPadding()
                        .padding(top = 6.dp)
                ) {
                    // Floating Glass Sidebar
                    LiquidGlassSurface(
                        shape = RoundedCornerShape(26.dp),
                        variant = GlassVariant.DOCK,
                        elevation = 8.dp,
                        modifier = Modifier
                            .weight(0.35f)
                            .widthIn(min = 215.dp, max = 310.dp)
                            .fillMaxHeight()
                            .padding(start = 12.dp, bottom = navBarBottom + 10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LiquidGlassSurface(
                                    shape = CircleShape,
                                    variant = GlassVariant.PILL,
                                    elevation = 4.dp,
                                    modifier = Modifier.size(34.dp),
                                    onClick = onBack
                                ) {
                                    Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = KnightIcons.BackChevron,
                                            contentDescription = "Back",
                                            tint = primary,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Settings",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp,
                                        letterSpacing = (-0.4).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Frosted iPad Search Pill
                            LiquidGlassSurface(
                                shape = CircleShape,
                                variant = GlassVariant.PILL,
                                elevation = 2.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = KnightIcons.Search,
                                        contentDescription = "Search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = "Search",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        BasicTextField(
                                            value = searchQuery,
                                            onValueChange = { searchQuery = it },
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurface
                                            ),
                                            cursorBrush = SolidColor(primary),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .focusRequester(searchFocusRequester)
                                        )
                                    }
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(
                                            onClick = { searchQuery = "" },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                imageVector = KnightIcons.Close,
                                                contentDescription = "Clear",
                                                tint = primary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .verticalEdgeFade(topFade = 4.dp, bottomFade = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Compact KnightWin Music badge in iPad sidebar
                                item(key = "ipad_hero") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { activeDialog = SettingsDialog.ABOUT_APP }
                                            .padding(horizontal = 8.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        HeaderLogoAvatar(
                                            customLogoPath = settings.customLogoPath,
                                            contentDescription = "KnightWin Music",
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "KnightWin",
                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "$songCount tracks • Local",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    HorizontalDivider(
                                        thickness = 0.55.dp,
                                        color = extras.oneUiDivider,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }

                                items(SettingsCategory.entries.size) { idx ->
                                    val category = SettingsCategory.entries[idx]
                                    val isSelected = q.isEmpty() && category == selectedCategory
                                    val pillBg = if (isSelected) {
                                        primary.copy(alpha = if (extras.isDark) 0.24f else 0.15f)
                                    } else {
                                        Color.Transparent
                                    }
                                    val pillBorder = if (isSelected) {
                                        Modifier.border(0.85.dp, primary.copy(alpha = 0.40f), RoundedCornerShape(14.dp))
                                    } else {
                                        Modifier
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(pillBg)
                                            .then(pillBorder)
                                            .clickable {
                                                selectedCategory = category
                                                if (q.isNotEmpty()) searchQuery = ""
                                                scope.launch { detailListState.scrollToItem(0) }
                                            }
                                            .padding(horizontal = 8.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IosCategoryIconChip(
                                            icon = category.icon,
                                            topColor = category.badgeTopColor,
                                            bottomColor = category.badgeBottomColor,
                                            size = 28.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = category.shortLabel,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                fontSize = 15.sp
                                            ),
                                            color = if (isSelected) primary else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Right iPadOS Detail Pane
                    Column(
                        modifier = Modifier
                            .weight(0.65f)
                            .fillMaxHeight()
                    ) {
                        Text(
                            text = if (q.isNotEmpty()) "Results for \"$q\"" else selectedCategory.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                letterSpacing = (-0.4).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 6.dp)
                        )

                        LazyColumn(
                            state = detailListState,
                            contentPadding = PaddingValues(top = 4.dp, bottom = navBarBottom + 28.dp),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalEdgeFade(
                                    topFade = 10.dp,
                                    bottomFade = navBarBottom + 22.dp
                                )
                        ) {
                            if (showArtProgress && (q.isNotEmpty() || selectedCategory == SettingsCategory.ARTWORK)) {
                                item(key = "ipad_art_progress") {
                                    ArtSyncProgressCard(
                                        artRefreshState = artRefreshState,
                                        onCancelArtFetch = onCancelArtFetch,
                                        onClearArtStatus = onClearArtStatus,
                                        isIosStyle = true
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                            if (showArtSuggestion && selectedCategory == SettingsCategory.ARTWORK) {
                                item(key = "ipad_art_banner") {
                                    ArtSyncSuggestionCard(
                                        onDismissBanner = onDismissBanner,
                                        onFetchMissingArt = onFetchMissingArt,
                                        isIosStyle = true
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }

                            if (q.isNotEmpty()) {
                                SettingsCategory.entries.forEach { cat -> emitIosCategory(cat) }
                            } else {
                                emitIosCategory(selectedCategory)
                            }
                        }
                    }
                }
            } else if (extras.liquidGlassEnabled) {
                // =====================================================================
                // LIQUID UI — iOS 26 / 27 LIQUID GLASS SETTINGS (PORTRAIT)
                // =====================================================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    // Floating iOS Glass Nav Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LiquidGlassSurface(
                            shape = CircleShape,
                            variant = GlassVariant.PILL,
                            elevation = 4.dp,
                            modifier = Modifier.size(38.dp),
                            onClick = onBack
                        ) {
                            Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = KnightIcons.BackChevron,
                                    contentDescription = "Back",
                                    tint = primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp
                            ),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .weight(1f)
                                .alpha(if (isCollapsed || q.isNotEmpty()) 1f else 0f)
                        )

                        LiquidGlassSurface(
                            shape = CircleShape,
                            variant = GlassVariant.PILL,
                            elevation = 4.dp,
                            modifier = Modifier.size(38.dp),
                            onClick = {
                                scope.launch {
                                    listState.animateScrollToItem(0)
                                    runCatching { searchFocusRequester.requestFocus() }
                                }
                            }
                        ) {
                            Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = KnightIcons.Search,
                                    contentDescription = "Jump to Search",
                                    tint = primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(bottom = navBarBottom + 32.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalEdgeFade(
                                topFade = 12.dp,
                                bottomFade = navBarBottom + 24.dp
                            )
                    ) {
                        if (q.isEmpty()) {
                            item(key = "ios_title") {
                                Text(
                                    text = "Settings",
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        fontSize = 34.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.6).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 12.dp)
                                )
                            }
                        }

                        item(key = "ios_search") {
                            LiquidGlassSurface(
                                shape = CircleShape,
                                variant = GlassVariant.PILL,
                                elevation = 4.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .padding(horizontal = 15.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = KnightIcons.Search,
                                        contentDescription = "Search settings",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = "Search settings",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        BasicTextField(
                                            value = searchQuery,
                                            onValueChange = { searchQuery = it },
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                                color = MaterialTheme.colorScheme.onSurface
                                            ),
                                            cursorBrush = SolidColor(primary),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .focusRequester(searchFocusRequester)
                                        )
                                    }
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(
                                            onClick = { searchQuery = "" },
                                            modifier = Modifier.size(22.dp)
                                        ) {
                                            Icon(
                                                imageVector = KnightIcons.Close,
                                                contentDescription = "Clear",
                                                tint = primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // iOS 26/27 App Hero Card
                        if (q.isEmpty()) {
                            item(key = "ios_hero_card") {
                                LiquidGlassSurface(
                                    shape = RoundedCornerShape(24.dp),
                                    variant = GlassVariant.CARD,
                                    elevation = 4.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    onClick = { activeDialog = SettingsDialog.ABOUT_APP }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 13.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        HeaderLogoAvatar(
                                            customLogoPath = settings.customLogoPath,
                                            contentDescription = "KnightWin Music",
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .border(1.1.dp, Color.White.copy(alpha = 0.50f), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "KnightWin Music",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 17.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "$songCount tracks • $albumCount albums • Offline",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Icon(
                                            imageVector = KnightIcons.ChevronRight,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(18.dp))
                            }
                        }

                        if (showArtSuggestion) {
                            item(key = "ios_art_banner") {
                                ArtSyncSuggestionCard(
                                    onDismissBanner = onDismissBanner,
                                    onFetchMissingArt = onFetchMissingArt,
                                    isIosStyle = true
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                            }
                        }

                        if (showArtProgress) {
                            item(key = "ios_art_progress") {
                                ArtSyncProgressCard(
                                    artRefreshState = artRefreshState,
                                    onCancelArtFetch = onCancelArtFetch,
                                    onClearArtStatus = onClearArtStatus,
                                    isIosStyle = true
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                            }
                        }

                        SettingsCategory.entries.forEach { cat -> emitIosCategory(cat) }
                    }
                }
            } else if (isLandscape) {
                // =====================================================================
                // MATERIAL UI — ONE UI 8 TABLET TWO-PANE SPLIT VIEW (LANDSCAPE)
                // =====================================================================
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .displayCutoutPadding()
                ) {
                    // Left Category Navigation Column
                    Column(
                        modifier = Modifier
                            .weight(0.35f)
                            .widthIn(min = 215.dp, max = 305.dp)
                            .fillMaxHeight()
                            .padding(start = 8.dp, end = 6.dp, top = 6.dp, bottom = navBarBottom + 10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = KnightIcons.BackChevron,
                                    contentDescription = "Back",
                                    tint = primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Text(
                                text = "Settings",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = extras.oneUiCard,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            border = if (extras.isOled) BorderStroke(0.85.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = KnightIcons.Search,
                                    contentDescription = "Search settings",
                                    tint = primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search settings",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    BasicTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurface
                                        ),
                                        cursorBrush = SolidColor(primary),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(searchFocusRequester)
                                    )
                                }
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(
                                            imageVector = KnightIcons.Close,
                                            contentDescription = "Clear",
                                            tint = primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = extras.oneUiCard,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            border = if (extras.isOled) BorderStroke(0.85.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp)
                        ) {
                            LazyColumn(
                                contentPadding = PaddingValues(vertical = 8.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalEdgeFade(topFade = 4.dp, bottomFade = 10.dp)
                            ) {
                                items(SettingsCategory.entries.size) { idx ->
                                    val category = SettingsCategory.entries[idx]
                                    val isSelected = q.isEmpty() && category == selectedCategory
                                    val rowBg = if (isSelected) {
                                        primary.copy(alpha = if (extras.isOled) 0.22f else 0.14f)
                                    } else {
                                        Color.Transparent
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(rowBg)
                                            .then(
                                                if (isSelected && extras.isOled) {
                                                    Modifier.border(0.85.dp, primary.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                                                } else {
                                                    Modifier
                                                }
                                            )
                                            .clickable {
                                                selectedCategory = category
                                                if (q.isNotEmpty()) searchQuery = ""
                                                scope.launch { detailListState.scrollToItem(0) }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OneUiIconChip(icon = category.icon, size = 34.dp, iconSize = 18.dp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = category.shortLabel,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 15.sp
                                            ),
                                            color = if (isSelected) primary else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Subtle OneUI Tablet Pane Divider
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(vertical = 18.dp)
                            .width(0.75.dp)
                            .background(extras.oneUiDivider)
                    )

                    // Right Detail Pane
                    Column(
                        modifier = Modifier
                            .weight(0.65f)
                            .fillMaxHeight()
                    ) {
                        Text(
                            text = if (q.isNotEmpty()) "Results for \"$q\"" else selectedCategory.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 23.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 6.dp)
                        )

                        LazyColumn(
                            state = detailListState,
                            contentPadding = PaddingValues(top = 4.dp, bottom = navBarBottom + 28.dp),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalEdgeFade(
                                    topFade = 10.dp,
                                    bottomFade = navBarBottom + 22.dp
                                )
                        ) {
                            if (showArtProgress && (q.isNotEmpty() || selectedCategory == SettingsCategory.ARTWORK)) {
                                item(key = "oneui_tablet_art_progress") {
                                    ArtSyncProgressCard(
                                        artRefreshState = artRefreshState,
                                        onCancelArtFetch = onCancelArtFetch,
                                        onClearArtStatus = onClearArtStatus,
                                        isIosStyle = false
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                            if (showArtSuggestion && selectedCategory == SettingsCategory.ARTWORK) {
                                item(key = "oneui_tablet_art_banner") {
                                    ArtSyncSuggestionCard(
                                        onDismissBanner = onDismissBanner,
                                        onFetchMissingArt = onFetchMissingArt,
                                        isIosStyle = false
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }

                            if (q.isNotEmpty()) {
                                SettingsCategory.entries.forEach { cat -> emitOneUiCategory(cat) }
                            } else {
                                emitOneUiCategory(selectedCategory)
                            }
                        }
                    }
                }
            } else {
                // =====================================================================
                // MATERIAL UI — SAMSUNG ONE UI 8 SETTINGS (PORTRAIT)
                // =====================================================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    // Sticky Compact OneUI Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = KnightIcons.BackChevron,
                                contentDescription = "Back",
                                tint = primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 10.dp)
                                .alpha(if (isCollapsed || q.isNotEmpty()) 1f else 0f)
                        )
                        IconButton(
                            onClick = {
                                scope.launch {
                                    listState.animateScrollToItem(0)
                                    runCatching { searchFocusRequester.requestFocus() }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = KnightIcons.Search,
                                contentDescription = "Jump to Search",
                                tint = primary,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }

                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(bottom = navBarBottom + 32.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalEdgeFade(
                                topFade = 12.dp,
                                bottomFade = navBarBottom + 24.dp
                            )
                    ) {
                        // OneUI 8 Extended Reachability Title Header
                        if (q.isEmpty()) {
                            item(key = "oneui_title") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 24.dp, bottom = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Settings",
                                        style = MaterialTheme.typography.displayLarge.copy(
                                            fontSize = 37.sp,
                                            fontWeight = FontWeight.Normal,
                                            letterSpacing = (-0.4).sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Search Pill
                        item(key = "oneui_search") {
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = extras.oneUiCard,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                border = if (extras.isOled) BorderStroke(0.85.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp)
                                    .padding(bottom = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = KnightIcons.Search,
                                        contentDescription = "Search settings",
                                        tint = primary,
                                        modifier = Modifier.size(19.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = "Search settings",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        BasicTextField(
                                            value = searchQuery,
                                            onValueChange = { searchQuery = it },
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                                color = MaterialTheme.colorScheme.onSurface
                                            ),
                                            cursorBrush = SolidColor(primary),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .focusRequester(searchFocusRequester)
                                        )
                                    }
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(
                                            onClick = { searchQuery = "" },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = KnightIcons.Close,
                                                contentDescription = "Clear",
                                                tint = primary,
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (showArtSuggestion) {
                            item(key = "oneui_art_banner") {
                                ArtSyncSuggestionCard(
                                    onDismissBanner = onDismissBanner,
                                    onFetchMissingArt = onFetchMissingArt,
                                    isIosStyle = false
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        if (showArtProgress) {
                            item(key = "oneui_art_progress") {
                                ArtSyncProgressCard(
                                    artRefreshState = artRefreshState,
                                    onCancelArtFetch = onCancelArtFetch,
                                    onClearArtStatus = onClearArtStatus,
                                    isIosStyle = false
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        SettingsCategory.entries.forEach { cat -> emitOneUiCategory(cat) }
                    }
                }
            }
        }
    }

    when (activeDialog) {
        SettingsDialog.THEME_MODE -> {
            OneUiRadioDialog(
                title = "Theme Mode",
                options = ThemeMode.entries,
                selected = settings.themeMode,
                labelOf = { it.label },
                onSelect = { onSetThemeMode(it); activeDialog = null },
                onDismiss = { activeDialog = null }
            )
        }
        SettingsDialog.UI_STYLE -> {
            OneUiRadioDialog(
                title = "UI Style",
                options = listOf(true, false),
                selected = settings.liquidGlassAccents,
                labelOf = { if (it) "Liquid UI" else "Material UI" },
                subtitleOf = {
                    if (it) "Frosted translucent glass across the entire app"
                    else "Pure Material 3 surfaces & elevation"
                },
                onSelect = { onSetLiquidGlass(it); activeDialog = null },
                onDismiss = { activeDialog = null }
            )
        }
        SettingsDialog.ACCENT_COLOR -> {
            val dynamicPreviewColor = if (extras.isDark) {
                dynamicDarkColorScheme(context).primary
            } else {
                dynamicLightColorScheme(context).primary
            }
            KnightDialog(
                onDismissRequest = { activeDialog = null },
                title = { Text("Accent Color", fontWeight = FontWeight.Bold) },
                text = {
                    LazyColumn(modifier = Modifier.height(360.dp)) {
                        items(AccentTheme.entries.size) { index ->
                            val item = AccentTheme.entries[index]
                            val isSelected = item == settings.accentTheme
                            val swatchColor = if (item == AccentTheme.DYNAMIC) dynamicPreviewColor else Color(item.primaryHex)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onSetAccentTheme(item); activeDialog = null }
                                    .padding(horizontal = 8.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(swatchColor)
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                RadioButton(selected = isSelected, onClick = null)
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { activeDialog = null }) { Text("Cancel") } }
            )
        }
        SettingsDialog.HOME_GREETER -> {
            HomeGreeterDialog(
                currentMode = settings.greeterMode,
                currentCustomText = settings.customGreeterText,
                currentRandomPreview = randomizedGreeter,
                onRerollPreview = onRerollGreeter,
                onSave = { mode, custom ->
                    onSetGreeterMode(mode)
                    onSetCustomGreeterText(custom)
                    activeDialog = null
                },
                onDismiss = { activeDialog = null }
            )
        }
        SettingsDialog.HEADER_LOGO -> {
            HeaderLogoDialog(
                customLogoPath = settings.customLogoPath,
                onChoosePicture = {
                    logoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onResetToDefault = onResetCustomLogo,
                onDismiss = { activeDialog = null }
            )
        }
        SettingsDialog.NOW_PLAYING_STYLE -> {
            OneUiRadioDialog(
                title = "Now Playing Artwork",
                options = NowPlayingStyle.entries,
                selected = settings.nowPlayingStyle,
                labelOf = { it.label },
                subtitleOf = { it.description },
                onSelect = { onSetNowPlayingStyle(it); activeDialog = null },
                onDismiss = { activeDialog = null }
            )
        }
        SettingsDialog.ALBUM_LAYOUT -> {
            OneUiRadioDialog(
                title = "Albums Layout",
                options = AlbumLayout.entries,
                selected = settings.albumLayout,
                labelOf = { it.label },
                onSelect = { onSetAlbumLayout(it); activeDialog = null },
                onDismiss = { activeDialog = null }
            )
        }
        SettingsDialog.ARTIST_LAYOUT -> {
            OneUiRadioDialog(
                title = "Artists Layout",
                options = AlbumLayout.entries,
                selected = settings.artistLayout,
                labelOf = { it.label },
                onSelect = { onSetArtistLayout(it); activeDialog = null },
                onDismiss = { activeDialog = null }
            )
        }
        SettingsDialog.SORT_ORDER -> {
            OneUiRadioDialog(
                title = "Default Song Sort",
                options = SortOrder.entries,
                selected = settings.sortOrder,
                labelOf = { it.label },
                onSelect = { onSetSortOrder(it); activeDialog = null },
                onDismiss = { activeDialog = null }
            )
        }
        SettingsDialog.MIN_DURATION -> {
            OneUiRadioDialog(
                title = "Filter Short Clips",
                options = MinDurationFilter.entries,
                selected = settings.minDurationFilter,
                labelOf = { it.label },
                onSelect = { onSetMinDuration(it); activeDialog = null },
                onDismiss = { activeDialog = null }
            )
        }
        SettingsDialog.CROSSFADE -> {
            val choices = listOf(0, 500, 1000, 1500, 2000)
            OneUiRadioDialog(
                title = "Track Crossfade",
                options = choices,
                selected = settings.crossfadeMs,
                labelOf = { if (it == 0) "Off" else "${it / 1000f} seconds" },
                onSelect = { onSetCrossfadeMs(it); activeDialog = null },
                onDismiss = { activeDialog = null }
            )
        }
        SettingsDialog.PLAYBACK_SPEED -> {
            var tempSpeed by remember(settings.playbackSpeed) { mutableFloatStateOf(settings.playbackSpeed) }
            KnightDialog(
                onDismissRequest = { activeDialog = null },
                title = { Text("Playback Speed", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(String.format(Locale.US, "%.2fx", tempSpeed), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(value = tempSpeed, onValueChange = { tempSpeed = (it * 20).toInt() / 20f }, valueRange = 0.5f..2.0f)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("0.5x", style = MaterialTheme.typography.labelSmall)
                            TextButton(onClick = { tempSpeed = 1.0f }) { Text("Reset 1.0x") }
                            Text("2.0x", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { onSetPlaybackSpeed(tempSpeed); activeDialog = null }) { Text("Apply") } },
                dismissButton = { TextButton(onClick = { activeDialog = null }) { Text("Cancel") } }
            )
        }
        SettingsDialog.REVERB_PRESET -> {
            OneUiRadioDialog(
                title = "Room Reverb",
                options = ReverbPreset.entries,
                selected = settings.reverbPreset,
                labelOf = { it.label },
                onSelect = { onSetReverbPreset(it); activeDialog = null },
                onDismiss = { activeDialog = null }
            )
        }
        SettingsDialog.EQUALIZER -> {
            BuiltInEqualizerSheet(
                currentPreset = settings.eqPreset,
                customBands = settings.eqCustomBands,
                audioSessionId = audioSessionId,
                onSelectPreset = onSetEqualizerPreset,
                onUpdateCustomBands = onSetEqualizerCustomBands,
                onDismiss = { activeDialog = null }
            )
        }
        SettingsDialog.PRIVACY_OFFLINE -> {
            KnightDialog(
                onDismissRequest = { activeDialog = null },
                title = { Text("100% Offline Guarantee") },
                text = {
                    Text(
                        "KnightWin Music contains zero ads, zero analytics, and zero background network activity.\n\n" +
                            "All audio scanning and playback happens strictly on your device via Android MediaStore and Media3.\n\n" +
                            "The network is accessed ONLY when you explicitly confirm the one-time first-run album art prompt or tap Fetch / Refresh Album Art here in Settings."
                    )
                },
                confirmButton = { TextButton(onClick = { activeDialog = null }) { Text("Got It") } }
            )
        }
        SettingsDialog.ABOUT_APP -> {
            KnightDialog(
                onDismissRequest = { activeDialog = null },
                title = { Text("KnightWin Music", fontWeight = FontWeight.Bold) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = R.drawable.knightwin_emblem),
                            contentDescription = "KnightWin Music Logo",
                            modifier = Modifier.size(78.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Version 1.0.0", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Copyright © 2026 KnightWin",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Free, ad-free offline music player for Android 13+.\nCrafted with Material 3 (OneUI 8–inspired Settings) and LiquidUI (iOS 26/27 Liquid Glass).\n\nLicensed under the GNU General Public License v3.0 (GPL-3.0).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                confirmButton = { TextButton(onClick = { activeDialog = null }) { Text("Close") } }
            )
        }
        null -> Unit
    }
}

@Composable
private fun OneUiSectionLabel(text: String) {
    val extras = LocalKnightExtras.current
    val labelColor = when {
        extras.isOled -> Color(0xFFA0A0AA)
        extras.isDark -> Color(0xFF96969E)
        else -> Color(0xFF6B6B73)
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        ),
        color = labelColor,
        modifier = Modifier.padding(horizontal = 28.dp, vertical = 6.dp)
    )
}

@Composable
private fun OneUiCard(content: @Composable ColumnScope.() -> Unit) {
    val extras = LocalKnightExtras.current
    if (extras.liquidGlassEnabled) {
        LiquidGlassSurface(
            shape = RoundedCornerShape(26.dp),
            variant = GlassVariant.CARD,
            elevation = 3.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(), content = content)
        }
    } else {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = extras.oneUiCard,
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = if (extras.isOled) {
                BorderStroke(0.85.dp, MaterialTheme.colorScheme.outlineVariant)
            } else {
                null
            },
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(), content = content)
        }
    }
}

@Composable
private fun OneUiIconChip(
    icon: ImageVector,
    size: Dp = 38.dp,
    iconSize: Dp = 20.dp
) {
    val extras = LocalKnightExtras.current
    val accent = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(12.dp)
    val chipModifier = if (extras.liquidGlassEnabled) {
        Modifier
            .size(size)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        accent.copy(alpha = if (extras.isDark) 0.24f else 0.17f),
                        accent.copy(alpha = if (extras.isDark) 0.12f else 0.09f)
                    )
                )
            )
            .border(
                width = 0.85.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (extras.isDark) 0.24f else 0.80f),
                        accent.copy(alpha = 0.42f)
                    )
                ),
                shape = shape
            )
    } else {
        Modifier
            .size(size)
            .clip(shape)
            .background(
                accent.copy(
                    alpha = when {
                        extras.isOled -> 0.16f
                        extras.isDark -> 0.18f
                        else -> 0.13f
                    }
                )
            )
    }

    Box(
        modifier = chipModifier,
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
private fun ArtSyncSuggestionCard(
    onDismissBanner: () -> Unit,
    onFetchMissingArt: () -> Unit,
    isIosStyle: Boolean
) {
    val body: @Composable ColumnScope.() -> Unit = {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isIosStyle) {
                    IosCategoryIconChip(
                        icon = KnightIcons.ArtSync,
                        topColor = Color(0xFF818CF8),
                        bottomColor = Color(0xFF4F46E5),
                        size = 34.dp
                    )
                } else {
                    OneUiIconChip(icon = KnightIcons.ArtSync)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Album Art Sync",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "App is offline. Optionally fetch missing covers once for your local albums.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismissBanner) { Text("Dismiss") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onFetchMissingArt,
                    shape = RoundedCornerShape(if (isIosStyle) 20.dp else 14.dp)
                ) {
                    Text("Fetch Missing Art")
                }
            }
        }
    }
    if (isIosStyle) IosGlassGroup(content = body) else OneUiCard(content = body)
}

@Composable
private fun ArtSyncProgressCard(
    artRefreshState: ArtRefreshState,
    onCancelArtFetch: () -> Unit,
    onClearArtStatus: () -> Unit,
    isIosStyle: Boolean
) {
    val body: @Composable ColumnScope.() -> Unit = {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (artRefreshState.isRunning) "Fetching Album Art..." else "Album Art Sync",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!artRefreshState.isRunning) {
                    IconButton(
                        onClick = onClearArtStatus,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = KnightIcons.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            if (artRefreshState.isRunning && artRefreshState.total > 0) {
                LinearProgressIndicator(
                    progress = { (artRefreshState.processed.toFloat() / artRefreshState.total.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${artRefreshState.processed} / ${artRefreshState.total} • ${artRefreshState.currentAlbum}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onCancelArtFetch) { Text("Cancel") }
                }
            } else {
                Text(
                    text = artRefreshState.error ?: artRefreshState.statusMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (artRefreshState.error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
    if (isIosStyle) IosGlassGroup(content = body) else OneUiCard(content = body)
}

@Composable
private fun IosSectionLabel(text: String) {
    val extras = LocalKnightExtras.current
    val labelColor = when {
        extras.isOled -> Color(0xFF9898A4)
        extras.isDark -> Color(0xFFA0A0AE)
        else -> Color(0xFF63636D)
    }
    Text(
        text = text.uppercase(Locale.US),
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            letterSpacing = 0.55.sp
        ),
        color = labelColor,
        modifier = Modifier.padding(horizontal = 30.dp, vertical = 6.dp)
    )
}

@Composable
private fun IosGlassGroup(content: @Composable ColumnScope.() -> Unit) {
    LiquidGlassSurface(
        shape = RoundedCornerShape(22.dp),
        variant = GlassVariant.CARD,
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

@Composable
private fun IosCategoryIconChip(
    icon: ImageVector,
    topColor: Color,
    bottomColor: Color,
    size: Dp = 30.dp
) {
    val corner = if (size >= 34.dp) 9.dp else 8.dp
    val shape = RoundedCornerShape(corner)
    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(Brush.verticalGradient(colors = listOf(topColor, bottomColor)))
            .border(
                width = 0.65.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.65f),
                        Color.White.copy(alpha = 0.12f)
                    )
                ),
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(size * 0.58f)
        )
    }
}

@Composable
private fun IosDivider() {
    val extras = LocalKnightExtras.current
    HorizontalDivider(
        thickness = 0.55.dp,
        color = extras.oneUiDivider,
        modifier = Modifier.padding(start = 58.dp, end = 16.dp)
    )
}

@Composable
private fun IosValueRow(
    icon: ImageVector,
    badgeTop: Color,
    badgeBottom: Color,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IosCategoryIconChip(icon = icon, topColor = badgeTop, bottomColor = badgeBottom)
        Spacer(modifier = Modifier.width(13.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Normal,
                fontSize = 15.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(max = 152.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Icon(
            imageVector = KnightIcons.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f),
            modifier = Modifier.size(15.dp)
        )
    }
}

@Composable
private fun IosSwitchRow(
    icon: ImageVector,
    badgeTop: Color,
    badgeBottom: Color,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 15.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IosCategoryIconChip(icon = icon, topColor = badgeTop, bottomColor = badgeBottom)
        Spacer(modifier = Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        OneUiSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun OneUiDivider() {
    val extras = LocalKnightExtras.current
    HorizontalDivider(
        thickness = 0.6.dp,
        color = extras.oneUiDivider,
        modifier = Modifier.padding(start = 72.dp, end = 20.dp)
    )
}

@Composable
private fun OneUiValueRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OneUiIconChip(icon = icon)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun OneUiSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OneUiIconChip(icon = icon)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        OneUiSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

/**
 * Subtle LiquidUI frosted glass switch when LiquidUI is enabled; clean solid OneUI/M3 switch otherwise.
 */
@Composable
private fun OneUiSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val extras = LocalKnightExtras.current
    if (extras.liquidGlassEnabled) {
        val primary = MaterialTheme.colorScheme.primary
        val checkedProgress by animateFloatAsState(
            targetValue = if (checked) 1f else 0f,
            animationSpec = spring(dampingRatio = 0.74f, stiffness = 380f),
            label = "liquidSwitchProgress"
        )
        val clamped = checkedProgress.coerceIn(0f, 1f)

        val uncheckedTop = when {
            extras.isOled -> Color(0xFF0A0C10).copy(alpha = 0.76f)
            extras.isDark -> Color.White.copy(alpha = 0.16f)
            else -> Color(0xFFD5DFEA).copy(alpha = 0.72f)
        }
        val uncheckedBot = when {
            extras.isOled -> Color(0xFF151922).copy(alpha = 0.82f)
            extras.isDark -> Color.White.copy(alpha = 0.08f)
            else -> Color(0xFFC0CCD9).copy(alpha = 0.80f)
        }
        val checkedTop = lerp(primary, Color.White, if (extras.isDark) 0.18f else 0.24f).copy(alpha = 0.86f)
        val checkedBot = primary.copy(alpha = 0.94f)

        val trackBrush = Brush.verticalGradient(
            colors = listOf(
                lerp(uncheckedTop, checkedTop, clamped),
                lerp(uncheckedBot, checkedBot, clamped)
            )
        )
        val innerSheen = Brush.verticalGradient(
            0f to Color.White.copy(alpha = if (extras.isDark) 0.16f else 0.36f),
            0.52f to Color.Transparent
        )
        val trackRim = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = if (extras.isDark) 0.32f + 0.10f * clamped else 0.78f),
                lerp(
                    Color.White.copy(alpha = if (extras.isDark) 0.12f else 0.26f),
                    primary.copy(alpha = 0.42f),
                    clamped
                )
            )
        )

        val stretchDp = (sin(clamped * PI.toFloat()) * 3.5f).dp
        val thumbWidth = 22.dp + stretchDp
        val thumbHeight = 22.dp - (stretchDp * 0.28f)
        val thumbX = (3.dp + (22.dp * checkedProgress) - (stretchDp / 2f)).coerceIn(2.5.dp, 25.5.dp)

        val thumbFill = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.98f),
                lerp(Color(0xFFEEF3F8), primary, 0.08f * clamped).copy(alpha = 0.92f)
            )
        )
        val thumbRim = Brush.verticalGradient(
            colors = listOf(
                Color.White,
                Color.White.copy(alpha = 0.45f)
            )
        )

        Box(
            modifier = Modifier
                .width(50.dp)
                .height(28.dp)
                .clip(CircleShape)
                .background(trackBrush)
                .background(innerSheen)
                .border(0.85.dp, trackRim, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onCheckedChange(!checked) },
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(thumbX.roundToPx(), 0) }
                    .width(thumbWidth)
                    .height(thumbHeight)
                    .shadow(2.5.dp, CircleShape, ambientColor = primary.copy(alpha = 0.22f * clamped))
                    .clip(CircleShape)
                    .background(thumbFill)
                    .border(0.75.dp, thumbRim, CircleShape)
            )
        }
        return
    }

    val trackColor by animateColorAsState(
        targetValue = when {
            checked -> MaterialTheme.colorScheme.primary
            extras.isOled -> Color.Black
            extras.isDark -> Color(0xFF47474E)
            else -> Color(0xFFC5C5CB)
        },
        animationSpec = tween(180),
        label = "switchTrack"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 23.dp else 3.dp,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 420f),
        label = "switchThumb"
    )

    Box(
        modifier = Modifier
            .width(48.dp)
            .height(28.dp)
            .clip(CircleShape)
            .background(trackColor)
            .then(
                if (!checked && extras.isOled) {
                    Modifier.border(1.3.dp, Color(0xFF565664), CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(thumbOffset.roundToPx(), 0) }
                .size(22.dp)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
private fun <T> OneUiRadioDialog(
    title: String,
    options: List<T>,
    selected: T,
    labelOf: (T) -> String,
    subtitleOf: ((T) -> String)? = null,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit
) {
    KnightDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelect(option) }
                            .padding(horizontal = 6.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = option == selected, onClick = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = labelOf(option),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (option == selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                            subtitleOf?.invoke(option)?.let { sub ->
                                Text(
                                    text = sub,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BuiltInEqualizerSheet(
    currentPreset: EqualizerPreset,
    customBands: List<Float>,
    audioSessionId: Int,
    onSelectPreset: (EqualizerPreset) -> Unit,
    onUpdateCustomBands: (List<Float>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val bandLabels = remember { listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz") }
    val activeBandsSource = if (currentPreset == EqualizerPreset.CUSTOM) customBands else currentPreset.bandDb
    var draftBands by remember(currentPreset, customBands) {
        mutableStateOf(List(5) { idx -> activeBandsSource.getOrElse(idx) { 0f } })
    }

    val eqPanelIntent = remember(audioSessionId, context) {
        Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
            putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
            putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
            putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
        }
    }
    val hasSystemPanel = remember(eqPanelIntent) {
        runCatching { eqPanelIntent.resolveActivity(context.packageManager) != null }.getOrDefault(false)
    }

    KnightModalSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Equalizer",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Active preset: ${currentPreset.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (currentPreset != EqualizerPreset.DEFAULT) {
                    TextButton(onClick = { onSelectPreset(EqualizerPreset.DEFAULT) }) {
                        Text("Reset")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Presets",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            val selectablePresets = remember(currentPreset) {
                val base = listOf(
                    EqualizerPreset.DEFAULT,
                    EqualizerPreset.JAZZ,
                    EqualizerPreset.TECHNO,
                    EqualizerPreset.BLUES,
                    EqualizerPreset.POP,
                    EqualizerPreset.ROCK,
                    EqualizerPreset.OPERA
                )
                if (currentPreset == EqualizerPreset.CUSTOM) base + EqualizerPreset.CUSTOM else base
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectablePresets.forEach { preset ->
                    val isSelected = preset == currentPreset
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (preset != EqualizerPreset.CUSTOM) {
                                onSelectPreset(preset)
                            }
                        },
                        label = {
                            Text(
                                text = preset.label,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "5-Band Frequency Response",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            bandLabels.forEachIndexed { idx, freqLabel ->
                val db = draftBands.getOrElse(idx) { 0f }
                val formattedDb = if (db > 0f) {
                    String.format(Locale.US, "+%.1f dB", db)
                } else {
                    String.format(Locale.US, "%.1f dB", db)
                }
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = freqLabel,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = formattedDb,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (db == 0f) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = db,
                        onValueChange = { raw ->
                            val snapped = ((raw * 2f).roundToInt() / 2f).coerceIn(-12f, 12f)
                            draftBands = draftBands.toMutableList().apply { set(idx, snapped) }
                        },
                        onValueChangeFinished = {
                            onUpdateCustomBands(draftBands)
                        },
                        valueRange = -12f..12f
                    )
                }
            }

            if (hasSystemPanel) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            runCatching { context.startActivity(eqPanelIntent) }
                        }
                    ) {
                        Text("Open Device EQ Panel")
                    }
                }
            }
        }
    }
}
