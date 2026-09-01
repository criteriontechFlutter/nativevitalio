package com.critetiontech.ctvitalio.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatDelegate
import com.critetiontech.ctvitalio.utils.AppConstants.THEME_DARK
import com.critetiontech.ctvitalio.utils.AppConstants.THEME_LIGHT
import com.critetiontech.ctvitalio.utils.AppConstants.THEME_SYSTEM

/**
 * NetworkUtils.kt
 *
 * Utility functions for checking internet connectivity and connection quality.
 * Also includes ThemeHelper for applying day/night mode.
 *
 * Previously had no package declaration — now correctly placed in the utils package.
 */
object NetworkUtils {

    /**
     * Returns true if the device currently has an active internet connection.
     */
    fun isInternetAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Checks connectivity and returns false without showing a Toast.
     * Callers are responsible for showing UI feedback (snackbar, toast, etc.).
     */
    fun isConnected(): Boolean = isInternetAvailable(MyApplication.appContext)

    /**
     * Returns a human-readable description of the current connection quality.
     */
    fun getConnectionQuality(context: Context): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return "No Connection"
        val capabilities = cm.getNetworkCapabilities(network) ?: return "No Connection"

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Strong (Wi-Fi)"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                when {
                    capabilities.linkDownstreamBandwidthKbps >= 5000 -> "Strong (4G/5G)"
                    capabilities.linkDownstreamBandwidthKbps >= 1000 -> "Moderate (3G/4G)"
                    capabilities.linkDownstreamBandwidthKbps >= 100  -> "Weak (2G/3G)"
                    else -> "Very Weak"
                }
            }
            else -> "Unknown"
        }
    }

    /**
     * Returns the estimated downlink speed in Mbps.
     */
    fun getDownlinkSpeedMbps(context: Context): Double {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return 0.0
        val capabilities = cm.getNetworkCapabilities(network) ?: return 0.0
        return capabilities.linkDownstreamBandwidthKbps / 1000.0
    }

    // ─── Theme Helper ─────────────────────────────────────────────────────────

    object ThemeHelper {
        const val MODE_LIGHT  = THEME_LIGHT
        const val MODE_DARK   = THEME_DARK
        const val MODE_SYSTEM = THEME_SYSTEM

        fun applyTheme(themePref: String) {
            val mode = when (themePref) {
                THEME_LIGHT  -> AppCompatDelegate.MODE_NIGHT_NO
                THEME_DARK   -> AppCompatDelegate.MODE_NIGHT_YES
                THEME_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                else         -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }
}
