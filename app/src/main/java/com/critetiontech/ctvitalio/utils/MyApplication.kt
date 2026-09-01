package com.critetiontech.ctvitalio.utils

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.critetiontech.ctvitalio.logging.LoggingManager
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

/**
 * MyApplication.kt
 *
 * Application entry point. Responsibilities:
 *  - Initialise Firebase (once)
 *  - Register FCM token
 *  - Apply saved theme preference
 *  - Create notification channels
 *  - Observe app foreground/background lifecycle for analytics
 *
 * Previously initialised Firebase and FCM token in BOTH MyApplication AND Splash.kt
 * causing double-registration. FCM token is now registered here only.
 */
class MyApplication : Application() {

    companion object {
        lateinit var appContext: android.content.Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        initFirebase()
        registerFcmToken()
        applyThemePreference()
        createNotificationChannels()
        observeAppLifecycle()
    }

    // ─── Firebase ─────────────────────────────────────────────────────────────

    private fun initFirebase() {
        FirebaseApp.initializeApp(this)
    }

    private fun registerFcmToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }
                val token = task.result
                PrefsManager().saveDeviceToken(token)
                Log.d("FCM", "FCM Registration Token acquired")
            }
    }

    // ─── Theme ────────────────────────────────────────────────────────────────

    private fun applyThemePreference() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val themePref = prefs.getString(
            AppConstants.PREF_THEME,
            AppConstants.THEME_LIGHT
        ) ?: AppConstants.THEME_LIGHT
        NetworkUtils.ThemeHelper.applyTheme(themePref)
    }

    // ─── Notification channels ────────────────────────────────────────────────

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Primary Vitalio channel
            val primaryChannel = NotificationChannel(
                AppConstants.NOTIFICATION_CHANNEL_ID,
                AppConstants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Vitalio health and wellness notifications"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager?.createNotificationChannel(primaryChannel)
        }
    }

    // ─── App lifecycle analytics ──────────────────────────────────────────────

    private fun observeAppLifecycle() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                val osInfo = "Android ${Build.VERSION.RELEASE}"
                val deviceModel = Build.MODEL
                when (event) {
                    Lifecycle.Event.ON_START -> LoggingManager.logEvent(
                        event = "app_open",
                        screen = "app",
                        extras = mapOf("os" to osInfo, "deviceModel" to deviceModel)
                    )
                    Lifecycle.Event.ON_STOP -> {
                        LoggingManager.logEvent(
                            event = "app_close",
                            screen = "app",
                            extras = mapOf("os" to osInfo, "deviceModel" to deviceModel)
                        )
                        LoggingManager.flushLogs(this)
                    }
                    else -> {}
                }
            }
        )
    }
}