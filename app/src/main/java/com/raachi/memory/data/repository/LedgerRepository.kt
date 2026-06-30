package com.raachi.memory.domain.repository

import com.raachi.memory.domain.model.LedgerEntry
import kotlinx.coroutines.flow.Flow

interface LedgerRepository {
    fun getAllEntries(): Flow<List<LedgerEntry>>
}