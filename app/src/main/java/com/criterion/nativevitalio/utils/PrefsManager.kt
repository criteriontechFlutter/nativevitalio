
import android.content.Context
import androidx.core.content.edit
import com.criterion.nativevitalio.utils.MyApplication
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
        get() = getPatient()?.patientName

    val currentPatientUHID: String?
        get() = getPatient()?.uhID
}