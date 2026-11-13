package com.knightwin.music.playback

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.TeeAudioProcessor
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.knightwin.music.KnightWinApp
import com.knightwin.music.MainActivity
import com.knightwin.music.R
import com.knightwin.music.data.model.EqualizerPreset
import com.knightwin.music.data.model.ReverbPreset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.ln

@OptIn(UnstableApi::class)
class KnightPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private var presetReverb: PresetReverb? = null
    private var attachedReverbSessionId: Int = 0
    private var activeReverbPreset: ReverbPreset = ReverbPreset.NONE
    private var equalizer: Equalizer? = null
    private var attachedEqSessionId: Int = 0
    private var activeEqPreset: EqualizerPreset = EqualizerPreset.DEFAULT
    private var activeEqCustomBands: List<Float> = listOf(0f, 0f, 0f, 0f, 0f)
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        const val CMD_GET_AUDIO_SESSION = "com.knightwin.music.GET_AUDIO_SESSION"
        const val EXTRA_AUDIO_SESSION_ID = "audio_session_id"
    }

    override fun onCreate() {
        super.onCreate()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        val renderersFactory = object : DefaultRenderersFactory(this) {
            override fun buildAudioSink(
                context: Context,
                enableFloatOutput: Boolean,
                enableAudioTrackPlaybackParams: Boolean
            ): AudioSink {
                return DefaultAudioSink.Builder(context)
                    .setEnableFloatOutput(enableFloatOutput)
                    .setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
                    .setAudioProcessors(arrayOf(TeeAudioProcessor(AudioBeatMonitor.createSink())))
                    .build()
            }
        }

        player = ExoPlayer.Builder(this, renderersFactory)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        player.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                applyReverb(activeReverbPreset, audioSessionId)
                applyEqualizer(activeEqPreset, activeEqCustomBands, audioSessionId)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (!isPlaying) {
                    AudioBeatMonitor.reset()
                }
            }
        })

        val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setChannelName(R.string.playback_channel_name)
            .build().apply {
                setSmallIcon(R.drawable.ic_notification_music)
            }
        setMediaNotificationProvider(notificationProvider)

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityPendingIntent)
            .setCallback(object : MediaSession.Callback {
                override fun onConnect(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): MediaSession.ConnectionResult {
                    val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS
                        .buildUpon()
                        .add(SessionCommand(CMD_GET_AUDIO_SESSION, Bundle.EMPTY))
                        .build()
                    return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                        .setAvailableSessionCommands(sessionCommands)
                        .build()
                }

                override fun onCustomCommand(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    customCommand: SessionCommand,
                    args: Bundle
                ): ListenableFuture<SessionResult> {
                    if (customCommand.customAction == CMD_GET_AUDIO_SESSION) {
                        val bundle = Bundle().apply {
                            putInt(EXTRA_AUDIO_SESSION_ID, player.audioSessionId)
                        }
                        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS, bundle))
                    }
                    return super.onCustomCommand(session, controller, customCommand, args)
                }
            })
            .build()

        val app = application as? KnightWinApp
        if (app != null) {
            serviceScope.launch {
                app.settingsRepository.settingsFlow
                    .map { it.playbackSpeed }
                    .distinctUntilChanged()
                    .collect { speed ->
                        player.setPlaybackSpeed(speed.coerceIn(0.5f, 2.0f))
                    }
            }
            serviceScope.launch {
                app.settingsRepository.settingsFlow
                    .map { it.reverbPreset }
                    .distinctUntilChanged()
                    .collect { preset ->
                        activeReverbPreset = preset
                        applyReverb(preset, player.audioSessionId)
                    }
            }
            serviceScope.launch {
                app.settingsRepository.settingsFlow
                    .map { it.eqPreset to it.eqCustomBands }
                    .distinctUntilChanged()
                    .collect { (preset, bands) ->
                        activeEqPreset = preset
                        activeEqCustomBands = bands
                        applyEqualizer(preset, bands, player.audioSessionId)
                    }
            }
        }
    }

    private fun applyReverb(preset: ReverbPreset, audioSessionId: Int) {
        if (audioSessionId == C.AUDIO_SESSION_ID_UNSET || audioSessionId == 0) return
        runCatching {
            if (presetReverb != null && attachedReverbSessionId != audioSessionId) {
                presetReverb?.enabled = false
                presetReverb?.release()
                presetReverb = null
            }
            if (preset == ReverbPreset.NONE) {
                presetReverb?.enabled = false
                return
            }
            if (presetReverb == null) {
                presetReverb = PresetReverb(0, audioSessionId)
                attachedReverbSessionId = audioSessionId
            }
            presetReverb?.apply {
                this.preset = preset.presetValue
                enabled = true
            }
        }
    }

    private fun applyEqualizer(preset: EqualizerPreset, customBands: List<Float>, audioSessionId: Int) {
        if (audioSessionId == C.AUDIO_SESSION_ID_UNSET || audioSessionId == 0) return
        runCatching {
            if (equalizer != null && attachedEqSessionId != audioSessionId) {
                equalizer?.enabled = false
                equalizer?.release()
                equalizer = null
            }
            val bandsDb = if (preset == EqualizerPreset.CUSTOM) customBands else preset.bandDb
            val isFlat = preset == EqualizerPreset.DEFAULT || bandsDb.all { abs(it) < 0.05f }
            if (isFlat) {
                equalizer?.enabled = false
                return
            }
            if (equalizer == null) {
                equalizer = Equalizer(0, audioSessionId)
                attachedEqSessionId = audioSessionId
            }
            val eq = equalizer ?: return
            val numBands = eq.numberOfBands.toInt()
            if (numBands <= 0) return
            val range = eq.bandLevelRange
            val minLevel = range[0].toInt()
            val maxLevel = range[1].toInt()
            val refFreqsHz = floatArrayOf(60f, 230f, 910f, 3600f, 14000f)

            for (band in 0 until numBands) {
                val centerHz = (eq.getCenterFreq(band.toShort()) / 1000f).coerceAtLeast(20f)
                val targetDb = interpolateEqDb(centerHz, refFreqsHz, bandsDb)
                val millibels = (targetDb * 100f).toInt().coerceIn(minLevel, maxLevel)
                eq.setBandLevel(band.toShort(), millibels.toShort())
            }
            eq.enabled = true
        }
    }

    private fun interpolateEqDb(freqHz: Float, refFreqs: FloatArray, refDb: List<Float>): Float {
        if (refDb.isEmpty()) return 0f
        if (freqHz <= refFreqs.first()) return refDb.first()
        if (freqHz >= refFreqs.last()) return refDb.getOrElse(refFreqs.lastIndex) { 0f }
        val logF = ln(freqHz.toDouble())
        for (i in 0 until refFreqs.lastIndex) {
            val f0 = refFreqs[i]
            val f1 = refFreqs[i + 1]
            if (freqHz in f0..f1) {
                val t = ((logF - ln(f0.toDouble())) / (ln(f1.toDouble()) - ln(f0.toDouble()))).toFloat().coerceIn(0f, 1f)
                val v0 = refDb.getOrElse(i) { 0f }
                val v1 = refDb.getOrElse(i + 1) { 0f }
                return v0 + (v1 - v0) * t
            }
        }
        return 0f
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val p = mediaSession?.player
        if (p == null || !p.playWhenReady || p.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        AudioBeatMonitor.reset()
        serviceScope.cancel()
        runCatching {
            presetReverb?.enabled = false
            presetReverb?.release()
        }
        presetReverb = null
        runCatching {
            equalizer?.enabled = false
            equalizer?.release()
        }
        equalizer = null
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
