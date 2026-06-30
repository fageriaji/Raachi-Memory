package com.raachi.memory.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.raachi.memory.R
import com.raachi.memory.core.navigation.AppRoute

@Composable
fun RaachiBottomBar(
    navController: NavHostController,
    currentRoute: String?,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.home)) },
            label = { Text(stringResource(R.string.home)) },
            selected = currentRoute == AppRoute.Dashboard.route,
            onClick = { navigateSafely(navController, AppRoute.Dashboard.route) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Notifications, contentDescription = stringResource(R.string.reminders)) },
            label = { Text(stringResource(R.string.reminders)) },
            selected = currentRoute == AppRoute.Reminder.route,
            onClick = { navigateSafely(navController, AppRoute.Reminder.route) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = stringResource(R.string.ledger)) },
            label = { Text(stringResource(R.string.ledger)) },
            selected = currentRoute == AppRoute.Ledger.route,
            onClick = { navigateSafely(navController, AppRoute.Ledger.route) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.DateRange, contentDescription = stringResource(R.string.activity)) },
            label = { Text(stringResource(R.string.activity)) },
            selected = currentRoute == AppRoute.Activity.route,
            onClick = { navigateSafely(navController, AppRoute.Activity.route) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = stringResource(R.string.profile)) },
            label = { Text(stringResource(R.string.profile)) },
            selected = currentRoute == AppRoute.Profile.route,
            onClick = { navigateSafely(navController, AppRoute.Profile.route) }
        )
    }
}

private fun navigateSafely(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(AppRoute.Dashboard.route) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}