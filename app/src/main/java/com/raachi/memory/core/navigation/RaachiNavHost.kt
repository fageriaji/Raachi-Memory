package com.raachi.memory.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.raachi.memory.features.activity.ActivityScreen
import com.raachi.memory.features.dashboard.DashboardScreen
import com.raachi.memory.features.ledger.LedgerScreen
import com.raachi.memory.features.profile.OnboardingScreen
import com.raachi.memory.features.profile.ProfileScreen
import com.raachi.memory.features.profile.SplashScreen
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
                onNavigateToOnboarding = {
                    navController.navigate(AppRoute.Onboarding.route) {
                        popUpTo(AppRoute.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoute.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(AppRoute.Dashboard.route) {
                        popUpTo(AppRoute.Onboarding.route) { inclusive = true }
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