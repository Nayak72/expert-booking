package com.expertconnect.app.presentation.expert.slots

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expertconnect.app.data.repository.ExpertRepository
import com.expertconnect.app.data.remote.WebSocketClient
import com.expertconnect.app.domain.model.Expert
import com.expertconnect.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Slot Management.
 *
 * Manages the expert's weekly availability schedule.
 * State = Map<DayOfWeek, Set<TimeSlot>>
 *
 * All 12 possible time slots (08:00-20:00) per day.
 * Expert can toggle each slot on/off.
 * Saves to backend via PATCH /api/experts/{id} with availability map.
 */
@HiltViewModel
class SlotManagementViewModel @Inject constructor(
    private val expertRepository: ExpertRepository,
    private val webSocketClient: WebSocketClient
) : ViewModel() {

    companion object {
        val ALL_TIME_SLOTS = listOf(
            "08:00-09:00", "09:00-10:00", "10:00-11:00", "11:00-12:00",
            "12:00-13:00", "13:00-14:00", "14:00-15:00", "15:00-16:00",
            "16:00-17:00", "17:00-18:00", "18:00-19:00", "19:00-20:00"
        )
        val DAYS_OF_WEEK = listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")
        val DAY_LABELS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    }

    private val _expertProfile = MutableStateFlow<UiState<Expert>>(UiState.Loading)
    val expertProfile: StateFlow<UiState<Expert>> = _expertProfile.asStateFlow()

    // availability: day -> set of enabled slots
    private val _availability = MutableStateFlow<Map<String, MutableSet<String>>>(
        DAYS_OF_WEEK.associateWith { mutableSetOf() }
    )
    val availability: StateFlow<Map<String, MutableSet<String>>> = _availability.asStateFlow()

    private val _selectedDay = MutableStateFlow("monday")
    val selectedDay: StateFlow<String> = _selectedDay.asStateFlow()

    private val _saveState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val saveState: StateFlow<UiState<Unit>> = _saveState.asStateFlow()

    private var expertId: String? = null

    init {
        loadExpertProfile()
    }

    fun selectDay(day: String) {
        _selectedDay.value = day
    }

    fun toggleSlot(day: String, slot: String) {
        val current = _availability.value.toMutableMap()
        val daySlots = current[day]?.toMutableSet() ?: mutableSetOf()
        if (daySlots.contains(slot)) {
            daySlots.remove(slot)
        } else {
            daySlots.add(slot)
        }
        current[day] = daySlots
        _availability.value = current
    }

    fun isSlotEnabled(day: String, slot: String): Boolean {
        return _availability.value[day]?.contains(slot) == true
    }

    fun enableAllSlotsForDay(day: String) {
        val current = _availability.value.toMutableMap()
        current[day] = ALL_TIME_SLOTS.toMutableSet()
        _availability.value = current
    }

    fun clearAllSlotsForDay(day: String) {
        val current = _availability.value.toMutableMap()
        current[day] = mutableSetOf()
        _availability.value = current
    }

    fun saveAvailability() {
        val id = expertId ?: return
        viewModelScope.launch {
            _saveState.value = UiState.Loading
            val availabilityMap = _availability.value.mapValues { (_, slots) -> slots.sorted() }
            expertRepository.updateExpertAvailability(id, availabilityMap)
                .onSuccess { _saveState.value = UiState.Success(Unit) }
                .onFailure { _saveState.value = UiState.Error(it.message ?: "Save failed") }
        }
    }

    fun resetSaveState() { _saveState.value = UiState.Idle }

    private fun loadExpertProfile() {
        viewModelScope.launch {
            _expertProfile.value = UiState.Loading
            expertRepository.getMyExpertProfile()
                .onSuccess { expert ->
                    expertId = expert.id
                    _expertProfile.value = UiState.Success(expert)

                    // Populate availability from existing profile
                    val existingAvailability = expert.availability
                    if (existingAvailability.isNotEmpty()) {
                        val populated = DAYS_OF_WEEK.associateWith { day ->
                            existingAvailability[day]?.toMutableSet() ?: mutableSetOf()
                        }
                        _availability.value = populated
                    }

                    // Connect WebSocket for real-time slot updates
                    webSocketClient.connect(expert.id)
                }
                .onFailure {
                    _expertProfile.value = UiState.Error(it.message ?: "Failed to load profile")
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketClient.disconnect()
    }
}
