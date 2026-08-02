package com.raachi.memory.data.ledger

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.raachi.memory.domain.repository.LedgerAlertScheduler
import com.raachi.memory.domain.repository.LedgerRepository
import com.raachi.memory.domain.model.ActivityEventType
import com.raachi.memory.domain.model.LedgerKind
import com.raachi.memory.domain.usecase.LogActivityUseCase
import com.raachi.memory.domain.usecase.MarkLedgerReturnedUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LedgerAlertReceiver : BroadcastReceiver() {
    @Inject lateinit var repository: LedgerRepository
    @Inject lateinit var scheduler: LedgerAlertScheduler
    @Inject lateinit var markReturned: MarkLedgerReturnedUseCase
    @Inject lateinit var logActivity: LogActivityUseCase

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(EXTRA_LEDGER_ID, 0L)
        if (id == 0L) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_TRIGGER -> {
                        val entry = repository.getById(id) ?: return@launch
                        if (!entry.isReturned && entry.dueDate != null) {
                            showLedgerNotification(context, entry)
                            val subject = if (entry.kind == LedgerKind.MONEY) {
                                "${entry.personName} - money due"
                            } else {
                                "${entry.personName} - ${entry.itemName.orEmpty()} due"
                            }
                            logActivity(ActivityEventType.LEDGER_ALERT_SENT, id, "Ledger alert sent", subject)
                            scheduler.scheduleNextOverdue(entry)
                        }
                    }
                    ACTION_MARK_RETURNED -> {
                        markReturned(id)
                        context.getSystemService(NotificationManager::class.java)
                            .cancel(ledgerNotificationId(id))
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_LEDGER_ID = "ledger_id"
        const val ACTION_TRIGGER = "com.raachi.memory.action.LEDGER_ALERT"
        const val ACTION_MARK_RETURNED = "com.raachi.memory.action.LEDGER_RETURNED"
    }
}
