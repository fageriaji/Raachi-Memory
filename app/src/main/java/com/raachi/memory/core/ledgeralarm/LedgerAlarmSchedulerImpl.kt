package com.raachi.memory.core.ledgeralarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.raachi.memory.domain.model.LedgerEntry
import com.raachi.memory.domain.model.LedgerStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

class LedgerAlarmSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManager: AlarmManager
) : LedgerAlarmScheduler {

    override fun schedule(entry: LedgerEntry) {
        cancel(entry.id)

        if (
            entry.status != LedgerStatus.PENDING ||
            entry.dueDateTime == null
        ) {
            return
        }

        val now = System.currentTimeMillis()
        val snoozedUntil = entry.snoozedUntil

        if (snoozedUntil != null && snoozedUntil > now) {
            scheduleAlarm(
                entryId = entry.id,
                triggerAtMillis = snoozedUntil,
                triggerType = LedgerAlarmReceiver.TRIGGER_SNOOZE
            )
            return
        }

        if (entry.dueDateTime > now) {
            scheduleAlarm(
                entryId = entry.id,
                triggerAtMillis = entry.dueDateTime,
                triggerType = LedgerAlarmReceiver.TRIGGER_DUE
            )
        }

        scheduleAlarm(
            entryId = entry.id,
            triggerAtMillis = calculateNextOverdueReminder(
                dueDateTime = entry.dueDateTime,
                nowMillis = now
            ),
            triggerType = LedgerAlarmReceiver.TRIGGER_OVERDUE
        )
    }

    override fun cancel(entryId: Int) {
        cancelAlarm(entryId, LedgerAlarmReceiver.TRIGGER_DUE)
        cancelAlarm(entryId, LedgerAlarmReceiver.TRIGGER_OVERDUE)
        cancelAlarm(entryId, LedgerAlarmReceiver.TRIGGER_SNOOZE)
    }

    @SuppressLint("MissingPermission")
    private fun scheduleAlarm(
        entryId: Int,
        triggerAtMillis: Long,
        triggerType: String
    ) {
        val pendingIntent = createPendingIntent(entryId, triggerType)

        try {
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                alarmManager.canScheduleExactAlarms()
            ) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    private fun cancelAlarm(
        entryId: Int,
        triggerType: String
    ) {
        alarmManager.cancel(createPendingIntent(entryId, triggerType))
    }

    private fun createPendingIntent(
        entryId: Int,
        triggerType: String
    ): PendingIntent {
        val intent = Intent(context, LedgerAlarmReceiver::class.java).apply {
            action = LedgerAlarmReceiver.ACTION_LEDGER_ALARM
            putExtra(LedgerAlarmReceiver.EXTRA_LEDGER_ID, entryId)
            putExtra(LedgerAlarmReceiver.EXTRA_TRIGGER_TYPE, triggerType)
        }

        return PendingIntent.getBroadcast(
            context,
            requestCodeFor(entryId, triggerType),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun requestCodeFor(
        entryId: Int,
        triggerType: String
    ): Int {
        val typeOffset = when (triggerType) {
            LedgerAlarmReceiver.TRIGGER_DUE -> 1
            LedgerAlarmReceiver.TRIGGER_OVERDUE -> 2
            LedgerAlarmReceiver.TRIGGER_SNOOZE -> 3
            else -> 0
        }

        return ALARM_REQUEST_CODE_OFFSET + (entryId * 10) + typeOffset
    }

    private fun calculateNextOverdueReminder(
        dueDateTime: Long,
        nowMillis: Long
    ): Long {
        val zoneId = ZoneId.systemDefault()
        val dueDateTimeZoned = Instant.ofEpochMilli(dueDateTime).atZone(zoneId)
        val nowZoned = Instant.ofEpochMilli(nowMillis).atZone(zoneId)

        var nextReminder = dueDateTimeZoned
            .toLocalDate()
            .atTime(LocalTime.of(9, 0))
            .atZone(zoneId)

        if (!nextReminder.toInstant().isAfter(dueDateTimeZoned.toInstant())) {
            nextReminder = nextReminder.plusDays(1)
        }

        while (!nextReminder.toInstant().isAfter(nowZoned.toInstant())) {
            nextReminder = nextReminder.plusDays(1)
        }

        return nextReminder.toInstant().toEpochMilli()
    }

    private companion object {
        const val ALARM_REQUEST_CODE_OFFSET = 200_000
    }
}
