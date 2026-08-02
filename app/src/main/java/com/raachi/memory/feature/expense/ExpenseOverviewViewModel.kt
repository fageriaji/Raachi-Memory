package com.raachi.memory.feature.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raachi.memory.domain.model.ExpenseAccount
import com.raachi.memory.domain.model.ExpenseAccountInput
import com.raachi.memory.domain.model.ExpenseAccountValidation
import com.raachi.memory.domain.model.ExpenseTransaction
import com.raachi.memory.domain.model.ExpenseTransactionType
import com.raachi.memory.domain.model.currentBalance
import com.raachi.memory.domain.repository.ExpenseRepository
import com.raachi.memory.domain.usecase.DeleteExpenseTransactionUseCase
import com.raachi.memory.domain.usecase.SaveExpenseAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ExpenseTypeFilter { ALL, DEBIT, CREDIT, TRANSFER }
enum class ExpenseDateFilter { TODAY, THIS_WEEK, THIS_MONTH, ALL }

data class ExpenseAccountBalance(
    val account: ExpenseAccount,
    val balancePaise: Long,
)

data class ExpenseOverviewUiState(
    val accounts: List<ExpenseAccountBalance> = emptyList(),
    val transactions: List<ExpenseTransaction> = emptyList(),
    val totalBalancePaise: Long = 0,
    val todayDebitPaise: Long = 0,
    val monthDebitPaise: Long = 0,
    val monthCreditPaise: Long = 0,
    val typeFilter: ExpenseTypeFilter = ExpenseTypeFilter.ALL,
    val dateFilter: ExpenseDateFilter = ExpenseDateFilter.THIS_MONTH,
    val accountFilterId: Long? = null,
    val accountValidation: ExpenseAccountValidation = ExpenseAccountValidation(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class ExpenseOverviewViewModel @Inject constructor(
    repository: ExpenseRepository,
    private val saveAccountUseCase: SaveExpenseAccountUseCase,
    private val deleteTransactionUseCase: DeleteExpenseTransactionUseCase,
) : ViewModel() {
    private val typeFilter = MutableStateFlow(ExpenseTypeFilter.ALL)
    private val dateFilter = MutableStateFlow(ExpenseDateFilter.THIS_MONTH)
    private val accountFilterId = MutableStateFlow<Long?>(null)
    private val accountValidation = MutableStateFlow(ExpenseAccountValidation())
    private val filters = combine(typeFilter, dateFilter, accountFilterId, accountValidation) { type, date, accountId, validation ->
        ExpenseFilters(type, date, accountId, validation)
    }

    val uiState: StateFlow<ExpenseOverviewUiState> = combine(
        repository.observeActiveAccounts(),
        repository.observeTransactions(),
        filters,
    ) { accounts, transactions, filters ->
        val today = LocalDate.now()
        val activeIds = accounts.map(ExpenseAccount::id).toSet()
        val visibleTransactions = transactions.filter { transaction ->
            transaction.matches(filters.type) && transaction.matches(filters.date, today) &&
                (filters.accountId == null || transaction.sourceAccountId == filters.accountId ||
                    transaction.destinationAccountId == filters.accountId)
        }
        ExpenseOverviewUiState(
            accounts = accounts.map { account -> ExpenseAccountBalance(account, account.currentBalance(transactions)) },
            transactions = visibleTransactions,
            totalBalancePaise = accounts.sumOf { it.currentBalance(transactions) },
            todayDebitPaise = transactions.filter {
                it.type == ExpenseTransactionType.DEBIT && it.transactionDate == today && it.sourceAccountId in activeIds
            }.sumOf(ExpenseTransaction::amountPaise),
            monthDebitPaise = transactions.filter {
                it.type == ExpenseTransactionType.DEBIT && it.transactionDate.month == today.month &&
                    it.transactionDate.year == today.year && it.sourceAccountId in activeIds
            }.sumOf(ExpenseTransaction::amountPaise),
            monthCreditPaise = transactions.filter {
                it.type == ExpenseTransactionType.CREDIT && it.transactionDate.month == today.month &&
                    it.transactionDate.year == today.year && it.destinationAccountId in activeIds
            }.sumOf(ExpenseTransaction::amountPaise),
            typeFilter = filters.type,
            dateFilter = filters.date,
            accountFilterId = filters.accountId?.takeIf(activeIds::contains),
            accountValidation = filters.validation,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExpenseOverviewUiState(),
    )

    fun selectTypeFilter(filter: ExpenseTypeFilter) { typeFilter.value = filter }
    fun selectDateFilter(filter: ExpenseDateFilter) { dateFilter.value = filter }
    fun selectAccountFilter(id: Long?) { accountFilterId.value = id }
    fun clearAccountValidation() { accountValidation.value = ExpenseAccountValidation() }

    fun saveAccount(input: ExpenseAccountInput, onSaved: () -> Unit) = viewModelScope.launch {
        val validation = saveAccountUseCase(input)
        accountValidation.value = validation
        if (validation.isValid) onSaved()
    }

    fun deleteTransaction(id: Long) = viewModelScope.launch { deleteTransactionUseCase(id) }
}

private data class ExpenseFilters(
    val type: ExpenseTypeFilter,
    val date: ExpenseDateFilter,
    val accountId: Long?,
    val validation: ExpenseAccountValidation,
)

private fun ExpenseTransaction.matches(filter: ExpenseTypeFilter): Boolean = when (filter) {
    ExpenseTypeFilter.ALL -> true
    ExpenseTypeFilter.DEBIT -> type == ExpenseTransactionType.DEBIT
    ExpenseTypeFilter.CREDIT -> type == ExpenseTransactionType.CREDIT
    ExpenseTypeFilter.TRANSFER -> type == ExpenseTransactionType.TRANSFER
}

private fun ExpenseTransaction.matches(filter: ExpenseDateFilter, today: LocalDate): Boolean = when (filter) {
    ExpenseDateFilter.TODAY -> transactionDate == today
    ExpenseDateFilter.THIS_WEEK -> {
        val start = today.with(java.time.DayOfWeek.MONDAY)
        !transactionDate.isBefore(start) && !transactionDate.isAfter(today)
    }
    ExpenseDateFilter.THIS_MONTH -> {
        val start = today.with(TemporalAdjusters.firstDayOfMonth())
        !transactionDate.isBefore(start) && !transactionDate.isAfter(today)
    }
    ExpenseDateFilter.ALL -> true
}
