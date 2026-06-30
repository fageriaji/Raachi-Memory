package com.raachi.memory.di

import android.app.AlarmManager
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

/**
 * Provides Android framework services used by the app foundation.
 */
@Module
@InstallIn(SingletonComponent::class)
object FoundationModule {
    @Provides
    fun provideAlarmManager(
        @ApplicationContext context: Context
    ): AlarmManager {
        return context.getSystemService(AlarmManager::class.java)
    }
}
