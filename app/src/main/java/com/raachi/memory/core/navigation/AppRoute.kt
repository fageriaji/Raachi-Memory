package com.raachi.memory.core.navigation

/**
 * App-level routes available during the project foundation phase.
 */
sealed interface AppRoute {
    val route: String

    data object Home : AppRoute {
        override val route: String = "home"
    }
}
