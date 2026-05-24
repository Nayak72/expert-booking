package com.expertconnect.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expertconnect.app.data.repository.AuthRepository
import com.expertconnect.app.domain.model.AuthToken
import com.expertconnect.app.domain.model.User
import com.expertconnect.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for authentication screens (Splash, Login, Signup).
 * Manages login/signup state using StateFlow + UiState pattern.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<UiState<AuthToken>>(UiState.Idle)
    val loginState: StateFlow<UiState<AuthToken>> = _loginState.asStateFlow()

    private val _signupState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val signupState: StateFlow<UiState<User>> = _signupState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            val result = authRepository.login(email.trim(), password)
            _loginState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Login failed") }
            )
        }
    }

    fun signup(name: String, email: String, password: String, role: String = "user") {
        viewModelScope.launch {
            _signupState.value = UiState.Loading
            val result = authRepository.signup(name, email.trim(), password, role)
            _signupState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Signup failed") }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun resetLoginState() { _loginState.value = UiState.Idle }
    fun resetSignupState() { _signupState.value = UiState.Idle }
}
