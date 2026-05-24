package com.expertconnect.app.presentation.expert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expertconnect.app.data.repository.ExpertRepository
import com.expertconnect.app.data.repository.FavoriteRepository
import com.expertconnect.app.data.repository.ReviewRepository
import com.expertconnect.app.domain.model.Expert
import com.expertconnect.app.domain.model.PaginatedResult
import com.expertconnect.app.domain.model.RatingSummary
import com.expertconnect.app.domain.model.Review
import com.expertconnect.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Expert List and Expert Detail screens.
 */
@HiltViewModel
class ExpertViewModel @Inject constructor(
    private val expertRepository: ExpertRepository,
    private val favoriteRepository: FavoriteRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _expertsState = MutableStateFlow<UiState<PaginatedResult<Expert>>>(UiState.Idle)
    val expertsState: StateFlow<UiState<PaginatedResult<Expert>>> = _expertsState.asStateFlow()

    private val _expertDetailState = MutableStateFlow<UiState<Expert>>(UiState.Idle)
    val expertDetailState: StateFlow<UiState<Expert>> = _expertDetailState.asStateFlow()

    private val _reviewsState = MutableStateFlow<UiState<List<Review>>>(UiState.Idle)
    val reviewsState: StateFlow<UiState<List<Review>>> = _reviewsState.asStateFlow()

    private val _ratingSummary = MutableStateFlow<RatingSummary?>(null)
    val ratingSummary: StateFlow<RatingSummary?> = _ratingSummary.asStateFlow()

    private val _isFavorited = MutableStateFlow(false)
    val isFavorited: StateFlow<Boolean> = _isFavorited.asStateFlow()

    private val _bookedSlots = MutableStateFlow<List<String>>(emptyList())
    val bookedSlots: StateFlow<List<String>> = _bookedSlots.asStateFlow()

    // Current filter state
    var currentSearch: String = ""
    var currentCategory: String? = null

    init {
        loadExperts()
    }

    fun loadExperts(
        search: String? = null,
        expertise: String? = null,
        category: String? = null,
        sortBy: String = "average_rating",
        page: Int = 1
    ) {
        viewModelScope.launch {
            _expertsState.value = UiState.Loading
            val result = expertRepository.getExperts(
                search = search,
                expertise = expertise,
                category = category,
                sortBy = sortBy,
                page = page
            )
            _expertsState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to load experts") }
            )
        }
    }

    fun loadExpertDetail(expertId: String) {
        viewModelScope.launch {
            _expertDetailState.value = UiState.Loading
            val result = expertRepository.getExpert(expertId)
            _expertDetailState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Expert not found") }
            )
        }
    }

    fun loadReviews(expertId: String) {
        viewModelScope.launch {
            val result = reviewRepository.getExpertReviews(expertId)
            result.onSuccess { paginated ->
                _reviewsState.value = UiState.Success(paginated.items)
            }
            val summaryResult = reviewRepository.getRatingSummary(expertId)
            summaryResult.onSuccess { _ratingSummary.value = it }
        }
    }

    fun checkFavoriteStatus(expertId: String) {
        viewModelScope.launch {
            _isFavorited.value = favoriteRepository.isFavorited(expertId)
        }
    }

    fun toggleFavorite(expertId: String) {
        viewModelScope.launch {
            if (_isFavorited.value) {
                favoriteRepository.removeFavorite(expertId)
                _isFavorited.value = false
            } else {
                favoriteRepository.addFavorite(expertId)
                _isFavorited.value = true
            }
        }
    }

    fun loadBookedSlots(expertId: String, date: String) {
        viewModelScope.launch {
            val result = expertRepository.getBookedSlots(expertId, date)
            result.onSuccess { _bookedSlots.value = it }
        }
    }

    fun search(query: String) {
        currentSearch = query
        loadExperts(search = query.ifBlank { null }, category = currentCategory)
    }

    fun filterByCategory(category: String?) {
        currentCategory = category
        loadExperts(search = currentSearch.ifBlank { null }, category = category)
    }
}
