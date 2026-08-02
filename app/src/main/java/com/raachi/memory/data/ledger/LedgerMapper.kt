package com.raachi.memory.data.ledger

import com.raachi.memory.domain.model.LedgerDirection
import com.raachi.memory.domain.model.LedgerEntry
import com.raachi.memory.domain.model.LedgerKind
import java.time.Instant
import java.time.LocalDate

fun LedgerEntryEntity.toDomain(): LedgerEntry = LedgerEntry(
    id = id,
    personName = personName,
    mobileNumber = mobileNumber,
    kind = enumValueOrDefault(kind, LedgerKind.ITEM),
    direction = enumValueOrDefault(direction, LedgerDirection.LENT),
    itemName = itemName,
    amountPaise = amountPaise,
    transactionDate = LocalDate.ofEpochDay(transactionDateEpochDay),
    dueDate = dueDateEpochDay?.let(LocalDate::ofEpochDay),
    isReturned = isReturned,
    returnedAt = returnedAtMillis?.let(Instant::ofEpochMilli),
    notes = notes,
    createdAt = Instant.ofEpochMilli(createdAtMillis),
    updatedAt = Instant.ofEpochMilli(updatedAtMillis),
)

fun LedgerEntry.toEntity(): LedgerEntryEntity = LedgerEntryEntity(
    id = id,
    personName = personName,
    mobileNumber = mobileNumber,
    kind = kind.name,
    direction = direction.name,
    itemName = itemName,
    amountPaise = amountPaise,
    transactionDateEpochDay = transactionDate.toEpochDay(),
    dueDateEpochDay = dueDate?.toEpochDay(),
    isReturned = isReturned,
    returnedAtMillis = returnedAt?.toEpochMilli(),
    notes = notes,
    createdAtMillis = createdAt.toEpochMilli(),
    updatedAtMillis = updatedAt.toEpochMilli(),
)

private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String, default: T): T =
    enumValues<T>().firstOrNull { it.name == value } ?: default
