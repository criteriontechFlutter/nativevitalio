package com.critetiontech.ctvitalio.model

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class Movies(
    val page: Int,
    val results: List<Result>,
    val total_pages: Int,
    val total_results: Int
)


data class FluidType(val name: String, val amount: Int, val color: Int,val id: Int,)



data class ManualFoodAssignResponse(
    val status: Int,
    val message: String,
    val responseValue: List<ManualFoodItem>
)


data class FluidOutputResponse(
    val status: Int,
    val message: String,
    val responseValue: List<FluidOutput>
)


data class FluidOutput(
    val id: Int,
    val pmID: Int,
    val outputID: Int,
    val quantity: Double,
    val unitID: Int,
    val outputDate: String,
    val userID: Int,
    val userName: String,
    val outputType: String,
    val unitName: String,
    val outputDateFormat: String,
    val outputTimeFormat: String,
    val colour: String
)

data class ManualFoodItem(
    val foodID: Int,
    val foodName: String,
    val quantity: String= 0.0.toString(),
    val givenFoodDate: String
)



data class GlassSize(
    val volume: Int, // e.g. 150
    var isSelected: Boolean = false
)


data class FluidPoint(
    val time: Float,     // in hours (e.g., 5.5 for 5:30 AM)
    val amount: Float,   // in ml
    val colorHex: String // color per fluid type (e.g., #3DA5F5)
)


data class FluidPointGraph(
    val timeHour: Float,     // e.g., 8.0 for 8:00 AM
    val quantity: Float,     // in ml
    val colorHex: String,    // "#FF0000" etc.
    val label: String        // e.g., "Milk"
)


data class FluidSummaryResponse(
    val status: Int,
    val message: String,
    val responseValue: List<FluidSummaryItem>
)

data class FluidSummaryItem(
    val foodQuantity: String,
    val foodId: Int,
    val givenFoodDate: String,
    val assignedLimit: Double
)





data class Vital(
    val type: String,
    val value: String,
    val timestamp: String
)



data class BloodPressureReading(
    val time: String,  // e.g. "02:26 PM"
    val sys: Int,
    val dia: Int,
    val bp: String     // "120/80 mmHg"
)


data class VitalResponse(
    val status: Int,
    val message: String,
    val responseValue: BloodPressureHistory
)


data class BloodPressureHistory(
    val patientGraph: List<PatientGraph>,
    val patientVital: List<PatientVital>,
    val vitalsDate: List<VitalsDate>
)


data class PatientGraph(
    val vitalDateTime: String,
    val vitalDetails: String // This is a raw JSON string that you'll parse separately
)

data class VitalDetail(
    val vitalid: Int,
    val vitalName: String,
    val vitalValue: Double,
    val vitaldate: String
)


data class PatientVital(
    val id: Int,
    val vitalName: String,
    val vitalIcon: String
)

data class VitalsDate(
    val vitalDate: String
)


data class ApiGenericResponse(
    val status: Int,
    val message: String,
    @SerializedName("data")
    val rawPayload: JsonObject? = null
)



data class FluidOutputSummaryResponse(
    val status: Int,
    val message: String,
    val responseValue: List<FluidOutputSummary>
)

data class FluidOutputSummary(
    val pmID: Int,
    val quantity: Double,
    val repetition: Int,
    val unitID: Int,
    val unitName: String,
    val outputDate: String
)



data class WatchModel(
    val brand: String,
    val model: String,
    val watch: Int,

    )



data class ChatResponse(
    val status: Int,
    val message: String,
     val responseValue: MutableList<ChatMessage>?
)

data class ChatMessage(
    val id: Int,
    val groupId: Int,
    val sendFrom: Int,
    val sendTo: Int,
    val message: String,
    val fileUrl: String,
    val fileName: String,
    val isOnline: Int,
    val fileLength: String,
    val messageDay: String,
    val chatTime: String,
    val chatDate: String,
    val sendFromName: String?,  // Nullable
    val sendToName: String?,    // Nullable
    val isPatient: Boolean,
    val messageDateTime: String
)







