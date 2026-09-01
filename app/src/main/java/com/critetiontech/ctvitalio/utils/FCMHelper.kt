package com.critetiontech.ctvitalio.utils

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging

/**
 * FCMHelper.kt
 *
 * Helper for Firebase Cloud Messaging token operations.
 *
 * ⚠️ FCM token registration happens in MyApplication.onCreate() automatically.
 * Call [refreshToken] only when you explicitly need to force a token refresh
 * (e.g. after logout/login to associate with a new user session).
 *
 * Do NOT call this from Splash or any Activity onCreate — it causes double
 * token registration and unnecessary network calls.
 */
object FCMHelper {

    private const val TAG = "FCMHelper"

    /**
     * Forces a FCM token refresh and saves the new token via PrefsManager.
     * Use sparingly — the token auto-registers in MyApplication on first launch.
     *
     * @param onTokenReceived Callback with the new token string.
     * @param onError         Optional error callback.
     */
    fun refreshToken(
        onTokenReceived: (token: String) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "FCM token refresh failed", task.exception)
                    onError(task.exception ?: Exception("Unknown FCM error"))
                    return@addOnCompleteListener
                }
                val token = task.result
                PrefsManager().saveDeviceToken(token)
                Log.d(TAG, "FCM token refreshed successfully")
                onTokenReceived(token)
            }
    }
}
