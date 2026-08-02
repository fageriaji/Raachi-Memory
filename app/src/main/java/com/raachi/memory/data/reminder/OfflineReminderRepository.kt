package com.raachi.memory.data.reminder

import com.raachi.memory.domain.model.Reminder
import com.raachi.memory.domain.repository.ReminderRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineReminderRepository @Inject constructor(private val dao: ReminderDao) : ReminderRepository {
    override fun observeAll(): Flow<List<Reminder>> = dao.observeAll().map { items -> items.map(ReminderEntity::toDomain) }
    override fun observeById(id: Long): Flow<Reminder?> = dao.observeById(id).map { it?.toDomain() }
    override suspend fun getById(id: Long): Reminder? = dao.getById(id)?.toDomain()
    override suspend fun getScheduled(): List<Reminder> = dao.getScheduled().map(ReminderEntity::toDomain)
    override suspend fun save(reminder: Reminder): Long = dao.upsert(reminder.toEntity())
    override suspend fun delete(id: Long) = dao.deleteById(id)
}
