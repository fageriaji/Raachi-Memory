package com.raachi.memory.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.raachi.memory.domain.model.LedgerKind

val LedgerMainColor: Color
    @Composable get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        Color(0xFFC061F2)
    } else {
        Color(0xFF7400B8)
    }

val LedgerOnMainColor: Color
    @Composable get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        Color(0xFF211127)
    } else {
        Color.White
    }

val LedgerMoneyColor = Color(0xFFF95738)
val LedgerItemColor = Color(0xFF43AA8B)

fun ledgerKindColor(kind: LedgerKind): Color = when (kind) {
    LedgerKind.MONEY -> LedgerMoneyColor
    LedgerKind.ITEM -> LedgerItemColor
}

fun ledgerKindSymbol(kind: LedgerKind): String = when (kind) {
    LedgerKind.MONEY -> "₹"
    LedgerKind.ITEM -> "📦"
}
