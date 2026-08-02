package com.raachi.memory.data.activity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activity_logs ORDER BY eventTimeMillis DESC, id DESC")
    fun observeAll(): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs ORDER BY id")
    suspend fun getAll(): List<ActivityLogEntity>

    @Insert
    suspend fun insert(activity: ActivityLogEntity): Long

    @Insert
    suspend fun insertAll(activities: List<ActivityLogEntity>)

    @Query("DELETE FROM activity_logs")
    suspend fun deleteAll()
}
