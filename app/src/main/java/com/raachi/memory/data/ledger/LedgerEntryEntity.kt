package com.raachi.memory.data.ledger

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ledger_entries",
    indices = [Index(value = ["dueDateEpochDay"]), Index(value = ["isReturned"])],
)
data class LedgerEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personName: String,
    val mobileNumber: String?,
    val kind: String,
    val direction: String,
    val itemName: String?,
    val amountPaise: Long?,
    @ColumnInfo(defaultValue = "0") val transactionDateEpochDay: Long,
    val dueDateEpochDay: Long?,
    val isReturned: Boolean,
    val returnedAtMillis: Long?,
    val notes: String?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
