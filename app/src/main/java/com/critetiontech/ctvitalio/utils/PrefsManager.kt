
import android.content.Context
import android.icu.text.DisplayOptions.DisplayLength
import android.util.Log
import androidx.core.content.edit
import com.critetiontech.ctvitalio.utils.MyApplication
import com.google.gson.Gson

// Data class for patient (include all fields from your JSON)
data class Patient(
    val pid: String,
    val patientName: String,
    val registrationDate: String,
    val address: String,
    val age: String,
    val ageUnitId: String,
    val bloodGroupId: String,
    val categoryId: String?,
    val cityId: String,
    val countryCallingCode: String,
    val countryId: String,
    val createdDate: String,
    val dob: String,
    val educationalQualificationId: String?,
    val emailID: String,
    val ethinicityId: String?,
    val gender: String,
    val genderId: String,
    val guardianAddress: String,
    val guardianMobileNo: String,
    val guardianName: String,
    val guardianRelationId: String?,
    val height: String,
    val id: String,
    val idNumber: String,
    val idTypeId: String,
    val imageURL: String,
    val languageId: String,
    val maritalStatusId: String,
    val mobileNo: String,
    val occupationId: String?,
    val raceTypeId: String?,
    val refferedFrom: String?,
    val sexualOrientation: String,
    val stateId: String,
    val status: String,
    val uhID: String,
    val userId: String,
    val weight: String,
    val zip: String,
    val departmentId: String,
    val doctorID: String,
    val patientGender: String,
    val departmentName: String,
    val clientId: Int,
    val isCashLess: Boolean,
    val insuranceCompanyId: Int,
    val policyOrCardNumber: String,
    val profileUrl: String
)


class PrefsManager( ) {
    val context = MyApplication.appContext
    private val sharedPref = context.getSharedPreferences("patient_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_PATIENT = "current_patient"
        private const val KEY_Allergies = "allergies_length"
        private const val KEY_Emergency = "Emergency_length"
        private const val KEY_SmartWatch = "SmartWatch_length"
        private const val KEY_DEVICE_TOKEN = "deviceToken"
    }

    // Save patient object
    fun savePatient(patient: Patient) {
        sharedPref.edit {
            putString(KEY_PATIENT, gson.toJson(patient))
        }
    }

    fun saveAllergies(allergiesLength: String) {
        sharedPref.edit {
            putString(KEY_Allergies,allergiesLength)
        }
    }

    fun saveEmergency(emergencyLength: String) {
        sharedPref.edit {
            putString(KEY_Emergency,emergencyLength)
        }
    }
    fun saveSmartWatch(smartWatchLength: String) {
        sharedPref.edit {
            putString(KEY_SmartWatch,smartWatchLength)
        }
    }


    fun saveDeviceToken(deviceToken: String) {
        Log.d("TAG", "saveDeviceToken: "+deviceToken.toString())
        sharedPref.edit {
            putString(KEY_DEVICE_TOKEN,deviceToken)
        }
    }

    fun getDeviceToken(): String? {
        Log.d("TAG", "saveDeviceToken: "+sharedPref.getString(KEY_DEVICE_TOKEN, null))
        return sharedPref.getString(KEY_DEVICE_TOKEN, null)

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
    fun getAllergies(): String? {
        return try {

            return sharedPref.getString(KEY_Allergies, "")
        } catch (e: Exception) {
            ""
        }
    }

    fun getEmergency(): String? {
        return try {

            return sharedPref.getString(KEY_Emergency, "")
        } catch (e: Exception) {
            ""
        }
    }
    fun getSmartWatch(): String? {
        return try {

            return sharedPref.getString(KEY_SmartWatch, "")
        } catch (e: Exception) {
            ""
        }
    }

    // Clear patient data
    fun clearPatient() {
        sharedPref.edit { remove(KEY_PATIENT) }
        sharedPref.edit { remove(KEY_Allergies) }
        sharedPref.edit { remove(KEY_Emergency) }
        sharedPref.edit { remove(KEY_SmartWatch) }
        sharedPref.edit { remove(KEY_DEVICE_TOKEN) }
    }

    // Optional: Direct property access
    val currentPatientName: String?
        get() = getPatient()?.patientName

    val currentPatientUHID: String?
        get() = getPatient()?.uhID
}