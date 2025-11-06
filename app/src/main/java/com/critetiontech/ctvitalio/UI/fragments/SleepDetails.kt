package com.critetiontech.ctvitalio.UI.fragments

import android.annotation.SuppressLint
import com.critetiontech.ctvitalio.R

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil.setContentView
import androidx.fragment.app.Fragment
import com.critetiontech.ctvitalio.databinding.ActivityForgotPasswordBinding
import com.critetiontech.ctvitalio.databinding.FragmentEnergyTankBinding
import com.critetiontech.ctvitalio.databinding.FragmentSleepDetailsBinding

data class SleepEntry(val day: Int, val value: Int)
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
        _binding = null // ✅ avoid memory leaks
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Example dataset
        val data = listOf(
            SleepEntry(12, 62),
            SleepEntry(13, 56),
            SleepEntry(14, 54),
            SleepEntry(15, 62),
            SleepEntry(16, 58),
            SleepEntry(17, 66)
        )

        // ✅ Create bar chart
        setData(data)

        // ✅ Safely set text for included layouts (ensure IDs exist)
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
        binding.hrProgress.cardTitle.text = "Hr Drop"
        binding.hrProgress.Title.text = "Need Attention"

        binding.restorativeSleepProgressId.sleepProgressBar.progress = 32
        binding.restorativeSleepProgressId.cardTitle.text = "Restorative sleep"
        binding.restorativeSleepProgressId.Title.text = "Need Attention"

    }

    @SuppressLint("SetTextI18n")
    private fun setData(entries: List<SleepEntry>) {
        binding.barsContainer.removeAllViews()
        if (entries.isEmpty()) return

        val maxValue = entries.maxOf { it.value }
        val avg = entries.map { it.value }.average().toInt()

        binding.tvScore.text = avg.toString()
        binding.tvLabel.text = "Sleep"

        // Wait for layout to be measured
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
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
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

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}