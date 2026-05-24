@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.expertconnect.app.presentation.expert.slots

import androidx.compose.animation.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.expertconnect.app.presentation.components.GradientButton
import com.expertconnect.app.ui.theme.*
import com.expertconnect.app.utils.UiState
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Slot Management Screen — expert's weekly availability calendar.
 *
 * Features:
 * - Day selector (Mon–Sun horizontal strip)
 * - 12 time slots per day in a grid (08:00–20:00)
 * - Toggle each slot green (available) / off (unavailable)
 * - Enable All / Clear All shortcuts per day
 * - Real-time WebSocket updates
 * - Save button persists to backend
 *
 * Visual style: productivity dashboard, calendar-inspired.
 */
@Composable
fun SlotManagementScreen(
    viewModel: SlotManagementViewModel = hiltViewModel()
) {
    val selectedDay by viewModel.selectedDay.collectAsState()
    val availability by viewModel.availability.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    var showSavedSnackbar by remember { mutableStateOf(false) }
    var showErrorSnackbar by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(saveState) {
        when (saveState) {
            is UiState.Success -> {
                showSavedSnackbar = true
                delay(2500)
                showSavedSnackbar = false
                viewModel.resetSaveState()
            }
            is UiState.Error -> {
                showErrorSnackbar = (saveState as UiState.Error).message
                delay(3000)
                showErrorSnackbar = null
                viewModel.resetSaveState()
            }
            else -> {}
        }
    }

    val enabledCount = availability[selectedDay]?.size ?: 0
    val totalSlots = SlotManagementViewModel.ALL_TIME_SLOTS.size

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(SecondaryTeal.copy(alpha = 0.25f), BackgroundDark)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "Slot Management 📅",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Set your weekly availability schedule",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Weekly overview strip
                        WeeklyOverviewStrip(availability = availability)
                    }
                }
            }

            // ── Day Selector (Calendar Popup) ──────────────────────────────────
            item {
                Column(modifier = Modifier.padding(top = 12.dp, start = 16.dp, end = 16.dp)) {
                    Text(
                        text = "Select Date to Manage Schedule",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    var showDatePicker by remember { mutableStateOf(false) }
                    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
                    
                    // Whenever selectedDate changes, update the selectedDay in ViewModel
                    LaunchedEffect(selectedDate) {
                        val dayOfWeekStr = selectedDate.dayOfWeek.name.lowercase()
                        viewModel.selectDay(dayOfWeekStr)
                    }

                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                    )

                    if (showDatePicker) {
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                                    }
                                    showDatePicker = false
                                }) {
                                    Text("OK", color = SecondaryTeal)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) {
                                    Text("Cancel", color = TextSecondary)
                                }
                            },
                            colors = DatePickerDefaults.colors(containerColor = SurfaceDark)
                        ) {
                            DatePicker(
                                state = datePickerState,
                                colors = DatePickerDefaults.colors(
                                    containerColor = SurfaceDark,
                                    titleContentColor = TextPrimary,
                                    headlineContentColor = SecondaryTeal,
                                    weekdayContentColor = TextSecondary,
                                    subheadContentColor = TextPrimary,
                                    yearContentColor = TextPrimary,
                                    currentYearContentColor = SecondaryTeal,
                                    selectedYearContainerColor = SecondaryTeal,
                                    dayContentColor = TextPrimary,
                                    disabledDayContentColor = TextTertiary,
                                    selectedDayContainerColor = SecondaryTeal,
                                    todayDateBorderColor = SecondaryTeal,
                                    todayContentColor = SecondaryTeal
                                )
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = SecondaryTeal)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy")),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                        }
                    }
                }
            }

            // ── Current Day Stats + Quick Actions ──────────────────────────────
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${SlotManagementViewModel.DAYS_OF_WEEK.zip(SlotManagementViewModel.DAY_LABELS).firstOrNull { it.first == selectedDay }?.second ?: selectedDay} Slots",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$enabledCount / $totalSlots slots enabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (enabledCount > 0) SecondaryTeal else TextTertiary
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SmallActionChip(
                            label = "All",
                            icon = Icons.Default.SelectAll,
                            onClick = { viewModel.enableAllSlotsForDay(selectedDay) }
                        )
                        SmallActionChip(
                            label = "Clear",
                            icon = Icons.Default.ClearAll,
                            onClick = { viewModel.clearAllSlotsForDay(selectedDay) },
                            tint = StatusCancelled
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── Time Slot Grid ────────────────────────────────────────────────
            item {
                FlowRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SlotManagementViewModel.ALL_TIME_SLOTS.forEach { slot ->
                        val isEnabled = viewModel.isSlotEnabled(selectedDay, slot)
                        SlotToggleChip(
                            slot = slot,
                            isEnabled = isEnabled,
                            onClick = { viewModel.toggleSlot(selectedDay, slot) }
                        )
                    }
                }
            }

            // ── Legend ────────────────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = SecondaryTeal, label = "Available")
                    LegendItem(color = SurfaceVariantDark, label = "Unavailable")
                }
            }
        }

        // ── Floating Save Button ───────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(BackgroundDark.copy(alpha = 0.95f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(visible = showSavedSnackbar) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SecondaryTeal.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = SecondaryTeal, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Availability saved successfully!", color = SecondaryTeal, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            showErrorSnackbar?.let { error ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusCancelled.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, null, tint = StatusCancelled, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(error, color = StatusCancelled, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            GradientButton(
                text = if (saveState is UiState.Loading) "Saving..." else "💾 Save Availability",
                onClick = { viewModel.saveAvailability() },
                isLoading = saveState is UiState.Loading,
                gradient = listOf(SecondaryTeal, PrimaryPurple)
            )
        }
    }
}

// ── Sub-components ─────────────────────────────────────────────────────────────

@Composable
private fun WeeklyOverviewStrip(availability: Map<String, MutableSet<String>>) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SlotManagementViewModel.DAYS_OF_WEEK.zip(SlotManagementViewModel.DAY_LABELS).forEach { (day, label) ->
                val count = availability[day]?.size ?: 0
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(label.take(1), style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (count > 0)
                                    Brush.linearGradient(listOf(SecondaryTeal.copy(count / 12f), SecondaryTeal))
                                else
                                    Brush.linearGradient(listOf(SurfaceVariantDark, SurfaceVariantDark))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (count > 0) count.toString() else "—",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (count > 0) Color.White else TextTertiary,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayChip(
    label: String,
    slotCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(72.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SecondaryTeal else SurfaceVariantDark
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) Color.White else TextSecondary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$slotCount",
                style = MaterialTheme.typography.titleSmall,
                color = if (isSelected) Color.White else if (slotCount > 0) SecondaryTeal else TextTertiary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "slots",
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) Color.White.copy(alpha = 0.7f) else TextTertiary,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun SlotToggleChip(
    slot: String,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isEnabled) SecondaryTeal.copy(alpha = 0.2f) else SurfaceVariantDark.copy(alpha = 0.6f),
        animationSpec = tween(200),
        label = "slotBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isEnabled) SecondaryTeal else TextTertiary,
        animationSpec = tween(200),
        label = "slotText"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isEnabled) {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint = SecondaryTeal,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
            }
            Text(
                text = slot,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                fontWeight = if (isEnabled) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun SmallActionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    tint: Color = SecondaryTeal
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        modifier = Modifier.height(32.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = tint),
        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(14.dp), tint = tint)
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = tint)
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

