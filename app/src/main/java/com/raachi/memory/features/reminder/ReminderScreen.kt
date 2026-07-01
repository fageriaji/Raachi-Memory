package com.raachi.memory.features.reminder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raachi.memory.R
import com.raachi.memory.domain.model.Reminder
import com.raachi.memory.domain.model.ReminderStatus
import com.raachi.memory.domain.model.ReminderType
import com.raachi.memory.domain.util.DateTimeUtils
import com.raachi.memory.ui.components.EmptyState
import com.raachi.memory.ui.components.SectionHeader

@Composable
fun ReminderScreen(
    viewModel: ReminderViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Int) -> Unit
) {
    val reminders by viewModel.reminders.collectAsState()

    var reminderToDelete by remember { mutableStateOf<Reminder?>(null) }

    if (reminderToDelete != null) {
        AlertDialog(
            onDismissRequest = { reminderToDelete = null },
            title = { Text(stringResource(R.string.delete_reminder)) },
            text = { Text(stringResource(R.string.delete_confirmation)) },
            confirmButton = {
                TextButton(onClick = {
                    reminderToDelete?.let { viewModel.deleteReminder(it) }
                    reminderToDelete = null
                }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { reminderToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_new_reminder))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            SectionHeader(title = stringResource(R.string.reminders))
            Spacer(modifier = Modifier.height(16.dp))

            if (reminders.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.all_caught_up),
                    message = stringResource(R.string.no_reminders_scheduled)
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(reminders, key = { it.id }) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onClick = { onNavigateToEdit(reminder.id) },
                            onDelete = { reminderToDelete = reminder }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderCard(
    reminder: Reminder,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = reminder.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(R.string.category_format, reminder.category.name.lowercase().replaceFirstChar { it.uppercase() }),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = when (reminder.reminderType) {
                        ReminderType.ONE_TIME -> stringResource(R.string.repeat_one_time)
                        ReminderType.DAILY -> stringResource(R.string.repeat_daily)
                        ReminderType.WEEKLY -> stringResource(R.string.repeat_weekly)
                        ReminderType.INTERVAL -> stringResource(R.string.repeat_interval)
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                val isActive = reminder.status == ReminderStatus.ACTIVE
                Text(
                    text = when (reminder.status) {
                        ReminderStatus.ACTIVE -> stringResource(R.string.status_active)
                        ReminderStatus.COMPLETED -> stringResource(R.string.status_completed)
                        ReminderStatus.SKIPPED -> stringResource(R.string.status_skipped)
                        ReminderStatus.ARCHIVED -> stringResource(R.string.status_archived)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isActive) {
                    Text(
                        text = DateTimeUtils.formatTime(reminder.nextTrigger),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}