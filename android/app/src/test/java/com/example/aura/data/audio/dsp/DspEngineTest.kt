package com.example.aura.data.audio.dsp

import com.example.aura.domain.model.audio.FilterType
import com.example.aura.domain.model.audio.Spatial4DSettings
import com.example.aura.domain.model.audio.SpatialMovementMode
import com.example.aura.domain.model.audio.SpatialPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.pow

class DspEngineTest {

    @Test
    fun testBiquadFilterPeakingMagnitude() {
        val sampleRate = 44100f
        val centerFreq = 1000f
        val gainDb = 6.0f
        val q = 1.0f

        val filter = BiquadFilter(sampleRate)
        filter.configure(FilterType.PEAKING, centerFreq, gainDb, q)

        val magAtCenter = filter.getMagnitudeResponseDb(centerFreq)
        // Magnitude at center frequency for peaking filter should match configured gainDb closely
        assertTrue("Expected magnitude near 6dB, got $magAtCenter", abs(magAtCenter - gainDb) < 0.2f)

        // Far away from center frequency (e.g. 50Hz), magnitude should approach 0dB
        val magFar = filter.getMagnitudeResponseDb(50f)
        assertTrue("Expected magnitude near 0dB, got $magFar", abs(magFar) < 0.5f)
    }

    @Test
    fun testBiquadFilterLowPassAttenuatesHighFreq() {
        val sampleRate = 44100f
        val cutoffFreq = 500f
        val filter = BiquadFilter(sampleRate)
        filter.configure(FilterType.LOW_PASS, cutoffFreq, 0f, 0.707f)

        val magPass = filter.getMagnitudeResponseDb(100f)
        val magStop = filter.getMagnitudeResponseDb(5000f)

        assertTrue("Passband should be near 0dB, was $magPass", magPass > -3.0f)
        assertTrue("Stopband should be significantly attenuated, was $magStop", magStop < -15.0f)
    }

    @Test
    fun testBiquadFilterProcessingStability() {
        val sampleRate = 44100f
        val filter = BiquadFilter(sampleRate)
        filter.configure(FilterType.LOW_SHELF, 120f, 8.0f, 0.7f)

        // Process 1000 samples of a synthetic sine wave
        var maxOutL = 0f
        var maxOutR = 0f
        for (i in 0 until 1000) {
            val sample = kotlin.math.sin(2.0 * Math.PI * 100.0 * i / sampleRate).toFloat() * 0.5f
            val (outL, outR) = filter.processStereo(sample, sample)
            assertFalse("Filter output should not be NaN", outL.isNaN() || outR.isNaN())
            assertFalse("Filter output should not be infinite", outL.isInfinite() || outR.isInfinite())
            maxOutL = maxOf(maxOutL, abs(outL))
            maxOutR = maxOf(maxOutR, abs(outR))
        }

        // Bass boost should increase amplitude above the 0.5f input
        assertTrue("Boosted low shelf output should exceed input amplitude", maxOutL > 0.5f)
    }

    @Test
    fun testDynamicCompressorThresholdAndRatio() {
        val sampleRate = 44100f
        val compressor = DynamicCompressor(sampleRate)
        compressor.configure(
            enabled = true,
            thresholdDb = -10.0f,
            ratio = 4.0f,
            attackMs = 1.0f,
            releaseMs = 50.0f,
            makeupGainDb = 0.0f
        )

        // Process loud 0 dBFS signal (exceeds -10dBFS threshold by 10dB)
        // With 4:1 ratio, the 10dB overshoot should be compressed to ~2.5dB overshoot
        var compressedL = 0f
        for (i in 0 until 500) {
            val (outL, _) = compressor.processStereo(1.0f, 1.0f)
            compressedL = outL
        }

        // Output should be visibly attenuated compared to 1.0f input
        assertTrue("Output should be reduced below input 1.0f, got $compressedL", compressedL < 0.9f)
    }

    @Test
    fun testDynamicCompressorSubThresholdTransparent() {
        val sampleRate = 44100f
        val compressor = DynamicCompressor(sampleRate)
        compressor.configure(
            enabled = true,
            thresholdDb = -6.0f,
            ratio = 4.0f,
            attackMs = 10.0f,
            releaseMs = 100.0f,
            makeupGainDb = 0.0f
        )

        // -20 dBFS signal (~0.1 amplitude) is well below the -6dB threshold
        val (outL, outR) = compressor.processStereo(0.1f, 0.1f)
        assertEquals("Sub-threshold signal should not be compressed", 0.1f, outL, 0.001f)
        assertEquals("Sub-threshold signal should not be compressed", 0.1f, outR, 0.001f)
    }

    @Test
    fun testPeakLimiterCeilingEnforcement() {
        val sampleRate = 44100f
        val limiter = PeakLimiter(sampleRate)
        limiter.configure(
            enabled = true,
            ceilingDb = -0.5f // ~0.944 linear ceiling
        )

        val linearCeiling = 10.0.pow(-0.5 / 20.0).toFloat()

        // Feed extreme 2.0f (+6dBFS) overshoot signal
        var maxObserved = 0f
        for (i in 0 until 1000) {
            val (outL, outR) = limiter.processStereo(2.0f, 2.0f)
            assertFalse("Limiter should not produce NaN", outL.isNaN() || outR.isNaN())
            maxObserved = maxOf(maxObserved, abs(outL), abs(outR))
        }

        // Peak limiter must prevent output from exceeding ceiling + margin
        assertTrue("Limiter max $maxObserved exceeded ceiling $linearCeiling", maxObserved <= linearCeiling + 0.02f)
    }

