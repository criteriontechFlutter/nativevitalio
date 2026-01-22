package com.critetiontech.ctvitalio.viewmodel

import PrefsManager
import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.model.HapticType
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.critetiontech.ctvitalio.utils.ToastUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import sealedClass.UiEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConnectionViewModel (application: Application) : BaseViewModel(application) {

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading
    fun insertPatientVital(
        navController: NavController,
        requireContext:Context,
        BPSys: String?= "0",
        BPDias: String?= "0",
        rr: String? = "0",
        spo2: String? = "0",
        pr: String? ="0",
        tmp: String? = "0",
        hr: String? ="0",
        weight: String? ="0",
        rbs: String? = "0",
        glucose: String? = "0",
        positionId:  String? = "0",
    ) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val queryParams = mapOf(
                    "userId" to PrefsManager().getPatient()!!.pid,
                    "vmValueBPSys" to BPSys.toString(),
                    "vmValueBPDias" to BPDias.toString(),
                    "vmValueRespiratoryRate" to rr.toString(),
                    "vmValueSPO2" to spo2.toString(),
                    "vmValuePulse" to pr.toString(),
                    "vmValueTemperature" to tmp.toString(),
                    "vmValueHeartRate" to hr.toString(),
                    "weight" to weight.toString(),
                    "vmValueRbs" to rbs.toString(),
                    "vmValueGlucose" to glucose.toString(),
                    "vitalTime" to SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                    "vitalDate" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                    "uhid" to PrefsManager().getPatient()?.empId.toString(),
                    "currentDate" to  SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                    "clientId" to PrefsManager().getPatient()?.clientId.toString(),
                    "isFromPatient" to true,
                    "positionId" to positionId.toString()
                )

                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true)
                    .dynamicRawPost(
                        url = ApiEndPoint().insertPatientVital,
                        body = queryParams
                    )



                if (response.isSuccessful) {
                    _loading.value = false
                    _events.emit(
                        UiEvent.ShowBottomSheet(
                            icon = R.raw.ic_success_anim,
                            title = "Glucose Logged Successfully",
                            message = "Your latest reading has been recorded.",
                            buttonText = "OK",
                            hapticType = HapticType.LIGHT
                        ))
//                    ToastUtils.showSuccessPopup(requireContext,"Vital Added Successfully!")
//                    navController.popBackStack()

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

    fun insertPatientVitalFromDevice(
        BPSys: String? = "0",
        BPDias: String? = "0",
        rr: String? = "0",
        spo2: String? = "0",
        pr: String? = "0",
        tmp: String? = "0",
        hr: String? = "0",
        weight: String? = "0",
        rbs: String? = "0",
        positionId: String? = "0",
    ): Boolean {
        var isSuccess = false

        runBlocking {  // Or use suspend function and call from coroutine scope
            try {
                val queryParams = mapOf(
                    "userId" to PrefsManager().getPatient()!!.pid,
                    "vmValueBPSys" to BPSys.toString(),
                    "vmValueBPDias" to BPDias.toString(),
                    "vmValueRespiratoryRate" to rr.toString(),
                    "vmValueSPO2" to spo2.toString(),
                    "vmValuePulse" to pr.toString(),
                    "vmValueTemperature" to tmp.toString(),
                    "vmValueHeartRate" to hr.toString(),
                    "weight" to weight.toString(),
                    "vmValueRbs" to rbs.toString(),
                    "vitalTime" to SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                    "vitalDate" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                    "uhid" to PrefsManager().getPatient()?.empId.toString(),
                    "currentDate" to SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                    "clientId" to PrefsManager().getPatient()?.clientId.toString(),
                    "isFromPatient" to true,
                    "positionId" to positionId.toString()
                )

                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true)
                    .dynamicRawPost(
                        url = ApiEndPoint().insertPatientVital,
                        body = queryParams
                    )

                isSuccess = response.isSuccessful
            } catch (e: Exception) {
                e.printStackTrace()
                isSuccess = false
            } finally {

            }
        }

        return isSuccess
    }
}