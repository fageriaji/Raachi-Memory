package com.raachi.memory.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.raachi.memory.foundation.FoundationScreen
import com.raachi.memory.feature.dashboard.DashboardScreen
import com.raachi.memory.feature.onboarding.OnboardingProfileScreen
import com.raachi.memory.feature.onboarding.WelcomeScreen
import com.raachi.memory.feature.profile.EditProfileScreen
import com.raachi.memory.feature.profile.ProfileScreen
import com.raachi.memory.core.ui.DrawerDestination
import com.raachi.memory.core.ui.RaachiNavigationDrawer
import com.raachi.memory.feature.reminder.ReminderEditorScreen
import com.raachi.memory.feature.reminder.ReminderEditorViewModel
import com.raachi.memory.feature.reminder.ReminderListScreen
import com.raachi.memory.feature.ledger.LedgerEditorScreen
import com.raachi.memory.feature.ledger.LedgerEditorViewModel
import com.raachi.memory.feature.ledger.LedgerListScreen
import com.raachi.memory.feature.activity.ActivityScreen
import com.raachi.memory.feature.settings.AboutScreen
import com.raachi.memory.feature.settings.SettingsScreen
import com.raachi.memory.feature.expense.ExpenseEditorScreen
import com.raachi.memory.feature.expense.ExpenseEditorViewModel
import com.raachi.memory.feature.expense.ExpenseOverviewScreen
import com.raachi.memory.feature.security.AppLockSettingsScreen
import kotlinx.coroutines.launch

@Composable
fun RaachiMemoryApp(
    modifier: Modifier = Modifier,
    biometricAvailable: Boolean = false,
    viewModel: AppViewModel = hiltViewModel(),
) {
    val startState by viewModel.startState.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()

    if (startState == AppStartState.Loading) {
        FoundationScreen(
            onOpenProfile = {},
            showProfileAction = false,
            modifier = modifier,
        )
        return
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val selectedDestination = backStackEntry?.destination?.route.toDrawerDestination()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val openDrawer: () -> Unit = { coroutineScope.launch { drawerState.open() } }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = selectedDestination != null,
        drawerContent = {
            RaachiNavigationDrawer(
                selected = selectedDestination,
                profileName = profile?.name.orEmpty(),
                profilePhotoUri = profile?.profilePhotoUri,
                onSelected = { destination ->
                    coroutineScope.launch { drawerState.close() }
                    navController.navigateToDrawerDestination(destination)
                },
            )
        },
    ) {
        NavHost(
            navController = navController,
            startDestination = if (startState == AppStartState.Ready) HOME_ROUTE else WELCOME_ROUTE,
            modifier = modifier.fillMaxSize(),
        ) {
        composable(WELCOME_ROUTE) {
            WelcomeScreen(
                onContinue = { navController.navigate(ONBOARDING_PROFILE_ROUTE) },
            )
        }
        composable(ONBOARDING_PROFILE_ROUTE) {
            OnboardingProfileScreen(
                onBack = navController::popBackStack,
                onComplete = {
                    navController.navigate(HOME_ROUTE) {
                        popUpTo(WELCOME_ROUTE) { inclusive = true }
                    }
                },
            )
        }
        composable(HOME_ROUTE) {
            DashboardScreen(
                onOpenProfile = { navController.navigate(PROFILE_ROUTE) },
                onOpenDrawer = openDrawer,
                onOpenPrimary = navController::navigateToDrawerDestination,
                onAddReminder = { navController.navigate(ADD_REMINDER_ROUTE) },
                onAddLedger = { navController.navigate(ADD_LEDGER_ROUTE) },
                onOpenReminder = { id -> navController.navigate("reminder_editor/$id") },
                onOpenLedger = { id -> navController.navigate("ledger_editor/$id") },
                onAddExpense = { navController.navigate(ADD_EXPENSE_TRANSACTION_ROUTE) },
            )
        }
        composable(REMINDERS_ROUTE) {
            ReminderListScreen(
                onOpenDrawer = openDrawer,
                onOpenPrimary = navController::navigateToDrawerDestination,
                onAddReminder = { navController.navigate(ADD_REMINDER_ROUTE) },
                onEditReminder = { id -> navController.navigate("reminder_editor/$id") },
            )
        }
        composable(LEDGER_ROUTE) {
            LedgerListScreen(
                onOpenDrawer = openDrawer,
                onOpenPrimary = navController::navigateToDrawerDestination,
                onAddEntry = { navController.navigate(ADD_LEDGER_ROUTE) },
                onEditEntry = { id -> navController.navigate("ledger_editor/$id") },
            )
        }
        composable(ACTIVITY_ROUTE) {
            ActivityScreen(
                onOpenDrawer = openDrawer,
            )
        }
        composable(EXPENSES_ROUTE) {
            ExpenseOverviewScreen(
                onOpenDrawer = openDrawer,
                onOpenPrimary = navController::navigateToDrawerDestination,
                onAddTransaction = { navController.navigate(ADD_EXPENSE_TRANSACTION_ROUTE) },
                onEditTransaction = { id -> navController.navigate("expense_editor/$id") },
            )
        }
        composable(ADD_EXPENSE_TRANSACTION_ROUTE) {
            ExpenseEditorScreen(
                onBack = navController::popBackStack,
                onSaved = navController::popBackStack,
            )
        }
        composable(
            route = EDIT_EXPENSE_TRANSACTION_ROUTE,
            arguments = listOf(
                navArgument(ExpenseEditorViewModel.TRANSACTION_ID_ARG) { type = NavType.LongType },
            ),
        ) {
            ExpenseEditorScreen(
                onBack = navController::popBackStack,
                onSaved = navController::popBackStack,
            )
        }
        composable(SETTINGS_ROUTE) {
            SettingsScreen(
                onOpenDrawer = openDrawer,
                onOpenAbout = { navController.navigate(ABOUT_ROUTE) },
                onOpenAppLock = { navController.navigate(APP_LOCK_ROUTE) },
            )
        }
        composable(BACKUP_RESTORE_ROUTE) {
            SettingsScreen(
                onOpenDrawer = openDrawer,
                onOpenAbout = { navController.navigate(ABOUT_ROUTE) },
                onOpenAppLock = { navController.navigate(APP_LOCK_ROUTE) },
                backupOnly = true,
            )
        }
        composable(APP_LOCK_ROUTE) {
            AppLockSettingsScreen(
                onBack = navController::popBackStack,
                biometricAvailable = biometricAvailable,
            )
        }
        composable(ABOUT_ROUTE) {
            AboutScreen(onOpenDrawer = openDrawer)
        }
        composable(ADD_REMINDER_ROUTE) {
            ReminderEditorScreen(
                onBack = navController::popBackStack,
                onSaved = navController::popBackStack,
            )
        }
        composable(
            route = EDIT_REMINDER_ROUTE,
            arguments = listOf(
                navArgument(ReminderEditorViewModel.REMINDER_ID_ARG) { type = NavType.LongType },
            ),
        ) {
            ReminderEditorScreen(
                onBack = navController::popBackStack,
                onSaved = navController::popBackStack,
            )
        }
        composable(ADD_LEDGER_ROUTE) {
            LedgerEditorScreen(
                onBack = navController::popBackStack,
                onSaved = navController::popBackStack,
            )
        }
        composable(
            route = EDIT_LEDGER_ROUTE,
            arguments = listOf(
                navArgument(LedgerEditorViewModel.LEDGER_ID_ARG) { type = NavType.LongType },
            ),
        ) {
            LedgerEditorScreen(
                onBack = navController::popBackStack,
                onSaved = navController::popBackStack,
            )
        }
        composable(PROFILE_ROUTE) {
            ProfileScreen(
                onOpenDrawer = openDrawer,
                onEditProfile = { navController.navigate(EDIT_PROFILE_ROUTE) },
            )
        }
        composable(EDIT_PROFILE_ROUTE) {
            EditProfileScreen(
                onBack = navController::popBackStack,
                onSaved = navController::popBackStack,
            )
        }
    }
    }
}

