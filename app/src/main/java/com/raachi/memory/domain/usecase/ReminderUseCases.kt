package com.raachi.memory.domain.usecase

import com.raachi.memory.domain.model.Reminder
import com.raachi.memory.domain.model.ActivityEventType
import com.raachi.memory.domain.model.ReminderInput
import com.raachi.memory.domain.model.ReminderRepeatType
import com.raachi.memory.domain.model.ReminderStatus
import com.raachi.memory.domain.model.ReminderValidation
import com.raachi.memory.domain.repository.ReminderRepository
import com.raachi.memory.domain.repository.ReminderScheduler
import com.raachi.memory.domain.repository.AppSettingsRepository
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class SaveReminderUseCase @Inject constructor(
    private val repository: ReminderRepository,
    private val scheduler: ReminderScheduler,
    private val clock: Clock,
    private val logActivity: LogActivityUseCase,
) {
    suspend operator fun invoke(input: ReminderInput): ReminderValidation {
        val validation = validateReminder(input, clock)
        if (!validation.isValid) return validation
        val existing = input.id.takeIf { it != 0L }?.let { repository.getById(it) }
        val now = clock.instant()
        val reminder = Reminder(
            id = input.id,
            title = input.title.trim(),
            category = input.category,
            description = input.description.trim().ifBlank { null },
            repeatType = input.repeatType,
            intervalHours = input.intervalHours.takeIf { input.repeatType == ReminderRepeatType.INTERVAL },
            scheduledAt = input.startDate.atTime(input.startTime).atZone(clock.zone).toInstant(),
            nextTriggerAt = calculateFirstTrigger(input, clock),
            soundEnabled = input.soundEnabled,
            vibrationEnabled = input.vibrationEnabled,
            status = ReminderStatus.ACTIVE,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
        )
        val insertedId = repository.save(reminder)
        val id = input.id.takeIf { it != 0L } ?: insertedId
        scheduler.schedule(reminder.copy(id = id))
        logActivity(
            eventType = if (existing == null) ActivityEventType.REMINDER_CREATED else ActivityEventType.REMINDER_UPDATED,
            referenceId = id,
            title = if (existing == null) "Reminder created" else "Reminder updated",
            description = reminder.title,
        )
        return validation
    }
}

class DeleteReminderUseCase @Inject constructor(
    private val repository: ReminderRepository,
    private val scheduler: ReminderScheduler,
    private val logActivity: LogActivityUseCase,
) {
    suspend operator fun invoke(id: Long) {
        val reminder = repository.getById(id) ?: return
        scheduler.cancel(id)
        repository.delete(id)
        logActivity(ActivityEventType.REMINDER_DELETED, id, "Reminder deleted", reminder.title)
    }
}

class SnoozeReminderUseCase @Inject constructor(
    private val repository: ReminderRepository,
    private val scheduler: ReminderScheduler,
    private val clock: Clock,
    private val logActivity: LogActivityUseCase,
    private val settingsRepository: AppSettingsRepository,
) {
    suspend operator fun invoke(id: Long, minutes: Long? = null) {
        val reminder = repository.getById(id) ?: return
        val snoozeMinutes = minutes ?: settingsRepository.preferences.first().defaultSnoozeMinutes.toLong()
        val now = clock.instant()
        val updated = reminder.copy(
            status = ReminderStatus.SNOOZED,
            nextTriggerAt = now.plus(Duration.ofMinutes(snoozeMinutes)),
            updatedAt = now,
        )
        repository.save(updated)
        scheduler.schedule(updated)
        logActivity(
            ActivityEventType.REMINDER_SNOOZED,
            id,
            "Reminder snoozed",
            "${reminder.title} for $snoozeMinutes minutes",
        )
    }
}

class CompleteReminderUseCase @Inject constructor(
    private val repository: ReminderRepository,
    private val scheduler: ReminderScheduler,
    private val clock: Clock,
    private val logActivity: LogActivityUseCase,
) {
    suspend operator fun invoke(id: Long) {
        val reminder = repository.getById(id) ?: return
        scheduler.cancel(id)
        repository.save(
            reminder.copy(
                status = ReminderStatus.COMPLETED,
                nextTriggerAt = null,
                updatedAt = clock.instant(),
            ),
        )
        logActivity(ActivityEventType.REMINDER_COMPLETED, id, "Reminder completed", reminder.title)
    }
}

class SkipReminderUseCase @Inject constructor(
    private val repository: ReminderRepository,
    private val scheduler: ReminderScheduler,
    private val clock: Clock,
    private val logActivity: LogActivityUseCase,
) {
    suspend operator fun invoke(id: Long) {
        val reminder = repository.getById(id) ?: return
        val next = calculateFollowingTrigger(reminder, clock.instant(), clock.zone)
        val updated = reminder.copy(
            status = if (next == null) ReminderStatus.SKIPPED else ReminderStatus.ACTIVE,
            nextTriggerAt = next,
            updatedAt = clock.instant(),
        )
        repository.save(updated)
        if (next == null) scheduler.cancel(id) else scheduler.schedule(updated)
        logActivity(ActivityEventType.REMINDER_SKIPPED, id, "Reminder skipped", reminder.title)
    }
}

fun validateReminder(input: ReminderInput, clock: Clock): ReminderValidation {
    val trigger = runCatching { calculateFirstTrigger(input, clock) }.getOrNull()
    return ReminderValidation(
        titleError = input.title.isBlank(),
        scheduleError = trigger == null,
        intervalError = input.repeatType == ReminderRepeatType.INTERVAL && input.intervalHours !in 1..24,
    )
}

fun calculateFirstTrigger(input: ReminderInput, clock: Clock): Instant {
    val now = clock.instant()
    val candidate = input.startDate.atTime(input.startTime).atZone(clock.zone).toInstant()
    require(candidate.isAfter(now))
    if (input.repeatType == ReminderRepeatType.INTERVAL) {
        require(input.intervalHours in 1..24)
    }
    return candidate
}

fun calculateFollowingTrigger(reminder: Reminder, now: Instant, zone: ZoneId): Instant? {
    if (reminder.repeatType == ReminderRepeatType.ONE_TIME) return null
    var candidate = reminder.nextTriggerAt ?: reminder.scheduledAt
    while (!candidate.isAfter(now)) {
        candidate = when (reminder.repeatType) {
            ReminderRepeatType.DAILY -> candidate.plusLocalDays(1, zone)
            ReminderRepeatType.WEEKLY -> candidate.plusLocalDays(7, zone)
            ReminderRepeatType.INTERVAL -> candidate.plus(Duration.ofHours(reminder.intervalHours?.toLong() ?: 1L))
            ReminderRepeatType.ONE_TIME -> return null
        }
    }
    return candidate
}

private fun Instant.plusLocalDays(days: Long, zone: ZoneId): Instant = atZone(zone).plusDays(days).toInstant()
