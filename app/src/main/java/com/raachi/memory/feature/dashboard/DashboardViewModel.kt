package com.raachi.memory.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raachi.memory.domain.usecase.ObserveProfileUseCase
import com.raachi.memory.domain.model.Reminder
import com.raachi.memory.domain.model.ReminderStatus
import com.raachi.memory.domain.repository.ReminderRepository
import com.raachi.memory.domain.model.LedgerEntry
import com.raachi.memory.domain.repository.LedgerRepository
import com.raachi.memory.domain.model.ExpenseAccount
import com.raachi.memory.domain.model.ExpenseTransaction
import com.raachi.memory.domain.model.ExpenseTransactionType
import com.raachi.memory.domain.model.currentBalance
import com.raachi.memory.domain.repository.ExpenseRepository
import java.time.LocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

sealed interface DashboardUiState {
    data object Loading : DashboardUiState

    data class Ready(
        val name: String,
        val greeting: GreetingPeriod,
        val reminderCount: Int = 0,
        val ledgerCount: Int = 0,
        val completedCount: Int = 0,
        val upcomingReminders: List<Reminder> = emptyList(),
        val pendingLedger: List<LedgerEntry> = emptyList(),
        val expenseAccountCount: Int = 0,
        val expenseBalancePaise: Long = 0,
        val todayExpensePaise: Long = 0,
    ) : DashboardUiState

    data object MissingProfile : DashboardUiState
}

enum class GreetingPeriod {
    MORNING,
    AFTERNOON,
    EVENING,
    NIGHT,
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeProfile: ObserveProfileUseCase,
    reminderRepository: ReminderRepository,
    ledgerRepository: LedgerRepository,
    expenseRepository: ExpenseRepository,
    clock: Clock,
) : ViewModel() {
    private val greeting = greetingPeriodAt(LocalTime.now(clock).hour)

    val uiState: StateFlow<DashboardUiState> = combine(
        observeProfile(),
        reminderRepository.observeAll(),
        ledgerRepository.observeAll(),
        expenseRepository.observeActiveAccounts(),
        expenseRepository.observeTransactions(),
    ) { profile, reminders, ledgerEntries, expenseAccounts, expenseTransactions ->
            profile?.let {
                val current = reminders.filter { reminder ->
                    reminder.status in setOf(ReminderStatus.ACTIVE, ReminderStatus.SNOOZED)
                }
                val pendingLedger = ledgerEntries.filterNot(LedgerEntry::isReturned)
                DashboardUiState.Ready(
                    name = it.name,
                    greeting = greeting,
                    reminderCount = current.size,
                    ledgerCount = pendingLedger.size,
                    completedCount = reminders.count { reminder -> reminder.status == ReminderStatus.COMPLETED },
                    upcomingReminders = current
                        .filter { reminder -> reminder.nextTriggerAt != null }
                        .sortedBy { reminder -> reminder.nextTriggerAt }
                        .take(2),
                    pendingLedger = pendingLedger
                        .sortedWith(
                            compareBy<LedgerEntry> { entry -> entry.dueDate == null }
                                .thenBy { entry -> entry.dueDate }
                                .thenByDescending { entry -> entry.createdAt },
                        )
                        .take(2),
                    expenseAccountCount = expenseAccounts.size,
                    expenseBalancePaise = expenseAccounts.sumOf { account -> account.currentBalance(expenseTransactions) },
                    todayExpensePaise = expenseTransactions.filter { transaction ->
                        transaction.type == ExpenseTransactionType.DEBIT && transaction.transactionDate == LocalDate.now(clock)
                    }.sumOf(ExpenseTransaction::amountPaise),
                )
            } ?: DashboardUiState.MissingProfile
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState.Loading,
        )
}

internal fun greetingPeriodAt(hour: Int): GreetingPeriod = when (hour) {
    in 6..11 -> GreetingPeriod.MORNING
    in 12..15 -> GreetingPeriod.AFTERNOON
    in 16..19 -> GreetingPeriod.EVENING
    else -> GreetingPeriod.NIGHT
}
