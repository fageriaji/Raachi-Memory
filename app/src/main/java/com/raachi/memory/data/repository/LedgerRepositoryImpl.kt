package com.raachi.memory.data.repository

import com.raachi.memory.data.local.dao.LedgerDao
import com.raachi.memory.domain.model.LedgerEntry
import com.raachi.memory.domain.repository.LedgerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LedgerRepositoryImpl @Inject constructor(
    private val ledgerDao: LedgerDao
) : LedgerRepository {

    override fun getAllEntries(): Flow<List<LedgerEntry>> {
        return ledgerDao.getAllEntries().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}