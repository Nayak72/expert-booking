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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.expertconnect.app.presentation.expert.dashboard.ExpertDashboardScreen
import com.expertconnect.app.presentation.expert.profile.ExpertProfileManagementScreen
import com.expertconnect.app.presentation.expert.sessions.ExpertSessionsScreen
import com.expertconnect.app.presentation.expert.slots.SlotManagementScreen
import com.expertconnect.app.ui.theme.*

data class ExpertNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val expertNavItems = listOf(
    ExpertNavItem("Dashboard", NavigationRoutes.ExpertDashboard.route, Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    ExpertNavItem("Sessions", NavigationRoutes.ExpertSessions.route, Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    ExpertNavItem("Slots", NavigationRoutes.SlotManagement.route, Icons.Filled.Schedule, Icons.Outlined.Schedule),
    ExpertNavItem("Profile", NavigationRoutes.ExpertProfileManagement.route, Icons.Filled.ManageAccounts, Icons.Outlined.ManageAccounts),
)

/**
 * Expert Navigation Graph — mentor management/dashboard experience.
 * Completely separate from UserNavGraph. Feels like a productivity console.
 */
@Composable
fun ExpertNavGraph(
    expertName: String,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            ExpertBottomNav(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationRoute ?: NavigationRoutes.ExpertDashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        },
        containerColor = BackgroundDark
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavigationRoutes.ExpertDashboard.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(250)) },
            exitTransition = { fadeOut(animationSpec = tween(250)) }
        ) {
            // ── Expert Main Tabs ───────────────────────────────────────────────
            composable(NavigationRoutes.ExpertDashboard.route) {
                ExpertDashboardScreen(expertName = expertName)
            }

            composable(NavigationRoutes.ExpertSessions.route) {
                ExpertSessionsScreen()
            }

            composable(NavigationRoutes.SlotManagement.route) {
                SlotManagementScreen()
            }

            composable(NavigationRoutes.ExpertProfileManagement.route) {
                ExpertProfileManagementScreen(
                    expertName = expertName,
                    onLogout = onLogout
                )
            }
        }
    }
}

@Composable
private fun ExpertBottomNav(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = SurfaceDark,
        tonalElevation = 8.0.dp
    ) {
        expertNavItems.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        tint = if (isSelected) SecondaryTeal else TextTertiary
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        color = if (isSelected) SecondaryTeal else TextTertiary,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SecondaryTeal,
                    unselectedIconColor = TextTertiary,
                    indicatorColor = SecondaryTeal.copy(alpha = 0.15f)
                )
            )
        }
    }
}


