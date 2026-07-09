package com.raachi.memory.core.ledgeralarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.raachi.memory.domain.model.LedgerStatus
import com.raachi.memory.domain.repository.LedgerRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LedgerAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var ledgerRepository: LedgerRepository

    @Inject
    lateinit var ledgerAlarmScheduler: LedgerAlarmScheduler

    @Inject
    lateinit var ledgerNotificationHelper: LedgerNotificationHelper

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val ledgerId = intent.getIntExtra(EXTRA_LEDGER_ID, INVALID_LEDGER_ID)
        val triggerType = intent.getStringExtra(EXTRA_TRIGGER_TYPE)

        if (
            intent.action != ACTION_LEDGER_ALARM ||
            ledgerId == INVALID_LEDGER_ID ||
            triggerType == null
        ) {
            return
        }

        val pendingResult = goAsync()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val entry = ledgerRepository
                    .getEntryById(ledgerId)
                    .first()
                    ?: return@launch

                if (
                    entry.status != LedgerStatus.PENDING ||
                    entry.dueDateTime == null
                ) {
                    ledgerAlarmScheduler.cancel(entry.id)
                    ledgerNotificationHelper.cancelNotification(entry.id)
                    return@launch
                }

                val now = System.currentTimeMillis()

                if (
                    triggerType != TRIGGER_SNOOZE &&
                    entry.snoozedUntil != null &&
                    entry.snoozedUntil > now
                ) {
                    ledgerAlarmScheduler.schedule(entry)
                    return@launch
                }

                val activeEntry = if (
                    triggerType == TRIGGER_SNOOZE &&
                    entry.snoozedUntil != null &&
                    entry.snoozedUntil <= now
                ) {
                    entry.copy(
                        snoozedUntil = null,
                        updatedAt = now
                    ).also { updatedEntry ->
                        ledgerRepository.updateEntry(updatedEntry)
                    }
                } else {
                    entry
                }

                ledgerNotificationHelper.showLedgerNotification(activeEntry)
                ledgerAlarmScheduler.schedule(activeEntry)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_LEDGER_ALARM =
            "com.raachi.memory.action.LEDGER_ALARM"

        const val EXTRA_LEDGER_ID = "LEDGER_ID"
        const val EXTRA_TRIGGER_TYPE = "LEDGER_TRIGGER_TYPE"

        const val TRIGGER_DUE = "DUE"
        const val TRIGGER_OVERDUE = "OVERDUE"
        const val TRIGGER_SNOOZE = "SNOOZE"

        private const val INVALID_LEDGER_ID = -1
    }
}