package com.raachi.memory.domain.model

data class AppSettings(
    val id: Int = 1,
    val darkMode: Boolean = false,
    val reminderSound: String? = null,
    val defaultSnoozeMinutes: Int = 10,
    val notificationsEnabled: Boolean = true,
    val firstLaunchCompleted: Boolean = false
)