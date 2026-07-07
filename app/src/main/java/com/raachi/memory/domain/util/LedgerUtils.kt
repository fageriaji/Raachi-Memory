package com.raachi.memory.domain.util

import com.raachi.memory.domain.model.LedgerEntry
import com.raachi.memory.domain.model.LedgerStatus

fun LedgerEntry.isOverdue(currentTimeMillis: Long): Boolean {
    return status == LedgerStatus.PENDING &&
            dueDateTime != null &&
            dueDateTime < currentTimeMillis
}
