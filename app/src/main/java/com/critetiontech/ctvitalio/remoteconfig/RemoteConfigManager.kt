package com.critetiontech.ctvitalio.remoteconfig

import android.util.Log
import com.critetiontech.ctvitalio.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.tasks.await

/**
 * RemoteConfigManager.kt
 * ─────────────────────────────────────────────────────────────────────────────
 * Single source of truth for all Firebase Remote Config interactions.
 *
 * Features:
 *  - Maintenance mode  → show blocking screen with custom title/message
 *  - Environment switch → dynamically swap base URL between
 *                         staging / development / production
 *  - Force update      → redirect user to Play Store when minimum version not met
 *
 * Usage:
 *   // In Application.onCreate():
 *   RemoteConfigManager.initialize()
 *
 *   // Anywhere in the app:
 *   val host = RemoteConfigManager.activeBaseHost
 *   val inMaintenance = RemoteConfigManager.isMaintenanceMode
 * ─────────────────────────────────────────────────────────────────────────────
 */
object RemoteConfigManager {

    private const val TAG = "RemoteConfig"

    // ── Remote Config key constants ───────────────────────────────────────────

    const val KEY_ACTIVE_ENV           = "active_environment"
    const val KEY_HOST_STAGING         = "base_host_staging"
    const val KEY_HOST_DEVELOPMENT     = "base_host_development"
    const val KEY_HOST_PRODUCTION      = "base_host_production"

    const val KEY_MAINTENANCE_MODE     = "is_maintenance_mode"
    const val KEY_MAINTENANCE_TITLE    = "maintenance_title"
    const val KEY_MAINTENANCE_MESSAGE  = "maintenance_message"
    const val KEY_MAINTENANCE_EMAIL    = "maintenance_contact_email"

    const val KEY_FORCE_UPDATE         = "force_update_required"
    const val KEY_MIN_VERSION          = "minimum_app_version"
    const val KEY_UPDATE_MESSAGE       = "update_message"

    // ── Environment constants ─────────────────────────────────────────────────

    const val ENV_STAGING     = "staging"
    const val ENV_DEVELOPMENT = "development"
    const val ENV_PRODUCTION  = "production"

    // ── Firebase instance ─────────────────────────────────────────────────────

    val remoteConfig: FirebaseRemoteConfig by lazy { Firebase.remoteConfig }

    // ─────────────────────────────────────────────────────────────────────────
    // Initialization
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Must be called once from [MyApplication.onCreate].
     * Sets up fetch interval and local defaults, then triggers a background fetch.
     */
    fun initialize() {
        val settings = remoteConfigSettings {
            // In debug builds, fetch every 30 s so developers see changes quickly.
            // In release builds Firebase enforces a minimum of 1 hour between fetches.
            minimumFetchIntervalInSeconds = if (isDebug()) 30L else 3600L
        }

        remoteConfig.apply {
            setConfigSettingsAsync(settings)
            setDefaultsAsync(R.xml.remote_config_defaults)
        }

        Log.d(TAG, "RemoteConfigManager initialised – env=${activeEnvironment}, maintenance=$isMaintenanceMode")
    }

    /**
     * Fetches latest values from Firebase and activates them.
     * Call from your Splash screen or first Activity to get fresh config.
     *
     * @return true if fetch + activate succeeded; false on error (local defaults remain active)
     */
    suspend fun fetchAndActivate(): Boolean {
        return try {
            val activated = remoteConfig.fetchAndActivate().await()
            Log.d(TAG, "fetchAndActivate → activated=$activated, env=${activeEnvironment}")
            activated
        } catch (e: Exception) {
            Log.e(TAG, "fetchAndActivate failed, using cached/default values", e)
            false
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Convenience accessors
    // ─────────────────────────────────────────────────────────────────────────

    /** Currently active environment name ("staging" | "development" | "production"). */
    val activeEnvironment: String
        get() = remoteConfig.getString(KEY_ACTIVE_ENV).ifBlank { ENV_STAGING }

    /**
     * The base host for the active environment.
     * This is used by [NetworkConfig] to build all port-specific URLs dynamically.
     *
     * Example return value: "https://demo.vitalio.care"
     */
    val activeBaseHost: String
        get() = when (activeEnvironment) {
            ENV_DEVELOPMENT -> remoteConfig.getString(KEY_HOST_DEVELOPMENT)
                .ifBlank { "http://182.156.200.177" }
            ENV_PRODUCTION  -> remoteConfig.getString(KEY_HOST_PRODUCTION)
                .ifBlank { "https://api.vitalio.care" }
            else            -> remoteConfig.getString(KEY_HOST_STAGING)
                .ifBlank { "https://demo.vitalio.care" }
        }

    /** Convenience: base URL on port 5082 for the active environment. */
    val baseUrl5082: String get() = "$activeBaseHost:5082/"
    val baseUrl5083: String get() = "$activeBaseHost:5083/"
    val baseUrl5090: String get() = "$activeBaseHost:5090/"
    val baseUrl5096: String get() = "$activeBaseHost:5096/"
    val baseUrl5100: String get() = "$activeBaseHost:5100/"
    val baseUrl5119: String get() = "$activeBaseHost:5119/"

    // ── Maintenance ───────────────────────────────────────────────────────────

    /** Whether the app should show the maintenance screen. */
    val isMaintenanceMode: Boolean
        get() = remoteConfig.getBoolean(KEY_MAINTENANCE_MODE)

    val maintenanceTitle: String
        get() = remoteConfig.getString(KEY_MAINTENANCE_TITLE)
            .ifBlank { "Under Maintenance" }

    val maintenanceMessage: String
        get() = remoteConfig.getString(KEY_MAINTENANCE_MESSAGE)
            .ifBlank { "We'll be back shortly." }

    val maintenanceContactEmail: String
        get() = remoteConfig.getString(KEY_MAINTENANCE_EMAIL)
            .ifBlank { "support@vitalio.care" }

    // ── Force update ──────────────────────────────────────────────────────────

    val isForceUpdateRequired: Boolean
        get() = remoteConfig.getBoolean(KEY_FORCE_UPDATE)

    val minimumAppVersion: Long
        get() = remoteConfig.getLong(KEY_MIN_VERSION)

    val updateMessage: String
        get() = remoteConfig.getString(KEY_UPDATE_MESSAGE)
            .ifBlank { "Please update the app to continue." }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────────────

    private fun isDebug(): Boolean =
        com.critetiontech.ctvitalio.BuildConfig.DEBUG
}
