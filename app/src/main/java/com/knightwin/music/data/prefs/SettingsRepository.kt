package com.knightwin.music.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.knightwin.music.data.model.AccentTheme
import com.knightwin.music.data.model.AlbumLayout
import com.knightwin.music.data.model.AppSettings
import com.knightwin.music.data.model.EqualizerPreset
import com.knightwin.music.data.model.GreeterMode
import com.knightwin.music.data.model.MinDurationFilter
import com.knightwin.music.data.model.NowPlayingStyle
import com.knightwin.music.data.model.Playlist
import com.knightwin.music.data.model.ReverbPreset
import com.knightwin.music.data.model.SortOrder
import com.knightwin.music.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "knightwin_prefs")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val OLED_BLACK = booleanPreferencesKey("oled_black")
        val ACCENT_THEME = stringPreferencesKey("accent_theme")
        val NOW_PLAYING_STYLE = stringPreferencesKey("now_playing_style")
        val ALBUM_LAYOUT = stringPreferencesKey("album_layout")
        val ARTIST_LAYOUT = stringPreferencesKey("artist_layout")
        val LIQUID_GLASS = booleanPreferencesKey("liquid_glass_accents")
        val GREETER_MODE = stringPreferencesKey("greeter_mode")
        val CUSTOM_GREETER_TEXT = stringPreferencesKey("custom_greeter_text")
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val MIN_DURATION = stringPreferencesKey("min_duration_filter")
        val CROSSFADE_MS = intPreferencesKey("crossfade_ms")
        val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        val REVERB_PRESET = stringPreferencesKey("reverb_preset")
        val EQ_PRESET = stringPreferencesKey("eq_preset")
        val EQ_CUSTOM_BANDS = stringPreferencesKey("eq_custom_bands_json")
        val CUSTOM_LOGO_PATH = stringPreferencesKey("custom_logo_path")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val PREFER_EMBEDDED_ART = booleanPreferencesKey("prefer_embedded_art")
        val FIRST_RUN_ART_PROMPT = booleanPreferencesKey("first_run_art_prompt_shown")
        val LAST_ART_SYNC = longPreferencesKey("last_art_sync_ms")
        val BANNER_DISMISSED = booleanPreferencesKey("banner_dismissed")

        val FAVORITE_IDS = stringPreferencesKey("favorite_ids_json")
        val PLAYLISTS = stringPreferencesKey("playlists_json")
        val RECENTLY_PLAYED = stringPreferencesKey("recently_played_json")
        val PLAY_COUNTS = stringPreferencesKey("play_counts_json")
        val RECENT_SEARCHES = stringPreferencesKey("recent_searches_json")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            themeMode = enumOrDefault(prefs[Keys.THEME_MODE], ThemeMode.SYSTEM),
            oledBlack = prefs[Keys.OLED_BLACK] ?: false,
            accentTheme = enumOrDefault(prefs[Keys.ACCENT_THEME], AccentTheme.KNIGHTWIN_BLUE),
            nowPlayingStyle = enumOrDefault(prefs[Keys.NOW_PLAYING_STYLE], NowPlayingStyle.SPINNING_DISC),
            albumLayout = enumOrDefault(prefs[Keys.ALBUM_LAYOUT], AlbumLayout.GRID),
            artistLayout = enumOrDefault(prefs[Keys.ARTIST_LAYOUT], AlbumLayout.GRID),
            liquidGlassAccents = prefs[Keys.LIQUID_GLASS] ?: true,
            greeterMode = enumOrDefault(prefs[Keys.GREETER_MODE], GreeterMode.RANDOMIZED),
            customGreeterText = prefs[Keys.CUSTOM_GREETER_TEXT] ?: "",
            sortOrder = enumOrDefault(prefs[Keys.SORT_ORDER], SortOrder.TITLE),
            minDurationFilter = enumOrDefault(prefs[Keys.MIN_DURATION], MinDurationFilter.SEC_30),
            crossfadeMs = prefs[Keys.CROSSFADE_MS] ?: 0,
            playbackSpeed = (prefs[Keys.PLAYBACK_SPEED] ?: 1.0f).coerceIn(0.5f, 2.0f),
            reverbPreset = enumOrDefault(prefs[Keys.REVERB_PRESET], ReverbPreset.NONE),
            eqPreset = enumOrDefault(prefs[Keys.EQ_PRESET], EqualizerPreset.DEFAULT),
            eqCustomBands = parseFloatList(prefs[Keys.EQ_CUSTOM_BANDS], size = 5),
            customLogoPath = prefs[Keys.CUSTOM_LOGO_PATH] ?: "",
            keepScreenOn = prefs[Keys.KEEP_SCREEN_ON] ?: false,
            preferEmbeddedArt = prefs[Keys.PREFER_EMBEDDED_ART] ?: true,
            firstRunArtPromptShown = prefs[Keys.FIRST_RUN_ART_PROMPT] ?: false,
            lastArtSyncEpochMs = prefs[Keys.LAST_ART_SYNC] ?: 0L,
            bannerDismissed = prefs[Keys.BANNER_DISMISSED] ?: false
        )
    }

    val favoriteIdsFlow: Flow<Set<Long>> = context.dataStore.data.map { prefs ->
        parseLongList(prefs[Keys.FAVORITE_IDS]).toSet()
    }

    val playlistsFlow: Flow<List<Playlist>> = context.dataStore.data.map { prefs ->
        parsePlaylists(prefs[Keys.PLAYLISTS])
    }

    val recentlyPlayedFlow: Flow<List<Long>> = context.dataStore.data.map { prefs ->
        parseLongList(prefs[Keys.RECENTLY_PLAYED])
    }

    val playCountsFlow: Flow<Map<Long, Int>> = context.dataStore.data.map { prefs ->
        parsePlayCounts(prefs[Keys.PLAY_COUNTS])
    }

    val recentSearchesFlow: Flow<List<String>> = context.dataStore.data.map { prefs ->
        parseStringList(prefs[Keys.RECENT_SEARCHES])
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setOledBlack(enabled: Boolean) {
        context.dataStore.edit { it[Keys.OLED_BLACK] = enabled }
    }

    suspend fun setAccentTheme(theme: AccentTheme) {
        context.dataStore.edit { it[Keys.ACCENT_THEME] = theme.name }
    }

    suspend fun setNowPlayingStyle(style: NowPlayingStyle) {
        context.dataStore.edit { it[Keys.NOW_PLAYING_STYLE] = style.name }
    }

    suspend fun setAlbumLayout(layout: AlbumLayout) {
        context.dataStore.edit { it[Keys.ALBUM_LAYOUT] = layout.name }
    }

    suspend fun setArtistLayout(layout: AlbumLayout) {
        context.dataStore.edit { it[Keys.ARTIST_LAYOUT] = layout.name }
    }

    suspend fun setLiquidGlassAccents(enabled: Boolean) {
        context.dataStore.edit { it[Keys.LIQUID_GLASS] = enabled }
    }

    suspend fun setGreeterMode(mode: GreeterMode) {
        context.dataStore.edit { it[Keys.GREETER_MODE] = mode.name }
    }

    suspend fun setCustomGreeterText(text: String) {
        context.dataStore.edit { it[Keys.CUSTOM_GREETER_TEXT] = text.trim().take(36) }
    }

    suspend fun setSortOrder(order: SortOrder) {
        context.dataStore.edit { it[Keys.SORT_ORDER] = order.name }
    }

    suspend fun setMinDurationFilter(filter: MinDurationFilter) {
        context.dataStore.edit { it[Keys.MIN_DURATION] = filter.name }
    }

    suspend fun setCrossfadeMs(ms: Int) {
        context.dataStore.edit { it[Keys.CROSSFADE_MS] = ms.coerceIn(0, 2000) }
    }

    suspend fun setPlaybackSpeed(speed: Float) {
        context.dataStore.edit { it[Keys.PLAYBACK_SPEED] = speed.coerceIn(0.5f, 2.0f) }
    }

    suspend fun setReverbPreset(preset: ReverbPreset) {
        context.dataStore.edit { it[Keys.REVERB_PRESET] = preset.name }
    }

    suspend fun setEqualizerPreset(preset: EqualizerPreset) {
        context.dataStore.edit { it[Keys.EQ_PRESET] = preset.name }
    }

    suspend fun setEqualizerCustomBands(bands: List<Float>) {
        val normalized = List(5) { idx -> (bands.getOrNull(idx) ?: 0f).coerceIn(-12f, 12f) }
        context.dataStore.edit {
            it[Keys.EQ_PRESET] = EqualizerPreset.CUSTOM.name
            it[Keys.EQ_CUSTOM_BANDS] = encodeFloatList(normalized)
        }
    }

    suspend fun setCustomLogoPath(path: String) {
        context.dataStore.edit { it[Keys.CUSTOM_LOGO_PATH] = path }
    }

    suspend fun setKeepScreenOn(enabled: Boolean) {
        context.dataStore.edit { it[Keys.KEEP_SCREEN_ON] = enabled }
    }

    suspend fun setPreferEmbeddedArt(prefer: Boolean) {
        context.dataStore.edit { it[Keys.PREFER_EMBEDDED_ART] = prefer }
    }

    suspend fun setFirstRunArtPromptShown(shown: Boolean) {
        context.dataStore.edit { it[Keys.FIRST_RUN_ART_PROMPT] = shown }
    }

    suspend fun setLastArtSyncEpochMs(epochMs: Long) {
        context.dataStore.edit { it[Keys.LAST_ART_SYNC] = epochMs }
    }

    suspend fun setBannerDismissed(dismissed: Boolean) {
        context.dataStore.edit { it[Keys.BANNER_DISMISSED] = dismissed }
    }

    suspend fun toggleFavorite(songId: Long) {
        context.dataStore.edit { prefs ->
            val list = parseLongList(prefs[Keys.FAVORITE_IDS]).toMutableList()
            if (list.contains(songId)) {
                list.remove(songId)
            } else {
                list.add(0, songId)
            }
            prefs[Keys.FAVORITE_IDS] = encodeLongList(list)
        }
    }

    suspend fun recordSongPlayed(songId: Long) {
        context.dataStore.edit { prefs ->
            val recent = parseLongList(prefs[Keys.RECENTLY_PLAYED]).toMutableList()
            recent.remove(songId)
            recent.add(0, songId)
            prefs[Keys.RECENTLY_PLAYED] = encodeLongList(recent.take(50))

            val counts = parsePlayCounts(prefs[Keys.PLAY_COUNTS]).toMutableMap()
            counts[songId] = (counts[songId] ?: 0) + 1
            prefs[Keys.PLAY_COUNTS] = encodePlayCounts(counts)
        }
    }

    suspend fun createPlaylist(name: String, initialSongIds: List<Long> = emptyList()) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return
        context.dataStore.edit { prefs ->
            val current = parsePlaylists(prefs[Keys.PLAYLISTS]).toMutableList()
            val id = "pl_${System.currentTimeMillis()}"
            current.add(
                0,
                Playlist(
                    id = id,
                    name = cleanName,
                    songIds = initialSongIds.distinct(),
                    createdAt = System.currentTimeMillis()
                )
            )
            prefs[Keys.PLAYLISTS] = encodePlaylists(current)
        }
    }

    suspend fun renamePlaylist(playlistId: String, newName: String) {
        val cleanName = newName.trim()
        if (cleanName.isEmpty()) return
        context.dataStore.edit { prefs ->
            val updated = parsePlaylists(prefs[Keys.PLAYLISTS]).map {
                if (it.id == playlistId) it.copy(name = cleanName) else it
            }
            prefs[Keys.PLAYLISTS] = encodePlaylists(updated)
        }
    }

    suspend fun deletePlaylist(playlistId: String) {
        context.dataStore.edit { prefs ->
            val updated = parsePlaylists(prefs[Keys.PLAYLISTS]).filterNot { it.id == playlistId }
            prefs[Keys.PLAYLISTS] = encodePlaylists(updated)
        }
    }

    suspend fun addSongToPlaylist(playlistId: String, songId: Long) {
        context.dataStore.edit { prefs ->
            val updated = parsePlaylists(prefs[Keys.PLAYLISTS]).map { pl ->
                if (pl.id == playlistId && !pl.songIds.contains(songId)) {
                    pl.copy(songIds = pl.songIds + songId)
                } else {
                    pl
                }
            }
            prefs[Keys.PLAYLISTS] = encodePlaylists(updated)
        }
    }

    suspend fun removeSongFromPlaylist(playlistId: String, songId: Long) {
        context.dataStore.edit { prefs ->
            val updated = parsePlaylists(prefs[Keys.PLAYLISTS]).map { pl ->
                if (pl.id == playlistId) {
                    pl.copy(songIds = pl.songIds.filterNot { it == songId })
                } else {
                    pl
                }
            }
            prefs[Keys.PLAYLISTS] = encodePlaylists(updated)
        }
    }

    suspend fun addRecentSearch(query: String) {
        val clean = query.trim()
        if (clean.length < 2) return
        context.dataStore.edit { prefs ->
            val current = parseStringList(prefs[Keys.RECENT_SEARCHES]).toMutableList()
            current.removeAll { it.equals(clean, ignoreCase = true) }
            current.add(0, clean)
            prefs[Keys.RECENT_SEARCHES] = encodeStringList(current.take(10))
        }
    }

    suspend fun clearRecentSearches() {
        context.dataStore.edit { it.remove(Keys.RECENT_SEARCHES) }
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(raw: String?, default: T): T {
        if (raw.isNullOrBlank()) return default
        return runCatching { enumValueOf<T>(raw) }.getOrDefault(default)
    }

    private fun parseLongList(raw: String?): List<Long> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            List(arr.length()) { arr.getLong(it) }
        }.getOrDefault(emptyList())
    }

    private fun encodeLongList(items: List<Long>): String {
        val arr = JSONArray()
        items.forEach { arr.put(it) }
        return arr.toString()
    }

    private fun parseStringList(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            List(arr.length()) { arr.getString(it) }
        }.getOrDefault(emptyList())
    }

    private fun encodeStringList(items: List<String>): String {
        val arr = JSONArray()
        items.forEach { arr.put(it) }
        return arr.toString()
    }

    private fun parsePlayCounts(raw: String?): Map<Long, Int> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching {
            val obj = JSONObject(raw)
            val result = mutableMapOf<Long, Int>()
            obj.keys().forEach { key ->
                key.toLongOrNull()?.let { id -> result[id] = obj.optInt(key, 0) }
            }
            result
        }.getOrDefault(emptyMap())
    }

    private fun encodePlayCounts(counts: Map<Long, Int>): String {
        val obj = JSONObject()
        counts.forEach { (id, count) -> obj.put(id.toString(), count) }
        return obj.toString()
    }

    private fun parsePlaylists(raw: String?): List<Playlist> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            List(arr.length()) { idx ->
                val obj = arr.getJSONObject(idx)
                val songsArr = obj.optJSONArray("songIds") ?: JSONArray()
                val songIds = List(songsArr.length()) { sIdx -> songsArr.getLong(sIdx) }
                Playlist(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    songIds = songIds,
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun encodePlaylists(playlists: List<Playlist>): String {
        val arr = JSONArray()
        playlists.forEach { pl ->
            val songsArr = JSONArray()
            pl.songIds.forEach { songsArr.put(it) }
            arr.put(
                JSONObject().apply {
                    put("id", pl.id)
                    put("name", pl.name)
                    put("songIds", songsArr)
                    put("createdAt", pl.createdAt)
                }
            )
        }
        return arr.toString()
    }

    private fun parseFloatList(raw: String?, size: Int): List<Float> {
        if (raw.isNullOrBlank()) return List(size) { 0f }
        return runCatching {
            val arr = JSONArray(raw)
            List(size) { idx ->
                if (idx < arr.length()) arr.getDouble(idx).toFloat().coerceIn(-12f, 12f) else 0f
            }
        }.getOrDefault(List(size) { 0f })
    }

    private fun encodeFloatList(items: List<Float>): String {
        val arr = JSONArray()
        items.forEach { arr.put(it.toDouble()) }
        return arr.toString()
    }
}
