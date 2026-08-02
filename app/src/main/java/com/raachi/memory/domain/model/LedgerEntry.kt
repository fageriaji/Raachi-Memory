package com.raachi.memory.domain.model

import java.time.Instant
import java.time.LocalDate

data class LedgerEntry(
    val id: Long = 0,
    val personName: String,
    val mobileNumber: String? = null,
    val kind: LedgerKind,
    val direction: LedgerDirection,
    val itemName: String? = null,
    val amountPaise: Long? = null,
    val transactionDate: LocalDate,
    val dueDate: LocalDate? = null,
    val isReturned: Boolean = false,
    val returnedAt: Instant? = null,
    val notes: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    fun isOverdue(today: LocalDate): Boolean = !isReturned && dueDate?.isBefore(today) == true
}

enum class LedgerKind { MONEY, ITEM }

enum class LedgerDirection { LENT, BORROWED }
