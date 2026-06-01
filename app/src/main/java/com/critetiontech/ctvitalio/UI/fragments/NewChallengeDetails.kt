package com.critetiontech.ctvitalio.UI.fragments

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentNewChallengeBinding
import com.critetiontech.ctvitalio.databinding.FragmentNewChallengeDetailsBinding
import okhttp3.Challenge
import androidx.core.graphics.toColorInt
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.model.DashboardActiveChallenges
import com.critetiontech.ctvitalio.viewmodel.ChallengesViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.getValue

class NewChallengeDetails : Fragment() {

    private var _binding: FragmentNewChallengeDetailsBinding? = null
    private val binding get() = _binding!!
    var isGraphLoaded = false
    private var challenge: DashboardActiveChallenges? = null

    private  val challengesViewModel: ChallengesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewChallengeDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        challenge?.let { bindAllData(it) }

        binding.wellnessImageArrow.setOnClickListener {

            findNavController().popBackStack()
        }
        arguments?.let {
            challenge = it.getSerializable("challenges") as DashboardActiveChallenges?

        }
        challengesViewModel.getJoinedChallengesDetailsByEmployeeId(challenge?.challengeId.toString())

        binding.titleText.text= challenge?.title.toString()
        binding.titleText.text= challenge?.title.toString()
        binding.discriptions.text =
            Html.fromHtml(challenge?.description ?: "", Html.FROM_HTML_MODE_LEGACY)

        binding.progressBar.progress = challenge?.progress?.toInt() ?:0
        binding.labelCurrent.text = "Progress "+ challenge?.progress.toString()+"%"
        Log.d("ChallengeData", "challenge = ${challenge?.challengeId}")

        challengesViewModel?.challengesDetails?.observe(viewLifecycleOwner) { response ->
            binding.dayLeftId.text=response.dayProgress

            if (isGraphLoaded) return@observe

            val graphJson = response?.graphData ?: "[]"

            val type = object : TypeToken<List<GraphItem>>() {}.type
            val graphList: List<GraphItem> = Gson().fromJson(graphJson, type)

            if (graphList.isNullOrEmpty()) return@observe

            isGraphLoaded = true

            // ✅ GROUP FIX
            val groupedList = graphList
                .groupBy { it.logDate.substring(0, 10) }
                .map { it.value.last() }
                .sortedBy { it.logDate }

            val glucoseValues = groupedList.mapNotNull { it.value.toInt() }

            val dayLabels = groupedList.map {
                it.logDate.substring(5).replace("-", "/")
            }

            val count = minOf(glucoseValues.size, dayLabels.size)

            val finalValues = glucoseValues.take(count)
            val finalDates = dayLabels.take(count)

            val glucoseRangeText =
                "${finalValues.minOrNull()}–${finalValues.maxOrNull()}"

            val daysFraction =
                "${finalValues.size}/${response?.duration ?: 0} Days"

            bindGlucoseDataWithAxis(
                rootLayout = binding.rootLayout,
                glucoseRange = glucoseRangeText,
                daysLeftText = "",
                daysFractionText = daysFraction,
                dayValues = finalValues,
                dayDates = finalDates
            )
        }
    }


    fun bindGlucoseDataWithAxis(
        rootLayout: View,
        glucoseRange: String,
        daysLeftText: String,
        daysFractionText: String,
        dayValues: List<Int>,
        dayDates: List<String>
    ) {
        val tvGlucoseRange = rootLayout.findViewById<TextView>(R.id.tvGlucoseRange)
        val tvDaysLeft = rootLayout.findViewById<TextView>(R.id.tvDaysLefts)
        val tvTitle = rootLayout.findViewById<TextView>(R.id.tvTitle)
        val barChartContainer = rootLayout.findViewById<LinearLayout>(R.id.barChartContainer)
        val xAxisDates = rootLayout.findViewById<LinearLayout>(R.id.xAxisDates)
        val yAxisContainer = rootLayout.findViewById<LinearLayout>(R.id.yAxisContainer)
        val centerLine = rootLayout.findViewById<View>(R.id.centerLine)

        tvGlucoseRange.text = glucoseRange
        tvDaysLeft.text = daysFractionText
        tvTitle.text = "Glucose Stability Status"

        barChartContainer.removeAllViews()
        xAxisDates.removeAllViews()
        yAxisContainer.removeAllViews()

        val minValue = 40
        val maxValue = 140
        val maxBarHeightDp = 100f

        fun dpToPx(dp: Float) =
            (dp * rootLayout.resources.displayMetrics.density).toInt()

        // current date
        val today = java.text.SimpleDateFormat(
            "MM/dd",
            java.util.Locale.getDefault()
        ).format(java.util.Date())

        // Y axis
        val midValue = (minValue + maxValue) / 2
        val yLabels = listOf(maxValue, midValue, minValue)

        yLabels.forEach { value ->
            val yText = TextView(rootLayout.context).apply {
                text = value.toString()
                textSize = 10f
                setTextColor(Color.parseColor("#777777"))
                gravity = Gravity.END
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            }
            yAxisContainer.addView(yText)
        }

        // bars
        dayValues.forEachIndexed { index, value ->

            val clampedValue = value.coerceIn(minValue, maxValue)
            val heightRatio =
                (clampedValue - minValue).toFloat() / (maxValue - minValue)

            val barHeightPx =
                (heightRatio * dpToPx(maxBarHeightDp)).toInt()

            val date = dayDates.getOrNull(index)

            val barView = View(rootLayout.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    barHeightPx,
                    1f
                ).apply {
                    setMargins(dpToPx(4f), 0, dpToPx(4f), 0)
                }

                // highlight only today
                setBackgroundColor(
                    if (date == today)
                        Color.parseColor("#00D492")
                    else
                        Color.parseColor("#EAF4FF")
                )
            }

            barChartContainer.addView(barView)

            // dates
            val dateTextView = TextView(rootLayout.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    setMargins(dpToPx(4f), 0, dpToPx(4f), 0)
                }

                text = date ?: ""
                textSize = 10f
                setTextColor(Color.parseColor("#AAAAAA"))
                gravity = Gravity.CENTER
            }

            xAxisDates.addView(dateTextView)
        }

        // center line
        centerLine.post {
            val barContainerHeight = barChartContainer.height
            val midRatio =
                (midValue - minValue).toFloat() / (maxValue - minValue)

            val yPos =
                barContainerHeight - (midRatio * barContainerHeight)

            centerLine.y = yPos - centerLine.height / 2f
        }
    }

