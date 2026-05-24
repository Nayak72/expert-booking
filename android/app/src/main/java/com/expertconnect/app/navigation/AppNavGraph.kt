package com.expertconnect.app.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.navigation.compose.*
import com.expertconnect.app.presentation.auth.LoginScreen
import com.expertconnect.app.presentation.auth.SignupScreen
import com.expertconnect.app.presentation.auth.SplashScreen
import com.expertconnect.app.utils.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Root Navigation Graph — the entry point of the app.
 *
 * Responsibilities:
 * 1. Auth flow: Splash → Login / Signup
 * 2. Role-based routing:
 *    - role == "user"   → UserNavGraph (marketplace experience)
 *    - role == "expert" → ExpertNavGraph (management dashboard)
 *
 * NO shared screens exist between UserNavGraph and ExpertNavGraph
 * except auth screens and reusable components.
 */
@Composable
fun AppNavGraph(dataStoreManager: DataStoreManager) {
    val navController = rememberNavController()

    val isLoggedIn = remember {
        runBlocking { dataStoreManager.isLoggedIn.first() }
    }
    val userRole = remember {
        runBlocking { dataStoreManager.userRole.first() }
    }

    NavHost(

            navController = navController,
            startDestination = NavigationRoutes.Splash.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            // ── Splash ────────────────────────────────────────────────────────────
            composable(NavigationRoutes.Splash.route) {
                SplashScreen(
                    isLoggedIn = isLoggedIn,
                    userRole = userRole,
                    onNavigateToUserMain = {
                        navController.navigate(NavigationRoutes.UserMain.route) {
                            popUpTo(NavigationRoutes.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToExpertMain = {
                        navController.navigate(NavigationRoutes.ExpertMain.route) {
                            popUpTo(NavigationRoutes.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(NavigationRoutes.Login.route) {
                            popUpTo(NavigationRoutes.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // ── Auth ──────────────────────────────────────────────────────────────
            composable(NavigationRoutes.Login.route) {
                LoginScreen(
                    onNavigateToSignup = { navController.navigate(NavigationRoutes.Signup.route) },
                    onLoginSuccess = { role ->
                        val destination = if (role == "expert")
                            NavigationRoutes.ExpertMain.route
                        else
                            NavigationRoutes.UserMain.route
                        navController.navigate(destination) {
                            popUpTo(NavigationRoutes.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(NavigationRoutes.Signup.route) {
                SignupScreen(
                    onNavigateToLogin = { navController.popBackStack() },
                    onSignupSuccess = {
                        navController.navigate(NavigationRoutes.Login.route) {
                            popUpTo(NavigationRoutes.Signup.route) { inclusive = true }
                        }
                    }
                )
            }

            // ── User Application Flow ──────────────────────────────────────────────
            // role == "user": marketplace/discovery experience
            composable(NavigationRoutes.UserMain.route) {
                val currentUserName by dataStoreManager.userName.collectAsState(initial = "User")
                UserNavGraph(
                    userName = currentUserName ?: "User",
                    onLogout = {
                        navController.navigate(NavigationRoutes.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // ── Expert Application Flow ────────────────────────────────────────────
            // role == "expert": management dashboard experience
            composable(NavigationRoutes.ExpertMain.route) {
                val currentExpertName by dataStoreManager.userName.collectAsState(initial = "Expert")
                ExpertNavGraph(
                    expertName = currentExpertName ?: "Expert",
                    onLogout = {
                        navController.navigate(NavigationRoutes.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
}
