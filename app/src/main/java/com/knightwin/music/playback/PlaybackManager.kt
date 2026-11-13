package com.knightwin.music.playback

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.knightwin.music.data.model.PlaybackUiState
import com.knightwin.music.data.model.Song
import com.knightwin.music.data.prefs.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlaybackManager(
    private val context: Context,
    private val scope: CoroutineScope,
    private val settingsRepository: SettingsRepository
) {
    private val _state = MutableStateFlow(PlaybackUiState())
    val state: StateFlow<PlaybackUiState> = _state.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private var songsById: Map<Long, Song> = emptyMap()

    private var tickerJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var crossfadeMs: Int = 0
    private var lastRecordedSongId: Long = -1L

    fun updateLibraryLookup(songs: List<Song>) {
        songsById = songs.associateBy { it.id }
        syncStateFromController()
    }

    fun setCrossfadeMs(ms: Int) {
        crossfadeMs = ms.coerceIn(0, 2000)
        if (crossfadeMs == 0) {
            controller?.volume = 1.0f
        }
    }

    fun connect() {
        if (controllerFuture != null) return
        val sessionToken = SessionToken(context, ComponentName(context, KnightPlaybackService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture = future
        future.addListener(
            {
                runCatching {
                    val mc = future.get()
                    controller = mc
                    attachListener(mc)
                    syncStateFromController()
                    fetchAudioSessionId(mc)
                    startTicker()
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    private fun fetchAudioSessionId(mc: MediaController) {
        val cmd = SessionCommand(KnightPlaybackService.CMD_GET_AUDIO_SESSION, Bundle.EMPTY)
        val resFuture = mc.sendCustomCommand(cmd, Bundle.EMPTY)
        resFuture.addListener(
            {
                runCatching {
                    val result = resFuture.get()
                    val sessionId = result.extras.getInt(KnightPlaybackService.EXTRA_AUDIO_SESSION_ID, 0)
                    _state.update { it.copy(audioSessionId = sessionId) }
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    private fun attachListener(mc: MediaController) {
        mc.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    val currentId = mc.currentMediaItem?.mediaId?.toLongOrNull()
                    if (currentId != null && currentId != lastRecordedSongId) {
                        lastRecordedSongId = currentId
                        scope.launch { settingsRepository.recordSongPlayed(currentId) }
                    }
                    fadeInIfCrossfading(mc)
                }
                syncStateFromController()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val id = mediaItem?.mediaId?.toLongOrNull()
                if (id != null && id != lastRecordedSongId && mc.playWhenReady) {
                    lastRecordedSongId = id
                    scope.launch { settingsRepository.recordSongPlayed(id) }
                }
                if (_state.value.sleepTimerEndOfTrack && reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                    mc.pause()
                    cancelSleepTimer()
                }
                fadeInIfCrossfading(mc)
                syncStateFromController()
                fetchAudioSessionId(mc)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED && _state.value.sleepTimerEndOfTrack) {
                    cancelSleepTimer()
                }
                syncStateFromController()
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                syncStateFromController()
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                syncStateFromController()
            }

            override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
                syncStateFromController()
            }
        })
    }

    private fun fadeInIfCrossfading(mc: MediaController) {
        if (crossfadeMs <= 0) {
            mc.volume = 1.0f
            return
        }
        scope.launch {
            val steps = 10
            val stepDelay = (crossfadeMs / 2L / steps).coerceAtLeast(20L)
            for (i in 1..steps) {
                mc.volume = (0.25f + 0.75f * (i.toFloat() / steps)).coerceIn(0f, 1f)
                delay(stepDelay)
            }
            mc.volume = 1.0f
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                val mc = controller
                if (mc != null && mc.isConnected) {
                    val pos = mc.currentPosition.coerceAtLeast(0L)
                    val dur = mc.duration.takeIf { it > 0L } ?: _state.value.currentSong?.durationMs ?: 0L

                    if (crossfadeMs > 0 && mc.isPlaying && dur > crossfadeMs * 2L) {
                        val remaining = dur - pos
                        if (remaining in 1..crossfadeMs) {
                            val ratio = (remaining.toFloat() / crossfadeMs.toFloat()).coerceIn(0.2f, 1f)
                            mc.volume = ratio
                        } else if (pos > crossfadeMs && mc.volume < 0.99f) {
                            mc.volume = 1.0f
                        }
                    }

                    _state.update {
                        it.copy(
                            positionMs = pos,
                            durationMs = dur,
                            isPlaying = mc.isPlaying
                        )
                    }
                }
                delay(250L)
            }
        }
    }

    private fun syncStateFromController() {
        val mc = controller ?: return
        if (!mc.isConnected) return

        val queue = (0 until mc.mediaItemCount).mapNotNull { idx ->
            val id = mc.getMediaItemAt(idx).mediaId.toLongOrNull()
            if (id != null) songsById[id] else null
        }
        val currentIdx = mc.currentMediaItemIndex.takeIf { it in queue.indices } ?: -1
        val currentSong = queue.getOrNull(currentIdx)
            ?: mc.currentMediaItem?.mediaId?.toLongOrNull()?.let { songsById[it] }

        _state.update {
            it.copy(
                currentSong = currentSong,
                isPlaying = mc.isPlaying,
                positionMs = mc.currentPosition.coerceAtLeast(0L),
                durationMs = mc.duration.takeIf { d -> d > 0L } ?: currentSong?.durationMs ?: 0L,
                shuffleEnabled = mc.shuffleModeEnabled,
                repeatMode = mc.repeatMode,
                queue = queue,
                currentQueueIndex = currentIdx
            )
        }
    }

    fun playSongs(songs: List<Song>, startIndex: Int = 0, shuffle: Boolean = false) {
        val mc = controller ?: return
        if (songs.isEmpty()) return

        val items = songs.map { it.toMediaItem() }
        val safeIndex = if (shuffle) songs.indices.random() else startIndex.coerceIn(0, songs.lastIndex)

        mc.shuffleModeEnabled = shuffle
        mc.volume = 1.0f
        mc.setMediaItems(items, safeIndex, 0L)
        mc.prepare()
        mc.play()
        syncStateFromController()
    }

    fun togglePlayPause() {
        val mc = controller ?: return
        if (mc.isPlaying) {
            mc.pause()
        } else {
            mc.volume = 1.0f
            if (mc.playbackState == Player.STATE_IDLE) {
                mc.prepare()
            }
            mc.play()
        }
        syncStateFromController()
    }

    fun seekTo(positionMs: Long) {
        val mc = controller ?: return
        mc.seekTo(positionMs.coerceAtLeast(0L))
        _state.update { it.copy(positionMs = positionMs.coerceAtLeast(0L)) }
    }

    fun skipNext() {
        val mc = controller ?: return
        mc.volume = 1.0f
        if (mc.hasNextMediaItem()) {
            mc.seekToNextMediaItem()
        } else if (mc.mediaItemCount > 0) {
            mc.seekTo(0, 0L)
        }
        if (!mc.isPlaying) mc.play()
        syncStateFromController()
    }

    fun skipPrevious() {
        val mc = controller ?: return
        mc.volume = 1.0f
        if (mc.currentPosition > 3500L) {
            mc.seekTo(0L)
        } else if (mc.hasPreviousMediaItem()) {
            mc.seekToPreviousMediaItem()
        } else {
            mc.seekTo(0L)
        }
        if (!mc.isPlaying) mc.play()
        syncStateFromController()
    }

    fun toggleShuffle() {
        val mc = controller ?: return
        mc.shuffleModeEnabled = !mc.shuffleModeEnabled
        syncStateFromController()
    }

    fun cycleRepeatMode() {
        val mc = controller ?: return
        mc.repeatMode = when (mc.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        syncStateFromController()
    }

    fun playNext(song: Song) {
        val mc = controller ?: return
        if (mc.mediaItemCount == 0) {
            playSongs(listOf(song))
            return
        }
        val insertIndex = (mc.currentMediaItemIndex + 1).coerceIn(0, mc.mediaItemCount)
        mc.addMediaItem(insertIndex, song.toMediaItem())
        syncStateFromController()
    }

    fun addToQueue(song: Song) {
        val mc = controller ?: return
        if (mc.mediaItemCount == 0) {
            playSongs(listOf(song))
            return
        }
        mc.addMediaItem(song.toMediaItem())
        syncStateFromController()
    }

    fun jumpToQueueIndex(index: Int) {
        val mc = controller ?: return
        if (index in 0 until mc.mediaItemCount) {
            mc.volume = 1.0f
            mc.seekTo(index, 0L)
            mc.play()
            syncStateFromController()
        }
    }

    fun removeFromQueue(index: Int) {
        val mc = controller ?: return
        if (index in 0 until mc.mediaItemCount && index != mc.currentMediaItemIndex) {
            mc.removeMediaItem(index)
            syncStateFromController()
        }
    }

    fun clearUpcomingQueue() {
        val mc = controller ?: return
        val currentIdx = mc.currentMediaItemIndex
        for (i in mc.mediaItemCount - 1 downTo 0) {
            if (i != currentIdx) {
                mc.removeMediaItem(i)
            }
        }
        syncStateFromController()
    }

    fun setSleepTimerMinutes(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _state.update { it.copy(sleepTimerRemainingMs = 0L, sleepTimerEndOfTrack = false) }
            return
        }
        val totalMs = minutes * 60_000L
        _state.update { it.copy(sleepTimerRemainingMs = totalMs, sleepTimerEndOfTrack = false) }
        sleepTimerJob = scope.launch {
            var remaining = totalMs
            while (remaining > 0L && isActive) {
                delay(1000L)
                remaining = (remaining - 1000L).coerceAtLeast(0L)
                _state.update { it.copy(sleepTimerRemainingMs = remaining) }
            }
            if (isActive) {
                controller?.pause()
                _state.update { it.copy(sleepTimerRemainingMs = 0L, sleepTimerEndOfTrack = false) }
            }
        }
    }

    fun setSleepTimerEndOfTrack() {
        sleepTimerJob?.cancel()
        _state.update { it.copy(sleepTimerRemainingMs = 0L, sleepTimerEndOfTrack = true) }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _state.update { it.copy(sleepTimerRemainingMs = 0L, sleepTimerEndOfTrack = false) }
    }

    fun release() {
        tickerJob?.cancel()
        sleepTimerJob?.cancel()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        controller = null
    }

    private fun Song.toMediaItem(): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .setArtworkUri(albumArtUri)
            .build()
        return MediaItem.Builder()
            .setMediaId(id.toString())
            .setUri(contentUri)
            .setMediaMetadata(metadata)
            .build()
    }
}
