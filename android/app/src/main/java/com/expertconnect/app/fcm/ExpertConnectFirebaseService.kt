package com.expertconnect.app.fcm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.expertconnect.app.ExpertConnectApp
import com.expertconnect.app.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log

/**
 * Firebase Cloud Messaging service for ExpertConnect.
 *
 * Handles:
 * 1. Incoming push notification messages (booking events).
 * 2. FCM token refresh — sends new token to backend.
 *
 * IMPORTANT: Requires google-services.json in app/ directory.
 * Add your actual Firebase project configuration before running.
 */
class ExpertConnectFirebaseService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "ExpertConnectFCM"
        private var notificationId = 0
    }

    /**
     * Called when a new FCM token is generated for this device.
     * The new token should be sent to the backend so notifications continue working.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        // TODO: Send token to backend via AuthRepository.updateFcmToken(token)
        // This requires the user to be logged in, so queue it for the next app launch.
    }

    /**
     * Called when a push notification is received while the app is in foreground.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: message.data["title"] ?: "ExpertConnect"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val type = message.data["type"] ?: "general"

        Log.d(TAG, "Message received: type=$type, title=$title")

        showNotification(title, body, type)
    }

    private fun showNotification(title: String, body: String, type: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", type)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, ExpertConnectApp.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .build()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId++, notification)
    }
}
