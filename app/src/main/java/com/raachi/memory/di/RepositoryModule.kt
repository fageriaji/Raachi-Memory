package com.raachi.memory.di

import com.raachi.memory.data.local.dao.LedgerDao
import com.raachi.memory.data.local.dao.ReminderDao
import com.raachi.memory.data.local.dao.SettingsDao
import com.raachi.memory.data.local.dao.UserDao
import com.raachi.memory.data.repository.LedgerRepositoryImpl
import com.raachi.memory.data.repository.ReminderRepositoryImpl
import com.raachi.memory.data.repository.SettingsRepositoryImpl
import com.raachi.memory.data.repository.UserRepositoryImpl
import com.raachi.memory.domain.repository.LedgerRepository
import com.raachi.memory.domain.repository.ReminderRepository
import com.raachi.memory.domain.repository.SettingsRepository
import com.raachi.memory.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideSettingsRepository(settingsDao: SettingsDao): SettingsRepository {
        return SettingsRepositoryImpl(settingsDao)
    }

    @Provides
    @Singleton
    fun provideUserRepository(userDao: UserDao): UserRepository {
        return UserRepositoryImpl(userDao)
    }

    @Provides
    @Singleton
    fun provideReminderRepository(reminderDao: ReminderDao): ReminderRepository {
        return ReminderRepositoryImpl(reminderDao)
    }

    @Provides
    @Singleton
    fun provideLedgerRepository(ledgerDao: LedgerDao): LedgerRepository {
        return LedgerRepositoryImpl(ledgerDao)
    }
}