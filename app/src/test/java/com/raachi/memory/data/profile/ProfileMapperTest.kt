package com.raachi.memory.data.profile

import com.raachi.memory.domain.model.UserProfile
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileMapperTest {
    @Test
    fun dateOfBirth_isStoredAndRestoredAsDayMonthYear() {
        val profile = UserProfile(
            name = "Mannu",
            dateOfBirth = LocalDate.of(1990, 1, 5),
            createdAtMillis = 1L,
            updatedAtMillis = 1L,
        )

        val entity = profile.toEntity()

        assertEquals("05-01-1990", entity.dateOfBirth)
        assertEquals(LocalDate.of(1990, 1, 5), entity.toDomain().dateOfBirth)
    }
}
