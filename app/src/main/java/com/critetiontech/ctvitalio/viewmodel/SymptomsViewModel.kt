package com.critetiontech.ctvitalio.viewmodel

import PrefsManager
import android.app.Application
import android.content.Context
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.critetiontech.ctvitalio.utils.ToastUtils
import com.critetiontech.ctvitalio.viewmodel.BaseViewModel
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.model.ProblemWithIcon
import com.critetiontech.ctvitalio.model.SymptomDetail
import com.critetiontech.ctvitalio.model.SymptomResponse
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.google.gson.Gson
import kotlinx.coroutines.launch

class SymptomsViewModel (application: Application) : BaseViewModel(application) {

    private val _symptomList = MutableLiveData<List<ProblemWithIcon>>()
    val symptomList: LiveData<List<ProblemWithIcon>> get() = _symptomList

    private val _moreSymptomList = MutableLiveData<List<ProblemWithIcon>>()
    val moreSymptomList: LiveData<List<ProblemWithIcon>> get() = _moreSymptomList


    private val _searchSymptomList = MutableLiveData<List<ProblemWithIcon>>()
    val searchSymptomList: LiveData<List<ProblemWithIcon>> get() = _searchSymptomList

    private val _searchSelectedSymptomList = MutableLiveData<List<ProblemWithIcon>>()
    val searchSelectedSymptomList: LiveData<List<ProblemWithIcon>> get() = _searchSelectedSymptomList


    private val _selectedSymptoms = MutableLiveData<MutableList<ProblemWithIcon>>(mutableListOf())
    val selectedSymptoms: MutableLiveData<MutableList<ProblemWithIcon>> get() = _selectedSymptoms

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading


    private val _patientSymptomList = MutableLiveData<List<SymptomDetail>>()
    val  patientSymptomList: LiveData<List<SymptomDetail>> get() = _patientSymptomList

