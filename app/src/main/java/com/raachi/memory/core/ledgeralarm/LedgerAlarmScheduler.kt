package com.raachi.memory.core.ledgeralarm

import com.raachi.memory.domain.model.LedgerEntry

interface LedgerAlarmScheduler {
    fun schedule(entry: LedgerEntry)
    fun cancel(entryId: Int)
}