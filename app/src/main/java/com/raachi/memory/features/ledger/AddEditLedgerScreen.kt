package com.raachi.memory.features.ledger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import com.raachi.memory.R
import com.raachi.memory.domain.model.ItemType
import com.raachi.memory.domain.util.DateTimeUtils
import com.raachi.memory.ui.components.PrimaryButton
import com.raachi.memory.ui.components.RaachiSnackbarHost
import com.raachi.memory.ui.components.SecondaryButton
import com.raachi.memory.ui.components.SectionHeader
import com.raachi.memory.ui.components.SnackbarType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun AddEditLedgerScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditLedgerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var showBorrowDatePicker by remember { mutableStateOf(false) }
    var showDueDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        launch {
            viewModel.messages.collect { messageResId ->
                snackbarHostState.showSnackbar(
                    context.getString(messageResId)
                )
            }
        }

        launch {
            viewModel.saveCompleted.collect {
                onNavigateBack()
            }
        }
    }

    Scaffold(
        snackbarHost = {
            RaachiSnackbarHost(
                hostState = snackbarHostState,
                type = SnackbarType.ERROR
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (showBorrowDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = uiState.borrowDateMillis
            )

            DatePickerDialog(
                onDismissRequest = {
                    showBorrowDatePicker = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let(
                                viewModel::updateBorrowDate
                            )
                            showBorrowDatePicker = false
                        }
                    ) {
                        Text(stringResource(R.string.save))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showBorrowDatePicker = false
                        }
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showDueDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = uiState.dueDateMillis
            )

            DatePickerDialog(
                onDismissRequest = {
                    showDueDatePicker = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let(
                                viewModel::updateDueDate
                            )
                            showDueDatePicker = false
                        }
                    ) {
                        Text(stringResource(R.string.save))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDueDatePicker = false
                        }
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionHeader(
                title = stringResource(R.string.ledger_entry_title)
            )

            OutlinedTextField(
                value = uiState.personName,
                onValueChange = viewModel::updatePersonName,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(R.string.ledger_person_name))
                },
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.mobileNumber,
                onValueChange = viewModel::updateMobileNumber,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(R.string.ledger_mobile_number_optional))
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                ),
                singleLine = true
            )

            Text(
                text = stringResource(R.string.ledger_item_type),
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ItemType.entries.forEach { itemType ->
                    FilterChip(
                        selected = uiState.itemType == itemType,
                        onClick = {
                            viewModel.updateItemType(itemType)
                        },
                        label = {
                            Text(stringResource(itemType.labelResId()))
                        }
                    )
                }
            }

            OutlinedTextField(
                value = uiState.itemName,
                onValueChange = viewModel::updateItemName,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(R.string.ledger_item_name))
                },
                singleLine = true
            )

            if (uiState.itemType == ItemType.MONEY) {
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = viewModel::updateAmount,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(stringResource(R.string.ledger_amount_required))
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    singleLine = true
                )
            }

            Text(
                text = stringResource(R.string.ledger_borrow_date),
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge
            )

            OutlinedButton(
                onClick = {
                    showBorrowDatePicker = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(DateTimeUtils.formatDate(uiState.borrowDateMillis))
            }

            Text(
                text = stringResource(R.string.ledger_borrow_time),
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge
            )

            key(uiState.borrowHour, uiState.borrowMinute) {
                val borrowTimePickerState = rememberTimePickerState(
                    initialHour = uiState.borrowHour,
                    initialMinute = uiState.borrowMinute,
                    is24Hour = false
                )

                TimePicker(
                    state = borrowTimePickerState,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                TextButton(
                    onClick = {
                        viewModel.updateBorrowTime(
                            hour = borrowTimePickerState.hour,
                            minute = borrowTimePickerState.minute
                        )
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.ledger_apply_time))
                }
            }

            Text(
                text = stringResource(R.string.ledger_due_date),
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge
            )

            OutlinedButton(
                onClick = {
                    showDueDatePicker = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(DateTimeUtils.formatDate(uiState.dueDateMillis))
            }

            Text(
                text = stringResource(R.string.ledger_due_time),
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge
            )

            key(uiState.dueHour, uiState.dueMinute) {
                val dueTimePickerState = rememberTimePickerState(
                    initialHour = uiState.dueHour,
                    initialMinute = uiState.dueMinute,
                    is24Hour = false
                )

                TimePicker(
                    state = dueTimePickerState,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                TextButton(
                    onClick = {
                        viewModel.updateDueTime(
                            hour = dueTimePickerState.hour,
                            minute = dueTimePickerState.minute
                        )
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.ledger_apply_time))
                }
            }

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::updateNotes,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(R.string.ledger_notes_optional))
                },
                minLines = 3
            )

            Spacer(modifier = Modifier.height(4.dp))

            PrimaryButton(
                text = stringResource(R.string.save),
                onClick = viewModel::saveLedgerEntry,
                modifier = Modifier.fillMaxWidth()
            )

            SecondaryButton(
                text = stringResource(R.string.cancel),
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
