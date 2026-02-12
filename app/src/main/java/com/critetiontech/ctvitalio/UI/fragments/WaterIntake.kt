package com.critetiontech.ctvitalio.UI.fragments

import PrefsManager
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.WaterRecord
import com.critetiontech.ctvitalio.adapter.WaterRecordAdapter
import com.critetiontech.ctvitalio.databinding.FragmentWaterIntakeBinding
import com.critetiontech.ctvitalio.viewmodel.DashboardViewModel
import com.critetiontech.ctvitalio.viewmodel.FluidChartData
import com.critetiontech.ctvitalio.viewmodel.WaterIntakeViewModel
import com.example.vitalio_pragya.viewmodel.AddActivityViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
class WaterIntakeFragment : Fragment() {

    private var _binding: FragmentWaterIntakeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WaterIntakeViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()

    private lateinit var adapter: WaterRecordAdapter

    private var selectedGlassSize = 0 // ml
    private val dailyGoalMl = 3000f   // goal

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWaterIntakeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /** ----------------------------
         * RecyclerView
         * ---------------------------- */
        adapter = WaterRecordAdapter(mutableListOf()) { item ->
            viewModel.deleteEmployeeFluidIntake(item.id.toString())
        }

        binding.rvRecords.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecords.adapter = adapter
        updateEmptyState()

        /** ----------------------------
         * LiveData
         * ---------------------------- */
        viewModel.dailyRecords.observe(viewLifecycleOwner) {
            adapter.updateData(it)
            updateEmptyState()
        }

        binding.waterRing.post {

            val halfWidth = binding.waterRing.width / 2.6f

            // Start completely hidden to the left
            binding.waterRing.translationX = -binding.waterRing.width.toFloat()

            // Animate to half visible
            binding.waterRing.animate()
                .translationX(-halfWidth)
                .setDuration(800)
                .setInterpolator(FastOutSlowInInterpolator())
                .start()
        }
        val goal = PrefsManager().getEmployeeGoals()
            ?.find { it.goalId == 13 }
            ?.targetValue
            ?.toInt() ?: 0

        binding.tvGoalLabel.text = "Goal ${goal * 10000} ml"
            viewModel.atotalWaterQty.observe(viewLifecycleOwner) { totalMl ->

            // Total ML
            binding.tvTotalMl.text = "$totalMl ml"

            // Percentage
            val percentage = (totalMl.toInt() * 100) / 4000
            binding.tvDailyper.text = "$percentage%"
            val goal = 4000f

            val fraction = (totalMl.toInt() / goal).coerceIn(0f, 1f)
            binding.waterRing.setLevelSmooth(percentage.toFloat(), 1800)

                binding.waterRing.setLevelSmooth(70f, 1800)


        }
//        binding.tvTotalMl.text=

        viewModel.chartRecords.observe(viewLifecycleOwner) {
            setupWaterChart(it)
        }

        /** ----------------------------
         * API Calls
         * ---------------------------- */
        viewModel.GetDailyEmployeeFluidIntake()
        viewModel.GetEmployeeMedicineIntakeByDate()

        /** ----------------------------
         * Default Water Ring
         * ---------------------------- */

        /** ----------------------------
         * Chip Selection
         * ---------------------------- */
        binding.chipGroupSizes.setOnCheckedChangeListener { _, checkedId ->

            selectedGlassSize = when (checkedId) {
                R.id.chip_150 -> 150
                R.id.chip_250 -> 250
                R.id.chip_300 -> 300
                R.id.chip_400 -> 400
                 else -> 0
            }

            val level =
                (selectedGlassSize / dailyGoalMl).coerceIn(0f, 1f)

         }

        /** ----------------------------
         * Add Intake Button
         * ---------------------------- */
        binding.btnAddIntake.setOnClickListener {

            if (selectedGlassSize == 0) return@setOnClickListener

            viewModel.fluidIntake(selectedGlassSize.toString())

        }
    }
    /** ----------------------------
     * Empty State
     * ---------------------------- */
    private fun updateEmptyState() {
        if (!::adapter.isInitialized || adapter.itemCount == 0) {
            binding.rvRecords.visibility = View.GONE
            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.rvRecords.visibility = View.VISIBLE
            binding.emptyView.visibility = View.GONE
        }
    }

    /** ----------------------------
     * Bar Chart
     * ---------------------------- */
    private fun setupWaterChart(data: List<FluidChartData>) {

        val chart = binding.waterBarChart
        val days = 7

        // ---- Build last 7 dates (old → today)
        val calendar = java.util.Calendar.getInstance()
        val dateList = mutableListOf<String>()

        for (i in days - 1 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -i)
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            dateList.add(day.toString())
        }

        // ---- Map API data by date (last 2 digits)
        val dataMap = data.associateBy { it.date.takeLast(2) }

        // ---- Entries (missing days = 0)
        val entries = dateList.mapIndexed { index, day ->
            val qty = dataMap[day]?.qty ?: 0f
            BarEntry(index.toFloat(), qty / 1000f)
        }

        // ---- Colors
        val normal = Color.parseColor("#E3EEF7")
        val highlight = Color.parseColor("#28D27D")

        val colors = MutableList(days) { normal }
        colors[days - 1] = highlight // today

        val dataSet = BarDataSet(entries, "").apply {
            setDrawValues(false)
            this.colors = colors
        }

        chart.data = BarData(dataSet).apply {
            barWidth = 0.5f
        }

        // ---- X Axis
        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(dateList)
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            setDrawAxisLine(false)
            textSize = 10f
        }

        // ---- Y Axis
        chart.axisLeft.apply {
            axisMinimum = 0f
            setDrawGridLines(true)
        }

        chart.axisRight.isEnabled = false
        chart.legend.isEnabled = false
        chart.description.isEnabled = false

        chart.animateY(700)
        chart.invalidate()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}