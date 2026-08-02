package com.raachi.memory.feature.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raachi.memory.core.ui.ExpenseCreditColor
import com.raachi.memory.core.ui.ExpenseDebitColor
import com.raachi.memory.core.ui.ExpenseMainColor
import com.raachi.memory.core.ui.AppSection
import com.raachi.memory.core.ui.RaachiBottomBar
import com.raachi.memory.core.ui.RaachiSectionTopBar
import com.raachi.memory.core.ui.expenseCategoryEmoji
import com.raachi.memory.core.ui.expenseTypeColor
import com.raachi.memory.domain.model.ExpenseAccount
import com.raachi.memory.domain.model.ExpenseAccountInput
import com.raachi.memory.domain.model.ExpenseAccountType
import com.raachi.memory.domain.model.ExpenseTransaction
import com.raachi.memory.domain.model.ExpenseTransactionType
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseOverviewScreen(
    onBack: () -> Unit,
    onOpenSection: (AppSection) -> Unit,
    onAddTransaction: () -> Unit,
    onEditTransaction: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExpenseOverviewViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var accountEditor by remember { mutableStateOf<ExpenseAccount?>(null) }
    var showAccountEditor by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<ExpenseTransaction?>(null) }

    if (showAccountEditor) {
        AccountEditorSheet(
            account = accountEditor,
            validation = state.accountValidation,
            onDismiss = {
                showAccountEditor = false
                accountEditor = null
                viewModel.clearAccountValidation()
            },
            onSave = { input ->
                viewModel.saveAccount(input) {
                    showAccountEditor = false
                    accountEditor = null
                }
            },
        )
    }

    pendingDelete?.let { transaction ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete Transaction?") },
            text = { Text("This transaction will be permanently removed and account balances will be recalculated.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTransaction(transaction.id)
                    pendingDelete = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            RaachiSectionTopBar(
                title = "Daily Expenses",
                onBack = onBack,
                actions = {
                    IconButton(onClick = {
                        accountEditor = null
                        showAccountEditor = true
                    }) {
                        Icon(Icons.Outlined.AccountBalance, contentDescription = "Add account")
                    }
                },
            )
        },
        floatingActionButton = {
            if (state.accounts.isNotEmpty()) {
                FloatingActionButton(
                    onClick = onAddTransaction,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                ) { Icon(Icons.Outlined.Add, contentDescription = "Add transaction") }
            }
        },
        bottomBar = {
            RaachiBottomBar(
                selected = AppSection.EXPENSES,
                onSelected = onOpenSection,
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
            state.accounts.isEmpty() -> ExpenseSetupEmptyState(
                onAddAccount = {
                    accountEditor = null
                    showAccountEditor = true
                },
                modifier = Modifier.padding(innerPadding),
            )
            else -> ExpenseOverviewContent(
                state = state,
                onSelectType = viewModel::selectTypeFilter,
                onSelectDate = viewModel::selectDateFilter,
                onSelectAccount = viewModel::selectAccountFilter,
                onEditAccount = {
                    accountEditor = it
                    showAccountEditor = true
                },
                onEditTransaction = onEditTransaction,
                onDeleteTransaction = { pendingDelete = it },
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun ExpenseOverviewContent(
    state: ExpenseOverviewUiState,
    onSelectType: (ExpenseTypeFilter) -> Unit,
    onSelectDate: (ExpenseDateFilter) -> Unit,
    onSelectAccount: (Long?) -> Unit,
    onEditAccount: (ExpenseAccount) -> Unit,
    onEditTransaction: (Long) -> Unit,
    onDeleteTransaction: (ExpenseTransaction) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            BalanceHero(
                totalBalance = state.totalBalancePaise,
                todaySpent = state.todayDebitPaise,
                monthSpent = state.monthDebitPaise,
                monthCredit = state.monthCreditPaise,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Accounts", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Tap an account to filter", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.accounts, key = { it.account.id }) { account ->
                        AccountBalanceCard(
                            item = account,
                            selected = state.accountFilterId == account.account.id,
                            onSelect = {
                                onSelectAccount(if (state.accountFilterId == account.account.id) null else account.account.id)
                            },
                            onEdit = { onEditAccount(account.account) },
                        )
                    }
                }
            }
        }
        item {
            FilterRows(
                typeFilter = state.typeFilter,
                dateFilter = state.dateFilter,
                onSelectType = onSelectType,
                onSelectDate = onSelectDate,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
        item {
            Text(
                "Transactions",
                modifier = Modifier.padding(horizontal = 20.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        if (state.transactions.isEmpty()) {
            item {
                Text(
                    "No transactions match these filters.",
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 28.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(state.transactions, key = { it.id }) { transaction ->
                ExpenseTransactionCard(
                    transaction = transaction,
                    accounts = state.accounts.map(ExpenseAccountBalance::account),
                    onClick = { onEditTransaction(transaction.id) },
                    onDelete = { onDeleteTransaction(transaction) },
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }
    }
}

@Composable
private fun BalanceHero(
    totalBalance: Long,
    todaySpent: Long,
    monthSpent: Long,
    monthCredit: Long,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Available balance", style = MaterialTheme.typography.bodyLarge)
            Text(formatExpenseCurrency(totalBalance), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniBalanceStat("Today", todaySpent, ExpenseDebitColor, Modifier.weight(1f))
                MiniBalanceStat("This month", monthSpent, ExpenseDebitColor, Modifier.weight(1f))
                MiniBalanceStat("Credits", monthCredit, ExpenseCreditColor, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MiniBalanceStat(label: String, value: Long, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = Color.White.copy(alpha = 0.12f), shape = RoundedCornerShape(6.dp)) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 11.sp)
            Text(formatExpenseCurrency(value), color = color.copy(alpha = 0.95f), maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AccountBalanceCard(
    item: ExpenseAccountBalance,
    selected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
) {
    val color = Color(item.account.colorValue.toInt())
    Surface(
        onClick = onSelect,
        modifier = Modifier.width(184.dp),
        color = if (selected) color.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(if (selected) 2.dp else 1.dp, color.copy(alpha = if (selected) 0.9f else 0.22f)),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.AccountBalance, contentDescription = null, tint = color)
                Text(item.account.name, modifier = Modifier.weight(1f).padding(start = 8.dp), maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Edit account", modifier = Modifier.size(18.dp))
                }
            }
            Text(formatExpenseCurrency(item.balancePaise), color = color, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(item.account.type.displayName(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun FilterRows(
    typeFilter: ExpenseTypeFilter,
    dateFilter: ExpenseDateFilter,
    onSelectType: (ExpenseTypeFilter) -> Unit,
    onSelectDate: (ExpenseDateFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExpenseTypeFilter.entries.forEach { filter ->
                FilterChip(selected = typeFilter == filter, onClick = { onSelectType(filter) }, label = { Text(filter.displayName()) })
            }
        }
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExpenseDateFilter.entries.forEach { filter ->
                AssistChip(onClick = { onSelectDate(filter) }, label = {
                    Text(if (dateFilter == filter) "✓ ${filter.displayName()}" else filter.displayName())
                })
            }
        }
    }
}

@Composable
private fun ExpenseTransactionCard(
    transaction: ExpenseTransaction,
    accounts: List<ExpenseAccount>,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = expenseTypeColor(transaction.type)
    val source = accounts.find { it.id == transaction.sourceAccountId }?.name
    val destination = accounts.find { it.id == transaction.destinationAccountId }?.name
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp,
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).background(accent.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) { Text(expenseCategoryEmoji(transaction.category), fontSize = 24.sp) }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(transaction.category.displayName(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    when (transaction.type) {
                        ExpenseTransactionType.DEBIT -> "Paid from ${source.orEmpty()}"
                        ExpenseTransactionType.CREDIT -> "Received in ${destination.orEmpty()}"
                        ExpenseTransactionType.TRANSFER -> "${source.orEmpty()} to ${destination.orEmpty()}"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(transaction.transactionDate.format(EXPENSE_DATE_FORMAT), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete transaction", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(19.dp))
                }
                Text(
                    "${if (transaction.type == ExpenseTransactionType.DEBIT) "−" else if (transaction.type == ExpenseTransactionType.CREDIT) "+" else ""}${formatExpenseCurrency(transaction.amountPaise)}",
                    color = accent,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun ExpenseSetupEmptyState(onAddAccount: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Outlined.Payments, contentDescription = null, tint = ExpenseMainColor, modifier = Modifier.size(56.dp))
            Text("Set up your money", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Add a bank account or Cash in Hand with its opening balance.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onAddAccount) { Icon(Icons.Outlined.Add, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Add account") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountEditorSheet(
    account: ExpenseAccount?,
    validation: com.raachi.memory.domain.model.ExpenseAccountValidation,
    onDismiss: () -> Unit,
    onSave: (ExpenseAccountInput) -> Unit,
) {
    var input by remember(account) {
        mutableStateOf(
            account?.let {
                ExpenseAccountInput(it.id, it.name, it.type, formatPlainPaise(it.openingBalancePaise), it.colorValue)
            } ?: ExpenseAccountInput(),
        )
    }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(if (account == null) "Add account" else "Edit account", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = input.name,
                onValueChange = { input = input.copy(name = it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Account name") },
                placeholder = { Text("SBI, HDFC or Cash in Hand") },
                isError = validation.nameError,
                supportingText = if (validation.nameError) ({ Text("Enter an account name.") }) else null,
                singleLine = true,
            )
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExpenseAccountType.entries.forEach { type ->
                    FilterChip(selected = input.type == type, onClick = { input = input.copy(type = type) }, label = { Text(type.displayName()) })
                }
            }
            OutlinedTextField(
                value = input.openingBalance,
                onValueChange = { value -> if (value.matches(Regex("^[0-9]*([.][0-9]{0,2})?$"))) input = input.copy(openingBalance = value) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Opening balance") },
                leadingIcon = { Text("₹") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = validation.balanceError,
                supportingText = if (validation.balanceError) ({ Text("Enter zero or a valid positive balance.") }) else null,
                singleLine = true,
            )
            Text("Account color", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ACCOUNT_COLORS.forEach { value ->
                    Surface(
                        onClick = { input = input.copy(colorValue = value) },
                        modifier = Modifier.size(38.dp),
                        color = Color(value.toInt()),
                        shape = RoundedCornerShape(6.dp),
                        border = if (input.colorValue == value) androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface) else null,
                    ) {}
                }
            }
            Button(onClick = { onSave(input) }, modifier = Modifier.fillMaxWidth()) { Text("Save account") }
        }
    }
}

internal fun formatExpenseCurrency(paise: Long): String = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")).apply {
    maximumFractionDigits = if (paise % 100 == 0L) 0 else 2
}.format(BigDecimal(paise).movePointLeft(2))

private fun formatPlainPaise(paise: Long) = BigDecimal(paise).movePointLeft(2).stripTrailingZeros().toPlainString()
internal fun ExpenseAccountType.displayName() = name.lowercase().replace('_', ' ').replaceFirstChar(Char::uppercase)
internal fun com.raachi.memory.domain.model.ExpenseCategory.displayName() = name.lowercase().replace('_', ' ').replaceFirstChar(Char::uppercase)
private fun ExpenseTypeFilter.displayName() = name.lowercase().replace('_', ' ').replaceFirstChar(Char::uppercase)
private fun ExpenseDateFilter.displayName() = name.lowercase().replace('_', ' ').replaceFirstChar(Char::uppercase)
private val EXPENSE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM uuuu")
private val ACCOUNT_COLORS = listOf(0xFF142B85, 0xFF7400B8, 0xFF00897B, 0xFFE76F51, 0xFF455A64)
