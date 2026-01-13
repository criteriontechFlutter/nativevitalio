

package com.critetiontech.ctvitalio.UI.fragments

import HrData
import HrvGraph
import MorningAlertness
import SleepValue
import TempGraph
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import com.critetiontech.ctvitalio.R

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil.setContentView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.UI.constructorFiles.HeartRateGraphView
import com.critetiontech.ctvitalio.databinding.ActivityForgotPasswordBinding
import com.critetiontech.ctvitalio.databinding.FragmentEnergyTankBinding
import com.critetiontech.ctvitalio.databinding.FragmentSleepDetailsBinding
import com.critetiontech.ctvitalio.databinding.IncludeProgressCardBinding
import com.critetiontech.ctvitalio.databinding.SleepLayoutBinding
import com.critetiontech.ctvitalio.model.SleepCycleView
import com.critetiontech.ctvitalio.utils.LoaderUtils.hideLoading
import com.critetiontech.ctvitalio.utils.LoaderUtils.showLoading
import com.critetiontech.ctvitalio.viewmodel.ChallengesViewModel
import com.critetiontech.ctvitalio.viewmodel.DashboardViewModel
import com.critetiontech.ctvitalio.viewmodel.SleepDetailsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.renderer.YAxisRenderer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.util.Calendar
import kotlin.getValue

// -------------------------------------------
// Data Classes
// -------------------------------------------
data class SleepEntry(val day: Int, val value: Int)
data class SleepSegment(val type: String, val durationWeight: Float)

// -------------------------------------------
// Fragment
// -------------------------------------------
class SleepDetails : Fragment() {

    private var _binding: FragmentSleepDetailsBinding? = null
    private val binding get() = _binding!!

    private val sleepManager = SleepCycleView()
    private   val viewModel: SleepDetailsViewModel by viewModels()

