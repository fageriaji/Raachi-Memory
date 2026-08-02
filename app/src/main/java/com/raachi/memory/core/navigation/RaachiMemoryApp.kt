package com.raachi.memory.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.raachi.memory.foundation.FoundationScreen
import com.raachi.memory.foundation.SectionShellScreen
import com.raachi.memory.feature.dashboard.DashboardScreen
import com.raachi.memory.feature.onboarding.OnboardingProfileScreen
import com.raachi.memory.feature.onboarding.WelcomeScreen
import com.raachi.memory.feature.profile.EditProfileScreen
import com.raachi.memory.feature.profile.ProfileScreen
import com.raachi.memory.core.ui.AppSection
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

@Composable
fun RaachiMemoryApp(
    modifier: Modifier = Modifier,
    viewModel: AppViewModel = hiltViewModel(),
) {
    val startState by viewModel.startState.collectAsStateWithLifecycle()

    if (startState == AppStartState.Loading) {
        FoundationScreen(
            onOpenProfile = {},
            showProfileAction = false,
            modifier = modifier,
        )
        return
    }

    val navController = rememberNavController()
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
                onOpenSection = { section -> navController.navigateToSection(section) },
                onAddReminder = { navController.navigate(ADD_REMINDER_ROUTE) },
                onAddLedger = { navController.navigate(ADD_LEDGER_ROUTE) },
                onOpenReminder = { id -> navController.navigate("reminder_editor/$id") },
                onOpenLedger = { id -> navController.navigate("ledger_editor/$id") },
                onOpenExpenses = { navController.navigate(EXPENSES_ROUTE) },
                onAddExpense = { navController.navigate(ADD_EXPENSE_TRANSACTION_ROUTE) },
            )
        }
        composable(REMINDERS_ROUTE) {
            ReminderListScreen(
                onBack = navController::popBackStack,
                onOpenSection = { section -> navController.navigateToSection(section) },
                onAddReminder = { navController.navigate(ADD_REMINDER_ROUTE) },
                onEditReminder = { id -> navController.navigate("reminder_editor/$id") },
            )
        }
        composable(LEDGER_ROUTE) {
            LedgerListScreen(
                onBack = navController::popBackStack,
                onOpenSection = { section -> navController.navigateToSection(section) },
                onAddEntry = { navController.navigate(ADD_LEDGER_ROUTE) },
                onEditEntry = { id -> navController.navigate("ledger_editor/$id") },
            )
        }
        composable(ACTIVITY_ROUTE) {
            ActivityScreen(
                onBack = navController::popBackStack,
                onOpenSection = { section -> navController.navigateToSection(section) },
            )
        }
        composable(EXPENSES_ROUTE) {
            ExpenseOverviewScreen(
                onBack = navController::popBackStack,
                onOpenSection = { section -> navController.navigateToSection(section) },
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
                onBack = navController::popBackStack,
                onOpenSection = { section -> navController.navigateToSection(section) },
                onOpenAbout = { navController.navigate(ABOUT_ROUTE) },
            )
        }
        composable(ABOUT_ROUTE) {
            AboutScreen(onBack = navController::popBackStack)
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
                onBack = navController::popBackStack,
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

private fun AppSection.route(): String = when (this) {
    AppSection.HOME -> HOME_ROUTE
    AppSection.REMINDERS -> REMINDERS_ROUTE
    AppSection.LEDGER -> LEDGER_ROUTE
    AppSection.EXPENSES -> EXPENSES_ROUTE
    AppSection.ACTIVITY -> ACTIVITY_ROUTE
    AppSection.SETTINGS -> SETTINGS_ROUTE
}

private fun androidx.navigation.NavHostController.navigateToSection(section: AppSection) {
    navigate(section.route()) {
        popUpTo(HOME_ROUTE) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
