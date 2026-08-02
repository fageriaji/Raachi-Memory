package com.raachi.memory.domain.model

import java.time.LocalDate

data class LedgerInput(
    val id: Long = 0,
    val personName: String = "",
    val mobileNumber: String = "",
    val kind: LedgerKind = LedgerKind.MONEY,
    val direction: LedgerDirection = LedgerDirection.LENT,
    val itemName: String = "",
    val amount: String = "",
    val transactionDate: LocalDate = LocalDate.now(),
    val dueDate: LocalDate? = null,
    val notes: String = "",
)

data class LedgerValidation(
    val personError: Boolean = false,
    val mobileError: Boolean = false,
    val amountError: Boolean = false,
    val itemError: Boolean = false,
) {
    val isValid: Boolean get() = !personError && !mobileError && !amountError && !itemError
}
