package com.raachi.memory.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.raachi.memory.domain.model.ExpenseCategory
import com.raachi.memory.domain.model.ExpenseTransactionType

val ExpenseMainColor = Color(0xFFFF8A1F)
val ExpenseDebitColor = Color(0xFFF95738)
val ExpenseCreditColor = Color(0xFF43AA8B)
val ExpenseTransferColor = Color(0xFF5267C9)

@Composable
fun expenseTypeColor(type: ExpenseTransactionType): Color = when (type) {
    ExpenseTransactionType.DEBIT -> ExpenseDebitColor
    ExpenseTransactionType.CREDIT -> ExpenseCreditColor
    ExpenseTransactionType.TRANSFER -> ExpenseTransferColor
}

fun expenseCategoryEmoji(category: ExpenseCategory): String = when (category) {
    ExpenseCategory.FOOD -> "🍽️"
    ExpenseCategory.GROCERIES -> "🛒"
    ExpenseCategory.TRAVEL -> "🚕"
    ExpenseCategory.BILLS -> "🧾"
    ExpenseCategory.SHOPPING -> "🛍️"
    ExpenseCategory.HEALTH -> "💊"
    ExpenseCategory.EDUCATION -> "📚"
    ExpenseCategory.ENTERTAINMENT -> "🎬"
    ExpenseCategory.RENT -> "🏠"
    ExpenseCategory.SALARY -> "💼"
    ExpenseCategory.REFUND -> "↩️"
    ExpenseCategory.INTEREST -> "📈"
    ExpenseCategory.GIFT -> "🎁"
    ExpenseCategory.CASHBACK -> "🪙"
    ExpenseCategory.OTHER -> "📝"
}
