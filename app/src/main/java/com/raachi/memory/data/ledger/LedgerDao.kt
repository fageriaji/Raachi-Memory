package com.raachi.memory.data.ledger

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerDao {
    @Query("SELECT * FROM ledger_entries ORDER BY isReturned, dueDateEpochDay IS NULL, dueDateEpochDay, createdAtMillis DESC")
    fun observeAll(): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries ORDER BY id")
    suspend fun getAll(): List<LedgerEntryEntity>

    @Query("SELECT * FROM ledger_entries WHERE id = :id")
    suspend fun getById(id: Long): LedgerEntryEntity?

    @Query("SELECT * FROM ledger_entries WHERE isReturned = 0 AND dueDateEpochDay IS NOT NULL")
    suspend fun getPendingDue(): List<LedgerEntryEntity>

    @Upsert
    suspend fun upsert(entry: LedgerEntryEntity): Long

    @Upsert
    suspend fun upsertAll(entries: List<LedgerEntryEntity>)

    @Query("DELETE FROM ledger_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM ledger_entries")
    suspend fun deleteAll()
}
