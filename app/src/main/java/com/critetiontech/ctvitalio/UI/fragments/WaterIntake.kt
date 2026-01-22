package com.critetiontech.ctvitalio.UI.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
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

    private lateinit var adapter: WaterRecordAdapter
    private val viewModel: WaterIntakeViewModel by viewModels()
    private   val dashboardViewModel: DashboardViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWaterIntakeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnAddIntake.setOnClickListener()  {val ml = getSelectedSizeMl()
            dashboardViewModel.fluidIntake(  ml.toString())
            viewModel.GetDailyEmployeeFluidIntake()
            viewModel.GetEmployeeMedicineIntakeByDate()

        }
        /** ----------------------------
         * 1️⃣ Initialize Adapter FIRST
         * ---------------------------- */
        adapter = WaterRecordAdapter(mutableListOf()) { item ->
             viewModel.deleteEmployeeFluidIntake(item.id.toString())
        }

        binding.rvRecords.adapter = adapter
        binding.rvRecords.layoutManager = LinearLayoutManager(requireContext())

        /** ----------------------------
         * 2️⃣ Empty State (Safe Now)
         * ---------------------------- */
        updateEmptyState()

        /** ----------------------------
         * 3️⃣ Recycler LiveData Binding
         * ---------------------------- */
        viewModel.dailyRecords.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
            updateEmptyState()
        }

        /** ----------------------------
         * 4️⃣ Chart LiveData Binding
         * ---------------------------- */
        viewModel.chartRecords.observe(viewLifecycleOwner) { list ->
            setupWaterChart(list)
        }

        /** ----------------------------
         * 5️⃣ Fire API Calls LAST
         * ---------------------------- */
        viewModel.GetDailyEmployeeFluidIntake()
        viewModel.GetEmployeeMedicineIntakeByDate()

        /** ----------------------------
         * 6️⃣ Animate Water Ring
         * ---------------------------- */
        binding.waterRing.setLevel(25f)
    }

    /** --------------------------------------
     * Empty State Toggle
     * -------------------------------------- */
    private fun updateEmptyState() {
        if (!::adapter.isInitialized || adapter.itemCount == 0) {
            binding.rvRecords.visibility = View.GONE
            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.rvRecords.visibility = View.VISIBLE
            binding.emptyView.visibility = View.GONE
        }
    }
    private fun getSelectedSizeMl(): Int? {
        val chipId = binding.chipGroupSizes.checkedChipId
        if (chipId == View.NO_ID) return null

        val chip = binding.chipGroupSizes.findViewById<com.google.android.material.chip.Chip>(chipId)
        val text = chip.text.toString()

        // handle custom chip
        if (text == "✎") {
            return null
        }

        return text.replace("ml", "").trim().toIntOrNull()
    }
    /** --------------------------------------
     * Bar Chart Setup (MPAndroidChart)
     * -------------------------------------- */
    private fun setupWaterChart(data: List<FluidChartData>) {

        val chart = binding.waterBarChart

        if (data.isEmpty()) {
            chart.clear()
            return
        }

        /** 🔹 Convert ml → L (BarEntry) */
        val entries = data.mapIndexed { i, v ->
            BarEntry(i.toFloat(), v.qty / 1000f)
        }

        /** 🔹 Colors */
        val normal = Color.parseColor("#E3EEF7")
        val highlight = Color.parseColor("#28D27D")

        val colors = MutableList(entries.size) { normal }
        colors[entries.size - 1] = highlight

        /** 🔹 DataSet */
        val dataSet = BarDataSet(entries, "").apply {
            setDrawValues(false)
            this.colors = colors
        }

        chart.data = BarData(dataSet).apply {
            barWidth = 0.50f
        }

        /** 🔹 X-Axis labels (last 2 digits of date) */
        val labels = data.map { it.date.takeLast(2) }

        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            textSize = 10f
            textColor = Color.parseColor("#95A5B2")
            setDrawAxisLine(false)
            setDrawGridLines(false)
            granularity = 1f
        }

        /** 🔹 Y-Axis */
        chart.axisLeft.apply {
            axisMinimum = 0f
            textSize = 10f
            textColor = Color.parseColor("#95A5B2")
            gridColor = Color.parseColor("#E8EEF3")
            setDrawGridLines(true)
        }

        chart.axisRight.isEnabled = false
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.animateY(600)
        chart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}