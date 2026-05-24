package com.expertconnect.app.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.expertconnect.app.presentation.booking.BookingScreen
import com.expertconnect.app.presentation.booking.UserBookingsScreen
import com.expertconnect.app.presentation.expert.ExpertDetailScreen
import com.expertconnect.app.presentation.expert.ExpertListScreen
import com.expertconnect.app.presentation.favorites.FavoritesScreen
import com.expertconnect.app.presentation.home.UserHomeScreen
import com.expertconnect.app.presentation.user.UserProfileScreen
import com.expertconnect.app.ui.theme.*
import androidx.compose.ui.unit.dp

data class UserNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val userNavItems = listOf(
    UserNavItem("Home", NavigationRoutes.UserHome.route, Icons.Filled.Home, Icons.Outlined.Home),
    UserNavItem("Discover", NavigationRoutes.Discover.route, Icons.Filled.Search, Icons.Outlined.Search),
    UserNavItem("Bookings", NavigationRoutes.UserBookings.route, Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    UserNavItem("Favorites", NavigationRoutes.Favorites.route, Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    UserNavItem("Profile", NavigationRoutes.UserProfile.route, Icons.Filled.Person, Icons.Outlined.Person),
)

/**
 * User Navigation Graph — marketplace/discovery experience.
 * Completely separate from ExpertNavGraph.
 */
@Composable
fun UserNavGraph(
    userName: String,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomNav = currentRoute in UserBottomNavRoutes.items

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                UserBottomNav(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationRoute ?: NavigationRoutes.UserHome.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        containerColor = BackgroundDark
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavigationRoutes.UserHome.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(250)) },
            exitTransition = { fadeOut(animationSpec = tween(250)) }
        ) {
            // ── User Main Tabs ─────────────────────────────────────────────────
            composable(NavigationRoutes.UserHome.route) {
                UserHomeScreen(
                    userName = userName,
                    onExpertClick = { expertId ->
                        navController.navigate(NavigationRoutes.ExpertDetail.createRoute(expertId))
                    },
                    onSeeAllExperts = { navController.navigate(NavigationRoutes.Discover.route) }
                )
            }

            composable(NavigationRoutes.Discover.route) {
                ExpertListScreen(
                    onExpertClick = { expertId ->
                        navController.navigate(NavigationRoutes.ExpertDetail.createRoute(expertId))
                    }
                )
            }

            composable(NavigationRoutes.UserBookings.route) {
                UserBookingsScreen()
            }

            composable(NavigationRoutes.Favorites.route) {
                FavoritesScreen(
                    onExpertClick = { expertId ->
                        navController.navigate(NavigationRoutes.ExpertDetail.createRoute(expertId))
                    }
                )
            }

            composable(NavigationRoutes.UserProfile.route) {
                UserProfileScreen(
                    userName = userName,
                    onLogout = onLogout
                )
            }

            // ── Detail Screens ──────────────────────────────────────────────────
            composable(
                route = NavigationRoutes.ExpertDetail.route,
                arguments = listOf(navArgument(NavigationRoutes.ExpertDetail.ARG_EXPERT_ID) {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val expertId = backStackEntry.arguments?.getString(NavigationRoutes.ExpertDetail.ARG_EXPERT_ID)
                    ?: return@composable
                ExpertDetailScreen(
                    expertId = expertId,
                    onBookClick = { id, name ->
                        navController.navigate(NavigationRoutes.BookSession.createRoute(id, name))
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = NavigationRoutes.BookSession.route,
                arguments = listOf(
                    navArgument("expertId") { type = NavType.StringType },
                    navArgument("expertName") { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStackEntry ->
                val expertId = backStackEntry.arguments?.getString("expertId") ?: return@composable
                val expertName = backStackEntry.arguments?.getString("expertName") ?: ""
                BookingScreen(
                    expertId = expertId,
                    expertName = expertName,
                    onBookingSuccess = {
                        navController.navigate(NavigationRoutes.UserBookings.route) {
                            popUpTo(NavigationRoutes.UserHome.route)
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun UserBottomNav(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = SurfaceDark,
        tonalElevation = 8.0.dp
    ) {
        userNavItems.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        tint = if (isSelected) PrimaryPurple else TextTertiary
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        color = if (isSelected) PrimaryPurple else TextTertiary,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryPurple,
                    unselectedIconColor = TextTertiary,
                    indicatorColor = PrimaryPurple.copy(alpha = 0.15f)
                )
            )
        }
    }
}


