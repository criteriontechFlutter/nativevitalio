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

    private lateinit var binding: FragmentSleepDetailsBinding

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        binding = FragmentSleepDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Example dataset
        val data = listOf(
            SleepEntry(12, 62),
            SleepEntry(13, 56),
            SleepEntry(14, 54),
            SleepEntry(15, 62),
            SleepEntry(13, 56),
            SleepEntry(14, 54),
        )
        setData(data)
//        binding.totalSleepId.cardTitle.text = "Total Sleep"
//        binding.totalSleepId.cardValue.text = "72%"
//        binding.totalSleepId.cardStatus.text = "Optimal"
//
//        binding.timeInBedId.cardTitle.text = "Time in Bed"
//        binding.timeInBedId.cardValue.text = "72%"
//        binding.timeInBedId.cardStatus.text = "Optimal"
//
//        binding.restorativeSleepId.cardTitle.text = "Restorative Sleep  "
//        binding.restorativeSleepId.cardValue.text = "72%"
//        binding.restorativeSleepId.cardStatus.text = "Optimal"
//
//        binding.hrId.cardTitle.text = "HR DROP"
//        binding.hrId.cardValue.text = "72%"
//        binding.hrId.cardStatus.text = "Optimal"







//        binding.sleepEfficiencyId.cardTitle.text = "Sleep Efficiency"
//        binding.sleepEfficiencyId.sleepProgressBar.progress = 72
//        binding.sleepEfficiencyId.Title.text = "Optimal"
//
//        binding.tempId.cardTitle.text = "Temperature"
//        binding.tempId.sleepProgressBar.progress = 72
//        binding.tempId.Title.text = "Optimal"
//
//        binding.restfulnessId.cardTitle.text = "Restfulness"
//        binding.restfulnessId.sleepProgressBar.progress = 72
//        binding.restfulnessId.Title.text = "Optimal"
//
//        binding.totalSleepProgressId.cardTitle.text = "Total Sleep"
//        binding.totalSleepProgressId.sleepProgressBar.progress = 72
//        binding.totalSleepProgressId.Title.text = "Optimal"
    }
     @SuppressLint("SetTextI18n")
     private fun setData(entries: List<SleepEntry>) {
        binding.barsContainer.removeAllViews()
        if (entries.isEmpty()) return

        val maxValue = entries.maxOf { it.value }
        val avg = entries.map { it.value }.average().toInt()

        binding.tvScore.text = avg.toString()
        binding.tvLabel.text = "Sleep"

        binding.barsContainer.post { // ensures barsContainer has been measured
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
                    layoutParams = FrameLayout.LayoutParams(
                        4.dp,
                        fillHeight,
                        Gravity.CENTER_HORIZONTAL
                    )
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
                val bubbles = TextView(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(12.dp, 12.dp, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL)
                    text = "T"
                    setTextColor(Color.parseColor("#0A84FF"))
                    textSize = 12f
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_circle_value)
                }

                barContainer.addView(fillView)
                barContainer.addView(bubble)
                barContainer.addView(bubbles)

                val dayLabel = TextView(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        24.dp,
                        24.dp
                    )
                    text = entry.day.toString()
                    setTextColor(Color.BLACK)
                    textSize = 12f
                    gravity = Gravity.BOTTOM
                    setPadding(0, 4.dp, 0, 0)
                }


                val fillViews = View(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT
                            ,
                        4.dp,
                        Gravity.CENTER_HORIZONTAL
                    )
                    setPadding(0, 4.dp, 0, 0)
                    background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_bar_fill)
                }

                barLayout.addView(barContainer)
                barLayout.addView(fillViews)
                barLayout.addView(dayLabel)
                binding.barsContainer.addView(barLayout)
            }
        }
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}