package com.critetiontech.ctvitalio.utils
import android.content.Context
import androidx.core.content.edit
import com.critetiontech.ctvitalio.model.EmployeeGoal
import com.critetiontech.ctvitalio.model.Patient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * PrefsManager.kt
 *
 * Manages SharedPreferences persistence for the app.
 * All preference key constants now live in AppConstants — do not add raw
 * string keys here.
 *
 * Patient / EmployeeGoal / LeaderboardItem data classes have been moved to
 * the model/ package. Import them from there.
 */
class PrefsManager {

    private val context: Context = MyApplication.appContext
    private val sharedPref = context.getSharedPreferences(
        AppConstants.PREF_FILE_PATIENT,
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    // ─── Patient ──────────────────────────────────────────────────────────────

    fun savePatient(patient: Patient) {
        sharedPref.edit { putString(AppConstants.KEY_PATIENT, gson.toJson(patient)) }
    }

    fun getPatient(): Patient? = try {
        gson.fromJson(
            sharedPref.getString(AppConstants.KEY_PATIENT, null),
            Patient::class.java
        )
    } catch (e: Exception) {
        null
    }

    fun getEmployeeGoals(): List<EmployeeGoal> {
        val patient = getPatient() ?: return emptyList()
        return try {
            gson.fromJson(
                patient.employeegoalsDetails,
                object : TypeToken<List<EmployeeGoal>>() {}.type
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ─── Allergies ────────────────────────────────────────────────────────────

    fun saveAllergies(value: String) {
        sharedPref.edit { putString(AppConstants.KEY_ALLERGIES, value) }
    }

    fun getAllergies(): String? = try {
        sharedPref.getString(AppConstants.KEY_ALLERGIES, "")
    } catch (e: Exception) {
        ""
    }

    // ─── Emergency contact count ──────────────────────────────────────────────

    fun saveEmergency(value: String) {
        sharedPref.edit { putString(AppConstants.KEY_EMERGENCY, value) }
    }

    fun getEmergency(): String? = try {
        sharedPref.getString(AppConstants.KEY_EMERGENCY, "")
    } catch (e: Exception) {
        ""
    }

    // ─── Smart Watch ──────────────────────────────────────────────────────────

    fun saveSmartWatch(value: String) {
        sharedPref.edit { putString(AppConstants.KEY_SMART_WATCH, value) }
    }

    fun getSmartWatch(): String? = try {
        sharedPref.getString(AppConstants.KEY_SMART_WATCH, "")
    } catch (e: Exception) {
        ""
    }

    // ─── FCM Device Token ─────────────────────────────────────────────────────

    fun saveDeviceToken(token: String) {
        sharedPref.edit { putString(AppConstants.KEY_DEVICE_TOKEN, token) }
    }

    fun getDeviceToken(): String? =
        sharedPref.getString(AppConstants.KEY_DEVICE_TOKEN, null)

    // ─── Clear all session data ───────────────────────────────────────────────

    /** Clears all persisted patient/session data in a single transaction. */
    fun clearPatient() {
        sharedPref.edit {
            remove(AppConstants.KEY_PATIENT)
            remove(AppConstants.KEY_ALLERGIES)
            remove(AppConstants.KEY_EMERGENCY)
            remove(AppConstants.KEY_SMART_WATCH)
            remove(AppConstants.KEY_DEVICE_TOKEN)
        }
    }

    // ─── Convenience properties ───────────────────────────────────────────────

    val currentPatientName: String?
        get() = getPatient()?.patientName

    val currentPatientUHID: String?
        get() = getPatient()?.uhID
}