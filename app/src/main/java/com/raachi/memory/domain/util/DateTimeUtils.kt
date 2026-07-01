package com.raachi.memory.domain.util

import com.raachi.memory.domain.model.ReminderType
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object DateTimeUtils {
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

    fun formatTime(timeMillis: Long): String {
        val zdt = Instant.ofEpochMilli(timeMillis).atZone(ZoneId.systemDefault())
        return zdt.format(timeFormatter)
    }
}