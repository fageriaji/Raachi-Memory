package com.raachi.memory.domain

import com.raachi.memory.domain.model.Reminder
import com.raachi.memory.domain.model.AppPreferences
import com.raachi.memory.domain.model.ReminderCategory
import com.raachi.memory.domain.model.ReminderInput
import com.raachi.memory.domain.model.ReminderRepeatType
import com.raachi.memory.domain.model.ReminderStatus
import com.raachi.memory.domain.usecase.calculateFirstTrigger
import com.raachi.memory.domain.usecase.calculateFollowingTrigger
import com.raachi.memory.domain.usecase.validateReminder
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderUseCasesTest {
    private val clock = Clock.fixed(Instant.parse("2026-07-29T10:00:00Z"), ZoneOffset.UTC)

    @Test
    fun oneTimeReminder_requiresFutureDateAndTime() {
        val input = ReminderInput(
            title = "Appointment",
            repeatType = ReminderRepeatType.ONE_TIME,
            startDate = LocalDate.of(2026, 7, 29),
            startTime = LocalTime.of(9, 0),
        )

        assertFalse(validateReminder(input, clock).isValid)
        assertTrue(validateReminder(input.copy(startTime = LocalTime.of(11, 0)), clock).isValid)
    }

    @Test
    fun dailyReminder_startsAtSelectedFutureDateAndTime() {
        val trigger = calculateFirstTrigger(
            ReminderInput(
                title = "Water",
                repeatType = ReminderRepeatType.DAILY,
                startDate = LocalDate.of(2026, 7, 30),
                startTime = LocalTime.of(8, 0),
            ),
            clock,
        )

        assertEquals(Instant.parse("2026-07-30T08:00:00Z"), trigger)
    }

    @Test
    fun weeklyReminder_startsAtSelectedFutureDate() {
        val trigger = calculateFirstTrigger(
            ReminderInput(
                title = "Review",
                repeatType = ReminderRepeatType.WEEKLY,
                startDate = LocalDate.of(2026, 8, 5),
                startTime = LocalTime.of(9, 0),
            ),
            clock,
        )

        assertEquals(Instant.parse("2026-08-05T09:00:00Z"), trigger)
    }

    @Test
    fun intervalReminder_advancesFromSelectedStartTime() {
        val trigger = calculateFirstTrigger(
            ReminderInput(
                title = "Water",
                repeatType = ReminderRepeatType.INTERVAL,
                intervalHours = 2,
                startDate = LocalDate.of(2026, 7, 29),
                startTime = LocalTime.of(12, 0),
            ),
            clock,
        )

        assertEquals(Instant.parse("2026-07-29T12:00:00Z"), trigger)
    }

    @Test
    fun intervalReminder_skipsDefaultQuietHoursAndPreservesCadence() {
        val trigger = calculateFirstTrigger(
            ReminderInput(
                title = "Water",
                repeatType = ReminderRepeatType.INTERVAL,
                intervalHours = 2,
                startDate = LocalDate.of(2026, 7, 29),
                startTime = LocalTime.of(23, 0),
            ),
            clock,
        )

        assertEquals(Instant.parse("2026-07-30T07:00:00Z"), trigger)
    }

    @Test
    fun intervalReminder_canExplicitlyAlertDuringQuietHours() {
        val trigger = calculateFirstTrigger(
            ReminderInput(
                title = "Critical medicine",
                repeatType = ReminderRepeatType.INTERVAL,
                intervalHours = 2,
                startDate = LocalDate.of(2026, 7, 29),
                startTime = LocalTime.of(23, 0),
                allowDuringQuietHours = true,
            ),
            clock,
        )

        assertEquals(Instant.parse("2026-07-29T23:00:00Z"), trigger)
    }

    @Test
    fun dailyReminder_isNotChangedByQuietHours() {
        val trigger = calculateFirstTrigger(
            ReminderInput(
                title = "Daily medicine",
                repeatType = ReminderRepeatType.DAILY,
                startDate = LocalDate.of(2026, 7, 29),
                startTime = LocalTime.of(23, 0),
            ),
            clock,
        )

        assertEquals(Instant.parse("2026-07-29T23:00:00Z"), trigger)
    }

    @Test
    fun intervalReminder_usesUserConfiguredQuietHours() {
        val trigger = calculateFirstTrigger(
            ReminderInput(
                title = "Water",
                repeatType = ReminderRepeatType.INTERVAL,
                intervalHours = 2,
                startDate = LocalDate.of(2026, 7, 29),
                startTime = LocalTime.of(23, 0),
            ),
            clock,
            AppPreferences(quietHoursStartMinutes = 22 * 60, quietHoursEndMinutes = 8 * 60),
        )

        assertEquals(Instant.parse("2026-07-30T09:00:00Z"), trigger)
    }

    @Test
    fun intervalReminder_supportsDaytimeQuietHours() {
        val trigger = calculateFirstTrigger(
            ReminderInput(
                title = "Focus break",
                repeatType = ReminderRepeatType.INTERVAL,
                intervalHours = 1,
                startDate = LocalDate.of(2026, 7, 29),
                startTime = LocalTime.of(13, 0),
            ),
            clock,
            AppPreferences(quietHoursStartMinutes = 12 * 60, quietHoursEndMinutes = 15 * 60),
        )

        assertEquals(Instant.parse("2026-07-29T15:00:00Z"), trigger)
    }

    @Test
    fun quietHoursEnd_isAnAllowedTriggerBoundary() {
        val trigger = calculateFirstTrigger(
            ReminderInput(
                title = "Water",
                repeatType = ReminderRepeatType.INTERVAL,
                intervalHours = 2,
                startDate = LocalDate.of(2026, 7, 30),
                startTime = LocalTime.of(6, 0),
            ),
            clock,
        )

        assertEquals(Instant.parse("2026-07-30T06:00:00Z"), trigger)
    }

    @Test
    fun twentyFourHourInterval_resumesAtQuietHoursEndWhenCadenceCannotEscape() {
        val trigger = calculateFirstTrigger(
            ReminderInput(
                title = "Daily interval",
                repeatType = ReminderRepeatType.INTERVAL,
                intervalHours = 24,
                startDate = LocalDate.of(2026, 7, 30),
                startTime = LocalTime.MIDNIGHT,
            ),
            clock,
        )

        assertEquals(Instant.parse("2026-07-30T06:00:00Z"), trigger)
    }

    @Test
    fun disabledQuietHours_keepTheSelectedIntervalTime() {
        val trigger = calculateFirstTrigger(
            ReminderInput(
                title = "Water",
                repeatType = ReminderRepeatType.INTERVAL,
                intervalHours = 2,
                startDate = LocalDate.of(2026, 7, 29),
                startTime = LocalTime.of(23, 0),
            ),
            clock,
            AppPreferences(quietHoursEnabled = false),
        )

        assertEquals(Instant.parse("2026-07-29T23:00:00Z"), trigger)
    }

    @Test
    fun everyRepeatType_rejectsSelectedPastDateAndTime() {
        ReminderRepeatType.entries.forEach { repeatType ->
            val validation = validateReminder(
                ReminderInput(
                    title = "Reminder",
                    repeatType = repeatType,
                    intervalHours = 2,
                    startDate = LocalDate.of(2026, 7, 29),
                    startTime = LocalTime.of(9, 59),
                ),
                clock,
            )

            assertFalse("Expected $repeatType to reject a past start", validation.isValid)
            assertTrue(validation.scheduleError)
        }
    }

    @Test
    fun skipRecurringReminder_findsNextFutureOccurrence() {
        val reminder = Reminder(
            id = 7,
            title = "Medicine",
            category = ReminderCategory.MEDICINE,
            repeatType = ReminderRepeatType.DAILY,
            scheduledAt = Instant.parse("2026-07-28T08:00:00Z"),
            nextTriggerAt = Instant.parse("2026-07-29T08:00:00Z"),
            status = ReminderStatus.ACTIVE,
            createdAt = Instant.parse("2026-07-01T00:00:00Z"),
            updatedAt = Instant.parse("2026-07-01T00:00:00Z"),
        )

        assertEquals(
            Instant.parse("2026-07-30T08:00:00Z"),
            calculateFollowingTrigger(reminder, clock.instant(), ZoneOffset.UTC),
        )
    }

    @Test
    fun followingIntervalTrigger_skipsQuietOccurrencesWithoutLoggingSkips() {
        val reminder = Reminder(
            id = 10,
            title = "Water",
            category = ReminderCategory.WATER,
            repeatType = ReminderRepeatType.INTERVAL,
            intervalHours = 2,
            scheduledAt = Instant.parse("2026-07-29T09:00:00Z"),
            nextTriggerAt = Instant.parse("2026-07-29T21:00:00Z"),
            status = ReminderStatus.ACTIVE,
            createdAt = Instant.parse("2026-07-01T00:00:00Z"),
            updatedAt = Instant.parse("2026-07-01T00:00:00Z"),
        )

        assertEquals(
            Instant.parse("2026-07-30T07:00:00Z"),
            calculateFollowingTrigger(reminder, Instant.parse("2026-07-29T21:01:00Z"), ZoneOffset.UTC),
        )
    }

    @Test
    fun dailyReminder_preservesLocalTimeAcrossDaylightSavingChange() {
        val zone = ZoneId.of("America/New_York")
        val reminder = Reminder(
            id = 8,
            title = "Morning medicine",
            category = ReminderCategory.MEDICINE,
            repeatType = ReminderRepeatType.DAILY,
            scheduledAt = Instant.parse("2026-03-07T13:00:00Z"),
            nextTriggerAt = Instant.parse("2026-03-07T13:00:00Z"),
            status = ReminderStatus.ACTIVE,
            createdAt = Instant.parse("2026-03-01T00:00:00Z"),
            updatedAt = Instant.parse("2026-03-01T00:00:00Z"),
        )

        assertEquals(
            Instant.parse("2026-03-08T12:00:00Z"),
            calculateFollowingTrigger(reminder, Instant.parse("2026-03-07T14:00:00Z"), zone),
        )
    }

    @Test
    fun oneTimeReminder_hasNoFollowingTrigger() {
        val reminder = Reminder(
            id = 9,
            title = "Appointment",
            category = ReminderCategory.CUSTOM,
            repeatType = ReminderRepeatType.ONE_TIME,
            scheduledAt = clock.instant(),
            nextTriggerAt = clock.instant(),
            status = ReminderStatus.ACTIVE,
            createdAt = clock.instant(),
            updatedAt = clock.instant(),
        )

        assertEquals(null, calculateFollowingTrigger(reminder, clock.instant(), clock.zone))
    }
}
