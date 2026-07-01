package com.raachi.memory.core.navigation

/**
 * App-level routes available for the application.
 */
sealed interface AppRoute {
    val route: String

    data object Splash : AppRoute {
        override val route: String = "splash"
    }

    data object Welcome : AppRoute {
        override val route: String = "welcome"
    }

    data object NameInput : AppRoute {
        override val route: String = "name_input"
    }

    data object OptionalProfile : AppRoute {
        override val route = "optional_profile/{name}"
        fun createRoute(name: String) = "optional_profile/$name"
    }

    data object Dashboard : AppRoute {
        override val route: String = "dashboard"
    }

    data object Reminder : AppRoute {
        override val route: String = "reminder"
    }

    // Phase 6 addition
    data object AddEditReminder : AppRoute {
        override val route = "add_edit_reminder/{reminderId}"
        fun createRoute(reminderId: Int = -1) = "add_edit_reminder/$reminderId"
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