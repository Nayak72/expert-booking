package com.expertconnect.app.utils

/**
 * Generic sealed UI state class for ViewModel state management.
 * Used across all ViewModels to represent loading, success, and error states.
 *
 * @param T The type of data in the success state.
 */
sealed class UiState<out T> {
    /** Initial idle state (before any action). */
    object Idle : UiState<Nothing>()

    /** Data is being loaded. */
    object Loading : UiState<Nothing>()

    /** Operation succeeded with data. */
    data class Success<T>(val data: T) : UiState<T>()

    /** Operation failed with an error message. */
    data class Error(val message: String) : UiState<Nothing>()
}

/** Convenience extension: returns data if Success, null otherwise. */
val <T> UiState<T>.dataOrNull: T?
    get() = (this as? UiState.Success)?.data

/** Convenience extension: returns true if currently loading. */
val UiState<*>.isLoading: Boolean
    get() = this is UiState.Loading

/** Convenience extension: returns error message or null. */
val UiState<*>.errorMessage: String?
    get() = (this as? UiState.Error)?.message
