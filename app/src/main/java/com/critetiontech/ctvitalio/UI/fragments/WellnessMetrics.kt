package com.critetiontech.ctvitalio.UI.fragments

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.databinding.FragmentWellnessMetricsBinding
import com.critetiontech.ctvitalio.model.MovementIndexViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.core.graphics.toColorInt

class WellnessMetrics : Fragment() {

    private lateinit var binding: FragmentWellnessMetricsBinding
    private lateinit var viewModel: MovementIndexViewModel




    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentWellnessMetricsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[MovementIndexViewModel::class.java]
        binding.wellnessImageArrow.setOnClickListener {
            findNavController().popBackStack()
        }


        // Observe LiveData
        observeMovementData()

        // Load initial data
        viewModel.loadInitialData()

        binding.circularProgress.setProgress(55.0F)

        // Setup refresh button
        binding.centerDatePicker.onDateChanged = { calendar ->
            val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            Toast.makeText(requireContext(), "Date changed to $formatted", Toast.LENGTH_SHORT).show()

            // 🔄 Refresh your data here
            refreshMetricsForDate()
        }


    }

    private fun observeMovementData() {
        // Instead of real API data, generate dummy hourly data
        val dummyData = generateDummyHourlyData()
        updateHourlyChart(dummyData)
        viewModel.updateCircularProgress(binding.circularProgress)
    }


    private fun refreshMetricsForDate() {
        // When date changes, regenerate dummy data
        val newData = generateDummyHourlyData()
        updateHourlyChart(newData)
    }

    private fun generateDummyHourlyData(): List<Int> {
        val list = mutableListOf<Int>()
        for (hour in 0 until 24) {
            val value = when (hour) {
                in 6..8 -> (40..60).random()   // early morning
                in 9..12 -> (70..100).random() // peak activity
                in 13..16 -> (40..70).random() // afternoon
                in 17..20 -> (30..60).random() // evening
                in 20..24 -> (0..10).random() // evening
                else -> (5..25).random()       // night hours
            }
            list.add(value)
        }
        return list
    }
    private fun updateHourlyChart(hourlyData: List<Int>) {
        if (hourlyData.isEmpty()) return

        val maxValue = hourlyData.maxOrNull() ?: 1
        val normalizedData = hourlyData.map { (it.toFloat() / maxValue) * 100 }

        binding.barContainer.removeAllViews()

        // Bar & spacing setup
        val barWidthPx = (resources.displayMetrics.density * 5).toInt()  // thin bars
        val barMarginPx = (resources.displayMetrics.density * 1.5f).toInt()

        normalizedData.forEachIndexed { index, value ->
            val barView = View(requireContext()).apply {
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(Color.parseColor("#546788")) // bar color
                    cornerRadii = floatArrayOf(
                        10f, 10f,   // top-left, top-right
                        0f, 10f,   // bottom-right, bottom-left
                        10f, 0f,   // (unused)
                        0f, 10f
                    )
                }

                layoutParams = LinearLayout.LayoutParams(
                    barWidthPx,
                    0
                ).apply {
                    setMargins(barMarginPx, 0, barMarginPx, 0)
                }
            }

            binding.barContainer.addView(barView)
            animateBarHeight(barView, value)
        }
    }


    /**
     * Animate bar height from 0 to target height
     */
    private fun animateBarHeight(view: View, targetPercent: Float) {
        view.post {
            val containerHeight = binding.barContainer.height.takeIf { it > 0 } ?: 100
            val targetHeight = (containerHeight * targetPercent / 100).toInt()

            val animator = android.animation.ValueAnimator.ofInt(0, targetHeight).apply {
                duration = 500
                addUpdateListener { animation ->
                    view.layoutParams.height = animation.animatedValue as Int
                    view.requestLayout()
                }
            }
            animator.start()
        }}
}