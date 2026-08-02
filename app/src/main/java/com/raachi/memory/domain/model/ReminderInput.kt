package com.raachi.memory.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class ReminderInput(
    val id: Long = 0,
    val title: String = "",
    val category: ReminderCategory = ReminderCategory.WATER,
    val description: String = "",
    val repeatType: ReminderRepeatType = ReminderRepeatType.DAILY,
    val intervalHours: Int = 2,
    val startDate: LocalDate = LocalDate.now(),
    val startTime: LocalTime = LocalTime.of(8, 0),
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
)

data class ReminderValidation(
    val titleError: Boolean = false,
    val scheduleError: Boolean = false,
    val intervalError: Boolean = false,
) {
    val isValid: Boolean get() = !titleError && !scheduleError && !intervalError
}
