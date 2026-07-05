package com.raachi.memory.data.repository

import com.raachi.memory.data.local.dao.LedgerDao
import com.raachi.memory.data.local.entity.LedgerEntryEntity
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

    override fun getEntryById(id: Int): Flow<LedgerEntry?> {
        return ledgerDao.getEntryById(id).map { it?.toDomain() }
    }

    override suspend fun insertEntry(entry: LedgerEntry): Long {
        return ledgerDao.insertEntry(LedgerEntryEntity.fromDomain(entry))
    }

    override suspend fun updateEntry(entry: LedgerEntry) {
        ledgerDao.updateEntry(LedgerEntryEntity.fromDomain(entry))
    }

    override suspend fun deleteEntry(entry: LedgerEntry) {
        ledgerDao.deleteEntry(LedgerEntryEntity.fromDomain(entry))
    }
}