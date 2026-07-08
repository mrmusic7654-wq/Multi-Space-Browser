package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

enum class AppTheme { LIGHT, DARK, OCHRE }

private val DefaultDarkColorScheme = darkColorScheme(
    primary = SpacePrimary,
    secondary = SpaceSecondary,
    background = SpaceDarkBackground,
    surface = SpaceSurface,
    onPrimary = SpaceDarkBackground,
    onSecondary = SpaceDarkBackground,
    onBackground = SpaceTextPrimary,
    onSurface = SpaceTextPrimary,
    surfaceVariant = SpaceSurface,
    onSurfaceVariant = SpaceTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = LightSurface,
    onSecondary = LightSurface,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurface,
    onSurfaceVariant = LightTextSecondary
)

private val OchreColorScheme = darkColorScheme(
    primary = OchrePrimary,
    secondary = OchreSecondary,
    background = OchreDarkBackground,
    surface = OchreSurface,
    onPrimary = OchreDarkBackground,
    onSecondary = OchreDarkBackground,
    onBackground = OchreTextPrimary,
    onSurface = OchreTextPrimary,
    surfaceVariant = OchreSurface,
    onSurfaceVariant = OchreTextSecondary
)

@Composable
fun MyApplicationTheme(
    themeSelection: AppTheme = AppTheme.OCHRE,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeSelection) {
        AppTheme.LIGHT -> LightColorScheme
        AppTheme.DARK -> DefaultDarkColorScheme
        AppTheme.OCHRE -> OchreColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
