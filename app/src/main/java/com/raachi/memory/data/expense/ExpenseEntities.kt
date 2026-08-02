package com.raachi.memory.data.expense

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "expense_accounts")
data class ExpenseAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val openingBalancePaise: Long,
    val colorValue: Long,
    val isArchived: Boolean,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)

@Entity(
    tableName = "expense_transactions",
    foreignKeys = [
        ForeignKey(
            entity = ExpenseAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceAccountId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = ExpenseAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["destinationAccountId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("sourceAccountId"), Index("destinationAccountId"), Index("transactionDateEpochDay"), Index("type")],
)
data class ExpenseTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val amountPaise: Long,
    val sourceAccountId: Long?,
    val destinationAccountId: Long?,
    val category: String,
    val paymentMethod: String?,
    val transactionDateEpochDay: Long,
    val transactionTimeMinutes: Int?,
    val note: String?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
