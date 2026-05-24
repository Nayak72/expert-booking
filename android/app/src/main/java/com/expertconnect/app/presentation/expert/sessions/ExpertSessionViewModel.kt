package com.expertconnect.app.presentation.expert.sessions

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
 * ViewModel for Expert Sessions Management.
 * Loads bookings received by this expert, filtered by status.
 *
 * Separate from BookingViewModel (user-side booking management).
 */
@HiltViewModel
class ExpertSessionViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _sessionsState = MutableStateFlow<UiState<PaginatedResult<Booking>>>(UiState.Idle)
    val sessionsState: StateFlow<UiState<PaginatedResult<Booking>>> = _sessionsState.asStateFlow()

    private val _cancelState = MutableStateFlow<UiState<Booking>>(UiState.Idle)
    val cancelState: StateFlow<UiState<Booking>> = _cancelState.asStateFlow()

    private val _confirmState = MutableStateFlow<UiState<Booking>>(UiState.Idle)
    val confirmState: StateFlow<UiState<Booking>> = _confirmState.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0=upcoming, 1=completed, 2=cancelled
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    init {
        loadSessions()
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
        loadSessions()
    }

    fun loadSessions() {
        val status = when (_selectedTab.value) {
            1 -> "completed"
            2 -> "cancelled"
            else -> null
        }
        viewModelScope.launch {
            _sessionsState.value = UiState.Loading
            bookingRepository.getBookingHistory(status = status, pageSize = 50)
                .onSuccess { _sessionsState.value = UiState.Success(it) }
                .onFailure { _sessionsState.value = UiState.Error(it.message ?: "Failed to load sessions") }
        }
    }

    fun cancelSession(bookingId: String) {
        viewModelScope.launch {
            _cancelState.value = UiState.Loading
            bookingRepository.cancelBooking(bookingId, "Rejected by expert")
                .onSuccess {
                    _cancelState.value = UiState.Success(it)
                    loadSessions()
                }
                .onFailure { _cancelState.value = UiState.Error(it.message ?: "Cancel failed") }
        }
    }

    fun confirmSession(bookingId: String) {
        viewModelScope.launch {
            _confirmState.value = UiState.Loading
            bookingRepository.confirmBooking(bookingId)
                .onSuccess {
                    _confirmState.value = UiState.Success(it)
                    loadSessions()
                }
                .onFailure { _confirmState.value = UiState.Error(it.message ?: "Confirm failed") }
        }
    }

    fun resetCancelState() { _cancelState.value = UiState.Idle }
    fun resetConfirmState() { _confirmState.value = UiState.Idle }
}