    @Test
    fun testSpatial4DEngineOrbitAndBinauralProcessing() {
        val sampleRate = 44100f
        val engine = Spatial4DEngine(sampleRate)
        engine.configure(
            Spatial4DSettings(
                enabled = true,
                intensity = 1.0f,
                width = 1.5f,
                depth = 0.5f,
                movementMode = SpatialMovementMode.SLOW_ORBIT,
                rotationSpeed = 1.0f
            ),
            sampleRate = sampleRate
        )

        // Process stereo buffer through 4D engine
        var sumDiff = 0f
        for (i in 0 until 1000) {
            val sample = kotlin.math.sin(2.0 * Math.PI * 440.0 * i / sampleRate).toFloat() * 0.4f
            val (outL, outR) = engine.processStereo(sample, sample)
            assertFalse("Spatial 4D outL was NaN", outL.isNaN())
            assertFalse("Spatial 4D outR was NaN", outR.isNaN())
            sumDiff += abs(outL - outR)
        }

        // With stereo widening and orbit ITD/IID, left and right channels should diverge
        assertTrue("Spatial 4D processing must produce binaural differences between L and R", sumDiff > 0.1f)
    }

    @Test
    fun testSpatial4DPresetConfiguration() {
        val sampleRate = 44100f
        val engine = Spatial4DEngine(sampleRate)

        // Configure Wide preset
        engine.configure(
            Spatial4DSettings(enabled = true, preset = SpatialPreset.WIDE),
            sampleRate = sampleRate
        )
        // Processing should produce stable stereo output
        val (outWideL, outWideR) = engine.processStereo(0.5f, 0.5f)
        assertFalse("Output should not be NaN", outWideL.isNaN() || outWideR.isNaN())

        // Configure 4D Fast preset
        engine.configure(
            Spatial4DSettings(enabled = true, preset = SpatialPreset.FOUR_D_FAST),
            sampleRate = sampleRate
        )
        val (outFastL, outFastR) = engine.processStereo(0.5f, 0.5f)
        assertFalse("Output should not be NaN", outFastL.isNaN() || outFastR.isNaN())
    }

    @Test
    fun testSpatial4DZeroIntensityNeutralBypass() {
        val sampleRate = 44100f
        val engine = Spatial4DEngine(sampleRate)

        // When disabled
        engine.configure(
            Spatial4DSettings(enabled = false, intensity = 0.8f),
            sampleRate = sampleRate
        )
        val (disL, disR) = engine.processStereo(0.65f, -0.42f)
        assertEquals("Disabled 4D must pass left channel unaltered", 0.65f, disL, 0.0001f)
        assertEquals("Disabled 4D must pass right channel unaltered", -0.42f, disR, 0.0001f)

        // When enabled with 0% intensity
        engine.configure(
            Spatial4DSettings(enabled = true, intensity = 0.0f),
            sampleRate = sampleRate
        )
        val (zeroL, zeroR) = engine.processStereo(0.33f, 0.77f)
        assertEquals("0% intensity 4D must pass left channel unaltered", 0.33f, zeroL, 0.0001f)
        assertEquals("0% intensity 4D must pass right channel unaltered", 0.77f, zeroR, 0.0001f)
    }

    @Test
    fun testSpatial4DBassPhasePreservation() {
        val sampleRate = 44100f
        val engine = Spatial4DEngine(sampleRate)
        engine.configure(
            Spatial4DSettings(
                enabled = true,
                intensity = 1.0f,
                width = 2.0f,
                depth = 0.8f,
                movementMode = SpatialMovementMode.MEDIUM_ORBIT
            ),
            sampleRate = sampleRate
        )

        // Process 50 Hz sub-bass mono signal
        var maxDiffAtLowFreq = 0f
        for (i in 0 until 1000) {
            val bassSample = kotlin.math.sin(2.0 * Math.PI * 50.0 * i / sampleRate).toFloat() * 0.8f
            val (outL, outR) = engine.processStereo(bassSample, bassSample)
            maxDiffAtLowFreq = maxOf(maxDiffAtLowFreq, abs(outL - outR))
        }

        // Bass frequencies (<160Hz) are anchored in centered mono and must not have destructive phase divergence
        assertTrue("Sub-bass phase difference must remain tightly controlled, max diff was $maxDiffAtLowFreq", maxDiffAtLowFreq < 0.25f)
    }

    @Test
    fun testExtremeEqAntiClippingProtection() {
        val sampleRate = 44100f
        val eqFilter = BiquadFilter(sampleRate)
        // Boost +15dB peaking
        eqFilter.configure(FilterType.PEAKING, 1000f, 15.0f, 1.0f)

        val limiter = PeakLimiter(sampleRate)
        limiter.configure(enabled = true, ceilingDb = -0.5f)

        // Extreme combination: full amplitude 1.0f signal into +15dB boost followed by brickwall limiter
        var maxOutput = 0f
        for (i in 0 until 500) {
            val sample = kotlin.math.sin(2.0 * Math.PI * 1000.0 * i / sampleRate).toFloat()
            val (boostedL, boostedR) = eqFilter.processStereo(sample, sample)
            val (limitedL, limitedR) = limiter.processStereo(boostedL, boostedR)

            assertFalse("Limited output L should not be NaN", limitedL.isNaN())
            assertFalse("Limited output R should not be NaN", limitedR.isNaN())
            maxOutput = maxOf(maxOutput, abs(limitedL), abs(limitedR))
        }

        // Output must remain strictly clamped below 1.0f to avoid 16-bit integer wrap-around clipping
        assertTrue("Output should not exceed 1.0f even under +15dB boost, got $maxOutput", maxOutput <= 1.0f)
    }
}
