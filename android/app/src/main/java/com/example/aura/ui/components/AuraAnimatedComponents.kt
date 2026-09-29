package com.example.aura.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.aura.R
import com.example.aura.theme.AuraMotion
import com.example.aura.theme.AuraOnSurface
import com.example.aura.theme.AuraOnSurfaceVariant
import com.example.aura.theme.AuraOutline
import com.example.aura.theme.AuraPrimary
import com.example.aura.theme.AuraPrimaryAccent
import com.example.aura.theme.AuraSecondary
import com.example.aura.theme.AuraSurfaceContainerHigh
import com.example.aura.theme.AuraSurfaceContainerHighest
import com.example.aura.theme.auraPressable
import kotlinx.coroutines.launch

/**
 * Animated Play/Pause button with smooth scale & icon transition.
 * Avoids abrupt pops between icons.
 */
@Composable
fun AuraAnimatedPlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconSize: Dp = 26.dp,
    tint: Color = AuraOnSurface,
    backgroundColor: Color = Color.Transparent,
    contentDescription: String = if (isPlaying) "Pause" else "Play"
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
            .auraPressable(
                pressedScale = 0.92f,
                hapticFeedback = true,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isPlaying,
            transitionSpec = {
                (scaleIn(
                    initialScale = 0.75f,
                    animationSpec = tween(AuraMotion.DurationQuick, easing = AuraMotion.CinematicEasing)
                ) + fadeIn(
                    animationSpec = tween(AuraMotion.DurationQuick)
                )) togetherWith (scaleOut(
                    targetScale = 0.75f,
                    animationSpec = tween(AuraMotion.DurationQuick, easing = AuraMotion.CinematicEasing)
                ) + fadeOut(
                    animationSpec = tween(AuraMotion.DurationQuick)
                ))
            },
            label = "play_pause_morph"
        ) { playing ->
            Icon(
                imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

/**
 * Animated Favorite heart with subtle, controlled pulse on tap.
 * Settles smoothly without excessive cartoon bounce.
 */
@Composable
fun AuraAnimatedFavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = AuraPrimary,
    inactiveColor: Color = AuraOutline,
    size: Dp = 40.dp,
    iconSize: Dp = 22.dp
) {
    val haptic = LocalHapticFeedback.current
    val scaleAnim = remember { Animatable(1.0f) }
    val coroutineScope = rememberCoroutineScope()

    val animatedColor by animateColorAsState(
        targetValue = if (isFavorite) accentColor else inactiveColor,
        animationSpec = tween(AuraMotion.DurationQuick, easing = AuraMotion.CinematicEasing),
        label = "fav_color"
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    coroutineScope.launch {
                        scaleAnim.animateTo(
                            targetValue = 1.22f,
                            animationSpec = tween(90, easing = FastOutSlowInEasing)
                        )
                        scaleAnim.animateTo(
                            targetValue = 1.0f,
                            animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow)
                        )
                    }
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
            tint = animatedColor,
            modifier = Modifier
                .size(iconSize)
                .scale(scaleAnim.value)
        )
    }
}

/**
 * Premium AURA fallback artwork when track has no album art or image fails to load.
 * Displays a subtle dark glass gradient with the official AURA brand emblem.
 */
@Composable
fun AuraFallbackArtwork(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    iconSize: Dp = 36.dp,
    accentColor: Color = AuraPrimaryAccent
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.22f),
                        AuraSurfaceContainerHighest.copy(alpha = 0.95f),
                        AuraSurfaceContainerHigh
                    )
                )
            )
            .border(
                1.dp,
                accentColor.copy(alpha = 0.15f),
                RoundedCornerShape(cornerRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.aura_logo),
            contentDescription = "AURA",
            modifier = Modifier
                .size(iconSize)
                .clip(CircleShape)
        )
    }
}

/**
 * Capsule Play/Pause Button as showcased in modern offline player designs.
 * Features an organic gradient capsule with Play/Pause icon + status label,
 * smooth animation morphing, subtle glowing border and shadow.
 */
@Composable
fun AuraCapsulePlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier,
    contentDescription: String = if (isPlaying) "Pause" else "Play"
) {
    val haptic = LocalHapticFeedback.current
    val capsuleGradient = remember(accentColor) {
        val darkCoffee = Color(0xFF1B0D09)
        val deepAccent = Color(
            red = (accentColor.red * 0.35f + darkCoffee.red * 0.65f).coerceIn(0f, 1f),
            green = (accentColor.green * 0.35f + darkCoffee.green * 0.65f).coerceIn(0f, 1f),
            blue = (accentColor.blue * 0.35f + darkCoffee.blue * 0.65f).coerceIn(0f, 1f)
        )
        Brush.horizontalGradient(
            listOf(
                deepAccent,
                accentColor.copy(alpha = 0.85f)
            )
        )
    }

    Box(
        modifier = modifier
            .height(50.dp)
            .shadow(12.dp, RoundedCornerShape(25.dp), spotColor = accentColor.copy(alpha = 0.45f))
            .clip(RoundedCornerShape(25.dp))
            .background(capsuleGradient)
            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(25.dp))
            .auraPressable(
                pressedScale = 0.94f,
                hapticFeedback = true,
                onClick = onClick
            )
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isPlaying,
            transitionSpec = {
                (scaleIn(initialScale = 0.85f, animationSpec = tween(AuraMotion.DurationQuick, easing = AuraMotion.CinematicEasing)) + fadeIn()) togetherWith
                        (scaleOut(targetScale = 0.85f, animationSpec = tween(AuraMotion.DurationQuick, easing = AuraMotion.CinematicEasing)) + fadeOut())
            },
            label = "capsule_play_pause"
        ) { playing ->
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Icon(
                    imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = contentDescription,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                androidx.compose.material3.Text(
                    text = if (playing) "Pause" else "Play",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/**
 * Translucent glass pill button matching the Stitch player UI design.
 */
@Composable
fun AuraGlassPillButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    isActive: Boolean = false,
    activeColor: Color = Color.White,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val bgColor = if (isActive) activeColor.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.08f)
    val borderColor = if (isActive) activeColor.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.12f)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, borderColor, CircleShape)
            .auraPressable(
                pressedScale = 0.90f,
                hapticFeedback = true,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

