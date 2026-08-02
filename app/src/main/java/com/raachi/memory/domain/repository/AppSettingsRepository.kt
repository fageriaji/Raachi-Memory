package com.raachi.memory.domain.repository

import com.raachi.memory.domain.model.AppPreferences
import com.raachi.memory.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    val preferences: Flow<AppPreferences>
    val onboardingCompleted: Flow<Boolean>

    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setReminderSoundEnabled(enabled: Boolean)
    suspend fun setDefaultSnoozeMinutes(minutes: Int)
    suspend fun replacePreferences(preferences: AppPreferences)
}
