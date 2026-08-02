package com.raachi.memory.data.ledger

import com.raachi.memory.domain.model.LedgerDirection
import com.raachi.memory.domain.model.LedgerEntry
import com.raachi.memory.domain.model.LedgerKind
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class LedgerMapperTest {
    @Test
    fun ledgerEntry_roundTripsThroughRoomEntity() {
        val entry = LedgerEntry(
            id = 8,
            personName = "Sana Chopra",
            mobileNumber = "9876543210",
            kind = LedgerKind.ITEM,
            direction = LedgerDirection.BORROWED,
            itemName = "Kotlin Book",
            transactionDate = LocalDate.of(2026, 7, 25),
            dueDate = LocalDate.of(2026, 8, 12),
            isReturned = true,
            returnedAt = Instant.parse("2026-08-10T10:00:00Z"),
            notes = "Good condition",
            createdAt = Instant.parse("2026-07-30T08:00:00Z"),
            updatedAt = Instant.parse("2026-08-10T10:00:00Z"),
        )

        assertEquals(entry, entry.toEntity().toDomain())
    }
}
