package com.expertconnect.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expertconnect.app.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpertProfileViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _expertId = MutableStateFlow<String?>(null)

    val bio = MutableStateFlow("")
    val expertise = MutableStateFlow("")
    val skills = MutableStateFlow("")
    val languages = MutableStateFlow("")
    val pricing = MutableStateFlow(0.0)

    init {
        loadExpertProfile()
    }

    private fun loadExpertProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getMyExpertProfile()
                if (response.isSuccessful && response.body() != null) {
                    val expert = response.body()!!
                    _expertId.value = expert.id
                    bio.value = expert.bio ?: ""
                    expertise.value = expert.expertise ?: ""
                    skills.value = expert.skills ?: ""
                    languages.value = expert.languages ?: ""
                    pricing.value = expert.pricing ?: 0.0
                } else {
                    _errorMessage.value = "Failed to load expert profile"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            if (expertise.value.trim().length < 2) {
                _errorMessage.value = "Expertise must be at least 2 characters"
                return@launch
            }
            
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {
                val updateData = mapOf(
                    "bio" to bio.value.trim(),
                    "expertise" to expertise.value.trim(),
                    "skills" to skills.value.trim(),
                    "languages" to languages.value.trim(),
                    "pricing" to pricing.value
                )
                
                val id = _expertId.value
                val response = if (id != null) {
                    apiService.updateExpertProfile(id, updateData)
                } else {
                    apiService.createExpertProfile(updateData)
                }

                if (response.isSuccessful) {
                    if (id == null && response.body() != null) {
                        _expertId.value = response.body()!!.id
                    }
                    _successMessage.value = "Expert profile saved successfully!"
                } else {
                    _errorMessage.value = "Failed to save: ${response.message()}"
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
}
