package com.expertconnect.app.presentation.expert.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expertconnect.app.data.repository.AuthRepository
import com.expertconnect.app.data.repository.ExpertRepository
import com.expertconnect.app.domain.model.Expert
import com.expertconnect.app.utils.DataStoreManager
import com.expertconnect.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Expert Profile Management Screen.
 * Full profile editing: bio, expertise, skills, languages, pricing, experience, categories.
 *
 * Enhanced version of old ExpertProfileViewModel with all fields + DataStore integration.
 */
@HiltViewModel
class ExpertProfileManagementViewModel @Inject constructor(
    private val expertRepository: ExpertRepository,
    private val authRepository: AuthRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _expertProfile = MutableStateFlow<UiState<Expert>>(UiState.Loading)
    val expertProfile: StateFlow<UiState<Expert>> = _expertProfile.asStateFlow()

    private val _saveState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val saveState: StateFlow<UiState<Unit>> = _saveState.asStateFlow()

    // Editable fields
    val name = MutableStateFlow("")
    val bio = MutableStateFlow("")
    val expertise = MutableStateFlow("")
    val skills = MutableStateFlow("")
    val languages = MutableStateFlow("")
    val pricing = MutableStateFlow(0.0)
    val experience = MutableStateFlow(0)
    val categories = MutableStateFlow("")

    private var expertId: String? = null

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _expertProfile.value = UiState.Loading
            expertRepository.getMyExpertProfile()
                .onSuccess { expert ->
                    expertId = expert.id
                    name.value = expert.userName ?: ""
                    bio.value = expert.bio ?: ""
                    expertise.value = expert.expertise
                    skills.value = expert.skills ?: ""
                    languages.value = expert.languages
                    pricing.value = expert.pricing
                    experience.value = expert.experience
                    categories.value = expert.categories ?: ""
                    _expertProfile.value = UiState.Success(expert)
                }
                .onFailure {
                    _expertProfile.value = UiState.Error(it.message ?: "Failed to load profile")
                }
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            if (name.value.trim().isEmpty()) {
                _saveState.value = UiState.Error("Name is required")
                return@launch
            }
            if (expertise.value.trim().length < 2) {
                _saveState.value = UiState.Error("Expertise must be at least 2 characters")
                return@launch
            }
            
            _saveState.value = UiState.Loading
            val updateData = mapOf(
                "bio" to bio.value.trim(),
                "expertise" to expertise.value.trim(),
                "skills" to skills.value.trim(),
                "languages" to languages.value.trim(),
                "pricing" to pricing.value,
                "experience" to experience.value,
                "categories" to categories.value.trim()
            )
            val nameUpdateResult = authRepository.updateMe(name.value)
            if (nameUpdateResult.isFailure) {
                _saveState.value = UiState.Error(nameUpdateResult.exceptionOrNull()?.message ?: "Name save failed")
                return@launch
            }
            
            val id = expertId
            if (id != null) {
                expertRepository.updateExpertProfile(id, updateData)
                    .onSuccess { _saveState.value = UiState.Success(Unit) }
                    .onFailure { _saveState.value = UiState.Error(it.message ?: "Save failed") }
            } else {
                expertRepository.createExpertProfile(updateData)
                    .onSuccess { expert ->
                        expertId = expert.id
                        _saveState.value = UiState.Success(Unit)
                    }
                    .onFailure { _saveState.value = UiState.Error(it.message ?: "Create failed") }
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            dataStoreManager.clearSession()
            onSuccess()
        }
    }

    fun resetSaveState() { _saveState.value = UiState.Idle }
}
