// Main Response
data class VitalsResponse(
    val status: Int,
    val message: String? = null,
    val responseValue: VitalResponseValue
)

// ResponseValue object
data class VitalResponseValue(
    val lastVital: List<Vital>,
    val allVitalAvg: List<AllVitalAvg>,
    val quickMetric: List<QuickMetric>,
   // val sleepmetrics: SleepMetrics? = null
)

// Vital object
data class Vital(
    val uhid: String? = null,
    val pmId: Int = 0,
    val vitalID: Int = 0,
    var vitalName: String? = null,
    var vitalValue: Double = 0.0,
    var unit: String? = null,
    var vitalDateTime: String? = null,
    val userId: Int = 0,
    val rowId: Int = 0
)
data class QuickMetric(
    val Title: String,
    val DisplayText: String,
    val Unit: String?,
    val Value: Value,
    val Type: String
)

data class Value(
    val ValueKind: Int
)
// Avg values
data class AllVitalAvg(
    val vmId: Int,
    val pmId: Int,
    val avgVmValue: Double
)

// Sleep metrics
data class SleepMetrics(
    val score: Int,
    val scoreQuality: String,
    val timeInBed: String,
    val totalSleepTime: String,
    val totalDeepSleep: String,
    val totalLightSleep: String,
    val totalRemSleep: String,
    val totalAwakeTime: String,
    val efficiency: Int,
    val avgHr: Int,
    val avgHrv: Int,
    val sleepGraph: List<SleepGraphItem>,
    val movementGraph: List<MovementGraphItem>,
    val hrGraph: List<GraphPoint>,
    val hrvGraph: List<GraphPoint>,
    val summary: SleepSummary
)

// Graphs
data class SleepGraphItem(
    val start: String,
    val end: String,
    val stage: String
)

data class MovementGraphItem(
    val time: String,
    val movement: String
)

data class GraphPoint(
    val time: String,
    val value: Int
)

// Sleep Summary
data class SleepSummary(
    val efficiency: String,
    val restfulness: String,
    val consistency: String,
    val interruptions: String,
    val sleepStages: String,
    val overall: String
)
