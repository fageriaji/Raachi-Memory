package com.raachi.memory.core.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.raachi.memory.domain.model.ReminderStatus
import com.raachi.memory.domain.model.ReminderType
import com.raachi.memory.domain.repository.ReminderRepository
import com.raachi.memory.domain.util.DateTimeUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var reminderRepository: ReminderRepository
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getIntExtra(NotificationHelper.EXTRA_REMINDER_ID, -1)
        if (reminderId == -1) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val reminder = reminderRepository.getReminderById(reminderId)

                if (reminder != null && reminder.status == ReminderStatus.ACTIVE) {
                    notificationHelper.showReminderNotification(reminder)

                    // Immediately step recurring alarms forward.
                    // This protects the cycle even if the user swipe-dismisses the notification.
                    if (reminder.reminderType != ReminderType.ONE_TIME) {
                        val next = DateTimeUtils.calculateNextTrigger(
                            baseTimeMillis = reminder.scheduledTime,
                            type = reminder.reminderType,
                            intervalHours = reminder.intervalHours
                        )
                        val updatedReminder = reminder.copy(
                            nextTrigger = next,
                            updatedAt = System.currentTimeMillis()
                        )
                        reminderRepository.updateReminder(updatedReminder)
                        alarmScheduler.schedule(updatedReminder)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}