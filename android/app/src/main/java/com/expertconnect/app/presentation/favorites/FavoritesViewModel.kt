package com.expertconnect.app.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expertconnect.app.data.repository.FavoriteRepository
import com.expertconnect.app.domain.model.Favorite
import com.expertconnect.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _favoritesState = MutableStateFlow<UiState<List<Favorite>>>(UiState.Idle)
    val favoritesState: StateFlow<UiState<List<Favorite>>> = _favoritesState.asStateFlow()

    val cachedFavorites = favoriteRepository.cachedFavorites

    init { loadFavorites() }

    fun loadFavorites() {
        viewModelScope.launch {
            _favoritesState.value = UiState.Loading
            val result = favoriteRepository.getFavorites()
            _favoritesState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to load favorites") }
            )
        }
    }

    fun removeFavorite(expertId: String) {
        viewModelScope.launch {
            favoriteRepository.removeFavorite(expertId)
            loadFavorites()
        }
    }
}
