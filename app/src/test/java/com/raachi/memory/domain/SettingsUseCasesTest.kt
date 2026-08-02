package com.raachi.memory.domain

import com.raachi.memory.domain.model.ActivityLog
import com.raachi.memory.domain.model.AppPreferences
import com.raachi.memory.domain.model.Reminder
import com.raachi.memory.domain.model.ReminderCategory
import com.raachi.memory.domain.model.ReminderRepeatType
import com.raachi.memory.domain.model.ReminderStatus
import com.raachi.memory.domain.model.ThemeMode
import com.raachi.memory.domain.repository.ActivityRepository
import com.raachi.memory.domain.repository.AppSettingsRepository
import com.raachi.memory.domain.repository.ReminderRepository
import com.raachi.memory.domain.repository.ReminderScheduler
import com.raachi.memory.domain.usecase.LogActivityUseCase
import com.raachi.memory.domain.usecase.SnoozeReminderUseCase
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsUseCasesTest {
    private val clock = Clock.fixed(Instant.parse("2026-07-30T10:00:00Z"), ZoneOffset.UTC)

    @Test
    fun snoozeAction_usesConfiguredDefaultDuration() = runTest {
        val reminder = Reminder(
            id = 9,
            title = "Medicine",
            category = ReminderCategory.MEDICINE,
            repeatType = ReminderRepeatType.DAILY,
            scheduledAt = clock.instant(),
            nextTriggerAt = clock.instant(),
            status = ReminderStatus.ACTIVE,
            createdAt = clock.instant(),
            updatedAt = clock.instant(),
        )
        val repository = SnoozeReminderRepository(reminder)
        val settings = SnoozeSettingsRepository(AppPreferences(defaultSnoozeMinutes = 30))
        val activityRepository = SnoozeActivityRepository()
        val useCase = SnoozeReminderUseCase(
            repository,
            SnoozeScheduler(),
            clock,
            LogActivityUseCase(activityRepository, clock),
            settings,
        )

        useCase(reminder.id)

        assertEquals(Instant.parse("2026-07-30T10:30:00Z"), repository.saved.nextTriggerAt)
        assertEquals("Medicine for 30 minutes", activityRepository.saved?.description)
    }
}

private class SnoozeReminderRepository(initial: Reminder) : ReminderRepository {
    var saved = initial
    override fun observeAll(): Flow<List<Reminder>> = flowOf(listOf(saved))
    override fun observeById(id: Long): Flow<Reminder?> = flowOf(saved.takeIf { it.id == id })
    override suspend fun getById(id: Long): Reminder? = saved.takeIf { it.id == id }
    override suspend fun getScheduled(): List<Reminder> = listOf(saved)
    override suspend fun save(reminder: Reminder): Long { saved = reminder; return reminder.id }
    override suspend fun delete(id: Long) = Unit
}

private class SnoozeScheduler : ReminderScheduler {
    override fun schedule(reminder: Reminder) = Unit
    override fun cancel(reminderId: Long) = Unit
}

private class SnoozeActivityRepository : ActivityRepository {
    var saved: ActivityLog? = null
    override fun observeAll(): Flow<List<ActivityLog>> = flowOf(emptyList())
    override suspend fun save(activity: ActivityLog): Long { saved = activity; return 1L }
}

private class SnoozeSettingsRepository(initial: AppPreferences) : AppSettingsRepository {
    private val state = MutableStateFlow(initial)
    override val preferences: Flow<AppPreferences> = state
    override val onboardingCompleted: Flow<Boolean> = state.map { it.onboardingCompleted }
    override suspend fun setOnboardingCompleted(completed: Boolean) { state.value = state.value.copy(onboardingCompleted = completed) }
    override suspend fun setThemeMode(mode: ThemeMode) { state.value = state.value.copy(themeMode = mode) }
    override suspend fun setReminderSoundEnabled(enabled: Boolean) { state.value = state.value.copy(reminderSoundEnabled = enabled) }
    override suspend fun setDefaultSnoozeMinutes(minutes: Int) { state.value = state.value.copy(defaultSnoozeMinutes = minutes) }
    override suspend fun replacePreferences(preferences: AppPreferences) { state.value = preferences }
}
