package com.knightwin.music.data.model

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val trackNumber: Int,
    val year: Int,
    val dateAddedSec: Long,
    val sizeBytes: Long,
    val mimeType: String,
    val contentUri: Uri,
    val albumArtUri: Uri,
    val folderName: String,
    val filePath: String
)

data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val year: Int,
    val songCount: Int,
    val artUri: Uri,
    val sampleSongUri: Uri,
    val totalDurationMs: Long
)

data class Artist(
    val id: String,
    val name: String,
    val albumCount: Int,
    val songCount: Int,
    val artUri: Uri,
    val sampleSongUri: Uri,
    val sampleAlbumName: String
)

data class Playlist(
    val id: String,
    val name: String,
    val songIds: List<Long>,
    val createdAt: Long
)

enum class MainTab(val label: String) {
    HOME("Home"),
    LIBRARY("Library"),
    SEARCH("Search")
}

enum class LibrarySubTab(val label: String) {
    SONGS("Songs"),
    ALBUMS("Albums"),
    ARTISTS("Artists"),
    PLAYLISTS("Playlists")
}

enum class GreeterMode(val label: String) {
    RANDOMIZED("Randomized"),
    CUSTOM("Custom")
}

object GreeterCatalog {
    val titles = listOf(
        "Virtuoso",
        "Maestro",
        "Audiophile",
        "Vinyl Soul",
        "Night Owl",
        "Soundsmith",
        "Beat Seeker",
        "Sonic Explorer",
        "Melody Chaser",
        "Crate Digger",
        "Hi-Fi Purist",
        "Rhythm Keeper",
        "Tempo Rider",
        "Groove Architect",
        "Echo Wanderer",
        "Chord Weaver",
        "Tune Alchemist",
        "Acoustic Dreamer"
    )
}

enum class SortOrder(val label: String) {
    TITLE("Title (A–Z)"),
    ARTIST("Artist"),
    ALBUM("Album"),
    DATE_ADDED("Recently Added"),
    DURATION("Duration")
}

enum class ThemeMode(val label: String) {
    SYSTEM("Follow System"),
    LIGHT("Light"),
    DARK("Dark")
}

enum class AccentTheme(
    val label: String,
    val primaryHex: Long,
    val secondaryHex: Long,
    val tertiaryHex: Long
) {
    DYNAMIC("Dynamic", 0xFF27B1FE, 0xFF0A84FF, 0xFF38BDF8),
    KNIGHTWIN_BLUE("KnightWin Blue", 0xFF27B1FE, 0xFF0284C7, 0xFF7DD3FC),
    APPLE_ROSE("Crimson Rose", 0xFFFA2D48, 0xFFE11D48, 0xFFFB7185),
    TUHKAR_EMERALD("Matcha Dessert", 0xFF6FA355, 0xFF4F7D39, 0xFF9AC97E),
    ROYAL_VIOLET("Royal Violet", 0xFF8B5CF6, 0xFF6D28D9, 0xFFA78BFA),
    SUNSET_AMBER("Sunset Amber", 0xFFF59E0B, 0xFFD97706, 0xFFFBBF24),
    CYAN_WAVE("Cyan Wave", 0xFF06B6D4, 0xFF0891B2, 0xFF22D3EE),
    CORAL_FLAME("Coral Sunset", 0xFFFF6B4A, 0xFFE5482E, 0xFFFF8A65),
    MINT_JADE("Mint Jade", 0xFF14B8A6, 0xFF0F766E, 0xFF2DD4BF),
    MONOCHROME("Monochrome Slate", 0xFF64748B, 0xFF475569, 0xFF94A3B8)
}

enum class NowPlayingStyle(val label: String, val description: String) {
    SPINNING_DISC("Spinning CD", "Rotating compact disc with album art & jewel hub"),
    CLASSIC_ART("Classic", "Vintage LP sleeve with spinning vinyl that coasts to a stop"),
    BEAT_GLOW("Ambient Glow", "Luminous animated color aura & halo framing cover art")
}

