package com.expertconnect.app.presentation.expert.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expertconnect.app.data.repository.AuthRepository
import com.expertconnect.app.data.repository.BookingRepository
import com.expertconnect.app.data.repository.ExpertRepository
import com.expertconnect.app.data.repository.ReviewRepository
import com.expertconnect.app.domain.model.Booking
import com.expertconnect.app.domain.model.Expert
import com.expertconnect.app.domain.model.Review
import com.expertconnect.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Expert Dashboard.
 * Loads analytics: expert profile stats, booking breakdown, recent reviews, upcoming sessions.
 *
 * COMPLETELY DIFFERENT from UserDashboardViewModel — this is a management/analytics ViewModel.
 */
@HiltViewModel
class ExpertDashboardViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val expertRepository: ExpertRepository,
    private val reviewRepository: ReviewRepository,
    private val dataStoreManager: com.expertconnect.app.utils.DataStoreManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _expertProfile = MutableStateFlow<UiState<Expert>>(UiState.Idle)
    val expertProfile: StateFlow<UiState<Expert>> = _expertProfile.asStateFlow()

    private val _upcomingSessions = MutableStateFlow<UiState<List<Booking>>>(UiState.Idle)
    val upcomingSessions: StateFlow<UiState<List<Booking>>> = _upcomingSessions.asStateFlow()

    private val _recentReviews = MutableStateFlow<UiState<List<Review>>>(UiState.Idle)
    val recentReviews: StateFlow<UiState<List<Review>>> = _recentReviews.asStateFlow()

    private val _bookingStats = MutableStateFlow<Map<String, Int>>(emptyMap())
    val bookingStats: StateFlow<Map<String, Int>> = _bookingStats.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val expertName = dataStoreManager.userName

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _isRefreshing.value = true

            // Silently sync user profile in the background so names display correctly
            authRepository.getMe()

            // Load expert profile (for rating, total bookings, etc.)
            _expertProfile.value = UiState.Loading
            expertRepository.getMyExpertProfile().onSuccess { expert ->
                _expertProfile.value = UiState.Success(expert)

                // Load reviews for this expert
                _recentReviews.value = UiState.Loading
                reviewRepository.getExpertReviews(expert.id, pageSize = 5).onSuccess { paginated ->
                    _recentReviews.value = UiState.Success(paginated.items)
                }.onFailure {
                    _recentReviews.value = UiState.Error(it.message ?: "Failed to load reviews")
                }
            }.onFailure {
                _expertProfile.value = UiState.Error(it.message ?: "Failed to load profile")
            }

            // Load upcoming sessions (bookings this expert has received)
            _upcomingSessions.value = UiState.Loading
            bookingRepository.getUpcomingBookings().onSuccess { bookings ->
                _upcomingSessions.value = UiState.Success(bookings)
            }.onFailure {
                _upcomingSessions.value = UiState.Error(it.message ?: "Error")
            }

            // Booking breakdown stats
            bookingRepository.getBookingHistory(pageSize = 100).onSuccess { paginated ->
                val stats = paginated.items.groupBy { it.status }.mapValues { it.value.size }
                _bookingStats.value = stats
            }

            _isRefreshing.value = false
        }
    }
}
