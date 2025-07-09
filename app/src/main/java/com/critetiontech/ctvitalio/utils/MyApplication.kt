package com.critetiontech.ctvitalio.utils

import NetworkUtils
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class MyApplication : Application() {
    companion object {
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        FirebaseApp.initializeApp(this)

        // Create notification channel
        createNotificationChannel()

        // Get FCM token
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("FCM", "FCM Registration Token: $token")
            // Send token to your server
        }

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val themePref = prefs.getString("theme_pref", NetworkUtils.ThemeHelper.MODE_SYSTEM)
        NetworkUtils.ThemeHelper.applyTheme(themePref ?: NetworkUtils.ThemeHelper.MODE_SYSTEM)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "custom_notification_channel",
                "Custom Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Custom notification channel for app"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

}