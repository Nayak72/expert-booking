package com.expertconnect.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expertconnect.app.data.repository.BookingRepository
import com.expertconnect.app.data.repository.ExpertRepository
import com.expertconnect.app.data.repository.RecommendationRepository
import com.expertconnect.app.domain.model.Booking
import com.expertconnect.app.domain.model.Expert
import com.expertconnect.app.domain.model.PaginatedResult
import com.expertconnect.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for User Home Screen (marketplace discovery).
 * Loads featured experts, recommendations, trending, and upcoming sessions in parallel.
 */
@HiltViewModel
class UserHomeViewModel @Inject constructor(
    private val expertRepository: ExpertRepository,
    private val recommendationRepository: RecommendationRepository,
    private val bookingRepository: BookingRepository,
    private val dataStoreManager: com.expertconnect.app.utils.DataStoreManager
) : ViewModel() {

    private val _featuredExperts = MutableStateFlow<UiState<PaginatedResult<Expert>>>(UiState.Idle)
    val featuredExperts: StateFlow<UiState<PaginatedResult<Expert>>> = _featuredExperts.asStateFlow()

    private val _trendingExperts = MutableStateFlow<UiState<List<Expert>>>(UiState.Idle)
    val trendingExperts: StateFlow<UiState<List<Expert>>> = _trendingExperts.asStateFlow()

    private val _recommendations = MutableStateFlow<UiState<List<Expert>>>(UiState.Idle)
    val recommendations: StateFlow<UiState<List<Expert>>> = _recommendations.asStateFlow()

    private val _upcomingBookings = MutableStateFlow<UiState<List<Booking>>>(UiState.Idle)
    val upcomingBookings: StateFlow<UiState<List<Booking>>> = _upcomingBookings.asStateFlow()

    val userName = dataStoreManager.userName

    init { loadHomeData() }

    fun loadHomeData() {
        viewModelScope.launch {
            // Load in parallel
            val featuredJob = async {
                _featuredExperts.value = UiState.Loading
                expertRepository.getExperts(sortBy = "average_rating", sortOrder = "desc", pageSize = 10)
                    .onSuccess { _featuredExperts.value = UiState.Success(it) }
                    .onFailure { _featuredExperts.value = UiState.Error(it.message ?: "Error") }
            }
            val trendingJob = async {
                _trendingExperts.value = UiState.Loading
                expertRepository.getExperts(sortBy = "total_bookings", sortOrder = "desc", pageSize = 6)
                    .onSuccess { _trendingExperts.value = UiState.Success(it.items) }
                    .onFailure { _trendingExperts.value = UiState.Error(it.message ?: "Error") }
            }
            val recoJob = async {
                _recommendations.value = UiState.Loading
                recommendationRepository.getRecommendations(topN = 6)
                    .onSuccess { _recommendations.value = UiState.Success(it) }
                    .onFailure { _recommendations.value = UiState.Error(it.message ?: "Error") }
            }
            val upcomingJob = async {
                bookingRepository.getUpcomingBookings()
                    .onSuccess { _upcomingBookings.value = UiState.Success(it) }
                    .onFailure { /* no-op */ }
            }
            featuredJob.await()
            trendingJob.await()
            recoJob.await()
            upcomingJob.await()
        }
    }
}
