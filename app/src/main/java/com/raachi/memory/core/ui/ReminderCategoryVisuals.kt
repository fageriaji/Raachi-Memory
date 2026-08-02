package com.raachi.memory.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.raachi.memory.domain.model.ReminderCategory

fun reminderCategoryEmoji(category: ReminderCategory): String = when (category) {
    ReminderCategory.WATER -> "💧"
    ReminderCategory.MEDICINE -> "💊"
    ReminderCategory.BREAKFAST -> "🍳"
    ReminderCategory.LUNCH -> "🥗"
    ReminderCategory.DINNER -> "🍽️"
    ReminderCategory.EXERCISE -> "🏃"
    ReminderCategory.SLEEP -> "🌙"
    ReminderCategory.CUSTOM -> "🔔"
}

@Composable
fun reminderCategoryAccent(category: ReminderCategory): Color {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return when (category) {
        ReminderCategory.WATER -> if (isDark) Color(0xFF73ACFF) else Color(0xFF2367BE)
        ReminderCategory.MEDICINE -> if (isDark) Color(0xFFFF83BD) else Color(0xFFC72F77)
        ReminderCategory.BREAKFAST -> if (isDark) Color(0xFFFFB75C) else Color(0xFFA95300)
        ReminderCategory.LUNCH -> if (isDark) Color(0xFF65D59D) else Color(0xFF087443)
        ReminderCategory.DINNER -> if (isDark) Color(0xFFC0A4FF) else Color(0xFF6742C2)
        ReminderCategory.EXERCISE -> if (isDark) Color(0xFF63D7B5) else Color(0xFF08745B)
        ReminderCategory.SLEEP -> if (isDark) Color(0xFFAAB4FF) else Color(0xFF4D5CB8)
        ReminderCategory.CUSTOM -> MaterialTheme.colorScheme.primary
    }
}
