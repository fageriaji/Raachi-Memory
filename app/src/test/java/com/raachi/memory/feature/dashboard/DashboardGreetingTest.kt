package com.raachi.memory.feature.dashboard

import com.raachi.memory.domain.model.LedgerKind
import com.raachi.memory.domain.model.ReminderCategory
import com.raachi.memory.core.ui.reminderCategoryEmoji
import com.raachi.memory.core.ui.ledgerKindSymbol
import java.time.LocalDate
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardGreetingTest {
    @Test
    fun dashboardDate_usesDayAndFullMonth() {
        assertEquals(
            "Friday, 31 July",
            formatDashboardDate(LocalDate.of(2026, 7, 31), Locale.ENGLISH),
        )
    }

    @Test
    fun greetingPeriod_changesAtExpectedBoundaries() {
        assertEquals(GreetingPeriod.NIGHT, greetingPeriodAt(5))
        assertEquals(GreetingPeriod.MORNING, greetingPeriodAt(6))
        assertEquals(GreetingPeriod.MORNING, greetingPeriodAt(11))
        assertEquals(GreetingPeriod.AFTERNOON, greetingPeriodAt(12))
        assertEquals(GreetingPeriod.AFTERNOON, greetingPeriodAt(15))
        assertEquals(GreetingPeriod.EVENING, greetingPeriodAt(16))
        assertEquals(GreetingPeriod.EVENING, greetingPeriodAt(19))
        assertEquals(GreetingPeriod.NIGHT, greetingPeriodAt(20))
    }

    @Test
    fun reminderCategories_useFullColorEmojiSymbols() {
        assertEquals("💧", reminderCategoryEmoji(ReminderCategory.WATER))
        assertEquals("💊", reminderCategoryEmoji(ReminderCategory.MEDICINE))
        assertEquals("🍳", reminderCategoryEmoji(ReminderCategory.BREAKFAST))
        assertEquals("🥗", reminderCategoryEmoji(ReminderCategory.LUNCH))
        assertEquals("🍽️", reminderCategoryEmoji(ReminderCategory.DINNER))
        assertEquals("🏃", reminderCategoryEmoji(ReminderCategory.EXERCISE))
        assertEquals("🌙", reminderCategoryEmoji(ReminderCategory.SLEEP))
        assertEquals("🔔", reminderCategoryEmoji(ReminderCategory.CUSTOM))
    }

    @Test
    fun ledgerKinds_useRupeeAndFullColorItemSymbol() {
        assertEquals("₹", ledgerKindSymbol(LedgerKind.MONEY))
        assertEquals("📦", ledgerKindSymbol(LedgerKind.ITEM))
    }
}
