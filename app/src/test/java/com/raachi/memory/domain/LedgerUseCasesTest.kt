package com.raachi.memory.domain

import com.raachi.memory.domain.model.LedgerDirection
import com.raachi.memory.domain.model.ActivityLog
import com.raachi.memory.domain.repository.ActivityRepository
import com.raachi.memory.domain.model.LedgerEntry
import com.raachi.memory.domain.model.LedgerInput
import com.raachi.memory.domain.model.LedgerKind
import com.raachi.memory.domain.repository.LedgerRepository
import com.raachi.memory.domain.repository.LedgerAlertScheduler
import com.raachi.memory.domain.usecase.SaveLedgerEntryUseCase
import com.raachi.memory.domain.usecase.LogActivityUseCase
import com.raachi.memory.domain.usecase.ledgerShareMessage
import com.raachi.memory.domain.usecase.validateLedger
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import com.raachi.memory.data.ledger.calculateLedgerAlertTrigger
import com.raachi.memory.feature.ledger.normalizeContactMobile
import com.raachi.memory.data.ledger.calculateNextOverdueAlert
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LedgerUseCasesTest {

    @Test
    fun contactMobileIsNormalizedToValidatedTenDigits() {
        assertEquals("9876543210", normalizeContactMobile("+91 98765-43210"))
        assertEquals("8765432109", normalizeContactMobile("08765432109"))
        assertEquals(null, normalizeContactMobile("+1 212 555 0100"))
        assertEquals(null, normalizeContactMobile("12345"))
    }
    private val clock = Clock.fixed(Instant.parse("2026-07-30T08:00:00Z"), ZoneOffset.UTC)

    @Test
    fun moneyEntry_requiresPersonAndPositiveAmount() {
        assertFalse(validateLedger(LedgerInput(kind = LedgerKind.MONEY)).isValid)
        assertTrue(validateLedger(LedgerInput(personName = "Amit", amount = "500")).isValid)
    }

    @Test
    fun itemEntry_requiresItemNameInsteadOfAmount() {
        val missing = validateLedger(LedgerInput(personName = "Amit", kind = LedgerKind.ITEM))
        val valid = validateLedger(LedgerInput(personName = "Amit", kind = LedgerKind.ITEM, itemName = "Book"))

        assertTrue(missing.itemError)
        assertTrue(valid.isValid)
    }

    @Test
    fun optionalMobile_usesStrictTenDigitRule() {
        assertTrue(validateLedger(LedgerInput(personName = "Amit", amount = "1", mobileNumber = "9876543210")).isValid)
        assertTrue(validateLedger(LedgerInput(personName = "Amit", amount = "1", mobileNumber = "")).isValid)
        assertTrue(validateLedger(LedgerInput(personName = "Amit", amount = "1", mobileNumber = "4123456789")).mobileError)
    }

    @Test
    fun saveMoneyEntry_convertsRupeesToIntegerPaise() = runTest {
        val repository = FakeLedgerRepository()
        val scheduler = FakeLedgerScheduler()
        val activityRepository = FakeActivityRepository()
        val useCase = SaveLedgerEntryUseCase(repository, scheduler, clock, LogActivityUseCase(activityRepository, clock))

        val result = useCase(
            LedgerInput(
                personName = "Neha",
                amount = "1234.56",
                transactionDate = LocalDate.of(2026, 7, 29),
                dueDate = LocalDate.of(2026, 8, 1),
            ),
        )

        assertTrue(result.isValid)
        assertEquals(123456L, repository.saved?.amountPaise)
        assertEquals(LocalDate.of(2026, 7, 29), repository.saved?.transactionDate)
        assertEquals(repository.saved, scheduler.scheduled)
        assertEquals("Ledger entry created", activityRepository.saved?.title)
    }

    @Test
    fun overdue_requiresPastDueDateAndPendingStatus() {
        val entry = ledgerEntry(dueDate = LocalDate.of(2026, 7, 29))

        assertTrue(entry.isOverdue(LocalDate.of(2026, 7, 30)))
        assertFalse(entry.copy(isReturned = true).isOverdue(LocalDate.of(2026, 7, 30)))
    }

    @Test
    fun shareMessage_containsPersonSubjectAndFriendlyDueDate() {
        val message = ledgerShareMessage(
            ledgerEntry(
                personName = "Amit",
                amountPaise = 50000,
                dueDate = LocalDate.of(2026, 8, 5),
            ),
        )

        assertTrue(message.contains("Amit"))
        assertTrue(message.contains("₹500"))
        assertTrue(message.contains("05-08-2026"))
    }

    @Test
    fun ledgerAlert_usesNineAmForFutureDueDate() {
        val entry = ledgerEntry(dueDate = LocalDate.of(2026, 7, 30))

        assertEquals(
            Instant.parse("2026-07-30T09:00:00Z"),
            calculateLedgerAlertTrigger(entry, clock.instant(), ZoneOffset.UTC),
        )
    }

    @Test
    fun overdueEntry_alertsPromptlyThenSchedulesNextMorning() {
        val now = Instant.parse("2026-07-30T10:00:00Z")
        val entry = ledgerEntry(dueDate = LocalDate.of(2026, 7, 29))

        assertEquals(
            Instant.parse("2026-07-30T10:00:10Z"),
            calculateLedgerAlertTrigger(entry, now, ZoneOffset.UTC),
        )
        assertEquals(
            Instant.parse("2026-07-31T09:00:00Z"),
            calculateNextOverdueAlert(now, ZoneOffset.UTC),
        )
    }

    @Test
    fun dueEntryAtExactlyNineAm_alertsPromptlyInsteadOfWaitingADay() {
        val now = Instant.parse("2026-07-30T09:00:00Z")
        val entry = ledgerEntry(dueDate = LocalDate.of(2026, 7, 30))

        assertEquals(
            Instant.parse("2026-07-30T09:00:10Z"),
            calculateLedgerAlertTrigger(entry, now, ZoneOffset.UTC),
        )
    }

    private fun ledgerEntry(
        personName: String = "Amit",
        amountPaise: Long? = 10000,
        dueDate: LocalDate? = null,
    ) = LedgerEntry(
        id = 1,
        personName = personName,
        kind = LedgerKind.MONEY,
        direction = LedgerDirection.LENT,
        amountPaise = amountPaise,
        transactionDate = LocalDate.of(2026, 7, 25),
        dueDate = dueDate,
        createdAt = clock.instant(),
        updatedAt = clock.instant(),
    )
}

