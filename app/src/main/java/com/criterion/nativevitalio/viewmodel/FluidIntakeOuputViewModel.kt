package com.critetiontech.ctvitalio.viewmodel

import android.app.Application
import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.criterion.nativevitalio.viewmodel.BaseViewModel
import com.critetiontech.ctvitalio.model.FluidSummaryItem
import com.critetiontech.ctvitalio.model.FluidSummaryResponse
import com.critetiontech.ctvitalio.model.FluidType
import com.critetiontech.ctvitalio.model.GlassSize
import com.critetiontech.ctvitalio.model.ManualFoodAssignResponse
import com.critetiontech.ctvitalio.model.ManualFoodItem
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class FluidIntakeOuputViewModel (application: Application) : BaseViewModel(application) {

    //recommended Variable
    private val _recommended = MutableLiveData(5000)
    val recommended: LiveData<Int> = _recommended


    //intake list
    private val _intakeList = MutableLiveData<List<ManualFoodItem>>()
    var intakeList: LiveData<List<ManualFoodItem>> = _intakeList


    private val _intakeListRangeWise = MutableLiveData<List<FluidSummaryItem>>()
    var intakeListRangeWise: LiveData<List<FluidSummaryItem>> = _intakeListRangeWise



    //fluid list to show progressBar
    private val _fluidList = MutableLiveData<List<FluidType>>()
    val fluidList: LiveData<List<FluidType>> = _fluidList

    //calculating total fluid intake
    val totalIntake: LiveData<Int> = fluidList.map { it.sumOf { it.amount } }

    //calculate remaining fluid intake
    val remaining: LiveData<Int> = fluidList.map {
        _recommended.value?.minus(it.sumOf { fluid -> fluid.amount }) ?: 0
    }


    //variable to store selected Fluid Preference
    private val _selectedFluid = MutableLiveData<ManualFoodItem?>()
    val selectedFluid: LiveData<ManualFoodItem?> = _selectedFluid


    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage


    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading



    private val _glassSizeList = MutableLiveData<List<GlassSize>>()
    val glassSizeList: LiveData<List<GlassSize>> = _glassSizeList

    private val _selectedVolume = MutableLiveData<Int>()
    val selectedVolume: LiveData<Int> = _selectedVolume



    fun setSelectedFluid(item: ManualFoodItem) {
        _selectedFluid.value = item

    }


    private val _customFluidAmount = MutableLiveData<Int>()
    val customFluidAmount: LiveData<Int> = _customFluidAmount

//    fun setFluidList(list: List<T>) {
//        _fluidList.value = list
//    }

    init {
        setDefaultSizes()
    }



    fun setCustomAmount(value: Int) {
        _customFluidAmount.value = value
    }

    private fun setDefaultSizes() {
        val sizes = listOf(
            GlassSize(0 ),
             GlassSize(150,isSelected = true),
            GlassSize(250),
            GlassSize(300),
            GlassSize(400)
        )
        _glassSizeList.value = sizes
        _selectedVolume.value = 0
    }

    fun setSelectedGlassSize(volume: Int) {
        _selectedVolume.value = volume
        _glassSizeList.value = _glassSizeList.value?.map {
            it.copy(isSelected = it.volume == volume)
        }
    }


    fun fetchManualFluidIntake(uhid: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val queryParams = mapOf("Uhid" to uhid, "intervalTimeInHour" to 24)

                val response = RetrofitInstance
                    .createApiService7096()
                    .dynamicGet(
                        url = ApiEndPoint().getFluidIntakeDetails,
                        params = queryParams
                    )
                _loading.value = false
                if (response.isSuccessful) {
                    val responseBodyString = response.body()?.string()
                    val type = object : TypeToken<ManualFoodAssignResponse>() {}.type
                    val parsed = Gson().fromJson<ManualFoodAssignResponse>(responseBodyString, type)
                    val allItems = parsed.responseValue
                    _intakeList.value= allItems
                    val filteredList = parsed.responseValue.mapNotNull {

                        Log.d("TAG", "fetchManualFluidIntake: "+intakeList.value)
                        val qty = it.quantity.toFloatOrNull() ?: 0f
                        if (qty > 0f) {
                            FluidType(
                                name = it.foodName.trim(),
                                amount = qty.toInt(),
                                color = mapColorForFood(it.foodName) ,
                                id= it.foodID
                            )
                        } else null
                    }
                    _fluidList.value = filteredList

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



    fun fetchManualFluidIntakeByRange(uhid: String, fromDate: String, toDate: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val queryParams = mapOf(
                    "Uhid" to uhid,
                    "fromDate" to fromDate,
                    "toDate" to toDate
                )

                val response = RetrofitInstance
                    .createApiService7096()
                    .dynamicGet(
                        url = ApiEndPoint().getFluidIntakeDetailsByRange,
                        params = queryParams
                    )
                _loading.value = false
                if (response.isSuccessful) {
                    val responseBodyString = response.body()?.string()
                    val type = object : TypeToken<FluidSummaryResponse>() {}.type
                    val parsed = Gson().fromJson<FluidSummaryResponse>(responseBodyString, type)
                    val allItems = parsed.responseValue
                    _intakeListRangeWise.value= allItems
//                    val filteredList = parsed.responseValue.mapNotNull {
//
//                        Log.d("TAG", "fetchManualFluidIntake: "+intakeList.value)
//                        val qty = it.quantity.toFloatOrNull() ?: 0f
//                        if (qty > 0f) {
//                            FluidType(
//                                name = it.foodName.trim(),
//                                amount = qty.toInt(),
//                                color = mapColorForFood(it.foodName) ,
//                                id= it.foodID
//                            )
//                        } else null
//                    }
                  //  _fluidList.value = filteredList

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

    private fun mapColorForFood(name: String): Int {
        return when (name.trim().lowercase()) {
            "milk" -> Color.parseColor("#FFEB3B")
            "water" -> Color.parseColor("#4FC3F7")
            "green tea", "tea" -> Color.parseColor("#A1887F")
            "coffee" -> Color.parseColor("#795548")
            "fruit juice", "juice" -> Color.parseColor("#FF9800")
            else -> Color.LTGRAY
        }}


}