package com.raachi.memory.features.reminder

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raachi.memory.core.alarm.AlarmScheduler
import com.raachi.memory.domain.model.Reminder
import com.raachi.memory.domain.model.ReminderCategory
import com.raachi.memory.domain.model.ReminderStatus
import com.raachi.memory.domain.model.ReminderType
import com.raachi.memory.domain.repository.ReminderRepository
import com.raachi.memory.domain.util.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class AddEditReminderViewModel @Inject constructor(
    private val repository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val reminderId: Int = checkNotNull(savedStateHandle["reminderId"])
    private var currentReminder: Reminder? = null

    var isLoading by mutableStateOf(reminderId != -1)
        private set

    var title by mutableStateOf("")
    var description by mutableStateOf("")
    var category by mutableStateOf(ReminderCategory.WATER)
    var reminderType by mutableStateOf(ReminderType.ONE_TIME)
    var intervalHours by mutableStateOf("2")

    var selectedDateMillis by mutableStateOf(
        LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
    var selectedHour by mutableIntStateOf(8)
    var selectedMinute by mutableIntStateOf(0)

    private val _saveCompleted = MutableSharedFlow<Unit>()
    val saveCompleted: SharedFlow<Unit> = _saveCompleted.asSharedFlow()

    // NEW: Flow for emitting validation errors to the UI
    private val _showError = MutableSharedFlow<String>()
    val showError: SharedFlow<String> = _showError.asSharedFlow()

    init {
        if (reminderId != -1) {
            viewModelScope.launch {
                repository.getReminderById(reminderId)?.let { reminder ->
                    currentReminder = reminder
                    title = reminder.title
                    description = reminder.description ?: ""
                    category = reminder.category
                    reminderType = reminder.reminderType
                    intervalHours = reminder.intervalHours?.toString() ?: "2"

                    val zdt = Instant.ofEpochMilli(reminder.scheduledTime).atZone(ZoneId.systemDefault())
                    selectedDateMillis = zdt.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    selectedHour = zdt.hour
                    selectedMinute = zdt.minute
                    isLoading = false
                } ?: run { isLoading = false }
            }
        }
    }

    fun saveReminder() {
        viewModelScope.launch {
            val selectedDate = Instant.ofEpochMilli(selectedDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
            val scheduledLdt = selectedDate.atTime(selectedHour, selectedMinute).withSecond(0).withNano(0)
            val now = LocalDateTime.now()

            // VALIDATION: Prevent saving if the selected date and time is in the past
            if (scheduledLdt.isBefore(now)) {
                _showError.emit("Past date and time is not allowed")
                return@launch
            }

            val baseTimeMillis = scheduledLdt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val intHours = intervalHours.toIntOrNull() ?: 2
            val nextTrigger = DateTimeUtils.calculateNextTrigger(baseTimeMillis, reminderType, intHours)

            val reminder = Reminder(
                id = if (reminderId == -1) 0 else reminderId,
                title = title.trim(),
                category = category,
                description = description.takeIf { it.isNotBlank() },
                reminderType = reminderType,
                repeatType = reminderType.name,
                intervalHours = if (reminderType == ReminderType.INTERVAL) intHours else null,
                scheduledTime = baseTimeMillis,
                nextTrigger = nextTrigger,
                ringtone = null,
                vibrationEnabled = true,
                status = ReminderStatus.ACTIVE,
                createdAt = currentReminder?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            if (reminderId == -1) {
                val newId = repository.insertReminder(reminder)
                alarmScheduler.schedule(reminder.copy(id = newId.toInt()))
            } else {
                alarmScheduler.cancel(reminder.id)
                repository.updateReminder(reminder)
                alarmScheduler.schedule(reminder)
            }

            _saveCompleted.emit(Unit)
        }
    }
}