private class FakeActivityRepository : ActivityRepository {
    var saved: ActivityLog? = null
    override fun observeAll(): Flow<List<ActivityLog>> = flowOf(saved?.let(::listOf).orEmpty())
    override suspend fun save(activity: ActivityLog): Long {
        saved = activity.copy(id = 1L)
        return 1L
    }
}

private class FakeLedgerRepository : LedgerRepository {
    var saved: LedgerEntry? = null
    override fun observeAll(): Flow<List<LedgerEntry>> = flowOf(saved?.let(::listOf).orEmpty())
    override suspend fun getById(id: Long): LedgerEntry? = saved?.takeIf { it.id == id }
    override suspend fun getPendingDue(): List<LedgerEntry> = saved?.takeIf { !it.isReturned && it.dueDate != null }?.let(::listOf).orEmpty()
    override suspend fun save(entry: LedgerEntry): Long {
        saved = entry.copy(id = entry.id.takeIf { it != 0L } ?: 1L)
        return saved!!.id
    }
    override suspend fun delete(id: Long) { if (saved?.id == id) saved = null }
}

private class FakeLedgerScheduler : LedgerAlertScheduler {
    var scheduled: LedgerEntry? = null
    var cancelledId: Long? = null
    override fun schedule(entry: LedgerEntry) { scheduled = entry }
    override fun scheduleNextOverdue(entry: LedgerEntry) { scheduled = entry }
    override fun cancel(entryId: Long) { cancelledId = entryId }
}
