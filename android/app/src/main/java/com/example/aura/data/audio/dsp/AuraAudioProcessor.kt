package com.example.aura.data.audio.dsp

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import com.example.aura.domain.model.audio.AudioEffectsState
import com.example.aura.domain.model.audio.FilterType
import com.example.aura.domain.model.audio.ParametricBand
import com.example.aura.domain.model.audio.ReplayGainMode
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.pow

/**
 * High-performance, audiophile real-time AudioProcessor for Media3 / ExoPlayer.
 *
 * Implements the full AURA processing pipeline according to Section 18:
 *
 * Source
 *   ↓
 * Media3 / ExoPlayer
 *   ↓
 * ReplayGain
 *   ↓
 * Parametric EQ (Multi-band Biquad IIR)
 *   ↓
 * Bass / Treble (Dedicated Shelving Filters)
 *   ↓
 * Compressor (Dynamic Range Compression)
 *   ↓
 * Spatial / 4D Processing (Binaural HRTF / ITD / IID / Orbit)
 *   ↓
 * Balance (Channel Level Adjust)
 *   ↓
 * Limiter (Brickwall Peak Limiter & Anti-Clipping)
 *   ↓
 * Visualizer Signal Taps
 *   ↓
 * Output
 */
@OptIn(UnstableApi::class)
class AuraAudioProcessor : BaseAudioProcessor() {

    // DSP Engine instances
    private val graphicEqFilters = mutableListOf<BiquadFilter>()
    private val parametricFilters = mutableListOf<BiquadFilter>()
    private val bassFilter = BiquadFilter()
    private val trebleFilter = BiquadFilter()
    private val dynamicCompressor = DynamicCompressor()
    private val spatial4DEngine = Spatial4DEngine()
    private val peakLimiter = PeakLimiter()

    // Current State
    @Volatile
    private var currentState = AudioEffectsState()

    // ReplayGain linear scale
    @Volatile
    private var replayGainLinear = 1.0f

    // EQ Preamp & Headroom linear scale
    @Volatile
    private var eqPreampLinear = 1.0f

    // Balance factors
    @Volatile
    private var balanceGainLeft = 1.0f
    @Volatile
    private var balanceGainRight = 1.0f

    // Visualizer tap callback (invoked on audio thread with downsampled floats)
    var onAudioFrameListener: ((FloatArray) -> Unit)? = null
    private val visualizerBuffer = FloatArray(128)
    private var visualizerSampleCount = 0

