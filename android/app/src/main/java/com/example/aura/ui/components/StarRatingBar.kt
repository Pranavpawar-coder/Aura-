package com.example.aura.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.aura.theme.AuraPrimary

@Composable
fun StarRatingBar(
    rating: Int,
    onRatingChanged: ((Int) -> Unit)? = null,
    starSize: Dp = 18.dp,
    activeColor: Color = Color(0xFFFFB800), // Gold
    inactiveColor: Color = Color(0xFF666666),
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..5) {
            val isFilled = i <= rating
            Icon(
                imageVector = if (isFilled) Icons.Default.Star else Icons.Outlined.StarBorder,
                contentDescription = "$i stars",
                tint = if (isFilled) activeColor else inactiveColor,
                modifier = Modifier
                    .size(starSize)
                    .padding(horizontal = 1.dp)
                    .then(
                        if (onRatingChanged != null) {
                            Modifier.clickable {
                                // Tap same star to clear rating, else set to i
                                if (rating == i) {
                                    onRatingChanged(0)
                                } else {
                                    onRatingChanged(i)
                                }
                            }
                        } else Modifier
                    )
            )
        }
    }
}
