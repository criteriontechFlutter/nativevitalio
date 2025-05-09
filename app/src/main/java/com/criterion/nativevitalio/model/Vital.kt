class Vital {
    var uhid: String? = null
    var pmId = 0
    var vitalID = 0
    var vitalName: String? = null
    var vitalValue = 0.0
    var unit: String? = null
    var vitalDateTime: String? = null
    var userId = 0
    var rowId = 0
}

data class VitalsResponse(
    val status: Int,
    val message: String? = null,
    val responseValue: VitalResponseValue
)


data class VitalResponseValue(
    val lastVital: List<Vital>,
    val allVitalAvg: List<AllVitalAvg>
)


data class AllVitalAvg(
    val vmId: Int,
    val pmId: Int,
    val avgVmValue: Double
)

