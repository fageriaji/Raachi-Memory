package com.raachi.memory

import org.junit.Assert.assertEquals
import org.junit.Test

class ArchitectureTest {
    @Test
    fun applicationId_isStable() {
        assertEquals("com.raachi.memory", BuildConfig.APPLICATION_ID)
    }
}
