package com.raachi.memory.feature.reminder

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.pluralStringResource
import com.raachi.memory.R
import com.raachi.memory.core.designsystem.theme.RaachiBrandNavy
import com.raachi.memory.core.ui.AppSection
import com.raachi.memory.core.ui.RaachiBottomBar
import com.raachi.memory.core.ui.RaachiSectionTopBar
import com.raachi.memory.core.ui.NotificationPermissionControls
import com.raachi.memory.core.ui.reminderCategoryAccent
import com.raachi.memory.core.ui.reminderCategoryEmoji
import com.raachi.memory.core.ui.raachiSuccessColor
import com.raachi.memory.core.ui.raachiSuccessContainerColor
import com.raachi.memory.domain.model.Reminder
import com.raachi.memory.domain.model.ReminderRepeatType
import com.raachi.memory.domain.model.ReminderStatus
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListScreen(
    onBack: () -> Unit,
    onOpenSection: (AppSection) -> Unit,
    onAddReminder: () -> Unit,
    onEditReminder: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReminderListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var searching by remember { mutableStateOf(false) }
    var reminderPendingDelete by remember { mutableStateOf<Reminder?>(null) }

    reminderPendingDelete?.let { reminder ->
        AlertDialog(
            onDismissRequest = { reminderPendingDelete = null },
            title = { Text(stringResource(R.string.delete_reminder)) },
            text = { Text(stringResource(R.string.delete_reminder_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete(reminder.id)
                        reminderPendingDelete = null
                    },
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { reminderPendingDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            RaachiSectionTopBar(
                title = stringResource(R.string.nav_reminders),
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = {
                            searching = !searching
                            if (!searching) viewModel.updateSearch("")
                        },
                    ) {
                        Icon(Icons.Outlined.Search, contentDescription = stringResource(R.string.search_reminders))
                    }
                },
            )
        },
        bottomBar = { RaachiBottomBar(AppSection.REMINDERS, onOpenSection) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddReminder,
                containerColor = RaachiBrandNavy,
                contentColor = Color.White,
            ) {
                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.add_reminder))
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (searching) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::updateSearch,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    placeholder = { Text(stringResource(R.string.search_reminders)) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                viewModel.updateSearch("")
                                searching = false
                            },
                        ) {
                            Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.close))
                        }
                    },
                    singleLine = true,
                )
            }
            ReminderTabs(
                selected = state.selectedTab,
                onSelected = viewModel::selectTab,
            )
            NotificationPermissionControls(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.reminders.isEmpty() -> ReminderEmptyState(state.selectedTab)
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 104.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(state.reminders, key = Reminder::id) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onEdit = { onEditReminder(reminder.id) },
                            onSnooze = { viewModel.snooze(reminder.id) },
                            onDone = { viewModel.complete(reminder.id) },
                            onSkip = { viewModel.skip(reminder.id) },
                            onDelete = { reminderPendingDelete = reminder },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderTabs(selected: ReminderTab, onSelected: (ReminderTab) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ReminderTab.entries.forEach { tab ->
                val isSelected = tab == selected
                Surface(
                    onClick = { onSelected(tab) },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(tab.labelRes()),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderEmptyState(tab: ReminderTab) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.reminder_header_emoji),
                color = Color.Unspecified,
                fontSize = 44.sp,
            )
            Text(
                text = stringResource(tab.emptyRes()),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun ReminderCard(
    reminder: Reminder,
    onEdit: () -> Unit,
    onSnooze: () -> Unit,
    onDone: () -> Unit,
    onSkip: () -> Unit,
    onDelete: () -> Unit,
) {
    val categoryAccent = reminderCategoryAccent(reminder.category)
    val isCompleted = reminder.status in setOf(ReminderStatus.COMPLETED, ReminderStatus.SKIPPED)
    val cardAccent = if (isCompleted) raachiSuccessColor() else categoryAccent

    Surface(
        onClick = onEdit,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, cardAccent.copy(alpha = 0.2f)),
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(cardAccent),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Surface(
                        modifier = Modifier.size(54.dp),
                        color = categoryAccent.copy(alpha = 0.12f),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = reminderCategoryEmoji(reminder.category),
                                color = Color.Unspecified,
                                fontSize = 28.sp,
                            )
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = reminder.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = reminder.nextTriggerAt?.atZone(ZoneId.systemDefault())?.format(TIME_FORMAT).orEmpty(),
                            color = cardAccent,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = reminder.scheduleLabel(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(44.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.delete_reminder),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                reminder.description?.takeIf(String::isNotBlank)?.let { description ->
                    Text(
                        text = description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (reminder.status in setOf(ReminderStatus.ACTIVE, ReminderStatus.SNOOZED)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FilledTonalButton(
                            onClick = onSnooze,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = categoryAccent.copy(alpha = 0.14f),
                                contentColor = categoryAccent,
                            ),
                        ) {
                            Icon(Icons.Outlined.Snooze, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.snooze), fontWeight = FontWeight.Bold)
                        }
                        ReminderActionButton(
                            icon = Icons.Outlined.CheckCircle,
                            contentDescription = stringResource(R.string.done),
                            containerColor = raachiSuccessContainerColor(),
                            contentColor = raachiSuccessColor(),
                            onClick = onDone,
                        )
                        FilledTonalButton(
                            onClick = onSkip,
                            modifier = Modifier.height(48.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        ) {
                            Icon(Icons.Outlined.FastForward, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.skip))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderActionButton(
    icon: ImageVector,
    contentDescription: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.medium,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
            )
        }
    }
}

@Composable
private fun Reminder.scheduleLabel(): String = when (repeatType) {
    ReminderRepeatType.ONE_TIME -> stringResource(R.string.schedule_one_time)
    ReminderRepeatType.DAILY -> stringResource(R.string.schedule_daily)
    ReminderRepeatType.WEEKLY -> stringResource(R.string.schedule_weekly)
    ReminderRepeatType.INTERVAL -> {
        val hours = intervalHours ?: 1
        pluralStringResource(R.plurals.schedule_interval, hours, hours)
    }
}

private fun ReminderTab.labelRes(): Int = when (this) {
    ReminderTab.ACTIVE -> R.string.active
    ReminderTab.SNOOZED -> R.string.snoozed
    ReminderTab.COMPLETED -> R.string.completed
}

private fun ReminderTab.emptyRes(): Int = when (this) {
    ReminderTab.ACTIVE -> R.string.no_active_reminders
    ReminderTab.SNOOZED -> R.string.no_snoozed_reminders
    ReminderTab.COMPLETED -> R.string.no_completed_reminders
}

private val TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a")
