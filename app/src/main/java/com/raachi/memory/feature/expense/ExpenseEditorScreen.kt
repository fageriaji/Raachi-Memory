package com.raachi.memory.feature.expense

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raachi.memory.core.ui.ExpenseCreditColor
import com.raachi.memory.core.ui.ExpenseDebitColor
import com.raachi.memory.core.ui.ExpenseTransferColor
import com.raachi.memory.domain.model.ExpenseAccount
import com.raachi.memory.domain.model.ExpenseCategory
import com.raachi.memory.domain.model.ExpensePaymentMethod
import com.raachi.memory.domain.model.ExpenseTransactionInput
import com.raachi.memory.domain.model.ExpenseTransactionType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEditorScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExpenseEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved, state.deleted) {
        if (state.saved || state.deleted) onSaved()
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete transaction?") },
            text = { Text("The affected account balances will be recalculated.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.delete()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (state.input.id == 0L) "New Transaction" else "Edit Transaction", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.input.id != 0L) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete transaction", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
    ) { innerPadding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            ExpenseEditorContent(
                state = state,
                onUpdate = viewModel::updateInput,
                onSelectType = viewModel::selectType,
                onSave = viewModel::save,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun ExpenseEditorContent(
    state: ExpenseEditorUiState,
    onUpdate: (ExpenseTransactionInput.() -> ExpenseTransactionInput) -> Unit,
    onSelectType: (ExpenseTransactionType) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val input = state.input
    val validation = state.validation
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExpenseTransactionType.entries.forEach { type ->
                    val color = when (type) {
                        ExpenseTransactionType.DEBIT -> ExpenseDebitColor
                        ExpenseTransactionType.CREDIT -> ExpenseCreditColor
                        ExpenseTransactionType.TRANSFER -> ExpenseTransferColor
                    }
                    FilterChip(
                        selected = input.type == type,
                        onClick = { onSelectType(type) },
                        label = { Text(type.displayName()) },
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color.copy(alpha = 0.16f),
                            selectedLabelColor = color,
                        ),
                    )
                }
            }
        }
        item {
            OutlinedTextField(
                value = input.amount,
                onValueChange = { value ->
                    if (value.matches(Regex("^[0-9]*([.][0-9]{0,2})?$"))) onUpdate { copy(amount = value) }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Amount") },
                leadingIcon = { Text("₹") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = validation.amountError,
                supportingText = if (validation.amountError) ({ Text("Enter an amount greater than zero.") }) else null,
                singleLine = true,
            )
        }
        if (input.type != ExpenseTransactionType.CREDIT) {
            item {
                AccountDropdown(
                    label = if (input.type == ExpenseTransactionType.DEBIT) "Paid from" else "From account",
                    accounts = state.accounts,
                    selectedId = input.sourceAccountId,
                    isError = validation.sourceAccountError || validation.sameAccountError || validation.insufficientFundsError,
                    supportingText = when {
                        validation.insufficientFundsError -> "This account does not have enough available balance."
                        validation.sameAccountError -> "Choose two different accounts."
                        validation.sourceAccountError -> "Choose a source account."
                        else -> null
                    },
                    onSelected = { id -> onUpdate { copy(sourceAccountId = id) } },
                )
            }
        }
        if (input.type != ExpenseTransactionType.DEBIT) {
            item {
                AccountDropdown(
                    label = if (input.type == ExpenseTransactionType.CREDIT) "Received in" else "To account",
                    accounts = state.accounts,
                    selectedId = input.destinationAccountId,
                    isError = validation.destinationAccountError || validation.sameAccountError,
                    supportingText = when {
                        validation.sameAccountError -> "Choose two different accounts."
                        validation.destinationAccountError -> "Choose a destination account."
                        else -> null
                    },
                    onSelected = { id -> onUpdate { copy(destinationAccountId = id) } },
                )
            }
        }
        if (input.type != ExpenseTransactionType.TRANSFER) {
            item {
                EnumDropdown(
                    label = "Category",
                    value = input.category,
                    options = categoriesFor(input.type),
                    display = ExpenseCategory::displayName,
                    onSelected = { category -> onUpdate { copy(category = category) } },
                )
            }
            item {
                EnumDropdown(
                    label = "Payment method",
                    value = input.paymentMethod ?: ExpensePaymentMethod.OTHER,
                    options = ExpensePaymentMethod.entries,
                    display = ExpensePaymentMethod::displayName,
                    onSelected = { method -> onUpdate { copy(paymentMethod = method) } },
                )
            }
        }
        item {
            ExpenseDateTimeFields(
                date = input.transactionDate,
                timeMinutes = input.transactionTimeMinutes,
                onDate = { date -> onUpdate { copy(transactionDate = date) } },
                onTime = { minutes -> onUpdate { copy(transactionTimeMinutes = minutes) } },
            )
        }
        item {
            OutlinedTextField(
                value = input.note,
                onValueChange = { value -> onUpdate { copy(note = value) } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Note (optional)") },
                minLines = 3,
                maxLines = 5,
            )
        }
        item {
            Button(onClick = onSave, enabled = !state.isSaving && state.accounts.isNotEmpty(), modifier = Modifier.fillMaxWidth()) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                }
                Text(if (input.id == 0L) "Add transaction" else "Save changes")
            }
        }
    }
}

@Composable
private fun AccountDropdown(
    label: String,
    accounts: List<ExpenseAccount>,
    selectedId: Long?,
    isError: Boolean,
    supportingText: String?,
    onSelected: (Long) -> Unit,
) {
    val selected = accounts.find { it.id == selectedId }
    EnumDropdown(
        label = label,
        value = selected,
        options = accounts,
        display = { it?.name ?: "Choose account" },
        onSelected = { account -> account?.id?.let(onSelected) },
        isError = isError,
        supportingText = supportingText,
    )
}

@Composable
private fun <T> EnumDropdown(
    label: String,
    value: T,
    options: List<T>,
    display: (T) -> String,
    onSelected: (T) -> Unit,
    isError: Boolean = false,
    supportingText: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = display(value),
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            trailingIcon = { Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null) },
            readOnly = true,
            isError = isError,
            supportingText = supportingText?.let { text -> ({ Text(text) }) },
            singleLine = true,
        )
        Box(modifier = Modifier.matchParentSize().clickable { expanded = true })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(display(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun ExpenseDateTimeFields(
    date: LocalDate,
    timeMinutes: Int?,
    onDate: (LocalDate) -> Unit,
    onTime: (Int?) -> Unit,
) {
    val context = LocalContext.current
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = date.format(EDITOR_DATE_FORMAT),
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Date") },
                trailingIcon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
                readOnly = true,
            )
            Box(modifier = Modifier.matchParentSize().clickable {
                DatePickerDialog(
                    context,
                    { _, year, month, day -> onDate(LocalDate.of(year, month + 1, day)) },
                    date.year,
                    date.monthValue - 1,
                    date.dayOfMonth,
                ).show()
            })
        }
        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = timeMinutes?.let(::formatTime).orEmpty(),
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Time (optional)") },
                trailingIcon = { Icon(Icons.Outlined.Schedule, contentDescription = null) },
                readOnly = true,
            )
            Box(modifier = Modifier.matchParentSize().clickable {
                val hour = timeMinutes?.div(60) ?: 12
                val minute = timeMinutes?.rem(60) ?: 0
                TimePickerDialog(context, { _, h, m -> onTime(h * 60 + m) }, hour, minute, false).show()
            })
        }
    }
    if (timeMinutes != null) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { onTime(null) }) { Text("Clear time") }
        }
    }
}

