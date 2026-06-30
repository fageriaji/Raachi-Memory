package com.raachi.memory.data.repository

import com.raachi.memory.data.local.dao.SettingsDao
import com.raachi.memory.data.local.entity.AppSettingsEntity
import com.raachi.memory.domain.model.AppSettings
import com.raachi.memory.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val settingsDao: SettingsDao
) : SettingsRepository {

    override fun getAppSettings(): Flow<AppSettings> {
        return settingsDao.getAppSettings().map { entity ->
            entity?.toDomain() ?: AppSettings()
        }
    }

    override suspend fun updateAppSettings(settings: AppSettings) {
        settingsDao.insertAppSettings(AppSettingsEntity.fromDomain(settings))
    }
}