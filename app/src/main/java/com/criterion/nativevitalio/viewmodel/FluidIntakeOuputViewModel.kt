package com.critetiontech.ctvitalio.viewmodel

import DateUtils
import PrefsManager
import android.app.Application
import android.content.Context
import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.utils.ToastUtils
import com.critetiontech.ctvitalio.viewmodel.BaseViewModel
import com.critetiontech.ctvitalio.model.FluidOutput
import com.critetiontech.ctvitalio.model.FluidOutputResponse
import com.critetiontech.ctvitalio.model.FluidOutputSummary
import com.critetiontech.ctvitalio.model.FluidOutputSummaryResponse
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


    private val _selectFluidIntakeVolume = MutableLiveData(0.0)
    val selectFluidIntakeVolume: LiveData<Double> = _selectFluidIntakeVolume


    private val _progressOutput = MutableLiveData(25.0)
    val progressOutput: LiveData<Double> = _progressOutput

    private val _selectedColor = MutableLiveData("")
    val selectedColor: LiveData<String> = _selectedColor


    private val _selectedIntakeButton = MutableLiveData(false)
    val selectedIntakeButton: LiveData<Boolean> = _selectedIntakeButton


    //intake list
    private val _intakeList = MutableLiveData<List<ManualFoodItem>>()
    var intakeList: LiveData<List<ManualFoodItem>> = _intakeList


    //intake list
    private val _outputList = MutableLiveData<List<FluidOutput>>()
    var outputList: LiveData<List<FluidOutput>> = _outputList

    private val _intakeListRangeWise = MutableLiveData<List<FluidSummaryItem>>()
    var intakeListRangeWise: LiveData<List<FluidSummaryItem>> = _intakeListRangeWise



    private val _outputListRangeWise = MutableLiveData<List<FluidOutputSummary>>()
    var outputListRangeWise: LiveData<List<FluidOutputSummary>> = _outputListRangeWise


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

    fun setSelectedFluidIntakeVolume(item: Double) {
        _selectFluidIntakeVolume.value = item

    }

   fun setSelectedIntakeButton(item: Boolean){
        _selectedIntakeButton.value=item
    }


    fun setSelectedColor(item: String) {
        _selectedColor.value = item

    }
    fun setSelectedOutputProgress(progress:Double){
        _progressOutput.value=progress
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

                if (response.isSuccessful) {
                    _loading.value = false
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


    fun fetchFluidOutputDaily(uhid: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val queryParams = mapOf("UHID" to uhid)

                val response = RetrofitInstance
                    .createApiService7082()
                    .dynamicGet(
                        url = ApiEndPoint().getFluidOutputDaily,
                        params = queryParams
                    )

                if (response.isSuccessful) {
                    _loading.value = false
                    val responseBodyString = response.body()?.string()
                    val type = object : TypeToken<FluidOutputResponse>() {}.type
                    val parsed = Gson().fromJson<FluidOutputResponse>(responseBodyString, type)
                    val allItems = parsed.responseValue
                    _outputList.value= allItems


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

                if (response.isSuccessful) {
                    _loading.value = false
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



    fun fetchManualFluidOutPutByRange(uhid: String, fromDate: String, toDate: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val queryParams = mapOf(
                    "Uhid" to uhid,
                    "fromDate" to fromDate,
                    "toDate" to toDate
                )

                val response = RetrofitInstance
                    .createApiService7082()
                    .dynamicGet(
                        url = ApiEndPoint().getFluidOutPutDetailsByRange ,
                        params = queryParams
                    )

                if (response.isSuccessful) {
                    _loading.value = false
                    val responseBodyString = response.body()?.string()
                    val type = object : TypeToken<FluidOutputSummaryResponse>() {}.type
                    val parsed = Gson().fromJson<FluidOutputSummaryResponse>(responseBodyString, type)
                    val allItems = parsed.responseValue
                    _outputListRangeWise.value= allItems


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



    fun insertFluidOutPut(requireContext: Context) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val queryParams = mapOf(
                    "id" to  PrefsManager().getPatient()!!.userId,
                "pmID" to 0.toString(),
                "outputTypeID"  to 51.toString(),
                "quantity"  to progressOutput.value.toString(),
                "unitID"  to 1.toString(),
                "outputDate"  to DateUtils.getTodayDate(),
                "colour"  to selectedColor.value.toString(),
                "userID"  to PrefsManager().getPatient()!!.userId,
                "clientId"  to PrefsManager().getPatient()!!.clientId,
                "uhid"  to PrefsManager().getPatient()!!.uhID
                    )

                val response = RetrofitInstance
                    .createApiService7082()
                    .dynamicRawPost(
                        url = ApiEndPoint().savePatientOutput,
                        body = queryParams
                    )

                if (response.isSuccessful) {
                    _loading.value = false
                    ToastUtils.showSuccessPopup(requireContext, "Output Saved Successfully!")

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



    fun insertFluidIntake(requireContext: Context) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val queryParams = mapOf(
                    "givenQuanitityInGram" to '0',
                "uhid" to PrefsManager().getPatient()!!.uhID,
                "foodId" to _selectedFluid.value!!.foodID, // Required(fixed)
                "pmId" to '0',
                "givenFoodQuantity" to _selectFluidIntakeVolume.value!!.toDouble(), //Required
                "givenFoodDate" to DateUtils.getTodayDateTime(), //Required
                "givenFoodUnitID" to 27, // Required (fixed)
                // "recommendedUserID": userRepository.getUser.admitDoctorId.toString(),
                "recommendedUserID" to 0,
                "jsonData" to "",
                "fromDate" to DateUtils.getTodayDateTime(),
                "isGiven" to '0',
                "entryType" to "N", // Required (fixed)
                "isFrom" to '0',
                "dietID" to '0',
                "userID" to PrefsManager().getPatient()!!.userId,
                )

                val response = RetrofitInstance
                    .createApiService7096()
                    .dynamicRawPost(
                        url = ApiEndPoint().savePatientIntake,
                        body = queryParams
                    )

                if (response.isSuccessful) {
                    _loading.value = false
                    ToastUtils.showSuccessPopup(requireContext, "Intake Saved Successfully!")
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