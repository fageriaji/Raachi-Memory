package com.raachi.memory.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.raachi.memory.domain.model.ItemType
import com.raachi.memory.domain.model.LedgerEntry
import com.raachi.memory.domain.model.LedgerStatus

@Entity(
    tableName = "ledger_entries",
    indices = [Index(value = ["due_date_time"]), Index(value = ["status"])]
)
data class LedgerEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "person_name") val personName: String,
    @ColumnInfo(name = "mobile_number") val mobileNumber: String?,
    @ColumnInfo(name = "item_type") val itemType: ItemType,
    @ColumnInfo(name = "item_name") val itemName: String?,
    val amount: Double?,
    @ColumnInfo(name = "borrow_date_time") val borrowDateTime: Long,
    @ColumnInfo(name = "due_date_time") val dueDateTime: Long?, // Reverted to nullable
    val status: LedgerStatus,
    @ColumnInfo(name = "returned_date_time") val returnedDateTime: Long?,
    val notes: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
) {
    fun toDomain() = LedgerEntry(
        id = id,
        personName = personName,
        mobileNumber = mobileNumber,
        itemType = itemType,
        itemName = itemName,
        amount = amount,
        borrowDateTime = borrowDateTime,
        dueDateTime = dueDateTime,
        status = status,
        returnedDateTime = returnedDateTime,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(model: LedgerEntry) = LedgerEntryEntity(
            id = model.id,
            personName = model.personName,
            mobileNumber = model.mobileNumber,
            itemType = model.itemType,
            itemName = model.itemName,
            amount = model.amount,
            borrowDateTime = model.borrowDateTime,
            dueDateTime = model.dueDateTime,
            status = model.status,
            returnedDateTime = model.returnedDateTime,
            notes = model.notes,
            createdAt = model.createdAt,
            updatedAt = model.updatedAt
        )
    }
}