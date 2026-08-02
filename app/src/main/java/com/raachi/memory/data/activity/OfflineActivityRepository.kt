package com.raachi.memory.data.activity

import com.raachi.memory.domain.model.ActivityLog
import com.raachi.memory.domain.repository.ActivityRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineActivityRepository @Inject constructor(
    private val dao: ActivityDao,
) : ActivityRepository {
    override fun observeAll(): Flow<List<ActivityLog>> =
        dao.observeAll().map { logs -> logs.map(ActivityLogEntity::toDomain) }

    override suspend fun save(activity: ActivityLog): Long = dao.insert(activity.toEntity())
}
