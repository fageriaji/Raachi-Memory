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
class LedgerNotificationActionReceiver : BroadcastReceiver() {

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
        val action = intent.action ?: return
        val ledgerId = intent.getIntExtra(
            LedgerNotificationHelper.EXTRA_LEDGER_ID,
            INVALID_LEDGER_ID
        )

        if (ledgerId == INVALID_LEDGER_ID) {
            return
        }

        val pendingResult = goAsync()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val entry = ledgerRepository
                    .getEntryById(ledgerId)
                    .first()
                    ?: return@launch

                when (action) {
                    LedgerNotificationHelper.ACTION_MARK_RETURNED -> {
                        val now = System.currentTimeMillis()

                        ledgerRepository.updateEntry(
                            entry.copy(
                                status = LedgerStatus.RETURNED,
                                returnedDateTime = now,
                                snoozedUntil = null,
                                updatedAt = now
                            )
                        )

                        ledgerAlarmScheduler.cancel(entry.id)
                        ledgerNotificationHelper.cancelNotification(entry.id)
                    }

                    LedgerNotificationHelper.ACTION_SNOOZE -> {
                        if (entry.status != LedgerStatus.PENDING) {
                            ledgerAlarmScheduler.cancel(entry.id)
                            ledgerNotificationHelper.cancelNotification(entry.id)
                            return@launch
                        }

                        val snoozedEntry = entry.copy(
                            snoozedUntil = System.currentTimeMillis() +
                                    SNOOZE_DURATION_MILLIS,
                            updatedAt = System.currentTimeMillis()
                        )

                        ledgerRepository.updateEntry(snoozedEntry)
                        ledgerAlarmScheduler.schedule(snoozedEntry)
                        ledgerNotificationHelper.cancelNotification(entry.id)
                    }

                    LedgerNotificationHelper.ACTION_SHARE -> {
                        ledgerNotificationHelper.shareLedgerReminder(entry)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        const val INVALID_LEDGER_ID = -1
        const val SNOOZE_DURATION_MILLIS = 24L * 60L * 60L * 1000L
    }
}
