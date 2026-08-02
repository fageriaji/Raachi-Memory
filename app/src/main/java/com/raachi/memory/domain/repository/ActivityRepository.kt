package com.raachi.memory.domain.repository

import com.raachi.memory.domain.model.ActivityLog
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun observeAll(): Flow<List<ActivityLog>>
    suspend fun save(activity: ActivityLog): Long
}
