package com.raachi.memory

import android.app.Application
import com.raachi.memory.data.reminder.createReminderNotificationChannel
import com.raachi.memory.data.ledger.createLedgerNotificationChannel
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RaachiMemoryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createReminderNotificationChannel(this)
        createLedgerNotificationChannel(this)
    }
}
