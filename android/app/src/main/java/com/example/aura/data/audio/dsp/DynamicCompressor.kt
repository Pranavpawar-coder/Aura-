package com.example.aura.data.audio.dsp

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow

/**
 * Real-time stereo dynamic range compressor.
 *
 * Provides smooth peak-detection envelope ballistics, soft-knee gain reduction,
 * and makeup gain.
 */
class DynamicCompressor(
    private var sampleRate: Float = 44100f
) {
    private var thresholdDb: Float = -18f
    private var ratio: Float = 4f
    private var attackCoeff: Float = 0f
    private var releaseCoeff: Float = 0f
    private var makeupLinear: Float = 1.0f

    private var gainReductionDb: Float = 0f
    private var isEnabled: Boolean = false

    fun reset() {
        gainReductionDb = 0f
    }

    fun configure(
        enabled: Boolean,
        thresholdDb: Float,
        ratio: Float,
        attackMs: Float,
        releaseMs: Float,
        makeupGainDb: Float,
        sampleRate: Float = this.sampleRate
    ) {
        this.isEnabled = enabled
        this.thresholdDb = thresholdDb.coerceIn(-60f, 0f)
        this.ratio = ratio.coerceIn(1.0f, 20.0f)
        this.sampleRate = sampleRate.coerceAtLeast(8000f)

        val attSec = (attackMs.coerceIn(0.1f, 200f) * 0.001f)
        val relSec = (releaseMs.coerceIn(10f, 1500f) * 0.001f)

        this.attackCoeff = exp(-1.0f / (attSec * this.sampleRate))
        this.releaseCoeff = exp(-1.0f / (relSec * this.sampleRate))
        this.makeupLinear = 10.0.pow(makeupGainDb.coerceIn(0f, 24f) / 20.0).toFloat()
    }

    fun processStereo(inputLeft: Float, inputRight: Float): Pair<Float, Float> {
        if (!isEnabled) return Pair(inputLeft, inputRight)

        // Peak level detection (linked stereo)
        val peak = max(abs(inputLeft), abs(inputRight)).coerceAtLeast(0.000001f)
        val peakDb = (20.0 * log10(peak.toDouble())).toFloat()

        // Static compression curve
        val targetReductionDb = if (peakDb > thresholdDb) {
            (thresholdDb + (peakDb - thresholdDb) / ratio) - peakDb
        } else {
            0f
        }

        // Ballistics filter (Attack if reducing more, Release if recovering)
        if (targetReductionDb < gainReductionDb) {
            gainReductionDb = attackCoeff * gainReductionDb + (1.0f - attackCoeff) * targetReductionDb
        } else {
            gainReductionDb = releaseCoeff * gainReductionDb + (1.0f - releaseCoeff) * targetReductionDb
        }

        val currentGain = 10.0.pow(gainReductionDb / 20.0).toFloat() * makeupLinear
        return Pair(inputLeft * currentGain, inputRight * currentGain)
    }
}
