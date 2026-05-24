package com.expertconnect.app.data.repository

import com.expertconnect.app.data.remote.ApiService
import com.expertconnect.app.data.remote.dto.LoginRequest
import com.expertconnect.app.data.remote.dto.SignupRequest
import com.expertconnect.app.domain.model.AuthToken
import com.expertconnect.app.domain.model.User
import com.expertconnect.app.utils.DataStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Auth repository: handles signup, login, session management.
 * Persists tokens to DataStore after successful auth.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val dataStoreManager: DataStoreManager
) {
    suspend fun signup(name: String, email: String, password: String, role: String): Result<User> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = apiService.signup(SignupRequest(name, email, password, role))
                if (response.isSuccessful) {
                    val dto = response.body()!!
                    User(dto.id, dto.name, dto.email, dto.role, dto.profileImage, dto.createdAt)
                } else {
                    throw Exception(response.errorBody()?.string() ?: "Signup failed")
                }
            }
        }

    suspend fun login(email: String, password: String): Result<AuthToken> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = apiService.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val dto = response.body()!!
                    // Persist session with empty name first so interceptor has the token
                    dataStoreManager.saveAuthSession(dto.accessToken, dto.userId, dto.role, "")
                    
                    val meResponse = apiService.getMe()
                    val userName = meResponse.body()?.name ?: ""
                    
                    // Update session with correct user name
                    dataStoreManager.saveAuthSession(dto.accessToken, dto.userId, dto.role, userName)
                    AuthToken(dto.accessToken, dto.tokenType, dto.role, dto.userId)
                } else {
                    throw Exception(response.errorBody()?.string() ?: "Login failed")
                }
            }
        }

    suspend fun logout() {
        dataStoreManager.clearSession()
    }

    suspend fun getMe(): Result<User> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getMe()
            if (response.isSuccessful) {
                val dto = response.body()!!
                
                // Sync DataStore with the fresh user profile
                val currentToken = dataStoreManager.authToken.firstOrNull()
                if (!currentToken.isNullOrEmpty()) {
                    dataStoreManager.saveAuthSession(currentToken, dto.id, dto.role, dto.name)
                }
                
                User(dto.id, dto.name, dto.email, dto.role, dto.profileImage, dto.createdAt)
            } else {
                throw Exception("Failed to get profile")
            }
        }
    }

    suspend fun updateMe(name: String): Result<User> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.updateMe(mapOf("name" to name))
            if (response.isSuccessful) {
                val dto = response.body()!!
                dataStoreManager.saveAuthSession(
                    dataStoreManager.authToken.firstOrNull() ?: "",
                    dto.id,
                    dto.role,
                    dto.name
                )
                User(dto.id, dto.name, dto.email, dto.role, dto.profileImage, dto.createdAt)
            } else {
                throw Exception("Failed to update profile")
            }
        }
    }

    suspend fun updateFcmToken(fcmToken: String) {
        runCatching {
            apiService.updateFcmToken(fcmToken)
            dataStoreManager.saveFcmToken(fcmToken)
        }
    }
}
