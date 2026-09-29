package com.example.aura.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.example.aura.R
import com.example.aura.theme.auraPressable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.aura.domain.model.PlaybackState
import com.example.aura.domain.model.PlaybackStatus
import com.example.aura.theme.AuraGlassBorder
import com.example.aura.theme.AuraGlassSurface
import com.example.aura.theme.AuraOnSurface
import com.example.aura.theme.AuraOnSurfaceVariant
import com.example.aura.theme.AuraOutline
import com.example.aura.theme.AuraPrimary
import com.example.aura.theme.AuraPrimaryContainer
import com.example.aura.theme.AuraSecondary
import com.example.aura.theme.AuraSurface
import com.example.aura.theme.AuraSurfaceContainer
import com.example.aura.theme.AuraSurfaceContainerHigh
import com.example.aura.theme.AuraSurfaceContainerHighest
import com.example.aura.theme.AuraTertiary

@Composable
fun AuraHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AuraSurface.copy(alpha = 0.85f))
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AuraSurfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.aura_logo),
                    contentDescription = "AURA",
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                color = AuraOnSurface,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.2).sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {},
                modifier = Modifier.auraPressable(pressedScale = 0.90f) {}
            ) {
                Icon(
                    imageVector = Icons.Default.Cast,
                    contentDescription = "Cast",
                    tint = AuraOnSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(AuraSurfaceContainerHighest)
                    .auraPressable(pressedScale = 0.90f, hapticFeedback = true) {},
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.aura_logo),
                    contentDescription = "AURA",
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                )
            }
        }
    }
}

@Composable
fun MiniPlayer(
    playbackState: PlaybackState,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit = {},
    onClick: () -> Unit,
    onSwipeUp: () -> Unit = onClick,
    onSwipeDown: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val song = playbackState.currentSong ?: return
    val isPlaying = playbackState.status == PlaybackStatus.PLAYING
    val accentColor = com.example.aura.theme.LocalAuraAccent.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AuraSurfaceContainer.copy(alpha = 0.95f))
            .border(1.dp, AuraGlassBorder, RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                var totalDragY = 0f
                var totalDragX = 0f
                detectDragGestures(
                    onDragStart = {
                        totalDragY = 0f
                        totalDragX = 0f
                    },
                    onDrag = { change, dragAmount ->
                        totalDragX += dragAmount.x
                        totalDragY += dragAmount.y
                        if (kotlin.math.abs(totalDragY) > kotlin.math.abs(totalDragX)) {
                            change.consume()
                        }
                    },
                    onDragEnd = {
                        if (totalDragY < -35f) {
                            onSwipeUp()
                        } else if (totalDragY > 35f) {
                            onSwipeDown()
                        }
                    }
                )
            }
            .auraPressable(pressedScale = 0.99f, onClick = onClick)
    ) {
        // Scrubber Hairline along bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(2.dp)
                .background(AuraSurfaceContainerHighest)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(playbackState.progressPercent)
                    .height(2.dp)
                    .background(accentColor)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cover Art
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AuraSurfaceContainerHigh)
                        .shadow(4.dp, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (song.artworkUri != null) {
                        AsyncImage(
                            model = song.artworkUri,
                            contentDescription = song.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        AuraFallbackArtwork(
                            modifier = Modifier.fillMaxSize(),
                            cornerRadius = 10.dp,
                            iconSize = 22.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = song.title,
                        color = AuraOnSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        color = AuraOnSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                AuraAnimatedFavoriteButton(
                    isFavorite = song.isFavorite,
                    onClick = onToggleFavorite,
                    accentColor = accentColor,
                    size = 32.dp,
                    iconSize = 18.dp
                )
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier
                        .size(34.dp)
                        .auraPressable(pressedScale = 0.88f, hapticFeedback = true, onClick = onPrevious)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = AuraOnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                AuraAnimatedPlayPauseButton(
                    isPlaying = isPlaying,
                    onClick = onPlayPause,
                    size = 38.dp,
                    iconSize = 22.dp,
                    tint = AuraOnSurface
                )
                IconButton(
                    onClick = onNext,
                    modifier = Modifier
                        .size(34.dp)
                        .auraPressable(pressedScale = 0.88f, hapticFeedback = true, onClick = onNext)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = AuraOnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AuraScrubber(
    positionMs: Long,
    durationMs: Long,
    onSeek: (Float) -> Unit,
    onSeekTimestamp: ((Long) -> Unit)? = null,
    activeColor: Color = AuraPrimary,
    showWaveformTrail: Boolean = true,
    modifier: Modifier = Modifier
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
                .height(30.dp)
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
            // Background track line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
            )

            // Progress bar with active gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(3.5.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                activeColor.copy(alpha = 0.45f),
                                activeColor
                            )
                        )
                    )
            )

            // Stylized organic soundwave visualizer trail + draggable thumb bead
            if (widthPx > 0) {
                val thumbOffset = widthPx * progress

                // Waveform trail Canvas preceding the thumb
                if (showWaveformTrail && progress > 0.08f) {
                    val waveWidthDp = 52.dp
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (thumbOffset - waveWidthDp.toPx() + 6.dp.toPx()).toInt().coerceAtLeast(0),
                                    0
                                )
                            }
                            .size(width = waveWidthDp, height = 26.dp)
                    ) {
                        val w = size.width
                        val h = size.height
                        val midY = h / 2f

                        // Harmonic wave line 1
                        val path1 = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, midY)
                            cubicTo(w * 0.25f, midY - 5.dp.toPx(), w * 0.55f, midY + 5.dp.toPx(), w, midY)
                        }
                        drawPath(
                            path = path1,
                            color = activeColor.copy(alpha = 0.45f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 1.5.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        )

                        // Harmonic wave line 2
                        val path2 = androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.2f, midY)
                            cubicTo(w * 0.45f, midY + 7.dp.toPx(), w * 0.75f, midY - 6.dp.toPx(), w, midY)
                        }
                        drawPath(
                            path = path2,
                            color = activeColor.copy(alpha = 0.75f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 1.8.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        )
                    }
                }

                // Glowing thumb bead
                Box(
                    modifier = Modifier
                        .offset { IntOffset((thumbOffset - 7.dp.toPx()).toInt(), 0) }
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFCD3CB))
                        .shadow(10.dp, CircleShape, spotColor = activeColor)
                        .border(2.dp, Color(0xFF1B0D09), CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = currentTimeStr,
                color = AuraOnSurfaceVariant.copy(alpha = 0.75f),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
            Text(
                text = remainingTimeStr,
                color = AuraOnSurfaceVariant.copy(alpha = 0.75f),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
        }
    }
}


@Composable
fun AnimatedEqualizerBars(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "eq")
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(400), RepeatMode.Reverse), label = "b1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(550), RepeatMode.Reverse), label = "b2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(350), RepeatMode.Reverse), label = "b3"
    )

    Row(
        modifier = modifier.height(14.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val h1 = if (isPlaying) bar1 else 0.4f
        val h2 = if (isPlaying) bar2 else 0.6f
        val h3 = if (isPlaying) bar3 else 0.3f

        Box(modifier = Modifier.width(3.dp).height((14 * h1).dp).clip(CircleShape).background(AuraPrimary))
        Box(modifier = Modifier.width(3.dp).height((14 * h2).dp).clip(CircleShape).background(AuraSecondary))
        Box(modifier = Modifier.width(3.dp).height((14 * h3).dp).clip(CircleShape).background(AuraTertiary))
    }
}
