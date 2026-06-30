package com.raachi.memory.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.raachi.memory.ui.theme.RaachiCornerRadius
import com.raachi.memory.ui.theme.RaachiMemoryTheme
import com.raachi.memory.ui.theme.RaachiSpacing

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = RaachiSpacing.touchTarget),
        enabled = enabled,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(RaachiCornerRadius.medium),
        contentPadding = PaddingValues(
            horizontal = RaachiSpacing.large,
            vertical = RaachiSpacing.medium
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(text = text)
    }
}

@Preview(showBackground = true)
@Composable
private fun PrimaryButtonPreview() {
    RaachiMemoryTheme {
        PrimaryButton(
            text = "Continue",
            onClick = {}
        )
    }
}
