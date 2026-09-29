package com.example.aura.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.aura.domain.model.audio.VisualizerStyle
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Lightweight, high-performance real-time audio visualizer.
 * Supports Spectrum, Bars, Waveform, Circular, and Minimal styles.
 * Only draws when visible, preserving battery and CPU resources.
 */
@Composable
fun AuraVisualizerView(
    audioData: FloatArray,
    style: VisualizerStyle,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return@Canvas

        val sampleCount = audioData.size
        val hasData = sampleCount > 0

        when (style) {
            VisualizerStyle.SPECTRUM -> {
                val barCount = 32
                val barWidth = (width / barCount) * 0.75f
                val spacing = (width / barCount) * 0.25f

                for (i in 0 until barCount) {
                    val rawVal = if (hasData) {
                        val dataIdx = (i * sampleCount / barCount).coerceIn(0, sampleCount - 1)
                        abs(audioData[dataIdx])
                    } else 0.05f

                    val norm = (rawVal * 2.5f).coerceIn(0.05f, 1.0f)
                    val barHeight = norm * height * 0.85f
                    val left = i * (barWidth + spacing)
                    val top = height - barHeight

                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(accentColor, accentColor.copy(alpha = 0.4f)),
                            startY = top,
                            endY = height
                        ),
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )
                }
            }

            VisualizerStyle.BARS -> {
                val barCount = 16
                val barWidth = (width / barCount) * 0.65f
                val spacing = (width / barCount) * 0.35f
                val centerY = height / 2f

                for (i in 0 until barCount) {
                    val rawVal = if (hasData) {
                        val dataIdx = (i * sampleCount / barCount).coerceIn(0, sampleCount - 1)
                        abs(audioData[dataIdx])
                    } else 0.08f

                    val norm = (rawVal * 2.8f).coerceIn(0.08f, 1.0f)
                    val barHeight = norm * height * 0.45f
                    val left = i * (barWidth + spacing)

                    drawRoundRect(
                        color = accentColor,
                        topLeft = Offset(left, centerY - barHeight),
                        size = Size(barWidth, barHeight * 2f),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )
                }
            }

            VisualizerStyle.WAVEFORM -> {
                val path = Path()
                val centerY = height / 2f
                val points = 64
                val stepX = width / (points - 1)

                var started = false
                for (i in 0 until points) {
                    val sample = if (hasData) {
                        val idx = (i * sampleCount / points).coerceIn(0, sampleCount - 1)
                        audioData[idx]
                    } else {
                        sin((i * 0.3f).toDouble()).toFloat() * 0.1f
                    }

                    val x = i * stepX
                    val y = centerY + sample * (height * 0.4f)

                    if (!started) {
                        path.moveTo(x, y)
                        started = true
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = accentColor,
                    style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                )
            }

            VisualizerStyle.CIRCULAR -> {
                val center = Offset(width / 2f, height / 2f)
                val baseRadius = (minOf(width, height) / 3f)
                val barCount = 36
                val angleStep = (2.0 * PI / barCount).toFloat()

                for (i in 0 until barCount) {
                    val rawVal = if (hasData) {
                        val idx = (i * sampleCount / barCount).coerceIn(0, sampleCount - 1)
                        abs(audioData[idx])
                    } else 0.05f

                    val barLen = (rawVal * baseRadius * 1.2f).coerceIn(4f, baseRadius)
                    val angle = i * angleStep
                    val startX = center.x + baseRadius * cos(angle)
                    val startY = center.y + baseRadius * sin(angle)
                    val endX = center.x + (baseRadius + barLen) * cos(angle)
                    val endY = center.y + (baseRadius + barLen) * sin(angle)

                    drawLine(
                        color = accentColor.copy(alpha = (0.5f + rawVal).coerceIn(0.4f, 1f)),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                }

                drawCircle(
                    color = accentColor.copy(alpha = 0.15f),
                    radius = baseRadius * 0.9f,
                    center = center
                )
            }

            VisualizerStyle.MINIMAL -> {
                val center = Offset(width / 2f, height / 2f)
                val energy = if (hasData) {
                    var sum = 0f
                    for (v in audioData) sum += abs(v)
                    (sum / sampleCount * 3f).coerceIn(0.1f, 1.0f)
                } else 0.1f

                val maxRadius = minOf(width, height) / 2.2f
                val haloRadius = maxRadius * (0.4f + energy * 0.6f)

                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(accentColor.copy(alpha = 0.4f * energy), Color.Transparent),
                        center = center,
                        radius = haloRadius
                    ),
                    radius = haloRadius,
                    center = center
                )

                drawCircle(
                    color = accentColor,
                    radius = 4f + energy * 6f,
                    center = center
                )
            }
        }
    }
}
