package com.raachi.memory.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.raachi.memory.data.profile.UserProfileDao
import com.raachi.memory.data.profile.UserProfileEntity
import com.raachi.memory.data.reminder.ReminderDao
import com.raachi.memory.data.reminder.ReminderEntity
import com.raachi.memory.data.ledger.LedgerDao
import com.raachi.memory.data.ledger.LedgerEntryEntity
import com.raachi.memory.data.activity.ActivityDao
import com.raachi.memory.data.activity.ActivityLogEntity
import com.raachi.memory.data.expense.ExpenseAccountEntity
import com.raachi.memory.data.expense.ExpenseDao
import com.raachi.memory.data.expense.ExpenseTransactionEntity

@Database(
    entities = [
        UserProfileEntity::class,
        ReminderEntity::class,
        LedgerEntryEntity::class,
        ActivityLogEntity::class,
        ExpenseAccountEntity::class,
        ExpenseTransactionEntity::class,
    ],
    version = 6,
    exportSchema = true,
)
abstract class RaachiDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao

    abstract fun reminderDao(): ReminderDao

    abstract fun ledgerDao(): LedgerDao

    abstract fun activityDao(): ActivityDao

    abstract fun expenseDao(): ExpenseDao
}
