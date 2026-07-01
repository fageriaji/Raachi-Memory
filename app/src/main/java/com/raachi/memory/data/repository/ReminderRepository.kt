package com.raachi.memory.domain.repository

import com.raachi.memory.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getAllReminders(): Flow<List<Reminder>>

    suspend fun getReminderById(id: Int): Reminder?

    suspend fun insertReminder(reminder: Reminder): Long

    suspend fun updateReminder(reminder: Reminder)

    suspend fun deleteReminder(reminder: Reminder)
}