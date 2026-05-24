package com.expertconnect.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expertconnect.app.data.repository.AuthRepository
import com.expertconnect.app.data.repository.BookingRepository
import com.expertconnect.app.data.repository.FavoriteRepository
import com.expertconnect.app.data.repository.RecommendationRepository
import com.expertconnect.app.domain.model.Booking
import com.expertconnect.app.domain.model.Expert
import com.expertconnect.app.domain.model.Favorite
import com.expertconnect.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for User Dashboard.
 * Loads upcoming sessions, booking stats, recommendations, and favorites.
 */
import com.expertconnect.app.utils.DataStoreManager
import kotlinx.coroutines.flow.first

@HiltViewModel
class UserDashboardViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val recommendationRepository: RecommendationRepository,
    private val favoriteRepository: FavoriteRepository,
    private val dataStoreManager: DataStoreManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _upcomingBookings = MutableStateFlow<UiState<List<Booking>>>(UiState.Idle)
    val upcomingBookings: StateFlow<UiState<List<Booking>>> = _upcomingBookings.asStateFlow()

    private val _recommendations = MutableStateFlow<UiState<List<Expert>>>(UiState.Idle)
    val recommendations: StateFlow<UiState<List<Expert>>> = _recommendations.asStateFlow()

    private val _favorites = MutableStateFlow<UiState<List<Favorite>>>(UiState.Idle)
    val favorites: StateFlow<UiState<List<Favorite>>> = _favorites.asStateFlow()

    private val _bookingStats = MutableStateFlow<Map<String, Int>>(emptyMap())
    val bookingStats: StateFlow<Map<String, Int>> = _bookingStats.asStateFlow()

    val userRole = dataStoreManager.userRole
    val userName = dataStoreManager.userName

    init { loadDashboardData() }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            dataStoreManager.clearSession()
            onSuccess()
        }
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            // Silently sync user profile in the background so names display correctly
            authRepository.getMe()

            // Observe cached upcoming bookings to keep UI in sync automatically
            viewModelScope.launch {
                bookingRepository.cachedUpcomingBookings.collect { bookings ->
                    _upcomingBookings.value = UiState.Success(bookings)
                }
            }

            // Observe all cached bookings to keep stats in sync automatically
            viewModelScope.launch {
                bookingRepository.cachedBookings.collect { bookings ->
                    if (bookings.isNotEmpty()) {
                        val stats = bookings.groupBy { it.status }.mapValues { it.value.size }
                        _bookingStats.value = stats
                    }
                }
            }

            // Trigger API refresh
            viewModelScope.launch {
                bookingRepository.getUpcomingBookings().onFailure {
                    if (_upcomingBookings.value !is UiState.Success) {
                        _upcomingBookings.value = UiState.Error(it.message ?: "Error")
                    }
                }
            }

            viewModelScope.launch {
                bookingRepository.getBookingHistory(pageSize = 100)
            }

            // Recommendations
            _recommendations.value = UiState.Loading
            recommendationRepository.getRecommendations(topN = 5).onSuccess {
                _recommendations.value = UiState.Success(it)
            }.onFailure {
                _recommendations.value = UiState.Error(it.message ?: "Error")
            }

            // Favorites
            _favorites.value = UiState.Loading
            favoriteRepository.getFavorites().onSuccess {
                _favorites.value = UiState.Success(it)
            }.onFailure {
                _favorites.value = UiState.Error(it.message ?: "Error")
            }
        }
    }
}
