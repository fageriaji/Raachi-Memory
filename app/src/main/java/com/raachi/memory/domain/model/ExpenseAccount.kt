package com.raachi.memory.domain.model

import java.time.Instant

enum class ExpenseAccountType { BANK, CASH, WALLET }

data class ExpenseAccount(
    val id: Long = 0,
    val name: String,
    val type: ExpenseAccountType,
    val openingBalancePaise: Long,
    val colorValue: Long,
    val isArchived: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class ExpenseAccountInput(
    val id: Long = 0,
    val name: String = "",
    val type: ExpenseAccountType = ExpenseAccountType.BANK,
    val openingBalance: String = "",
    val colorValue: Long = 0xFF142B85,
)

data class ExpenseAccountValidation(
    val nameError: Boolean = false,
    val balanceError: Boolean = false,
) {
    val isValid: Boolean get() = !nameError && !balanceError
}
