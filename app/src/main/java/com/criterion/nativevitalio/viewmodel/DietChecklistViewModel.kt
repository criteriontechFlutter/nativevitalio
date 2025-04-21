package com.critetiontech.ctvitalio.viewmodel

import PrefsManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.model.DietItemModel
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DietChecklistViewModel: ViewModel() {

    private val _dietList = MutableLiveData<List<DietItemModel>>()
    val dietList: LiveData<List<DietItemModel>> get() = _dietList

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun getFoodIntake(date:  String?) {
        _loading.value = true
        val finalDate = if (date.isNullOrBlank()) {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        } else {
            date
        }

        viewModelScope.launch {
            try {
                val queryParams = mapOf(
                    "Uhid" to PrefsManager().getPatient()?.uhID.toString(),
                    "entryType" to "D",
                    "fromDate" to finalDate,
                )

                val response = RetrofitInstance
                    .createApiService7096(includeAuthHeader=true)
                    .dynamicGet(
                        url = ApiEndPoint().getFoodIntake,
                        params = queryParams
                    )

                _loading.value = false

                if (response.isSuccessful) {
                    val json = response.body()?.string()
                    val rootObj = Gson().fromJson(json, JsonObject::class.java)
                    val listJson = rootObj.getAsJsonArray("foodIntakeList")

                    val type = object : TypeToken<List<DietItemModel>>() {}.type
                    val parsedList: List<DietItemModel> = Gson().fromJson(listJson, type)

                    _dietList.postValue(parsedList)
                } else {
                    _dietList.postValue(emptyList())
                    _errorMessage.value = "Error: ${response.code()}"
                }

            } catch (e: Exception) {
                _loading.value = false
                _dietList.postValue(emptyList())
                _errorMessage.value = e.message ?: "Unknown error occurred"
                e.printStackTrace()
            }
        }
    }




    fun intakeByDietID(
        dietID:String,
        fullDateTime:String
    ) {
        _loading.value = true

        viewModelScope.launch {
            try {
//                val convertedTime = convertTo24Hour(compareTime)

                val queryParams = mapOf(
                    "Uhid" to PrefsManager().getPatient()?.uhID.toString(),
                    "userID" to PrefsManager().getPatient()?.id.toString(),
                    "dietID"  to  dietID,
//                    "intakeDateAndTime"  to    java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())+" "+convertedTime,

                    "givenAt" to fullDateTime

//                      givenAt=2025-04-17 07:34 PM
                )

                // This response is of type Response<ResponseBody>
                val response = RetrofitInstance
                    .createApiService7096(
                    )
                    .queryDynamicRawPost(
                        url = ApiEndPoint().intakeByDietID,
                        params = queryParams
                    )
                getFoodIntake( "")
                _loading.value = false
                if (response.isSuccessful) {
                    val json = response.body()?.string()
                    Log.d("RESPONSE", "responseValue: $json")

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