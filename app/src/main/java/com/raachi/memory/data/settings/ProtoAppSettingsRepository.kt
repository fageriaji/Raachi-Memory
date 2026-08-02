package com.raachi.memory.data.settings

import androidx.datastore.core.DataStore
import com.raachi.memory.domain.model.AppPreferences
import com.raachi.memory.domain.model.ThemeMode
import com.raachi.memory.domain.repository.AppSettingsRepository
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProtoAppSettingsRepository @Inject constructor(
    private val dataStore: DataStore<AppSettings>,
) : AppSettingsRepository {
    override val preferences: Flow<AppPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(AppSettings.getDefaultInstance())
            } else {
                throw exception
            }
        }
        .map(AppSettings::toDomain)

    override val onboardingCompleted: Flow<Boolean> = preferences.map { it.onboardingCompleted }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.updateData { settings ->
            settings.toBuilder()
                .setOnboardingCompleted(completed)
                .build()
        }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.updateData { settings -> settings.toBuilder().setThemeMode(mode.toProto()).build() }
    }

    override suspend fun setReminderSoundEnabled(enabled: Boolean) {
        dataStore.updateData { settings ->
            settings.toBuilder()
                .setReminderSound(if (enabled) SoundModeProto.SOUND_MODE_ENABLED else SoundModeProto.SOUND_MODE_DISABLED)
                .build()
        }
    }

    override suspend fun setDefaultSnoozeMinutes(minutes: Int) {
        require(minutes in ALLOWED_SNOOZE_MINUTES)
        dataStore.updateData { settings -> settings.toBuilder().setDefaultSnoozeMinutes(minutes).build() }
    }

    override suspend fun replacePreferences(preferences: AppPreferences) {
        dataStore.updateData {
            AppSettings.newBuilder()
                .setOnboardingCompleted(preferences.onboardingCompleted)
                .setThemeMode(preferences.themeMode.toProto())
                .setReminderSound(
                    if (preferences.reminderSoundEnabled) SoundModeProto.SOUND_MODE_ENABLED else SoundModeProto.SOUND_MODE_DISABLED,
                )
                .setDefaultSnoozeMinutes(preferences.defaultSnoozeMinutes.takeIf { it in ALLOWED_SNOOZE_MINUTES } ?: 10)
                .build()
        }
    }
}

private fun AppSettings.toDomain(): AppPreferences = AppPreferences(
    onboardingCompleted = onboardingCompleted,
    themeMode = when (themeMode) {
        ThemeModeProto.THEME_MODE_LIGHT -> ThemeMode.LIGHT
        ThemeModeProto.THEME_MODE_DARK -> ThemeMode.DARK
        else -> ThemeMode.SYSTEM
    },
    reminderSoundEnabled = reminderSound != SoundModeProto.SOUND_MODE_DISABLED,
    defaultSnoozeMinutes = defaultSnoozeMinutes.takeIf { it in ALLOWED_SNOOZE_MINUTES } ?: 10,
)

private fun ThemeMode.toProto(): ThemeModeProto = when (this) {
    ThemeMode.SYSTEM -> ThemeModeProto.THEME_MODE_SYSTEM
    ThemeMode.LIGHT -> ThemeModeProto.THEME_MODE_LIGHT
    ThemeMode.DARK -> ThemeModeProto.THEME_MODE_DARK
}

private val ALLOWED_SNOOZE_MINUTES = setOf(5, 10, 15, 30, 60)
