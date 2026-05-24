package com.expertconnect.app.navigation

/**
 * Type-safe navigation routes for the ExpertConnect app.
 * Split into Auth, User, and Expert route namespaces.
 */
sealed class NavigationRoutes(val route: String) {

    // ── Auth (shared) ─────────────────────────────────────────────────────────
    object Splash : NavigationRoutes("splash")
    object Login : NavigationRoutes("login")
    object Signup : NavigationRoutes("signup")

    // ── Role root destinations ─────────────────────────────────────────────────
    object UserMain : NavigationRoutes("user_main")
    object ExpertMain : NavigationRoutes("expert_main")

    // ── User Routes ───────────────────────────────────────────────────────────
    object UserHome : NavigationRoutes("user_home")
    object Discover : NavigationRoutes("discover")
    object UserBookings : NavigationRoutes("user_bookings")
    object Favorites : NavigationRoutes("favorites")
    object UserProfile : NavigationRoutes("user_profile")

    // ── User Detail Screens ────────────────────────────────────────────────────
    object ExpertDetail : NavigationRoutes("expert/{expertId}") {
        fun createRoute(expertId: String) = "expert/$expertId"
        const val ARG_EXPERT_ID = "expertId"
    }

    object BookSession : NavigationRoutes("book/{expertId}?name={expertName}") {
        fun createRoute(expertId: String, expertName: String = "") =
            "book/$expertId?name=${expertName.encodeUrl()}"
        const val ARG_EXPERT_ID = "expertId"
        const val ARG_EXPERT_NAME = "expertName"
    }

    // ── Expert Routes ──────────────────────────────────────────────────────────
    object ExpertDashboard : NavigationRoutes("expert_dashboard")
    object ExpertSessions : NavigationRoutes("expert_sessions")
    object SlotManagement : NavigationRoutes("slot_management")
    object ExpertProfileManagement : NavigationRoutes("expert_profile_management")
}

/**
 * User bottom navigation items.
 */
object UserBottomNavRoutes {
    val items = listOf(
        NavigationRoutes.UserHome.route,
        NavigationRoutes.Discover.route,
        NavigationRoutes.UserBookings.route,
        NavigationRoutes.Favorites.route,
        NavigationRoutes.UserProfile.route
    )
}

/**
 * Expert bottom navigation items.
 */
object ExpertBottomNavRoutes {
    val items = listOf(
        NavigationRoutes.ExpertDashboard.route,
        NavigationRoutes.ExpertSessions.route,
        NavigationRoutes.SlotManagement.route,
        NavigationRoutes.ExpertProfileManagement.route
    )
}

private fun String.encodeUrl() = java.net.URLEncoder.encode(this, "UTF-8")
