package com.expertconnect.app.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.expertconnect.app.presentation.components.*
import com.expertconnect.app.ui.theme.*
import com.expertconnect.app.utils.UiState
import com.expertconnect.app.utils.toDisplayDate

/**
 * User Dashboard Screen — comprehensive overview of sessions, stats, recommendations.
 */
@Composable
fun UserDashboardScreen(
    userName: String = "User",
    onExpertClick: (String) -> Unit,
    onSeeBookings: () -> Unit,
    onEditProfileClick: () -> Unit,
    onEditExpertProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    viewModel: UserDashboardViewModel = hiltViewModel()
) {
    val upcomingState by viewModel.upcomingBookings.collectAsState()
    val recommendationsState by viewModel.recommendations.collectAsState()
    val favoritesState by viewModel.favorites.collectAsState()
    val bookingStats by viewModel.bookingStats.collectAsState()
    val userRole by viewModel.userRole.collectAsState(initial = "user")
    val currentUserName by viewModel.userName.collectAsState(initial = userName)
    val displayUserName = currentUserName ?: userName

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundDark),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Hero header
        item {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(PrimaryPurple.copy(alpha = 0.35f), BackgroundDark)))
                    .padding(20.dp)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        Column {
                            Text("My Dashboard 📊", style = MaterialTheme.typography.headlineSmall, color = TextPrimary, fontWeight = FontWeight.ExtraBold)
                            Text("Welcome back, ${displayUserName.split(" ").firstOrNull()}!", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                        IconButton(onClick = {
                            viewModel.logout(onSuccess = onLogoutClick)
                        }) {
                            Icon(Icons.Default.Logout, contentDescription = "Logout", tint = ErrorRed)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onEditProfileClick,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Edit Profile", color = Color.White)
                        }
                        if (userRole == "expert") {
                            Button(
                                onClick = onEditExpertProfileClick,
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Expert Profile", color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Booking stats
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(
                            Triple("📅", "Total", (bookingStats.values.sum()).toString()),
                            Triple("✅", "Confirmed", (bookingStats["confirmed"] ?: 0).toString()),
                            Triple("🎓", "Completed", (bookingStats["completed"] ?: 0).toString()),
                            Triple("❌", "Cancelled", (bookingStats["cancelled"] ?: 0).toString()),
                        ).forEach { (emoji, label, value) ->
                            DashboardStatCard(emoji = emoji, label = label, value = value, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Upcoming sessions
        item { SectionHeader(title = "Upcoming Sessions", onSeeAllClick = onSeeBookings) }
        when (val state = upcomingState) {
            is UiState.Loading -> item { Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryPurple, modifier = Modifier.size(28.dp)) } }
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    item { EmptyState(emoji = "📅", title = "No upcoming sessions", subtitle = "Book a session to get started") }
                } else {
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.data) { booking ->
                                UpcomingBookingDashCard(booking = booking)
                            }
                        }
                    }
                }
            }
            else -> {}
        }

        // Recommended experts
        item { SectionHeader(title = "✨ Recommended For You") }
        when (val state = recommendationsState) {
            is UiState.Loading -> item { ShimmerHomeSection() }
            is UiState.Success -> {
                items(state.data.take(4)) { expert ->
                    ExpertCard(
                        expert = expert,
                        onClick = { onExpertClick(expert.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
            else -> {}
        }

        // Favorited experts
        item { SectionHeader(title = "❤️ Favorites") }
        when (val state = favoritesState) {
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    item { EmptyState(emoji = "❤️", title = "No favorites yet") }
                } else {
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.data.take(10)) { fav ->
                                FavoriteMiniCard(
                                    name = fav.expertName ?: "Expert",
                                    expertise = fav.expertExpertise ?: "",
                                    onClick = { onExpertClick(fav.expertId) }
                                )
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun DashboardStatCard(emoji: String, label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark.copy(alpha = 0.8f))) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 16.sp)
            Text(value, style = MaterialTheme.typography.titleMedium, color = PrimaryPurple, fontWeight = FontWeight.ExtraBold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
private fun UpcomingBookingDashCard(booking: com.expertconnect.app.domain.model.Booking) {
    Card(
        modifier = Modifier.width(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("📅", fontSize = 22.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(booking.expertName ?: "Expert", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
            Text(booking.expertExpertise ?: "", style = MaterialTheme.typography.bodySmall, color = PrimaryPurpleLight)
            Spacer(modifier = Modifier.height(8.dp))
            Text(booking.bookingDate.toDisplayDate(), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(booking.slot, style = MaterialTheme.typography.labelSmall, color = SecondaryTeal)
        }
    }
}

@Composable
private fun FavoriteMiniCard(name: String, expertise: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(130.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(44.dp).background(Brush.linearGradient(listOf(PrimaryPurple, SecondaryTeal)), shape = androidx.compose.foundation.shape.CircleShape)) {
                Text(name.firstOrNull()?.uppercase() ?: "E", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(name, style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(expertise, style = MaterialTheme.typography.labelSmall, color = TextSecondary, maxLines = 1)
        }
    }
}
