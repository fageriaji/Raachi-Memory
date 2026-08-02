package com.raachi.memory.core.ui

import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.raachi.memory.R

@Composable
fun RaachiWordmark(
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge,
) {
    val wordmark = buildAnnotatedString {
        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            append("Raachi")
        }
        append(" ")
        withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
            append("Memory")
        }
    }
    Text(text = wordmark, modifier = modifier, style = style)
}

@Composable
fun RaachiMark(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.ic_raachi_logo),
        contentDescription = null,
        modifier = modifier,
    )
}

@Composable
fun raachiSuccessColor(): Color = if (isRaachiDarkTheme()) {
    Color(0xFF7CDAA5)
} else {
    Color(0xFF168653)
}

@Composable
fun raachiSuccessContainerColor(): Color = if (isRaachiDarkTheme()) {
    Color(0xFF0C5532)
} else {
    Color(0xFFD9F4E6)
}

@Composable
private fun isRaachiDarkTheme(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f
