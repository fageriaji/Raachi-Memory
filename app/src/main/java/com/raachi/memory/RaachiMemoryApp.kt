package com.raachi.memory

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point for app-wide dependency injection and background work configuration.
 */
@HiltAndroidApp
class RaachiMemoryApp : Application(), Configuration.Provider {
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()
}
