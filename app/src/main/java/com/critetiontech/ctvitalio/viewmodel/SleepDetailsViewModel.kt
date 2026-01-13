package com.critetiontech.ctvitalio.viewmodel

import DailyCheckItem
import DailyCheckListWrapper
import InsightJson
import PillReminderModel
import PrefsManager
import QuickMetric
import QuickMetricsTiled
import SleepValue
import Summary
import Vital
import VitalInsight
import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.Database.appDatabase.AppDatabase
import com.critetiontech.ctvitalio.adapter.PriorityAction
import com.critetiontech.ctvitalio.adapter.PriorityActionWrapper
import com.critetiontech.ctvitalio.model.DietItemModel
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import kotlin.collections.firstOrNull
import kotlin.collections.isNullOrEmpty
import kotlin.collections.orEmpty
import kotlin.jvm.java
import kotlin.let
import kotlin.text.startsWith
import kotlin.text.trim
import kotlin.to
import kotlin.toString

class SleepDetailsViewModel(application: Application) : BaseViewModel(application) {




    private val _vitalList = MutableLiveData<List<Vital>>()
    val vitalList: LiveData<List<Vital>> get() = _vitalList
    private val dao = AppDatabase.getDB(application).vitalsDao()
    private val _quickMetricList = MutableLiveData<List<QuickMetric>>()
    val  quickMetricListList: LiveData<List<QuickMetric>> get() = _quickMetricList
    private val _dietList = MutableLiveData<List<DietItemModel>>()
    val dietList: LiveData<List<DietItemModel>> get() = _dietList
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _pillList = MutableLiveData<List<PillReminderModel>>()
    val pillList: LiveData<List<PillReminderModel>> get() = _pillList


    private val _webSocketStatus = MutableLiveData<WebSocketState>()
    val webSocketStatus: LiveData<WebSocketState> get() = _webSocketStatus

    fun setWebSocketState(state: WebSocketState) {
        _webSocketStatus.postValue(state)
    }


    private val _sleepValueList = MutableLiveData<SleepValue>()
    val sleepValueList: LiveData<SleepValue> get() = _sleepValueList

    private val _vitalInsights = MutableLiveData<List<VitalInsight>?>()
    val vitalInsights: LiveData<List<VitalInsight>?> get() = _vitalInsights


