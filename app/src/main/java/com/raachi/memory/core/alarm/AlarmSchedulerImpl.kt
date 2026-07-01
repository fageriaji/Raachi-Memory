package com.raachi.memory.core.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.raachi.memory.domain.model.Reminder
import javax.inject.Inject

class AlarmSchedulerImpl @Inject constructor(
    private val context: Context,
    private val alarmManager: AlarmManager
) : AlarmScheduler {

    @SuppressLint("MissingPermission") // Handled safely via canScheduleExactAlarms()
    override fun schedule(reminder: Reminder) {
        val pendingIntent = createPendingIntent(reminder.id)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminder.nextTrigger,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminder.nextTrigger,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminder.nextTrigger,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback for unexpected permission revocation
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminder.nextTrigger,
                pendingIntent
            )
        }
    }

    override fun cancel(reminderId: Int) {
        alarmManager.cancel(createPendingIntent(reminderId))
    }

    private fun createPendingIntent(reminderId: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(NotificationHelper.EXTRA_REMINDER_ID, reminderId)
        }
        return PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}