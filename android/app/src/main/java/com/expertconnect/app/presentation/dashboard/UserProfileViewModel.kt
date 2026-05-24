package com.expertconnect.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expertconnect.app.data.remote.ApiService
import com.expertconnect.app.utils.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val apiService: ApiService,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    init {
        viewModelScope.launch {
            _userName.value = dataStoreManager.userName.first() ?: ""
        }
    }

    fun updateName(newName: String) {
        _userName.value = newName
    }

    fun saveProfile() {
        if (_userName.value.isBlank()) {
            _errorMessage.value = "Name cannot be empty"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {
                val updateData = mapOf("name" to _userName.value)
                val response = apiService.updateMe(updateData)

                if (response.isSuccessful && response.body() != null) {
                    val updatedUser = response.body()!!
                    // Update local datastore
                    dataStoreManager.saveAuthSession(
                        token = dataStoreManager.authToken.first() ?: "",
                        userId = updatedUser.id,
                        role = updatedUser.role,
                        name = updatedUser.name
                    )
                    _successMessage.value = "Profile updated successfully!"
                } else {
                    _errorMessage.value = "Failed to update profile: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearMessages() {
        _successMessage.value = null
        _errorMessage.value = null
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            dataStoreManager.clearSession()
            onSuccess()
        }
    }
}
