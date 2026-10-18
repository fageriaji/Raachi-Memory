package com.raachi.memory.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.raachi.memory.domain.repository.LedgerRepository
import com.raachi.memory.domain.repository.LedgerAlertScheduler
import com.raachi.memory.domain.usecase.ReconcileReminderSchedulesUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReminderBootReceiver : BroadcastReceiver() {
    @Inject lateinit var reconcileReminderSchedules: ReconcileReminderSchedulesUseCase
    @Inject lateinit var ledgerRepository: LedgerRepository
    @Inject lateinit var ledgerScheduler: LedgerAlertScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in SUPPORTED_ACTIONS) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                reconcileReminderSchedules()
                ledgerRepository.getPendingDue().forEach(ledgerScheduler::schedule)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        val SUPPORTED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
        )
    }
}
