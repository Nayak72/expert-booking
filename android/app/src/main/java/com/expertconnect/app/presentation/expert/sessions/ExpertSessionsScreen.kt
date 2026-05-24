@file:OptIn(ExperimentalMaterial3Api::class)

package com.expertconnect.app.presentation.expert.sessions

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.expertconnect.app.domain.model.Booking
import com.expertconnect.app.presentation.components.EmptyState
import com.expertconnect.app.presentation.components.ErrorState
import com.expertconnect.app.presentation.components.StatusBadge
import com.expertconnect.app.ui.theme.*
import com.expertconnect.app.utils.UiState
import com.expertconnect.app.utils.toDisplayDate

/**
 * Expert Sessions Management Screen.
 *
 * Shows the expert's RECEIVED bookings from users.
 * Tabs: Upcoming | Completed | Cancelled
 *
 * This is a management-console style screen, NOT a user-facing booking history.
 */
@Composable
fun ExpertSessionsScreen(
    viewModel: ExpertSessionViewModel = hiltViewModel()
) {
    val sessionsState by viewModel.sessionsState.collectAsState()
    val cancelState by viewModel.cancelState.collectAsState()
    val confirmState by viewModel.confirmState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    LaunchedEffect(cancelState) {
        if (cancelState is UiState.Success) {
            viewModel.resetCancelState()
        }
    }

    LaunchedEffect(confirmState) {
        if (confirmState is UiState.Success) {
            viewModel.resetConfirmState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(SecondaryTeal.copy(alpha = 0.2f), BackgroundDark)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "Session Management",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Review and manage your booked sessions",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // ── Tab Selector ──────────────────────────────────────────────────────
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceDark,
            contentColor = SecondaryTeal,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = SecondaryTeal
                )
            }
        ) {
            listOf("Upcoming", "Completed", "Cancelled").forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { viewModel.selectTab(index) },
                    text = {
                        Text(
                            title,
                            color = if (selectedTab == index) SecondaryTeal else TextTertiary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // ── Session List ──────────────────────────────────────────────────────
        when (val state = sessionsState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SecondaryTeal)
                }
            }
            is UiState.Success -> {
                val sessions = state.data.items
                if (sessions.isEmpty()) {
                    EmptyState(
                        emoji = when (selectedTab) {
                            1 -> "✅"
                            2 -> "❌"
                            else -> "📅"
                        },
                        title = when (selectedTab) {
                            1 -> "No completed sessions"
                            2 -> "No cancelled sessions"
                            else -> "No upcoming sessions"
                        },
                        subtitle = "Sessions from users will appear here"
                    )
                } else {
                    // Summary count bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceVariantDark.copy(alpha = 0.4f))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "${sessions.size} session${if (sessions.size != 1) "s" else ""} found",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }
                    LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                        items(sessions) { session ->
                            ExpertSessionManagementCard(
                                booking = session,
                                onConfirm = { viewModel.confirmSession(session.id) },
                                onCancel = { viewModel.cancelSession(session.id) }
                            )
                        }
                    }
                }
            }
            is UiState.Error -> {
                ErrorState(message = state.message, onRetry = { viewModel.loadSessions() })
            }
            else -> {}
        }
    }
}

@Composable
private fun ExpertSessionManagementCard(
    booking: Booking,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                Brush.linearGradient(listOf(SecondaryTeal.copy(0.2f), PrimaryPurple.copy(0.2f))),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = booking.userName?.firstOrNull()?.uppercase() ?: "U",
                            color = SecondaryTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            booking.userName ?: "User",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Booking ID: ${booking.id.take(8)}...",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                    }
                }
                StatusBadge(booking.status)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = SurfaceVariantDark, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Date & slot info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = SecondaryTeal, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(booking.bookingDate.toDisplayDate(), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, null, tint = AccentAmber, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(booking.slot, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }

            // Notes
            if (!booking.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark.copy(alpha = 0.5f))
                ) {
                    Text(
                        "📝 ${booking.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            // Meeting link
            if (!booking.meetingLink.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VideoCall, null, tint = PrimaryPurpleLight, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Meeting link available",
                        style = MaterialTheme.typography.labelSmall,
                        color = PrimaryPurpleLight
                    )
                }
            }

            // Confirm / Reject action (only for pending)
            if (booking.status == "pending") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusCancelled),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                    ) {
                        Icon(Icons.Default.Cancel, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reject")
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Confirm")
                    }
                }
            }

            // Cancel action (for confirmed)
            if (booking.status == "confirmed") {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusCancelled),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                ) {
                    Icon(Icons.Default.Cancel, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cancel Session")
                }
            }
        }
    }
}
