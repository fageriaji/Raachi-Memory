package com.raachi.memory.core.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

private val PRIMARY_DESTINATIONS = listOf(
    DrawerDestination.HOME,
    DrawerDestination.REMINDERS,
    DrawerDestination.LEDGER,
    DrawerDestination.EXPENSES,
)

@Composable
fun RaachiBottomBar(
    selected: DrawerDestination,
    onSelected: (DrawerDestination) -> Unit,
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        PRIMARY_DESTINATIONS.forEach { destination ->
            NavigationBarItem(
                selected = destination == selected,
                onClick = { onSelected(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = null,
                    )
                },
                label = {
                    Text(
                        text = stringResource(destination.labelRes),
                        maxLines = 1,
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = destination.iconColor,
                    selectedTextColor = destination.iconColor,
                    indicatorColor = destination.iconColor.copy(alpha = 0.14f),
                    unselectedIconColor = destination.iconColor,
                ),
            )
        }
    }
}
