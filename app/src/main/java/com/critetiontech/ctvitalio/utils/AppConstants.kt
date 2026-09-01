package com.critetiontech.ctvitalio.utils

/**
 * AppConstants.kt
 *
 * Single source of truth for all application-level string constants, keys,
 * channel IDs, preference keys, and other magic values used throughout the app.
 *
 * DO NOT scatter string literals across activities/fragments/viewmodels.
 * Add them here instead.
 */
object AppConstants {

    // ─── Notification Channels ────────────────────────────────────────────────
    const val NOTIFICATION_CHANNEL_ID        = "vitalio_notification_channel"
    const val NOTIFICATION_CHANNEL_NAME      = "Vitalio Notifications"
    const val NOTIFICATION_CHANNEL_HEALTH_ID = "custom_notification_channel"

    // ─── SharedPreferences ────────────────────────────────────────────────────
    const val PREF_FILE_PATIENT   = "patient_prefs"
    const val PREF_THEME          = "theme_pref"

    // Preference keys
    const val KEY_PATIENT         = "current_patient"
    const val KEY_ALLERGIES       = "allergies_length"
    const val KEY_EMERGENCY       = "Emergency_length"
    const val KEY_SMART_WATCH     = "SmartWatch_length"
    const val KEY_DEVICE_TOKEN    = "deviceToken"

    // ─── Bundle / Intent Extra Keys ───────────────────────────────────────────
    const val EXTRA_SELECTED_DEVICE     = "selectedDevice"
    const val EXTRA_DEVICE_INFO_LISTS   = "deviceInfoLists"
    const val EXTRA_DEVICE_LOCAL_NAME   = "localName"
    const val EXTRA_MODEL_IDENTIFIER    = "modelName"
    const val EXTRA_SELECTED_USER       = "selectedUser"
    const val EXTRA_IS_BLE_PROTOCOL     = "selectProtocol"

    const val BUNDLE_HEIGHT_CM          = "height_cm"
    const val BUNDLE_STRIDE_CM          = "stride_cm"
    const val BUNDLE_WEIGHT_KG          = "weight_kg"
    const val BUNDLE_ACTIVITY_DATA_KEY  = "activity_data_key"
    const val BUNDLE_ACTIVITY_DATA_TYPE = "activity_data_type"
    const val BUNDLE_ACTIVITY_DATA_SEQ  = "activity_data_seq"
    const val BUNDLE_ACTIVITY_DATA_DATE = "activity_data_date"
    const val BUNDLE_DOB                = "dob"
    const val BUNDLE_GENDER             = "gender"
    const val BUNDLE_WEIGHT_UNIT        = "weight_unit"

    // ─── Omron Device Info Keys ───────────────────────────────────────────────
    const val DEVICE_KEY_LOCAL_NAME   = "LocalNameKey"
    const val DEVICE_KEY_UUID         = "uuidKey"
    const val DEVICE_KEY_SELECTED_USER= "selectedUserKey"
    const val DEVICE_KEY_SEQUENCE_NO  = "sequenceNoKey"

    // ─── Theme Modes ─────────────────────────────────────────────────────────
    const val THEME_LIGHT  = "light"
    const val THEME_DARK   = "dark"
    const val THEME_SYSTEM = "system"

    // ─── Network / Timeout ───────────────────────────────────────────────────
    const val CONNECTION_TIMEOUT_SECONDS = 60L

    // ─── WaterIntake Widget ───────────────────────────────────────────────────
    const val WATER_DAILY_GOAL_ML  = 2000
    const val WATER_INCREMENT_ML   = 200

    // ─── Bingo ────────────────────────────────────────────────────────────────
    const val BINGO_CENTER_TILE_ID = 5

    // ─── Error messages ───────────────────────────────────────────────────────
    const val ERROR_UNKNOWN              = "Something went wrong"
    const val ERROR_NO_INTERNET          = "No internet connection"
    const val ERROR_UNABLE_TO_PARSE      = "Unable to parse error"

    // ─── Image picker request codes ───────────────────────────────────────────
    const val REQUEST_CODE_PICK_IMAGE = 1001
    const val REQUEST_CODE_CAMERA     = 1002
}
