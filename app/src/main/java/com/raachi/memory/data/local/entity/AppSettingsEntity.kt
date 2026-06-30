package com.raachi.memory.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.raachi.memory.domain.model.AppSettings

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "dark_mode") val darkMode: Boolean,
    @ColumnInfo(name = "reminder_sound") val reminderSound: String?,
    @ColumnInfo(name = "default_snooze_minutes") val defaultSnoozeMinutes: Int,
    @ColumnInfo(name = "notifications_enabled") val notificationsEnabled: Boolean,
    @ColumnInfo(name = "first_launch_completed") val firstLaunchCompleted: Boolean
) {
    fun toDomain() = AppSettings(id, darkMode, reminderSound, defaultSnoozeMinutes, notificationsEnabled, firstLaunchCompleted)

    companion object {
        fun fromDomain(model: AppSettings) = AppSettingsEntity(model.id, model.darkMode, model.reminderSound, model.defaultSnoozeMinutes, model.notificationsEnabled, model.firstLaunchCompleted)
    }
}