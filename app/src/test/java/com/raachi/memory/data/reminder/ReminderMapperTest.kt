package com.raachi.memory.data.reminder

import com.raachi.memory.domain.model.Reminder
import com.raachi.memory.domain.model.ReminderCategory
import com.raachi.memory.domain.model.ReminderRepeatType
import com.raachi.memory.domain.model.ReminderStatus
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderMapperTest {
    @Test
    fun reminder_roundTripsThroughRoomEntity() {
        val reminder = Reminder(
            id = 42,
            title = "Drink water",
            category = ReminderCategory.WATER,
            description = "One glass",
            repeatType = ReminderRepeatType.INTERVAL,
            intervalHours = 2,
            scheduledAt = Instant.parse("2026-07-29T08:00:00Z"),
            nextTriggerAt = Instant.parse("2026-07-29T12:00:00Z"),
            soundEnabled = false,
            vibrationEnabled = true,
            status = ReminderStatus.SNOOZED,
            createdAt = Instant.parse("2026-07-20T08:00:00Z"),
            updatedAt = Instant.parse("2026-07-29T10:00:00Z"),
        )

        assertEquals(reminder, reminder.toEntity().toDomain())
    }
}
