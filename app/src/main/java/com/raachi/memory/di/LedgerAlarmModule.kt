package com.raachi.memory.di

import com.raachi.memory.core.ledgeralarm.LedgerAlarmScheduler
import com.raachi.memory.core.ledgeralarm.LedgerAlarmSchedulerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LedgerAlarmModule {

    @Binds
    @Singleton
    abstract fun bindLedgerAlarmScheduler(
        implementation: LedgerAlarmSchedulerImpl
    ): LedgerAlarmScheduler
}