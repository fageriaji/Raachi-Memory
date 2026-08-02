package com.raachi.memory.data.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.raachi.memory.domain.model.Reminder
import com.raachi.memory.domain.repository.ReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AlarmReminderScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ReminderScheduler {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(reminder: Reminder) {
        val triggerAt = reminder.nextTriggerAt?.toEpochMilli() ?: return
        val pendingIntent = reminderPendingIntent(context, reminder.id)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    override fun cancel(reminderId: Long) {
        alarmManager.cancel(reminderPendingIntent(context, reminderId))
    }
}

internal fun reminderPendingIntent(context: Context, reminderId: Long): PendingIntent {
    val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
        action = ReminderAlarmReceiver.ACTION_TRIGGER
        putExtra(ReminderAlarmReceiver.EXTRA_REMINDER_ID, reminderId)
    }
    return PendingIntent.getBroadcast(
        context,
        reminderId.requestCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}

internal fun Long.requestCode(): Int = (this xor (this ushr 32)).toInt()
