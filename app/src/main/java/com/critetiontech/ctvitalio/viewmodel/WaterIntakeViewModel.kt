package com.critetiontech.ctvitalio.viewmodel

import PrefsManager
import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.adapter.WaterRecord
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPointCorporateModule
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

data class FluidChartData(
    val date: String,     // "2025-11-11"
    val qty: Float        // 300f
)
class WaterIntakeViewModel(application: Application) : AndroidViewModel(application) {


    private val _totalWaterQty = MutableLiveData<String>()
    val atotalWaterQty: LiveData<String> = _totalWaterQty

    private val _activityInsertSuccess = MutableLiveData<Boolean>()
    val activityInsertSuccess: LiveData<Boolean> = _activityInsertSuccess
    private val _dailyRecords = MutableLiveData<List<WaterRecord>>()
    val dailyRecords: LiveData<List<WaterRecord>> = _dailyRecords
    private val _chartRecords = MutableLiveData<List<FluidChartData>>()
    val chartRecords: LiveData<List<FluidChartData>> = _chartRecords

    @RequiresApi(Build.VERSION_CODES.O)
    fun fluidIntake(givenFoodQuantity: String) {
        viewModelScope.launch {
            try {
                 val currentDate : String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Date())
                val currentTime: String = SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(Date())

                val user = PrefsManager().getPatient()
                val body = mapOf(
                    "pid" to user?.id.toString(),
                    "clientId" to  user?.clientId.toString(),
                    "intakeDate" to currentDate,
                    "intakeTime" to currentTime,
                    "fluidType" to "water",
                    "quantity" to givenFoodQuantity,
                    "remarks" to " Feeling Thristy",
                )

                val response = RetrofitInstance
                    .createApiService()
                    .dynamicRawPost(
                        url = "api/EmployeeFluidIntake/InsertEmployeeFluidIntake",
                        body = body
                    )


                if (response.isSuccessful) {
                    GetDailyEmployeeFluidIntake()
                    GetEmployeeMedicineIntakeByDate()

                } else {
                 }

            } catch (e: Exception) {
                 e.printStackTrace()
            }
        }
    }
    fun GetDailyEmployeeFluidIntake( ) {
        viewModelScope.launch {
            try {
                val params = mapOf(
                    "pid" to PrefsManager().getPatient()?.id.toString(),
                    "clientId" to PrefsManager().getPatient()?.clientId.toString()
                )

                val response = RetrofitInstance.createApiService(includeAuthHeader = true)
                    .dynamicGet(
                        url = "/api/EmployeeFluidIntake/GetDailyEmployeeFluidIntake",
                        params = params
                    )

                if (response.isSuccessful) {
                    val json = response.body()?.string()

                    val jsonObj = JSONObject(json)
                    val arr = jsonObj.getJSONArray("responseValue")

                    val list = mutableListOf<WaterRecord>()
                    var totalWaterQty = 0   // 👈 total quantity

                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)

                        val id = obj.getDouble("id").toInt()
                        val amount = obj.getDouble("quantity").toInt()
                        val time = obj.getString("intakeTime")

                        totalWaterQty += amount   // 👈 add quantity

                        list.add(
                            WaterRecord(
                                amount = amount,
                                time = time,
                                id = id
                            )
                        )
                    }

                    _totalWaterQty.postValue(totalWaterQty.toString())
                    _dailyRecords.postValue(list)
                    _activityInsertSuccess.postValue(true) // Live update event success

                } else {
                    Log.e("INSERT_ACTIVITY", "FAILED → ${response.errorBody()?.string()}")
                    _activityInsertSuccess.postValue(false)
                }

            } catch (e: Exception) {
                Log.e("INSERT_ACTIVITY", e.localizedMessage.toString())
                _activityInsertSuccess.postValue(false)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun GetEmployeeMedicineIntakeByDate( ) {
        viewModelScope.launch {
            try {

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                val toDate = LocalDate.now()
                val fromDate = toDate.minusDays(7)

                val params = mapOf(
                    "pid" to PrefsManager().getPatient()?.id.toString(),
                    "clientId" to PrefsManager().getPatient()?.clientId.toString(),
                    "fromDate" to fromDate.format(formatter),
                    "toDate" to toDate.format(formatter)
                )

                val response = RetrofitInstance.createApiService(includeAuthHeader = true)
                    .dynamicGet(
                        url = "api/EmployeeFluidIntake/EmployeeFluidIntakeByDateRange",
                        params = params
                    )

                if (response.isSuccessful) {
                    val jsonStr = response.body()?.string()
                    val jsonObj = JSONObject(jsonStr)
                    val arr = jsonObj.getJSONArray("responseValue")




                    val list = mutableListOf<FluidChartData>()

                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)

                        val date = obj.getString("intakeDate")
                        val qty = obj.getDouble("totalQuantity").toFloat()

                        list.add(FluidChartData(date, qty))
                    }

                    _chartRecords.postValue(list)

                    _activityInsertSuccess.postValue(true) // Live update event success

                } else {
                    Log.e("INSERT_ACTIVITY", "FAILED → ${response.errorBody()?.string()}")
                    _activityInsertSuccess.postValue(false)
                }

            } catch (e: Exception) {
                Log.e("INSERT_ACTIVITY", e.localizedMessage.toString())
                _activityInsertSuccess.postValue(false)
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteEmployeeFluidIntake(id:String) {
        viewModelScope.launch {
            try {
                val params = mapOf(
                    "id" to id.toString(),
                )

                val response = RetrofitInstance.createApiService(includeAuthHeader = true)
                    .dynamicDelete(
                        url = "api/EmployeeFluidIntake/DeleteEmployeeFluidIntake",
                        params = params
                    )

                if (response.isSuccessful) {
                    GetDailyEmployeeFluidIntake( )
                    GetEmployeeMedicineIntakeByDate( )

                    _activityInsertSuccess.postValue(true) // Live update event success

                } else {
                    Log.e("INSERT_ACTIVITY", "FAILED → ${response.errorBody()?.string()}")
                    _activityInsertSuccess.postValue(false)
                }

            } catch (e: Exception) {
                Log.e("INSERT_ACTIVITY", e.localizedMessage.toString())
                _activityInsertSuccess.postValue(false)
            }
        }
    }
}