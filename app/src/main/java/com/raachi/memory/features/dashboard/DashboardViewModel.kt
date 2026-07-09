// app/src/main/java/com/raachi/memory/features/dashboard/DashboardViewModel.kt

package com.raachi.memory.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raachi.memory.domain.model.LedgerEntry
import com.raachi.memory.domain.model.LedgerStatus
import com.raachi.memory.domain.model.Reminder
import com.raachi.memory.domain.model.ReminderStatus
import com.raachi.memory.domain.repository.LedgerRepository
import com.raachi.memory.domain.repository.ReminderRepository
import com.raachi.memory.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

enum class TimeOfDay { MORNING, AFTERNOON, EVENING, NIGHT }

sealed class DailySummary {
    data class RemindersAndLedgers(val reminders: Int, val ledgers: Int) : DailySummary()
    data class RemindersOnly(val reminders: Int) : DailySummary()
    data class LedgersOnly(val ledgers: Int) : DailySummary()
    data object AllCaughtUp : DailySummary()
}

data class DashboardState(
    val userName: String = "",
    val activeRemindersCount: Int = 0,
    val pendingLedgerCount: Int = 0,
    val pendingLedgerAmount: Double = 0.0,
    val completedTodayCount: Int = 0,
    val upcomingReminders: List<Reminder> = emptyList(),
    val topPendingLedgers: List<LedgerEntry> = emptyList(),
    val timeOfDay: TimeOfDay = TimeOfDay.MORNING,
    val currentDate: String = "",
    val dailySummary: DailySummary = DailySummary.AllCaughtUp
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    userRepository: UserRepository,
    reminderRepository: ReminderRepository,
    ledgerRepository: LedgerRepository
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern(
        "EEEE '·' d MMMM",
        Locale.getDefault()
    )

    private val currentTimeMillis = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(CLOCK_REFRESH_INTERVAL_MILLIS)
        }
    }

    val uiState: StateFlow<DashboardState> = combine(
        userRepository.getUserProfile(),
        reminderRepository.getAllReminders(),
        ledgerRepository.getAllEntries(),
        currentTimeMillis
    ) { user, reminders, ledgers, now ->

        val activeReminders = reminders.filter {
            it.status == ReminderStatus.ACTIVE
        }

        val pendingLedgers = ledgers.filter {
            it.status == LedgerStatus.PENDING
        }

        val upcomingDueLedgers = pendingLedgers
            .filter { entry ->
                val dueDateTime = entry.dueDateTime
                dueDateTime != null &&
                        dueDateTime >= now &&
                        dueDateTime <= now + UPCOMING_DUE_WINDOW_MILLIS
            }
            .sortedBy { entry ->
                entry.dueDateTime
            }
            .take(2)

        val hour = LocalTime.now().hour

        val timeOfDay = when (hour) {
            in 6..11 -> TimeOfDay.MORNING
            in 12..16 -> TimeOfDay.AFTERNOON
            in 17..21 -> TimeOfDay.EVENING
            else -> TimeOfDay.NIGHT
        }

        val upNext = activeReminders
            .sortedBy { it.nextTrigger }
            .take(2)

        val reminderCount = activeReminders.size
        val ledgerCount = pendingLedgers.size

        val summary = when {
            reminderCount > 0 && ledgerCount > 0 ->
                DailySummary.RemindersAndLedgers(reminderCount, ledgerCount)

            reminderCount > 0 ->
                DailySummary.RemindersOnly(reminderCount)

            ledgerCount > 0 ->
                DailySummary.LedgersOnly(ledgerCount)

            else ->
                DailySummary.AllCaughtUp
        }

        DashboardState(
            userName = user?.name ?: "User",
            activeRemindersCount = reminderCount,
            pendingLedgerCount = ledgerCount,
            pendingLedgerAmount = pendingLedgers.sumOf { it.amount ?: 0.0 },
            completedTodayCount = reminders.count {
                it.status == ReminderStatus.COMPLETED
            },
            upcomingReminders = upNext,
            topPendingLedgers = upcomingDueLedgers,
            timeOfDay = timeOfDay,
            currentDate = LocalDate.now().format(dateFormatter),
            dailySummary = summary
        )
    }.flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = DashboardState()
        )

    private companion object {
        const val CLOCK_REFRESH_INTERVAL_MILLIS = 60_000L
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val UPCOMING_DUE_WINDOW_MILLIS = 72L * 60L * 60L * 1000L
    }
}