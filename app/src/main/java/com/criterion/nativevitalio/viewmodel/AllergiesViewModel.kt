package com.criterion.nativevitalio.viewmodel

import PrefsManager
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.criterion.nativevitalio.model.AllergyApiResponse
import com.criterion.nativevitalio.model.AllergyGroup
import com.criterion.nativevitalio.model.AllergyHistoryItem
import com.criterion.nativevitalio.model.AllergyTypeItem
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AllergiesViewModel  :ViewModel(){

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _allergyList = MutableLiveData<List<AllergyHistoryItem>>()
    val allergyList: LiveData<List<AllergyHistoryItem>> get() = _allergyList

    val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    val allergyTypes = MutableLiveData<List<AllergyTypeItem>>()

    fun getAllergies() {
        _loading.postValue(true)

        viewModelScope.launch {
            try {
                val queryParams = mapOf(
                    "uhID" to PrefsManager().getPatient()?.uhID.orEmpty(),
                    "clientID" to PrefsManager().getPatient()?.clientId.toString()
                )

                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true)
                    .dynamicGet(
                        url = ApiEndPoint().patientAllergies,
                        params = queryParams
                    )



                if (response.isSuccessful) {
                    _loading.value = false
                    val json = response.body()?.string()
                    if (json.isNullOrEmpty()) {
                        _errorMessage.postValue("Empty response")
                        _loading.postValue(false)
                        return@launch
                    }

                    val type = object : TypeToken<AllergyApiResponse<List<AllergyGroup>>>() {}.type
                    val parsed = Gson().fromJson<AllergyApiResponse<List<AllergyGroup>>>(json, type)

                    val allItems = mutableListOf<AllergyHistoryItem>()

                    parsed.responseValue?.forEach { group ->
                        val itemType = object : TypeToken<List<AllergyHistoryItem>>() {}.type
                        val historyItems: List<AllergyHistoryItem> = Gson().fromJson(group.jsonHistory, itemType)

                        historyItems.forEachIndexed { index, item ->
                            allItems.add(
                                item.copy(
                                    substance = item.substance ?: "Unknown",             // ✅ fallback
                                    remark = item.remark ?: "",
                                    severityLevel = item.severityLevel ?: "",
                                    category = if (index == 0) group.parameterName else null
                                )
                            )
                        }
                    }

                    _allergyList.value = allItems
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

    fun getHistorySubCategoryMasterById() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = false)
                    .dynamicGet(
                        url = ApiEndPoint().getHistorySubCategoryMasterById,
                        params = mapOf("CategoryId" to 23)
                    )

                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    println("Fetched Allergy Types: $responseBody")

                    val parsed = JSONObject(responseBody)
                    if (parsed.getInt("status") == 1) {
                        val jsonArray = parsed.getJSONArray("responseValue")
                        val type = object : TypeToken<List<AllergyTypeItem>>() {}.type
                        val data: List<AllergyTypeItem> = Gson().fromJson(jsonArray.toString(), type)
                        allergyTypes.postValue(data)
                    } else {
                        _errorMessage.postValue(parsed.getString("responseValue"))
                    }
                } else {
                    _errorMessage.postValue("API Error: ${response.code()}")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.postValue("Exception: ${e.localizedMessage}")
            }
        }
    }



    fun savePatientAllergies(
        parameterStatement: String,
        substance: String,
        reaction: String,
        severity: String,
        historyParameterAssignId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        _loading.value = true
        val user = PrefsManager().getPatient() ?: return

        val allergiesJson = listOf(
            mapOf(
                "parameterValueId" to "133",
                "parameterStatement" to parameterStatement,
                "clinicalDataTypeId" to "0",
                "clinicalDataTypeRowId" to "0",
                "date" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date()),
                "remark" to reaction,
                "substance" to substance,
                "severityLevel" to severity,
                "isFromPatient" to "1",
                "historyParameterAssignId" to historyParameterAssignId
            )
        )

        val encodedJson = Uri.encode(Gson().toJson(allergiesJson))
//        val url = "api/PatientIPDPrescription/SavePatientAllergies?" +
//                "uhID=${user.uhID}&clientID=${user.clientId}&allergiesJson=$encodedJson"
        val params = mapOf(
            "uhID" to user.uhID,
            "clientID" to user.clientId,
            "allergiesJson" to encodedJson,
            )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true)
                    .queryDynamicRawPost(
                        url = ApiEndPoint().savePatientAllergies,
                        params = params
                    )


                withContext(Dispatchers.Main) {

                    if (response.isSuccessful) {
                        _loading.value = false
                        val data = response.body()?.string()
                        val jsonObject = JSONObject(data ?: "{}")
                        if (jsonObject.getInt("status") == 1) {
                            _loading.value = false
                       getAllergies()
                            onSuccess()
                        } else {
                            _loading.value = false
                            onError(jsonObject.getString("responseValue"))
                        }
                    } else {
                        _loading.value = false
                        onError("Error: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    e.printStackTrace()
                    onError(e.localizedMessage ?: "Unknown error occurred")
                }
            }
        }
    }

}