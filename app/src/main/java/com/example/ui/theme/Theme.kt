package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class AppPalette(
    val background: Color,
    val card: Color,
    val cardSecondary: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val increase: Color,
    val decrease: Color,
    val increaseBg: Color,
    val decreaseBg: Color,
    val isDark: Boolean
)

val DarkPalette = AppPalette(
    background = DarkBackground,
    card = CardBackground,
    cardSecondary = CardSecondary,
    border = CardBorder,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    accent = GoldAccent,
    increase = RateIncrease,
    decrease = RateDecrease,
    increaseBg = RateIncreaseBg,
    decreaseBg = RateDecreaseBg,
    isDark = true
)

val LightPalette = AppPalette(
    background = Color(0xFFF7F6F3),
    card = Color(0xFFFFFFFF),
    cardSecondary = Color(0xFFF0EEEA),
    border = Color(0xFFE2DED6),
    textPrimary = Color(0xFF1A1C1E),
    textSecondary = Color(0xFF6B7078),
    accent = Color(0xFFB8860B),
    increase = Color(0xFF1B9E5A),
    decrease = Color(0xFFD64545),
    increaseBg = Color(0x1F1B9E5A),
    decreaseBg = Color(0x1FD64545),
    isDark = false
)

val LocalAppPalette = staticCompositionLocalOf { DarkPalette }

private val DarkColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = DarkBackground,
    primaryContainer = CardSecondary,
    onPrimaryContainer = TextPrimary,
    secondary = GoldAccent,
    onSecondary = DarkBackground,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    surfaceVariant = CardSecondary,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = LightPalette.accent,
    onPrimary = Color.White,
    primaryContainer = LightPalette.cardSecondary,
    onPrimaryContainer = LightPalette.textPrimary,
    secondary = LightPalette.accent,
    onSecondary = Color.White,
    background = LightPalette.background,
    onBackground = LightPalette.textPrimary,
    surface = LightPalette.card,
    onSurface = LightPalette.textPrimary,
    surfaceVariant = LightPalette.cardSecondary,
    onSurfaceVariant = LightPalette.textSecondary,
    outline = LightPalette.border
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) DarkPalette else LightPalette
    CompositionLocalProvider(LocalAppPalette provides palette) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
