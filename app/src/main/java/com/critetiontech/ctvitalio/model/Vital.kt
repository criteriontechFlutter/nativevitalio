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
    val quickMetric: String,
    val sleepmetrics: List<SleepMetric>? ,
    val vitalInsights: List<VitalInsight>?,
    val summary: List<SleepSummaryData>
)
data class SleepMetric(
    val uhid: String,
    val pmId: Int,
    val vitalID: Int,
    val vitalName: String,
    val vitalValue: String?
)
data class SleepValue(
    val Score: Int,
    val Title: String,
    val BedtimeStart: String,
    val BedtimeEnd: String,
    val SleepScore: SleepScore,
    val QuickMetrics: List<QuickMetric>?,
    val QuickMetricsTiled: List<QuickMetricsTiled>?,
    val SleepStages: List<SleepStage>?,
    val MovementGraph: MovementGraph?,
    val MorningAlertness: MorningAlertness?,
    val lastVital: List<Vital>
)
data class MorningAlertness(
    val Minutes: String,
)

data class VitalInsight(
    val vitalID: Int,
    val vitalValue: Double,
    val vitalName: String,
    val unit: String,
    val vitalDateTime: String,
    val severityLevel: String,
    val insight: String,
    val colourCode: String
)
data class SleepSummaryData (
    val title: String,
    val state: String,
    val stateTitle: String,
    val score: Int
)
data class Vitals(
    val id: Int,
    val pmId: Int,
    val vitalID: Int,
    val vitalName: String,
    val vitalPosition: String?,
    val vitalValue: Double,
    val totalValue: Double,
    val unit: String,
    val vitalDateTime: String,
    val userId: Int,
    val rowId: Int
)
data class MovementGraph(
    val Title: String,
    val Data: List<MovementData>
)
data class MovementData(
    val Timestamp: String,
    val Type: String
)
data class QuickMetricsTiled(
    val Title: String,
    val Value: String,
    val Tag: String,
    val TagColor: String,
    val Type: String
)
data class SleepStage(
    val Title: String,
    val Type: String,
    val Percentage: Int,
    val StageTimeText: String,
    val StageTime: Int
)
//data class SleepDetails(
//    val Score: Int,
//    val QuickMetrics: List<QuickMetric>?
//)
// Vital object
data class Vital(
    val uhid: String? = null,
    val pmId: Int = 0,
    val vitalID: Int = 0,
    var vitalName: String? = null,
    var vitalValue: Double = 0.0,
    var totalValue: Double = 0.0,
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

data class SleepScore(
    val Score: Int
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
data class MoodResponse(
    val status: Int,
    val message: String,
    val responseValue: List<MoodItem>
)

data class MoodItem(
    val pid: Int,
    val moodId: Int,
    val label: String,
    val description: String
)
data class MoodsResponse(
    val status: Int,
    val message: String,
    val responseValue: List<Mood>
)

data class Mood(
    val id: Int,
    val label: String,
    val color: String,
    val description: String,
    val emojiRes: Int,   // Drawable resource ID
)


data class EnergyResponse(
    val status: Int,
    val message: String,
    val responseValue: List<EnergyItem>
)

data class EnergyItem(
    val id: Int,
    val pid: Int,
    val energyPercentage: Int,
    val statusLabel: String,
    val userId: Int,
    val clientId: Int,
    val status: Boolean,
    val createdDate: String
)