package com.example.aura.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Centralized AURA Typography System (Section 7 & 23)
object AuraTypography {
    val Display = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.5).sp
    )

    val Title = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.2).sp
    )

    val Subtitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )

    val Body = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    )

    val Caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.3.sp
    )

    val Overline = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 2.2.sp
    )
}

// Material 3 Typography mapping
val Typography = Typography(
    displayLarge = AuraTypography.Display,
    titleLarge = AuraTypography.Title,
    titleMedium = AuraTypography.Subtitle,
    bodyLarge = AuraTypography.Body.copy(fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = AuraTypography.Body,
    bodySmall = AuraTypography.Caption,
    labelLarge = AuraTypography.Subtitle.copy(fontSize = 13.sp),
    labelMedium = AuraTypography.Caption,
    labelSmall = AuraTypography.Overline
)

