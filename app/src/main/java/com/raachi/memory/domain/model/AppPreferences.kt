package com.raachi.memory.domain.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppPreferences(
    val onboardingCompleted: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val reminderSoundEnabled: Boolean = true,
    val defaultSnoozeMinutes: Int = 10,
)
