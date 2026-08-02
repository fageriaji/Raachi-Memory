package com.raachi.memory.data.activity

import com.raachi.memory.domain.model.ActivityEventType
import com.raachi.memory.domain.model.ActivityLog
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class ActivityMapperTest {
    @Test
    fun activity_roundTripsWithoutLosingEventData() {
        val activity = ActivityLog(
            id = 8,
            eventType = ActivityEventType.LEDGER_ALERT_SENT,
            referenceId = 42,
            title = "Ledger alert sent",
            description = "Amit - money due",
            eventTime = Instant.parse("2026-07-30T10:15:30Z"),
        )

        assertEquals(activity, activity.toEntity().toDomain())
    }
}
