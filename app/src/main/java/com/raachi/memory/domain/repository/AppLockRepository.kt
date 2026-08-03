package com.raachi.memory.domain.repository

import com.raachi.memory.domain.model.AppLockSettings
import kotlinx.coroutines.flow.Flow

interface AppLockRepository {
    val appLockSettings: Flow<AppLockSettings>

    suspend fun replaceAppLockSettings(settings: AppLockSettings)
}
