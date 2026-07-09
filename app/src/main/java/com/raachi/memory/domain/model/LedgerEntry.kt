package com.raachi.memory.domain.model

data class LedgerEntry(
    val id: Int = 0,
    val personName: String,
    val mobileNumber: String?,
    val itemType: ItemType,
    val itemName: String?,
    val amount: Double?,
    val borrowDateTime: Long,
    val dueDateTime: Long?,
    val status: LedgerStatus,
    val returnedDateTime: Long?,
    val snoozedUntil: Long?,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long
)