    // -------------------------------------------
    // Lifecycle
    // -------------------------------------------
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSleepDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }


    // -------------------------------------------
    // onViewCreated
    // -------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) showLoading() else hideLoading()
        }
        lifecycleScope.launch {
            delay(2000) // 2 seconds delay
            viewModel.getVitals("1")
        }

        Log.d("FULL_GISGISTGISTGISTT", viewModel.sleepsummary.value.toString())

        // Example dataset for bar chart
        val calendar = Calendar.getInstance()
        val data = listOf(
            SleepEntry(calendar.get(Calendar.DAY_OF_MONTH), 100),
            SleepEntry(calendar.get(Calendar.DAY_OF_MONTH) + 1, 56),
            SleepEntry(calendar.get(Calendar.DAY_OF_MONTH) + 2, 54),
            SleepEntry(calendar.get(Calendar.DAY_OF_MONTH) + 3, 72),
            SleepEntry(calendar.get(Calendar.DAY_OF_MONTH) + 4, 58),
            SleepEntry(calendar.get(Calendar.DAY_OF_MONTH) + 5, 66),
            SleepEntry(calendar.get(Calendar.DAY_OF_MONTH), 70)
        )

        setData(data) // Build main chart

        binding.wellnessImageArrow.setOnClickListener {
            findNavController().popBackStack()
        }

        // Progress cards
        setDefaultProgress(binding.sleepEfficiencyProgressId)
        setDefaultProgress(binding.tempProgressId)
        setDefaultProgress(binding.restfulnessProgressId)
        setDefaultProgress(binding.totalSleepProgressId)
        setDefaultProgress(binding.hrProgress)
        setDefaultProgress(binding.restorativeSleepProgressId)

        // Observers: HR Graph data + HRV
        // Observers: HR Graph data + HRV
        viewModel.sleepValueList.observe(viewLifecycleOwner) { sleepValue ->
            sleepValue.HrGraph?.Data?.let { hrFromServerData(it) }

        }
        viewModel.sleepValueList.observe(viewLifecycleOwner) { sleepValue ->
            bindHRV(sleepValue.HrvGraph)
        }
        // Setup sleep-cycle graph
        setupSleepCycleGraph()
        hrVariability() // dummy generator-based HRV for initial display

        // Contributors

        // Morning alertness binding
        viewModel.sleepValueList.observe(viewLifecycleOwner) { sleepValue ->
            sleepValue.MorningAlertness?.let { alertness ->
                bindMorningAlertness(alertness)
                binding.tvAlertnessValue.text = "${alertness.Avg ?: 0} min"
            }
        }

        // Open detailed sleep graph fragment
        openSleepGraph()

        // Quick metrics tiles
        viewModel.quickMetricsTiledList.observe(viewLifecycleOwner) { tiles ->
            tiles.firstOrNull { it.Title.equals("TOTAL SLEEP", true) }?.let {
                bindContributorCard(binding.totalSleepIds, it.Title, it.Value, it.Tag, it.TagColor)
            }
            tiles.firstOrNull { it.Title.equals("TIME IN BED", true) }?.let {
                bindContributorCard(binding.timeInBedId, it.Title, it.Value, it.Tag, it.TagColor)
            }
            tiles.firstOrNull { it.Title.equals("RESTORATIVE SLEEP", true) }?.let {
                bindContributorCard(binding.restorativeSleepId, it.Title, it.Value, it.Tag, it.TagColor)
            }
            tiles.firstOrNull { it.Title.equals("HR DROP", true) }?.let {
                bindContributorCard(binding.hr, it.Title, it.Value, it.Tag, it.TagColor)
            }
            Log.d("FULL_GISGISTGISTGISTT", viewModel.sleepsummary.value.toString())

        }


        // Gist HR values
        viewModel.sleepValueList.observe(viewLifecycleOwner) { sleepValue ->
            val hrGraph = sleepValue.HrGraph
            val gist = hrGraph?.GistObject

            val avgBpm = gist?.Avg ?: 0
            val minBpm = gist?.Min ?: 0
            val maxBpm = gist?.Max ?: 0

            binding.tvAvgValue.text = "$avgBpm bpm"
            binding.tvMinValue.text = "$minBpm bpm"
            binding.tvMaxValue.text = "$maxBpm bpm"
        }

        // Map of cards for quick binding from sleepsummary
        val cardMap = mapOf(
            "Sleep Efficiency" to binding.sleepEfficiencyProgressId,
            "Temperature" to binding.tempProgressId,
            "Restfulness" to binding.restfulnessProgressId,
            "Total Sleep" to binding.totalSleepProgressId,
            "HR Drop" to binding.hrProgress,
            "Restorative Sleep" to binding.restorativeSleepProgressId
        )

        viewModel.sleepsummary.observe(viewLifecycleOwner) { list ->
            list.orEmpty().forEach { item ->
                val title = item.Title?.trim() ?: return@forEach
                val card = cardMap[title] ?: return@forEach

                bindProgressCard(
                    card,
                    title,
                    item.StateTitle.orEmpty(),
                    item.Score.toInt()
                )
            }
        }

        viewModel.sleepValueList.observe(viewLifecycleOwner) { sleepValue ->

            val spo2Value = sleepValue.Spo2?.Value ?: 0

            val start = sleepValue.BedtimeStart
            val end = sleepValue.BedtimeEnd

            val sleepHours = calculateSleepHours(start, end)

            bindOxygenSaturation(spo2Value, sleepHours)
        }
        viewModel.sleepValueList.observe(viewLifecycleOwner) { sleepValue ->
            bindSkinTemperature(sleepValue.TempGraph)
        }
        viewModel.sleepValueList.observe(viewLifecycleOwner) { sleepValue ->

            val turns = sleepValue.TossTurn?.Value ?: 0
            val desc  = sleepValue.TossTurn?.Subtitle ?: ""

            val hours = calculateSleepHours(
                sleepValue.BedtimeStart,
                sleepValue.BedtimeEnd
            )

            bindTossTurns(turns, hours, desc)
        }
        bindContributorsData()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun bindSkinTemperature(tempGraph: TempGraph?) {

        if (tempGraph == null || tempGraph.Data.isNullOrEmpty()) return

        val points = tempGraph.Data.map {
            HeartRateGraphView.HeartRatePoint(
                Instant.parse(it.Timestamp).toEpochMilli(),
                it.Value.toInt()
            )
        }

        val avg = points.map { it.bpm }.average()

        binding.skinTempId.apply {
            setData(points)
            bindHeader("Skin Temp", avg.toInt(), "°C")

            val minY = points.minOf { it.bpm } - 2
            val maxY = points.maxOf { it.bpm } + 2
            setYAxisRange(minY, maxY)

            setTimeRange(points.first().timestamp, points.last().timestamp)
            resetDynamicAxes()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun bindTossTurns(turns: Int, sleepHours: Int, desc: String) {

        binding.tvTurnsCount.text = String.format("%02d", turns)   // 08
        binding.tvTurnsDuration.text = "During $sleepHours hrs sleep"
        binding.tvTurnsDescription.text = desc

        Log.d("TOSS_TURN_BIND", "Turns=$turns SleepHours=$sleepHours Desc=$desc")
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateSleepHours(start: String?, end: String?): Int {
        if (start == null || end == null) return 0
        return try {
            val s = Instant.parse(start)
            val e = Instant.parse(end)
            val diff = Duration.between(s, e).toHours().toInt()
            diff
        } catch (e: Exception) {
            0
        }
    }
    @SuppressLint("SetTextI18n")
    private fun bindOxygenSaturation(spo2Value: Int, sleepHours: Int) {

        binding.tvOxygenValue.text = "$spo2Value%"
        binding.tvOxygenDuration.text = "During $sleepHours hrs sleep"

        binding.oxygenProgress.progress = spo2Value

        Log.d("OXYGEN_BIND", "SpO2=$spo2Value SleepHours=$sleepHours")
    }
    // -------------------------------------------
    // Binding helpers & small UI helpers
    // -------------------------------------------

    private fun bindOxygenData(oxygenValue: Int?, sleepHours: Int?) {
        val percentage = oxygenValue ?: 0
        val hours = sleepHours ?: 0

        binding.tvOxygenValue.text = "$percentage%"
        binding.tvOxygenDuration.text = "During $hours hrs sleep"
        binding.oxygenProgress.progress = percentage
    }

    private fun bindProgressCard(
        card: IncludeProgressCardBinding,
        title: String,
        stateTitle: String,
        score: Int
    ) {
        card.cardTitle.text = title                  // Left text
        card.Title.text = stateTitle                 // Right text
        card.sleepProgressBar.progress = score       // Progress bar
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun bindContributorCard(
        layout: SleepLayoutBinding,
        title: String?,
        value: String?,
        tag: String?,
        tagColor: String?
    ) {
        layout.title.text = title ?: "--"

        layout.value.text = HtmlCompat.fromHtml(
            value ?: "--",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        // If no tag/status exists in layout, stop here
        val statusView = layout.status ?: return

        if (tag != null && tagColor != null) {
            statusView.text = tag

            val color = Color.parseColor("#$tagColor")
            statusView.setTextColor(color)

            val bg = statusView.background as? GradientDrawable
            bg?.setColor(color.withAlpha(0.15f))
        }
    }

    // extension-like helper inside class (keeps original behaviour)
    fun Int.withAlpha(alpha: Float): Int {
        val a = (alpha * 255).toInt().coerceIn(0, 255)
        return (this and 0x00FFFFFF) or (a shl 24)
    }

    // -------------------------------------------
    // HR Variability (API-bound)
    // -------------------------------------------

    // -------------------------------------------
    // Open sleep graph fragment
    // -------------------------------------------
    private fun openSleepGraph() {
        childFragmentManager.beginTransaction()
            .replace(R.id.sleepGraph, SleepGraphFragment())
            .commit()
    }

    // -------------------------------------------
    // Default progress setup
    // -------------------------------------------
    private fun setDefaultProgress(card: IncludeProgressCardBinding) {
        card.sleepProgressBar.progress = 1
        card.cardTitle.text = "--"
        card.Title.text = "--"
    }

    // -------------------------------------------
    // Bind contributors (keeps original observers)
    // -------------------------------------------
    private fun bindContributorsData() {

        viewModel.vitalList.observe(viewLifecycleOwner) { sleepValue ->

            val totalSleep = sleepValue
                ?.firstOrNull { it.vitalName.equals("TotalSleep", ignoreCase = true) }

            binding.totalSleepIds.title.text = "Total Sleep"
            binding.totalSleepIds.value.text = totalSleep?.vmValueText.toString()
            binding.totalSleepIds.status.text = totalSleep?.severityLevel.toString()


            val restorative = sleepValue
                ?.firstOrNull { it.vitalName.equals("RestorativeSleep", ignoreCase = true) }
            // original code used '==' in one spot; kept as-is (no logic edits)
            binding.restorativeSleepId.title.text  ="Restorative Sleep"
            binding.restorativeSleepId.value.text  =restorative?.vmValueText.toString()
            binding.restorativeSleepId.status.text  =restorative?.severityLevel.toString()

            val timeinBed = sleepValue
                ?.firstOrNull { it.vitalName.equals("TimeInBed", ignoreCase = true) }
            binding.timeInBedId.title.text ="Time In Bed"
            binding.timeInBedId.value.text =timeinBed?.vmValueText.toString()
            binding.timeInBedId.status.text =timeinBed?.severityLevel.toString()
            val hr = sleepValue
                ?.firstOrNull { it.vitalName.equals("HRV", ignoreCase = true) }
            binding.hr.title.text ="HR Drop"
            binding.hr.value.text =hr?.vmValueText.toString()
            binding.hr.status.text =hr?.severityLevel.toString()
            Log.d("EfficiencyEfficiencyEfficiency", viewModel.sleepsummary.value.toString())
        }

        val cardMap = mapOf(
            "Sleep Efficiency" to binding.sleepEfficiencyProgressId,
            "Temperature" to binding.tempProgressId,
            "Restfulness" to binding.restfulnessProgressId,
            "Total Sleep" to binding.totalSleepProgressId,
            "HR Drop" to binding.hrProgress,
            "Restorative Sleep" to binding.restorativeSleepProgressId
        )
        viewModel.sleepValueList.observe(viewLifecycleOwner) { sleepValue ->
            sleepValue.MorningAlertness?.let { alertness ->
                bindMorningAlertnessCard(alertness)
            }
        }
        viewModel.sleepsummary.observe(viewLifecycleOwner) { list ->
            list?.forEach { item ->
                val card = cardMap[item.Title]
                card?.let { c ->
                    c.sleepProgressBar.progress = item.Score.toInt()
                    c.cardTitle.text = item.Title
                    c.Title.text = item.StateTitle
                }
            }
        }
    }
    @SuppressLint("SetTextI18n")
    private fun bindMorningAlertnessCard(alertness: MorningAlertness) {

        val avg = alertness.Avg ?: 0
        val subtitle = "7 Days Average"

        binding.tvAlertnessValue.text = "$avg min"
        binding.tvAlertnessSubtitle.text = subtitle

        Log.d("ALERTNESS_BIND", "Avg=$avg  Subtitle=$subtitle")
    }
    // -------------------------------------------
    // Morning Alertness (MPAndroidChart)
    // -------------------------------------------
    private fun bindMorningAlertness(alertness: MorningAlertness) {

        val chart = binding.alertnessBarChart

        val values = alertness.Values ?: emptyList()
        if (values.isEmpty()) return

        // Create bar entries: X = day index, Y = alertness score
        val entries = values.mapIndexed { index, v ->
            BarEntry(index.toFloat(), v.toFloat())
        }

        val dataSet = BarDataSet(entries, "Morning Alertness").apply {
            color = Color.parseColor("#1976D2")
            valueTextColor = Color.BLACK
            valueTextSize = 10f
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.4f
        chart.data = barData

        // X-axis labels → show last 7 days: Mon Tue Wed Thu...
        val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(dayLabels)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            textSize = 12f
            setDrawGridLines(false)
        }

        chart.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = 100f
            textSize = 12f
        }
        chart.axisRight.isEnabled = false

        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.animateY(900)

        chart.invalidate()
    }

    // -------------------------------------------
    // ISO -> millis utility
    // -------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    fun isoToMillis(isoDate: String): Long {
        return Instant.parse(isoDate).toEpochMilli()
    }

    // -------------------------------------------
    // HR from server data (binds to heartRateGraph)
    // -------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun hrFromServerData(hrList: List<HrData>) {

        if (hrList.isEmpty()) return

        val points = hrList.map {
            HeartRateGraphView.HeartRatePoint(
                isoToMillis(it.Timestamp),
                it.Value.toInt()
            )
        }

        val avg = points.map { it.bpm }.average().toInt()

        binding.heartRateGraph.apply {
            setData(points)

            // ⭐ Correct Title + Value
            bindHeader("Heart Rate", avg, "bpm")

            thresholdValue = 84
            setYAxisRange(40, 120)
            setTimeRange(points.first().timestamp, points.last().timestamp)
            // resetDynamicAxes() // enable if needed
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun bindHRV(hrvGraph: HrvGraph?) {

        if (hrvGraph == null || hrvGraph.Data.isNullOrEmpty()) return

        val points = hrvGraph.Data.map {
            HeartRateGraphView.HeartRatePoint(
                Instant.parse(it.Timestamp).toEpochMilli(),
                it.Value.toInt()
            )
        }

        val avg = points.map { it.bpm }.average().toInt()

        binding.heartRateVariablityGraph.apply {
            setData(points)

            // ⭐ Proper Header
            bindHeader("HRV Zone", avg, "ms")

            val minY = points.minOf { it.bpm } - 5
            val maxY = points.maxOf { it.bpm } + 5
            setYAxisRange(minY, maxY)

            setTimeRange(points.first().timestamp, points.last().timestamp)
            resetDynamicAxes()
        }
    }
    // -------------------------------------------
    // Dummy HR Variability (generator)
    // -------------------------------------------
    @SuppressLint("SetTextI18n")
    private fun hrVariability() {
        val now = System.currentTimeMillis()
        val points = mutableListOf<HeartRateGraphView.HeartRatePoint>()

        // Generate 10 dynamic random BPM points, 1 min apart
        for (i in 0..10) {
            points.add(
                HeartRateGraphView.HeartRatePoint(
                    now + i * 60_000L, // each minute
                    (60..90).random()
                )
            )
        }

        // Update graph with new data
        binding.heartRateVariablityGraph.setData(points)

        // Set threshold line (e.g., safe HR zone)
        binding.heartRateVariablityGraph.thresholdValue = 74

        // Example: fix Y-axis and time range manually
        binding.heartRateVariablityGraph.setYAxisRange(40, 90)
        binding.heartRateVariablityGraph.setTimeRange(now, now + 600_000L)

        // If you want to restore automatic scaling later:
        binding.heartRateVariablityGraph.resetDynamicAxes()
    }

    // -------------------------------------------
    // Sleep cycle graph setup (uses SleepCycleView.parseFromJson)
    // -------------------------------------------
    private fun setupSleepCycleGraph() {
        val sleepManager = SleepCycleView()
        val cyclesData = sleepManager.parseFromJson()

        // Create time labels programmatically
        val timeContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
        }

        val timeLabels = mutableListOf<TextView>()
        val gravities = listOf(
            Gravity.START,
            Gravity.CENTER,
            Gravity.CENTER,
            Gravity.CENTER,
            Gravity.END
        )

        for (i in 0..4) {
            val tv = TextView(requireContext()).apply {
                layoutParams =
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                textSize = 11f
                setTextColor(Color.parseColor("#999999"))
                gravity = gravities[i]
            }
            timeLabels.add(tv)
            timeContainer.addView(tv)
        }

        // Add time container below sleep graph (adjust based on your parent layout)
        // binding.yourParentLayout.addView(timeContainer)

        sleepManager.setupSleepCycleGraph(
            context = requireContext(),
            sleepGraphContainer = binding.sleepGraph,
            tvSleepCycleCount = binding.tvSleepCycleCount,
            cyclesData = cyclesData,
            timeLabels = timeLabels
        )
        binding.tvLegend.text = "${cyclesData.fullCount} Full / ${cyclesData.partialCount} Partial"
    }

    // -------------------------------------------
    // Alternate Sleep graph builder (keeps original)
    // -------------------------------------------
    private fun setupSleepCycleGrapha() {
        val sleepSegments = listOf(
            SleepSegment("full", 2f),
            SleepSegment("partial", 1f),
            SleepSegment("full", 3f),
            SleepSegment("partial", 1.5f),
            SleepSegment("full", 2f)
        )

        val movements =
            listOf(true, false, true, true, false, true, false, false, true, true)

        binding.tvSleepCycleCount.text = "Sleep Cycle ${sleepSegments.size}"

        binding.sleepGraph.removeAllViews()
        binding.movementRow.removeAllViews()

        // Build sleep bars
        for (segment in sleepSegments) {
            val bar = View(requireContext())
            val params = LinearLayout.LayoutParams(0, 30.dp, segment.durationWeight)
            params.setMargins(2.dp, 0, 2.dp, 0)
            bar.layoutParams = params
            bar.background = when (segment.type) {
                "full" -> createBarDrawable("#5AE3B1", "#A0E8D0")
                else -> createBarDrawable("#E0E0E0", "#F5F5F5")
            }
            binding.sleepGraph.addView(bar)
        }

        // Build movement indicators
        for (move in movements) {
            val bar = View(requireContext())
            val params = LinearLayout.LayoutParams(3.dp, LinearLayout.LayoutParams.MATCH_PARENT)
            params.setMargins(1.dp, 0, 1.dp, 0)
            bar.layoutParams = params
            bar.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 2.dp.toFloat()
                setColor(if (move) 0xFF4BA3FF.toInt() else 0xFFD0D0D0.toInt())
            }
            binding.movementRow.addView(bar)
        }

        // Optional icons (Bed + Alarm)
        val bedIcon = ImageView(requireContext()).apply {
            setImageResource(R.drawable.ic_graph)
            layoutParams = LinearLayout.LayoutParams(20.dp, 20.dp)
        }
        val alarmIcon = ImageView(requireContext()).apply {
            setImageResource(R.drawable.ic_mic)
            layoutParams = LinearLayout.LayoutParams(20.dp, 20.dp)
        }

        binding.movementRow.addView(bedIcon, 0)
        binding.movementRow.addView(alarmIcon)
    }

    // -------------------------------------------
    // Create gradient drawable for bars
    // -------------------------------------------
    private fun createBarDrawable(startColor: String, endColor: String): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor))
        ).apply {
            cornerRadius = 8.dp.toFloat()
        }
    }

    // -------------------------------------------
    // Chart Builder for SleepEntry data
    // -------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun setData(entries: List<SleepEntry>) {
        binding.barsContainer.removeAllViews()
        if (entries.isEmpty()) return

        val maxValue = entries.maxOf { it.value }
        val avg = entries.map { it.value }.average().toInt()

        binding.tvScore.text = avg.toString()
        binding.tvLabel.text = "Sleep"

        // Wait for container height to be ready
        binding.barsContainer.post {
            val containerHeight = binding.barsContainer.height

            // Calculate maximum bar height (leaving space for bubble and labels)
            val maxBarHeight = containerHeight - 60.dp // Space for bubble (24dp) + baseline (4dp) + label (20dp) + padding

            entries.forEach { entry ->
                // Calculate proportional fill height based on value
                val fillRatio = entry.value.toFloat() / maxValue.toFloat()
                val fillHeight = (maxBarHeight * fillRatio).toInt()

                val barLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1f
                    ).apply {
                        // Add horizontal spacing between bars
                        marginStart = 0.dp
                        marginEnd = 0.dp
                    }
                }

                // Spacer to push bar to bottom
                val spacer = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1f
                    )
                }

                val barContainer = FrameLayout(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        24.dp,
                        fillHeight - 30.dp // Bar height + bubble height
                    )
                    setPadding(0, 0, 0, 0)
                }

                // Background track (semi-transparent white)
                val trackView = View(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        24.dp,
                        fillHeight,
                        Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    )
                    background = GradientDrawable().apply {
                        setColor(Color.parseColor("#40FFFFFF"))
                        cornerRadius = 12.dp.toFloat()
                    }
                }

                // Fill view (white)
                val fillView = View(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        4.dp,
                        fillHeight,
                        Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    )
                    background = GradientDrawable().apply {
                        setColor(Color.WHITE)
                        cornerRadius = 2.dp.toFloat()
                    }
                }

                // Value bubble at top of bar
                val bubble = TextView(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        24.dp,
                        24.dp,
                        Gravity.TOP or Gravity.CENTER_HORIZONTAL
                    )
                    text = entry.value.toString()
                    setTextColor(Color.parseColor("#0A84FF"))
                    textSize = 11f
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    background = GradientDrawable().apply {
                        setColor(Color.WHITE)
                        shape = GradientDrawable.OVAL
                    }
                    elevation = 2.dp.toFloat()
                }

                // Weekday bubble at bottom of bar
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_MONTH, -(entries.size - 1 - entries.indexOf(entry)))
                val weekdayInitial = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.SUNDAY -> "S"
                    Calendar.MONDAY -> "M"
                    Calendar.TUESDAY -> "T"
                    Calendar.WEDNESDAY -> "W"
                    Calendar.THURSDAY -> "T"
                    Calendar.FRIDAY -> "F"
                    Calendar.SATURDAY -> "S"
                    else -> ""
                }

                val weekdayBubble = TextView(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        20.dp,
                        20.dp,
                        Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    )
                    text = weekdayInitial
                    setTextColor(Color.parseColor("#0A84FF"))
                    textSize = 10f
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    background = GradientDrawable().apply {
                        setColor(Color.WHITE)
                        shape = GradientDrawable.OVAL
                    }
                    elevation = 1.dp.toFloat()
                }

                // Horizontal baseline (full width)
                val baseLine = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2.dp
                    ).apply {
                        topMargin = 6.dp
                        bottomMargin = 6.dp
                    }
                    background = GradientDrawable().apply {
                        setColor(Color.parseColor("#40FFFFFF"))
                    }
                }

                // Date label
                val dayMonth = "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}"

                val dayLabel = TextView(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    text = dayMonth
                    setTextColor(Color.parseColor("#80FFFFFF"))
                    textSize = 10f
                    gravity = Gravity.CENTER
                }
                barContainer.addView(trackView)
                barContainer.addView(fillView)
                barContainer.addView(bubble)
                barContainer.addView(weekdayBubble)
                barLayout.addView(spacer)
                barLayout.addView(barContainer)
                barLayout.addView(baseLine)
                barLayout.addView(dayLabel)
                binding.barsContainer.addView(barLayout)
            }
        }
    }

    // -------------------------------------------
    // Utilities / Extensions
    // -------------------------------------------
    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()
}

