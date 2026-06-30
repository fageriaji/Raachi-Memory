package com.raachi.memory.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun RaachiMemoryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) RaachiDarkColorScheme else RaachiLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = RaachiShapes,
        typography = Typography,
        content = content
    )
}
