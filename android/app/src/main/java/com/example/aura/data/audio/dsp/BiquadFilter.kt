package com.example.aura.data.audio.dsp

import com.example.aura.domain.model.audio.FilterType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt

/**
 * Standard Audio EQ Cookbook Biquad IIR Filter (Direct Form II Transposed).
 *
 * Implements real-time filtering for:
 * - Peaking EQ
 * - Low Shelf
 * - High Shelf
 * - Low Pass
 * - High Pass
 *
 * Also provides frequency response magnitude computation for accurate curve visualization.
 */
class BiquadFilter(
    private var sampleRate: Float = 44100f
) {
    data class Coefficients(
        val b0: Float = 1.0f,
        val b1: Float = 0.0f,
        val b2: Float = 0.0f,
        val a1: Float = 0.0f,
        val a2: Float = 0.0f
    )

    @Volatile
    private var coeffs = Coefficients()

    // Direct Form II Transposed filter states for Left & Right channels
    private var z1Left = 0.0f
    private var z2Left = 0.0f
    private var z1Right = 0.0f
    private var z2Right = 0.0f

    fun resetState() {
        z1Left = 0.0f
        z2Left = 0.0f
        z1Right = 0.0f
        z2Right = 0.0f
    }

    fun configure(
        filterType: FilterType,
        frequencyHz: Float,
        gainDb: Float,
        qFactor: Float,
        sampleRate: Float = this.sampleRate
    ) {
        this.sampleRate = sampleRate.coerceAtLeast(8000f)
        val f0 = frequencyHz.coerceIn(20f, (this.sampleRate / 2f) - 100f)
        val q = qFactor.coerceIn(0.1f, 18.0f)
        val aLinear = 10.0.pow(gainDb / 40.0).toFloat() // sqrt of 10^(dB/20)

        val w0 = (2.0 * PI * f0 / this.sampleRate).toFloat()
        val cosW0 = cos(w0)
        val sinW0 = sin(w0)
        val alpha = sinW0 / (2.0f * q)

        var b0Raw = 1.0f
        var b1Raw = 0.0f
        var b2Raw = 0.0f
        var a0Raw = 1.0f
        var a1Raw = 0.0f
        var a2Raw = 0.0f

        when (filterType) {
            FilterType.PEAKING -> {
                b0Raw = 1.0f + alpha * aLinear
                b1Raw = -2.0f * cosW0
                b2Raw = 1.0f - alpha * aLinear
                a0Raw = 1.0f + alpha / aLinear
                a1Raw = -2.0f * cosW0
                a2Raw = 1.0f - alpha / aLinear
            }
            FilterType.LOW_SHELF -> {
                val aPlus1 = aLinear + 1.0f
                val aMinus1 = aLinear - 1.0f
                val twoSqrtAAlpha = 2.0f * sqrt(aLinear) * alpha

                b0Raw = aLinear * (aPlus1 - aMinus1 * cosW0 + twoSqrtAAlpha)
                b1Raw = 2.0f * aLinear * (aMinus1 - aPlus1 * cosW0)
                b2Raw = aLinear * (aPlus1 - aMinus1 * cosW0 - twoSqrtAAlpha)
                a0Raw = aPlus1 + aMinus1 * cosW0 + twoSqrtAAlpha
                a1Raw = -2.0f * (aMinus1 + aPlus1 * cosW0)
                a2Raw = aPlus1 + aMinus1 * cosW0 - twoSqrtAAlpha
            }
            FilterType.HIGH_SHELF -> {
                val aPlus1 = aLinear + 1.0f
                val aMinus1 = aLinear - 1.0f
                val twoSqrtAAlpha = 2.0f * sqrt(aLinear) * alpha

                b0Raw = aLinear * (aPlus1 + aMinus1 * cosW0 + twoSqrtAAlpha)
                b1Raw = -2.0f * aLinear * (aMinus1 + aPlus1 * cosW0)
                b2Raw = aLinear * (aPlus1 + aMinus1 * cosW0 - twoSqrtAAlpha)
                a0Raw = aPlus1 - aMinus1 * cosW0 + twoSqrtAAlpha
                a1Raw = 2.0f * (aMinus1 - aPlus1 * cosW0)
                a2Raw = aPlus1 - aMinus1 * cosW0 - twoSqrtAAlpha
            }
            FilterType.LOW_PASS -> {
                b0Raw = (1.0f - cosW0) / 2.0f
                b1Raw = 1.0f - cosW0
                b2Raw = (1.0f - cosW0) / 2.0f
                a0Raw = 1.0f + alpha
                a1Raw = -2.0f * cosW0
                a2Raw = 1.0f - alpha
            }
            FilterType.HIGH_PASS -> {
                b0Raw = (1.0f + cosW0) / 2.0f
                b1Raw = -(1.0f + cosW0)
                b2Raw = (1.0f + cosW0) / 2.0f
                a0Raw = 1.0f + alpha
                a1Raw = -2.0f * cosW0
                a2Raw = 1.0f - alpha
            }
        }

        if (a0Raw != 0.0f) {
            coeffs = Coefficients(
                b0 = b0Raw / a0Raw,
                b1 = b1Raw / a0Raw,
                b2 = b2Raw / a0Raw,
                a1 = a1Raw / a0Raw,
                a2 = a2Raw / a0Raw
            )
        }
    }

    /**
     * Process a single stereo sample (in-place).
     */
    fun processStereo(inputLeft: Float, inputRight: Float): Pair<Float, Float> {
        // Direct Form II Transposed with atomic coefficient read:
        // y[n] = b0 * x[n] + z1[n-1]
        // z1[n] = b1 * x[n] - a1 * y[n] + z2[n-1]
        // z2[n] = b2 * x[n] - a2 * y[n]
        val c = coeffs
        val outLeft = c.b0 * inputLeft + z1Left
        z1Left = c.b1 * inputLeft - c.a1 * outLeft + z2Left
        z2Left = c.b2 * inputLeft - c.a2 * outLeft

        val outRight = c.b0 * inputRight + z1Right
        z1Right = c.b1 * inputRight - c.a1 * outRight + z2Right
        z2Right = c.b2 * inputRight - c.a2 * outRight

        return Pair(outLeft, outRight)
    }

    /**
     * Compute filter response magnitude |H(f)| in dB at a given frequency f.
     * Used to plot real-time EQ curves in the UI.
     */
    fun getMagnitudeResponseDb(frequencyHz: Float): Float {
        val c = coeffs
        val w = (2.0 * PI * frequencyHz / sampleRate).toFloat()
        val cosW = cos(w)
        val cos2W = cos(2.0f * w)
        val sinW = sin(w)
        val sin2W = sin(2.0f * w)

        // Numerator = b0 + b1*e^(-jw) + b2*e^(-2jw)
        val numReal = c.b0 + c.b1 * cosW + c.b2 * cos2W
        val numImag = -(c.b1 * sinW + c.b2 * sin2W)
        val numMagSq = numReal * numReal + numImag * numImag

        // Denominator = 1 + a1*e^(-jw) + a2*e^(-2jw)
        val denReal = 1.0f + c.a1 * cosW + c.a2 * cos2W
        val denImag = -(c.a1 * sinW + c.a2 * sin2W)
        val denMagSq = denReal * denReal + denImag * denImag

        if (denMagSq <= 0.0000001f) return 0f
        val mag = sqrt(numMagSq / denMagSq)
        return (20.0 * log10(mag.coerceAtLeast(0.00001f).toDouble())).toFloat()
    }
}
