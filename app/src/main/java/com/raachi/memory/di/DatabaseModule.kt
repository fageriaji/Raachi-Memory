package com.raachi.memory.di

import android.content.Context
import androidx.room.Room
import com.raachi.memory.data.local.RaachiDatabase
import com.raachi.memory.data.local.dao.SettingsDao
import com.raachi.memory.data.local.dao.UserDao
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
        ).build()
    }

    @Provides
    @Singleton
    fun provideUserDao(
        database: RaachiDatabase
    ): UserDao {
        return database.userDao
    }

    @Provides
    @Singleton
    fun provideSettingsDao(
        database: RaachiDatabase
    ): SettingsDao {
        return database.settingsDao
    }
}