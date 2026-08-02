package com.raachi.memory.domain.model

import java.time.Instant

data class Reminder(
    val id: Long = 0,
    val title: String,
    val category: ReminderCategory,
    val description: String? = null,
    val repeatType: ReminderRepeatType,
    val intervalHours: Int? = null,
    val scheduledAt: Instant,
    val nextTriggerAt: Instant?,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val status: ReminderStatus = ReminderStatus.ACTIVE,
    val createdAt: Instant,
    val updatedAt: Instant,
)

enum class ReminderCategory { WATER, MEDICINE, BREAKFAST, LUNCH, DINNER, EXERCISE, SLEEP, CUSTOM }

enum class ReminderRepeatType { ONE_TIME, DAILY, WEEKLY, INTERVAL }

enum class ReminderStatus { ACTIVE, SNOOZED, COMPLETED, SKIPPED, ARCHIVED }
