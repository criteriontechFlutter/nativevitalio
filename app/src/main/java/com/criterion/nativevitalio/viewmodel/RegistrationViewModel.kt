package com.criterion.nativevitalio.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.criterion.nativevitalio.model.StateModel
import com.criterion.nativevitalio.networking.RetrofitInstance
import com.criterion.nativevitalio.utils.ApiEndPoint
import kotlinx.coroutines.launch

class RegistrationViewModel  : ViewModel(){

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
                    "id" to id.toString()
                )
                // This response is of type Response<ResponseBody>
                val response = RetrofitInstance
                    .createApiService7083( )
                    .dynamicGet(
                        url = ApiEndPoint().getStateMasterByCountryId,
                        params = queryParams
                    )
                _loading.value = false
                if (response.isSuccessful) {
                    val body = response.body()?.string()  // response is Response<ResponseBody>
//                    val parsedResponse = Gson().fromJson(body, ApiResponse<List<StateModel>>::class.java)

//                    if (parsedResponse.status == 1) {
//                        _updateStateList.value = parsedResponse.responseValue
//                    } else {
//                        _errorMessage.value = "No states found"
//                    }


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


    fun getCityMasterByStateId( id:String) {
        _loading.value = true

        viewModelScope.launch {
            try {


                val queryParams = mapOf(
                    "id" to id.toString()
                )
                // This response is of type Response<ResponseBody>
                val response = RetrofitInstance
                    .createApiService7083( )
                    .dynamicGet(
                        url = ApiEndPoint().getCityMasterByStateId,
                        params = queryParams
                    )
                _loading.value = false
                if (response.isSuccessful) {
                    val body = response.body()?.string()  // response is Response<ResponseBody>
//                    val parsedResponse = Gson().fromJson(body, ApiResponse<List<StateModel>>::class.java)

//                    if (parsedResponse.status == 1) {
//                        _updateStateList.value = parsedResponse.responseValue
//                    } else {
//                        _errorMessage.value = "No states found"
//                    }


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
}data class ApiResponse<T>(
    val status: Int,
    val responseValue: T
)