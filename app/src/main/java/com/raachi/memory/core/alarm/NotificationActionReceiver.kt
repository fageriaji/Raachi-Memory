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
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject lateinit var reminderRepository: ReminderRepository
    @Inject lateinit var alarmScheduler: AlarmScheduler
    @Inject lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val reminderId = intent.getIntExtra(NotificationHelper.EXTRA_REMINDER_ID, -1)
        if (reminderId == -1) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val reminder = reminderRepository.getReminderById(reminderId) ?: return@launch

                notificationHelper.cancelNotification(reminderId)

                when (action) {
                    NotificationHelper.ACTION_DONE, NotificationHelper.ACTION_SKIP -> {
                        if (reminder.reminderType == ReminderType.ONE_TIME) {
                            val newStatus = if (action == NotificationHelper.ACTION_DONE) {
                                ReminderStatus.COMPLETED
                            } else {
                                ReminderStatus.SKIPPED
                            }

                            reminderRepository.updateReminder(
                                reminder.copy(
                                    status = newStatus,
                                    updatedAt = System.currentTimeMillis()
                                )
                            )
                        } else {
                            // The ReminderReceiver already steps forward repeating alarms.
                            // However, we handle the case here just in case the notification
                            // was clicked extremely quickly, to ensure the DB state remains clean.
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
                    NotificationHelper.ACTION_SNOOZE -> {
                        val snoozeTime = System.currentTimeMillis() + 600_000 // 10 minutes from now
                        val snoozedReminder = reminder.copy(
                            nextTrigger = snoozeTime,
                            updatedAt = System.currentTimeMillis()
                        )
                        reminderRepository.updateReminder(snoozedReminder)
                        alarmScheduler.schedule(snoozedReminder)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}