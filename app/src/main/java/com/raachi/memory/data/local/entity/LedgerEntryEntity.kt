package com.raachi.memory.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.raachi.memory.domain.model.ItemType
import com.raachi.memory.domain.model.LedgerEntry

@Entity(
    tableName = "ledger_entries",
    indices = [Index(value = ["due_date"]), Index(value = ["returned"])]
)
data class LedgerEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "person_name") val personName: String,
    @ColumnInfo(name = "mobile_number") val mobileNumber: String?,
    @ColumnInfo(name = "item_type") val itemType: ItemType,
    @ColumnInfo(name = "item_name") val itemName: String?,
    val amount: Double?,
    @ColumnInfo(name = "due_date") val dueDate: Long?,
    val returned: Boolean,
    @ColumnInfo(name = "returned_date") val returnedDate: Long?,
    val notes: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long
) {
    fun toDomain() = LedgerEntry(id, personName, mobileNumber, itemType, itemName, amount, dueDate, returned, returnedDate, notes, createdAt)

    companion object {
        fun fromDomain(model: LedgerEntry) = LedgerEntryEntity(model.id, model.personName, model.mobileNumber, model.itemType, model.itemName, model.amount, model.dueDate, model.returned, model.returnedDate, model.notes, model.createdAt)
    }
}