//package com.critetiontech.ctvitalio.UI.fragments
//
//import HrData
//import HrvGraph
//import MorningAlertness
//import SleepValue
//import TempGraph
//import android.annotation.SuppressLint
//import android.content.Context
//import android.content.res.Resources
//import android.graphics.Canvas
//import com.critetiontech.ctvitalio.R
//
//import android.graphics.Color
//import android.graphics.Paint
//import android.graphics.RectF
//import android.graphics.Typeface
//import android.graphics.drawable.GradientDrawable
//import android.os.Build
//import android.os.Bundle
//import android.util.Log
//import android.view.Gravity
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.FrameLayout
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.ScrollView
//import android.widget.TextView
//import androidx.annotation.RequiresApi
//import androidx.core.content.ContextCompat
//import androidx.core.text.HtmlCompat
//import androidx.databinding.DataBindingUtil.setContentView
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.ViewModelProvider
//import androidx.navigation.fragment.findNavController
//import com.critetiontech.ctvitalio.UI.constructorFiles.HeartRateGraphView
//import com.critetiontech.ctvitalio.databinding.ActivityForgotPasswordBinding
//import com.critetiontech.ctvitalio.databinding.FragmentEnergyTankBinding
//import com.critetiontech.ctvitalio.databinding.FragmentSleepDetailsBinding
//import com.critetiontech.ctvitalio.databinding.IncludeProgressCardBinding
//import com.critetiontech.ctvitalio.databinding.SleepLayoutBinding
//import com.critetiontech.ctvitalio.model.SleepCycleView
//import com.critetiontech.ctvitalio.utils.LoaderUtils.hideLoading
//import com.critetiontech.ctvitalio.utils.LoaderUtils.showLoading
//import com.critetiontech.ctvitalio.viewmodel.ChallengesViewModel
//import com.critetiontech.ctvitalio.viewmodel.DashboardViewModel
//import com.github.mikephil.charting.components.XAxis
//import com.github.mikephil.charting.components.YAxis
//import com.github.mikephil.charting.data.BarData
//import com.github.mikephil.charting.data.BarDataSet
//import com.github.mikephil.charting.data.BarEntry
//import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
//import com.github.mikephil.charting.renderer.YAxisRenderer
//import java.nio.file.Path
//import java.time.Duration
//import java.time.Instant
//import java.util.Calendar
//
//// -------------------------------------------
//// Data Classes
//// -------------------------------------------
//data class SleepEntry(val day: Int, val value: Int)
//data class SleepSegment(val type: String, val durationWeight: Float)
//
//// -------------------------------------------
//// Fragment
//// -------------------------------------------
//class SleepDetails : Fragment() {
//
//    private var _binding: FragmentSleepDetailsBinding? = null
//    private val binding get() = _binding!!
//
//    private val sleepManager = SleepCycleView()
//    private lateinit var viewModel: DashboardViewModel
//
//    // -------------------------------------------
//    // Lifecycle
//    // -------------------------------------------
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentSleepDetailsBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null // ✅ Avoid memory leaks
//    }
//
//    // -------------------------------------------
//    // onViewCreated
//    // -------------------------------------------
//    @RequiresApi(Build.VERSION_CODES.O)
//    @SuppressLint("SetTextI18n")
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
//        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
//            if (isLoading) showLoading() else hideLoading()
//        }
//
//        // Example dataset for bar chart
//        val calendar = Calendar.getInstance()
//        viewModel.getVitals()
//        val data = listOf(
//            SleepEntry(calendar.get(Calendar.DAY_OF_MONTH), 100),
//            SleepEntry(calendar.get(Calendar.DAY_OF_MONTH) + 1, 56),
//            SleepEntry(calendar.get(Calendar.DAY_OF_MONTH) + 2, 54),
//            SleepEntry(calendar.get(Calendar.DAY_OF_MONTH) + 3, 72),
//            SleepEntry(calendar.get(Calendar.DAY_OF_MONTH) + 4, 58),
//            SleepEntry(calendar.get(Calendar.DAY_OF_MONTH) + 5, 66),
//            SleepEntry(calendar.get(Calendar.DAY_OF_MONTH), 70)
//        )
//
//        setData(data) // Build main chart
//
//        binding.wellnessImageArrow.setOnClickListener {
//            findNavController().popBackStack()
//        }
//
//        // Progress cards
//        setDefaultProgress(binding.sleepEfficiencyProgressId)
//        setDefaultProgress(binding.tempProgressId)
//        setDefaultProgress(binding.restfulnessProgressId)
//        setDefaultProgress(binding.totalSleepProgressId)
//        setDefaultProgress(binding.hrProgress)
//        setDefaultProgress(binding.restorativeSleepProgressId)
//
//        // Observers: HR Graph data + HRV
//        viewModel.sleepValueList.observe(viewLifecycleOwner) { sleepValue ->
//            sleepValue.HrGraph.Data?.let { hrFromServerData(it) }
//
//        }
//        viewModel.sleepValueList.observe(viewLifecycleOwner) { sleepValue ->
//            bindHRV(sleepValue.HrvGraph)
//        }
//        // Setup sleep-cycle graph
//        setupSleepCycleGraph()
//        hrVariability() // dummy generator-based HRV for initial display
//
//        // Contributors
//        bindContributorsData()
//
//        // Morning alertness binding
//        viewModel.sleepValueList.observe(viewLifecycleOwner) { sleepValue ->
//            sleepValue.MorningAlertness?.let { alertness ->
//                bindMorningAlertness(alertness)
//                binding.tvAlertnessValue.text = "${alertness.Avg ?: 0} min"
//            }
//        }
//
//        // Open detailed sleep graph fragment
//        openSleepGraph()
//
//        // Quick metrics tiles
//        viewModel.quickMetricsTiledList.observe(viewLifecycleOwner) { tiles ->
//            tiles.firstOrNull { it.Title.equals("TOTAL SLEEP", true) }?.let {
//                bindContributorCard(binding.totalSleepIds, it.Title, it.Value, it.Tag, it.TagColor)
//            }
//            tiles.firstOrNull { it.Title.equals("TIME IN BED", true) }?.let {
//                bindContributorCard(binding.timeInBedId, it.Title, it.Value, it.Tag, it.TagColor)
//            }
//            tiles.firstOrNull { it.Title.equals("RESTORATIVE SLEEP", true) }?.let {
//                bindContributorCard(binding.restorativeSleepId, it.Title, it.Value, it.Tag, it.TagColor)
//            }
//            tiles.firstOrNull { it.Title.equals("HR DROP", true) }?.let {
//                bindContributorCard(binding.hr, it.Title, it.Value, it.Tag, it.TagColor)
//            }
//        }
//
//        // Gist HR values
//        viewModel.sleepValueList.observe(viewLifecycleOwner) { sleepValue ->
//            val hrGraph = sleepValue.HrGraph
//            val gist = hrGraph.GistObject
//
//            val avgBpm = gist?.Avg ?: 0
//            val minBpm = gist?.Min ?: 0
//            val maxBpm = gist?.Max ?: 0
//
//            Log.d("GIST_DATA", "Avg=$avgBpm  Min=$minBpm  Max=$maxBpm")
//            Log.d("FULL_GIST", "GIST OBJECT = $gist")
//
//            binding.tvAvgValue.text = "$avgBpm bpm"
//            binding.tvMinValue.text = "$minBpm bpm"
//            binding.tvMaxValue.text = "$maxBpm bpm"
//        }
//
//        // Map of cards for quick binding from sleepsummary
//        val cardMap = mapOf(
//            "Sleep Efficiency" to binding.sleepEfficiencyProgressId,
//            "Temperature" to binding.tempProgressId,
//            "Restfulness" to binding.restfulnessProgressId,
//            "Total Sleep" to binding.totalSleepProgressId,
//            "HR Drop" to binding.hrProgress,
//            "Restorative Sleep" to binding.restorativeSleepProgressId
//        )
//
//        viewModel.sleepsummary.observe(viewLifecycleOwner) { list ->
//            list?.forEach { item ->
//                val card = cardMap[item.Title]
//                if (card != null) {
//                    bindProgressCard(
//                        card,
//                        item.Title,          // LEFT LABEL
//                        item.StateTitle,     // RIGHT STATUS
//                        item.Score.toInt()   // PROGRESS
//                    )
//                }
//            }
//        }
//        viewModel.sleepValueList.observe(viewLifecycleOwner) { sleepValue ->
//
//            val spo2Value = sleepValue.Spo2?.Value ?: 0
//
//            val start = sleepValue.BedtimeStart
//            val end = sleepValue.BedtimeEnd
//
//            val sleepHours = calculateSleepHours(start, end)
//
//            bindOxygenSaturation(spo2Value, sleepHours)
//        }
//        viewModel.sleepValueList.observe(viewLifecycleOwner) { sleepValue ->
//            bindSkinTemperature(sleepValue.TempGraph)
//        }
//viewModel.sleepValueList.observe(viewLifecycleOwner) { sleepValue ->
//
//            val turns = sleepValue.TossTurn?.Value ?: 0
//            val desc  = sleepValue.TossTurn?.Subtitle ?: ""
//
//            val hours = calculateSleepHours(
//                sleepValue.BedtimeStart,
//                sleepValue.BedtimeEnd
//            )
//
//            bindTossTurns(turns, hours, desc)
//        }
//    }
//    @RequiresApi(Build.VERSION_CODES.O)
//    @SuppressLint("SetTextI18n")
//    private fun bindSkinTemperature(tempGraph: TempGraph?) {
//
//        if (tempGraph == null || tempGraph.Data.isNullOrEmpty()) return
//
//        val points = tempGraph.Data.map {
//            HeartRateGraphView.HeartRatePoint(
//                Instant.parse(it.Timestamp).toEpochMilli(),
//                it.Value.toInt()
//            )
//        }
//
//        val avg = points.map { it.bpm }.average()
//
//        binding.skinTempId.apply {
//            setData(points)
//            bindHeader("Skin Temp", avg.toInt(), "°C")
//
//            val minY = points.minOf { it.bpm } - 2
//            val maxY = points.maxOf { it.bpm } + 2
//            setYAxisRange(minY, maxY)
//
//            setTimeRange(points.first().timestamp, points.last().timestamp)
//            resetDynamicAxes()
//        }
//    }
//
//    @SuppressLint("SetTextI18n")
//    private fun bindTossTurns(turns: Int, sleepHours: Int, desc: String) {
//
//        binding.tvTurnsCount.text = String.format("%02d", turns)   // 08
//        binding.tvTurnsDuration.text = "During $sleepHours hrs sleep"
//        binding.tvTurnsDescription.text = desc
//
//        Log.d("TOSS_TURN_BIND", "Turns=$turns SleepHours=$sleepHours Desc=$desc")
//    }
//     @RequiresApi(Build.VERSION_CODES.O)
//    private fun calculateSleepHours(start: String?, end: String?): Int {
//        if (start == null || end == null) return 0
//        return try {
//            val s = Instant.parse(start)
//            val e = Instant.parse(end)
//            val diff = Duration.between(s, e).toHours().toInt()
//            diff
//        } catch (e: Exception) {
//            0
//        }
//    }
//    @SuppressLint("SetTextI18n")
//    private fun bindOxygenSaturation(spo2Value: Int, sleepHours: Int) {
//
//        binding.tvOxygenValue.text = "$spo2Value%"
//        binding.tvOxygenDuration.text = "During $sleepHours hrs sleep"
//
//        binding.oxygenProgress.progress = spo2Value
//
//        Log.d("OXYGEN_BIND", "SpO2=$spo2Value SleepHours=$sleepHours")
//    }
//    // -------------------------------------------
//    // Binding helpers & small UI helpers
//    // -------------------------------------------
//
//    private fun bindOxygenData(oxygenValue: Int?, sleepHours: Int?) {
//        val percentage = oxygenValue ?: 0
//        val hours = sleepHours ?: 0
//
//        binding.tvOxygenValue.text = "$percentage%"
//        binding.tvOxygenDuration.text = "During $hours hrs sleep"
//        binding.oxygenProgress.progress = percentage
//    }
//
//    private fun bindProgressCard(
//        card: IncludeProgressCardBinding,
//        title: String,
//        stateTitle: String,
//        score: Int
//    ) {
//        card.cardTitle.text = title                  // Left text
//        card.Title.text = stateTitle                 // Right text
//        card.sleepProgressBar.progress = score       // Progress bar
//    }
//
//    @SuppressLint("UseCompatLoadingForDrawables")
//    private fun bindContributorCard(
//        layout: SleepLayoutBinding,
//        title: String?,
//        value: String?,
//        tag: String?,
//        tagColor: String?
//    ) {
//        layout.title.text = title ?: "--"
//
//        layout.value.text = HtmlCompat.fromHtml(
//            value ?: "--",
//            HtmlCompat.FROM_HTML_MODE_LEGACY
//        )
//
//        // If no tag/status exists in layout, stop here
//        val statusView = layout.status ?: return
//
//        if (tag != null && tagColor != null) {
//            statusView.text = tag
//
//            val color = Color.parseColor("#$tagColor")
//            statusView.setTextColor(color)
//
//            val bg = statusView.background as? GradientDrawable
//            bg?.setColor(color.withAlpha(0.15f))
//        }
//    }
//
//    // extension-like helper inside class (keeps original behaviour)
//    fun Int.withAlpha(alpha: Float): Int {
//        val a = (alpha * 255).toInt().coerceIn(0, 255)
//        return (this and 0x00FFFFFF) or (a shl 24)
//    }
//
//    // -------------------------------------------
//    // HR Variability (API-bound)
//    // -------------------------------------------
//
//    // -------------------------------------------
//    // Open sleep graph fragment
//    // -------------------------------------------
//    private fun openSleepGraph() {
//        childFragmentManager.beginTransaction()
//            .replace(R.id.sleepGraph, SleepGraphFragment())
//            .commit()
//    }
//
//    // -------------------------------------------
//    // Default progress setup
//    // -------------------------------------------
//    private fun setDefaultProgress(card: IncludeProgressCardBinding) {
//        card.sleepProgressBar.progress = 1
//        card.cardTitle.text = "--"
//        card.Title.text = "--"
//    }
//
//    // -------------------------------------------
//    // Bind contributors (keeps original observers)
//    // -------------------------------------------
//    @SuppressLint("SuspiciousIndentation")
//    private fun bindContributorsData() {
//
//        viewModel.sleepValueList.observe(viewLifecycleOwner) { sleepValue ->
//
//            val totalSleep = sleepValue.QuickMetricsTiled
//                ?.firstOrNull { it.Title.equals("TOTAL SLEEP", ignoreCase = true) }
//
//            binding.totalSleepIds.value.text =
//                HtmlCompat.fromHtml(totalSleep?.Value.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
//
//            val restorative = sleepValue.QuickMetricsTiled
//                ?.firstOrNull { it.Title.equals("RESTORATIVE SLEEP", ignoreCase = true) }
//            // original code used '==' in one spot; kept as-is (no logic edits)
//            binding.restorativeSleepId.value.text == HtmlCompat.fromHtml(
//                restorative?.Value.toString(),
//                HtmlCompat.FROM_HTML_MODE_LEGACY
//            )
//
//            val timeinBed = sleepValue.QuickMetricsTiled
//                ?.firstOrNull { it.Title.equals("TIME IN BED", ignoreCase = true) }
//            binding.timeInBedId.value.text =
//                HtmlCompat.fromHtml(timeinBed?.Value.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
//        }
//
//        val cardMap = mapOf(
//            "Sleep Efficiency" to binding.sleepEfficiencyProgressId,
//            "Temperature" to binding.tempProgressId,
//            "Restfulness" to binding.restfulnessProgressId,
//            "Total Sleep" to binding.totalSleepProgressId,
//            "HR Drop" to binding.hrProgress,
//            "Restorative Sleep" to binding.restorativeSleepProgressId
//        )
//        viewModel.sleepValueList.observe(viewLifecycleOwner) { sleepValue ->
//            sleepValue.MorningAlertness?.let { alertness ->
//                bindMorningAlertnessCard(alertness)
//            }
//        }
//        viewModel.sleepsummary.observe(viewLifecycleOwner) { list ->
//            list?.forEach { item ->
//                val card = cardMap[item.Title]
//                card?.let { c ->
//                    c.sleepProgressBar.progress = item.Score.toInt()
//                    c.cardTitle.text = item.Title
//                    c.Title.text = item.StateTitle
//                }
//            }
//        }
//    }
//    @SuppressLint("SetTextI18n")
//    private fun bindMorningAlertnessCard(alertness: MorningAlertness) {
//
//        val avg = alertness.Avg ?: 0
//        val subtitle = "7 Days Average"
//
//        binding.tvAlertnessValue.text = "$avg min"
//        binding.tvAlertnessSubtitle.text = subtitle
//
//        Log.d("ALERTNESS_BIND", "Avg=$avg  Subtitle=$subtitle")
//    }
//    // -------------------------------------------
//    // Morning Alertness (MPAndroidChart)
//    // -------------------------------------------
//    private fun bindMorningAlertness(alertness: MorningAlertness) {
//
//        val chart = binding.alertnessBarChart
//
//        val values = alertness.Values ?: emptyList()
//        if (values.isEmpty()) return
//
//        // Create bar entries: X = day index, Y = alertness score
//        val entries = values.mapIndexed { index, v ->
//            BarEntry(index.toFloat(), v.toFloat())
//        }
//
//        val dataSet = BarDataSet(entries, "Morning Alertness").apply {
//            color = Color.parseColor("#1976D2")
//            valueTextColor = Color.BLACK
//            valueTextSize = 10f
//        }
//
//        val barData = BarData(dataSet)
//        barData.barWidth = 0.4f
//        chart.data = barData
//
//        // X-axis labels → show last 7 days: Mon Tue Wed Thu...
//        val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
//        chart.xAxis.apply {
//            valueFormatter = IndexAxisValueFormatter(dayLabels)
//            position = XAxis.XAxisPosition.BOTTOM
//            granularity = 1f
//            textSize = 12f
//            setDrawGridLines(false)
//        }
//
//        chart.axisLeft.apply {
//            axisMinimum = 0f
//            axisMaximum = 100f
//            textSize = 12f
//        }
//        chart.axisRight.isEnabled = false
//
//        chart.description.isEnabled = false
//        chart.legend.isEnabled = false
//        chart.animateY(900)
//
//        chart.invalidate()
//    }
//
//    // -------------------------------------------
//    // ISO -> millis utility
//    // -------------------------------------------
//    @RequiresApi(Build.VERSION_CODES.O)
//    fun isoToMillis(isoDate: String): Long {
//        return Instant.parse(isoDate).toEpochMilli()
//    }
//
//    // -------------------------------------------
//    // HR from server data (binds to heartRateGraph)
//    // -------------------------------------------
//    @RequiresApi(Build.VERSION_CODES.O)
//    @SuppressLint("SetTextI18n")
//    private fun hrFromServerData(hrList: List<HrData>) {
//
//        if (hrList.isEmpty()) return
//
//        val points = hrList.map {
//            HeartRateGraphView.HeartRatePoint(
//                isoToMillis(it.Timestamp),
//                it.Value.toInt()
//            )
//        }
//
//        val avg = points.map { it.bpm }.average().toInt()
//
//        binding.heartRateGraph.apply {
//            setData(points)
//
//            // ⭐ Correct Title + Value
//            bindHeader("Heart Rate", avg, "bpm")
//
//            thresholdValue = 84
//            setYAxisRange(40, 120)
//            setTimeRange(points.first().timestamp, points.last().timestamp)
//            // resetDynamicAxes() // enable if needed
//        }
//    }
//    @RequiresApi(Build.VERSION_CODES.O)
//    @SuppressLint("SetTextI18n")
//    private fun bindHRV(hrvGraph: HrvGraph?) {
//
//        if (hrvGraph == null || hrvGraph.Data.isNullOrEmpty()) return
//
//        val points = hrvGraph.Data.map {
//            HeartRateGraphView.HeartRatePoint(
//                Instant.parse(it.Timestamp).toEpochMilli(),
//                it.Value.toInt()
//            )
//        }
//
//        val avg = points.map { it.bpm }.average().toInt()
//
//        binding.heartRateVariablityGraph.apply {
//            setData(points)
//
//            // ⭐ Proper Header
//            bindHeader("HRV Zone", avg, "ms")
//
//            val minY = points.minOf { it.bpm } - 5
//            val maxY = points.maxOf { it.bpm } + 5
//            setYAxisRange(minY, maxY)
//
//            setTimeRange(points.first().timestamp, points.last().timestamp)
//            resetDynamicAxes()
//        }
//    }
//    // -------------------------------------------
//    // Dummy HR Variability (generator)
//    // -------------------------------------------
//    @SuppressLint("SetTextI18n")
//    private fun hrVariability() {
//        val now = System.currentTimeMillis()
//        val points = mutableListOf<HeartRateGraphView.HeartRatePoint>()
//
//        // Generate 10 dynamic random BPM points, 1 min apart
//        for (i in 0..10) {
//            points.add(
//                HeartRateGraphView.HeartRatePoint(
//                    now + i * 60_000L, // each minute
//                    (60..90).random()
//                )
//            )
//        }
//
//        // Update graph with new data
//        binding.heartRateVariablityGraph.setData(points)
//
//        // Set threshold line (e.g., safe HR zone)
//        binding.heartRateVariablityGraph.thresholdValue = 74
//
//        // Example: fix Y-axis and time range manually
//        binding.heartRateVariablityGraph.setYAxisRange(40, 90)
//        binding.heartRateVariablityGraph.setTimeRange(now, now + 600_000L)
//
//        // If you want to restore automatic scaling later:
//        binding.heartRateVariablityGraph.resetDynamicAxes()
//    }
//
//    // -------------------------------------------
//    // Sleep cycle graph setup (uses SleepCycleView.parseFromJson)
//    // -------------------------------------------
//    private fun setupSleepCycleGraph() {
//        val sleepManager = SleepCycleView()
//        val cyclesData = sleepManager.parseFromJson()
//
//        // Create time labels programmatically
//        val timeContainer = LinearLayout(requireContext()).apply {
//            layoutParams = LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT
//            )
//            orientation = LinearLayout.HORIZONTAL
//        }
//
//        val timeLabels = mutableListOf<TextView>()
//        val gravities = listOf(
//            Gravity.START,
//            Gravity.CENTER,
//            Gravity.CENTER,
//            Gravity.CENTER,
//            Gravity.END
//        )
//
//        for (i in 0..4) {
//            val tv = TextView(requireContext()).apply {
//                layoutParams =
//                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
//                textSize = 11f
//                setTextColor(Color.parseColor("#999999"))
//                gravity = gravities[i]
//            }
//            timeLabels.add(tv)
//            timeContainer.addView(tv)
//        }
//
//        // Add time container below sleep graph (adjust based on your parent layout)
//        // binding.yourParentLayout.addView(timeContainer)
//
//        sleepManager.setupSleepCycleGraph(
//            context = requireContext(),
//            sleepGraphContainer = binding.sleepGraph,
//            tvSleepCycleCount = binding.tvSleepCycleCount,
//            cyclesData = cyclesData,
//            timeLabels = timeLabels
//        )
//        binding.tvLegend.text = "${cyclesData.fullCount} Full / ${cyclesData.partialCount} Partial"
//    }
//
//    // -------------------------------------------
//    // Alternate Sleep graph builder (keeps original)
//    // -------------------------------------------
//    private fun setupSleepCycleGrapha() {
//        val sleepSegments = listOf(
//            SleepSegment("full", 2f),
//            SleepSegment("partial", 1f),
//            SleepSegment("full", 3f),
//            SleepSegment("partial", 1.5f),
//            SleepSegment("full", 2f)
//        )
//
//        val movements =
//            listOf(true, false, true, true, false, true, false, false, true, true)
//
//        binding.tvSleepCycleCount.text = "Sleep Cycle ${sleepSegments.size}"
//
//        binding.sleepGraph.removeAllViews()
//        binding.movementRow.removeAllViews()
//
//        // Build sleep bars
//        for (segment in sleepSegments) {
//            val bar = View(requireContext())
//            val params = LinearLayout.LayoutParams(0, 30.dp, segment.durationWeight)
//            params.setMargins(2.dp, 0, 2.dp, 0)
//            bar.layoutParams = params
//            bar.background = when (segment.type) {
//                "full" -> createBarDrawable("#5AE3B1", "#A0E8D0")
//                else -> createBarDrawable("#E0E0E0", "#F5F5F5")
//            }
//            binding.sleepGraph.addView(bar)
//        }
//
//        // Build movement indicators
//        for (move in movements) {
//            val bar = View(requireContext())
//            val params = LinearLayout.LayoutParams(3.dp, LinearLayout.LayoutParams.MATCH_PARENT)
//            params.setMargins(1.dp, 0, 1.dp, 0)
//            bar.layoutParams = params
//            bar.background = GradientDrawable().apply {
//                shape = GradientDrawable.RECTANGLE
//                cornerRadius = 2.dp.toFloat()
//                setColor(if (move) 0xFF4BA3FF.toInt() else 0xFFD0D0D0.toInt())
//            }
//            binding.movementRow.addView(bar)
//        }
//
//        // Optional icons (Bed + Alarm)
//        val bedIcon = ImageView(requireContext()).apply {
//            setImageResource(R.drawable.ic_graph)
//            layoutParams = LinearLayout.LayoutParams(20.dp, 20.dp)
//        }
//        val alarmIcon = ImageView(requireContext()).apply {
//            setImageResource(R.drawable.ic_mic)
//            layoutParams = LinearLayout.LayoutParams(20.dp, 20.dp)
//        }
//
//        binding.movementRow.addView(bedIcon, 0)
//        binding.movementRow.addView(alarmIcon)
//    }
//
//    // -------------------------------------------
//    // Create gradient drawable for bars
//    // -------------------------------------------
//    private fun createBarDrawable(startColor: String, endColor: String): GradientDrawable {
//        return GradientDrawable(
//            GradientDrawable.Orientation.LEFT_RIGHT,
//            intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor))
//        ).apply {
//            cornerRadius = 8.dp.toFloat()
//        }
//    }
//
//    // -------------------------------------------
//    // Chart Builder for SleepEntry data
//    // -------------------------------------------
//    @RequiresApi(Build.VERSION_CODES.O)
//    @SuppressLint("SetTextI18n")
//    private fun setData(entries: List<SleepEntry>) {
//        binding.barsContainer.removeAllViews()
//        if (entries.isEmpty()) return
//
//        val maxValue = entries.maxOf { it.value }
//        val avg = entries.map { it.value }.average().toInt()
//
//        binding.tvScore.text = avg.toString()
//        binding.tvLabel.text = "Sleep"
//
//        // Wait for container height to be ready
//        binding.barsContainer.post {
//            val containerHeight = binding.barsContainer.height
//
//            // Calculate maximum bar height (leaving space for bubble and labels)
//            val maxBarHeight = containerHeight - 60.dp // Space for bubble (24dp) + baseline (4dp) + label (20dp) + padding
//
//            entries.forEach { entry ->
//                // Calculate proportional fill height based on value
//                val fillRatio = entry.value.toFloat() / maxValue.toFloat()
//                val fillHeight = (maxBarHeight * fillRatio).toInt()
//
//                val barLayout = LinearLayout(requireContext()).apply {
//                    orientation = LinearLayout.VERTICAL
//                    gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
//                    layoutParams = LinearLayout.LayoutParams(
//                        0,
//                        LinearLayout.LayoutParams.MATCH_PARENT,
//                        1f
//                    ).apply {
//                        // Add horizontal spacing between bars
//                        marginStart = 0.dp
//                        marginEnd = 0.dp
//                    }
//                }
//
//                // Spacer to push bar to bottom
//                val spacer = View(requireContext()).apply {
//                    layoutParams = LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.MATCH_PARENT,
//                        0,
//                        1f
//                    )
//                }
//
//                val barContainer = FrameLayout(requireContext()).apply {
//                    layoutParams = LinearLayout.LayoutParams(
//                        24.dp,
//                        fillHeight - 30.dp // Bar height + bubble height
//                    )
//                    setPadding(0, 0, 0, 0)
//                }
//
//                // Background track (semi-transparent white)
//                val trackView = View(requireContext()).apply {
//                    layoutParams = FrameLayout.LayoutParams(
//                        24.dp,
//                        fillHeight,
//                        Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
//                    )
//                    background = GradientDrawable().apply {
//                        setColor(Color.parseColor("#40FFFFFF"))
//                        cornerRadius = 12.dp.toFloat()
//                    }
//                }
//
//                // Fill view (white)
//                val fillView = View(requireContext()).apply {
//                    layoutParams = FrameLayout.LayoutParams(
//                        4.dp,
//                        fillHeight,
//                        Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
//                    )
//                    background = GradientDrawable().apply {
//                        setColor(Color.WHITE)
//                        cornerRadius = 2.dp.toFloat()
//                    }
//                }
//
//                // Value bubble at top of bar
//                val bubble = TextView(requireContext()).apply {
//                    layoutParams = FrameLayout.LayoutParams(
//                        24.dp,
//                        24.dp,
//                        Gravity.TOP or Gravity.CENTER_HORIZONTAL
//                    )
//                    text = entry.value.toString()
//                    setTextColor(Color.parseColor("#0A84FF"))
//                    textSize = 11f
//                    typeface = Typeface.DEFAULT_BOLD
//                    gravity = Gravity.CENTER
//                    background = GradientDrawable().apply {
//                        setColor(Color.WHITE)
//                        shape = GradientDrawable.OVAL
//                    }
//                    elevation = 2.dp.toFloat()
//                }
//
//                // Weekday bubble at bottom of bar
//                val calendar = Calendar.getInstance()
//                calendar.add(Calendar.DAY_OF_MONTH, -(entries.size - 1 - entries.indexOf(entry)))
//                val weekdayInitial = when (calendar.get(Calendar.DAY_OF_WEEK)) {
//                    Calendar.SUNDAY -> "S"
//                    Calendar.MONDAY -> "M"
//                    Calendar.TUESDAY -> "T"
//                    Calendar.WEDNESDAY -> "W"
//                    Calendar.THURSDAY -> "T"
//                    Calendar.FRIDAY -> "F"
//                    Calendar.SATURDAY -> "S"
//                    else -> ""
//                }
//
//                val weekdayBubble = TextView(requireContext()).apply {
//                    layoutParams = FrameLayout.LayoutParams(
//                        20.dp,
//                        20.dp,
//                        Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
//                    )
//                    text = weekdayInitial
//                    setTextColor(Color.parseColor("#0A84FF"))
//                    textSize = 10f
//                    typeface = Typeface.DEFAULT_BOLD
//                    gravity = Gravity.CENTER
//                    background = GradientDrawable().apply {
//                        setColor(Color.WHITE)
//                        shape = GradientDrawable.OVAL
//                    }
//                    elevation = 1.dp.toFloat()
//                }
//
//                // Horizontal baseline (full width)
//                val baseLine = View(requireContext()).apply {
//                    layoutParams = LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.MATCH_PARENT,
//                        2.dp
//                    ).apply {
//                        topMargin = 6.dp
//                        bottomMargin = 6.dp
//                    }
//                    background = GradientDrawable().apply {
//                        setColor(Color.parseColor("#40FFFFFF"))
//                    }
//                }
//
//                // Date label
//                val dayMonth = "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}"
//
//                val dayLabel = TextView(requireContext()).apply {
//                    layoutParams = LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.WRAP_CONTENT,
//                        LinearLayout.LayoutParams.WRAP_CONTENT
//                    )
//                    text = dayMonth
//                    setTextColor(Color.parseColor("#80FFFFFF"))
//                    textSize = 10f
//                    gravity = Gravity.CENTER
//                }
//                barContainer.addView(trackView)
//                barContainer.addView(fillView)
//                barContainer.addView(bubble)
//                barContainer.addView(weekdayBubble)
//                barLayout.addView(spacer)
//                barLayout.addView(barContainer)
//                barLayout.addView(baseLine)
//                barLayout.addView(dayLabel)
//                binding.barsContainer.addView(barLayout)
//            }
//        }
//    }
//
//    // -------------------------------------------
//    // Utilities / Extensions
//    // -------------------------------------------
//    val Int.dp: Int
//        get() = (this * Resources.getSystem().displayMetrics.density).toInt()
//}