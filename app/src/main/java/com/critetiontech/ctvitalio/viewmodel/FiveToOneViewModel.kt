package com.critetiontech.ctvitalio.viewmodel

import PrefsManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.model.StatusApiResponse
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.google.gson.Gson
import kotlinx.coroutines.launch

private data class InsertResponse(
    val status: Int,
    val message: String,
    val responseValue: String?
)

class FiveToOneViewModel : ViewModel() {

    private val _itemsByModule = MutableLiveData<Map<String, List<String>>>()
    val itemsByModule: LiveData<Map<String, List<String>>> = _itemsByModule

    private val _insertMessage = MutableLiveData<Pair<Boolean, String>>()
    val insertMessage: LiveData<Pair<Boolean, String>> = _insertMessage

    init {
        fetchStatusItems()
    }

    private fun fetchStatusItems() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.createApiService().dynamicGet(
                    url = "api/StatusMaster/GetAllStatus?typeModule=SEE%2CTOUCH%2CHEAR%2CSMELL%2CTASTE",
                    params = emptyMap<String, Any>()
                )
                if (response.isSuccessful) {
                    val json = response.body()?.string()
                    val parsed = Gson().fromJson(json, StatusApiResponse::class.java)
                    if (parsed?.status == 1) {
                        val grouped = parsed.responseValue
                            .groupBy { it.module.uppercase() }
                            .mapValues { (_, items) -> items.map { it.remark } }
                        _itemsByModule.postValue(grouped)
                    }
                }
            } catch (e: Exception) {
                Log.e("FiveToOneViewModel", "fetchStatusItems error: ${e.message}", e)
            }
        }
    }

    fun insertMindfulness(mindfulnessJson: String, totalSteps: Int) {
        val patient = PrefsManager().getPatient()
        viewModelScope.launch {
            try {
                val body = mapOf<String, Any>(
                    "pid" to (patient?.id ?: 0),
                    "exerciseId" to 9,
                    "duration" to 5,
                    "totalSteps" to totalSteps,
                    "mindfulnessJson" to mindfulnessJson,
                    "userId" to (patient?.userId?.toIntOrNull() ?: 0),
                    "clientId" to (patient?.clientId ?: 0)
                )
                val response = RetrofitInstance.createApiService().dynamicRawPost(
                    url = "api/CorporateMindfulness/InsertCorporateMindfulness",
                    body = body
                )
                val json = if (response.isSuccessful) response.body()?.string()
                           else response.errorBody()?.string()
                val parsed = try { Gson().fromJson(json, InsertResponse::class.java) } catch (e: Exception) { null }
                val success = parsed?.status == 1
                val msg = if (success) "Activity completed successfully!"
                          else parsed?.responseValue ?: parsed?.message ?: "Something went wrong"
                _insertMessage.postValue(Pair(success, msg))
            } catch (e: Exception) {
                Log.e("FiveToOneViewModel", "insertMindfulness error: ${e.message}", e)
                _insertMessage.postValue(Pair(false, "Something went wrong"))
            }
        }
    }
}