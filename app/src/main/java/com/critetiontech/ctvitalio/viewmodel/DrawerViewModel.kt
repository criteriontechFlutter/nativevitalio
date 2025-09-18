package com.critetiontech.ctvitalio.viewmodel

import Patient
import PrefsManager
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.UI.Login
import com.critetiontech.ctvitalio.model.BaseResponse
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.critetiontech.ctvitalio.utils.ToastUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class DrawerViewModel : ViewModel() {



    val _loading = MutableLiveData<Boolean>()
    val _updateSuccess = MutableLiveData<Boolean>()
    val _errorMessage = MutableLiveData<String>()
   
//    fun updateUserData(
//        requireContext: Context,
//        filePath: String? = null,
//
//    ) {
//        _loading.value = true
//        viewModelScope.launch {
//            try {
//                val patient = PrefsManager().getPatient() ?: return@launch
//                val parts = mutableListOf<MultipartBody.Part>()
//                fun partFromField(key: String, value: String): MultipartBody.Part {
//                    Log.d("UpdateProfile", "Field: $key = $value")
//                    return MultipartBody.Part.createFormData(key, value)
//                }
//
//                parts += partFromField("Pid", patient.pid)
//                parts += partFromField("PatientName", patient.patientName)
//                parts += partFromField("EmailID", patient.emailID)
//                parts += partFromField("GenderId", patient.genderId)
//                parts += partFromField("BloodGroupId", patient.bloodGroupId)
//                parts += partFromField("Height",patient.height)
//                parts += partFromField("Weight", patient.weight)
//                parts += partFromField("Dob", patient.dob)
//                parts += partFromField("Zip",  patient.zip)
//                parts += partFromField("AgeUnitId", patient.ageUnitId)
//                parts += partFromField("Age", patient.age)
//                parts += partFromField("Address", patient.address)
//                parts += partFromField("MobileNo", patient.mobileNo)
//                parts += partFromField("CountryId", patient.countryId)
//                parts += partFromField("StateId", patient.stateId)
//                parts += partFromField("CityId", patient.cityId)
//                parts += partFromField("UserId", patient.userId)
//                parts += partFromField("ChoronicDiseasesJson", "")
//                parts += partFromField("FamilyDiseaseJson", "")
//                // Add all text fields
//
////                parts += partFromField("ProfileURL", patient.profileUrl.replace("https://api.medvantage.tech:7082/", ""))
//
//                // Add file if present
//                filePath?.takeIf { it.isNotEmpty() }?.let {
//                    val file = File(it)
//                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
//                    val filePart = MultipartBody.Part.createFormData("FormFile", file.name, requestFile)
//                    parts += filePart
//                    Log.d("UpdateProfile", "File attached: ${file.name}")
//                }
//
//                // Print final parts for debug
//                parts.forEach { part ->
//                    val headers = part.headers?.toString() ?: "No Headers"
//                    val bodyString = try {
//                        val buffer = okio.Buffer()
//                        part.body.writeTo(buffer)
//                        buffer.readUtf8()
//                    } catch (e: Exception) {
//                        "Binary or file content"
//                    }
//                    val dispositionHeader = part.headers?.get("Content-Disposition")
//                    val nameRegex = Regex("name=\"(.*?)\"")
//                    val fieldName = nameRegex.find(dispositionHeader ?: "")?.groupValues?.getOrNull(1) ?: "unknown"
//
//                    Log.d("UpdateProfile", "Field: $fieldName = $bodyString")
//                }
//                // API Call
//                val response = RetrofitInstance
//                    .createApiService(
//                        includeAuthHeader=true)
//                    .dynamicMultipartPut(
//                        url = ApiEndPoint().updatePatient,
//                        headers = generateAuthHeaderMap(true),
//                        parts = parts
//                    )
//
//                if (response.isSuccessful) {
//                    ToastUtils.showSuccessPopup(requireContext,"Profile updated successfully!")
//
//                    _updateSuccess.postValue(true)
//                    getPatientDetailsByUHID()
//                } else {
//                    Log.e("UpdateProfile", "Update failed. Code: ${response.code()}")
//                    _errorMessage.postValue("Error: ${response.code()}")
//                }
//
//            } catch (e: Exception) {
//                Log.e("UpdateProfile", "Exception: ${e.message}", e)
//                _errorMessage.postValue(e.message ?: "Unknown error")
//            } finally {
//                _loading.postValue(false)
//            }
//        }
//    }
private val _selectedImageUri = MutableLiveData<Uri?>()
    val selectedImageUri: LiveData<Uri?> get() = _selectedImageUri

    // Function to update value
    fun setSelectedImage(uri: Uri?) {
        _selectedImageUri.value = uri
    }
    fun updateUserData(
        requireContext: Context,
        filePath: Uri? = null,
//        chronicData:String,
//        familyDiseaseJson:String,
//        street:String,
//        zipCode: String,
//        countryId: String,
//        stateId: String,
//        cityId: String,
//        weight:String,
//        height:String,
//        bgId:String,
//        EmployeeGoalsJson:String,
    ) {
        _loading.value = true
        viewModelScope.launch {
            _updateSuccess.postValue(false)
            try {
                val patient = PrefsManager().getPatient() ?: return@launch
                val parts = mutableListOf<MultipartBody.Part>()
                fun partFromField(key: String, value: String): MultipartBody.Part {
                    Log.d("UpdateProfile", "Field: $key = $value")
                    return MultipartBody.Part.createFormData(key, value)
                }

                parts += partFromField("Pid", patient.id.toString())
                parts += partFromField("PatientName", patient.patientName)
                parts += partFromField("EmailID", patient.emailID)
                parts += partFromField("GenderId",  patient.genderId.toString())
                parts += partFromField("BloodGroupId", patient.bloodGroupId.toString())
                parts += partFromField("Height",patient.height.toString())
                parts += partFromField("Weight", patient.weight.toString())
                parts += partFromField("Dob",  patient.dob.toString())
                parts += partFromField("Zip",  patient.zip.toString())
                parts += partFromField("AgeUnitId", "1")
                parts += partFromField("Age",  patient.age.toString())
                parts += partFromField("Address",  patient.address.toString())
                parts += partFromField("MobileNo",   patient.mobileNo.toString())
                parts += partFromField("CountryId",   patient.countryId.toString())
                parts += partFromField("StateId",   patient.stateId.toString())
                parts += partFromField("CityId",   patient.cityId.toString())
                parts += partFromField("UserId", "99")


//                parts += partFromField("ChoronicDiseasesJson", chronicData)
//                parts += partFromField("FamilyDiseaseJson", familyDiseaseJson)
//                parts += partFromField("EmployeeGoalsJson",EmployeeGoalsJson  )
                // Add all text fields

//                parts += partFromField("ProfileURL", patient.profileUrl.replace("https://api.medvantage.tech:7082/", ""))

                // Add file if present
                if (filePath != null) {
                    filePath.path?.takeIf { it.isEmpty() }?.let {
                        val file = File(it)
                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())


                        val filePart =

                            MultipartBody.Part.createFormData("FormFile",
                                PrefsManager().getPatient()?.profileUrl, requestFile)


                        parts += filePart
                        Log.d("UpdateProfile", "File attached: ${file.name}")
                    }
                }

                // Print final parts for debug
                parts.forEach { part ->
                    val headers = part.headers?.toString() ?: "No Headers"
                    val bodyString = try {
                        val buffer = okio.Buffer()
                        part.body.writeTo(buffer)
                        buffer.readUtf8()
                    } catch (e: Exception) {
                        "Binary or file content"
                    }
                    val dispositionHeader = part.headers?.get("Content-Disposition")
                    val nameRegex = Regex("name=\"(.*?)\"")
                    val fieldName = nameRegex.find(dispositionHeader ?: "")?.groupValues?.getOrNull(1) ?: "unknown"

                    Log.d("UpdateProfile", "Field: $fieldName = $bodyString")
                }
                // API Call
                val response = RetrofitInstance
                    .createApiService(
                        includeAuthHeader=true)
                    .dynamicMultipartPut(
                        url = ApiEndPoint().updatePatient,
                        parts = parts
                    )

                if (response.isSuccessful) {
                    _updateSuccess.postValue(true)
                    ToastUtils.showSuccessPopup(requireContext,"Profile updated successfully!")
                    getPatientDetailsByUHID()

                } else {
                    _updateSuccess.postValue(false)
                    Log.e("UpdateProfile", "Update failed. Code: ${response.code()}")
                    _errorMessage.postValue("Error: ${response.code()}")
                }

            } catch (e: Exception) {
                Log.e("UpdateProfile", "Exception: ${e.message}", e)
                _errorMessage.postValue(e.message ?: "Unknown error")
            } finally {
                _loading.postValue(false)
            }
        }
    }
    private fun getPatientDetailsByUHID( ) {
        _loading.value = true

        viewModelScope.launch {
            try {

                val queryParams = mapOf(
                    "mobileNo" to "",
                    "uhid" to PrefsManager().getPatient()?.empId.toString(),
                    "ClientId" to 194
                )

                // This response is of type Response<ResponseBody>
                val response = RetrofitInstance
                    .createApiService( )
                    .dynamicGet(
                        url = ApiEndPoint().getPatientDetailsByMobileNo,
                        params = queryParams
                    )

                if (response.isSuccessful) {

                    _loading.value = false

                    val responseBodyString = response.body()?.string()

                    val type = object : TypeToken<BaseResponse<List<Patient>>>() {}.type
                    val parsed = Gson().fromJson<BaseResponse<List<Patient>>>(responseBodyString, type)
                    Log.d("RESPONSE", "responseValue: ${Gson().toJson(parsed.responseValue)}")
                    val firstPatient = parsed.responseValue.firstOrNull()


                    firstPatient?.let {
                        PrefsManager( ).savePatient(it)
                        Login.storedUHID=it
//                        val intent = Intent(MyApplication.appContext, Home::class.java)
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                        MyApplication.appContext.startActivity(intent)
                        Log.d("RESPONSE", "Full Patients: ${PrefsManager().getPatient()?.uhID.toString()}")
                    }


                } else {

                    _loading.value = false

                    _errorMessage.value = "Error: ${response.code()}"
                }

            } catch (e: Exception) {
                _loading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
                e.printStackTrace()
            }
        }
    }
}