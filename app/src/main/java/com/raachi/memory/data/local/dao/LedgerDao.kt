package com.raachi.memory.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.raachi.memory.data.local.entity.LedgerEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerDao {
    @Query("SELECT * FROM ledger_entries ORDER BY due_date_time ASC")
    fun getAllEntries(): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries WHERE id = :id")
    fun getEntryById(id: Int): Flow<LedgerEntryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: LedgerEntryEntity): Long

    @Update
    suspend fun updateEntry(entry: LedgerEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: LedgerEntryEntity)
}