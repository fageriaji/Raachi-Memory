package com.raachi.memory.feature.activity

import com.raachi.memory.domain.model.ActivityEventType
import com.raachi.memory.domain.model.ActivityLog
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class ActivityViewModelTest {
    private val reminder = activity(
        id = 1,
        type = ActivityEventType.REMINDER_COMPLETED,
        title = "Reminder completed",
        description = "Drink water",
    )
    private val ledger = activity(
        id = 2,
        type = ActivityEventType.LEDGER_RETURNED,
        title = "Ledger entry returned",
        description = "Amit - 500 rupees",
    )

    @Test
    fun sourceFilter_returnsOnlySelectedModule() {
        assertEquals(listOf(reminder), filterActivityLogs(listOf(reminder, ledger), ActivityFilter.REMINDERS, ""))
        assertEquals(listOf(ledger), filterActivityLogs(listOf(reminder, ledger), ActivityFilter.LEDGER, ""))
    }

    @Test
    fun search_matchesTitleAndDescriptionIgnoringCase() {
        assertEquals(listOf(reminder), filterActivityLogs(listOf(reminder, ledger), ActivityFilter.ALL, "WATER"))
        assertEquals(listOf(ledger), filterActivityLogs(listOf(reminder, ledger), ActivityFilter.ALL, "returned"))
    }

    @Test
    fun searchAndSourceFilter_areAppliedTogether() {
        assertEquals(emptyList<ActivityLog>(), filterActivityLogs(listOf(reminder, ledger), ActivityFilter.REMINDERS, "Amit"))
    }

    private fun activity(
        id: Long,
        type: ActivityEventType,
        title: String,
        description: String,
    ) = ActivityLog(
        id = id,
        eventType = type,
        referenceId = id,
        title = title,
        description = description,
        eventTime = Instant.parse("2026-07-30T10:00:00Z"),
    )
}
