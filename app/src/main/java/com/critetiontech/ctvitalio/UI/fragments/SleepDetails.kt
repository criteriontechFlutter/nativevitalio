package com.critetiontech.ctvitalio.UI.fragments

import HrData
import HrvGraph
import android.annotation.SuppressLint
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.UI.constructorFiles.HeartRateGraphView
import com.critetiontech.ctvitalio.databinding.ActivityForgotPasswordBinding
import com.critetiontech.ctvitalio.databinding.FragmentEnergyTankBinding
import com.critetiontech.ctvitalio.databinding.FragmentSleepDetailsBinding
import com.critetiontech.ctvitalio.databinding.IncludeProgressCardBinding
import com.critetiontech.ctvitalio.model.SleepCycleView
import com.critetiontech.ctvitalio.utils.LoaderUtils.hideLoading
import com.critetiontech.ctvitalio.utils.LoaderUtils.showLoading
import com.critetiontech.ctvitalio.viewmodel.ChallengesViewModel
import com.critetiontech.ctvitalio.viewmodel.DashboardViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.renderer.YAxisRenderer
import java.nio.file.Path
import java.time.Instant
import java.util.Calendar

data class SleepEntry(val day: Int, val value: Int)
data class SleepSegment(val type: String, val durationWeight: Float)

class SleepDetails : Fragment() {

    private var _binding: FragmentSleepDetailsBinding? = null
    private val binding get() = _binding!!

    private val sleepManager = SleepCycleView()
    private lateinit var viewModel: DashboardViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSleepDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // ✅ Avoid memory leaks
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) showLoading() else hideLoading()
        }
         // ✅ Example dataset for bar chart
        val calendar = Calendar.getInstance()
        viewModel.getVitals()
        val data = listOf(
            SleepEntry(calendar.get(Calendar.DAY_OF_MONTH), 100),
            SleepEntry(calendar.get(Calendar.DAY_OF_MONTH) + 1, 56),
            SleepEntry(calendar.get(Calendar.DAY_OF_MONTH) + 2, 54),
            SleepEntry(calendar.get(Calendar.DAY_OF_MONTH) + 3, 72),
            SleepEntry(calendar.get(Calendar.DAY_OF_MONTH) + 4, 58),
            SleepEntry(calendar.get(Calendar.DAY_OF_MONTH) + 5, 66),
            SleepEntry(calendar.get(Calendar.DAY_OF_MONTH), 70)
        )

        setData(data)// Build main chart

        binding.wellnessImageArrow.setOnClickListener {
            findNavController().popBackStack()
        }

        // ✅ Progress cards
        setDefaultProgress(binding.sleepEfficiencyProgressId)
        setDefaultProgress(binding.tempProgressId)
        setDefaultProgress(binding.restfulnessProgressId)
        setDefaultProgress(binding.totalSleepProgressId)
        setDefaultProgress(binding.hrProgress)
        setDefaultProgress(binding.restorativeSleepProgressId)
        viewModel.sleepValueList.observe(viewLifecycleOwner) { sleepValue  ->
        hrFromServerData(sleepValue.HrGraph.Data)
            hrVariability(sleepValue.HrvGraph )
           // binding.tvAvgValue.text=sleepValue.GistObject.Avg.toString()
//            binding.tvMaxValue.text=sleepValue.GistObject.Max.toString()
//            binding.tvMinValue.text=sleepValue.GistObject.Min.toString()


        }
        // ✅ Add the dynamic sleep cycle chart
        setupSleepCycleGraph()
        hrVariability()

        bindContributorsData()
        setupChart()
        openSleepGraph()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun hrVariability(hrvGraph: HrvGraph) {

        // Convert API data → graph points
        val points = hrvGraph.Data?.map { item ->
            HeartRateGraphView.HeartRatePoint(
                Instant.parse(item.Timestamp).toEpochMilli(),
                item.Value.toInt()
            )
        } ?: emptyList()

        if (points.isEmpty()) return

        // 🔵 Bind data to the graph
        binding.heartRateVariablityGraph.setData(points)
        binding.heartRateVariablityGraph.graphTile   = "Lower Heart Rate"

        // 🔵 Optional: threshold line
//        binding.heartRateVariablityGraph.thresholdValue = 74

        // 🔵 Auto-calc Y range
        val minValue = points.minOf { it.bpm } - 5
        val maxValue = points.maxOf { it.bpm } + 5
        binding.heartRateVariablityGraph.setYAxisRange(minValue, maxValue)

        // 🔵 Time range from first to last point
        binding.heartRateVariablityGraph.setTimeRange(
            points.first().timestamp,
            points.last().timestamp
        )

        // 🔵 Let graph scale dynamically again
        binding.heartRateVariablityGraph.resetDynamicAxes()
    }
