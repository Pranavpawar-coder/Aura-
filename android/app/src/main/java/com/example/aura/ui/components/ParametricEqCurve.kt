package com.example.aura.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.aura.data.audio.dsp.BiquadFilter
import com.example.aura.domain.model.audio.ParametricBand
import com.example.aura.theme.AuraOutline
import com.example.aura.theme.AuraSurfaceContainer
import kotlin.math.log10
import kotlin.math.pow

/**
 * Authentic audiophile Parametric EQ Curve visualizer and interactive touch surface.
 * Computes exact cumulative complex magnitude response across 20 Hz to 20 kHz.
 */
@Composable
fun ParametricEqCurve(
    bands: List<ParametricBand>,
    accentColor: Color,
    modifier: Modifier = Modifier,
    selectedBandId: String? = null,
    onBandDragged: ((String, Float, Float) -> Unit)? = null
) {
    // Instantiate calculation filters once
    val dspFilters = remember(bands) {
        bands.map { band ->
            val filter = BiquadFilter(48000f)
            if (band.enabled) {
                filter.configure(band.filterType, band.frequencyHz, band.gainDb, band.qFactor, 48000f)
            }
            Pair(band, filter)
        }
    }

    val minLogF = log10(20.0)
    val maxLogF = log10(20000.0)
    val logRange = maxLogF - minLogF

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AuraSurfaceContainer)
            .border(1.dp, AuraOutline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(bands) {
                    if (onBandDragged != null) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val width = size.width.toFloat()
                            val height = size.height.toFloat()
                            val tapX = change.position.x.coerceIn(0f, width)
                            val tapY = change.position.y.coerceIn(0f, height)

                            // Find nearest band or currently selected band
                            val normX = (tapX / width).toDouble()
                            val freq = 10.0.pow(minLogF + normX * logRange).toFloat()
                            // -15dB (bottom) to +15dB (top)
                            val normY = (tapY / height).coerceIn(0f, 1f)
                            val gainDb = ((1f - normY) * 30f - 15f)

                            val targetBand = bands.find { it.id == selectedBandId }
                                ?: bands.minByOrNull { band ->
                                    val bNormX = (log10(band.frequencyHz.toDouble()) - minLogF) / logRange
                                    kotlin.math.abs(bNormX.toFloat() * width - tapX)
                                }

                            targetBand?.let {
                                onBandDragged(it.id, freq.coerceIn(20f, 20000f), gainDb.coerceIn(-15f, 15f))
                            }
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            if (width <= 0f || height <= 0f) return@Canvas

            val centerY = height / 2f
            val maxDbRange = 15f // -15 dB to +15 dB

            // 1. Grid lines (0 dB, +10 dB, -10 dB, and standard octave freqs)
            val gridColor = Color.White.copy(alpha = 0.08f)
            // Center 0dB line
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = 1.dp.toPx()
            )
            // +10dB
            val yPlus10 = centerY - (10f / maxDbRange) * centerY
            drawLine(color = gridColor, start = Offset(0f, yPlus10), end = Offset(width, yPlus10))
            // -10dB
            val yMinus10 = centerY + (10f / maxDbRange) * centerY
            drawLine(color = gridColor, start = Offset(0f, yMinus10), end = Offset(width, yMinus10))

            // Frequency grid lines (100 Hz, 1 kHz, 10 kHz)
            listOf(100.0, 1000.0, 10000.0).forEach { f ->
                val normX = ((log10(f) - minLogF) / logRange).toFloat()
                drawLine(
                    color = gridColor,
                    start = Offset(normX * width, 0f),
                    end = Offset(normX * width, height)
                )
            }

            // 2. Compute curve points
            val curvePath = Path()
            val fillPath = Path()
            val steps = 120
            var started = false

            for (step in 0..steps) {
                val normX = step.toDouble() / steps.toDouble()
                val freq = 10.0.pow(minLogF + normX * logRange).toFloat()

                // Sum all active filter responses in dB
                var totalGainDb = 0f
                for ((band, filter) in dspFilters) {
                    if (band.enabled) {
                        totalGainDb += filter.getMagnitudeResponseDb(freq)
                    }
                }

                val x = (normX * width).toFloat()
                val y = (centerY - (totalGainDb / maxDbRange) * centerY).coerceIn(4f, height - 4f)

                if (!started) {
                    curvePath.moveTo(x, y)
                    fillPath.moveTo(x, centerY)
                    fillPath.lineTo(x, y)
                    started = true
                } else {
                    curvePath.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }

            fillPath.lineTo(width, centerY)
            fillPath.close()

            // 3. Draw curve fill gradient
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    listOf(accentColor.copy(alpha = 0.28f), accentColor.copy(alpha = 0.03f), Color.Transparent),
                    startY = 0f,
                    endY = height
                )
            )

            // 4. Draw curve line
            drawPath(
                path = curvePath,
                color = accentColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // 5. Draw band control nodes
            bands.forEach { band ->
                val bNormX = ((log10(band.frequencyHz.toDouble().coerceIn(20.0, 20000.0)) - minLogF) / logRange).toFloat()
                val nodeX = bNormX * width
                val nodeY = (centerY - (band.gainDb / maxDbRange) * centerY).coerceIn(12f, height - 12f)
                val isSelected = band.id == selectedBandId

                // Glow ring if selected
                if (isSelected) {
                    drawCircle(
                        color = accentColor.copy(alpha = 0.35f),
                        radius = 16.dp.toPx(),
                        center = Offset(nodeX, nodeY)
                    )
                }

                // Outer circle
                drawCircle(
                    color = if (band.enabled) accentColor else Color.Gray,
                    radius = if (isSelected) 8.dp.toPx() else 6.dp.toPx(),
                    center = Offset(nodeX, nodeY)
                )
                // Inner center dot
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = Offset(nodeX, nodeY)
                )
            }
        }
    }
}
