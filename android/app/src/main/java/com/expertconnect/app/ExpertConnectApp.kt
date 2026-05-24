package com.expertconnect.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for ExpertConnect.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 * Also sets up the FCM notification channel.
 */
@HiltAndroidApp
class ExpertConnectApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    /**
     * Create the default notification channel for FCM messages.
     * Required for Android 8.0 (API 26) and above.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "ExpertConnect Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Booking confirmations, reminders, and updates"
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "expertconnect_notifications"
    }
}
