package com.raachi.memory.core.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.raachi.memory.R

enum class AppSection(
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    HOME(R.string.nav_home, Icons.Outlined.Home),
    REMINDERS(R.string.nav_reminders, Icons.Outlined.Notifications),
    LEDGER(R.string.nav_ledger, Icons.Outlined.AccountBalanceWallet),
    EXPENSES(R.string.nav_expenses, Icons.Outlined.Payments),
    ACTIVITY(R.string.nav_activity, Icons.Outlined.History),
    SETTINGS(R.string.nav_settings, Icons.Outlined.Settings),
}

@Composable
fun RaachiBottomBar(
    selected: AppSection,
    onSelected: (AppSection) -> Unit,
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        AppSection.entries.forEach { section ->
            val selectedColor = when (section) {
                AppSection.LEDGER -> LedgerMainColor
                AppSection.EXPENSES -> ExpenseMainColor
                else -> MaterialTheme.colorScheme.primary
            }
            NavigationBarItem(
                selected = section == selected,
                onClick = { onSelected(section) },
                icon = {
                    Icon(
                        imageVector = section.icon,
                        contentDescription = null,
                    )
                },
                label = {
                    Text(
                        text = stringResource(section.labelRes),
                        maxLines = 1,
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = selectedColor.copy(alpha = 0.14f),
                    selectedIconColor = selectedColor,
                    selectedTextColor = selectedColor,
                ),
            )
        }
    }
}