private fun openSleepGraph() {
    childFragmentManager.beginTransaction()
        .replace(R.id.sleepGraph, SleepGraphFragment())
        .commit()

    }
    private fun setDefaultProgress(card: IncludeProgressCardBinding) {
        card.sleepProgressBar.progress = 1
        card.cardTitle.text = "--"
        card.Title.text = "--"
    }

    @SuppressLint("SuspiciousIndentation")
    private fun bindContributorsData() {

        viewModel.sleepValueList.observe(viewLifecycleOwner) { sleepValue  ->

        val totalSleep = sleepValue.QuickMetricsTiled
            ?.firstOrNull { it.Title.equals("TOTAL SLEEP", ignoreCase = true) }

         binding.totalSleepIds.value .text= HtmlCompat.fromHtml(totalSleep?.Value.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)

            val restorative = sleepValue.QuickMetricsTiled
                ?.firstOrNull { it.Title.equals("RESTORATIVE SLEEP", ignoreCase = true) }
            binding.restorativeSleepId.value.text==HtmlCompat.fromHtml(restorative?.Value.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)

            val timeinBed = sleepValue.QuickMetricsTiled
                ?.firstOrNull { it.Title.equals("TIME IN BED", ignoreCase = true) }
             binding.timeInBedId.value.text=HtmlCompat.fromHtml(timeinBed?.Value.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)

        }
            viewModel.sleepsummary.observe(viewLifecycleOwner) { list ->
                val timeinBed =list
                    ?.firstOrNull { it.Title.equals("TIME IN BED", ignoreCase = true) }
                binding.timeInBedId.value.text=HtmlCompat.fromHtml (timeinBed?.Score.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY) }
              val cardMap = mapOf(
            "Sleep Efficiency" to binding.sleepEfficiencyProgressId,
            "Temperature" to binding.tempProgressId,
            "Restfulness" to binding.restfulnessProgressId,
            "Total Sleep" to binding.totalSleepProgressId,
            "HR Drop" to binding.hrProgress,
            "Restorative Sleep" to binding.restorativeSleepProgressId
             )

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


    private fun setupChart() {
        val alertnessMin = 70
        val height = "170 cm"
        val weight = "70 kg"

        // Bind simple text values
        binding.tvAlertnessValue.text = "$alertnessMin min"
        val entries = listOf(
            BarEntry(0f, 60f),
            BarEntry(1f, 85f),
            BarEntry(2f, 110f),
            BarEntry(3f, 95f),
            BarEntry(4f, 120f),
            BarEntry(5f, 130f),
            BarEntry(6f, 160f)
        )

        val dataSet = BarDataSet(entries, "")
        dataSet.color = Color.parseColor("#EAF4FF")      // unselected
        dataSet.highLightColor = Color.parseColor("#77B7FF") // selected
        dataSet.highLightAlpha = 255
        dataSet.valueTextColor = Color.TRANSPARENT

        val barData = BarData(dataSet)
        barData.barWidth = 0.3f
        val maxY = 200f           // example: your chart max
        val target20 = maxY * 0.20f
        binding.alertnessBarChart.data = barData
        binding.alertnessBarChart.description.isEnabled = false
        binding.alertnessBarChart.legend.isEnabled = false
        binding.alertnessBarChart.axisLeft.axisMinimum = 30f
        binding.alertnessBarChart.scrollBarSize=0
        binding.alertnessBarChart.axisRight.isEnabled = false
        binding.alertnessBarChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.alertnessBarChart.xAxis.setDrawGridLines(false)
        binding.alertnessBarChart.axisLeft.setDrawGridLines(false)
        binding.alertnessBarChart.rendererLeftYAxis =
            object : YAxisRenderer(
                binding.alertnessBarChart.viewPortHandler,
                binding.alertnessBarChart.axisLeft,
                binding.alertnessBarChart.getTransformer(YAxis.AxisDependency.LEFT)
            ) {

                override fun renderGridLines(c: Canvas?) {
                    super.renderGridLines(c)

                    val target = mYAxis.mAxisMaximum * 0.30f
                    val transformer = binding.alertnessBarChart.getTransformer(YAxis.AxisDependency.LEFT)
                    val targetY = transformer.getPixelForValues(0f, target)

                    val rect = RectF(
                        binding.alertnessBarChart.viewPortHandler.contentLeft(),
                        targetY.y.toFloat(),
                        binding.alertnessBarChart.viewPortHandler.contentRight(),
                        binding.alertnessBarChart.viewPortHandler.contentBottom()
                    )

                    val paint = Paint().apply {
                        color = Color.parseColor("#1A00D492") // light blue transparent
                        style = Paint.Style.FILL
                    }

                    c?.drawRect(rect, paint)
                }
            }
        binding.alertnessBarChart.invalidate()
    }


//    @SuppressLint("SetTextI18n")
//    private fun hr() {
//        val now = System.currentTimeMillis()
//        val points = mutableListOf<HeartRateGraphView.HeartRatePoint>()
//
//        // ✅ Generate 10 dynamic random BPM points, 1 min apart
//        for (i in 0..10) {
//            points.add(
//                HeartRateGraphView.HeartRatePoint(
//                    now + i * 60_000L, // each minute
//                    (60..130).random()
//                )
//            )
//        }
//
//        // ✅ Update graph with new data
//        binding.heartRateGraph.setData(points)
//
//        // ✅ Set threshold line (e.g., safe HR zone)
//        binding.heartRateGraph.thresholdValue = 100
//
//        // ✅ Example: fix Y-axis and time range manually
//        binding.heartRateGraph.setYAxisRange(40, 180)
//        binding.heartRateGraph.setTimeRange(now, now + 600_000L)
//
//        // ✅ If you want to restore automatic scaling later:
//        binding.heartRateGraph.resetDynamicAxes()
//    }
@RequiresApi(Build.VERSION_CODES.O)
fun isoToMillis(isoDate: String): Long {
    return Instant.parse(isoDate).toEpochMilli()
}
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("SetTextI18n")
private fun hrFromServerData(hrList: List<HrData>) {

    // Convert data to HeartRatePoint list
    val points = hrList.map { item ->
        HeartRateGraphView.HeartRatePoint(
            isoToMillis(item.Timestamp),
            item.Value.toInt()
        )
    }

    // Set data to graph
    binding.heartRateGraph.setData(points)

    binding.heartRateVariablityGraph.graphTile   = "HRV Zone"
    // Set threshold line
    binding.heartRateGraph.thresholdValue = 84

    // Fix Y-axis range (optional)
    binding.heartRateGraph.setYAxisRange(40, 120)

    // Fix time range (optional)
    binding.heartRateGraph.setTimeRange(
        points.first().timestamp,
        points.last().timestamp
    )

    // Restore dynamic scaling later if needed
    // binding.heartRateGraph.resetDynamicAxes()
}
    @SuppressLint("SetTextI18n")
    private fun hrVariability() {
        val now = System.currentTimeMillis()
        val points = mutableListOf<HeartRateGraphView.HeartRatePoint>()

        // ✅ Generate 10 dynamic random BPM points, 1 min apart
        for (i in 0..10) {
            points.add(
                HeartRateGraphView.HeartRatePoint(
                    now + i * 60_000L, // each minute
                    (60..90).random()
                )
            )
        }

        // ✅ Update graph with new data
        binding.heartRateVariablityGraph.setData(points)

        // ✅ Set threshold line (e.g., safe HR zone)
        binding.heartRateVariablityGraph.thresholdValue = 74

        // ✅ Example: fix Y-axis and time range manually
        binding.heartRateVariablityGraph.setYAxisRange(40, 90)
        binding.heartRateVariablityGraph.setTimeRange(now, now + 600_000L)

        // ✅ If you want to restore automatic scaling later:
        binding.heartRateVariablityGraph.resetDynamicAxes()
    }
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
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
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
    // 💤 Dynamic Sleep Cycle Section (from your "freg" function)
    private fun setupSleepCycleGrapha() {
        val sleepSegments = listOf(
            SleepSegment("full", 2f),
            SleepSegment("partial", 1f),
            SleepSegment("full", 3f),
            SleepSegment("partial", 1.5f),
            SleepSegment("full", 2f)
        )

        val movements = listOf(true, false, true, true, false, true, false, false, true, true)

        binding.tvSleepCycleCount.text = "Sleep Cycle ${sleepSegments.size}"

        binding.sleepGraph.removeAllViews()
        binding.movementRow.removeAllViews()

        // ✅ Build sleep bars
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

        // ✅ Build movement indicators
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

        // ✅ Optional icons (Bed + Alarm)
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

    // 🧱 Utility: create gradient bar
    private fun createBarDrawable(startColor: String, endColor: String): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor))
        ).apply {
            cornerRadius = 8.dp.toFloat()
        }
    }

    // 🧮 Utility: convert dp to px

    // 📊 Chart Builder for SleepEntry data
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


    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    // Data class
    data class SleepEntry(val day: Int, val value: Int)

}