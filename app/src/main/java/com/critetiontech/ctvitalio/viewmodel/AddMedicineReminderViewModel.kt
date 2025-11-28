package com.critetiontech.ctvitalio.viewmodel

import Medicine
import MedicineResponse
import PrefsManager
import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.model.EmployeeActivityResponse
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPointCorporateModule
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale


class AddMedicineReminderViewModel(application: Application) : AndroidViewModel(application) {

    /** ---------------- Live Data ---------------- **/
    val selectedMedicine = MutableLiveData<Medicine>()
    val selectedMedicineName = MutableLiveData<String>()
    val medicineLiveData = MutableLiveData<List<Medicine>>()
    var selectedFrequency = ""
    fun setFrequency(type:String){ selectedFrequency = type }

    var everyXDayValue = 1
    fun setEveryXDay(value:Int){ everyXDayValue = value }
    fun updateSelectedMedicine(model: Medicine) {
        selectedMedicine.value = model   // ← instant update
    }
    /** --------------- Frequency Objects --------------- **/
    var everyXday = 1
    fun updateEveryXDay(value:Int){ everyXday = value }

    val selectedWeekDays = mutableListOf<String>()   // ["1","4"]
    val selectedMonthDays = mutableListOf<String>()  // ["2nd","4th"]

    /** --------------- Time Slot List --------------- **/
    val timeSlots = mutableListOf<String>() // ["08:00","14:00"]
    fun addTimeSlot(time:String){ timeSlots.add(time) }
    fun updateTimeSlot(i:Int,new:String){ if(i in timeSlots.indices) timeSlots[i] = new }
    fun removeTimeSlot(i:Int){ if(i in timeSlots.indices) timeSlots.removeAt(i) }

    /** 🏥 Fetch Medicine List */
    fun getBrandList(search:String){
        viewModelScope.launch {
            try {
                val res = RetrofitInstance.createApiService("http://182.156.200.177:5082/")
                    .dynamicGet("api/KnowMedApis/GetBrandList", params = mapOf("alphabet" to search))

                if(res.isSuccessful){
                    val data = Gson().fromJson(res.body()?.string(),MedicineResponse::class.java)
                    medicineLiveData.postValue(data.responseValue)
                }
            } catch (e:Exception){ Log.e("MEDICINE_ERROR",e.toString()) }
        }
    }

    /** 🔥 Build Final Body & POST */
    fun addMedicineFinal(startDate: String, endDate: String, instructions: String) {

        val body = mutableMapOf<String, Any>(
            "pid"            to PrefsManager().getPatient()?.id.toString(),
            "clientId"       to PrefsManager().getPatient()?.clientId.toString(),
            "medicineId"     to selectedMedicine.value!!.id,
            "dosageType"     to selectedMedicine.value!!.dosageFormName,
            "dosageStrength" to selectedMedicine.value!!.doseStrength,
            "instructions"   to instructions,
            "startdate"      to convert(startDate),
            "enddate"        to convert(endDate),

            // common key stored for all expect As Needed
            "timeSlotsJson"  to buildTimeSlotJson()
        )

        when (selectedFrequency) {

            /** 🔵 EVERY DAY */
            "Every x day" -> {
                body["frequency"] = "Every x day"
                body["frequencyValue"] = everyXDayValue   // <----- FINAL ANSWER
                body["monthDates"] = ""
                body["weekDays"] = "[]"
            }

            /** 🟢 EVERY X DAY */
            "Every x day" -> {
                body["frequency"] = "Every x day"
                body["frequencyValue"] = everyXDayValue
                body["monthDates"] = ""
                body["weekDays"] = "[]"
            }

            /** 🟡 WEEKLY */
            "Every week" -> {
                body["frequency"] = "Every week"
                body["weekDays"] = selectedWeekDays.toString()  // ["1","4"]
                body["frequencyValue"] = everyXDayValue.toString()
                body["monthDates"] = ""
            }

            /** 🟣 MONTHLY */
            "Every month" -> {
                body["frequency"] = "Every month"
                body["monthDates"] = selectedMonthDays.joinToString(", ")   // → "2nd, 4th"
                body["weekDays"] = "[]"
                body["frequencyValue"] = everyXDayValue.toString()
            }

            /** ⚪ AS NEEDED */
            "As Needed" -> {
                body["frequency"] = "As Needed"
                body.remove("timeSlotsJson") // no time needed here
                body["monthDates"] = ""
                body["weekDays"] = "[]"
            }
        }

        postMedicine(body)
    }

    private fun postMedicine(body: Map<String, Any>) {
        viewModelScope.launch {
            try {
                val res = RetrofitInstance.createApiService()
                    .dynamicRawPost("api/EmployeeMedicineIntake/InsertEmployeeMedicineIntake", body = body)

                val responseText = res.body()?.string() ?: ""

                Log.e("FINAL_RESPONSE", responseText)

                if(res.isSuccessful){
                    resetAllFields()   // << CLEAR ALL ON SUCCESS
                }

            } catch (e: Exception) {
                Log.e("POST_ERROR", e.toString())
            }
        }
    }
    fun resetAllFields() {
        selectedMedicine.value = null
        selectedMedicineName.value = ""
        selectedFrequency = ""

        everyXDayValue = 1
        everyXday = 1

        selectedWeekDays.clear()
        selectedMonthDays.clear()

        timeSlots.clear()

        Log.e("RESET","All input fields cleared successfully!")
    }

    private fun convert(d:String):String =
        SimpleDateFormat("yyyy-MM-dd",Locale.getDefault())
            .format(SimpleDateFormat("dd/MM/yyyy",Locale.getDefault()).parse(d)!!)

    fun buildTimeSlotJson() =
        timeSlots.joinToString(prefix="[", postfix="]") { """{"timeSlot":"$it"}""" }

    fun addSpecificDate(d:String){ if(d !in selectedMonthDays) selectedMonthDays.add(d) }
    fun setWeekDay(id:String){ if(id !in selectedWeekDays) selectedWeekDays.add(id) }
}
