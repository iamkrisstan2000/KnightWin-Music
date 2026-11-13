package com.knightwin.music.playback

import android.os.SystemClock
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.audio.TeeAudioProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max
import kotlin.math.sqrt

@OptIn(UnstableApi::class)
object AudioBeatMonitor {
    private val _beatPulse = MutableStateFlow(0f)
    val beatPulse: StateFlow<Float> = _beatPulse.asStateFlow()

    @Volatile
    private var sampleRateHz: Int = 44100

    @Volatile
    private var channelCount: Int = 2

    @Volatile
    private var encoding: Int = C.ENCODING_PCM_16BIT

    private var lowPassState = 0f
    private var adaptiveFloor = 0.04f
    private var envelope = 0f
    private var lastEmitMs = 0L

    fun createSink(): TeeAudioProcessor.AudioBufferSink = object : TeeAudioProcessor.AudioBufferSink {
        override fun flush(sampleRateHz: Int, channelCount: Int, encoding: Int) {
            this@AudioBeatMonitor.sampleRateHz = sampleRateHz.coerceAtLeast(8000)
            this@AudioBeatMonitor.channelCount = channelCount.coerceAtLeast(1)
            this@AudioBeatMonitor.encoding = encoding
            lowPassState = 0f
        }

        override fun handleBuffer(buffer: ByteBuffer) {
            analyzeBuffer(buffer)
        }
    }

    fun reset() {
        envelope = 0f
        lowPassState = 0f
        _beatPulse.value = 0f
    }

    private fun analyzeBuffer(buffer: ByteBuffer) {
        val dup = buffer.asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN)
        val remaining = dup.remaining()
        if (remaining <= 0) return

        val channels = channelCount.coerceAtLeast(1)
        var bassSumSq = 0.0
        var totalSumSq = 0.0
        var frames = 0

        when (encoding) {
            C.ENCODING_PCM_16BIT -> {
                val frameBytes = channels * 2
                val totalFrames = remaining / frameBytes
                if (totalFrames <= 0) return
                val strideFrames = (totalFrames / 256).coerceAtLeast(1)
                val alpha = (1800f / sampleRateHz.toFloat() * strideFrames).coerceIn(0.04f, 0.45f)

                var f = 0
                val basePos = dup.position()
                while (f < totalFrames) {
                    val idx = basePos + f * frameBytes
                    val mono = if (channels >= 2) {
                        val l = dup.getShort(idx).toFloat() / 32768f
                        val r = dup.getShort(idx + 2).toFloat() / 32768f
                        0.5f * (l + r)
                    } else {
                        dup.getShort(idx).toFloat() / 32768f
                    }
                    lowPassState += alpha * (mono - lowPassState)
                    bassSumSq += (lowPassState * lowPassState).toDouble()
                    totalSumSq += (mono * mono).toDouble()
                    frames++
                    f += strideFrames
                }
            }
            C.ENCODING_PCM_FLOAT -> {
                val frameBytes = channels * 4
                val totalFrames = remaining / frameBytes
                if (totalFrames <= 0) return
                val strideFrames = (totalFrames / 256).coerceAtLeast(1)
                val alpha = (1800f / sampleRateHz.toFloat() * strideFrames).coerceIn(0.04f, 0.45f)

                var f = 0
                val basePos = dup.position()
                while (f < totalFrames) {
                    val idx = basePos + f * frameBytes
                    val mono = if (channels >= 2) {
                        val l = dup.getFloat(idx)
                        val r = dup.getFloat(idx + 4)
                        0.5f * (l + r)
                    } else {
                        dup.getFloat(idx)
                    }
                    lowPassState += alpha * (mono - lowPassState)
                    bassSumSq += (lowPassState * lowPassState).toDouble()
                    totalSumSq += (mono * mono).toDouble()
                    frames++
                    f += strideFrames
                }
            }
            else -> return
        }

        if (frames <= 0) return
        val bassRms = sqrt(bassSumSq / frames).toFloat().coerceIn(0f, 1f)
        val totalRms = sqrt(totalSumSq / frames).toFloat().coerceIn(0f, 1f)
        val energy = (bassRms * 0.72f + totalRms * 0.28f).coerceIn(0f, 1f)

        adaptiveFloor = (adaptiveFloor * 0.96f + energy * 0.04f).coerceIn(0.025f, 0.45f)
        val ratio = ((energy - adaptiveFloor * 0.72f) / (adaptiveFloor * 0.95f).coerceAtLeast(0.04f))
            .coerceIn(0f, 1.4f)
        val transient = if (energy > adaptiveFloor * 1.15f) {
            (ratio * 0.85f).coerceIn(0f, 1f)
        } else {
            (ratio * 0.42f).coerceIn(0f, 1f)
        }
        val target = max(transient, (energy * 1.45f).coerceIn(0f, 0.55f))

        envelope = if (target > envelope) {
            envelope * 0.25f + target * 0.75f
        } else {
            envelope * 0.82f + target * 0.18f
        }

        val now = SystemClock.elapsedRealtime()
        if (now - lastEmitMs >= 24L) {
            lastEmitMs = now
            _beatPulse.value = envelope.coerceIn(0f, 1f)
        }
    }
}
