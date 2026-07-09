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
class LedgerBootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var ledgerRepository: LedgerRepository

    @Inject
    lateinit var ledgerAlarmScheduler: LedgerAlarmScheduler

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        val pendingResult = goAsync()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                ledgerRepository
                    .getAllEntries()
                    .first()
                    .filter { entry ->
                        entry.status == LedgerStatus.PENDING &&
                                entry.dueDateTime != null
                    }
                    .forEach(ledgerAlarmScheduler::schedule)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
