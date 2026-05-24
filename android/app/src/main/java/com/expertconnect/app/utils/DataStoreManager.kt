package com.expertconnect.app.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore manager for persisting JWT token and user session data.
 * Uses Jetpack DataStore (Preferences) for async, type-safe storage.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.DATASTORE_PREFS_NAME
)

@Singleton
class DataStoreManager @Inject constructor(private val context: Context) {

    private companion object {
        val TOKEN_KEY = stringPreferencesKey(Constants.TOKEN_KEY)
        val USER_ID_KEY = stringPreferencesKey(Constants.USER_ID_KEY)
        val USER_ROLE_KEY = stringPreferencesKey(Constants.USER_ROLE_KEY)
        val USER_NAME_KEY = stringPreferencesKey(Constants.USER_NAME_KEY)
        val FCM_TOKEN_KEY = stringPreferencesKey(Constants.FCM_TOKEN_KEY)
    }

    // ── Save ─────────────────────────────────────────────────────────────────

    suspend fun saveAuthSession(token: String, userId: String, role: String, name: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_ID_KEY] = userId
            prefs[USER_ROLE_KEY] = role
            prefs[USER_NAME_KEY] = name
        }
    }

    suspend fun saveFcmToken(fcmToken: String) {
        context.dataStore.edit { prefs ->
            prefs[FCM_TOKEN_KEY] = fcmToken
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
            prefs.remove(USER_ID_KEY)
            prefs.remove(USER_ROLE_KEY)
            prefs.remove(USER_NAME_KEY)
        }
    }

    // ── Observe ───────────────────────────────────────────────────────────────

    val authToken: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    val userId: Flow<String?> = context.dataStore.data.map { it[USER_ID_KEY] }
    val userRole: Flow<String?> = context.dataStore.data.map { it[USER_ROLE_KEY] }
    val userName: Flow<String?> = context.dataStore.data.map { it[USER_NAME_KEY] }
    val fcmToken: Flow<String?> = context.dataStore.data.map { it[FCM_TOKEN_KEY] }

    /** True if a JWT token exists in storage. */
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { it[TOKEN_KEY] != null }
}
