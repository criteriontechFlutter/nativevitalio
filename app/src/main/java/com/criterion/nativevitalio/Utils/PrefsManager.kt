import android.content.Context
import androidx.core.content.edit
import com.criterion.nativevitalio.Utils.MyApplication
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

// Data class for patient (include all fields from your JSON)
data class Patient(
    @SerializedName("id") val id: String,
    @SerializedName("token") val token: String,
    @SerializedName("pid") val pid: Int,
    @SerializedName("patientName") val name: String,
    @SerializedName("registrationDate") val registrationDate: String,
    val address: String,
    val age: Int,
    @SerializedName("ageUnitId") val ageUnit: Int,
    @SerializedName("bloodGroupId") val bloodGroup: Int,
    @SerializedName("cityId") val cityId: Int,
    @SerializedName("countryId") val countryId: Int,
    @SerializedName("dob") val dateOfBirth: String,
    @SerializedName("emailID") val email: String,
    @SerializedName("mobileNo") val phone: String,
    @SerializedName("uhID") val uhid: String,
    @SerializedName("departmentName") val department: String,
    @SerializedName("isCashLess") val cashless: Boolean,

    // Nullable fields
    @SerializedName("categoryId") val category: Int? = null,
    @SerializedName("guardianName") val guardian: String? = null,
    @SerializedName("height") val height: Double? = null,
    @SerializedName("weight") val weight: Double? = null,
    // Add all other nullable fields...
)

class PrefsManager( ) {
    val context = MyApplication.appContext
    private val sharedPref = context.getSharedPreferences("patient_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_PATIENT = "current_patient"
    }

    // Save patient object
    fun savePatient(patient: Patient) {
        sharedPref.edit {
            putString(KEY_PATIENT, gson.toJson(patient))
        }
    }

    // Retrieve patient with null safety
    fun getPatient(): Patient? {
        return try {
            gson.fromJson(
                sharedPref.getString(KEY_PATIENT, null),
                Patient::class.java
            )
        } catch (e: Exception) {
            null
        }
    }

    // Clear patient data
    fun clearPatient() {
        sharedPref.edit { remove(KEY_PATIENT) }
    }

    // Optional: Direct property access
    val currentPatientName: String?
        get() = getPatient()?.name

    val currentPatientUHID: String?
        get() = getPatient()?.uhid
}