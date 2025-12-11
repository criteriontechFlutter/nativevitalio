import com.critetiontech.ctvitalio.adapter.PriorityAction
import com.critetiontech.ctvitalio.adapter.PriorityActionWrapper

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
    val sleepmetrics: List<SleepMetric>?,
    val vitalInsights: List<VitalInsightWrapper>?,
    val priorityAction: List<PriorityActionWrapper>?,
    val dailyCheckList: List<DailyCheckListWrapper>?,
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
    val Summary: List<Summary>?,
    val QuickMetricsTiled: List<QuickMetricsTiled>?,
    val SleepStages: List<SleepStage>?,
    val MovementGraph: MovementGraph?,
    val MorningAlertness: MorningAlertness?,
    val lastVital: List<Vital>,
    val HrGraph:HrGraph,
    val GistObject:GistObject,
    val HrvGraph:HrvGraph
)
data class MorningAlertness(
    val Minutes: String,
)
data class QuickMetricsTile(
    val Title: String,
    val Value: String,
    val Tag: String?,
    val TagColor: String?
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

data class  Summary (
    val Title: String,
    val State: String,
    val StateTitle: String,
    val Score: Double
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
    val Value: String?="",
    val Tag: String,
    val TagColor: String,
    val Type: String,
 )

data class SleepStage(
    val Title: String,
    val Type: String,
    val Percentage: Int,
    val StageTimeText: String,
    val StageTime: Int
)
data class SleepValueList(
    val SleepStages: SleepStages?
)

data class SleepStages(
    val awake: SleepStage?,
    val rem_sleep: SleepStage?,
    val light_sleep: SleepStage?,
    val deep_sleep: SleepStage?
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
    var vitalValue: Double? = 0.0,
    var vmValueText: String? = "--",
    var totalValue: Double = 0.0,
    var unit: String? = null,
    var vitalDateTime: String? = null,
    val userId: Int = 0,
    val rowId: Int = 0
)
data class SleepVital(
    val Title: String,
    val State: String,
    val StateTitle: String,
    val Score: Double
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


data class SleepCycle(
    val startTime: String,
    val endTime: String,
    val cycleType: String,  // "complete", "partial", "none"
    val color: String?
)

data class SleepCyclesData(
    val title: String,
    val cycles: List<SleepCycle>,
    val fullCount: Int,
    val partialCount: Int
)


data class LegendItem(
    val title: String,
    val color: String
)
data class HrGraphResponse(
    val HrGraph: HrGraph
)

data class HrGraph(
    val Title: String,
    val Data: List<HrData>
)

data class HrData(
    val Timestamp: String,
    val Value: Double
)
data class GistObject(
    val Title: String?,
    val DetailText: String?,
    val DetailUnitText: String?,
    val Subtitle: String?,
    val Avg: Int?,
    val Min: Int?,
    val Max: Int?
)
data class HrvGraphData(
    val Timestamp: String,
    val Value: Double
)
data class HrvGraph(
    val Title: String?,
    val Data: List<HrvGraphData>?
)
data class Medicine(
    val id: Int,
    val medicineID: Int,
    val medicineName: String,
    val name: String,
    val brandName: String,
    val dosageFormID: Int,
    val dosageFormName: String,
    val shortName: String,
    val doseStrength: Double,
    val doseUnitID: Int,
    val unitName: String,
    val isAntibiotic: Int,
    val translation: String
)
data class MedicineResponse(
    val status: Int,
    val message: String,
    val responseValue: List<Medicine>
)
data class MedicineIntakeResponse(
    val status: Int,
    val message: String,
    val responseValue: MedicineResponseValue
)

data class MedicineResponseValue(
    val loggedMedicines: List<LoggedMedicine>,
    val allMedicines: List<AllMedicine>
)

data class LoggedMedicine(
    val medicineId: Int,
    val medicineName: String,
    val unit: String,
    val dosageType: String,
    val doseDate: String,
    val doseStatus: String,
    val isTaken: Int,
    val takenDateTime: String? = null
)

data class AllMedicine(
    val medicineId: Int,
    val medicineName: String,
    val unit: String,
    val dosageType: String
)
data class FluidResponse(
    val status: Int,
    val message: String,
    val responseValue: List<FluidItem>
)
data class FluidItem(
    val id: Int,
    val pid: Int,
    val intakeDate: String,
    val intakeTime: String,
    val fluidType: String,
    val quantity: Double,
    val remarks: String,
    val clientId: Int
)

   data class DailyCheckListWrapper(
    val pid: Int,
    val dailyChecklist: String
)

data class DailyCheckItem(
    val vmId: Int,
    val goalId: Int,
    val isPinned: Int,
    val targetValue: String,
    val vitalValue: Double,
    val totalFluid_L: Double,
    val isGoalAchieved: Int,
    val goalName: String,
    val unit: String
)
data class InsightJson(
    val date: String,
    val wellnessScore: Int,
    val colorCode: String,
    val wellnessMessage: String,
    val wellnessStatus: String,
    val scores: InsightScores,
    val insights: InsightSections
)

data class InsightScores(
    val sleepScore: Double,
    val recoveryScore: Double,
    val movementScore: Double,
    val stressScore: Double
)
data class VitalInsightWrapper(
    val pid: Int,
    val insightDate: String,
    val insightJson: String     // IMPORTANT: Keep this as String
)
data class InsightSections(
    val sleep: SleepInsight,
    val recovery: RecoveryInsight,
    val movement: MovementInsight,
    val stress: StressInsight
)
data class StressInsight(
    val level: String,
    val message: String,
    val data: StressData,
    val score: Double,
    val colorCode: String
)

data class StressData(
    val level: String
)
data class SleepInsight(
    val quality: String,
    val message: String,
    val data: SleepInsightData,
    val score: Double,
    val colorCode: String
)

data class SleepInsightData(
    val duration_minutes: Int?,
    val efficiency: Int?,
    val deepSleepMinutes: Int?,
    val remSleepMinutes: Int?
)
data class RecoveryInsight(
    val status: String,
    val message: String,
    val data: RecoveryData,
    val score: Double,
    val colorCode: String
)

data class RecoveryData(
    val hrv: Double?,
    val hrv_avg: Double?,
    val resting_hr: Double?
)
data class MovementInsight(
    val progress: String,
    val message: String,
    val data: MovementDataa,
    val score: Double,
    val calories_burned: Int,
    val active_minutes: Int,
    val colorCode: String
)

data class MovementDataa(
    val steps: Int,
    val goal_steps: Int,
    val active_minutes: Int,
    val calories_burned: Int
)




