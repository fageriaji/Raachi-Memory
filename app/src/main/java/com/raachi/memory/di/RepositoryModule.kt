package com.raachi.memory.di

import com.raachi.memory.data.profile.OfflineProfileRepository
import com.raachi.memory.data.reminder.OfflineReminderRepository
import com.raachi.memory.data.reminder.AlarmReminderScheduler
import com.raachi.memory.data.ledger.OfflineLedgerRepository
import com.raachi.memory.data.ledger.AlarmLedgerAlertScheduler
import com.raachi.memory.data.settings.ProtoAppSettingsRepository
import com.raachi.memory.domain.repository.AppSettingsRepository
import com.raachi.memory.domain.repository.AppLockRepository
import com.raachi.memory.domain.repository.ProfileRepository
import com.raachi.memory.domain.repository.ReminderRepository
import com.raachi.memory.domain.repository.ReminderScheduler
import com.raachi.memory.domain.repository.LedgerRepository
import com.raachi.memory.domain.repository.LedgerAlertScheduler
import com.raachi.memory.data.activity.OfflineActivityRepository
import com.raachi.memory.domain.repository.ActivityRepository
import com.raachi.memory.data.expense.OfflineExpenseRepository
import com.raachi.memory.domain.repository.ExpenseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        repository: OfflineProfileRepository,
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindReminderRepository(
        repository: OfflineReminderRepository,
    ): ReminderRepository

    @Binds
    @Singleton
    abstract fun bindReminderScheduler(
        scheduler: AlarmReminderScheduler,
    ): ReminderScheduler

    @Binds
    @Singleton
    abstract fun bindLedgerRepository(
        repository: OfflineLedgerRepository,
    ): LedgerRepository

    @Binds
    @Singleton
    abstract fun bindLedgerAlertScheduler(
        scheduler: AlarmLedgerAlertScheduler,
    ): LedgerAlertScheduler

    @Binds
    @Singleton
    abstract fun bindActivityRepository(
        repository: OfflineActivityRepository,
    ): ActivityRepository

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        repository: OfflineExpenseRepository,
    ): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindAppSettingsRepository(
        repository: ProtoAppSettingsRepository,
    ): AppSettingsRepository

    @Binds
    @Singleton
    abstract fun bindAppLockRepository(
        repository: ProtoAppSettingsRepository,
    ): AppLockRepository
}
