package com.critetiontech.ctvitalio.firebase

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.critetiontech.ctvitalio.MainActivity
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.utils.MyApplication
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "custom_notification_channel"
        private const val CHANNEL_NAME = "Custom Notifications"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "Message received in: ${if (isAppInForeground()) "FOREGROUND" else "BACKGROUND"}")
        Log.d("FCM", "Data: ${remoteMessage.data}")
        Log.d("FCM", "Notification: ${remoteMessage.notification}")

        // ALWAYS show custom notification regardless of app state
        showCustomNotification(remoteMessage)
    }

    @SuppressLint("RemoteViewLayout")
    private fun showCustomNotification(remoteMessage: RemoteMessage) {
        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("from_notification", true)
            // Add all FCM data
            remoteMessage.data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Get data - prioritize data payload over notification payload
        val title = remoteMessage.notification?.title
        val body =  remoteMessage.notification?.body
        val timestamp = "2 Min Ago"

        // Create custom view
        val customView = RemoteViews(packageName, R.layout.custom_notification_layout)
        customView.setTextViewText(R.id.title, title)
        customView.setTextViewText(R.id.body, body)
        customView.setTextViewText(R.id.timestamp, timestamp)

        // Set icon
        customView.setImageViewResource(R.id.notification_icon, R.drawable.logo)

        // Set click listener
       // customView.setOnClickPendingIntent(R.id.notification_container, pendingIntent)

        // Build notification
        val notification = NotificationCompat.Builder(MyApplication.appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .setCustomContentView(customView)
            .setCustomBigContentView(customView)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOnlyAlertOnce(false)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        Log.d("FCM", "Custom notification displayed")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Custom notification channel"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false

        return appProcesses.any {
            it.processName == packageName &&
                    it.importance == android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
        }
    }

    override fun onNewToken(token: String) {
        Log.d("FCM_TOKEN", "Refreshed token: $token")
        // Send token to your backend server
    }
}