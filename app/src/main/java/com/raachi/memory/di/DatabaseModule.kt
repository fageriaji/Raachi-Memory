package com.raachi.memory.di

import android.content.Context
import androidx.room.Room
import com.raachi.memory.data.local.RaachiDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RaachiDatabase {
        return Room.databaseBuilder(
            context,
            RaachiDatabase::class.java,
            RaachiDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideUserDao(db: RaachiDatabase) = db.userDao

    @Provides
    fun provideSettingsDao(db: RaachiDatabase) = db.settingsDao

    @Provides
    fun provideReminderDao(db: RaachiDatabase) = db.reminderDao

    @Provides
    fun provideLedgerDao(db: RaachiDatabase) = db.ledgerDao

    @Provides
    fun provideActivityDao(db: RaachiDatabase) = db.activityDao
}