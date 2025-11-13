package com.critetiontech.ctvitalio.UI.fragments

import android.annotation.SuppressLint
import android.content.res.Resources
import com.critetiontech.ctvitalio.R

import android.graphics.Color
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
import androidx.databinding.DataBindingUtil.setContentView
import androidx.fragment.app.Fragment
import com.critetiontech.ctvitalio.UI.constructorFiles.HeartRateGraphView
import com.critetiontech.ctvitalio.databinding.ActivityForgotPasswordBinding
import com.critetiontech.ctvitalio.databinding.FragmentEnergyTankBinding
import com.critetiontech.ctvitalio.databinding.FragmentSleepDetailsBinding
import java.util.Calendar

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

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Example dataset for bar chart
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

        setData(data)// Build main chart

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
                        marginStart = 4.dp
                        marginEnd = 4.dp
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