package com.raachi.memory.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val RaachiIndigo = Color(0xFF4F46E5)
val RaachiIndigoDark = Color(0xFF818CF8)
val RaachiEmerald = Color(0xFF10B981)
val RaachiEmeraldDark = Color(0xFF34D399)
val RaachiAmber = Color(0xFFF59E0B)
val RaachiAmberDark = Color(0xFFFBBF24)
val RaachiSoftRed = Color(0xFFEF4444)
val RaachiSoftRedDark = Color(0xFFF87171)

val RaachiWhite = Color(0xFFFFFFFF)
val RaachiCloud = Color(0xFFF8FAFC)
val RaachiMist = Color(0xFFE2E8F0)
val RaachiSlate = Color(0xFF475569)
val RaachiCharcoal = Color(0xFF111827)
val RaachiCharcoalSoft = Color(0xFF1F2937)

val RaachiLightColorScheme = lightColorScheme(
    primary = RaachiIndigo,
    onPrimary = RaachiWhite,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF312E81),
    secondary = RaachiEmerald,
    onSecondary = RaachiWhite,
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF064E3B),
    tertiary = RaachiAmber,
    onTertiary = RaachiCharcoal,
    tertiaryContainer = Color(0xFFFEF3C7),
    onTertiaryContainer = Color(0xFF78350F),
    error = RaachiSoftRed,
    onError = RaachiWhite,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
    background = RaachiCloud,
    onBackground = RaachiCharcoal,
    surface = RaachiWhite,
    onSurface = RaachiCharcoal,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = RaachiSlate,
    outline = Color(0xFFCBD5E1),
    outlineVariant = RaachiMist
)

val RaachiDarkColorScheme = darkColorScheme(
    primary = RaachiIndigoDark,
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF3730A3),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = RaachiEmeraldDark,
    onSecondary = Color(0xFF022C22),
    secondaryContainer = Color(0xFF047857),
    onSecondaryContainer = Color(0xFFD1FAE5),
    tertiary = RaachiAmberDark,
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = Color(0xFF92400E),
    onTertiaryContainer = Color(0xFFFEF3C7),
    error = RaachiSoftRedDark,
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF991B1B),
    onErrorContainer = Color(0xFFFEE2E2),
    background = RaachiCharcoal,
    onBackground = Color(0xFFF8FAFC),
    surface = RaachiCharcoalSoft,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFF334155)
)
