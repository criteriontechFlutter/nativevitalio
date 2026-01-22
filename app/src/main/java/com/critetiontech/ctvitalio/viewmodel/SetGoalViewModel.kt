package com.critetiontech.ctvitalio.viewmodel

import PrefsManager
import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ToastUtils
import kotlinx.coroutines.launch
import org.json.JSONObject

class SetGoalViewModel (application: Application) : BaseViewModel(application) {

    var selectedDays: MutableSet<Int> = mutableSetOf()
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage
    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> = _updateSuccess
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

fun updateUserData(
    requireContext: Context,
    categoryId: String,
    goalId: String,
    targetValue: String,
    unit: String,
    vmId: String?
) {
    _loading.value = true

    viewModelScope.launch {
        try {
            val patient = PrefsManager().getPatient() ?: return@launch

            /** ---------------- GOALWEEKS JSON ---------------- **/
            val goalWeeksJson = selectedDays.joinToString(
                prefix = "[",
                postfix = "]"
            ) { """{"dayId":${it + 1}}""" }

            /** ---------------- PARAMS ---------------- **/
            val params = mapOf(
                "pid" to patient.id.toString(),
                "vmId" to vmId.toString(),
                "userId" to patient.userId,
                "clientId" to "194",
                "categoryId" to categoryId,
                "goalId" to goalId,
                "targetValue" to targetValue,
                "unit" to unit,
                "goalWeeksJson" to goalWeeksJson
            )

            /** ---------------- API CALL ---------------- **/
            val response = RetrofitInstance
                .createApiService(includeAuthHeader = true)
                .dynamicRawPost(
                    url = "api/EmployeeGoals/InsertEmployeeGoals",
                    body = params
                )

            /** ---------------- HANDLE RESPONSE ---------------- **/

            val raw = response.body()?.string()
                ?: response.errorBody()?.string()
                ?: ""

            val json = JSONObject(raw)
            val status = json.optInt("status")
            val message = json.optString("responseValue")

            if (status == 1) {
                // BUSINESS SUCCESS
                ToastUtils.showSuccessPopup(requireContext, message)
                _updateSuccess.postValue(true)
            } else {
                // BUSINESS FAILURE ("Goal already exists !!" etc.)
                ToastUtils.showFailure(requireContext, message)
                _updateSuccess.postValue(false)
            }
        }
        catch (e: Exception) {
            _errorMessage.postValue(e.message ?: "Unknown error")
        }
        finally {
            _loading.postValue(false)
        }
    }
}
}