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
    private var presetReverb: PresetReverb? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

    private var currentSessionId: Int = 0
    var onWaveformListener: ((ByteArray) -> Unit)? = null
    var onFftListener: ((ByteArray) -> Unit)? = null

    fun attachSession(audioSessionId: Int, state: AudioEffectsState) {
        if (audioSessionId == 0 || audioSessionId == currentSessionId) return
        release()
        currentSessionId = audioSessionId

        // Native PresetReverb (only if enabled)
        if (state.reverb.enabled && state.reverb.preset != ReverbPreset.OFF) {
            try {
                val rev = PresetReverb(0, audioSessionId)
                rev.enabled = true
                presetReverb = rev
                applyReverbSettings(state)
            } catch (e: Throwable) {
                Log.w("AuraAudioFX", "Native PresetReverb not supported: ${e.message}")
            }
        }

        // Native LoudnessEnhancer (API 19+, only if enabled)
        if (state.loudness.enabled) {
            try {
                val le = LoudnessEnhancer(audioSessionId)
                le.enabled = true
                loudnessEnhancer = le
                applyLoudnessSettings(state)
            } catch (e: Throwable) {
                Log.w("AuraAudioFX", "Native LoudnessEnhancer not supported: ${e.message}")
            }
        }
    }

    fun applyEffectsState(state: AudioEffectsState) {
        // Equalizer, Bass/Treble, and 4D/Virtualizer are authoritatively processed in AuraAudioProcessor (PCM DSP)
        // to prevent double-processing, clipping, and vendor driver inconsistencies.
        if (state.reverb.enabled && state.reverb.preset != ReverbPreset.OFF) {
            if (presetReverb == null && currentSessionId != 0) {
                try {
                    presetReverb = PresetReverb(0, currentSessionId)
                } catch (e: Throwable) {
                    Log.w("AuraAudioFX", "Native PresetReverb not supported: ${e.message}")
                }
            }
            applyReverbSettings(state)
        } else {
            presetReverb?.enabled = false
        }

        if (state.loudness.enabled) {
            if (loudnessEnhancer == null && currentSessionId != 0) {
                try {
                    loudnessEnhancer = LoudnessEnhancer(currentSessionId)
                } catch (e: Throwable) {
                    Log.w("AuraAudioFX", "Native LoudnessEnhancer not supported: ${e.message}")
                }
            }
            applyLoudnessSettings(state)
        } else {
            loudnessEnhancer?.enabled = false
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

    fun release() {
        try { presetReverb?.release() } catch (_: Throwable) {}
        try { loudnessEnhancer?.release() } catch (_: Throwable) {}
        presetReverb = null
        loudnessEnhancer = null
        currentSessionId = 0
    }
}
