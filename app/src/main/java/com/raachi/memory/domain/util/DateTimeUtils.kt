package com.raachi.memory.domain.util

import com.raachi.memory.domain.model.ReminderType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object DateTimeUtils {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    fun calculateNextTrigger(
        baseTimeMillis: Long,
        type: ReminderType,
        intervalHours: Int? = null
    ): Long {
        val now = ZonedDateTime.now().plusMinutes(1)
        var next = Instant.ofEpochMilli(baseTimeMillis).atZone(ZoneId.systemDefault())

        while (next.isBefore(now)) {
            next = when (type) {
                ReminderType.ONE_TIME -> return baseTimeMillis
                ReminderType.DAILY -> next.plusDays(1)
                ReminderType.WEEKLY -> next.plusWeeks(1)
                ReminderType.INTERVAL -> next.plusHours((intervalHours ?: 1).toLong())
            }
        }

        return next.toInstant().toEpochMilli()
    }

    fun formatDate(timeMillis: Long): String {
        return Instant.ofEpochMilli(timeMillis)
            .atZone(ZoneId.systemDefault())
            .format(dateFormatter)
    }

    fun formatTime(timeMillis: Long): String {
        return Instant.ofEpochMilli(timeMillis)
            .atZone(ZoneId.systemDefault())
            .format(timeFormatter)
    }

    fun todayStartMillis(): Long {
        return LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    fun combineDateAndTime(
        dateMillis: Long,
        hour: Int,
        minute: Int
    ): Long {
        val localDate = Instant.ofEpochMilli(dateMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        return localDate
            .atTime(hour, minute)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}
