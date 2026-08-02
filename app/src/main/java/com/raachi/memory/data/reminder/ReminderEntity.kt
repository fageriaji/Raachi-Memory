package com.raachi.memory.data.reminder

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    indices = [Index(value = ["nextTriggerAtMillis"]), Index(value = ["status"])],
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val description: String?,
    val repeatType: String,
    val intervalHours: Int?,
    val scheduledAtMillis: Long,
    val nextTriggerAtMillis: Long?,
    val soundEnabled: Boolean,
    val vibrationEnabled: Boolean,
    val status: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
