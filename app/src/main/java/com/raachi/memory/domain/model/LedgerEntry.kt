package com.raachi.memory.domain.model

data class LedgerEntry(
    val id: Int = 0,
    val personName: String,
    val mobileNumber: String?,
    val itemType: ItemType,
    val itemName: String?,
    val amount: Double?,
    val dueDate: Long?,
    val returned: Boolean,
    val returnedDate: Long?,
    val notes: String?,
    val createdAt: Long
)