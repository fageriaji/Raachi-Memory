package com.raachi.memory.feature.reminder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raachi.memory.R
import com.raachi.memory.core.ui.NotificationPermissionControls
import com.raachi.memory.domain.model.ReminderCategory
import com.raachi.memory.domain.model.ReminderInput
import com.raachi.memory.domain.model.ReminderRepeatType
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderEditorScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReminderEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }
    LaunchedEffect(state.saved, state.deleted) {
        if (state.saved || state.deleted) onSaved()
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.delete_reminder)) },
            text = { Text(stringResource(R.string.delete_reminder_confirmation)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.delete()
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(if (state.input.id == 0L) R.string.new_reminder else R.string.edit_reminder),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (state.input.id != 0L) {
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.delete_reminder))
                        }
                    }
                    TextButton(onClick = viewModel::save, enabled = !state.isSaving) {
                        if (state.isSaving) CircularProgressIndicator(modifier = Modifier.width(20.dp))
                        else Text(stringResource(R.string.save_changes))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
    ) { innerPadding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) { CircularProgressIndicator() }
        } else {
            ReminderEditorContent(
                input = state.input,
                titleError = state.validation.titleError,
                scheduleError = state.validation.scheduleError,
                intervalError = state.validation.intervalError,
                onUpdate = viewModel::updateInput,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun ReminderEditorContent(
    input: ReminderInput,
    titleError: Boolean,
    scheduleError: Boolean,
    intervalError: Boolean,
    onUpdate: (ReminderInput.() -> ReminderInput) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        NotificationPermissionControls()

        OutlinedTextField(
            value = input.title,
            onValueChange = { value -> onUpdate { copy(title = value) } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.reminder_title)) },
            isError = titleError,
            supportingText = if (titleError) ({ Text(stringResource(R.string.field_required)) }) else null,
            singleLine = true,
        )

        FormSection(stringResource(R.string.category)) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ReminderCategory.entries.forEach { category ->
                    FilterChip(
                        selected = input.category == category,
                        onClick = { onUpdate { copy(category = category) } },
                        label = { Text(stringResource(category.labelRes())) },
                    )
                }
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.large,
            tonalElevation = 1.dp,
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(stringResource(R.string.frequency), style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ReminderRepeatType.entries.forEach { repeat ->
                        FilterChip(
                            selected = input.repeatType == repeat,
                            onClick = { onUpdate { copy(repeatType = repeat) } },
                            label = { Text(stringResource(repeat.labelRes())) },
                        )
                    }
                }
                if (input.repeatType == ReminderRepeatType.INTERVAL) {
                    OutlinedTextField(
                        value = input.intervalHours.toString(),
                        onValueChange = { value ->
                            value.filter(Char::isDigit).take(2).toIntOrNull()?.let { hours ->
                                onUpdate { copy(intervalHours = hours) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.repeat_every_hours)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = intervalError,
                        supportingText = if (intervalError) ({ Text(stringResource(R.string.interval_hours_error)) }) else null,
                        singleLine = true,
                    )
                }
            }
        }

        ScheduleRow(
            icon = Icons.Outlined.CalendarMonth,
            title = stringResource(R.string.start_date),
            value = input.startDate.format(DATE_FORMAT),
            onClick = {
                DatePickerDialog(
                    context,
                    { _, year, month, day -> onUpdate { copy(startDate = LocalDate.of(year, month + 1, day)) } },
                    input.startDate.year,
                    input.startDate.monthValue - 1,
                    input.startDate.dayOfMonth,
                ).apply {
                    datePicker.minDate = LocalDate.now()
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                }.show()
            },
        )
        ScheduleRow(
            icon = Icons.Outlined.Schedule,
            title = stringResource(R.string.start_time),
            value = input.startTime.format(TIME_FORMAT),
            onClick = {
                TimePickerDialog(
                    context,
                    { _, hour, minute -> onUpdate { copy(startTime = java.time.LocalTime.of(hour, minute)) } },
                    input.startTime.hour,
                    input.startTime.minute,
                    false,
                ).show()
            },
        )
        if (scheduleError) {
            Text(
                text = stringResource(R.string.future_schedule_error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        OutlinedTextField(
            value = input.description,
            onValueChange = { value -> onUpdate { copy(description = value) } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.reminder_notes)) },
            minLines = 3,
        )
        ToggleRow(
            title = stringResource(R.string.reminder_sounds),
            checked = input.soundEnabled,
            onChecked = { enabled -> onUpdate { copy(soundEnabled = enabled) } },
        )
        ToggleRow(
            title = stringResource(R.string.vibration),
            checked = input.vibrationEnabled,
            onChecked = { enabled -> onUpdate { copy(vibrationEnabled = enabled) } },
        )
    }
}

@Composable
private fun FormSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@Composable
private fun ScheduleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ToggleRow(title: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Switch(checked = checked, onCheckedChange = onChecked)
        }
    }
}

private fun ReminderCategory.labelRes(): Int = when (this) {
    ReminderCategory.WATER -> R.string.category_water
    ReminderCategory.MEDICINE -> R.string.category_medicine
    ReminderCategory.BREAKFAST -> R.string.category_breakfast
    ReminderCategory.LUNCH -> R.string.category_lunch
    ReminderCategory.DINNER -> R.string.category_dinner
    ReminderCategory.EXERCISE -> R.string.category_exercise
    ReminderCategory.SLEEP -> R.string.category_sleep
    ReminderCategory.CUSTOM -> R.string.category_custom
}

private fun ReminderRepeatType.labelRes(): Int = when (this) {
    ReminderRepeatType.ONE_TIME -> R.string.one_time
    ReminderRepeatType.DAILY -> R.string.daily
    ReminderRepeatType.WEEKLY -> R.string.weekly
    ReminderRepeatType.INTERVAL -> R.string.interval
}

private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-uuuu")
private val TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a")
