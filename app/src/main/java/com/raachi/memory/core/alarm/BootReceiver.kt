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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var reminderRepository: ReminderRepository
    @Inject lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                try {
                    val reminders = reminderRepository.getAllReminders().first()
                    reminders.filter { it.status == ReminderStatus.ACTIVE }.forEach { reminder ->
                        if (reminder.nextTrigger < System.currentTimeMillis() && reminder.reminderType != ReminderType.ONE_TIME) {
                            val next = DateTimeUtils.calculateNextTrigger(
                                baseTimeMillis = reminder.scheduledTime,
                                type = reminder.reminderType,
                                intervalHours = reminder.intervalHours
                            )
                            val updatedReminder = reminder.copy(nextTrigger = next)
                            reminderRepository.updateReminder(updatedReminder)
                            alarmScheduler.schedule(updatedReminder)
                        } else {
                            alarmScheduler.schedule(reminder)
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}