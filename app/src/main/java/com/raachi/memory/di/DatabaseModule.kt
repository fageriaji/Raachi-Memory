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
    fun provideDatabase(
        @ApplicationContext context: Context
    ): RaachiDatabase {
        return Room.databaseBuilder(
            context,
            RaachiDatabase::class.java,
            RaachiDatabase.DATABASE_NAME
        )
            .addMigrations(
                RaachiDatabase.MIGRATION_1_2,
                RaachiDatabase.MIGRATION_2_3
            )
            .build()
    }

    @Provides
    fun provideUserDao(database: RaachiDatabase) = database.userDao

    @Provides
    fun provideSettingsDao(database: RaachiDatabase) = database.settingsDao

    @Provides
    fun provideReminderDao(database: RaachiDatabase) = database.reminderDao

    @Provides
    fun provideLedgerDao(database: RaachiDatabase) = database.ledgerDao

    @Provides
    fun provideActivityDao(database: RaachiDatabase) = database.activityDao
}