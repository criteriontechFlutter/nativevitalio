package com.critetiontech.ctvitalio.viewmodel

import Medicine
import MedicineResponse
import PrefsManager
import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class AddMedicineReminderViewModel(application: Application) : AndroidViewModel(application) {
private val _selectedMedicineName = MutableLiveData<String>()
    val selectedMedicineName: LiveData<String> get() = _selectedMedicineName

    fun updateSelectedMedicineName(name: String) {
        _selectedMedicineName.value = name
    }
    var selectedMonthDays = mutableStateListOf<String>()
        private set

    fun addSpecificDate(day: String) {
        if (!selectedMonthDays.contains(day)) {
            selectedMonthDays.add(day)
        }
    }

    fun removeSpecificDate(day: String) {
        selectedMonthDays.remove(day)
    }
var timeSlots = mutableStateListOf<String>()
    private set

    fun addTimeSlot(time: String) {
        timeSlots.add(time)
    }

    fun updateTimeSlot(index: Int, time: String) {
        timeSlots[index] = time
    }

    fun removeTimeSlot(index: Int) {
        timeSlots.removeAt(index)
    }
    var selectedMedicine = MutableLiveData<Medicine>()

    fun updateSelectedMedicine(data: Medicine) {
        selectedMedicine.value = data
    }
    private val _medicineLiveData = MutableLiveData<List<Medicine>>()
    val medicineLiveData: LiveData<List<Medicine>> get() = _medicineLiveData
     fun getBrandList(alphabet: String) {
        viewModelScope.launch {
            try {
                val queryParams = mapOf("alphabet" to alphabet)

                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true, overrideBaseUrl = "http://182.156.200.177:5082/")
                    .dynamicGet(
                        url = "api/KnowMedApis/GetBrandList",
                        params = queryParams
                    )

                if (response.isSuccessful) {
                    val json = response.body()?.string()

                    Log.d("RESPONSE", json ?: "null")

                    if (!json.isNullOrEmpty()) {

                        // Parse JSON using Gson
                        val data = Gson().fromJson(json, MedicineResponse::class.java)

                        // Get list from response
                        _medicineLiveData.value = data.responseValue


                        // Example: Print names

                    }
                }else {
                }

            } catch (e: Exception) {
                Log.d("ITEM", e.toString())
            }
        }
    }
       fun addMedicine(
           startdate: String,
           enddate: String,
           timeSlotsJson: String,
           instructions: String,
           dosageStrength: Int,
           frequency: String,
           dosageType: String?,
           medicineId: Int
    ) {
        viewModelScope.launch {
            try {val formattedStartDate = convertDateFormat(startdate)
                val formattedEndDate = convertDateFormat(enddate)
                val queryParams = mapOf(
                    "pid" to PrefsManager().getPatient()?.id.toString(),
                    "medicineId" to medicineId,
                    "dosageType" to dosageType,
                    "dosageStrength" to dosageStrength,
                    "frequency" to frequency,
                    "instructions" to instructions,
                    "timeSlotsJson" to timeSlotsJson,
                    "startdate" to formattedStartDate,
                    "enddate" to formattedEndDate,
                    "clientId" to PrefsManager().getPatient()?.clientId.toString(),
                )
//                {
//                    "pid": 89,
//                    "medicineId": 30,
//                    "dosageType": "Capsule",
//                    "dosageStrength": 250,
//                    "frequency": "Every day",
//                    "instructions": "After having milk",
//                    "timeSlotsJson": '[{"timeSlot":"09:30"}]',
//                    "startdate": "2025-11-04",
//                    "enddate": "2025-11-06",
//                    "clientId": 194
//                }
                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true,  )
                    .dynamicRawPost(
                        url = "api/EmployeeMedicineIntake/InsertEmployeeMedicineIntake",
                        body = queryParams as Map<String, @JvmSuppressWildcards Any>
                    )

                if (response.isSuccessful) {


                }else {
                }

            } catch (e: Exception) {
                Log.d("ITEM", e.toString())
            }
        }
    }
fun convertDateFormat(date: String): String {
        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = inputFormat.parse(date)
        return outputFormat.format(parsedDate!!)
    }
}