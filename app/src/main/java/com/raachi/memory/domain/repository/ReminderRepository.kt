package com.raachi.memory.domain.repository

import com.raachi.memory.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun observeAll(): Flow<List<Reminder>>
    fun observeById(id: Long): Flow<Reminder?>
    suspend fun getById(id: Long): Reminder?
    suspend fun getScheduled(): List<Reminder>
    suspend fun save(reminder: Reminder): Long
    suspend fun delete(id: Long)
}

interface ReminderScheduler {
    fun schedule(reminder: Reminder)
    fun cancel(reminderId: Long)
}
