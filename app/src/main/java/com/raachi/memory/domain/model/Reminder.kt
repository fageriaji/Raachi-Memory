package com.raachi.memory.domain.model

data class Reminder(
    val id: Int = 0,
    val title: String,
    val category: ReminderCategory,
    val description: String?,
    val reminderType: ReminderType,
    val repeatType: String?,
    val intervalHours: Int?,
    val scheduledTime: Long,
    val nextTrigger: Long,
    val ringtone: String?,
    val vibrationEnabled: Boolean,
    val status: ReminderStatus,
    val createdAt: Long,
    val updatedAt: Long
)