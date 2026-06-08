package com.critetiontech.ctvitalio.UI.fragments

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Html
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentNewChallengeDetailsBinding
import androidx.core.graphics.toColorInt
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.model.DashboardActiveChallenges
import com.critetiontech.ctvitalio.viewmodel.ChallengesViewModel
import kotlin.getValue

class NewChallengeDetails : Fragment() {

    private var _binding: FragmentNewChallengeDetailsBinding? = null
    private val binding get() = _binding!!

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
            challengesViewModel.getJoinedChallengesDetailsByEmployeeId(challenge?.challengeId ?: 0)
        }

        binding.titleText.text= challenge?.title.toString()
        binding.titleText.text= challenge?.title.toString()
        binding.discriptions.text =
            Html.fromHtml(challenge?.description ?: "", Html.FROM_HTML_MODE_LEGACY)

        binding.progressBar.progress = challenge?.progress?.toInt() ?: 0
        binding.labelCurrent.text = "Progress "+ challenge?.progress.toString()+"%"

        Log.d("ChallengeData", "challenge = ${challenge?.challengeId.toString()}")
        val glucoseValues = listOf(95, 105, 90, 98, 80, 85, 92)  // example data
        val dayLabels = listOf("18/09", "19/09", "20/09", "21/09", "22/09", "23/09", "24/09")
        val glucoseRangeText = "95–99 mg/dL"
        val daysFraction = "4/7 Days"


        bindGlucoseDataWithAxis(
            rootLayout = view.findViewById(R.id.rootLayout),
            glucoseRange = glucoseRangeText,
            daysLeftText = "", // not used here
            daysFractionText = daysFraction,
            dayValues = glucoseValues,
            dayDates = dayLabels
        )
    }


    fun bindGlucoseDataWithAxis(
        rootLayout: View,
        glucoseRange: String,
        daysLeftText: String,
        daysFractionText: String,
        dayValues: List<Int>,    // glucose values for 7 days
        dayDates: List<String>   // corresponding dates
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

        fun dpToPx(dp: Float) = (dp * rootLayout.resources.displayMetrics.density).toInt()

        // --- Y-axis labels: Max, Mid, Min ---
        val midValue = (minValue + maxValue) / 2
        val yLabels = listOf(maxValue, midValue, minValue)
        yLabels.forEach { value ->
            val yText = TextView(rootLayout.context).apply {
                text = value.toString()
                textSize = 10f
                setTextColor("#777777".toColorInt())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0, 1f
                )
            }
            yAxisContainer.addView(yText)
        }

        // --- Bars & X-axis dates ---
        dayValues.forEachIndexed { index, value ->
            val clampedValue = value.coerceIn(minValue, maxValue)
            val heightRatio = (clampedValue - minValue).toFloat() / (maxValue - minValue)
            val barHeightPx = (heightRatio * dpToPx(maxBarHeightDp)).toInt()

            // Bar
            val barView = View(rootLayout.context).apply {
                layoutParams = LinearLayout.LayoutParams(0, barHeightPx, 1f).apply {
                    setMargins(dpToPx(4f), 0, dpToPx(4f), 0)
                }
                setBackgroundColor(
                    if (value in 95..99) android.graphics.Color.parseColor("#7BD197")
                    else android.graphics.Color.parseColor("#D3E4CD")
                )
            }
            barChartContainer.addView(barView)

            // X-axis date
            val dateTextView = TextView(rootLayout.context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(dpToPx(4f), 0, dpToPx(4f), 0)
                }
                text = dayDates.getOrNull(index) ?: ""
                textSize = 10f
                setTextColor(android.graphics.Color.parseColor("#AAAAAA"))
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
            xAxisDates.addView(dateTextView)
        }

        // --- Center line position ---
        centerLine.post {
            val barContainerHeight = barChartContainer.height
            val midRatio = (midValue - minValue).toFloat() / (maxValue - minValue)
            val yPos = barContainerHeight - (midRatio * barContainerHeight)
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