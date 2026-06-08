package com.critetiontech.ctvitalio.UI.fragments

import DosageChartView
import android.animation.ValueAnimator
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup as AndroidViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentWellnessMetricsBinding
import com.critetiontech.ctvitalio.model.MovementIndexResponseValue
import com.critetiontech.ctvitalio.model.MovementIndexViewModel
import com.critetiontech.ctvitalio.model.VitalItem
import java.text.SimpleDateFormat
import java.util.Locale

class WellnessMetrics : Fragment() {

    private var _binding: FragmentWellnessMetricsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MovementIndexViewModel

    companion object {
        private const val DATE_FORMAT = "yyyy-MM-dd"
        private const val DEFAULT_VALUE = "0"
        private const val BAR_COLOR = "#546788"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWellnessMetricsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewModel()
        setupBinding()
        setupClickListeners()
        setupDatePicker()
        setupCharts()
        observeVitals()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this)[MovementIndexViewModel::class.java]
    }

    private fun setupBinding() {
        binding.vm = viewModel
    }

    private fun setupClickListeners() {

        binding.wellnessImageArrow.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.movementIndexId.setOnClickListener {
            navigateTo(R.id.action_wellnessMetrics_to_movemenetIndex)
        }

        binding.stressRhythmId.setOnClickListener {
            navigateTo(R.id.action_wellnessMetrics_to_stressRhythm)
        }

        binding.dynamicRecovery.setOnClickListener {
            navigateTo(R.id.action_wellnessMetrics_to_dynamicRecovery)
        }

        binding.sleepContainerId.setOnClickListener {
            navigateTo(R.id.action_wellnessMetrics_to_sleepDetails)
        }

        binding.sleepBarGreen.setOnClickListener {
            navigateTo(R.id.action_wellnessMetrics_to_brainWasteClearance)
        }
    }

    private fun navigateTo(destination: Int) {
        findNavController().navigate(destination)
    }

    private fun setupDatePicker() {
        binding.centerDatePicker.onDateChanged = { calendar ->

            val formattedDate = SimpleDateFormat(
                DATE_FORMAT,
                Locale.getDefault()
            ).format(calendar.time)

            refreshMetricsForDate(formattedDate)
        }
    }

    private fun setupCharts() {

        val dosageChart = DosageChartView(requireContext())

        val caffeineBars = dosageChart.createBarsOnly(
            values = listOf(
                0.8f,
                0.7f,
                0.6f,
                0.8f,
                0.4f,
                0.3f,
                0.8f,
                0.1f
            ),
            color = 0xFF26BD78.toInt()
        )

        // binding.caffeineBarGraph.addView(caffeineBars)
    }

    private fun observeVitals() {

        viewModel.wellnessMetrics.observe(viewLifecycleOwner) { metrics ->

            updateCircularProgress(metrics)
            updateTextValues(metrics)
        }
    }

    private fun updateCircularProgress(metrics: MovementIndexResponseValue) {

        getVitalValue(metrics, "MovementIndex")?.toFloatOrNull()?.let {
            binding.circularProgress.setProgress(it)
        }

        getVitalValue(metrics, "RecoveryIndex")?.toFloatOrNull()?.let {
            binding.dyRecovery.setProgress(it)
        }
    }

    private fun updateTextValues(metrics: MovementIndexResponseValue) {

        binding.stressScore =
            getVitalValue(metrics, "StressScore") ?: DEFAULT_VALUE

        binding.brainClear =
            getVitalValue(metrics, "BrainWasteClearance") ?: DEFAULT_VALUE
    }

    private fun getVitalValue(
        metrics: MovementIndexResponseValue,
        vitalName: String
    ): String? {

        return metrics.vitals.firstOrNull {
            it.vitalName.equals(vitalName, ignoreCase = true)
        }?.vmValue
    }

    private fun refreshMetricsForDate(date: String) {
        viewModel.getWellnessData(date)
    }

    private fun updateHourlyChart(hourlyData: List<Int>) {

        if (hourlyData.isEmpty()) return

        val maxValue = hourlyData.maxOrNull() ?: 1

        val normalizedData = hourlyData.map {
            (it.toFloat() / maxValue) * 100f
        }

        clearCharts()

        normalizedData.forEach { value ->
            addAnimatedBar(binding.barContainer, value)
            addAnimatedBar(binding.barContainerStress, value)
        }
    }

    private fun clearCharts() {
        binding.barContainer.removeAllViews()
        binding.barContainerStress.removeAllViews()
    }

    private fun addAnimatedBar(
        container: LinearLayout,
        value: Float
    ) {

        val density = resources.displayMetrics.density

        val barWidthPx = (density * 5).toInt()
        val barMarginPx = (density * 1.5f).toInt()

        val barView = createHourlyBar(
            barWidthPx,
            barMarginPx
        )

        container.addView(barView)

        animateBarHeight(
            barView,
            value,
            container
        )
    }

    private fun createHourlyBar(
        barWidthPx: Int,
        barMarginPx: Int
    ): View {

        return View(requireContext()).apply {

            background = GradientDrawable().apply {

                shape = GradientDrawable.RECTANGLE

                setColor(BAR_COLOR.toColorInt())

                cornerRadii = floatArrayOf(
                    10f, 10f,
                    10f, 10f,
                    0f, 0f,
                    0f, 0f
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

    private fun animateBarHeight(
        view: View,
        targetPercent: Float,
        container: AndroidViewGroup
    ) {

        view.post {

            val containerHeight =
                container.height.takeIf { it > 0 } ?: 120

            val targetHeight =
                (containerHeight * targetPercent / 100f).toInt()

            ValueAnimator.ofInt(0, targetHeight).apply {

                duration = 600

                interpolator = AccelerateDecelerateInterpolator()

                addUpdateListener { animation ->

                    view.layoutParams.height =
                        animation.animatedValue as Int

                    view.requestLayout()
                }
            }.start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}