    init {
        // Initialize 5 default graphic EQ filter slots
        repeat(5) {
            graphicEqFilters.add(BiquadFilter())
        }
        // Initialize 10 default parametric filter slots
        repeat(10) {
            parametricFilters.add(BiquadFilter())
        }
    }

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
            throw AudioProcessor.UnhandledAudioFormatException(inputAudioFormat)
        }

        val sampleRate = inputAudioFormat.sampleRate.toFloat()
        updateDspConfigurations(currentState, sampleRate)

        // We process in stereo 16-bit PCM
        return AudioProcessor.AudioFormat(
            inputAudioFormat.sampleRate,
            2,
            C.ENCODING_PCM_16BIT
        )
    }

    fun updateEffects(state: AudioEffectsState) {
        this.currentState = state
        val sampleRate = if (inputAudioFormat.sampleRate > 0) inputAudioFormat.sampleRate.toFloat() else 44100f
        updateDspConfigurations(state, sampleRate)
    }

    private fun updateDspConfigurations(state: AudioEffectsState, sampleRate: Float) {
        // 1. ReplayGain
        val rg = state.replayGain
        var totalDb = rg.preampGainDb
        when (rg.mode) {
            ReplayGainMode.OFF -> {}
            ReplayGainMode.TRACK -> rg.trackGainDb?.let { totalDb += it }
            ReplayGainMode.ALBUM -> rg.albumGainDb?.let { totalDb += it }
        }
        replayGainLinear = 10.0.pow(totalDb.coerceIn(-24f, 24f) / 20.0).toFloat()

        // 2. Graphic Equalizer (Dedicated Multi-band Biquad IIR with Auto-Headroom)
        val eq = state.equalizer
        if (eq.enabled) {
            val bands = eq.bands
            while (graphicEqFilters.size < bands.size) {
                graphicEqFilters.add(BiquadFilter(sampleRate))
            }

            // Headroom and Preamp calculation (Anti-clipping protection)
            var maxBoostDb = 0f
            bands.forEach { band ->
                if (band.gainDb > maxBoostDb) maxBoostDb = band.gainDb
            }
            if (state.bassTreble.bassEnabled && state.bassTreble.bassLevel > maxBoostDb) {
                maxBoostDb = state.bassTreble.bassLevel
            }
            if (state.bassTreble.trebleEnabled && state.bassTreble.trebleLevel > maxBoostDb) {
                maxBoostDb = state.bassTreble.trebleLevel
            }

            // Automatic headroom compensation reduces gain proportionally to peak boost
            val autoHeadroomDb = if (eq.autoHeadroomEnabled && maxBoostDb > 0f) {
                -(maxBoostDb * 0.65f)
            } else 0f

            val totalPreampDb = eq.preampGainDb + autoHeadroomDb
            eqPreampLinear = 10.0.pow(totalPreampDb.coerceIn(-24f, 12f) / 20.0).toFloat()

            bands.forEachIndexed { i, band ->
                val freq = band.centerFreqHz.toFloat()
                val gain = band.gainDb
                val q = when {
                    i == 0 -> 1.0f
                    i == bands.size - 1 -> 1.0f
                    else -> 1.4f
                }
                val type = when {
                    i == 0 && freq <= 100f -> FilterType.LOW_SHELF
                    i == bands.size - 1 && freq >= 10000f -> FilterType.HIGH_SHELF
                    else -> FilterType.PEAKING
                }
                graphicEqFilters[i].configure(type, freq, gain, q, sampleRate)
            }
        } else {
            eqPreampLinear = 1.0f
            graphicEqFilters.forEach { it.configure(FilterType.PEAKING, 1000f, 0f, 1f, sampleRate) }
        }

        // 3. Parametric EQ
        val p = state.parametric
        if (p.enabled) {
            val bands = p.bands
            while (parametricFilters.size < bands.size) {
                parametricFilters.add(BiquadFilter(sampleRate))
            }
            bands.forEachIndexed { i, band ->
                if (band.enabled) {
                    parametricFilters[i].configure(
                        filterType = band.filterType,
                        frequencyHz = band.frequencyHz,
                        gainDb = band.gainDb,
                        qFactor = band.qFactor,
                        sampleRate = sampleRate
                    )
                } else {
                    // Bypass filter
                    parametricFilters[i].configure(
                        filterType = FilterType.PEAKING,
                        frequencyHz = 1000f,
                        gainDb = 0f,
                        qFactor = 1f,
                        sampleRate = sampleRate
                    )
                }
            }
        }

        // 3. Bass and Treble
        val bt = state.bassTreble
        if (bt.bassEnabled) {
            bassFilter.configure(
                filterType = FilterType.LOW_SHELF,
                frequencyHz = 120f,
                gainDb = bt.bassLevel.coerceIn(-10f, 10f),
                qFactor = 0.8f,
                sampleRate = sampleRate
            )
        } else {
            bassFilter.configure(FilterType.LOW_SHELF, 120f, 0f, 0.8f, sampleRate)
        }

        if (bt.trebleEnabled) {
            trebleFilter.configure(
                filterType = FilterType.HIGH_SHELF,
                frequencyHz = 8000f,
                gainDb = bt.trebleLevel.coerceIn(-10f, 10f),
                qFactor = 0.8f,
                sampleRate = sampleRate
            )
        } else {
            trebleFilter.configure(FilterType.HIGH_SHELF, 8000f, 0f, 0.8f, sampleRate)
        }

        // 4. Compressor
        val comp = state.compressor
        dynamicCompressor.configure(
            enabled = comp.enabled,
            thresholdDb = comp.thresholdDb,
            ratio = comp.ratio,
            attackMs = comp.attackMs,
            releaseMs = comp.releaseMs,
            makeupGainDb = comp.makeupGainDb,
            sampleRate = sampleRate
        )

        // 5. 4D Spatial Audio
        spatial4DEngine.configure(state.spatial4d, sampleRate)

        // 6. Balance
        val bal = state.balance.balance.coerceIn(-1.0f, 1.0f)
        if (bal < 0f) {
            // Panned to Left: Full left, attenuate right
            balanceGainLeft = 1.0f
            balanceGainRight = 1.0f + bal // bal is negative
        } else {
            // Panned to Right: Full right, attenuate left
            balanceGainLeft = 1.0f - bal
            balanceGainRight = 1.0f
        }

        // 7. Limiter
        val lim = state.limiter
        peakLimiter.configure(
            enabled = lim.enabled,
            ceilingDb = lim.ceilingDb,
            sampleRate = sampleRate
        )
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val remaining = inputBuffer.remaining()
        if (remaining == 0) return

        // 16-bit PCM = 2 bytes per sample, stereo = 4 bytes per frame
        val frameCount = remaining / 4
        val outputBuffer = replaceOutputBuffer(frameCount * 4)

        val graphicEqActive = currentState.equalizer.enabled
        val activeGraphicBandsCount = currentState.equalizer.bands.size
        val parametricActive = currentState.parametric.enabled
        val bassActive = currentState.bassTreble.bassEnabled
        val trebleActive = currentState.bassTreble.trebleEnabled
        val spatialActive = currentState.spatial4d.enabled || currentState.spatial4d.isPreviewActive
        val activeBandsCount = currentState.parametric.bands.size

        var visIndex = 0

        while (inputBuffer.hasRemaining()) {
            val inLeftShort = inputBuffer.short
            val inRightShort = inputBuffer.short

            // Normalize to [-1.0f, 1.0f]
            var l = inLeftShort / 32768.0f
            var r = inRightShort / 32768.0f

            // 1. ReplayGain & EQ Preamp / Headroom Compensation
            val preGain = replayGainLinear * eqPreampLinear
            l *= preGain
            r *= preGain

            // 2. Graphic Equalizer
            if (graphicEqActive) {
                for (i in 0 until activeGraphicBandsCount) {
                    val outEq = graphicEqFilters[i].processStereo(l, r)
                    l = outEq.first
                    r = outEq.second
                }
            }

            // 3. Parametric EQ
            if (parametricActive) {
                for (i in 0 until activeBandsCount) {
                    val out = parametricFilters[i].processStereo(l, r)
                    l = out.first
                    r = out.second
                }
            }

            // 4. Bass / Treble
            if (bassActive) {
                val outBass = bassFilter.processStereo(l, r)
                l = outBass.first
                r = outBass.second
            }
            if (trebleActive) {
                val outTreble = trebleFilter.processStereo(l, r)
                l = outTreble.first
                r = outTreble.second
            }

            // 5. Dynamic Compressor
            val compOut = dynamicCompressor.processStereo(l, r)
            l = compOut.first
            r = compOut.second

            // 6. 4D Spatial Audio Engine
            if (spatialActive) {
                val spatOut = spatial4DEngine.processStereo(l, r)
                l = spatOut.first
                r = spatOut.second
            }

            // 7. Channel Balance
            l *= balanceGainLeft
            r *= balanceGainRight

            // 8. Peak Limiter (Brickwall Anti-Clipping Protection)
            val limOut = peakLimiter.processStereo(l, r)
            l = limOut.first
            r = limOut.second

            // 9. Capture for Visualizer (downsampled)
            if (visIndex < visualizerBuffer.size && (visualizerSampleCount++ % 8 == 0)) {
                visualizerBuffer[visIndex++] = (l + r) * 0.5f
            }

            // Convert back to 16-bit PCM with clamp
            val outLShort = (l * 32767.0f).toInt().coerceIn(-32768, 32767).toShort()
            val outRShort = (r * 32767.0f).toInt().coerceIn(-32768, 32767).toShort()

            outputBuffer.putShort(outLShort)
            outputBuffer.putShort(outRShort)
        }

        outputBuffer.flip()

        if (visIndex > 0) {
            onAudioFrameListener?.invoke(visualizerBuffer.copyOf(visIndex))
        }
    }

    override fun onReset() {
        graphicEqFilters.forEach { it.resetState() }
        parametricFilters.forEach { it.resetState() }
        bassFilter.resetState()
        trebleFilter.resetState()
        dynamicCompressor.reset()
        spatial4DEngine.reset()
        peakLimiter.reset()
    }
}
