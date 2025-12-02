package com.critetiontech.ctvitalio.viewmodel

import PrefsManager
import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.critetiontech.ctvitalio.utils.ToastUtils
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class SetGoalViewModel (application: Application) : BaseViewModel(application) {

    var selectedDays: MutableSet<Int> = mutableSetOf()
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage
    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> = _updateSuccess
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading
    fun updateUserData(
        requireContext: Context,
        filePath: Uri? = null,
        categoryId:String,
        goalId: String,
        targetValue:String,
        unit:String,
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
                val employeeGoalsJson = """
    [
      {
        "pid": ${patient.id},
        "categoryId": $categoryId,
        "goalId": $goalId,
        "targetValue": $targetValue,
        "unit": "steps"
      }
    ]
""".trimIndent()
                val goalWeeksJson = selectedDays.joinToString(
                    prefix = "[",
                    postfix = "]"
                ) { """{"dayId":${it + 1}}""" }
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
                parts += partFromField("EmployeeGoalsJson", employeeGoalsJson)
                parts += partFromField("GoalWeeksJson", goalWeeksJson)
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
}