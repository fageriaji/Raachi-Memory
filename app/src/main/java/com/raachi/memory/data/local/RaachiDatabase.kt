package com.raachi.memory.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 3,
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

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE `ledger_entries_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `person_name` TEXT NOT NULL,
                        `mobile_number` TEXT,
                        `item_type` TEXT NOT NULL,
                        `item_name` TEXT,
                        `amount` REAL,
                        `borrow_date_time` INTEGER NOT NULL,
                        `due_date_time` INTEGER,
                        `status` TEXT NOT NULL,
                        `returned_date_time` INTEGER,
                        `notes` TEXT,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    INSERT INTO `ledger_entries_new` (
                        `id`,
                        `person_name`,
                        `mobile_number`,
                        `item_type`,
                        `item_name`,
                        `amount`,
                        `borrow_date_time`,
                        `due_date_time`,
                        `status`,
                        `returned_date_time`,
                        `notes`,
                        `created_at`,
                        `updated_at`
                    )
                    SELECT
                        `id`,
                        `person_name`,
                        `mobile_number`,
                        `item_type`,
                        `item_name`,
                        `amount`,
                        `created_at`,
                        `due_date`,
                        CASE WHEN `returned` THEN 'RETURNED' ELSE 'PENDING' END,
                        `returned_date`,
                        `notes`,
                        `created_at`,
                        `created_at`
                    FROM `ledger_entries`
                    """.trimIndent()
                )

                db.execSQL("DROP TABLE `ledger_entries`")
                db.execSQL(
                    "ALTER TABLE `ledger_entries_new` RENAME TO `ledger_entries`"
                )
                db.execSQL(
                    "CREATE INDEX `index_ledger_entries_due_date_time` ON `ledger_entries` (`due_date_time`)"
                )
                db.execSQL(
                    "CREATE INDEX `index_ledger_entries_status` ON `ledger_entries` (`status`)"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `ledger_entries` ADD COLUMN `snoozed_until` INTEGER"
                )
            }
        }
    }
}