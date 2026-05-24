package com.expertconnect.app.presentation.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expertconnect.app.data.repository.BookingRepository
import com.expertconnect.app.domain.model.Booking
import com.expertconnect.app.domain.model.PaginatedResult
import com.expertconnect.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for booking screens: book session, history, cancellation.
 */
@HiltViewModel
class BookingViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _bookingState = MutableStateFlow<UiState<Booking>>(UiState.Idle)
    val bookingState: StateFlow<UiState<Booking>> = _bookingState.asStateFlow()

    private val _historyState = MutableStateFlow<UiState<PaginatedResult<Booking>>>(UiState.Idle)
    val historyState: StateFlow<UiState<PaginatedResult<Booking>>> = _historyState.asStateFlow()

    private val _cancelState = MutableStateFlow<UiState<Booking>>(UiState.Idle)
    val cancelState: StateFlow<UiState<Booking>> = _cancelState.asStateFlow()

    val cachedUpcomingBookings = bookingRepository.cachedUpcomingBookings
    val cachedBookings = bookingRepository.cachedBookings

    fun bookSession(expertId: String, date: String, slot: String, notes: String? = null) {
        viewModelScope.launch {
            _bookingState.value = UiState.Loading
            val result = bookingRepository.createBooking(expertId, date, slot, notes)
            _bookingState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Booking failed") }
            )
        }
    }

    fun loadHistory(page: Int = 1, status: String? = null) {
        viewModelScope.launch {
            _historyState.value = UiState.Loading
            val result = bookingRepository.getBookingHistory(page = page, status = status)
            _historyState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to load history") }
            )
        }
    }

    fun cancelBooking(bookingId: String, reason: String? = null) {
        viewModelScope.launch {
            _cancelState.value = UiState.Loading
            val result = bookingRepository.cancelBooking(bookingId, reason)
            _cancelState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Cancellation failed") }
            )
        }
    }

    fun resetBookingState() { _bookingState.value = UiState.Idle }
    fun resetCancelState() { _cancelState.value = UiState.Idle }
}