enum class AlbumLayout(val label: String) {
    GRID("2-Column Grid"),
    LIST("Detailed List")
}

enum class ReverbPreset(val label: String, val presetValue: Short) {
    NONE("Off", 0),
    SMALL_ROOM("Small Room", 1),
    MEDIUM_ROOM("Medium Room", 2),
    LARGE_ROOM("Large Room", 3),
    MEDIUM_HALL("Medium Hall", 4),
    LARGE_HALL("Large Hall", 5),
    PLATE("Studio Plate", 6)
}

enum class EqualizerPreset(
    val label: String,
    val bandDb: List<Float> // 5 anchor bands: 60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz (-12..+12 dB)
) {
    DEFAULT("Default", listOf(0f, 0f, 0f, 0f, 0f)),
    JAZZ("Jazz", listOf(3.5f, 2.0f, -1.5f, 2.0f, 4.0f)),
    TECHNO("Techno", listOf(5.5f, 3.5f, 0.0f, 3.0f, 5.0f)),
    BLUES("Blues", listOf(2.5f, 3.5f, 1.0f, 2.5f, 1.5f)),
    POP("Pop", listOf(-1.0f, 2.5f, 4.0f, 2.5f, -1.0f)),
    ROCK("Rock", listOf(4.5f, 2.5f, -1.5f, 3.0f, 4.5f)),
    OPERA("Opera", listOf(1.5f, 1.0f, 3.0f, 4.0f, 2.5f)),
    CUSTOM("Custom", listOf(0f, 0f, 0f, 0f, 0f))
}

enum class MinDurationFilter(val label: String, val durationMs: Long) {
    OFF("Show All Audio", 0L),
    SEC_15("Ignore < 15 sec", 15_000L),
    SEC_30("Ignore < 30 sec", 30_000L),
    SEC_60("Ignore < 1 min", 60_000L)
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val oledBlack: Boolean = false,
    val accentTheme: AccentTheme = AccentTheme.KNIGHTWIN_BLUE,
    val nowPlayingStyle: NowPlayingStyle = NowPlayingStyle.SPINNING_DISC,
    val albumLayout: AlbumLayout = AlbumLayout.GRID,
    val artistLayout: AlbumLayout = AlbumLayout.GRID,
    val liquidGlassAccents: Boolean = true,
    val greeterMode: GreeterMode = GreeterMode.RANDOMIZED,
    val customGreeterText: String = "",
    val sortOrder: SortOrder = SortOrder.TITLE,
    val minDurationFilter: MinDurationFilter = MinDurationFilter.SEC_30,
    val crossfadeMs: Int = 0,
    val playbackSpeed: Float = 1.0f,
    val reverbPreset: ReverbPreset = ReverbPreset.NONE,
    val eqPreset: EqualizerPreset = EqualizerPreset.DEFAULT,
    val eqCustomBands: List<Float> = listOf(0f, 0f, 0f, 0f, 0f),
    val customLogoPath: String = "",
    val keepScreenOn: Boolean = false,
    val preferEmbeddedArt: Boolean = true,
    val firstRunArtPromptShown: Boolean = false,
    val lastArtSyncEpochMs: Long = 0L,
    val bannerDismissed: Boolean = false
)

data class ArtRefreshState(
    val isRunning: Boolean = false,
    val processed: Int = 0,
    val total: Int = 0,
    val fetched: Int = 0,
    val skipped: Int = 0,
    val currentAlbum: String = "",
    val statusMessage: String = "",
    val completed: Boolean = false,
    val error: String? = null
)

data class PlaybackUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffleEnabled: Boolean = false,
    val repeatMode: Int = 0,
    val queue: List<Song> = emptyList(),
    val currentQueueIndex: Int = -1,
    val sleepTimerRemainingMs: Long = 0L,
    val sleepTimerEndOfTrack: Boolean = false,
    val audioSessionId: Int = 0
)
