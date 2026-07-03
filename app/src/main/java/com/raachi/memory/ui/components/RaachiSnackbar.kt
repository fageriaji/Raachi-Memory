package com.raachi.memory.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class SnackbarType {
    SUCCESS, ERROR, WARNING, INFO
}

@Composable
fun RaachiSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    type: SnackbarType = SnackbarType.INFO
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
    ) { data ->
        RaachiSnackbar(data, type)
    }
}

@Composable
fun RaachiSnackbar(
    snackbarData: SnackbarData,
    type: SnackbarType
) {
    val containerColor = when (type) {
        SnackbarType.SUCCESS -> MaterialTheme.colorScheme.primaryContainer
        SnackbarType.ERROR -> MaterialTheme.colorScheme.errorContainer
        SnackbarType.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
        SnackbarType.INFO -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when (type) {
        SnackbarType.SUCCESS -> MaterialTheme.colorScheme.onPrimaryContainer
        SnackbarType.ERROR -> MaterialTheme.colorScheme.onErrorContainer
        SnackbarType.WARNING -> MaterialTheme.colorScheme.onTertiaryContainer
        SnackbarType.INFO -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Snackbar(
        snackbarData = snackbarData,
        containerColor = containerColor,
        contentColor = contentColor,
        actionColor = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(12.dp)
    )
}