@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.expertconnect.app.presentation.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.expertconnect.app.presentation.components.*
import com.expertconnect.app.presentation.expert.ExpertViewModel
import com.expertconnect.app.ui.theme.*
import com.expertconnect.app.utils.UiState
import com.expertconnect.app.utils.toDisplayDate
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

// All available time slots
private val ALL_SLOTS = listOf(
    "08:00-09:00", "09:00-10:00", "10:00-11:00", "11:00-12:00",
    "12:00-13:00", "13:00-14:00", "14:00-15:00", "15:00-16:00",
    "16:00-17:00", "17:00-18:00", "18:00-19:00", "19:00-20:00"
)

/**
 * Booking Screen — select date, slot, and confirm booking.
 */
@Composable
fun BookingScreen(
    expertId: String,
    expertName: String = "Expert",
    onBookingSuccess: () -> Unit,
    onBackClick: () -> Unit,
    bookingViewModel: BookingViewModel = hiltViewModel(),
    expertViewModel: ExpertViewModel = hiltViewModel()
) {
    val bookingState by bookingViewModel.bookingState.collectAsState()
    val bookedSlots by expertViewModel.bookedSlots.collectAsState()
    val expertDetailState by expertViewModel.expertDetailState.collectAsState()

    LaunchedEffect(expertId) {
        expertViewModel.loadExpertDetail(expertId)
    }

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedSlot by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // Ensure only today or future dates are selectable
                val todayStart = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                return utcTimeMillis >= todayStart
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        selectedSlot = null
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = PrimaryPurple)
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
                    headlineContentColor = PrimaryPurple,
                    weekdayContentColor = TextSecondary,
                    subheadContentColor = TextPrimary,
                    yearContentColor = TextPrimary,
                    currentYearContentColor = PrimaryPurple,
                    selectedYearContainerColor = PrimaryPurple,
                    dayContentColor = TextPrimary,
                    disabledDayContentColor = TextTertiary,
                    selectedDayContainerColor = PrimaryPurple,
                    todayDateBorderColor = PrimaryPurple,
                    todayContentColor = PrimaryPurple
                )
            )
        }
    }

    // Load booked slots when date changes
    LaunchedEffect(selectedDate) {
        expertViewModel.loadBookedSlots(expertId, selectedDate.toString())
    }

    LaunchedEffect(bookingState) {
        when (val state = bookingState) {
            is UiState.Success -> {
                onBookingSuccess()
                bookingViewModel.resetBookingState()
            }
            is UiState.Error -> {
                errorMessage = state.message
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Session", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().background(SurfaceDark).padding(16.dp)) {
                GradientButton(
                    text = if (selectedSlot != null) "Confirm Booking — $selectedSlot" else "Select a Slot",
                    onClick = {
                        selectedSlot?.let { slot ->
                            bookingViewModel.bookSession(expertId, selectedDate.toString(), slot, notes.ifBlank { null })
                        }
                    },
                    isLoading = bookingState is UiState.Loading,
                    enabled = selectedSlot != null
                )
            }
        },
        containerColor = BackgroundDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Expert info header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = PrimaryPurple, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(expertName, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text("Expert Consultation", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            }

            // Date picker
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("Select Date", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
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
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = PrimaryPurple)
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

            // Slot grid
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Available Slots", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                    val expertProfile = (expertDetailState as? UiState.Success)?.data
                    val dayOfWeekName = selectedDate.dayOfWeek.name.lowercase()
                    
                    val slotsToDisplay = if (expertProfile?.availability.isNullOrEmpty()) {
                        ALL_SLOTS
                    } else {
                        expertProfile?.availability?.get(dayOfWeekName) ?: emptyList()
                    }
                    
                    val availableCount = slotsToDisplay.size - bookedSlots.count { it in slotsToDisplay }
                    Text("${availableCount} slots available", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    if (slotsToDisplay.isEmpty()) {
                        Text("No slots available on this day.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    } else {
                        // Slot grid using FlowRow
                        androidx.compose.foundation.layout.FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            slotsToDisplay.forEach { slot ->
                                val isBooked = slot in bookedSlots
                                val isSelected = slot == selectedSlot
                                SlotChip(
                                    slot = slot,
                                    isBooked = isBooked,
                                    isSelected = isSelected,
                                    onClick = { if (!isBooked) selectedSlot = if (isSelected) null else slot }
                                )
                            }
                        }
                    }
                }
            }

            // Notes field
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("Notes (Optional)", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("What would you like to discuss?", color = TextTertiary) },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryPurple, unfocusedBorderColor = TextTertiary,
                            focusedContainerColor = SurfaceVariantDark, unfocusedContainerColor = SurfaceVariantDark
                        ),
                        minLines = 3
                    )
                }
            }

            errorMessage?.let { msg ->
                item {
                    Text(
                        text = msg,
                        color = StatusCancelled,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SlotChip(
    slot: String,
    isBooked: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = when {
        isBooked -> SurfaceVariantDark.copy(alpha = 0.4f)
        isSelected -> PrimaryPurple
        else -> SurfaceVariantDark
    }
    val textColor = when {
        isBooked -> TextTertiary
        isSelected -> Color.White
        else -> TextSecondary
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable(enabled = !isBooked, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = if (isBooked) "$slot ✗" else slot,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            textDecoration = if (isBooked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
        )
    }
}

/**
 * Booking History Screen — shows all bookings with status filter.
 */
@Composable
fun BookingHistoryScreen(
    onBackClick: () -> Unit,
    viewModel: BookingViewModel = hiltViewModel()
) {
    val historyState by viewModel.historyState.collectAsState()
    val cancelState by viewModel.cancelState.collectAsState()
    var selectedFilter by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedFilter) {
        viewModel.loadHistory(status = selectedFilter)
    }

    LaunchedEffect(cancelState) {
        if (cancelState is UiState.Success) {
            viewModel.loadHistory(status = selectedFilter)
            viewModel.resetCancelState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Bookings", color = TextPrimary) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, null, tint = TextPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        containerColor = BackgroundDark
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Status filter chips
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(null to "All", "confirmed" to "Confirmed", "cancelled" to "Cancelled", "completed" to "Completed")
                    .forEach { (status, label) ->
                        FilterChip(
                            selected = selectedFilter == status,
                            onClick = { selectedFilter = if (selectedFilter == status) null else status },
                            label = { Text(label, color = if (selectedFilter == status) Color.White else TextSecondary, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryPurple, containerColor = SurfaceVariantDark
                            )
                        )
                    }
            }

            when (val state = historyState) {
                is UiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
                is UiState.Success -> {
                    if (state.data.items.isEmpty()) {
                        EmptyState(emoji = "📅", title = "No bookings found")
                    } else {
                        LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                            items(state.data.items) { booking ->
                                BookingHistoryCard(
                                    booking = booking,
                                    onCancel = { viewModel.cancelBooking(booking.id) }
                                )
                            }
                        }
                    }
                }
                is UiState.Error -> ErrorState(message = state.message, onRetry = { viewModel.loadHistory() })
                else -> {}
            }
        }
    }
}

@Composable
private fun BookingHistoryCard(
    booking: com.expertconnect.app.domain.model.Booking,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(booking.expertName ?: "Expert", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text(booking.expertExpertise ?: "", style = MaterialTheme.typography.bodySmall, color = PrimaryPurpleLight)
                }
                StatusBadge(booking.status)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = SecondaryTeal, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(booking.bookingDate.toDisplayDate(), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, null, tint = AccentAmber, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(booking.slot, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
            if (booking.status == "confirmed") {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onCancel,
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
