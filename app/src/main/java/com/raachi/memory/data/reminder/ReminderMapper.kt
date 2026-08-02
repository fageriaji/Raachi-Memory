package com.raachi.memory.data.reminder

import com.raachi.memory.domain.model.Reminder
import com.raachi.memory.domain.model.ReminderCategory
import com.raachi.memory.domain.model.ReminderRepeatType
import com.raachi.memory.domain.model.ReminderStatus
import java.time.Instant

fun ReminderEntity.toDomain(): Reminder = Reminder(
    id = id,
    title = title,
    category = enumValueOrDefault(category, ReminderCategory.CUSTOM),
    description = description,
    repeatType = enumValueOrDefault(repeatType, ReminderRepeatType.ONE_TIME),
    intervalHours = intervalHours,
    scheduledAt = Instant.ofEpochMilli(scheduledAtMillis),
    nextTriggerAt = nextTriggerAtMillis?.let(Instant::ofEpochMilli),
    soundEnabled = soundEnabled,
    vibrationEnabled = vibrationEnabled,
    status = enumValueOrDefault(status, ReminderStatus.ARCHIVED),
    createdAt = Instant.ofEpochMilli(createdAtMillis),
    updatedAt = Instant.ofEpochMilli(updatedAtMillis),
)

fun Reminder.toEntity(): ReminderEntity = ReminderEntity(
    id = id,
    title = title,
    category = category.name,
    description = description,
    repeatType = repeatType.name,
    intervalHours = intervalHours,
    scheduledAtMillis = scheduledAt.toEpochMilli(),
    nextTriggerAtMillis = nextTriggerAt?.toEpochMilli(),
    soundEnabled = soundEnabled,
    vibrationEnabled = vibrationEnabled,
    status = status.name,
    createdAtMillis = createdAt.toEpochMilli(),
    updatedAtMillis = updatedAt.toEpochMilli(),
)

private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String, default: T): T =
    enumValues<T>().firstOrNull { it.name == value } ?: default
