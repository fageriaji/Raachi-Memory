package com.raachi.memory.domain.model

import java.time.Instant
import java.time.LocalDate

enum class ExpenseTransactionType { DEBIT, CREDIT, TRANSFER }

enum class ExpenseCategory {
    FOOD, GROCERIES, TRAVEL, BILLS, SHOPPING, HEALTH, EDUCATION, ENTERTAINMENT, RENT,
    SALARY, REFUND, INTEREST, GIFT, CASHBACK, OTHER,
}

enum class ExpensePaymentMethod { UPI, DEBIT_CARD, CASH, NET_BANKING, OTHER }

data class ExpenseTransaction(
    val id: Long = 0,
    val type: ExpenseTransactionType,
    val amountPaise: Long,
    val sourceAccountId: Long?,
    val destinationAccountId: Long?,
    val category: ExpenseCategory,
    val paymentMethod: ExpensePaymentMethod?,
    val transactionDate: LocalDate,
    val transactionTimeMinutes: Int?,
    val note: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class ExpenseTransactionInput(
    val id: Long = 0,
    val type: ExpenseTransactionType = ExpenseTransactionType.DEBIT,
    val amount: String = "",
    val sourceAccountId: Long? = null,
    val destinationAccountId: Long? = null,
    val category: ExpenseCategory = ExpenseCategory.FOOD,
    val paymentMethod: ExpensePaymentMethod? = ExpensePaymentMethod.UPI,
    val transactionDate: LocalDate = LocalDate.now(),
    val transactionTimeMinutes: Int? = null,
    val note: String = "",
)

data class ExpenseTransactionValidation(
    val amountError: Boolean = false,
    val sourceAccountError: Boolean = false,
    val destinationAccountError: Boolean = false,
    val sameAccountError: Boolean = false,
    val insufficientFundsError: Boolean = false,
) {
    val isValid: Boolean get() = !amountError && !sourceAccountError && !destinationAccountError &&
        !sameAccountError && !insufficientFundsError
}

fun ExpenseAccount.currentBalance(transactions: List<ExpenseTransaction>): Long =
    transactions.fold(openingBalancePaise) { balance, transaction ->
        when {
            transaction.type == ExpenseTransactionType.CREDIT && transaction.destinationAccountId == id ->
                balance + transaction.amountPaise
            transaction.type == ExpenseTransactionType.DEBIT && transaction.sourceAccountId == id ->
                balance - transaction.amountPaise
            transaction.type == ExpenseTransactionType.TRANSFER && transaction.sourceAccountId == id ->
                balance - transaction.amountPaise
            transaction.type == ExpenseTransactionType.TRANSFER && transaction.destinationAccountId == id ->
                balance + transaction.amountPaise
            else -> balance
        }
    }
