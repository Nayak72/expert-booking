package com.expertconnect.app.utils

/**
 * Application-wide constants.
 * Update BASE_URL and WS_BASE_URL in build.gradle.kts for production.
 */
object Constants {
    // API endpoints (values injected from BuildConfig)
    const val DATASTORE_PREFS_NAME = "expertconnect_prefs"
    const val TOKEN_KEY = "jwt_token"
    const val USER_ID_KEY = "user_id"
    const val USER_ROLE_KEY = "user_role"
    const val USER_NAME_KEY = "user_name"

    // Notification
    const val FCM_TOKEN_KEY = "fcm_token"

    // Pagination
    const val DEFAULT_PAGE_SIZE = 20

    // Booking status
    const val STATUS_PENDING = "pending"
    const val STATUS_CONFIRMED = "confirmed"
    const val STATUS_CANCELLED = "cancelled"
    const val STATUS_COMPLETED = "completed"

    // User roles
    const val ROLE_USER = "user"
    const val ROLE_EXPERT = "expert"
}
