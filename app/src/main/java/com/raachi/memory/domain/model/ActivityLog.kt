package com.raachi.memory.domain.model

import java.time.Instant

enum class ActivitySource { REMINDER, LEDGER }

enum class ActivityEventType(val source: ActivitySource) {
    REMINDER_CREATED(ActivitySource.REMINDER),
    REMINDER_UPDATED(ActivitySource.REMINDER),
    REMINDER_ALERT_SENT(ActivitySource.REMINDER),
    REMINDER_SNOOZED(ActivitySource.REMINDER),
    REMINDER_COMPLETED(ActivitySource.REMINDER),
    REMINDER_SKIPPED(ActivitySource.REMINDER),
    REMINDER_DELETED(ActivitySource.REMINDER),
    LEDGER_CREATED(ActivitySource.LEDGER),
    LEDGER_UPDATED(ActivitySource.LEDGER),
    LEDGER_ALERT_SENT(ActivitySource.LEDGER),
    LEDGER_RETURNED(ActivitySource.LEDGER),
    LEDGER_DELETED(ActivitySource.LEDGER),
}

data class ActivityLog(
    val id: Long = 0,
    val eventType: ActivityEventType,
    val referenceId: Long?,
    val title: String,
    val description: String?,
    val eventTime: Instant,
) {
    val source: ActivitySource get() = eventType.source
}
