@file:OptIn(ExperimentalMaterial3Api::class)

package com.expertconnect.app.presentation.expert.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.expertconnect.app.presentation.components.GradientButton
import com.expertconnect.app.ui.theme.*
import com.expertconnect.app.utils.UiState
import kotlinx.coroutines.delay

/**
 * Expert Profile Management Screen — full profile editor.
 *
 * The expert's dedicated Profile tab. Includes:
 * - Expert summary card (name, expertise, rating)
 * - All editable profile fields (bio, expertise, skills, languages, pricing, experience, categories)
 * - Save button with feedback
 * - Logout button
 */
@Composable
fun ExpertProfileManagementScreen(
    expertName: String = "Expert",
    onLogout: () -> Unit,
    viewModel: ExpertProfileManagementViewModel = hiltViewModel()
) {
    val expertProfile by viewModel.expertProfile.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val name by viewModel.name.collectAsState()
    val bio by viewModel.bio.collectAsState()
    val expertise by viewModel.expertise.collectAsState()
    val skills by viewModel.skills.collectAsState()
    val languages by viewModel.languages.collectAsState()
    val pricing by viewModel.pricing.collectAsState()
    val experience by viewModel.experience.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var showSaved by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(saveState) {
        when (saveState) {
            is UiState.Success -> {
                showSaved = true
                delay(2500)
                showSaved = false
                viewModel.resetSaveState()
            }
            is UiState.Error -> {
                errorMsg = (saveState as UiState.Error).message
                delay(3000)
                errorMsg = null
                viewModel.resetSaveState()
            }
            else -> {}
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = SurfaceDark,
            title = { Text("Sign Out", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to sign out of your expert account?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = { viewModel.logout(onLogout) },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusCancelled)
                ) {
                    Text("Sign Out", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // ── Expert Profile Header ──────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(SecondaryTeal.copy(alpha = 0.3f), BackgroundDark)
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(SecondaryTeal, PrimaryPurple))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = expertName.firstOrNull()?.uppercase() ?: "E",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = expertName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )

                    val expert = (expertProfile as? UiState.Success)?.data
                    if (expert != null) {
                        Text(
                            text = expert.expertise,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SecondaryTeal
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            ProfileStatBadge("⭐ %.1f".format(expert.averageRating), "Rating")
                            ProfileStatBadge("📅 ${expert.totalBookings}", "Sessions")
                            ProfileStatBadge("📝 ${expert.totalReviews}", "Reviews")
                        }
                    }
                }
            }
        }

        // ── Edit Profile Section ───────────────────────────────────────────────
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(8.dp))

                SectionDivider(title = "🎓 Professional Details")

                ExpertField(
                    label = "Name",
                    value = name,
                    onValueChange = { viewModel.name.value = it },
                    icon = Icons.Default.Badge,
                    placeholder = "Your full name"
                )

                ExpertField(
                    label = "Expertise Title",
                    value = expertise,
                    onValueChange = { viewModel.expertise.value = it },
                    icon = Icons.Default.Work,
                    placeholder = "e.g. Senior Software Engineer"
                )

                ExpertField(
                    label = "Bio",
                    value = bio,
                    onValueChange = { viewModel.bio.value = it },
                    icon = Icons.Default.Person,
                    placeholder = "Tell users about yourself...",
                    minLines = 3,
                    maxLines = 5
                )

                ExpertField(
                    label = "Skills (comma separated)",
                    value = skills,
                    onValueChange = { viewModel.skills.value = it },
                    icon = Icons.Default.Psychology,
                    placeholder = "e.g. Python, Machine Learning, Data Science"
                )

                ExpertField(
                    label = "Categories",
                    value = categories,
                    onValueChange = { viewModel.categories.value = it },
                    icon = Icons.Default.Category,
                    placeholder = "e.g. Tech, AI/ML"
                )

                Spacer(modifier = Modifier.height(8.dp))
                SectionDivider(title = "💼 Experience & Rates")

                ExpertField(
                    label = "Years of Experience",
                    value = if (experience == 0) "" else experience.toString(),
                    onValueChange = { viewModel.experience.value = it.toIntOrNull() ?: 0 },
                    icon = Icons.Default.Timeline,
                    placeholder = "e.g. 5"
                )

                ExpertField(
                    label = "Hourly Rate (USD)",
                    value = if (pricing == 0.0) "" else pricing.toString(),
                    onValueChange = { viewModel.pricing.value = it.toDoubleOrNull() ?: 0.0 },
                    icon = Icons.Default.AttachMoney,
                    placeholder = "e.g. 100"
                )

                ExpertField(
                    label = "Languages",
                    value = languages,
                    onValueChange = { viewModel.languages.value = it },
                    icon = Icons.Default.Language,
                    placeholder = "e.g. English, Hindi"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Save feedback
                if (showSaved) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = SecondaryTeal.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = SecondaryTeal, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Profile saved successfully!", color = SecondaryTeal, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                errorMsg?.let {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = StatusCancelled.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, null, tint = StatusCancelled, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(it, color = StatusCancelled, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Save button
                GradientButton(
                    text = "Save Changes",
                    onClick = { viewModel.saveProfile() },
                    isLoading = saveState is UiState.Loading,
                    gradient = listOf(SecondaryTeal, PrimaryPurple)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Logout button
                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusCancelled),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                ) {
                    Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ExpertField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    placeholder: String,
    minLines: Int = 1,
    maxLines: Int = 1
) {
    Spacer(modifier = Modifier.height(12.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextSecondary) },
        leadingIcon = { Icon(icon, null, tint = SecondaryTeal, modifier = Modifier.size(20.dp)) },
        placeholder = { Text(placeholder, color = TextTertiary) },
        modifier = Modifier.fillMaxWidth(),
        minLines = minLines,
        maxLines = maxLines,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SecondaryTeal,
            unfocusedBorderColor = TextTertiary,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedLabelColor = SecondaryTeal
        )
    )
}

@Composable
private fun SectionDivider(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            color = SecondaryTeal,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(8.dp))
        HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceVariantDark)
    }
}

@Composable
private fun ProfileStatBadge(value: String, label: String) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}
