package com.raachi.memory.domain.usecase

import com.raachi.memory.domain.model.ExpenseAccount
import com.raachi.memory.domain.model.ExpenseAccountInput
import com.raachi.memory.domain.model.ExpenseAccountValidation
import com.raachi.memory.domain.model.ExpenseTransaction
import com.raachi.memory.domain.model.ExpenseTransactionInput
import com.raachi.memory.domain.model.ExpenseTransactionType
import com.raachi.memory.domain.model.ExpenseTransactionValidation
import com.raachi.memory.domain.model.currentBalance
import com.raachi.memory.domain.repository.ExpenseRepository
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Clock
import javax.inject.Inject

class SaveExpenseAccountUseCase @Inject constructor(
    private val repository: ExpenseRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(input: ExpenseAccountInput): ExpenseAccountValidation {
        val openingBalance = input.openingBalance.toExpensePaise()
        val validation = ExpenseAccountValidation(
            nameError = input.name.isBlank(),
            balanceError = openingBalance == null || openingBalance < 0,
        )
        if (!validation.isValid) return validation
        val existing = input.id.takeIf { it != 0L }?.let { repository.getAccountById(it) }
        val now = clock.instant()
        repository.saveAccount(
            ExpenseAccount(
                id = input.id,
                name = input.name.trim(),
                type = input.type,
                openingBalancePaise = requireNotNull(openingBalance),
                colorValue = input.colorValue,
                isArchived = existing?.isArchived ?: false,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
            ),
        )
        return validation
    }
}

class ArchiveExpenseAccountUseCase @Inject constructor(
    private val repository: ExpenseRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(id: Long) {
        val account = repository.getAccountById(id) ?: return
        repository.saveAccount(account.copy(isArchived = true, updatedAt = clock.instant()))
    }
}

class SaveExpenseTransactionUseCase @Inject constructor(
    private val repository: ExpenseRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(input: ExpenseTransactionInput): ExpenseTransactionValidation {
        val amount = input.amount.toExpensePaise()
        val accounts = repository.getAllAccounts().filterNot(ExpenseAccount::isArchived)
        val source = input.sourceAccountId?.let { id -> accounts.find { it.id == id } }
        val destination = input.destinationAccountId?.let { id -> accounts.find { it.id == id } }
        val needsSource = input.type != ExpenseTransactionType.CREDIT
        val needsDestination = input.type != ExpenseTransactionType.DEBIT
        val sameAccount = input.type == ExpenseTransactionType.TRANSFER && source?.id == destination?.id
        val transactions = repository.getAllTransactions().filterNot { it.id == input.id }
        val insufficientFunds = needsSource && amount != null && amount > 0 &&
            source != null && source.currentBalance(transactions) < amount
        val validation = ExpenseTransactionValidation(
            amountError = amount == null || amount <= 0,
            sourceAccountError = needsSource && source == null,
            destinationAccountError = needsDestination && destination == null,
            sameAccountError = sameAccount,
            insufficientFundsError = insufficientFunds,
        )
        if (!validation.isValid) return validation
        val existing = input.id.takeIf { it != 0L }?.let { repository.getTransactionById(it) }
        val now = clock.instant()
        repository.saveTransaction(
            ExpenseTransaction(
                id = input.id,
                type = input.type,
                amountPaise = requireNotNull(amount),
                sourceAccountId = source?.id.takeIf { needsSource },
                destinationAccountId = destination?.id.takeIf { needsDestination },
                category = input.category,
                paymentMethod = input.paymentMethod.takeUnless { input.type == ExpenseTransactionType.TRANSFER },
                transactionDate = input.transactionDate,
                transactionTimeMinutes = input.transactionTimeMinutes,
                note = input.note.trim().ifBlank { null },
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
            ),
        )
        return validation
    }
}

class DeleteExpenseTransactionUseCase @Inject constructor(private val repository: ExpenseRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteTransaction(id)
}

fun String.toExpensePaise(): Long? = runCatching {
    BigDecimal(trim()).setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact()
}.getOrNull()
