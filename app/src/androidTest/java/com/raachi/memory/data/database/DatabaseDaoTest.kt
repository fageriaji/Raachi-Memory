package com.raachi.memory.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.raachi.memory.data.activity.ActivityLogEntity
import com.raachi.memory.data.ledger.LedgerEntryEntity
import com.raachi.memory.data.reminder.ReminderEntity
import com.raachi.memory.data.expense.ExpenseAccountEntity
import com.raachi.memory.data.expense.ExpenseTransactionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseDaoTest {
    private lateinit var database: RaachiDatabase

    @Before
    fun setUp() {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, RaachiDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun featureDaos_persistFilterAndOrderRecords() = runBlocking {
        database.reminderDao().upsertAll(
            listOf(
                reminder(id = 1, title = "Later", nextTrigger = 2_000),
                reminder(id = 2, title = "Sooner", nextTrigger = 1_000),
            ),
        )
        database.ledgerDao().upsertAll(
            listOf(
                ledger(id = 1, person = "Returned", returned = true, dueDate = 10),
                ledger(id = 2, person = "Pending", returned = false, dueDate = 11),
            ),
        )
        database.activityDao().insertAll(
            listOf(
                activity(id = 1, title = "Older", time = 1_000),
                activity(id = 2, title = "Newer", time = 2_000),
            ),
        )
        database.expenseDao().upsertAccounts(
            listOf(
                ExpenseAccountEntity(1, "Bank", "BANK", 100_000, 0xFF142B85, false, 100, 100),
                ExpenseAccountEntity(2, "Cash", "CASH", 5_000, 0xFFFF8A1F, false, 100, 100),
            ),
        )
        database.expenseDao().upsertTransactions(
            listOf(
                ExpenseTransactionEntity(
                    id = 1,
                    type = "TRANSFER",
                    amountPaise = 10_000,
                    sourceAccountId = 1,
                    destinationAccountId = 2,
                    category = "OTHER",
                    paymentMethod = null,
                    transactionDateEpochDay = 20,
                    transactionTimeMinutes = 600,
                    note = null,
                    createdAtMillis = 100,
                    updatedAtMillis = 100,
                ),
            ),
        )

        assertEquals(listOf("Sooner", "Later"), database.reminderDao().observeAll().first().map { it.title })
        assertEquals(listOf("Pending"), database.ledgerDao().getPendingDue().map { it.personName })
        assertEquals(listOf("Newer", "Older"), database.activityDao().observeAll().first().map { it.title })
        assertEquals(listOf("Bank", "Cash"), database.expenseDao().observeActiveAccounts().first().map { it.name })
        assertEquals("TRANSFER", database.expenseDao().getAllTransactions().single().type)

        database.reminderDao().deleteAll()
        database.ledgerDao().deleteAll()
        database.activityDao().deleteAll()
        database.expenseDao().deleteAllTransactions()
        database.expenseDao().deleteAllAccounts()
        assertTrue(database.reminderDao().getAll().isEmpty())
        assertTrue(database.ledgerDao().getAll().isEmpty())
        assertTrue(database.activityDao().getAll().isEmpty())
        assertTrue(database.expenseDao().getAllAccounts().isEmpty())
    }

    private fun reminder(id: Long, title: String, nextTrigger: Long) = ReminderEntity(
        id = id,
        title = title,
        category = "CUSTOM",
        description = null,
        repeatType = "ONE_TIME",
        intervalHours = null,
        scheduledAtMillis = nextTrigger,
        nextTriggerAtMillis = nextTrigger,
        soundEnabled = true,
        vibrationEnabled = true,
        status = "ACTIVE",
        createdAtMillis = 100,
        updatedAtMillis = 100,
    )

    private fun ledger(id: Long, person: String, returned: Boolean, dueDate: Long) = LedgerEntryEntity(
        id = id,
        personName = person,
        mobileNumber = null,
        kind = "MONEY",
        direction = "LENT",
        itemName = null,
        amountPaise = 10_000,
        transactionDateEpochDay = 5,
        dueDateEpochDay = dueDate,
        isReturned = returned,
        returnedAtMillis = null,
        notes = null,
        createdAtMillis = 100,
        updatedAtMillis = 100,
    )

    private fun activity(id: Long, title: String, time: Long) = ActivityLogEntity(
        id = id,
        eventType = "LEDGER_CREATED",
        referenceId = id,
        title = title,
        description = null,
        eventTimeMillis = time,
    )
}
