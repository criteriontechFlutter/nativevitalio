package com.criterion.nativevitalio.model
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


data class ChatMessage(
    val message: String,
    val time: String,
    val isUser: Boolean
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




