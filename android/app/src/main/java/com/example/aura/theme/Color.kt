package com.example.aura.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Default Fallback Color Values (Static)
val AuraPrimaryAccentDefault = Color(0xFF7C5CFC)

// Dynamic Color Tokens routed directly through LocalAuraColors
// Every Composable reading these tokens will instantly react to System/Light/Dark Theme and Accent Color changes!

val AuraDeepBlack: Color
    @Composable get() = LocalAuraColors.current.deepBlack
val AuraSoftBlack: Color
    @Composable get() = LocalAuraColors.current.softBlack
val AuraSurfaceBlack: Color
    @Composable get() = LocalAuraColors.current.surfaceBlack
val AuraElevated1: Color
    @Composable get() = LocalAuraColors.current.surfaceContainerHigh
val AuraElevated2: Color
    @Composable get() = LocalAuraColors.current.surfaceContainerHighest
val AuraElevated3: Color
    @Composable get() = LocalAuraColors.current.surfaceBright

val AuraTextPrimary: Color
    @Composable get() = LocalAuraColors.current.textPrimary
val AuraTextSecondary: Color
    @Composable get() = LocalAuraColors.current.textSecondary
val AuraTextTertiary: Color
    @Composable get() = LocalAuraColors.current.textTertiary
val AuraTextDisabled: Color
    @Composable get() = LocalAuraColors.current.textDisabled

val AuraGlassSurfaceDefault: Color
    @Composable get() = LocalAuraColors.current.glassSurface
val AuraGlassBorderDefault: Color
    @Composable get() = LocalAuraColors.current.glassBorder
val AuraGlassHighlightDefault: Color
    @Composable get() = LocalAuraColors.current.glassHighlight

// Dynamic Color Tokens routed directly through LocalAuraColors
// Every Composable reading these tokens will instantly react to System/Light/Dark Theme and Accent Color changes!

val AuraSurface: Color
    @Composable get() = LocalAuraColors.current.surface

val AuraSurfaceDim: Color
    @Composable get() = LocalAuraColors.current.surfaceDim

val AuraSurfaceBright: Color
    @Composable get() = LocalAuraColors.current.surfaceBright

val AuraSurfaceContainerLowest: Color
    @Composable get() = LocalAuraColors.current.surfaceContainerLowest

val AuraSurfaceContainerLow: Color
    @Composable get() = LocalAuraColors.current.surfaceContainerLow

val AuraSurfaceContainer: Color
    @Composable get() = LocalAuraColors.current.surfaceContainer

val AuraSurfaceContainerHigh: Color
    @Composable get() = LocalAuraColors.current.surfaceContainerHigh

val AuraSurfaceContainerHighest: Color
    @Composable get() = LocalAuraColors.current.surfaceContainerHighest

val AuraOnSurface: Color
    @Composable get() = LocalAuraColors.current.onSurface

val AuraOnSurfaceVariant: Color
    @Composable get() = LocalAuraColors.current.onSurfaceVariant

val AuraOutline: Color
    @Composable get() = LocalAuraColors.current.outline

val AuraOutlineVariant: Color
    @Composable get() = LocalAuraColors.current.outlineVariant

val AuraPrimary: Color
    @Composable get() = LocalAuraColors.current.primary

val AuraOnPrimary: Color
    @Composable get() = LocalAuraColors.current.onPrimary

val AuraPrimaryContainer: Color
    @Composable get() = LocalAuraColors.current.primaryContainer

val AuraOnPrimaryContainer: Color
    @Composable get() = LocalAuraColors.current.onPrimaryContainer

val AuraPrimaryAccent: Color
    @Composable get() = LocalAuraColors.current.primaryAccent

val AuraSecondary: Color
    @Composable get() = LocalAuraColors.current.secondary

val AuraOnSecondary: Color
    @Composable get() = LocalAuraColors.current.onSecondary

val AuraSecondaryContainer: Color
    @Composable get() = LocalAuraColors.current.secondaryContainer

val AuraOnSecondaryContainer: Color
    @Composable get() = LocalAuraColors.current.onSecondaryContainer

val AuraTertiary: Color
    @Composable get() = LocalAuraColors.current.tertiary

val AuraOnTertiary: Color
    @Composable get() = LocalAuraColors.current.onTertiary

val AuraTertiaryContainer: Color
    @Composable get() = LocalAuraColors.current.tertiaryContainer

val AuraOnTertiaryContainer: Color
    @Composable get() = LocalAuraColors.current.onTertiaryContainer

val AuraError: Color
    @Composable get() = LocalAuraColors.current.error

val AuraOnError: Color
    @Composable get() = LocalAuraColors.current.onError

val AuraGlassSurface: Color
    @Composable get() = LocalAuraColors.current.glassSurface

val AuraGlassBorder: Color
    @Composable get() = LocalAuraColors.current.glassBorder

val AuraScrim: Color
    @Composable get() = LocalAuraColors.current.scrim