//    private fun bindAllData(challenge: Challenge) {
//        // ---- 1️⃣ Title & Subtitle ----
//        binding.titleText.text = challenge.title ?: "No Title"
//        binding.subtitleText.text = "Duration: ${challenge.duration ?: 0} days"
//
//        // ---- 2️⃣ Progress ----
//        val progress = challenge.progress?.toInt() ?: 0
//        binding.progressBar.progress = progress
//        binding.labelCurrent.text = "Progress $progress%"
//
//        // ---- 3️⃣ Reminder / Streak ----
//        val streakMessage = challenge.streakMessage ?: "Complete your streak!"
//        binding.reminderText.text = streakMessage
//        binding.tvConsistencyWins.text = streakMessage
//
//        // ---- 4️⃣ Streak Days ----
//        val streakDays = challenge.streakDays ?: ""
//        val dayViews = listOf(
//            binding.tvMonday,
//            binding.tvTuesday,
//            binding.tvWednesday,
//            binding.tvThursday,
//            binding.tvFriday,
//            binding.tvFireIcon,
//            binding.tvSaturday
//        )
//        bindStreakDays(streakDays, dayViews)
//
//        // ---- 5️⃣ Buttons ----
//        binding.btnLogReading.setOnClickListener {
//            // TODO: Navigate to log reading screen
//        }
//        binding.btnMessageCoach.setOnClickListener {
//            // TODO: Navigate to chat screen
//        }
//    }

    private fun bindStreakDays(streakDays: String, dayViews: List<TextView>) {
        dayViews.forEach {
            it.setTextColor(Color.parseColor("#999999"))
            it.background = null
            it.textSize = 14f
        }

        if (streakDays.isEmpty()) return

        val activeDays = streakDays.split(",").map { it.trim().uppercase() }
        val dayMap = listOf("M", "T", "W", "T", "F", "🔥", "S")

        dayViews.forEachIndexed { index, textView ->
            val dayKey = dayMap.getOrNull(index) ?: return@forEachIndexed

            if (dayKey != "🔥" && activeDays.contains(dayKey)) {
                textView.setTextColor(Color.parseColor("#FF6B35"))
                textView.background = createDayBg("#FFE5D9")
            }

            if (dayKey == "🔥" && activeDays.isNotEmpty()) {
                textView.setTextColor(Color.parseColor("#FF6B35"))
                textView.background = createDayBg("#FFE5D9")
                textView.textSize = 18f
            }
        }
    }

    private fun createDayBg(color: String) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = 20f
        setColor(Color.parseColor(color))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class GraphItem(
    val logDate: String,
    val value: Double
)