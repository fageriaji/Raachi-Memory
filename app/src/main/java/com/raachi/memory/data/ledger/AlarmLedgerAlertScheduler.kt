package com.raachi.memory.data.ledger

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.raachi.memory.domain.model.LedgerEntry
import com.raachi.memory.domain.repository.LedgerAlertScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

class AlarmLedgerAlertScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val clock: Clock,
) : LedgerAlertScheduler {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(entry: LedgerEntry) {
        val trigger = calculateLedgerAlertTrigger(entry, clock.instant(), clock.zone) ?: return
        scheduleAt(entry.id, trigger)
    }

    override fun scheduleNextOverdue(entry: LedgerEntry) {
        if (entry.isReturned || entry.dueDate == null) return
        scheduleAt(entry.id, calculateNextOverdueAlert(clock.instant(), clock.zone))
    }

    override fun cancel(entryId: Long) {
        alarmManager.cancel(ledgerAlertPendingIntent(context, entryId))
    }

    private fun scheduleAt(entryId: Long, trigger: Instant) {
        val pendingIntent = ledgerAlertPendingIntent(context, entryId)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger.toEpochMilli(), pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger.toEpochMilli(), pendingIntent)
        }
    }
}

fun calculateLedgerAlertTrigger(entry: LedgerEntry, now: Instant, zone: ZoneId): Instant? {
    val dueDate = entry.dueDate ?: return null
    if (entry.isReturned) return null
    val dueTime = dueDate.atTime(LEDGER_ALERT_TIME).atZone(zone).toInstant()
    return if (dueTime.isAfter(now)) dueTime else now.plus(Duration.ofSeconds(10))
}

fun calculateNextOverdueAlert(now: Instant, zone: ZoneId): Instant {
    val todayAtAlert = now.atZone(zone).toLocalDate().atTime(LEDGER_ALERT_TIME).atZone(zone).toInstant()
    return if (todayAtAlert.isAfter(now)) todayAtAlert else todayAtAlert.atZone(zone).plusDays(1).toInstant()
}

internal fun ledgerAlertPendingIntent(context: Context, entryId: Long): PendingIntent {
    val intent = Intent(context, LedgerAlertReceiver::class.java).apply {
        action = LedgerAlertReceiver.ACTION_TRIGGER
        putExtra(LedgerAlertReceiver.EXTRA_LEDGER_ID, entryId)
    }
    return PendingIntent.getBroadcast(
        context,
        ledgerRequestCode(entryId),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}

internal fun ledgerRequestCode(entryId: Long): Int = (entryId xor (entryId ushr 32)).toInt() xor 0x4C454447

private val LEDGER_ALERT_TIME = LocalTime.of(9, 0)
