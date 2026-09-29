package com.example.aura.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.aura.domain.model.settings.AppearanceSettings

data class AuraColorScheme(
    val isDark: Boolean,
    val surface: Color,
    val surfaceDim: Color,
    val surfaceBright: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val outlineVariant: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val primaryAccent: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val error: Color,
    val onError: Color,
    val glassSurface: Color,
    val glassBorder: Color,
    val scrim: Color
)

fun createAuraDarkColors(accent: Color, isDeepBlack: Boolean = false) = AuraColorScheme(
    isDark = true,
    surface = if (isDeepBlack) Color(0xFF000000) else Color(0xFF10131A),
    surfaceDim = if (isDeepBlack) Color(0xFF000000) else Color(0xFF0B0E15),
    surfaceBright = Color(0xFF363941),
    surfaceContainerLowest = if (isDeepBlack) Color(0xFF000000) else Color(0xFF0B0E15),
    surfaceContainerLow = if (isDeepBlack) Color(0xFF050505) else Color(0xFF191B23),
    surfaceContainer = if (isDeepBlack) Color(0xFF0B0B0B) else Color(0xFF1D2027),
    surfaceContainerHigh = if (isDeepBlack) Color(0xFF141416) else Color(0xFF272A31),
    surfaceContainerHighest = if (isDeepBlack) Color(0xFF1F2128) else Color(0xFF32353C),
    onSurface = Color(0xFFE0E2EC),
    onSurfaceVariant = Color(0xFFC9C4D8),
    outline = Color(0xFF938EA1),
    outlineVariant = Color(0xFF484555),
    primary = accent,
    onPrimary = Color.White,
    primaryContainer = accent.copy(alpha = 0.35f),
    onPrimaryContainer = Color.White,
    primaryAccent = accent,
    secondary = accent,
    onSecondary = Color.White,
    secondaryContainer = accent.copy(alpha = 0.2f),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFFF6ADFF),
    onTertiary = Color(0xFF560068),
    tertiaryContainer = Color(0xFFDC52F9),
    onTertiaryContainer = Color(0xFF4B005B),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    glassSurface = if (isDeepBlack) Color(0xEE080808) else Color(0xAA161B26),
    glassBorder = Color(0x14FFFFFF),
    scrim = Color(0xCC080A0F)
)

fun createAuraLightColors(accent: Color) = AuraColorScheme(
    isDark = false,
    surface = Color(0xFFF7F8FC),
    surfaceDim = Color(0xFFE9ECF3),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF0F2F8),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFE7EAF3),
    surfaceContainerHighest = Color(0xFFDCE0EC),
    onSurface = Color(0xFF14161D),
    onSurfaceVariant = Color(0xFF505462),
    outline = Color(0xFF868B9A),
    outlineVariant = Color(0xFFCBD0DD),
    primary = accent,
    onPrimary = Color.White,
    primaryContainer = accent.copy(alpha = 0.18f),
    onPrimaryContainer = Color(0xFF10131A),
    primaryAccent = accent,
    secondary = accent,
    onSecondary = Color.White,
    secondaryContainer = accent.copy(alpha = 0.15f),
    onSecondaryContainer = Color(0xFF10131A),
    tertiary = Color(0xFF9C4146),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD9DA),
    onTertiaryContainer = Color(0xFF40000A),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    glassSurface = Color(0xEEF7F8FC),
    glassBorder = Color(0x18000000),
    scrim = Color(0x66000000)
)

val LocalAuraColors = compositionLocalOf { createAuraDarkColors(Color(0xFF7C5CFC)) }
val LocalAuraAccent = compositionLocalOf { Color(0xFF7C5CFC) }
val LocalAuraArtworkCornerRadius = compositionLocalOf { 24.dp }
val LocalAuraAppearance = compositionLocalOf { AppearanceSettings() }

fun auraDarkColorScheme(accent: Color, isDeepBlack: Boolean = false) = darkColorScheme(
    primary = accent,
    onPrimary = Color.White,
    primaryContainer = accent.copy(alpha = 0.35f),
    onPrimaryContainer = Color.White,
    secondary = accent,
    onSecondary = Color.White,
    secondaryContainer = accent.copy(alpha = 0.2f),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFFF6ADFF),
    onTertiary = Color(0xFF560068),
    tertiaryContainer = Color(0xFFDC52F9),
    onTertiaryContainer = Color(0xFF4B005B),
    background = if (isDeepBlack) Color(0xFF000000) else Color(0xFF0B0E15),
    onBackground = Color(0xFFE0E2EC),
    surface = if (isDeepBlack) Color(0xFF080808) else Color(0xFF10131A),
    onSurface = Color(0xFFE0E2EC),
    surfaceVariant = if (isDeepBlack) Color(0xFF141416) else Color(0xFF32353C),
    onSurfaceVariant = Color(0xFFC9C4D8),
    outline = Color(0xFF938EA1),
    outlineVariant = Color(0xFF484555),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

fun auraLightColorScheme(accent: Color) = lightColorScheme(
    primary = accent,
    onPrimary = Color.White,
    primaryContainer = accent.copy(alpha = 0.2f),
    onPrimaryContainer = Color(0xFF10131A),
    secondary = accent,
    onSecondary = Color.White,
    secondaryContainer = accent.copy(alpha = 0.15f),
    onSecondaryContainer = Color(0xFF10131A),
    tertiary = Color(0xFF9C4146),
    onTertiary = Color.White,
    background = Color(0xFFF7F8FC),
    onBackground = Color(0xFF14161D),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF14161D),
    surfaceVariant = Color(0xFFE7EAF3),
    onSurfaceVariant = Color(0xFF505462),
    outline = Color(0xFF868B9A),
    outlineVariant = Color(0xFFCBD0DD),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun AuraTheme(
    appearance: AppearanceSettings = AppearanceSettings(),
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (appearance.theme.lowercase()) {
        "light" -> false
        "dark" -> true
        else -> isSystemDark // "system" follows Android system configuration dynamically
    }

    val accentColor = remember(appearance.accentColorHex) {
        runCatching {
            Color(android.graphics.Color.parseColor(appearance.accentColorHex))
        }.getOrDefault(Color(0xFF7C5CFC))
    }

    val isDeepBlack = appearance.backgroundStyle == "Deep Black"
    val auraColors = if (isDark) {
        createAuraDarkColors(accentColor, isDeepBlack)
    } else {
        createAuraLightColors(accentColor)
    }

    val materialColorScheme = if (isDark) {
        auraDarkColorScheme(accentColor, isDeepBlack)
    } else {
        auraLightColorScheme(accentColor)
    }

    CompositionLocalProvider(
        LocalAuraColors provides auraColors,
        LocalAuraAccent provides accentColor,
        LocalAuraArtworkCornerRadius provides appearance.playerArtworkCornerRadiusDp.dp,
        LocalAuraAppearance provides appearance
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = Typography,
            content = content
        )
    }
}
