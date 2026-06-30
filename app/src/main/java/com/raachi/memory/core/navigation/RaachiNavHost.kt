package com.raachi.memory.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.raachi.memory.features.activity.ActivityScreen
import com.raachi.memory.features.dashboard.DashboardScreen
import com.raachi.memory.features.ledger.LedgerScreen
import com.raachi.memory.features.profile.NameInputScreen
import com.raachi.memory.features.profile.OptionalProfileScreen
import com.raachi.memory.features.profile.ProfileScreen
import com.raachi.memory.features.profile.SplashScreen
import com.raachi.memory.features.profile.WelcomeScreen
import com.raachi.memory.features.reminder.ReminderScreen
import com.raachi.memory.features.settings.SettingsScreen

/**
 * Root navigation host orchestrating transitions between all feature screens.
 */
@Composable
fun RaachiNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppRoute.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(AppRoute.Splash.route) {
            SplashScreen(
                onNavigateToWelcome = {
                    navController.navigate(AppRoute.Welcome.route) {
                        popUpTo(AppRoute.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(AppRoute.Dashboard.route) {
                        popUpTo(AppRoute.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoute.Welcome.route) {
            WelcomeScreen(
                onGetStarted = { navController.navigate(AppRoute.NameInput.route) }
            )
        }

        composable(AppRoute.NameInput.route) {
            NameInputScreen(
                onContinue = { name ->
                    navController.navigate(AppRoute.OptionalProfile.createRoute(name))
                }
            )
        }

        composable(
            route = AppRoute.OptionalProfile.route,
            arguments = listOf(navArgument("name") { type = NavType.StringType })
        ) {
            OptionalProfileScreen(
                onComplete = {
                    navController.navigate(AppRoute.Dashboard.route) {
                        // Clear the entire onboarding flow from the backstack
                        popUpTo(AppRoute.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoute.Dashboard.route) {
            DashboardScreen(
                onNavigateToReminder = { navController.navigate(AppRoute.Reminder.route) },
                onNavigateToLedger = { navController.navigate(AppRoute.Ledger.route) },
                onNavigateToActivity = { navController.navigate(AppRoute.Activity.route) },
                onNavigateToProfile = { navController.navigate(AppRoute.Profile.route) }
            )
        }

        composable(AppRoute.Reminder.route) {
            ReminderScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(AppRoute.Ledger.route) {
            LedgerScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(AppRoute.Activity.route) {
            ActivityScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(AppRoute.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate(AppRoute.Settings.route) }
            )
        }

        composable(AppRoute.Settings.route) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}