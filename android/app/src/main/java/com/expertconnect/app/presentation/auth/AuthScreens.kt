package com.expertconnect.app.presentation.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.expertconnect.app.presentation.components.AuthTextField
import com.expertconnect.app.presentation.components.GradientButton
import com.expertconnect.app.ui.theme.*
import com.expertconnect.app.utils.UiState
import kotlinx.coroutines.delay

/**
 * Animated Splash Screen — reads role and routes accordingly.
 * role == "user"   → onNavigateToUserMain
 * role == "expert" → onNavigateToExpertMain
 * not logged in    → onNavigateToLogin
 */
@Composable
fun SplashScreen(
    isLoggedIn: Boolean,
    userRole: String?,
    onNavigateToUserMain: () -> Unit,
    onNavigateToExpertMain: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow"
    )

    LaunchedEffect(Unit) {
        delay(2000)
        when {
            !isLoggedIn -> onNavigateToLogin()
            userRole == "expert" -> onNavigateToExpertMain()
            else -> onNavigateToUserMain()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundDark, SurfaceVariantDark, BackgroundDark)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Logo glow effect
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    PrimaryPurple.copy(alpha = glowAlpha),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Text(text = "⚡", fontSize = 52.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "ExpertConnect",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "AI-Powered Expert Consultations",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = PrimaryPurple,
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * Premium Login Screen — routes to correct flow based on returned role.
 */
@Composable
fun LoginScreen(
    onNavigateToSignup: () -> Unit,
    onLoginSuccess: (String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val loginState by viewModel.loginState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is UiState.Success -> {
                onLoginSuccess(state.data.role)
                viewModel.resetLoginState()
            }
            is UiState.Error -> {
                errorMessage = state.message
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundDark, SurfaceVariantDark)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Header
            Text(text = "⚡", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Sign in to your ExpertConnect account",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Input fields
            AuthTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = "Email",
                leadingIcon = { Icon(Icons.Default.Email, null, tint = PrimaryPurple) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            AuthTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = "Password",
                isPassword = true,
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = PrimaryPurple) },
                imeAction = ImeAction.Done,
                onImeAction = { if (email.isNotBlank() && password.isNotBlank()) viewModel.login(email, password) }
            )

            // Error message
            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = StatusCancelled, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Login button
            GradientButton(
                text = "Sign In",
                onClick = { viewModel.login(email, password) },
                isLoading = loginState is UiState.Loading,
                enabled = email.isNotBlank() && password.isNotBlank()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Signup link
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Don't have an account? ", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onNavigateToSignup) {
                    Text("Sign Up", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Premium Signup Screen with role selection.
 */
@Composable
fun SignupScreen(
    onNavigateToLogin: () -> Unit,
    onSignupSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val signupState by viewModel.signupState.collectAsState()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("user") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(signupState) {
        when (val state = signupState) {
            is UiState.Success -> {
                onSignupSuccess()
                viewModel.resetSignupState()
            }
            is UiState.Error -> errorMessage = state.message
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(BackgroundDark, SurfaceVariantDark)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(text = "🚀", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Create Account", style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.ExtraBold)
            Text("Join ExpertConnect today", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)

            Spacer(modifier = Modifier.height(32.dp))

            AuthTextField(
                value = name,
                onValueChange = { name = it; errorMessage = null },
                label = "Full Name",
                leadingIcon = { Icon(Icons.Default.Person, null, tint = PrimaryPurple) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            AuthTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = "Email",
                leadingIcon = { Icon(Icons.Default.Email, null, tint = PrimaryPurple) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            AuthTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = "Password",
                isPassword = true,
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = PrimaryPurple) },
                imeAction = ImeAction.Done
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Role selection
            Text(
                "I want to:",
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("user" to "👤 Book Experts", "expert" to "🎓 Become an Expert").forEach { (role, label) ->
                    FilterChip(
                        selected = selectedRole == role,
                        onClick = { selectedRole = role },
                        label = { Text(label, color = if (selectedRole == role) Color.White else TextSecondary) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (role == "expert") SecondaryTeal else PrimaryPurple,
                            containerColor = SurfaceVariantDark
                        )
                    )
                }
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = StatusCancelled, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(28.dp))

            GradientButton(
                text = "Create Account",
                onClick = { viewModel.signup(name, email, password, selectedRole) },
                isLoading = signupState is UiState.Loading,
                enabled = name.isNotBlank() && email.isNotBlank() && password.length >= 6
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account? ", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onNavigateToLogin) {
                    Text("Sign In", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
