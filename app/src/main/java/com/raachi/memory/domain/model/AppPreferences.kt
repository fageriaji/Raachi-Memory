package com.raachi.memory.domain.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppPreferences(
    val onboardingCompleted: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val reminderSoundEnabled: Boolean = true,
    val defaultSnoozeMinutes: Int = 10,
    val quietHoursEnabled: Boolean = true,
    val quietHoursStartMinutes: Int = DEFAULT_QUIET_HOURS_START_MINUTES,
    val quietHoursEndMinutes: Int = DEFAULT_QUIET_HOURS_END_MINUTES,
)

const val DEFAULT_QUIET_HOURS_START_MINUTES = 23 * 60
const val DEFAULT_QUIET_HOURS_END_MINUTES = 6 * 60
