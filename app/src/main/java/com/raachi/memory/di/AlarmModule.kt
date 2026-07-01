package com.raachi.memory.di

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import com.raachi.memory.core.alarm.AlarmScheduler
import com.raachi.memory.core.alarm.AlarmSchedulerImpl
import com.raachi.memory.core.alarm.NotificationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AlarmModule {

    // Removed provideAlarmManager because FoundationModule already provides it.
    // Hilt will automatically pull it from there.

    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @Provides
    @Singleton
    fun provideAlarmScheduler(
        @ApplicationContext context: Context,
        alarmManager: AlarmManager
    ): AlarmScheduler {
        return AlarmSchedulerImpl(context, alarmManager)
    }

    @Provides
    @Singleton
    fun provideNotificationHelper(
        @ApplicationContext context: Context,
        notificationManager: NotificationManager
    ): NotificationHelper {
        return NotificationHelper(context, notificationManager)
    }
}