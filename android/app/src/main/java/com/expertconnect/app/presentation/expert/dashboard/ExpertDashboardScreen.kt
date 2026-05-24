@file:OptIn(ExperimentalMaterial3Api::class)

package com.expertconnect.app.presentation.expert.dashboard

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import com.expertconnect.app.domain.model.Booking
import com.expertconnect.app.domain.model.Expert
import com.expertconnect.app.domain.model.Review
import com.expertconnect.app.presentation.components.EmptyState
import com.expertconnect.app.presentation.components.SectionHeader
import com.expertconnect.app.presentation.components.StatusBadge
import com.expertconnect.app.ui.theme.*
import com.expertconnect.app.utils.UiState
import com.expertconnect.app.utils.toDisplayDate

/**
 * Expert Dashboard Screen — analytics, management console feel.
 *
 * COMPLETELY DIFFERENT from UserDashboardScreen.
 * This is a productivity/analytics-first layout, NOT a marketplace.
 *
 * Sections:
 * 1. Welcome header with expert branding
 * 2. KPI analytics cards (total bookings, avg rating, completion rate, pending)
 * 3. Upcoming sessions (expert's received bookings)
 * 4. Recent reviews received
 * 5. Quick action shortcuts
 */
@Composable
fun ExpertDashboardScreen(
    expertName: String = "Expert",
    viewModel: ExpertDashboardViewModel = hiltViewModel()
) {
    val expertProfile by viewModel.expertProfile.collectAsState()
    val upcomingSessions by viewModel.upcomingSessions.collectAsState()
    val recentReviews by viewModel.recentReviews.collectAsState()
    val bookingStats by viewModel.bookingStats.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    
    val currentExpertName by viewModel.expertName.collectAsState(initial = expertName)
    val displayExpertName = currentExpertName ?: expertName

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.loadDashboard() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // ── Expert Welcome Header ──────────────────────────────────────────
            item {
                ExpertDashboardHeader(
                    expertName = displayExpertName,
                    expertProfile = (expertProfile as? UiState.Success)?.data
                )
            }

            // ── KPI Analytics Cards ────────────────────────────────────────────
            item {
                val expert = (expertProfile as? UiState.Success)?.data
                ExpertKpiSection(
                    expert = expert,
                    bookingStats = bookingStats
                )
            }

            // ── Upcoming Sessions ──────────────────────────────────────────────
            item {
                SectionHeader(
                    title = "📅 Upcoming Sessions",
                    onSeeAllClick = null
                )
            }
            when (val state = upcomingSessions) {
                is UiState.Loading -> item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SecondaryTeal, modifier = Modifier.size(28.dp))
                    }
                }
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        item {
                            EmptyState(
                                emoji = "📅",
                                title = "No upcoming sessions",
                                subtitle = "Sessions booked by users will appear here"
                            )
                        }
                    } else {
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.data.take(5)) { booking ->
                                    ExpertSessionCard(booking = booking)
                                }
                            }
                        }
                    }
                }
                else -> {}
            }

            // ── Recent Reviews Received ────────────────────────────────────────
            item {
                SectionHeader(
                    title = "⭐ Recent Reviews",
                    onSeeAllClick = null
                )
            }
            when (val state = recentReviews) {
                is UiState.Loading -> item {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SecondaryTeal, modifier = Modifier.size(24.dp))
                    }
                }
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        item { EmptyState(emoji = "📝", title = "No reviews yet", subtitle = "Complete sessions to receive reviews") }
                    } else {
                        items(state.data) { review ->
                            ExpertReviewCard(review = review)
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

// ── Sub-components ─────────────────────────────────────────────────────────────

@Composable
private fun ExpertDashboardHeader(expertName: String, expertProfile: Expert?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SecondaryTeal.copy(alpha = 0.35f),
                        PrimaryPurple.copy(alpha = 0.15f),
                        BackgroundDark
                    )
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Expert Console 🎓",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Welcome, ${expertName.split(" ").firstOrNull() ?: "Expert"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(SecondaryTeal, PrimaryPurple))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = expertName.firstOrNull()?.uppercase() ?: "E",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                }
            }

            if (expertProfile != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ExpertQuickStat("⭐", "%.1f".format(expertProfile.averageRating), "Rating")
                        ExpertQuickStat("📚", "${expertProfile.experience}yr", "Experience")
                        ExpertQuickStat("💰", "$${expertProfile.pricing.toInt()}/hr", "Rate")
                        ExpertQuickStat(
                            if (expertProfile.isAvailable == 1) "🟢" else "🔴",
                            if (expertProfile.isAvailable == 1) "Active" else "Away",
                            "Status"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpertQuickStat(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 16.sp)
        Text(value, style = MaterialTheme.typography.labelLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

@Composable
private fun ExpertKpiSection(expert: Expert?, bookingStats: Map<String, Int>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            "📊 Analytics Overview",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Row 1: Primary KPIs
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AnalyticsCard(
                icon = Icons.Default.CalendarMonth,
                value = bookingStats.values.sum().toString(),
                label = "Total Sessions",
                gradient = listOf(Color(0xFF6C63FF), Color(0xFF9C89FF)),
                modifier = Modifier.weight(1f)
            )
            AnalyticsCard(
                icon = Icons.Default.Star,
                value = expert?.let { "%.1f".format(it.averageRating) } ?: "—",
                label = "Avg Rating",
                gradient = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24)),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 2: Secondary KPIs
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AnalyticsCard(
                icon = Icons.Default.CheckCircle,
                value = (bookingStats["completed"] ?: 0).toString(),
                label = "Completed",
                gradient = listOf(Color(0xFF10B981), Color(0xFF34D399)),
                modifier = Modifier.weight(1f)
            )
            AnalyticsCard(
                icon = Icons.Default.Pending,
                value = (bookingStats["confirmed"] ?: 0).toString(),
                label = "Upcoming",
                gradient = listOf(Color(0xFF3B82F6), Color(0xFF60A5FA)),
                modifier = Modifier.weight(1f)
            )
            AnalyticsCard(
                icon = Icons.Default.RateReview,
                value = expert?.totalReviews?.toString() ?: "0",
                label = "Reviews",
                gradient = listOf(Color(0xFFEC4899), Color(0xFFF472B6)),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AnalyticsCard(
    icon: ImageVector,
    value: String,
    label: String,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(gradient.map { it.copy(alpha = 0.18f) }),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(14.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Brush.linearGradient(gradient),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun ExpertSessionCard(booking: Booking) {
    Card(
        modifier = Modifier.width(190.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text("👤", fontSize = 24.sp)
                StatusBadge(booking.status)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                booking.userName ?: "User",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, null, tint = SecondaryTeal, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(booking.bookingDate.toDisplayDate(), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, null, tint = AccentAmber, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(booking.slot, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun ExpertReviewCard(review: Review) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SecondaryTeal.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = review.reviewerName?.firstOrNull()?.uppercase() ?: "?",
                            color = SecondaryTeal,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        review.reviewerName ?: "User",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                // Star rating display
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < review.rating.toInt()) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = AccentAmber,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            if (!review.reviewText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    review.reviewText,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
