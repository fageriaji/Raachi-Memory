package com.raachi.memory.domain

import com.raachi.memory.domain.model.ExpenseAccount
import com.raachi.memory.domain.model.ExpenseAccountInput
import com.raachi.memory.domain.model.ExpenseAccountType
import com.raachi.memory.domain.model.ExpenseCategory
import com.raachi.memory.domain.model.ExpenseTransaction
import com.raachi.memory.domain.model.ExpenseTransactionInput
import com.raachi.memory.domain.model.ExpenseTransactionType
import com.raachi.memory.domain.model.currentBalance
import com.raachi.memory.domain.repository.ExpenseRepository
import com.raachi.memory.domain.usecase.SaveExpenseAccountUseCase
import com.raachi.memory.domain.usecase.SaveExpenseTransactionUseCase
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpenseUseCasesTest {
    private val now = Instant.parse("2026-08-01T10:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Test
    fun balances_includeCreditsDebitsAndTransfersWithoutChangingCombinedTransferValue() {
        val bank = account(1, "Bank", 100_000)
        val cash = account(2, "Cash", 5_000)
        val transactions = listOf(
            transaction(1, ExpenseTransactionType.CREDIT, 20_000, destination = 1),
            transaction(2, ExpenseTransactionType.DEBIT, 15_000, source = 1),
            transaction(3, ExpenseTransactionType.TRANSFER, 10_000, source = 1, destination = 2),
        )

        assertEquals(95_000, bank.currentBalance(transactions))
        assertEquals(15_000, cash.currentBalance(transactions))
        assertEquals(110_000, bank.currentBalance(transactions) + cash.currentBalance(transactions))
    }

    @Test
    fun debitAboveAvailableBalance_isRejected() = runBlocking {
        val repository = FakeExpenseRepository(accounts = mutableListOf(account(1, "Bank", 10_000)))
        val validation = SaveExpenseTransactionUseCase(repository, clock)(
            ExpenseTransactionInput(
                type = ExpenseTransactionType.DEBIT,
                amount = "101",
                sourceAccountId = 1,
                category = ExpenseCategory.FOOD,
            ),
        )

        assertTrue(validation.insufficientFundsError)
        assertTrue(repository.transactions.isEmpty())
    }

    @Test
    fun transferToSameAccount_isRejected() = runBlocking {
        val repository = FakeExpenseRepository(accounts = mutableListOf(account(1, "Bank", 10_000)))
        val validation = SaveExpenseTransactionUseCase(repository, clock)(
            ExpenseTransactionInput(
                type = ExpenseTransactionType.TRANSFER,
                amount = "10",
                sourceAccountId = 1,
                destinationAccountId = 1,
                category = ExpenseCategory.OTHER,
            ),
        )

        assertTrue(validation.sameAccountError)
        assertFalse(validation.isValid)
    }

    @Test
    fun accountOpeningBalance_isStoredInPaise() = runBlocking {
        val repository = FakeExpenseRepository()
        val validation = SaveExpenseAccountUseCase(repository, clock)(
            ExpenseAccountInput(name = "SBI", type = ExpenseAccountType.BANK, openingBalance = "1234.50"),
        )

        assertTrue(validation.isValid)
        assertEquals(123_450, repository.accounts.single().openingBalancePaise)
    }

    private fun account(id: Long, name: String, opening: Long) = ExpenseAccount(
        id = id,
        name = name,
        type = ExpenseAccountType.BANK,
        openingBalancePaise = opening,
        colorValue = 0xFF142B85,
        createdAt = now,
        updatedAt = now,
    )

    private fun transaction(
        id: Long,
        type: ExpenseTransactionType,
        amount: Long,
        source: Long? = null,
        destination: Long? = null,
    ) = ExpenseTransaction(
        id = id,
        type = type,
        amountPaise = amount,
        sourceAccountId = source,
        destinationAccountId = destination,
        category = ExpenseCategory.OTHER,
        paymentMethod = null,
        transactionDate = LocalDate.of(2026, 8, 1),
        transactionTimeMinutes = null,
        note = null,
        createdAt = now,
        updatedAt = now,
    )
}

private class FakeExpenseRepository(
    val accounts: MutableList<ExpenseAccount> = mutableListOf(),
    val transactions: MutableList<ExpenseTransaction> = mutableListOf(),
) : ExpenseRepository {
    override fun observeActiveAccounts(): Flow<List<ExpenseAccount>> = flowOf(accounts.filterNot { it.isArchived })
    override fun observeTransactions(): Flow<List<ExpenseTransaction>> = flowOf(transactions)
    override suspend fun getAllAccounts(): List<ExpenseAccount> = accounts
    override suspend fun getAllTransactions(): List<ExpenseTransaction> = transactions
    override suspend fun getAccountById(id: Long): ExpenseAccount? = accounts.find { it.id == id }
    override suspend fun getTransactionById(id: Long): ExpenseTransaction? = transactions.find { it.id == id }
    override suspend fun saveAccount(account: ExpenseAccount): Long {
        val saved = account.copy(id = account.id.takeIf { it != 0L } ?: ((accounts.maxOfOrNull { it.id } ?: 0) + 1))
        accounts.removeAll { it.id == saved.id }
        accounts += saved
        return saved.id
    }
    override suspend fun saveTransaction(transaction: ExpenseTransaction): Long {
        val saved = transaction.copy(id = transaction.id.takeIf { it != 0L } ?: ((transactions.maxOfOrNull { it.id } ?: 0) + 1))
        transactions.removeAll { it.id == saved.id }
        transactions += saved
        return saved.id
    }
    override suspend fun deleteTransaction(id: Long) { transactions.removeAll { it.id == id } }
}