private fun DrawerDestination.route(): String = when (this) {
    DrawerDestination.HOME -> HOME_ROUTE
    DrawerDestination.REMINDERS -> REMINDERS_ROUTE
    DrawerDestination.LEDGER -> LEDGER_ROUTE
    DrawerDestination.EXPENSES -> EXPENSES_ROUTE
    DrawerDestination.ACTIVITY -> ACTIVITY_ROUTE
    DrawerDestination.PROFILE -> PROFILE_ROUTE
    DrawerDestination.BACKUP_RESTORE -> BACKUP_RESTORE_ROUTE
    DrawerDestination.SETTINGS -> SETTINGS_ROUTE
    DrawerDestination.ABOUT -> ABOUT_ROUTE
}

private fun String?.toDrawerDestination(): DrawerDestination? = when (this) {
    HOME_ROUTE -> DrawerDestination.HOME
    REMINDERS_ROUTE -> DrawerDestination.REMINDERS
    LEDGER_ROUTE -> DrawerDestination.LEDGER
    EXPENSES_ROUTE -> DrawerDestination.EXPENSES
    ACTIVITY_ROUTE -> DrawerDestination.ACTIVITY
    PROFILE_ROUTE -> DrawerDestination.PROFILE
    BACKUP_RESTORE_ROUTE -> DrawerDestination.BACKUP_RESTORE
    SETTINGS_ROUTE -> DrawerDestination.SETTINGS
    ABOUT_ROUTE -> DrawerDestination.ABOUT
    else -> null
}

private fun androidx.navigation.NavHostController.navigateToDrawerDestination(destination: DrawerDestination) {
    if (currentDestination?.route == destination.route()) return
    navigate(destination.route()) {
        popUpTo(HOME_ROUTE) { inclusive = false }
        launchSingleTop = true
    }
}
