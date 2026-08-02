package com.raachi.memory.data.reminder

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.raachi.memory.domain.model.ReminderStatus
import com.raachi.memory.domain.model.ActivityEventType
import com.raachi.memory.domain.repository.ReminderRepository
import com.raachi.memory.domain.repository.ReminderScheduler
import com.raachi.memory.domain.repository.AppSettingsRepository
import com.raachi.memory.domain.usecase.CompleteReminderUseCase
import com.raachi.memory.domain.usecase.SkipReminderUseCase
import com.raachi.memory.domain.usecase.SnoozeReminderUseCase
import com.raachi.memory.domain.usecase.calculateFollowingTrigger
import com.raachi.memory.domain.usecase.LogActivityUseCase
import dagger.hilt.android.AndroidEntryPoint
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

@AndroidEntryPoint
class ReminderAlarmReceiver : BroadcastReceiver() {
    @Inject lateinit var repository: ReminderRepository
    @Inject lateinit var scheduler: ReminderScheduler
    @Inject lateinit var snoozeReminder: SnoozeReminderUseCase
    @Inject lateinit var completeReminder: CompleteReminderUseCase
    @Inject lateinit var skipReminder: SkipReminderUseCase
    @Inject lateinit var clock: Clock
    @Inject lateinit var logActivity: LogActivityUseCase
    @Inject lateinit var settingsRepository: AppSettingsRepository

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(EXTRA_REMINDER_ID, 0L)
        if (id == 0L) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_TRIGGER -> handleTrigger(context, id)
                    ACTION_SNOOZE -> snoozeReminder(id)
                    ACTION_DONE -> completeReminder(id)
                    ACTION_SKIP -> skipReminder(id)
                }
                if (intent.action != ACTION_TRIGGER) {
                    context.getSystemService(NotificationManager::class.java).cancel(id.requestCode())
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleTrigger(context: Context, id: Long) {
        val reminder = repository.getById(id) ?: return
        if (reminder.status !in setOf(ReminderStatus.ACTIVE, ReminderStatus.SNOOZED)) return
        val soundEnabled = settingsRepository.preferences.first().reminderSoundEnabled && reminder.soundEnabled
        showReminderNotification(context, reminder, soundEnabled)
        logActivity(ActivityEventType.REMINDER_ALERT_SENT, id, "Reminder alert sent", reminder.title)
        val next = calculateFollowingTrigger(reminder, clock.instant(), clock.zone)
        val updated = reminder.copy(
            status = ReminderStatus.ACTIVE,
            nextTriggerAt = next,
            updatedAt = clock.instant(),
        )
        repository.save(updated)
        if (next != null) scheduler.schedule(updated)
    }

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val ACTION_TRIGGER = "com.raachi.memory.action.REMINDER_TRIGGER"
        const val ACTION_SNOOZE = "com.raachi.memory.action.REMINDER_SNOOZE"
        const val ACTION_DONE = "com.raachi.memory.action.REMINDER_DONE"
        const val ACTION_SKIP = "com.raachi.memory.action.REMINDER_SKIP"
    }
}
