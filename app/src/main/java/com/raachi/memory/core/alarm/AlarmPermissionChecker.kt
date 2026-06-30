package com.raachi.memory.core.alarm

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Checks whether exact alarms can be scheduled on the current Android version.
 */
class AlarmPermissionChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun canScheduleExactAlarms(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true
        }

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        return alarmManager.canScheduleExactAlarms()
    }
}
