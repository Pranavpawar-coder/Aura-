package com.example.aura.data.audio.dsp

import com.example.aura.domain.model.audio.Spatial4DSettings
import com.example.aura.domain.model.audio.SpatialMovementMode
import com.example.aura.domain.model.audio.SpatialPreset
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * AURA 4D Spatial Audio DSP Engine.
 *
 * Implements real-time binaural spatialization, ITD (Interaural Time Difference),
 * IID (Interaural Intensity Difference), head-shadowing acoustic filtering,
 * front/back pinna spectral cues, orbital rotation dynamics, and stereo expansion.
 */
class Spatial4DEngine(
    private var sampleRate: Float = 44100f
) {
    // Delay buffer for fractional ITD (max 128 samples is ample for ~2.8ms at 44.1/48kHz)
    private val delayBufferLeft = FloatArray(256)
    private val delayBufferRight = FloatArray(256)
    private var delayWriteIndex = 0

    // Early reflection delay line (depth / distance simulation)
    private val earlyReflectionBufferL = FloatArray(2048)
    private val earlyReflectionBufferR = FloatArray(2048)
    private var earlyReflWriteIndex = 0

    // Filter states for head shadowing and front/back cues
    private var shadowFilterL = 0f
    private var shadowFilterR = 0f
    private var frontBackFilterL = 0f
    private var frontBackFilterR = 0f

    // Current spatial parameters
    private var isEnabled = false
    private var intensity = 0.7f
    private var width = 1.2f
    private var depth = 0.5f
    private var distance = 0.3f
    private var rotationSpeed = 0.5f
    private var movementMode = SpatialMovementMode.STATIC

    // Current angle in radians (for orbit)
    private var currentAzimuthRad = 0.0f
    private var positionX = 0f
    private var positionY = 0.5f
    private var positionZ = 0f

    // Internal smoothed state
    private var currentPanLeft = 1.0f
    private var currentPanRight = 1.0f
    private var currentDelayL = 0f
    private var currentDelayR = 0f

    // Low-frequency mono crossover state (~160 Hz bass safety anchor)
    private var lowPassStateL = 0f
    private var lowPassStateR = 0f

    fun reset() {
        delayBufferLeft.fill(0f)
        delayBufferRight.fill(0f)
        earlyReflectionBufferL.fill(0f)
        earlyReflectionBufferR.fill(0f)
        delayWriteIndex = 0
        earlyReflWriteIndex = 0
        shadowFilterL = 0f
        shadowFilterR = 0f
        frontBackFilterL = 0f
        frontBackFilterR = 0f
        lowPassStateL = 0f
        lowPassStateR = 0f
    }

    fun configure(
        settings: Spatial4DSettings,
        sampleRate: Float
    ) {
        this.sampleRate = sampleRate.coerceAtLeast(8000f)
        this.isEnabled = settings.enabled || settings.isPreviewActive
        this.intensity = settings.intensity.coerceIn(0f, 1f)
        this.width = settings.width.coerceIn(0f, 2.5f)
        this.depth = settings.depth.coerceIn(0f, 1f)
        this.distance = settings.distance.coerceIn(0f, 1f)
        this.rotationSpeed = settings.rotationSpeed.coerceIn(0f, 2f)
        this.movementMode = settings.movementMode
        this.positionX = settings.positionX.coerceIn(-1f, 1f)
        this.positionY = settings.positionY.coerceIn(-1f, 1f)
        this.positionZ = settings.positionZ.coerceIn(-1f, 1f)

        // Apply preset overrides if applicable
        when (settings.preset) {
            SpatialPreset.NORMAL -> {
                width = 1.0f
                depth = 0.2f
                distance = 0.1f
            }
            SpatialPreset.WIDE -> {
                width = 1.6f
                depth = 0.4f
                distance = 0.25f
            }
            SpatialPreset.SURROUND -> {
                width = 1.8f
                depth = 0.7f
                distance = 0.45f
            }
            SpatialPreset.FOUR_D -> {
                width = 1.5f
                depth = 0.6f
                distance = 0.35f
                movementMode = SpatialMovementMode.MEDIUM_ORBIT
            }
            SpatialPreset.FOUR_D_SLOW -> {
                width = 1.4f
                depth = 0.5f
                distance = 0.3f
                movementMode = SpatialMovementMode.SLOW_ORBIT
            }
            SpatialPreset.FOUR_D_FAST -> {
                width = 1.6f
                depth = 0.7f
                distance = 0.4f
                movementMode = SpatialMovementMode.FAST_ORBIT
            }
            SpatialPreset.CINEMA -> {
                width = 2.0f
                depth = 0.85f
                distance = 0.5f
            }
            SpatialPreset.CONCERT -> {
                width = 1.7f
                depth = 0.9f
                distance = 0.6f
            }
            SpatialPreset.STUDIO -> {
                width = 1.15f
                depth = 0.25f
                distance = 0.15f
            }
        }
    }

    /**
     * Process stereo frame through the 4D spatial audio engine.
     */
    fun processStereo(inLeft: Float, inRight: Float): Pair<Float, Float> {
        if (!isEnabled || intensity <= 0.001f) {
            return Pair(inLeft, inRight)
        }

        // 0. Low-frequency crossover (~160 Hz)
        // Keep bass frequencies mono-centered to eliminate destructive phase cancellation and diaphragm distortion
        val crossoverAlpha = (2.0f * PI.toFloat() * 160f / sampleRate).coerceIn(0.001f, 0.4f)
        lowPassStateL += crossoverAlpha * (inLeft - lowPassStateL)
        lowPassStateR += crossoverAlpha * (inRight - lowPassStateR)

        val bassMono = (lowPassStateL + lowPassStateR) * 0.5f
        val spatialInL = inLeft - lowPassStateL
        val spatialInR = inRight - lowPassStateR

        // 1. Calculate Azimuth & Orbit Rotation
        val angularVelocity = when (movementMode) {
            SpatialMovementMode.STATIC -> 0.0f
            SpatialMovementMode.SLOW_ORBIT -> (2.0f * PI.toFloat() / (sampleRate * 6.5f)) * rotationSpeed
            SpatialMovementMode.MEDIUM_ORBIT -> (2.0f * PI.toFloat() / (sampleRate * 2.8f)) * rotationSpeed
            SpatialMovementMode.FAST_ORBIT -> (2.0f * PI.toFloat() / (sampleRate * 1.3f)) * rotationSpeed
            SpatialMovementMode.CUSTOM -> (2.0f * PI.toFloat() / (sampleRate * 3.5f)) * rotationSpeed
        }

        if (angularVelocity != 0.0f) {
            currentAzimuthRad += angularVelocity
            if (currentAzimuthRad > 2.0 * PI) {
                currentAzimuthRad -= (2.0 * PI).toFloat()
            }
        } else {
            // Static/interactive position
            currentAzimuthRad = atan2(positionX, positionY.coerceAtLeast(0.01f))
        }

        val sinAzimuth = sin(currentAzimuthRad)
        val cosAzimuth = cos(currentAzimuthRad)

        // 2. Interaural Time Difference (ITD)
        // Max delay is ~0.65ms (head radius ~8.7cm / sound speed ~343 m/s)
        val maxItdSamples = (0.00065f * sampleRate).coerceAtMost(60f)
        val targetDelayL: Float
        val targetDelayR: Float
        if (sinAzimuth > 0) { // Source on the Right -> Left ear delayed
            targetDelayL = (sinAzimuth * maxItdSamples) * intensity
            targetDelayR = 0f
        } else { // Source on the Left -> Right ear delayed
            targetDelayL = 0f
            targetDelayR = (-sinAzimuth * maxItdSamples) * intensity
        }

        // Smooth delay transitions (fractional 1-pole filter)
        currentDelayL += 0.005f * (targetDelayL - currentDelayL)
        currentDelayR += 0.005f * (targetDelayR - currentDelayR)

        // Write into ITD delay buffers (spatial mid/highs only)
        delayBufferLeft[delayWriteIndex] = spatialInL
        delayBufferRight[delayWriteIndex] = spatialInR

        // Read with fractional linear interpolation
        val readIdxL = (delayWriteIndex - currentDelayL + 256f) % 256f
        val idxL0 = readIdxL.toInt() % 256
        val idxL1 = (idxL0 + 1) % 256
        val fracL = readIdxL - idxL0
        val delayedL = delayBufferLeft[idxL0] * (1f - fracL) + delayBufferLeft[idxL1] * fracL

        val readIdxR = (delayWriteIndex - currentDelayR + 256f) % 256f
        val idxR0 = readIdxR.toInt() % 256
        val idxR1 = (idxR0 + 1) % 256
        val fracR = readIdxR - idxR0
        val delayedR = delayBufferRight[idxR0] * (1f - fracR) + delayBufferRight[idxR1] * fracR

        delayWriteIndex = (delayWriteIndex + 1) % 256

        // 3. Interaural Intensity Difference (IID) & Head-Shadowing
        // Attenuate contralateral channel with 1-pole low pass filter
        val shadowAmountL = (sinAzimuth.coerceAtLeast(0f) * 0.5f * intensity)
        val shadowAmountR = ((-sinAzimuth).coerceAtLeast(0f) * 0.5f * intensity)

        shadowFilterL += (1.0f - shadowAmountL * 0.7f) * (delayedL - shadowFilterL)
        shadowFilterR += (1.0f - shadowAmountR * 0.7f) * (delayedR - shadowFilterR)

        val iidLeft = delayedL * (1.0f - shadowAmountL * 0.5f) + shadowFilterL * (shadowAmountL * 0.5f)
        val iidRight = delayedR * (1.0f - shadowAmountR * 0.5f) + shadowFilterR * (shadowAmountR * 0.5f)

        // 4. Front-to-Back Spectral Pinna Cue (cosAzimuth: +1 front, -1 back)
        val rearFactor = ((-cosAzimuth).coerceAtLeast(0f) * 0.35f * intensity)
        frontBackFilterL += (1.0f - rearFactor) * (iidLeft - frontBackFilterL)
        frontBackFilterR += (1.0f - rearFactor) * (iidRight - frontBackFilterR)

        val spatL = iidLeft * (1.0f - rearFactor) + frontBackFilterL * rearFactor
        val spatR = iidRight * (1.0f - rearFactor) + frontBackFilterR * rearFactor

        // 5. Stereo Width Expansion with Center/Vocal Anchor
        val mid = (spatL + spatR) * 0.7071f
        val side = (spatL - spatR) * 0.7071f
        val effectiveWidth = 1.0f + (width - 1.0f) * 0.75f
        val expandedSide = side * effectiveWidth

        val wideL = (mid + expandedSide) * 0.7071f
        val wideR = (mid - expandedSide) * 0.7071f

        // 6. Early Reflections & Depth/Distance Simulation
        earlyReflectionBufferL[earlyReflWriteIndex] = wideL
        earlyReflectionBufferR[earlyReflWriteIndex] = wideR

        // Multi-tap reflection delays: 8ms (~352 samples) and 15ms (~660 samples)
        val tap1 = (earlyReflWriteIndex - (0.008f * sampleRate).toInt() + 2048) % 2048
        val tap2 = (earlyReflWriteIndex - (0.015f * sampleRate).toInt() + 2048) % 2048

        val reflL = (earlyReflectionBufferR[tap1] * 0.18f + earlyReflectionBufferL[tap2] * 0.12f) * depth
        val reflR = (earlyReflectionBufferL[tap1] * 0.18f + earlyReflectionBufferR[tap2] * 0.12f) * depth

        earlyReflWriteIndex = (earlyReflWriteIndex + 1) % 2048

        // Combine direct spatial sound with normalized reflection energy and distance attenuation
        val wetNorm = 1.0f / sqrt(1.0f + 0.3f * depth * depth)
        val distanceAtten = (1.0f - distance * 0.2f)

        // Add back the rock-solid, centered mono bass anchor
        val finalL = (wideL * distanceAtten + reflL) * wetNorm + bassMono
        val finalR = (wideR * distanceAtten + reflR) * wetNorm + bassMono

        // Smooth crossfade between dry input and 4D output using intensity
        val outL = inLeft * (1.0f - intensity) + finalL * intensity
        val outR = inRight * (1.0f - intensity) + finalR * intensity

        return Pair(outL, outR)

        return Pair(outL, outR)
    }
}
