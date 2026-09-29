package com.example.aura.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * AURA Unified Motion System
 * Provides standardized duration tokens, cinematic easing curves, and interaction modifiers
 * so the entire application feels coherent, responsive, and commercial-grade.
 */
object AuraMotion {
    // Standardized Duration Tokens (ms)
    const val DurationMicro = 150
    const val DurationButtonPress = 100
    const val DurationSelection = 200
    const val DurationQuick = 250
    const val DurationPageTransition = 300
    const val DurationBottomSheet = 350
    const val DurationPlayerExpand = 380
    const val DurationArtworkTransition = 450
    const val DurationArtworkColorTransition = 500
    const val DurationLyricsScroll = 400

    // Premium Easing Curves
    // Smooth, cinematic, controlled with zero jarring spring bounce
    val CinematicEasing: Easing = CubicBezierEasing(0.22f, 1.0f, 0.36f, 1.0f)
    val EmphasizedEasing: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val ResponsiveEasing: Easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
    val StandardEasing: Easing = FastOutSlowInEasing

    // Helper Animation Specs
    fun <T> cinematicTween(durationMillis: Int = DurationQuick) =
        tween<T>(durationMillis = durationMillis, easing = CinematicEasing)

    fun <T> artworkTween() =
        tween<T>(durationMillis = DurationArtworkTransition, easing = CinematicEasing)

    fun <T> colorTween() =
        tween<T>(durationMillis = DurationArtworkColorTransition, easing = LinearOutSlowInEasing)
}

/**
 * Subtle tactile press modifier for buttons, icons, and cards.
 * Scales down subtly (default 0.95f) on touch down and springs back smoothly on release.
 */
fun Modifier.auraPressable(
    enabled: Boolean = true,
    pressedScale: Float = 0.95f,
    hapticFeedback: Boolean = false,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else 1.0f,
        animationSpec = tween(
            durationMillis = AuraMotion.DurationButtonPress,
            easing = AuraMotion.ResponsiveEasing
        ),
        label = "aura_press_scale"
    )

    this
        .scale(scale)
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = {
                        if (hapticFeedback) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onClick()
                    }
                )
            } else Modifier
        )
}

/**
 * Low-contrast, slow, elegant shimmer for loading placeholders.
 * Avoids blinding white flashes.
 */
fun Modifier.auraShimmer(
    visible: Boolean = true,
    baseColor: Color? = null,
    highlightColor: Color? = null
): Modifier = composed {
    if (!visible) return@composed this

    val transition = rememberInfiniteTransition(label = "aura_shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -300f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val base = baseColor ?: Color(0xFF1E212B).copy(alpha = 0.45f)
    val highlight = highlightColor ?: Color(0xFF2C303E).copy(alpha = 0.75f)

    val brush = Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(translateAnim - 250f, translateAnim - 250f),
        end = Offset(translateAnim + 250f, translateAnim + 250f)
    )

    this.background(brush)
}
