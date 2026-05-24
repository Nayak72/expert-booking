@file:OptIn(ExperimentalMaterial3Api::class)

package com.expertconnect.app.presentation.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.expertconnect.app.presentation.components.EmptyState
import com.expertconnect.app.presentation.components.ErrorState
import com.expertconnect.app.presentation.components.StatusBadge
import com.expertconnect.app.ui.theme.*
import com.expertconnect.app.utils.UiState
import com.expertconnect.app.utils.toDisplayDate

/**
 * User Bookings Screen — the Bookings tab in user bottom navigation.
 *
 * Shows user's own bookings with tab filter (Upcoming / Completed / Cancelled).
 * This is a TAB screen — no back button, full-screen layout.
 *
 * Separate from ExpertSessionsScreen (which shows expert-received bookings).
 */
@Composable
fun UserBookingsScreen(
    viewModel: BookingViewModel = hiltViewModel()
) {
    val historyState by viewModel.historyState.collectAsState()
    val cancelState by viewModel.cancelState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    val statusForTab = listOf(null, "completed", "cancelled")

    LaunchedEffect(selectedTab) {
        viewModel.loadHistory(status = statusForTab[selectedTab])
    }

    LaunchedEffect(cancelState) {
        if (cancelState is UiState.Success) {
            viewModel.loadHistory(status = statusForTab[selectedTab])
            viewModel.resetCancelState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // ── Header ─────────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(PrimaryPurple.copy(alpha = 0.2f), BackgroundDark)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "My Bookings",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Track your expert sessions",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // ── Tab Selector ───────────────────────────────────────────────────────
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceDark,
            contentColor = PrimaryPurple,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = PrimaryPurple
                )
            }
        ) {
            listOf("Upcoming", "Completed", "Cancelled").forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title,
                            color = if (selectedTab == index) PrimaryPurple else TextTertiary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // ── Booking List ───────────────────────────────────────────────────────
        when (val state = historyState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            }
            is UiState.Success -> {
                val bookings = state.data.items
                if (bookings.isEmpty()) {
                    EmptyState(
                        emoji = when (selectedTab) {
                            1 -> "✅"
                            2 -> "❌"
                            else -> "📅"
                        },
                        title = when (selectedTab) {
                            1 -> "No completed sessions yet"
                            2 -> "No cancelled sessions"
                            else -> "No upcoming sessions"
                        },
                        subtitle = when (selectedTab) {
                            0 -> "Browse experts and book your first session"
                            else -> ""
                        }
                    )
                } else {
                    LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                        item {
                            Text(
                                "${bookings.size} booking${if (bookings.size != 1) "s" else ""}",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary,
                                modifier = Modifier
                                    .background(SurfaceVariantDark.copy(alpha = 0.4f))
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(bookings) { booking ->
                            UserBookingCard(
                                expertName = booking.expertName ?: "Expert",
                                expertise = booking.expertExpertise ?: "",
                                date = booking.bookingDate.toDisplayDate(),
                                slot = booking.slot,
                                status = booking.status,
                                notes = booking.notes,
                                meetingLink = booking.meetingLink,
                                onCancel = if (booking.status == "confirmed") {
                                    { viewModel.cancelBooking(booking.id) }
                                } else null
                            )
                        }
                    }
                }
            }
            is UiState.Error -> {
                ErrorState(
                    message = state.message,
                    onRetry = { viewModel.loadHistory(status = statusForTab[selectedTab]) }
                )
            }
            else -> {}
        }
    }
}

@Composable
private fun UserBookingCard(
    expertName: String,
    expertise: String,
    date: String,
    slot: String,
    status: String,
    notes: String?,
    meetingLink: String?,
    onCancel: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Expert info header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                Brush.linearGradient(listOf(PrimaryPurple.copy(0.3f), SecondaryTeal.copy(0.3f))),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            expertName.firstOrNull()?.uppercase() ?: "E",
                            color = PrimaryPurple,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(expertName, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(expertise, style = MaterialTheme.typography.bodySmall, color = PrimaryPurpleLight, maxLines = 1)
                    }
                }
                StatusBadge(status)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = SurfaceVariantDark, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = PrimaryPurple, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(date, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, null, tint = AccentAmber, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(slot, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }

            if (!notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "📝 $notes",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }

            if (!meetingLink.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VideoCall, null, tint = SecondaryTeal, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Meeting link ready", style = MaterialTheme.typography.labelSmall, color = SecondaryTeal)
                }
            }

            onCancel?.let {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = it,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusCancelled),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                ) {
                    Text("Cancel Booking", color = StatusCancelled)
                }
            }
        }
    }
}
