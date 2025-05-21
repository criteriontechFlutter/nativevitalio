package com.critetiontech.ctvitalio.viewmodel

import Patient
import PrefsManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.UI.Login
import com.critetiontech.ctvitalio.model.FrequencyModel
import com.critetiontech.ctvitalio.model.VitalReminder
import com.critetiontech.ctvitalio.model.BaseResponse
import com.critetiontech.ctvitalio.model.CityModel
import com.critetiontech.ctvitalio.model.Problem
import com.critetiontech.ctvitalio.model.StateModel
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.critetiontech.ctvitalio.utils.MyApplication
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class RegistrationViewModel  : ViewModel(){
    val firstName = MutableLiveData<String>()
    val lastName = MutableLiveData<String>()
    val gender = MutableLiveData<String>()
    val genderId = MutableLiveData<String>()
    val dob = MutableLiveData<String>()
    val bg  = MutableLiveData<String>()
    val bgId = MutableLiveData<String>()
    val selectedCountryId = MutableLiveData<String>()
    val selectedCountryName = MutableLiveData<String>()

    val selectedStateId = MutableLiveData<String>()
    val selectedStateName = MutableLiveData<String>()

    val selectedCityId = MutableLiveData<String>()
    val selectedCityName = MutableLiveData<String>()
    val pinCode = MutableLiveData<String>()
    val streetAddress = MutableLiveData<String>()
    val wt = MutableLiveData<String>()
    val ht = MutableLiveData<String>()
    val htInCm = MutableLiveData<String>()


    val chronicDisease = MutableLiveData<String>()
    val otherChronicDiseases = MutableLiveData<String>()
    val familyDiseases = MutableLiveData<String>()
    val mobileNo = MutableLiveData<String>()


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







    // VITAL SET PREFERENCES
//    val setVitalList = MutableLiveData<MutableList<VitalReminder>>().apply {
//        value = mutableListOf(
//            VitalReminder(1, 4, "Blood Pressure", 0, "ONCE A DAY (24 HOURLY)", false),
//            VitalReminder(1, 74, "Heart Rate", 0, "ONCE A DAY (24 HOURLY)", false),
//            VitalReminder(1, 56, "Blood Oxygen (spo2)", 0, "ONCE A DAY (24 HOURLY)", false),
//            VitalReminder(1, 7, "Respiratory Rate", 0, "ONCE A DAY (24 HOURLY)", false),
//            VitalReminder(1, 3, "Pulse Rate", 0, "ONCE A DAY (24 HOURLY)", false),
//            VitalReminder(1, 10, "RBS", 0, "ONCE A DAY (24 HOURLY)", false)
//        )
//    }

    val setVitalList = MutableLiveData<MutableList<VitalReminder>>().apply {
        value = mutableListOf(
            VitalReminder(1, 4, "Blood Pressure", 0, "ONCE A DAY (24 HOURLY)", false),
            VitalReminder(1, 74, "Heart Rate", 0, "ONCE A DAY (24 HOURLY)", false),
            VitalReminder(1, 56, "Blood Oxygen (spo2)", 0, "ONCE A DAY (24 HOURLY)", false),
            VitalReminder(1, 7, "Respiratory Rate", 0, "ONCE A DAY (24 HOURLY)", false),
            VitalReminder(1, 3, "Pulse Rate", 0, "ONCE A DAY (24 HOURLY)", false),
            VitalReminder(1, 10, "RBS", 0, "ONCE A DAY (24 HOURLY)", false)
        )
    }
    fun updateVitalFrequency(index: Int, frequency: String) {
        val list = setVitalList.value ?: return
        list[index].frequencyType = frequency
        list[index].isCheck = true
        setVitalList.value = list
    }

    private val _frequencyDataList = MutableLiveData<List<FrequencyModel>>()
    val frequencyDataList: LiveData<List<FrequencyModel>> get() = _frequencyDataList

    fun getFrequencyList() {
        _loading.value = true

        viewModelScope.launch {
            try {
                val queryParams = mapOf(
                    "userId" to PrefsManager().getPatient()?.userId.toString(),
                    "alphabet" to ""
                )

                val response = RetrofitInstance
                    .createApiService7082()
                    .dynamicGet(
                        url = ApiEndPoint().getFrequencyList,
                        params = queryParams
                    )

                _loading.value = false

                if (response.isSuccessful) {
                    val body = response.body()?.string() ?: return@launch
                    val json = JSONObject(body)

                    if (json.optInt("status") == 1) {
                        val responseValue = json.getJSONArray("responseValue")
                        val list = Gson().fromJson<List<FrequencyModel>>(
                            responseValue.toString(),
                            object : TypeToken<List<FrequencyModel>>() {}.type
                        )
                        _frequencyDataList.value = list
                    } else {
                        _errorMessage.value = json.optString("message", "No data found")
                    }

                } else {
                    _errorMessage.value = "Error: ${response.code()}"
                }

            } catch (e: Exception) {
                _loading.value = false
                _errorMessage.value = e.localizedMessage ?: "Unknown error occurred"
                e.printStackTrace()
            }
        }
    }
    fun patientSignUp(
         ){
        _loading.value = true


        viewModelScope.launch {
            try {

                    val summary = "${ streetAddress.value.orEmpty()}, " +
                            "${ selectedCityName.value.orEmpty()}, " +
                            "${ selectedStateName.value.orEmpty()}, " +
                            "${ selectedCountryName.value.orEmpty()} - " +
                            "${ pinCode.value.orEmpty()}"
                val queryParams = mapOf(
                    "patientName" to "${firstName.value.orEmpty()} ${lastName.value.orEmpty()}",
                    "genderId" to genderId.value?.split(".")?.firstOrNull().orEmpty(),
                    "bloodGroupId" to bgId.value?.split(".")?.firstOrNull().orEmpty(),
                    "height" to htInCm.value.orEmpty(),
                    "weight" to wt.value.orEmpty(),
                    "zip" to "",
                    "address" to summary,
                    "countryCallingCode" to "+91",
                    "mobileNo" to mobileNo.value.orEmpty(),
                    "countryId" to selectedCountryId.value?.split(".")?.firstOrNull().orEmpty(),
                    "stateId" to selectedStateId.value?.split(".")?.firstOrNull().orEmpty(),
                    "cityId" to selectedCityId.value?.split(".")?.firstOrNull().orEmpty(),
                    "dob" to formatDob(dob.value.orEmpty()),
                    "choronicDiseasesJson" to Gson().toJson(selectedOtherChronicDiseaseList.value ?: emptyList<Map<String, String>>()),
                    "familyDiseaseJson" to Gson().toJson(familyDiseaseMap.value ?: emptyMap<String, List<String>>()),
                    "clientId" to "194",
                    "isExternal" to "1"
                )

                // This response is of type Response<ResponseBody>
                val response = RetrofitInstance
                    .createApiService7082( )
                    .dynamicRawPost(
                        url = ApiEndPoint().patientSignUp,
                        body = queryParams
                    )
                _loading.value = false
                if (response.isSuccessful) {
                    val responseString = response.body()?.string() ?: return@launch
                    val json = JSONObject(responseString)

                    if (json.optInt("status") == 1) {
                        val uhid = json.getJSONArray("responseValue")
                            .getJSONObject(0)
                            .getString("uhid")
                        getPatientDetailsByUHID(uhid=uhid.toString(),
                            context= MyApplication.appContext)
                        // Save UHID (example: in SharedPreferences or LiveData)
                        println("UHID = $uhid")
                        // PrefsManager().saveUHID(uhid) // Optional
                    } else {
                        _errorMessage.value = json.optString("message", "Signup failed.")
                    }
//
//                    findNavController().navigate(R.id.accountSuccess)

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
    private fun getPatientDetailsByUHID(uhid: String,context: Context) {
        _loading.value = true

        viewModelScope.launch {
            try {
                var mo = ""
                var uhidVal = ""

                if (uhid.toLowerCase().contains("uhid")) {
                    uhidVal = uhid
                } else {
                    mo = uhid
                }
                val queryParams = mapOf(
                    "mobileNo" to mo,
                    "uhid" to uhidVal,
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
//                        val intent = Intent(context, Home::class.java)
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                        context.startActivity(intent)
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
    private val _fluidIntake = MutableLiveData<Pair<Float, String>>() // value + unit
    val fluidIntake: LiveData<Pair<Float, String>> get() = _fluidIntake

    fun setFluidIntake(amount: Float, unit: String) {
        _fluidIntake.value = amount to unit
    }

    fun patientParameterSettingInsert(
//        fluidQty: String,
//        fluidQtyUnit: String
    ) {
        viewModelScope.launch {
            try {
                _loading.value = true

                // Convert fluid quantity to mL if needed
//                val fluidQtyData = if (fluidIntake == "litre") {
//                    fluidQty
//                } else {
//                    (fluidQty.toDouble() * 1000).toInt().toString()
//                }

                // Prepare vital parameter JSON
                val temp: MutableList<Map<String, Any>> = setVitalList.value?.map { vital ->
                    mapOf(
                        "parameterId" to vital.parameterId.toString(),
                        "parameterTypeId" to vital.parameterTypeId.toString(),
                        "name" to vital.name,
                        "quantity" to vital.quantity.toString(),
                        "frequencyType" to vital.frequencyType,
                        "isCheck" to false,
                        "uhid" to 0
                    )
                }?.toMutableList() ?: mutableListOf()
// Now you can safely add
                temp.add(
                    mapOf(
                        "parameterId" to "2",
                        "parameterTypeId" to "1",
                        "name" to "Fluid Limit",
                        "quantity" to fluidIntake.value.toString().split(".")[0].toString(),
                        "frequencyType" to "Daily",
                        "isCheck" to false,
                        "uhid" to "0"
                    )
                )

                val body = mapOf(
                    "parameterJson" to Gson().toJson(temp),
                    "clientId" to "194",
                    "pid" to PrefsManager().getPatient()?.pid.toString(),
                    "userId" to 0
                )

                val response = RetrofitInstance
                    .createApiService7082()
                    .dynamicRawPost(
                        url = "api/PatientParameterSetting/Insert",
                        body = body
                    )

                _loading.value = false

                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    val json = JSONObject(responseBody ?: "")
                    if (json.optInt("status") == 1) {
                        // Success case
                        println("Success: $json")
                    } else {
                        _errorMessage.value = json.optString("message", "Insert failed")
                    }
                } else {
                    _errorMessage.value = "API Error: ${response.code()}"
                }

            } catch (e: Exception) {
                _loading.value = false
                _errorMessage.value = e.message ?: "Unexpected error"
                e.printStackTrace()
            }
        }
    }
    private fun formatDob(input: String): String {
        return try {
            val inputFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val outputFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormatter.parse(input)
            outputFormatter.format(date!!)
        } catch (e: Exception) {
            input // fallback to original if parsing fails
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
            "detailsDate" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date()),
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

    // Optional: clear all selections

    private val _selectedOtherChronicDiseaseList = MutableLiveData<MutableList<Map<String, String>>>(mutableListOf())
    val selectedOtherChronicDiseaseList: LiveData<MutableList<Map<String, String>>> get() = _selectedOtherChronicDiseaseList

    fun addOtherChronicDisease(problem: Problem, context: Context) {
        val currentList = _selectedOtherChronicDiseaseList.value ?: mutableListOf()
        val detailID = problem.id.toString()

        if (currentList.any { it["detailID"] == detailID }) {
            Toast.makeText(context, "Already added", Toast.LENGTH_SHORT).show()
            return
        }

        val newEntry = mapOf(
            "detailID" to detailID,
            "detailsDate" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date()),
            "details" to problem.problemName,
            "isFromPatient" to "1"
        )

        currentList.add(newEntry)
        _selectedOtherChronicDiseaseList.value = currentList
    }

    fun removeOtherChronicDiseaseByName(name: String) {
        _selectedOtherChronicDiseaseList.value = _selectedOtherChronicDiseaseList.value?.filterNot {
            it["details"] == name
        }?.toMutableList()
    }




    // Family Disease List
    private val _familyDiseaseMap = MutableLiveData<MutableMap<String, MutableList<String>>>(mutableMapOf())
    val familyDiseaseMap: LiveData<MutableMap<String, MutableList<String>>> get() = _familyDiseaseMap

    // Unified Setter/Updater
    fun addDiseaseForRelation(relation: String, disease: String) {
        val currentMap = _familyDiseaseMap.value ?: mutableMapOf()

        val currentList = currentMap[relation] ?: mutableListOf()
        if (!currentList.contains(disease)) {
            currentList.add(disease)
            currentMap[relation] = currentList
            _familyDiseaseMap.value = currentMap.toMutableMap()
        }
    }

    // Remove full relation
    fun removeFamilyRelation(relation: String) {
        val currentMap = _familyDiseaseMap.value ?: return
        currentMap.remove(relation)
        _familyDiseaseMap.value = currentMap.toMutableMap()
    }

    // Remove specific disease from a relation
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