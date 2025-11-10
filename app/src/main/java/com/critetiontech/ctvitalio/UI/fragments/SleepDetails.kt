package com.critetiontech.ctvitalio.UI.fragments

import android.annotation.SuppressLint
import com.critetiontech.ctvitalio.R

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
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
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil.setContentView
import androidx.fragment.app.Fragment
import com.critetiontech.ctvitalio.UI.constructorFiles.HeartRateGraphView
import com.critetiontech.ctvitalio.databinding.ActivityForgotPasswordBinding
import com.critetiontech.ctvitalio.databinding.FragmentEnergyTankBinding
import com.critetiontech.ctvitalio.databinding.FragmentSleepDetailsBinding

data class SleepEntry(val day: Int, val value: Int)
data class SleepSegment(val type: String, val durationWeight: Float)

class SleepDetails : Fragment() {

    private var _binding: FragmentSleepDetailsBinding? = null
    private val binding get() = _binding!!

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

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Example dataset for bar chart
        val data = listOf(
            SleepEntry(12, 62),
            SleepEntry(13, 56),
            SleepEntry(14, 54),
            SleepEntry(15, 62),
            SleepEntry(16, 58),
            SleepEntry(17, 66)
        )

        setData(data) // Build main chart

        // ✅ Example stats
        binding.totalSleepIds.title.text = "Total Sleep"
        binding.totalSleepIds.value.text = "7h 12m"
        binding.totalSleepIds.status.text = "Optimal"

        binding.timeInBedId.title.text = "Time in Bed"
        binding.timeInBedId.value.text = "8h 02m"
        binding.timeInBedId.status.text = "Good"

        binding.restorativeSleepId.title.text = "Restorative Sleep"
        binding.restorativeSleepId.value.text = "5h 32m"
        binding.restorativeSleepId.status.text = "Optimal"

        binding.hr.title.text = "HR Drop"
        binding.hr.value.text = "68%"
        binding.hr.status.text = "Fair"

        // ✅ Progress cards
        binding.sleepEfficiencyProgressId.sleepProgressBar.progress = 41
        binding.sleepEfficiencyProgressId.cardTitle.text = "Sleep Efficiency"
        binding.sleepEfficiencyProgressId.Title.text = "Need Attention"

        binding.tempProgressId.sleepProgressBar.progress = 87
        binding.tempProgressId.cardTitle.text = "Temperature"
        binding.tempProgressId.Title.text = "Elevated"

        binding.restfulnessProgressId.sleepProgressBar.progress = 41
        binding.restfulnessProgressId.cardTitle.text = "Restfulness"
        binding.restfulnessProgressId.Title.text = "Need Attention"

        binding.totalSleepProgressId.sleepProgressBar.progress = 74
        binding.totalSleepProgressId.cardTitle.text = "Total Sleep"
        binding.totalSleepProgressId.Title.text = "Need Attention"

        binding.hrProgress.sleepProgressBar.progress = 41
        binding.hrProgress.cardTitle.text = "HR Drop"
        binding.hrProgress.Title.text = "Need Attention"

        binding.restorativeSleepProgressId.sleepProgressBar.progress = 32
        binding.restorativeSleepProgressId.cardTitle.text = "Restorative Sleep"
        binding.restorativeSleepProgressId.Title.text = "Need Attention"
        hr()
        // ✅ Add the dynamic sleep cycle chart
        setupSleepCycleGraph()
        hrVariability()
    }



    @SuppressLint("SetTextI18n")
    private fun hr() {
        val now = System.currentTimeMillis()
        val points = mutableListOf<HeartRateGraphView.HeartRatePoint>()

        // ✅ Generate 10 dynamic random BPM points, 1 min apart
        for (i in 0..10) {
            points.add(
                HeartRateGraphView.HeartRatePoint(
                    now + i * 60_000L, // each minute
                    (60..130).random()
                )
            )
        }

        // ✅ Update graph with new data
        binding.heartRateGraph.setData(points)

        // ✅ Set threshold line (e.g., safe HR zone)
        binding.heartRateGraph.thresholdValue = 100

        // ✅ Example: fix Y-axis and time range manually
        binding.heartRateGraph.setYAxisRange(40, 180)
        binding.heartRateGraph.setTimeRange(now, now + 600_000L)

        // ✅ If you want to restore automatic scaling later:
        binding.heartRateGraph.resetDynamicAxes()
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

    // 💤 Dynamic Sleep Cycle Section (from your "freg" function)
    private fun setupSleepCycleGraph() {
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
    private val Int.dp get() = (this * resources.displayMetrics.density).toInt()

    // 📊 Chart Builder for SleepEntry data
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

            entries.forEach { entry ->
                val fillHeight = (containerHeight * (entry.value / maxValue.toFloat())).toInt()

                val barLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                }

                val barContainer = FrameLayout(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(24.dp, fillHeight)
                    background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_bar_track)
                }

                val fillView = View(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(4.dp, fillHeight, Gravity.CENTER_HORIZONTAL)
                    background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_bar_fill)
                }

                val bubble = TextView(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(24.dp, 24.dp, Gravity.TOP or Gravity.CENTER_HORIZONTAL)
                    text = entry.value.toString()
                    setTextColor(Color.parseColor("#0A84FF"))
                    textSize = 12f
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_circle_value)
                }

                val baseLine = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 4.dp)
                    background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_bar_fill)
                }

                val dayLabel = TextView(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    text = entry.day.toString()
                    setTextColor(Color.BLACK)
                    textSize = 12f
                    gravity = Gravity.CENTER
                }

                barContainer.addView(fillView)
                barContainer.addView(bubble)
                barLayout.addView(barContainer)
                barLayout.addView(baseLine)
                barLayout.addView(dayLabel)

                binding.barsContainer.addView(barLayout)
            }
        }
    }
}