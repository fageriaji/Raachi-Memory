package com.raachi.memory.domain.repository

import com.raachi.memory.domain.model.LedgerEntry
import kotlinx.coroutines.flow.Flow

interface LedgerRepository {
    fun observeAll(): Flow<List<LedgerEntry>>
    suspend fun getById(id: Long): LedgerEntry?
    suspend fun getPendingDue(): List<LedgerEntry>
    suspend fun save(entry: LedgerEntry): Long
    suspend fun delete(id: Long)
}

interface LedgerAlertScheduler {
    fun schedule(entry: LedgerEntry)
    fun scheduleNextOverdue(entry: LedgerEntry)
    fun cancel(entryId: Long)
}