    fun getSymptoms() {
        _loading.value = true
        viewModelScope.launch {
            try {
                val queryParams = mapOf(
                    "uhID" to PrefsManager().getPatient()?.uhID.toString(),
                    "clientID" to PrefsManager().getPatient()?.clientId.toString(),
                )

                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true)
                    .queryDynamicRawPost(
                        url = ApiEndPoint().getSymptoms,
                        params = queryParams
                    )



                if (response.isSuccessful) {
                    _loading.value = false
                    val json = response.body()?.string()
                    val parsed = Gson().fromJson(json, SymptomResponse::class.java)
                    _patientSymptomList.value = parsed.responseValue
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
    fun getAllPatientMedication() {
        _loading.value = true
        viewModelScope.launch {
            try {
                val queryParams = mapOf(
                    "problemName" to "",
                    "languageId" to "1"
                )

                val response = RetrofitInstance
                    .createApiService(
                        overrideBaseUrl = ApiEndPoint().digiDoctorBaseUrl,
                        additionalHeaders = mapOf(
                            "Content-Type" to "application/json; charset=UTF-8"
                        )
                    )
                    .dynamicRawPost(
                        url = ApiEndPoint().getProblemsWithIcon,
                        body = queryParams
                    )



                if (response.isSuccessful) {
                    _loading.value = false
                    val json = response.body()?.string()
                    val parsed = Gson().fromJson(json, SymptomApiResponse::class.java)
                    _symptomList.value = parsed.responseValue
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

    fun getAllSuggestedProblem() {
        _loading.value = true
        viewModelScope.launch {
            try {
                val queryParams = mapOf(
                    "memberId" to "380258"
                )

                val response = RetrofitInstance
                    .createApiService(
                        overrideBaseUrl = ApiEndPoint().digiDoctorBaseUrl,
                        additionalHeaders = mapOf(
                            "Content-Type" to "application/json; charset=UTF-8"
                        )
                    )
                    .dynamicRawPost(
                        url = ApiEndPoint().getAllSuggestedProblem,
                        body = queryParams
                    )



                if (response.isSuccessful) {
                    _loading.value = false
                    val json = response.body()?.string()
                    val parsed = Gson().fromJson(json, SymptomApiResponse::class.java)
                    val symptomsList: List<ProblemWithIcon> = parsed.responseValue
                    _moreSymptomList.value = parsed.responseValue
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




    fun getAllProblems(query:String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val queryParams = mapOf(
                    "alphabet" to query,
                    "language" to  1
                )

                val response = RetrofitInstance
                    .createApiService(
                        overrideBaseUrl = ApiEndPoint().digiDoctorBaseUrl,
                        additionalHeaders = mapOf(
                            "Content-Type" to "application/json; charset=UTF-8"
                        )
                    )
                    .dynamicRawPost(
                        url = ApiEndPoint().getAllProblems,
                        body = queryParams
                    )



                if (response.isSuccessful) {
                    _loading.value = false
                    val json = response.body()?.string()
                    val parsed = Gson().fromJson(json, SymptomApiResponse::class.java)
                    _searchSymptomList.value = parsed.responseValue
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


// &clientID=${userRepository.getUser.clientId.toString()}',

fun insertSymptoms(findNavController: NavController, requireContext: Context) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val dtDataTable = mutableListOf<Map<String, String>>()

// Get the current timestamp once
                val now = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"))
                } else {
                    TODO("VERSION.SDK_INT < O")
                }

// Add items from _selectedSymptoms
                _selectedSymptoms.value?.forEach { symptom ->
                    dtDataTable.add(
                        mapOf(
                            "detailID" to symptom.problemId.toString(),
                            "detailsDate" to now,
                            "details" to symptom.problemName,
                            "isFromPatient" to "1"
                        )
                    )
                }
                patientSymptomList.value?.forEach { symptom ->
                    dtDataTable.add(
                        mapOf(
                            "detailID" to symptom.detailID.toString(),
                            "detailsDate" to symptom.detailsDate ,
                            "details" to symptom.details,
                            "isFromPatient" to "1"
                        )
                    )
                }

// Add items from _searchSelectedSymptomList
                _searchSelectedSymptomList.value?.forEach { symptom ->
                    dtDataTable.add(
                        mapOf(
                            "detailID" to symptom.problemId.toString(),
                            "detailsDate" to now,
                            "details" to symptom.problemName,
                            "isFromPatient" to "1"
                        )
                    )
                }

                val queryParams = mapOf(
                    "uhID" to (PrefsManager().getPatient()?.uhID ?: ""),
                    "userID" to  "0",
                    "doctorId" to  "0",
                    "jsonSymtoms" to  Gson().toJson(dtDataTable),
                    "clientID" to  (PrefsManager().getPatient()?.clientId ?: ""),
                )

                val response = RetrofitInstance
                    .createApiService(

                    )
                    .queryDynamicRawPost(
                        url = ApiEndPoint().insertSymtoms,
                        params = queryParams
                    )



                if (response.isSuccessful) {
                    _loading.value = false
                    ToastUtils.showSuccessPopup(requireContext, "Symptom saved successfully!!")
                    getSymptoms()
                    _selectedSymptoms.value = mutableListOf()
                    _searchSelectedSymptomList.value = mutableListOf()
                    val json = response.body()?.string()
                    findNavController.navigate(R.id.action_symptomsFragment_to_symptomHistory)

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
    fun clearSearchResults() {
        _searchSymptomList.value = emptyList()
    }
    fun toggleSymptomSelection(symptom: ProblemWithIcon, isFromSearch: Boolean = false) {
        val normalSelected = _selectedSymptoms.value?.toMutableList() ?: mutableListOf()
        val searchSelected = _searchSelectedSymptomList.value?.toMutableList() ?: mutableListOf()

        if (isFromSearch) {
            _searchSymptomList.value = emptyList()
            if (searchSelected.contains(symptom)) {
                searchSelected.remove(symptom)
//                normalSelected.remove(symptom)
            } else {
                searchSelected.add(symptom)
//                if (!normalSelected.contains(symptom)) {
//                    normalSelected.add(symptom)
//                }
            }
        } else {
            if (normalSelected.contains(symptom)) {
                normalSelected.remove(symptom)
//                searchSelected.remove(symptom)
            } else {
                normalSelected.add(symptom)
//                if (!searchSelected.contains(symptom)) {
//                    searchSelected.add(symptom)
//                }
            }
        }

        _selectedSymptoms.value = normalSelected
        _searchSelectedSymptomList.value = searchSelected
    }
}
data class SymptomApiResponse(
    val responseCode: Int,
    val responseMessage: String,
    val responseValue: List<ProblemWithIcon>
)

// SymptomModel.kt
data class SymptomModel(
    val problemId: Int,
    val problemName: String,
    val isVisible: Int,
    val displayIcon: String,
    val translation: String?
)