@file:OptIn(ExperimentalMaterial3Api::class)

package com.expertconnect.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
 * User Home Screen — marketplace/discovery experience.
 *
 * Sections:
 * 1. Hero header with greeting + search shortcut
 * 2. Category quick-filter chips
 * 3. Upcoming sessions (if any)
 * 4. ✨ Recommended For You (ML-powered)
 * 5. 🔥 Trending Experts
 * 6. 🏆 Top Rated Experts
 */
@Composable
fun UserHomeScreen(
    userName: String = "User",
    onExpertClick: (String) -> Unit,
    onSeeAllExperts: () -> Unit,
    viewModel: UserHomeViewModel = hiltViewModel()
) {
    val featuredState by viewModel.featuredExperts.collectAsState()
    val trendingState by viewModel.trendingExperts.collectAsState()
    val recommendationState by viewModel.recommendations.collectAsState()
    val upcomingState by viewModel.upcomingBookings.collectAsState()
    val isRefreshing = featuredState is UiState.Loading
    
    val currentUserName by viewModel.userName.collectAsState(initial = userName)
    val displayUserName = currentUserName ?: userName

    val categories = listOf("All", "Tech", "AI/ML", "Business", "Health", "Finance", "Design", "Law")
    var selectedCategory by remember { mutableStateOf("All") }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.loadHomeData() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // ── Hero Header ────────────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    PrimaryPurple.copy(alpha = 0.35f),
                                    PrimaryPurple.copy(alpha = 0.1f),
                                    BackgroundDark
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "Hello, ${displayUserName.split(" ").firstOrNull() ?: "User"} 👋",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Find your perfect expert today",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                            IconButton(
                                onClick = {},
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(SurfaceVariantDark, RoundedCornerShape(12.dp))
                            ) {
                                Icon(Icons.Default.Notifications, "Notifications", tint = TextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick stats row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            UserHomeStatCard(
                                emoji = "📅",
                                label = "Sessions",
                                value = if (upcomingState is UiState.Success)
                                    "${(upcomingState as UiState.Success).data.size}" else "–",
                                modifier = Modifier.weight(1f)
                            )
                            UserHomeStatCard(
                                emoji = "⭐",
                                label = "Top Rated",
                                value = if (featuredState is UiState.Success) "20+" else "–",
                                modifier = Modifier.weight(1f)
                            )
                            UserHomeStatCard(
                                emoji = "🤖",
                                label = "For You",
                                value = if (recommendationState is UiState.Success)
                                    "${(recommendationState as UiState.Success).data.size}" else "–",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // ── Category Chips ─────────────────────────────────────────────────
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = {
                                Text(
                                    category,
                                    color = if (selectedCategory == category) Color.White else TextSecondary,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryPurple,
                                containerColor = SurfaceVariantDark
                            )
                        )
                    }
                }
            }

            // ── Upcoming Sessions ──────────────────────────────────────────────
            when (val state = upcomingState) {
                is UiState.Success -> {
                    if (state.data.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "📅 Upcoming Sessions",
                                onSeeAllClick = null
                            )
                        }
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(state.data.take(3)) { booking ->
                                    UserSessionMiniCard(
                                        expertName = booking.expertName ?: "Expert",
                                        expertise = booking.expertExpertise ?: "",
                                        date = booking.bookingDate.toDisplayDate(),
                                        slot = booking.slot,
                                        status = booking.status
                                    )
                                }
                            }
                        }
                    }
                }
                else -> {}
            }

            // ── Recommended For You ────────────────────────────────────────────
            item {
                SectionHeader(
                    title = "✨ Recommended For You",
                    onSeeAllClick = onSeeAllExperts
                )
            }
            when (val state = recommendationState) {
                is UiState.Loading -> item { ShimmerHomeSection() }
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        item {
                            EmptyState(
                                emoji = "🤖",
                                title = "No recommendations yet",
                                subtitle = "Book sessions to get personalized picks"
                            )
                        }
                    } else {
                        items(state.data.take(4)) { expert ->
                            ExpertCard(
                                expert = expert,
                                onClick = { onExpertClick(expert.id) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                is UiState.Error -> item {
                    ErrorState(message = state.message, onRetry = { viewModel.loadHomeData() })
                }
                else -> {}
            }

            // ── Trending Experts ───────────────────────────────────────────────
            item {
                SectionHeader(title = "🔥 Trending", onSeeAllClick = onSeeAllExperts)
            }
            when (val state = trendingState) {
                is UiState.Loading -> item { ShimmerHomeSection() }
                is UiState.Success -> {
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.data.take(6)) { expert ->
                                TrendingExpertCard(
                                    name = expert.userName ?: "Expert",
                                    expertise = expert.expertise,
                                    rating = expert.averageRating,
                                    price = expert.pricing,
                                    onClick = { onExpertClick(expert.id) }
                                )
                            }
                        }
                    }
                }
                else -> {}
            }

            // ── Top Rated ──────────────────────────────────────────────────────
            item {
                SectionHeader(title = "🏆 Top Rated", onSeeAllClick = onSeeAllExperts)
            }
            when (val state = featuredState) {
                is UiState.Loading -> item { ShimmerHomeSection() }
                is UiState.Success -> {
                    items(state.data.items.take(5)) { expert ->
                        ExpertCard(
                            expert = expert,
                            onClick = { onExpertClick(expert.id) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
                is UiState.Error -> item {
                    ErrorState(message = state.message, onRetry = { viewModel.loadHomeData() })
                }
                else -> {}
            }
        }
    }
}

// ── Sub-composables ─────────────────────────────────────────────────────────────

@Composable
private fun UserHomeStatCard(emoji: String, label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Text(text = value, style = MaterialTheme.typography.titleMedium, color = PrimaryPurple, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
private fun UserSessionMiniCard(
    expertName: String,
    expertise: String,
    date: String,
    slot: String,
    status: String
) {
    Card(
        modifier = Modifier.width(170.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("📅", fontSize = 22.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(expertName, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(expertise, style = MaterialTheme.typography.bodySmall, color = PrimaryPurpleLight, maxLines = 1)
            Spacer(modifier = Modifier.height(8.dp))
            Text(date, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(slot, style = MaterialTheme.typography.labelSmall, color = SecondaryTeal)
        }
    }
}

@Composable
private fun TrendingExpertCard(
    name: String,
    expertise: String,
    rating: Double,
    price: Double,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.width(150.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(
                        Brush.linearGradient(listOf(PrimaryPurple, SecondaryTeal)),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    name.firstOrNull()?.uppercase() ?: "E",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                name,
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                expertise,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = AccentAmber, modifier = Modifier.size(12.dp))
                    Text("%.1f".format(rating), style = MaterialTheme.typography.labelSmall, color = AccentAmber)
                }
                Text("$${price.toInt()}/hr", style = MaterialTheme.typography.labelSmall, color = PrimaryPurpleLight)
            }
        }
    }
}
