package com.raachi.memory.domain.repository

import com.raachi.memory.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getAppSettings(): Flow<AppSettings>
    suspend fun updateAppSettings(settings: AppSettings)
}