    private val _sleepsummary = MutableLiveData<List<Summary>?>()
    val  sleepsummary: LiveData<List<Summary>?> get() = _sleepsummary
    private val _quickMetricsTiledList = MutableLiveData<List<QuickMetricsTiled>>()
    val quickMetricsTiledList: LiveData<List<QuickMetricsTiled>> = _quickMetricsTiledList
    private val _priorityAction = MutableLiveData<List<PriorityAction>?>()
    val  priorityAction: LiveData<List<PriorityAction>?> get() = _priorityAction
    private val _dailyCheckList = MutableLiveData<List<DailyCheckItem>>()
    val dailyCheckList: LiveData<List<DailyCheckItem>> = _dailyCheckList
    private val _insightWrapperList = MutableLiveData< InsightJson? >()
    val insightWrapperList: MutableLiveData<InsightJson?> =  _insightWrapperList
    fun getVitals(data: String? = "") {
        viewModelScope.launch {
            _loading.value = true

            Log.d("GIST_DATAdatadata", data. toString())

            // 1️⃣ Load from local first
//            val localVitals = loadVitalsFromLocal()
//            localVitals?.let { entity ->
//                loadVitalsFromLocal(entity)
//            }

            try {
                val queryParams = mapOf(
                    "pid" to PrefsManager().getPatient()?.id.toString(),
                    "clientId" to 194,
                )

                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true)
                    .dynamicGet(
                        url = ApiEndPoint().getPatientLastVital,
                        params = queryParams
                    )

                if (response.isSuccessful) {
                    val json = response.body()?.string()
                    val parsed = Gson().fromJson(json, VitalsResponse::class.java)
                    // Update UI from API
                    _vitalList.value = parsed.responseValue.lastVital
//                    _vitalInsights.value = parsed.responseValue.vitalInsights
                    val decoded = decodePriorityAction(parsed.responseValue.priorityAction)
                    _priorityAction.value = decoded                // Store locally 2️⃣ SAVE API DATA INTO ROOM DB
//                    saveVitalsToLocal(parsed.responseValue)
                    _dailyCheckList.value = decodeDailyCheckList(parsed.responseValue.dailyCheckList)
                    val jsonString = parsed.responseValue.vitalInsights
                        ?.firstOrNull()
                        ?.insightJson
                    // stop if nothing found

                    val decodedInsight = jsonString?.let { decodeInsightJson(it) }

                    if (decodedInsight != null) {
                        _insightWrapperList.value = decodedInsight
                    }
                    val sleepMetric243 = parsed.responseValue.sleepmetrics
                        ?.firstOrNull { it.vitalID == 243 }
                        ?.vitalValue

                    sleepMetric243?.let { vitalValueJson ->
                        try {
                            val sleepValue: SleepValue = if (vitalValueJson.trim().startsWith("{")) {
                                // Already a JSON object
                                Gson().fromJson(vitalValueJson, SleepValue::class.java)
                            } else {
                                // Double-encoded JSON string
                                val unescapedJson = Gson().fromJson(vitalValueJson, String::class.java)
                                Gson().fromJson(unescapedJson, SleepValue::class.java)
                            }
                            printListData()
                            _sleepValueList.value = sleepValue
                            _quickMetricList.value = sleepValue.QuickMetrics.orEmpty()
                            _quickMetricsTiledList.value = sleepValue.QuickMetricsTiled.orEmpty()
                            _sleepsummary.value = sleepValue.Summary.orEmpty()

                     // Log decoded data

                        } catch (e: Exception) {
                            Log.e("SleepDetailsVM", "Failed to decode SleepValue: ${e.message}")
                        }
                    }

                    _loading.value = false
                } else {
                    _loading.value = false
                    _errorMessage.value = "Error Code: ${response.code()}"
                }

            } catch (e: Exception) {
                _loading.value = false
                _errorMessage.value = e.message ?: "Unexpected error"
            }
        }
    }
    fun decodeInsightJson(jsonString: String): InsightJson {
        val gson = Gson()
        return gson.fromJson(jsonString, InsightJson::class.java)
    }  fun printListData() {
        Log.d("DEBUG_VITALS", "Vitals: ${_vitalList.value}")
        Log.d("DEBUG_QUICK_METRICS", "Quick Metrics: ${_quickMetricList.value}")
        Log.d("DEBUG_QUICK_METRICS_TILED", "Quick Metrics Tiled: ${_quickMetricsTiledList.value}")
        Log.d("DEBUG_SLEEP_VALUE", "Sleep Value: ${_sleepValueList.value}")
        Log.d("DEBUG_SLEEP_SUMMARY", "Sleep Summary: ${_sleepsummary.value}")
        Log.d("DEBUG_PRIORITY_ACTION", "Priority Actions: ${_priorityAction.value}")
        Log.d("DEBUG_DAILY_CHECKLIST", "Daily Checklist: ${_dailyCheckList.value}")
        Log.d("DEBUG_INSIGHT_WRAPPER", "Insight Wrapper: ${_insightWrapperList.value}")
    }
 fun decodeDailyCheckList(wrapperList: List<DailyCheckListWrapper>?): List<DailyCheckItem> {
        if (wrapperList.isNullOrEmpty()) return emptyList()

        val gson = Gson()
        val listType = object : TypeToken<List<DailyCheckItem>>() {}.type

        val jsonString = wrapperList[0].dailyChecklist
        return gson.fromJson(jsonString, listType)
    }
    fun decodePriorityAction(wrapperList: List<PriorityActionWrapper>?): List<PriorityAction> {
        if (wrapperList.isNullOrEmpty()) return emptyList()

        val gson = Gson()
        val listType = object : TypeToken<List<PriorityAction>>() {}.type

        // The backend ALWAYS sends a STRING containing a JSON array
        val jsonString = wrapperList[0].actions

        return gson.fromJson(jsonString, listType)
    }
}