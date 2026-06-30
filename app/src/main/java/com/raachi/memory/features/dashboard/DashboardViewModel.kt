package com.raachi.memory.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raachi.memory.domain.model.LedgerEntry
import com.raachi.memory.domain.model.Reminder
import com.raachi.memory.domain.model.ReminderStatus
import com.raachi.memory.domain.repository.LedgerRepository
import com.raachi.memory.domain.repository.ReminderRepository
import com.raachi.memory.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

enum class TimeOfDay { MORNING, AFTERNOON, EVENING }

data class DashboardState(
    val userName: String = "",
    val activeRemindersCount: Int = 0,
    val pendingLedgerCount: Int = 0,
    val pendingLedgerAmount: Double = 0.0,
    val completedTodayCount: Int = 0,
    val nextReminder: Reminder? = null,
    val timeOfDay: TimeOfDay = TimeOfDay.MORNING,
    val currentDate: String = ""
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    userRepository: UserRepository,
    reminderRepository: ReminderRepository,
    ledgerRepository: LedgerRepository
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())

    val uiState: StateFlow<DashboardState> = combine(
        userRepository.getUserProfile(),
        reminderRepository.getAllReminders(),
        ledgerRepository.getAllEntries()
    ) { user, reminders, ledgers ->

        val activeReminders = reminders.filter { it.status == ReminderStatus.ACTIVE }
        val pendingLedgers = ledgers.filter { !it.returned }

        val hour = LocalTime.now().hour
        val timeOfDay = when (hour) {
            in 0..11 -> TimeOfDay.MORNING
            in 12..16 -> TimeOfDay.AFTERNOON
            else -> TimeOfDay.EVENING
        }

        DashboardState(
            userName = user?.name ?: "User",
            activeRemindersCount = activeReminders.size,
            pendingLedgerCount = pendingLedgers.size,
            pendingLedgerAmount = pendingLedgers.sumOf { it.amount ?: 0.0 },
            completedTodayCount = reminders.count { it.status == ReminderStatus.COMPLETED },
            nextReminder = activeReminders.minByOrNull { it.scheduledTime },
            timeOfDay = timeOfDay,
            currentDate = LocalDate.now().format(dateFormatter)
        )
    }.flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardState()
        )
}