package com.raachi.memory.domain.repository

import com.raachi.memory.domain.model.LedgerEntry
import kotlinx.coroutines.flow.Flow

interface LedgerRepository {
    fun getAllEntries(): Flow<List<LedgerEntry>>
    fun getEntryById(id: Int): Flow<LedgerEntry?>
    suspend fun insertEntry(entry: LedgerEntry): Long
    suspend fun updateEntry(entry: LedgerEntry)
    suspend fun deleteEntry(entry: LedgerEntry)
}