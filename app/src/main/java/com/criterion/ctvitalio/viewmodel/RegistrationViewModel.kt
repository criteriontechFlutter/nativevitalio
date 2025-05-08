package com.criterion.nativevitalio.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.criterion.nativevitalio.model.CityModel
import com.criterion.nativevitalio.model.Problem
import com.criterion.nativevitalio.model.StateModel
import com.criterion.nativevitalio.networking.RetrofitInstance
import com.criterion.nativevitalio.utils.ApiEndPoint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import org.json.JSONObject

class RegistrationViewModel  : ViewModel(){
    val firstName = MutableLiveData<String>()
    val lastName = MutableLiveData<String>()
    val gender = MutableLiveData<String>()
    val dob = MutableLiveData<String>()
    val bg = MutableLiveData<String>()
    val selectedCountryId = MutableLiveData<String>()
    val selectedCountryName = MutableLiveData<String>()

    val selectedStateId = MutableLiveData<String>()
    val selectedStateName = MutableLiveData<String>()

    val selectedCityId = MutableLiveData<String>()
    val selectedCityName = MutableLiveData<String>()
    val pinCode = MutableLiveData<String>()
    val streetAddress = MutableLiveData<String>()
    val wt = MutableLiveData<String>()


    val chronicDisease = MutableLiveData<String>()
    val otherChronicDiseases = MutableLiveData<String>()
    val familyDiseases = MutableLiveData<String>()


    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

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


    fun patientSignUp(
        nameV: String,
        bloodGroupIdV: String,
        genderIdV: String,
        heightV: String,
        weightV: String,
        zipV: String,
        addressV: String,
        countryCallingCodeV: String,
        mobileNoV: String,
        countryIdV: String,
        stateIdV: String,
        cityIdV: String,
        age: String){
        _loading.value = true

        viewModelScope.launch {
            try {
                val feet = "5"
                val inch = "8"

                val height = (if (feet.isEmpty()) 0.0 else feet.toInt() * 30.48) +
                        (if (inch.isEmpty()) 0.0 else inch.toInt() * 2.54)

                val queryParams = mapOf(
                    "patientName" to nameV,
                    "genderId" to genderIdV,
                    "bloodGroupId" to bloodGroupIdV,
                    "height" to height,
                    "weight" to weightV,
                    "zip" to zipV,
                    "address" to addressV,
                    "countryCallingCode" to countryCallingCodeV,
                    "mobileNo" to mobileNoV,
                    "countryId" to countryIdV,
                    "stateId" to stateIdV,
                    "cityId" to cityIdV,
                    "dob" to age.toString(),
//                    "choronicDiseasesJson" to Gson().toJson(getSelectedProblemList),
//                    "familyDiseaseJson" to Gson().toJson(getSelectedFamilyProblemList),
                    // "reminderJson" to Gson().toJson(temp),
                    "clientId" to "194",
                    "isExternal" to "1"
                )

                // This response is of type Response<ResponseBody>
                val response = RetrofitInstance
                    .createApiService7083( )
                    .dynamicGet(
                        url = ApiEndPoint().patientSignUp,
                        params = queryParams
                    )
                _loading.value = false
                if (response.isSuccessful) {



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
    private val _problemList = MutableLiveData<List<Problem>>()
    val problemList: LiveData<List<Problem>> = _problemList
    private val _selectedDiseaseList = MutableLiveData<MutableList<String>>(mutableListOf())
    val selectedDiseaseList: LiveData<MutableList<String>> get() = _selectedDiseaseList

    // Function to add a disease (if not already added)
    fun addSelectedDisease(disease: String) {
        if (!_selectedDiseaseList.value!!.contains(disease)) {
            _selectedDiseaseList.value = _selectedDiseaseList.value!!.apply {
                add(disease)
            }
        }
    }

    // Optional: remove a disease
    fun removeSelectedDisease(disease: String) {
        _selectedDiseaseList.value = _selectedDiseaseList.value!!.apply {
            remove(disease)
        }
    }

    // Optional: clear all selections
    fun clearSelectedDiseases() {
        _selectedDiseaseList.value = mutableListOf()
    }
    private val _otherChronicDiseaseList = MutableLiveData<MutableList<String>>(mutableListOf())
    val otherChronicDiseaseList: LiveData<MutableList<String>> get() = _otherChronicDiseaseList

    fun addOtherChronicDisease(disease: String) {
        if (!_otherChronicDiseaseList.value!!.contains(disease)) {
            _otherChronicDiseaseList.value = _otherChronicDiseaseList.value!!.apply { add(disease) }
        }
    }

    fun removeOtherChronicDisease(disease: String) {
        _otherChronicDiseaseList.value = _otherChronicDiseaseList.value!!.apply { remove(disease) }
    }

    fun clearOtherChronicDiseases() {
        _otherChronicDiseaseList.value = mutableListOf()
    }



    // Family Disease List
    private val _familyDiseaseList = MutableLiveData<MutableList<String>>(mutableListOf())
    private val _familyDiseaseMap = MutableLiveData<MutableMap<String, MutableList<String>>>(mutableMapOf())
    val familyDiseaseMap: LiveData<MutableMap<String, MutableList<String>>> get() = _familyDiseaseMap

    fun addDiseaseForRelation(relation: String, disease: String) {
        val currentMap = _familyDiseaseMap.value ?: mutableMapOf()
        val currentList = currentMap[relation] ?: mutableListOf()
        if (!currentList.contains(disease)) {
            currentList.add(disease)
            currentMap[relation] = currentList
            _familyDiseaseMap.value = currentMap.toMutableMap()
        }
    }

    fun removeFamilyRelation(relation: String) {
        val currentMap = _familyDiseaseMap.value ?: return
        currentMap.remove(relation)
        _familyDiseaseMap.value = currentMap.toMutableMap()
    }
    fun removeDiseaseFromRelation(relation: String, disease: String) {
        val map = _familyDiseaseMap.value ?: return
        val updatedList = map[relation]?.toMutableList() ?: return

        updatedList.remove(disease)
        if (updatedList.isEmpty()) {
            map.remove(relation)
        } else {
            map[relation] = updatedList
        }
        _familyDiseaseMap.value = map.toMutableMap()
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
}
data class ApiResponse<T>(
    val status: Int,
    val responseValue: T
)