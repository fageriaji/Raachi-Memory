package com.raachi.memory.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.raachi.memory.data.local.converter.Converters
import com.raachi.memory.data.local.dao.ActivityDao
import com.raachi.memory.data.local.dao.LedgerDao
import com.raachi.memory.data.local.dao.ReminderDao
import com.raachi.memory.data.local.dao.SettingsDao
import com.raachi.memory.data.local.dao.UserDao
import com.raachi.memory.data.local.entity.ActivityLogEntity
import com.raachi.memory.data.local.entity.AppSettingsEntity
import com.raachi.memory.data.local.entity.LedgerEntryEntity
import com.raachi.memory.data.local.entity.ReminderEntity
import com.raachi.memory.data.local.entity.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class,
        ReminderEntity::class,
        LedgerEntryEntity::class,
        ActivityLogEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RaachiDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val reminderDao: ReminderDao
    abstract val ledgerDao: LedgerDao
    abstract val activityDao: ActivityDao
    abstract val settingsDao: SettingsDao

    companion object {
        const val DATABASE_NAME = "raachi_memory_db"
    }
}