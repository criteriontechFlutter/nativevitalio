package com.critetiontech.ctvitalio.viewmodel

import Patient
import PrefsManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.UI.Login
import com.critetiontech.ctvitalio.UI.Home
import com.critetiontech.ctvitalio.model.BaseResponse
import com.critetiontech.ctvitalio.model.BloodGroup
import com.critetiontech.ctvitalio.model.CityModel
import com.critetiontech.ctvitalio.model.Problem
import com.critetiontech.ctvitalio.model.StateModel
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.utils.ToastUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditProfileViewModel :ViewModel() {


    val _loading = MutableLiveData<Boolean>()
    val _updateSuccess = MutableLiveData<Boolean>()
    val _errorMessage = MutableLiveData<String>()


    fun updateUserData(
        requireContext: Context,
        filePath: String? = null,
        name: String,
        phone: String,
        email: String,
        dob: String,
        genderId: String,

        chronicData:String,
        street:String,
        zipCode: String,
        countryId: String,
        stateId: String,
        cityId: String,
        weight:String,
        height:String,
        bgId:String,

    ) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val patient = PrefsManager().getPatient() ?: return@launch
                val parts = mutableListOf<MultipartBody.Part>()
                fun partFromField(key: String, value: String): MultipartBody.Part {
                    Log.d("UpdateProfile", "Field: $key = $value")
                    return MultipartBody.Part.createFormData(key, value)
                }

                parts += partFromField("Pid", patient.pid)
                parts += partFromField("PatientName", name)
                parts += partFromField("EmailID",email)
                parts += partFromField("GenderId", genderId)
                parts += partFromField("BloodGroupId", bgId)
                parts += partFromField("Height","%.2f".format(height.toDoubleOrNull() ?: 0.0))
                parts += partFromField("Weight", "%.2f".format(weight.toDoubleOrNull() ?: 0.0))
                parts += partFromField("Dob", dob)
                parts += partFromField("Zip",   zipCode)
                parts += partFromField("AgeUnitId", patient.ageUnitId)
                parts += partFromField("Age", patient.age)
                parts += partFromField("Address", street)
                parts += partFromField("MobileNo", phone)
                parts += partFromField("CountryId",  countryId.split(".")[0])
                parts += partFromField("StateId",  stateId)
                parts += partFromField("CityId",  cityId)
                parts += partFromField("UserId", patient.userId)
                parts += partFromField("ChoronicDiseasesJson", chronicData)
                parts += partFromField("FamilyDiseaseJson", "")
                // Add all text fields

//                parts += partFromField("ProfileURL", patient.profileUrl.replace("https://api.medvantage.tech:7082/", ""))

                // Add file if present
                filePath?.takeIf { it.isEmpty() }?.let {
                    val file = File(it)
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val filePart = MultipartBody.Part.createFormData("FormFile",
                        PrefsManager().getPatient()?.profileUrl, requestFile)
                    parts += filePart
                    Log.d("UpdateProfile", "File attached: ${file.name}")
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
                    ToastUtils.showSuccessPopup(requireContext,"Profile updated successfully!")

                    _updateSuccess.postValue(true)
                    getPatientDetailsByUHID()
                } else {
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
                        val intent = Intent(MyApplication.appContext, Home::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        MyApplication.appContext.startActivity(intent)
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




    private val _problemList = MutableLiveData<List<Problem>>()
    val problemList: LiveData<List<Problem>> = _problemList




    private val _selectedDiseaseList = MutableLiveData<MutableList<Map<String, String>>>(mutableListOf())
    val selectedDiseaseList: LiveData<MutableList<Map<String, String>>> get() = _selectedDiseaseList

    fun addSelectedDisease(problem: Problem, context: Context) {
        val currentList = _selectedDiseaseList.value ?: mutableListOf()
        val detailID = problem.id.toString()

        if (currentList.any { it["detailID"] == detailID }) {
            Toast.makeText(context, "Already added", Toast.LENGTH_SHORT).show()
            return
        }

        val newEntry = mapOf(
            "detailID" to detailID,
            "detailsDate" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(
                Date()
            ),
            "details" to problem.problemName,
            "isFromPatient" to "1"
        )

        currentList.add(newEntry)
        _selectedDiseaseList.value = currentList
    }

    fun removeSelectedDisease(detailID: String) {
        val currentList = _selectedDiseaseList.value ?: mutableListOf()
        currentList.removeAll { it["detailID"] == detailID }
        _selectedDiseaseList.value = currentList
    }

    fun getProblemList( alphabet:String) {
        _loading.value = true

        viewModelScope.launch {
            try {


                val queryParams = mapOf(
                    "withICDCode" to true,
                    "alphabet" to alphabet
                )
                // This response is of type Response<ResponseBody>
                val response = RetrofitInstance
                    .createApiService7082( )
                    .dynamicGet(
                        url = ApiEndPoint().getProblemList,
                        params = queryParams
                    )
                _loading.value = false
                if (response.isSuccessful) {
                    val body = response.body()?.string()
                    body?.let {
                        val list = parseJsonToProblemList(it)
                        _problemList.postValue(list)
                    } ?: run {
                        _errorMessage.postValue("Empty response body")
                    }
                } else {
                    _errorMessage.value = "Error: ${response.code()}"
                }

            } catch (e: Exception) {
                _loading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
                e.printStackTrace()
            }
        }
    }

    private fun parseJsonToProblemList(json: String): List<Problem> {
        val jsonObject = JSONObject(json)
        val jsonArray = jsonObject.getJSONArray("responseValue")

        val type = object : TypeToken<List<Problem>>() {}.type
        return Gson().fromJson(jsonArray.toString(), type)
    }


    val selectedCountryId = MutableLiveData<String>()
    val selectedCountryName = MutableLiveData<String>()

    val selectedStateId = MutableLiveData<String>()
    val selectedStateName = MutableLiveData<String>()

    val selectedCityId = MutableLiveData<String>()
    val selectedCityName = MutableLiveData<String>()

    val pinCode = MutableLiveData<String>()
    val streetAddress = MutableLiveData<String>()
    private val _updateStateList = MutableLiveData<List<StateModel>>()
    val updateStateList: LiveData<List<StateModel>> get() = _updateStateList


    fun getStateMasterByCountryId( id:String) {
        _loading.value = true

        viewModelScope.launch {
            try {


                val queryParams = mapOf(
                    "id" to id.split(".")[0].toString(),
                    "clientId" to "176"
                )
                // This response is of type Response<ResponseBody>
                val response = RetrofitInstance
                    .createApiService5119( )
                    .dynamicGet(
                        url = ApiEndPoint().getStateMasterByCountryId,
                        params = queryParams
                    )
                _loading.value = false
                if (response.isSuccessful) {
                    val body = response.body()?.string()
                    val type = object : TypeToken<ApiResponse<List<StateModel>>>() {}.type
                    val parsedResponse = Gson().fromJson<ApiResponse<List<StateModel>>>(body, type)

                    if (parsedResponse.status == 1) {
                        _updateStateList.value = parsedResponse.responseValue
                    } else {
                        _errorMessage.value = "No states found"
                    }


                } else {
                    _errorMessage.value = "Error: ${response.code()}"
                }

            } catch (e: Exception) {
                _loading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
                e.printStackTrace()
            }
        }
    }

    val _updateCityList = MutableLiveData<List<CityModel>>()
    val updateCityList: LiveData<List<CityModel>> = _updateCityList

    fun getCityMasterByStateId( id:String) {
        _loading.value = true

        viewModelScope.launch {
            try {


                val queryParams = mapOf(
                    "id" to id.split(".")[0].toString(),
                    "clientId" to "176"
                )
                // This response is of type Response<ResponseBody>
                val response = RetrofitInstance
                    .createApiService5119( )
                    .dynamicGet(
                        url = ApiEndPoint().getCityMasterByStateId,
                        params = queryParams
                    )
                _loading.value = false
                if (response.isSuccessful) {
                    val body = response.body()?.string()

                    val type = object : TypeToken<ApiResponse<List<CityModel>>>() {}.type
                    val parsedResponse = Gson().fromJson<ApiResponse<List<CityModel>>>(body, type)

                    if (parsedResponse.status == 1) {
                        _updateCityList.value = parsedResponse.responseValue
                    } else {
                        _errorMessage.value = "No states found"
                    }


                } else {
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