package com.raachi.memory.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.raachi.memory.domain.model.Reminder
import com.raachi.memory.domain.model.ReminderCategory
import com.raachi.memory.domain.model.ReminderStatus
import com.raachi.memory.domain.model.ReminderType

@Entity(
    tableName = "reminders",
    indices = [Index(value = ["next_trigger"]), Index(value = ["status"])]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: ReminderCategory,
    val description: String?,
    @ColumnInfo(name = "reminder_type") val reminderType: ReminderType,
    @ColumnInfo(name = "repeat_type") val repeatType: String?,
    @ColumnInfo(name = "interval_hours") val intervalHours: Int?,
    @ColumnInfo(name = "scheduled_time") val scheduledTime: Long,
    @ColumnInfo(name = "next_trigger") val nextTrigger: Long,
    val ringtone: String?,
    @ColumnInfo(name = "vibration_enabled") val vibrationEnabled: Boolean,
    val status: ReminderStatus,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
) {
    fun toDomain() = Reminder(id, title, category, description, reminderType, repeatType, intervalHours, scheduledTime, nextTrigger, ringtone, vibrationEnabled, status, createdAt, updatedAt)

    companion object {
        fun fromDomain(model: Reminder) = ReminderEntity(model.id, model.title, model.category, model.description, model.reminderType, model.repeatType, model.intervalHours, model.scheduledTime, model.nextTrigger, model.ringtone, model.vibrationEnabled, model.status, model.createdAt, model.updatedAt)
    }
}