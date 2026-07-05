package com.raachi.memory.domain.model

data class LedgerEntry(
    val id: Int = 0,
    val personName: String,
    val mobileNumber: String?,
    val itemType: ItemType,
    val itemName: String?,
    val amount: Double?,
    val borrowDateTime: Long,
    val dueDateTime: Long?, // Reverted to nullable to preserve legacy records safely
    val status: LedgerStatus,
    val returnedDateTime: Long?,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long
)