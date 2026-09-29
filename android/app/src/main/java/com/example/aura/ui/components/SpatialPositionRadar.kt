package com.example.aura.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aura.domain.model.audio.SpatialMovementMode
import com.example.aura.theme.AuraOutline
import com.example.aura.theme.AuraSurfaceContainer
import com.example.aura.theme.AuraSurfaceContainerHigh
import kotlin.math.cos
import kotlin.math.sin

/**
 * Interactive 2D binaural sound position radar for 4D Spatial Audio.
 * Allows repositioning the virtual audio source in the soundstage (FRONT / BACK / LEFT / RIGHT).
 */
@Composable
fun SpatialPositionRadar(
    positionX: Float, // -1 (Left) to +1 (Right)
    positionY: Float, // -1 (Back) to +1 (Front)
    movementMode: SpatialMovementMode,
    accentColor: Color,
    onPositionChanged: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.75f)
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(AuraSurfaceContainer)
            .border(1.5.dp, AuraOutline.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val radius = size.width / 2f
                        val center = Offset(radius, radius)
                        val touch = change.position

                        // Relative vector from center [-1.0f, +1.0f]
                        val relX = ((touch.x - center.x) / radius).coerceIn(-0.9f, 0.9f)
                        // Invert Y so up is positive Front, down is negative Back
                        val relY = (-(touch.y - center.y) / radius).coerceIn(-0.9f, 0.9f)

                        onPositionChanged(relX, relY)
                    }
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width / 2f

            // 1. Concentric distance rings
            listOf(0.33f, 0.66f, 0.95f).forEach { fraction ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.07f),
                    radius = radius * fraction,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // 2. Crosshair axes
            drawLine(
                color = Color.White.copy(alpha = 0.1f),
                start = Offset(center.x, 0f),
                end = Offset(center.x, size.height),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color.White.copy(alpha = 0.1f),
                start = Offset(0f, center.y),
                end = Offset(size.width, center.y),
                strokeWidth = 1.dp.toPx()
            )

            // 3. Listener Head Indicator at center
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = 8.dp.toPx(),
                center = center
            )
            drawCircle(
                color = accentColor,
                radius = 4.dp.toPx(),
                center = center
            )

            // 4. Source Beacon position
            val beaconX = center.x + positionX * radius
            val beaconY = center.y - positionY * radius // inverted for screen coords
            val beaconPos = Offset(beaconX, beaconY)

            // Pulsing glow rings around beacon
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(accentColor.copy(alpha = 0.45f), Color.Transparent),
                    center = beaconPos,
                    radius = 28.dp.toPx()
                ),
                radius = 28.dp.toPx(),
                center = beaconPos
            )

            // Inner solid beacon dot
            drawCircle(
                color = accentColor,
                radius = 8.dp.toPx(),
                center = beaconPos
            )
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = beaconPos
            )
        }

        // Cardinal Direction Labels
        Text(
            text = "FRONT",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.TopCenter)
        )
        Text(
            text = "BACK",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        Text(
            text = "LEFT",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.CenterStart)
        )
        Text(
            text = "RIGHT",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}
