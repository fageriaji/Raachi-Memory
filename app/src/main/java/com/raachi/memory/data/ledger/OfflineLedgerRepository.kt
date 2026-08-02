package com.raachi.memory.data.ledger

import com.raachi.memory.domain.model.LedgerEntry
import com.raachi.memory.domain.repository.LedgerRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineLedgerRepository @Inject constructor(private val dao: LedgerDao) : LedgerRepository {
    override fun observeAll(): Flow<List<LedgerEntry>> = dao.observeAll().map { entries -> entries.map(LedgerEntryEntity::toDomain) }
    override suspend fun getById(id: Long): LedgerEntry? = dao.getById(id)?.toDomain()
    override suspend fun getPendingDue(): List<LedgerEntry> = dao.getPendingDue().map(LedgerEntryEntity::toDomain)
    override suspend fun save(entry: LedgerEntry): Long = dao.upsert(entry.toEntity())
    override suspend fun delete(id: Long) = dao.deleteById(id)
}
