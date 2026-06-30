package com.raachi.memory.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
import com.raachi.memory.ui.components.RaachiBottomBar

@Composable
fun RaachiNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppRoute.Splash.route
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavRoutes = listOf(
        AppRoute.Dashboard.route,
        AppRoute.Reminder.route,
        AppRoute.Ledger.route,
        AppRoute.Activity.route,
        AppRoute.Profile.route
    )

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                RaachiBottomBar(navController = navController, currentRoute = currentRoute)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
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
                WelcomeScreen(onGetStarted = { navController.navigate(AppRoute.NameInput.route) })
            }

            composable(AppRoute.NameInput.route) {
                NameInputScreen(onContinue = { name ->
                    navController.navigate(AppRoute.OptionalProfile.createRoute(name))
                })
            }

            composable(
                route = AppRoute.OptionalProfile.route,
                arguments = listOf(navArgument("name") { type = NavType.StringType })
            ) {
                OptionalProfileScreen(
                    onComplete = {
                        navController.navigate(AppRoute.Dashboard.route) {
                            popUpTo(AppRoute.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(AppRoute.Dashboard.route) {
                DashboardScreen(
                    onNavigateToReminder = { /* Create Reminder functionality later */ },
                    onNavigateToLedger = { /* Create Ledger functionality later */ }
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
}