private fun categoriesFor(type: ExpenseTransactionType): List<ExpenseCategory> = when (type) {
    ExpenseTransactionType.DEBIT -> listOf(
        ExpenseCategory.FOOD, ExpenseCategory.GROCERIES, ExpenseCategory.TRAVEL, ExpenseCategory.BILLS,
        ExpenseCategory.SHOPPING, ExpenseCategory.HEALTH, ExpenseCategory.EDUCATION,
        ExpenseCategory.ENTERTAINMENT, ExpenseCategory.RENT, ExpenseCategory.OTHER,
    )
    ExpenseTransactionType.CREDIT -> listOf(
        ExpenseCategory.SALARY, ExpenseCategory.REFUND, ExpenseCategory.INTEREST,
        ExpenseCategory.GIFT, ExpenseCategory.CASHBACK, ExpenseCategory.OTHER,
    )
    ExpenseTransactionType.TRANSFER -> listOf(ExpenseCategory.OTHER)
}

private fun ExpenseTransactionType.displayName() = name.lowercase().replaceFirstChar(Char::uppercase)
private fun ExpensePaymentMethod.displayName() = name.lowercase().replace('_', ' ').replaceFirstChar(Char::uppercase)
private val EDITOR_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-uuuu")
private fun formatTime(minutes: Int): String = "%02d:%02d".format(minutes / 60, minutes % 60)
