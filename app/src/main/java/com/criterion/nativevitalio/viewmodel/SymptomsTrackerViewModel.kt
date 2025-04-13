package com.criterion.nativevitalio.viewmodel

import PrefsManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.criterion.nativevitalio.Utils.ApiEndPoint
import com.criterion.nativevitalio.model.ProblemWithIcon
import com.criterion.nativevitalio.model.SymptomDetail
import com.criterion.nativevitalio.model.SymptomResponse
import com.criterion.nativevitalio.networking.RetrofitInstance
import com.google.gson.Gson
import kotlinx.coroutines.launch

class SymptomsTrackerViewModel : ViewModel() {

    private val _symptomList = MutableLiveData<List<SymptomDetail>>()
    val symptomList: LiveData<List<SymptomDetail>> get() = _symptomList

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun getSymptoms() {
        _loading.value = true
        viewModelScope.launch {
            try {
                val queryParams = mapOf(
                    "uhID" to PrefsManager().getPatient()?.uhid.toString(),
                    "clientID" to PrefsManager().getPatient()?.clientId.toString(),
                )

                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true)
                    .queryDynamicRawPost(
                        url = ApiEndPoint().getSymptoms,
                        params = queryParams
                    )

                _loading.value = false

                if (response.isSuccessful) {
                    val json = response.body()?.string()
                    val parsed = Gson().fromJson(json, SymptomResponse::class.java)
                    _symptomList.value = parsed.responseValue
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
