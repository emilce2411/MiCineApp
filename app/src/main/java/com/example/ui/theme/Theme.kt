package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CineDarkColorScheme = darkColorScheme(
    primary = CinemaGold,
    onPrimary = Color(0xFF1E1400),
    primaryContainer = Color(0xFF3E2D00),
    onPrimaryContainer = CinemaGoldLight,
    secondary = CinemaPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF39116B),
    onSecondaryContainer = Color(0xFFE9D5FF),
    tertiary = CinemaRed,
    onTertiary = Color.White,
    background = DeepDarkBackground,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder
)

private val CineLightColorScheme = lightColorScheme(
    primary = CinemaGoldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFECC4),
    onPrimaryContainer = Color(0xFF261900),
    secondary = CinemaPurple,
    onSecondary = Color.White,
    background = Color(0xFF0F131D),
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent cinematic look
    content: @Composable () -> Unit
) {
    val colorScheme = CineDarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
