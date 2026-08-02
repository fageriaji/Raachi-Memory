package com.raachi.memory.data.expense

import com.raachi.memory.domain.model.ExpenseAccount
import com.raachi.memory.domain.model.ExpenseAccountType
import com.raachi.memory.domain.model.ExpenseCategory
import com.raachi.memory.domain.model.ExpensePaymentMethod
import com.raachi.memory.domain.model.ExpenseTransaction
import com.raachi.memory.domain.model.ExpenseTransactionType
import java.time.Instant
import java.time.LocalDate

fun ExpenseAccountEntity.toDomain() = ExpenseAccount(
    id = id,
    name = name,
    type = ExpenseAccountType.valueOf(type),
    openingBalancePaise = openingBalancePaise,
    colorValue = colorValue,
    isArchived = isArchived,
    createdAt = Instant.ofEpochMilli(createdAtMillis),
    updatedAt = Instant.ofEpochMilli(updatedAtMillis),
)

fun ExpenseAccount.toEntity() = ExpenseAccountEntity(
    id = id,
    name = name,
    type = type.name,
    openingBalancePaise = openingBalancePaise,
    colorValue = colorValue,
    isArchived = isArchived,
    createdAtMillis = createdAt.toEpochMilli(),
    updatedAtMillis = updatedAt.toEpochMilli(),
)

fun ExpenseTransactionEntity.toDomain() = ExpenseTransaction(
    id = id,
    type = ExpenseTransactionType.valueOf(type),
    amountPaise = amountPaise,
    sourceAccountId = sourceAccountId,
    destinationAccountId = destinationAccountId,
    category = ExpenseCategory.valueOf(category),
    paymentMethod = paymentMethod?.let(ExpensePaymentMethod::valueOf),
    transactionDate = LocalDate.ofEpochDay(transactionDateEpochDay),
    transactionTimeMinutes = transactionTimeMinutes,
    note = note,
    createdAt = Instant.ofEpochMilli(createdAtMillis),
    updatedAt = Instant.ofEpochMilli(updatedAtMillis),
)

fun ExpenseTransaction.toEntity() = ExpenseTransactionEntity(
    id = id,
    type = type.name,
    amountPaise = amountPaise,
    sourceAccountId = sourceAccountId,
    destinationAccountId = destinationAccountId,
    category = category.name,
    paymentMethod = paymentMethod?.name,
    transactionDateEpochDay = transactionDate.toEpochDay(),
    transactionTimeMinutes = transactionTimeMinutes,
    note = note,
    createdAtMillis = createdAt.toEpochMilli(),
    updatedAtMillis = updatedAt.toEpochMilli(),
)
