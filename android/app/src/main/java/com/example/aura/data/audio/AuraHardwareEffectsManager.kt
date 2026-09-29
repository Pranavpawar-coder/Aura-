package com.example.aura.data.audio

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.media.audiofx.Visualizer
import android.os.Build
import android.util.Log
import com.example.aura.domain.model.audio.AudioEffectsState
import com.example.aura.domain.model.audio.EqualizerPreset
import com.example.aura.domain.model.audio.ReverbPreset

/**
 * Manages Android native AudioEffects attached to ExoPlayer's audioSessionId.
 *
 * Robust & fault-tolerant: Every effect operation is shielded with try-catch blocks
 * to ensure that platform or vendor DSP quirks never disrupt audio playback.
 */
class AuraHardwareEffectsManager(
    private val context: Context
) {
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var presetReverb: PresetReverb? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var nativeVisualizer: Visualizer? = null

    private var currentSessionId: Int = 0
    var onWaveformListener: ((ByteArray) -> Unit)? = null
    var onFftListener: ((ByteArray) -> Unit)? = null

    fun attachSession(audioSessionId: Int, state: AudioEffectsState) {
        if (audioSessionId == 0 || audioSessionId == currentSessionId) return
        release()
        currentSessionId = audioSessionId

        // 1. Native Equalizer (Bypassed to prevent double-processing with AuraAudioProcessor)
        try {
            val eq = Equalizer(0, audioSessionId)
            eq.enabled = false
            equalizer = eq
        } catch (e: Throwable) {
            Log.w("AuraAudioFX", "Native Equalizer not supported on this device: ${e.message}")
        }

        // 2. Native BassBoost (Bypassed to prevent double-processing with AuraAudioProcessor)
        try {
            val bb = BassBoost(0, audioSessionId)
            bb.enabled = false
            bassBoost = bb
        } catch (e: Throwable) {
            Log.w("AuraAudioFX", "Native BassBoost not supported: ${e.message}")
        }

        // 3. Native Virtualizer (Bypassed to prevent double-processing with AuraAudioProcessor)
        try {
            val virt = Virtualizer(0, audioSessionId)
            virt.enabled = false
            virtualizer = virt
        } catch (e: Throwable) {
            Log.w("AuraAudioFX", "Native Virtualizer not supported: ${e.message}")
        }

        // 4. Native PresetReverb
        try {
            val rev = PresetReverb(0, audioSessionId)
            rev.enabled = state.reverb.enabled && state.reverb.preset != ReverbPreset.OFF
            presetReverb = rev
            applyReverbSettings(state)
        } catch (e: Throwable) {
            Log.w("AuraAudioFX", "Native PresetReverb not supported: ${e.message}")
        }

        // 5. Native LoudnessEnhancer (API 19+)
        try {
            val le = LoudnessEnhancer(audioSessionId)
            le.enabled = state.loudness.enabled
            loudnessEnhancer = le
            applyLoudnessSettings(state)
        } catch (e: Throwable) {
            Log.w("AuraAudioFX", "Native LoudnessEnhancer not supported: ${e.message}")
        }

        // 6. Native Visualizer (if allowed)
        try {
            val vis = Visualizer(audioSessionId)
            val captureSizeRange = Visualizer.getCaptureSizeRange()
            val captureSize = captureSizeRange.getOrElse(1) { 256 }.coerceAtMost(512)
            vis.captureSize = captureSize
            vis.setDataCaptureListener(
                object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(visualizer: Visualizer?, waveform: ByteArray?, samplingRate: Int) {
                        if (waveform != null) onWaveformListener?.invoke(waveform)
                    }

                    override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {
                        if (fft != null) onFftListener?.invoke(fft)
                    }
                },
                Visualizer.getMaxCaptureRate() / 2,
                true,
                true
            )
            vis.enabled = state.visualizer.enabled
            nativeVisualizer = vis
        } catch (e: Throwable) {
            Log.i("AuraAudioFX", "Native Visualizer disabled or requires RECORD_AUDIO: ${e.message}")
        }
    }

    fun applyEffectsState(state: AudioEffectsState) {
        // Equalizer, Bass/Treble, and 4D/Virtualizer are authoritatively processed in AuraAudioProcessor (PCM DSP)
        // to prevent double-processing, clipping, and vendor driver inconsistencies.
        equalizer?.enabled = false
        bassBoost?.enabled = false
        virtualizer?.enabled = false
        applyReverbSettings(state)
        applyLoudnessSettings(state)
        applyVisualizerState(state)
    }

    private fun applyEqualizerSettings(state: AudioEffectsState) {
        val eq = equalizer ?: return
        try {
            eq.enabled = state.equalizer.enabled
            if (!state.equalizer.enabled) return

            val numBands = eq.numberOfBands.toInt()
            val minLevel = eq.bandLevelRange[0]
            val maxLevel = eq.bandLevelRange[1]

            // Apply preset or custom bands
            if (state.equalizer.preset != EqualizerPreset.CUSTOM) {
                // Preset mapping
                val presetIndex = findPresetIndex(eq, state.equalizer.preset)
                if (presetIndex >= 0) {
                    eq.usePreset(presetIndex.toShort())
                    return
                }
            }

            // Custom or fallback band levels
            state.equalizer.bands.forEach { band ->
                if (band.index < numBands) {
                    val clamped = band.gainMb.coerceIn(minLevel.toInt(), maxLevel.toInt()).toShort()
                    eq.setBandLevel(band.index.toShort(), clamped)
                }
            }
        } catch (e: Throwable) {
            Log.w("AuraAudioFX", "Failed to apply EQ: ${e.message}")
        }
    }

    private fun findPresetIndex(eq: Equalizer, preset: EqualizerPreset): Int {
        val targetName = when (preset) {
            EqualizerPreset.FLAT -> "flat"
            EqualizerPreset.ACOUSTIC -> "acoustic"
            EqualizerPreset.BASS -> "bass"
            EqualizerPreset.BASS_BOOST -> "bass"
            EqualizerPreset.CLASSICAL -> "classical"
            EqualizerPreset.DANCE -> "dance"
            EqualizerPreset.DEEP -> "deep"
            EqualizerPreset.ELECTRONIC -> "electronic"
            EqualizerPreset.HIP_HOP -> "hip hop"
            EqualizerPreset.JAZZ -> "jazz"
            EqualizerPreset.POP -> "pop"
            EqualizerPreset.ROCK -> "rock"
            EqualizerPreset.VOCAL -> "vocal"
            EqualizerPreset.CUSTOM -> return -1
        }
        val count = eq.numberOfPresets.toInt()
        for (i in 0 until count) {
            val name = eq.getPresetName(i.toShort())?.lowercase() ?: ""
            if (name.contains(targetName)) {
                return i
            }
        }
        return -1
    }

    private fun applyBassSettings(state: AudioEffectsState) {
        val bb = bassBoost ?: return
        try {
            bb.enabled = state.bassTreble.bassEnabled
            if (state.bassTreble.bassEnabled) {
                // Map -10dB..+10dB to 0..1000 strength
                val strength = ((state.bassTreble.bassLevel + 10f) * 50f).coerceIn(0f, 1000f).toInt().toShort()
                bb.setStrength(strength)
            }
        } catch (e: Throwable) {
            Log.w("AuraAudioFX", "Failed to apply BassBoost: ${e.message}")
        }
    }

    private fun applyVirtualizerSettings(state: AudioEffectsState) {
        val virt = virtualizer ?: return
        try {
            virt.enabled = state.virtualizer.enabled
            if (state.virtualizer.enabled) {
                virt.setStrength(state.virtualizer.strength.coerceIn(0, 1000).toShort())
            }
        } catch (e: Throwable) {
            Log.w("AuraAudioFX", "Failed to apply Virtualizer: ${e.message}")
        }
    }

    private fun applyReverbSettings(state: AudioEffectsState) {
        val rev = presetReverb ?: return
        try {
            val enabled = state.reverb.enabled && state.reverb.preset != ReverbPreset.OFF
            rev.enabled = enabled
            if (enabled) {
                val presetVal = when (state.reverb.preset) {
                    ReverbPreset.OFF -> PresetReverb.PRESET_NONE
                    ReverbPreset.SMALL_ROOM -> PresetReverb.PRESET_SMALLROOM
                    ReverbPreset.ROOM -> PresetReverb.PRESET_MEDIUMROOM
                    ReverbPreset.LARGE_ROOM -> PresetReverb.PRESET_LARGEROOM
                    ReverbPreset.HALL -> PresetReverb.PRESET_MEDIUMHALL
                    ReverbPreset.LARGE_HALL -> PresetReverb.PRESET_LARGEHALL
                    ReverbPreset.CONCERT -> PresetReverb.PRESET_LARGEHALL
                    ReverbPreset.STUDIO -> PresetReverb.PRESET_SMALLROOM
                }
                rev.preset = presetVal
            }
        } catch (e: Throwable) {
            Log.w("AuraAudioFX", "Failed to apply Reverb: ${e.message}")
        }
    }

    private fun applyLoudnessSettings(state: AudioEffectsState) {
        val le = loudnessEnhancer ?: return
        try {
            le.enabled = state.loudness.enabled
            if (state.loudness.enabled) {
                le.setTargetGain(state.loudness.gainMb.coerceIn(0, 2000))
            }
        } catch (e: Throwable) {
            Log.w("AuraAudioFX", "Failed to apply LoudnessEnhancer: ${e.message}")
        }
    }

    private fun applyVisualizerState(state: AudioEffectsState) {
        try {
            nativeVisualizer?.enabled = state.visualizer.enabled
        } catch (_: Throwable) {
        }
    }

    fun release() {
        try { equalizer?.release() } catch (_: Throwable) {}
        try { bassBoost?.release() } catch (_: Throwable) {}
        try { virtualizer?.release() } catch (_: Throwable) {}
        try { presetReverb?.release() } catch (_: Throwable) {}
        try { loudnessEnhancer?.release() } catch (_: Throwable) {}
        try { nativeVisualizer?.release() } catch (_: Throwable) {}
        equalizer = null
        bassBoost = null
        virtualizer = null
        presetReverb = null
        loudnessEnhancer = null
        nativeVisualizer = null
        currentSessionId = 0
    }
}
