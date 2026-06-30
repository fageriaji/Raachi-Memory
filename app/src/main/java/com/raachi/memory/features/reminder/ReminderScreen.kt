package com.raachi.memory.features.reminder

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raachi.memory.ui.components.EmptyState
import com.raachi.memory.ui.components.SecondaryButton
import com.raachi.memory.ui.components.SectionHeader

@Composable
fun ReminderScreen(onNavigateBack: () -> Unit) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            SectionHeader(title = "Reminders")
            Spacer(modifier = Modifier.height(16.dp))

            // FIX: Added the 'title' parameter to EmptyState
            EmptyState(
                title = "Empty",
                message = "No reminders yet."
            )

            Spacer(modifier = Modifier.height(32.dp))
            SecondaryButton(text = "Back", onClick = onNavigateBack)
        }
    }
}