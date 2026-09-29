package com.example.aura.data.audio.dsp

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.tanh

/**
 * Fast transparent peak limiter with soft-knee saturation.
 * Prevents digital clipping when EQ or effect boosts exceed 0 dBFS.
 */
class PeakLimiter(
    private var sampleRate: Float = 44100f
) {
    private var isEnabled: Boolean = true
    private var ceilingLinear: Float = 0.95f // ~ -0.5 dBFS
    private var envelope: Float = 0f
    private var releaseCoeff: Float = 0f

    fun configure(
        enabled: Boolean,
        ceilingDb: Float,
        sampleRate: Float = this.sampleRate
    ) {
        this.isEnabled = enabled
        this.ceilingLinear = 10.0.pow(ceilingDb.coerceIn(-12f, 0f) / 20.0).toFloat().coerceIn(0.1f, 1.0f)
        this.sampleRate = sampleRate.coerceAtLeast(8000f)
        // 50ms smooth release
        this.releaseCoeff = exp(-1.0f / (0.05f * this.sampleRate))
    }

    fun reset() {
        envelope = 0f
    }

    fun processStereo(inputLeft: Float, inputRight: Float): Pair<Float, Float> {
        if (!isEnabled) {
            // Hard clamp fallback to prevent integer wrap-around distortion
            return Pair(
                inputLeft.coerceIn(-1.0f, 1.0f),
                inputRight.coerceIn(-1.0f, 1.0f)
            )
        }

        val peak = max(abs(inputLeft), abs(inputRight))
        if (peak > envelope) {
            envelope = peak // Instant attack
        } else {
            envelope = releaseCoeff * envelope + (1.0f - releaseCoeff) * peak
        }

        var gain = 1.0f
        if (envelope > ceilingLinear) {
            gain = ceilingLinear / envelope
        }

        var outL = inputLeft * gain
        var outR = inputRight * gain

        // Transparent soft-knee analog saturation if still exceeding ceiling
        if (abs(outL) > ceilingLinear) {
            outL = ceilingLinear * tanh(outL / ceilingLinear)
        }
        if (abs(outR) > ceilingLinear) {
            outR = ceilingLinear * tanh(outR / ceilingLinear)
        }

        return Pair(outL, outR)
    }
}
