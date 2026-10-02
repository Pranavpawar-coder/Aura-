package com.example.aura.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.aura.theme.AuraDeepBlack
import com.example.aura.theme.AuraGlassBorderDefault
import com.example.aura.theme.AuraGlassSurfaceDefault
import com.example.aura.theme.AuraMotion
import com.example.aura.theme.AuraPrimary
import com.example.aura.theme.AuraTextSecondary
import com.example.aura.theme.AuraTextTertiary
import com.example.aura.theme.auraPressable

/**
 * Reusable AURA Glass Surface (Section 6)
 * Subtle translucent dark/white surface with thin low-opacity border, soft shadow,
 * and restrained corner radius.
 */
@Composable
fun AuraGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = AuraGlassSurfaceDefault,
    borderColor: Color = AuraGlassBorderDefault,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 0.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .then(if (elevation > 0.dp) Modifier.shadow(elevation, shape) else Modifier)
            .clip(shape)
            .background(backgroundColor)
            .border(borderWidth, borderColor, shape),
        content = content
    )
}

/**
 * Reusable AURA Glass Pill (Section 6)
 * Translucent rounded pill for badges, tags, and small utility indicators.
 */
@Composable
fun AuraGlassPill(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White.copy(alpha = 0.08f),
    borderColor: Color = Color.White.copy(alpha = 0.12f),
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    AuraGlassSurface(
        modifier = modifier,
        shape = CircleShape,
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        borderWidth = borderWidth,
        content = content
    )
}

/**
 * Reusable AURA Glass Button (Section 6)
 * Interactive tactile button with subtle translucent glass appearance.
 */
@Composable
fun AuraGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    shape: Shape = CircleShape,
    isActive: Boolean = false,
    activeColor: Color = AuraPrimary,
    backgroundColor: Color = if (isActive) activeColor.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.08f),
    borderColor: Color = if (isActive) activeColor.copy(alpha = 0.40f) else Color.White.copy(alpha = 0.12f),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
            .auraPressable(pressedScale = 0.90f, hapticFeedback = true, onClick = onClick),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * Reusable AURA Glass Control (Section 6)
 * Capsule or pill shaped interactive button for primary/secondary player actions.
 */
@Composable
fun AuraGlassControl(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    backgroundColor: Color = Color.White.copy(alpha = 0.08f),
    borderColor: Color = Color.White.copy(alpha = 0.12f),
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
            .auraPressable(pressedScale = 0.94f, hapticFeedback = true, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        content = content
    )
}

/**
 * Reusable AURA Glass Bottom Bar Container (Section 6 & 19)
 * Used for minimal navigation bars and player docks.
 */
@Composable
fun AuraGlassBottomBar(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xCC090909),
    borderColor: Color = Color.White.copy(alpha = 0.08f),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        content = content
    )
}

/**
 * Atmospheric Artwork Background (Section 5)
 * Pipeline: Artwork -> Scale -> Blur -> Dark Overlay -> Vertical Gradient -> Deep Black lower region.
 * Guarantees high contrast and text readability.
 */
@Composable
fun AuraAtmosphericBackground(
    artworkUri: String?,
    modifier: Modifier = Modifier,
    gradientStart: Color = Color(0xFF140D12),
    gradientEnd: Color = AuraDeepBlack,
    glowColor: Color = Color.Transparent,
    blurRadius: Dp = 60.dp,
    imageOpacity: Float = 0.35f
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (!artworkUri.isNullOrBlank()) {
            AsyncImage(
                model = artworkUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(1.25f)
                    .blur(blurRadius)
            )
        }

        // Deep vignette and dark gradient overlay fading down to Deep Black (#050505)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            gradientStart.copy(alpha = 0.85f),
                            Color(0xFF090909).copy(alpha = 0.90f),
                            gradientEnd
                        )
                    )
                )
        )

        // Subtle ambient radial glow from artwork tones
        if (glowColor != Color.Transparent) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp)
                    .size(320.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                glowColor.copy(alpha = 0.20f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

/**
 * Reusable AURA Progress Bar & Scrubber (Section 10)
 * Thin track, soft accent progress, glowing fluid wave scrubber/thumb bead,
 * harmonic sine curve trace, and monospace time indicators.
 */
@Composable
fun AuraProgressBar(
    positionMs: Long,
    durationMs: Long,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onSeekTimestamp: ((Long) -> Unit)? = null,
    activeColor: Color = AuraPrimary,
    showWaveformTrail: Boolean = true,
    showTimestamps: Boolean = true
) {
    var widthPx by remember { mutableFloatStateOf(0f) }
    val progress = remember(positionMs, durationMs) {
        if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    }

    val totalSec = positionMs / 1000
    val currentTimeStr = remember(totalSec) { "%d:%02d".format(totalSec / 60, totalSec % 60) }
    val remainingSec = ((durationMs - positionMs) / 1000).coerceAtLeast(0)
    val remainingTimeStr = remember(remainingSec) { "-%d:%02d".format(remainingSec / 60, remainingSec % 60) }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .onSizeChanged { widthPx = it.width.toFloat() }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (widthPx > 0) {
                            val fraction = (offset.x / widthPx).coerceIn(0f, 1f)
                            onSeek(fraction)
                            if (onSeekTimestamp != null && durationMs > 0) {
                                onSeekTimestamp((durationMs * fraction).toLong())
                            }
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        if (widthPx > 0) {
                            val fraction = (change.position.x / widthPx).coerceIn(0f, 1f)
                            onSeek(fraction)
                            if (onSeekTimestamp != null && durationMs > 0) {
                                onSeekTimestamp((durationMs * fraction).toLong())
                            }
                        }
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            // Background track line (thin, 2.5dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
            )

            // Progress bar with soft accent gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                activeColor.copy(alpha = 0.50f),
                                activeColor
                            )
                        )
                    )
            )

            // Soundwave trail Canvas preceding the thumb
            if (widthPx > 0) {
                val thumbOffset = widthPx * progress

                if (showWaveformTrail && progress > 0.06f) {
                    val waveWidthDp = 50.dp
                    Canvas(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (thumbOffset - waveWidthDp.toPx() + 6.dp.toPx()).toInt().coerceAtLeast(0),
                                    0
                                )
                            }
                            .size(width = waveWidthDp, height = 24.dp)
                    ) {
                        val w = size.width
                        val h = size.height
                        val midY = h / 2f

                        // Harmonic wave curve
                        val path = Path().apply {
                            moveTo(0f, midY)
                            cubicTo(w * 0.25f, midY - 4.dp.toPx(), w * 0.55f, midY + 4.dp.toPx(), w, midY)
                        }
                        drawPath(
                            path = path,
                            color = activeColor.copy(alpha = 0.60f),
                            style = Stroke(
                                width = 1.6.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )
                    }
                }

                // Fluid glowing thumb bead
                Box(
                    modifier = Modifier
                        .offset { IntOffset((thumbOffset - 6.dp.toPx()).toInt(), 0) }
                        .size(13.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .shadow(8.dp, CircleShape, spotColor = activeColor, ambientColor = activeColor)
                        .border(1.5.dp, Color(0xFF1B0D09), CircleShape)
                )
            }
        }

        if (showTimestamps) {
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = currentTimeStr,
                    color = AuraTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = remainingTimeStr,
                    color = AuraTextTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
