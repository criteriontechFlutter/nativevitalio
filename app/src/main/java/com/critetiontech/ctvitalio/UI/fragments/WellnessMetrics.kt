package com.critetiontech.ctvitalio.UI.fragments

import DosageChartView
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentWellnessMetricsBinding
import com.critetiontech.ctvitalio.model.MovementIndexViewModel
 import java.text.SimpleDateFormat
import java.util.Locale
import android.animation.ValueAnimator
import android.view.ViewGroup as AndroidViewGroup
import android.graphics.drawable.GradientDrawable

class WellnessMetrics : Fragment() {

    private lateinit var binding: FragmentWellnessMetricsBinding
    private lateinit var viewModel: MovementIndexViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWellnessMetricsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[MovementIndexViewModel::class.java]

        binding.wellnessImageArrow.setOnClickListener {
            findNavController().popBackStack()
        }

        // Dummy / live movement data
        observeMovementData()
        viewModel.loadInitialData()

        binding.circularProgress.setProgress(55.0F)
        binding.dyRecovery.setProgressAnimated(65f, "Moderate")

        // Date change refresh
        binding.centerDatePicker.onDateChanged = { calendar ->
            val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(calendar.time)
            // Use 'formatted' if needed for API calls
            refreshMetricsForDate()
        }

        // Caffeine/bar graph using DosageChartView.createBarsOnly()
        val dosageChart = DosageChartView(requireContext())
        val caffeineBars = dosageChart.createBarsOnly(
            values = listOf(
                0.8f, 0.7f, 0.6f, 0.8f,
                0.4f, 0.3f, 0.8f, 0.1f
            ),
            color = 0xFF26BD78.toInt()
        )
        binding.caffeineBarGraph.addView(caffeineBars)

        // RulerSeekBar: Cardio Age
        binding.rulerSeekBar.apply {
            minValue = 12
            maxValue = 52
            currentValue = 32

        }
        binding.tvCardioValue.text = binding.rulerSeekBar.currentValue.toString()
    }

    private fun observeMovementData() {
        val dummyData = generateDummyHourlyData()
        updateHourlyChart(dummyData)
        viewModel.updateCircularProgress(binding.circularProgress)
    }

    private fun refreshMetricsForDate() {
        val newData = generateDummyHourlyData()
        updateHourlyChart(newData)
    }

    private fun generateDummyHourlyData(): List<Int> {
        val list = mutableListOf<Int>()
        for (hour in 0 until 24) {
            val value = when (hour) {
                in 6..8 -> (40..60).random()
                in 9..12 -> (70..100).random()
                in 13..16 -> (40..70).random()
                in 17..20 -> (30..60).random()
                in 20..24 -> (0..10).random()
                else -> (5..25).random()
            }
            list.add(value)
        }
        return list
    }

    private fun updateHourlyChart(hourlyData: List<Int>) {
        if (hourlyData.isEmpty()) return

        val maxValue = hourlyData.maxOrNull() ?: 1
        val normalizedData = hourlyData.map { (it.toFloat() / maxValue) * 100f }

        binding.barContainer.removeAllViews()
        binding.barContainerStress.removeAllViews()

        val density = resources.displayMetrics.density
        val barWidthPx = (density * 5).toInt()
        val barMarginPx = (density * 1.5f).toInt()

        normalizedData.forEach { value ->
            val barView = createHourlyBar(barWidthPx, barMarginPx)
            binding.barContainer.addView(barView)
            animateBarHeight(barView, value, binding.barContainer)
        }

        normalizedData.forEach { value ->
            val barViewStress = createHourlyBar(barWidthPx, barMarginPx)
            binding.barContainerStress.addView(barViewStress)
            animateBarHeight(barViewStress, value, binding.barContainerStress)
        }
    }

    private fun createHourlyBar(barWidthPx: Int, barMarginPx: Int): View {
        return View(requireContext()).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor("#546788".toColorInt())
                cornerRadii = floatArrayOf(
                    10f, 10f,  // top-left
                    10f, 10f,  // top-right
                    0f, 0f,    // bottom-right
                    0f, 0f     // bottom-left
                )
            }

            layoutParams = LinearLayout.LayoutParams(
                barWidthPx,
                0
            ).apply {
                setMargins(barMarginPx, 0, barMarginPx, 0)
            }
        }
    }

    /**
     * Animate bar height from 0 to target height inside its container.
     */
    private fun animateBarHeight(view: View, targetPercent: Float, container: AndroidViewGroup) {
        view.post {
            val containerHeight = container.height.takeIf { it > 0 } ?: 120
            val targetHeight = (containerHeight * targetPercent / 100f).toInt()

            ValueAnimator.ofInt(0, targetHeight).apply {
                duration = 600
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener { animation ->
                    view.layoutParams.height = animation.animatedValue as Int
                    view.requestLayout()
                }
            }.start()
        }
    }
}
