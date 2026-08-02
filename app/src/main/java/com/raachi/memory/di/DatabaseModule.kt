package com.raachi.memory.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.raachi.memory.data.database.RaachiDatabase
import com.raachi.memory.data.profile.UserProfileDao
import com.raachi.memory.data.reminder.ReminderDao
import com.raachi.memory.data.ledger.LedgerDao
import com.raachi.memory.data.activity.ActivityDao
import com.raachi.memory.data.expense.ExpenseDao
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
        @ApplicationContext context: Context,
    ): RaachiDatabase = Room.databaseBuilder(
        context,
        RaachiDatabase::class.java,
        "raachi_memory.db",
    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6).build()

    @Provides
    fun provideUserProfileDao(database: RaachiDatabase): UserProfileDao = database.userProfileDao()

    @Provides
    fun provideReminderDao(database: RaachiDatabase): ReminderDao = database.reminderDao()

    @Provides
    fun provideLedgerDao(database: RaachiDatabase): LedgerDao = database.ledgerDao()

    @Provides
    fun provideActivityDao(database: RaachiDatabase): ActivityDao = database.activityDao()

    @Provides
    fun provideExpenseDao(database: RaachiDatabase): ExpenseDao = database.expenseDao()
}

internal val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `reminders` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `title` TEXT NOT NULL,
                    `category` TEXT NOT NULL,
                    `description` TEXT,
                    `repeatType` TEXT NOT NULL,
                    `intervalHours` INTEGER,
                    `scheduledAtMillis` INTEGER NOT NULL,
                    `nextTriggerAtMillis` INTEGER,
                    `soundEnabled` INTEGER NOT NULL,
                    `vibrationEnabled` INTEGER NOT NULL,
                    `status` TEXT NOT NULL,
                    `createdAtMillis` INTEGER NOT NULL,
                    `updatedAtMillis` INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_nextTriggerAtMillis` ON `reminders` (`nextTriggerAtMillis`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_status` ON `reminders` (`status`)")
        }
    }

internal val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `ledger_entries` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `personName` TEXT NOT NULL,
                    `mobileNumber` TEXT,
                    `kind` TEXT NOT NULL,
                    `direction` TEXT NOT NULL,
                    `itemName` TEXT,
                    `amountPaise` INTEGER,
                    `dueDateEpochDay` INTEGER,
                    `isReturned` INTEGER NOT NULL,
                    `returnedAtMillis` INTEGER,
                    `notes` TEXT,
                    `createdAtMillis` INTEGER NOT NULL,
                    `updatedAtMillis` INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_ledger_entries_dueDateEpochDay` ON `ledger_entries` (`dueDateEpochDay`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_ledger_entries_isReturned` ON `ledger_entries` (`isReturned`)")
        }
    }

internal val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `activity_logs` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `eventType` TEXT NOT NULL,
                    `referenceId` INTEGER,
                    `title` TEXT NOT NULL,
                    `description` TEXT,
                    `eventTimeMillis` INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_activity_logs_eventTimeMillis` ON `activity_logs` (`eventTimeMillis`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_activity_logs_eventType` ON `activity_logs` (`eventType`)")
        }
}

internal val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `ledger_entries` ADD COLUMN `transactionDateEpochDay` INTEGER NOT NULL DEFAULT 0")
        db.execSQL(
            "UPDATE `ledger_entries` SET `transactionDateEpochDay` = CAST(`createdAtMillis` / 86400000 AS INTEGER)",
        )
    }
}

internal val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `expense_accounts` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `openingBalancePaise` INTEGER NOT NULL,
                `colorValue` INTEGER NOT NULL,
                `isArchived` INTEGER NOT NULL,
                `createdAtMillis` INTEGER NOT NULL,
                `updatedAtMillis` INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `expense_transactions` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `type` TEXT NOT NULL,
                `amountPaise` INTEGER NOT NULL,
                `sourceAccountId` INTEGER,
                `destinationAccountId` INTEGER,
                `category` TEXT NOT NULL,
                `paymentMethod` TEXT,
                `transactionDateEpochDay` INTEGER NOT NULL,
                `transactionTimeMinutes` INTEGER,
                `note` TEXT,
                `createdAtMillis` INTEGER NOT NULL,
                `updatedAtMillis` INTEGER NOT NULL,
                FOREIGN KEY(`sourceAccountId`) REFERENCES `expense_accounts`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT,
                FOREIGN KEY(`destinationAccountId`) REFERENCES `expense_accounts`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_expense_transactions_sourceAccountId` ON `expense_transactions` (`sourceAccountId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_expense_transactions_destinationAccountId` ON `expense_transactions` (`destinationAccountId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_expense_transactions_transactionDateEpochDay` ON `expense_transactions` (`transactionDateEpochDay`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_expense_transactions_type` ON `expense_transactions` (`type`)")
    }
}
