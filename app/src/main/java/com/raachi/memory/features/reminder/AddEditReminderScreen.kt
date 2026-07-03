package com.raachi.memory.features.reminder

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raachi.memory.R
import com.raachi.memory.domain.model.ReminderCategory
import com.raachi.memory.domain.model.ReminderType
import com.raachi.memory.ui.components.PrimaryButton
import com.raachi.memory.ui.components.RaachiSnackbarHost
import com.raachi.memory.ui.components.SecondaryButton
import com.raachi.memory.ui.components.SectionHeader
import com.raachi.memory.ui.components.SnackbarType
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditReminderScreen(
    viewModel: AddEditReminderViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        launch {
            viewModel.saveCompleted.collect {
                onNavigateBack()
            }
        }

        launch {
            viewModel.showError.collect { message ->
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        // Integrate the new Material 3 RaachiSnackbarHost
        snackbarHost = { RaachiSnackbarHost(hostState = snackbarHostState, type = SnackbarType.ERROR) }
    ) { padding ->
        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val timePickerState = rememberTimePickerState(
                initialHour = viewModel.selectedHour,
                initialMinute = viewModel.selectedMinute
            )

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(initialSelectedDateMillis = viewModel.selectedDateMillis)
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { viewModel.selectedDateMillis = it }
                            showDatePicker = false
                        }) { Text(stringResource(R.string.save)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SectionHeader(title = stringResource(if (viewModel.title.isEmpty()) R.string.add_new_reminder else R.string.edit_reminder))

                OutlinedTextField(
                    value = viewModel.title,
                    onValueChange = { viewModel.title = it },
                    label = { Text(stringResource(R.string.title)) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = viewModel.description,
                    onValueChange = { viewModel.description = it },
                    label = { Text(stringResource(R.string.description_optional)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(stringResource(R.string.category), style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ReminderCategory.entries.toTypedArray()) { cat ->
                        FilterChip(
                            selected = viewModel.category == cat,
                            onClick = { viewModel.category = cat },
                            label = { Text(stringResource(cat.stringResId())) }
                        )
                    }
                }

                Text(stringResource(R.string.repeat_type), style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ReminderType.entries.toTypedArray()) { type ->
                        FilterChip(
                            selected = viewModel.reminderType == type,
                            onClick = { viewModel.reminderType = type },
                            label = { Text(stringResource(type.stringResId())) }
                        )
                    }
                }

                if (viewModel.reminderType == ReminderType.INTERVAL) {
                    OutlinedTextField(
                        value = viewModel.intervalHours,
                        onValueChange = { viewModel.intervalHours = it },
                        label = { Text(stringResource(R.string.interval_hours)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text(stringResource(R.string.date), style = MaterialTheme.typography.labelLarge)
                val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy") }
                val dateText = remember(viewModel.selectedDateMillis) {
                    Instant.ofEpochMilli(viewModel.selectedDateMillis).atZone(ZoneId.systemDefault()).toLocalDate().format(dateFormatter)
                }
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = dateText)
                }

                Text(stringResource(R.string.time), style = MaterialTheme.typography.labelLarge)
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }

                Spacer(modifier = Modifier.weight(1f))

                PrimaryButton(
                    text = stringResource(R.string.save),
                    enabled = viewModel.title.isNotBlank(),
                    onClick = {
                        viewModel.selectedHour = timePickerState.hour
                        viewModel.selectedMinute = timePickerState.minute
                        viewModel.saveReminder()
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                SecondaryButton(
                    text = stringResource(R.string.cancel),
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun ReminderCategory.stringResId() = when(this) {
    ReminderCategory.WATER -> R.string.cat_water
    ReminderCategory.MEDICINE -> R.string.cat_medicine
    ReminderCategory.BREAKFAST -> R.string.cat_breakfast
    ReminderCategory.LUNCH -> R.string.cat_lunch
    ReminderCategory.DINNER -> R.string.cat_dinner
    ReminderCategory.EXERCISE -> R.string.cat_exercise
    ReminderCategory.SLEEP -> R.string.cat_sleep
    ReminderCategory.CUSTOM -> R.string.cat_custom
}

private fun ReminderType.stringResId() = when(this) {
    ReminderType.ONE_TIME -> R.string.repeat_one_time
    ReminderType.DAILY -> R.string.repeat_daily
    ReminderType.WEEKLY -> R.string.repeat_weekly
    ReminderType.INTERVAL -> R.string.repeat_interval
}