package com.raachi.memory.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.raachi.memory.ui.theme.RaachiMemoryTheme
import com.raachi.memory.ui.theme.RaachiShapes
import com.raachi.memory.ui.theme.RaachiSpacing

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(RaachiSpacing.card),
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RaachiShapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = RaachiSpacing.extraSmall)
    ) {
        Column(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppCardPreview() {
    RaachiMemoryTheme {
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Up next",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Drink water at 10:00 AM",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
