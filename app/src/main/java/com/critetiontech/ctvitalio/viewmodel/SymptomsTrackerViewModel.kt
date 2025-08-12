package com.critetiontech.ctvitalio.viewmodel

import PrefsManager
import android.app.Application
import android.content.Context
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.utils.ToastUtils
import com.critetiontech.ctvitalio.viewmodel.BaseViewModel
import com.critetiontech.ctvitalio.model.SymptomDetail
import com.critetiontech.ctvitalio.model.SymptomResponse
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SymptomsTrackerViewModel(application: Application) : BaseViewModel(application){

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
                    "uhID" to PrefsManager().getPatient()?.empId.toString(),
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
                }

            } catch (e: Exception) {
                _loading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
                e.printStackTrace()
            }
        }
    }


    fun insertSymptoms(
        navController: NavController,
        requireContext: Context,
        selectedSymptoms: List<SymptomDetail>
    ) {
        _loading.value = true

        viewModelScope.launch {
            try {
                val dtDataTable = mutableListOf<Map<String, String>>()

                // Get the current timestamp once
                val now = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"))
                } else {
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                }

                // Populate data table from selected symptoms
                selectedSymptoms.forEach { symptom ->
                    dtDataTable.add(
                        mapOf(
                            "detailID" to symptom.pdmID.toString(),
                            "detailsDate" to now,
                            "details" to symptom.details,
                            "isFromPatient" to "1"
                        )
                    )
                }

                val queryParams = mapOf(
                    "uhID" to (PrefsManager().getPatient()?.empId ?: ""),
                    "userID" to (PrefsManager().getPatient()?.userId ?: ""),
                    "doctorId" to (PrefsManager().getPatient()?.doctorID ?: ""),
                    "jsonSymtoms" to Gson().toJson(dtDataTable),
                    "clientID" to (PrefsManager().getPatient()?.clientId ?: "")
                )

                val response = RetrofitInstance
                    .createApiService()
                    .dynamicRawPost(
                        url = ApiEndPoint().insertSymtoms,
                        body = queryParams
                    )



                if (response.isSuccessful) {
                    _loading.value = false
                    ToastUtils.showSuccessPopup(requireContext, "Symptoms updated successfully!")
                    getSymptoms() // reload updated list

                    navController.navigate(R.id.action_symptomTrackerFragments_to_symptomsFragment)


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
