package com.example.cinestream.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CinemaRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF5C0006),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = CinemaGold,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF423200),
    onSecondaryContainer = Color(0xFFFFDF9E),
    tertiary = CinemaRedBright,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC7C5D0)
)

private val LightColorScheme = lightColorScheme(
    primary = CinemaRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFF795900),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDF9E),
    onSecondaryContainer = Color(0xFF261A00),
    tertiary = CinemaRedBright,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF46464F)
)

@Composable
fun CineStreamTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
