package com.raachi.memory.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.raachi.memory.data.database.RaachiDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {
    private lateinit var context: Context
    private var database: RaachiDatabase? = null

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(DATABASE_NAME)
    }

    @After
    fun tearDown() {
        database?.close()
        context.deleteDatabase(DATABASE_NAME)
    }

    @Test
    fun migrationFromVersionOne_preservesProfileAndCreatesEveryFeatureTable() = runBlocking {
        createVersionOneDatabase()

        database = Room.databaseBuilder(context, RaachiDatabase::class.java, DATABASE_NAME)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
            .allowMainThreadQueries()
            .build()
        val migrated = requireNotNull(database)

        assertEquals("Mannu", migrated.userProfileDao().getProfile()?.name)
        assertEquals("9876543210", migrated.userProfileDao().getProfile()?.mobile)
        assertTrue(migrated.reminderDao().getAll().isEmpty())
        assertTrue(migrated.ledgerDao().getAll().isEmpty())
        assertTrue(migrated.activityDao().getAll().isEmpty())
        assertTrue(migrated.expenseDao().getAllAccounts().isEmpty())
        assertTrue(migrated.expenseDao().getAllTransactions().isEmpty())
    }

    private fun createVersionOneDatabase() {
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(DATABASE_NAME)
            .callback(
                object : SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS `user_profile` (
                                `id` INTEGER NOT NULL,
                                `name` TEXT NOT NULL,
                                `dateOfBirth` TEXT,
                                `mobile` TEXT,
                                `gender` TEXT,
                                `email` TEXT,
                                `heightCm` REAL,
                                `weightKg` REAL,
                                `profilePhotoUri` TEXT,
                                `createdAtMillis` INTEGER NOT NULL,
                                `updatedAtMillis` INTEGER NOT NULL,
                                PRIMARY KEY(`id`)
                            )
                            """.trimIndent(),
                        )
                        db.execSQL(
                            """
                            INSERT INTO `user_profile` (
                                `id`, `name`, `dateOfBirth`, `mobile`, `gender`, `email`,
                                `heightCm`, `weightKg`, `profilePhotoUri`, `createdAtMillis`, `updatedAtMillis`
                            ) VALUES (1, 'Mannu', '29-07-1995', '9876543210', 'MALE', 'mannu@gmail.com',
                                180.0, 80.0, NULL, 1000, 2000)
                            """.trimIndent(),
                        )
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                },
            )
            .build()
        FrameworkSQLiteOpenHelperFactory().create(configuration).use { helper ->
            helper.writableDatabase
        }
    }

    private companion object {
        const val DATABASE_NAME = "migration-test.db"
    }
}
