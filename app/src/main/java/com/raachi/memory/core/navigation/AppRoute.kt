package com.raachi.memory.core.navigation

/**
 * App-level routes available for the application.
 */
sealed interface AppRoute {
    val route: String

    data object Splash : AppRoute {
        override val route: String = "splash"
    }

    data object Onboarding : AppRoute {
        override val route: String = "onboarding"
    }

    data object Dashboard : AppRoute {
        override val route: String = "dashboard"
    }

    data object Reminder : AppRoute {
        override val route: String = "reminder"
    }

    data object Ledger : AppRoute {
        override val route: String = "ledger"
    }

    data object Activity : AppRoute {
        override val route: String = "activity"
    }

    data object Profile : AppRoute {
        override val route: String = "profile"
    }

    data object Settings : AppRoute {
        override val route: String = "settings"
    }
}