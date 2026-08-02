package com.raachi.memory.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = RaachiBrandNavy,
    onPrimary = RaachiSurface,
    primaryContainer = RaachiNavyContainer,
    onPrimaryContainer = RaachiOnNavyContainer,
    secondary = RaachiBrandOrange,
    onSecondary = RaachiOnOrange,
    secondaryContainer = RaachiOrangeContainer,
    onSecondaryContainer = RaachiOnOrangeContainer,
    tertiary = RaachiTeal,
    tertiaryContainer = RaachiTealContainer,
    background = RaachiBackground,
    onBackground = RaachiInk,
    surface = RaachiSurface,
    onSurface = RaachiInk,
    surfaceVariant = RaachiSurfaceVariant,
    onSurfaceVariant = RaachiMutedInk,
    outline = RaachiOutline,
    outlineVariant = RaachiOutlineVariant,
    error = RaachiError,
    errorContainer = RaachiErrorContainer,
)

private val DarkColorScheme = darkColorScheme(
    primary = RaachiNavyDark,
    onPrimary = RaachiBackgroundDark,
    primaryContainer = RaachiNavyContainerDark,
    onPrimaryContainer = RaachiOnNavyContainerDark,
    secondary = RaachiOrangeDark,
    onSecondary = RaachiOnOrangeDark,
    secondaryContainer = RaachiOrangeContainerDark,
    onSecondaryContainer = RaachiOnOrangeContainerDark,
    tertiary = RaachiTealDark,
    tertiaryContainer = RaachiTealContainerDark,
    background = RaachiBackgroundDark,
    onBackground = RaachiInkDark,
    surface = RaachiSurfaceDark,
    onSurface = RaachiInkDark,
    surfaceVariant = RaachiSurfaceVariantDark,
    onSurfaceVariant = RaachiMutedInkDark,
    outline = RaachiOutlineDark,
    outlineVariant = RaachiOutlineVariantDark,
    error = RaachiErrorDark,
    errorContainer = RaachiErrorContainerDark,
)

@Composable
fun RaachiMemoryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = RaachiTypography,
        shapes = RaachiShapes,
        content = content,
    )
}
