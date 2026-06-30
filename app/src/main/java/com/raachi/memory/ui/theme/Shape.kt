package com.raachi.memory.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object RaachiCornerRadius {
    val extraSmall = 8.dp
    val small = 12.dp
    val medium = 16.dp
    val card = 24.dp
    val full = 999.dp
}

val RaachiShapes = Shapes(
    extraSmall = RoundedCornerShape(RaachiCornerRadius.extraSmall),
    small = RoundedCornerShape(RaachiCornerRadius.small),
    medium = RoundedCornerShape(RaachiCornerRadius.medium),
    large = RoundedCornerShape(RaachiCornerRadius.card),
    extraLarge = RoundedCornerShape(RaachiCornerRadius.card)
)
