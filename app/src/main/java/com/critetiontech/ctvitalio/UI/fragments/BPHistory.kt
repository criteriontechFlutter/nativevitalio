package com.critetiontech.ctvitalio.UI.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.fragments.MovemenetIndex.MovementEntrys
import com.critetiontech.ctvitalio.adapter.BPLogAdapter
import com.critetiontech.ctvitalio.databinding.FragmentBPHistoryBinding
import com.critetiontech.ctvitalio.model.WeeklyMapGraph
import com.critetiontech.ctvitalio.model.dp
import com.critetiontech.ctvitalio.viewmodel.BloosPresureHistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.toColorInt
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class BPHistory : Fragment() {

    private var _binding: FragmentBPHistoryBinding? = null
    private val binding get() = _binding!!
    private var selectedIndex = -1  // -1 = nothing selected yet
    private var selectedValue = 0
    private lateinit var viewModel: BloosPresureHistoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBPHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[BloosPresureHistoryViewModel::class.java]

        // RecyclerView setup
        binding.recyclerTodayLog.layoutManager =
            LinearLayoutManager(requireContext())

        val adapter = BPLogAdapter(emptyList())
        binding.recyclerTodayLog.adapter = adapter

        viewModel.getBloodPressureDetailsByPid()
            binding.wellnessImageArrow.setOnClickListener {

                findNavController().popBackStack()
            }
        // Logs
        viewModel.bpLogs.observe(viewLifecycleOwner) { logs ->
            adapter.updateList(logs)   // Make sure adapter has updateList()
        }
        viewModel.weeklyMapGraph.observe(viewLifecycleOwner) { weeklyData ->
            setData(weeklyData)
        }
        binding.logBloodPressure.setOnClickListener {
            findNavController().navigate(R.id.action_BPHistory_to_connection )
        }
        val formatter = SimpleDateFormat("dd/MM", Locale.getDefault())
        val date = formatter.format(Date())
       // binding.wellnessText.text = date
//        viewModel.weeklyTrend.observe(viewLifecycleOwner) { trend ->
//
//            if (trend.isEmpty()) return@observe
//
//            // Prepare data for chart
//            val maxList = trend.map { it.maxMAP.toInt() }
//            val minList = trend.map { it.minMAP.toInt() }
//            val days = trend.map { it.dayName.take(3) }
//
//            binding.bpChart.setDataa(
//                systolic = maxList,
//                diastolic = minList,
//                days = days
//            )
//
//            // Format date range
//            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//            val outputFormat = SimpleDateFormat("dd", Locale.getDefault())      // for day
//            val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())    // for full month name
//
//            val startDate = inputFormat.parse(trend.first().date)
//            val endDate = inputFormat.parse(trend.last().date)
//
//            val startDay = outputFormat.format(startDate!!)
//            val endDay = outputFormat.format(endDate!!)
//            val month = monthFormat.format(startDate)  // assuming start and end in same month
//
//            val totalDays = trend.size
//
//            binding.tvDateRange.text = "$startDay-$endDay $month ($totalDays days records)"
//        }
        viewModel.weeklyTrend.observe(viewLifecycleOwner) { trend ->

            if (trend.isEmpty()) return@observe

            val maxList = trend.map { it.maxMAP.toInt() }
            val minList = trend.map { it.minMAP.toInt() }
            val days = trend.map { it.dayName.take(3) }

            binding.bpChart.setDataa(
                systolic = maxList,
                diastolic = minList,
                days = days
            )

            // Average BP calculation
            val avgSystolic = maxList.average().toInt()
            val avgDiastolic = minList.average().toInt()

            binding.tvAverage.text = "$avgSystolic / $avgDiastolic mmHg"

            // Date range code (same as yours)
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd", Locale.getDefault())
            val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())

            val startDate = inputFormat.parse(trend.first().date)
            val endDate = inputFormat.parse(trend.last().date)

            val startDay = outputFormat.format(startDate!!)
            val endDay = outputFormat.format(endDate!!)
            val month = monthFormat.format(startDate)

            val totalDays = trend.size
            binding.tvDateRange.text = "$startDay-$endDay $month ($totalDays days records)"
        }
        // Summary
        viewModel.summary.observe(viewLifecycleOwner) { summary ->

            if (summary == null) {
                binding.tvBPValue.text = "--/--"
                binding.tvStatus.text = "No Data"
                binding.tvStatus.setTextColor(Color.GRAY)
                return@observe
            }

            val sys = summary.systolic?.toInt() ?: 0
            val dia = summary.diastolic?.toInt() ?: 0

            binding.tvBPValue.text = "$sys/$dia"

            val status = getBPStatus(sys, dia)
            binding.tvStatus.text = status

            when (status) {
                "Low" -> {
                    binding.tvStatus.setTextColor(Color.BLUE)
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_low)
                }
                "Normal" -> {
                    binding.tvStatus.setTextColor(Color.parseColor("#1B8F5A"))
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_normal)
                }
                "Elevated" -> {
                    binding.tvStatus.setTextColor(Color.parseColor("#E6A700"))
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_elevated)
                }
                "High" -> {
                    binding.tvStatus.setTextColor(Color.RED)
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_high)
                }
            }
        }

        // Chart sample data
        binding.bpChart.setDataa(
            systolic = listOf(120,130,140,115,128,132,118),
            diastolic = listOf(80,75,85,78,77,82,70),
            days = listOf("9","10","11","12","13","14","15")
        )
    }

    // BP Classification
    fun getBPStatus(systolic: Int, diastolic: Int): String {
        return when {
            systolic < 90 || diastolic < 60 -> "Low"
            systolic < 120 && diastolic < 80 -> "Normal"
            systolic in 120..129 && diastolic < 80 -> "Elevated"
            systolic >= 130 || diastolic >= 80 -> "High"
            else -> "Normal"
        }
    }

    // Weekly Graph
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun setData(entries: List<WeeklyMapGraph>) {

        binding.barsContainer.removeAllViews()
        if (entries.isEmpty()) return

        val maxValue = entries.maxOf { it.avgValue }.takeIf { it > 0 } ?: 1.0

        // ✅ Select current date only if nothing selected yet
        if (selectedIndex == -1) {
            val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val todayIndex = entries.indexOfFirst { it.date == todayStr }
            selectedIndex = if (todayIndex != -1) todayIndex else 0
        }

        selectedValue = entries[selectedIndex].avgValue.toInt()
        binding.tvScore.text = selectedValue.toString()
        binding.tvLabel.text = "Glucose Avg"

        binding.barsContainer.post {
            val containerHeight = binding.barsContainer.height
            val maxBarHeight = containerHeight - 60.dp

            entries.forEachIndexed { index, entry ->

                val isSelected = index == selectedIndex
                val value = entry.avgValue
                val fillRatio = value.toFloat() / maxValue.toFloat()
                val fillHeight = (maxBarHeight * fillRatio).toInt()
                val visibleHeight = if (value == 0.0) 30.dp else fillHeight.coerceAtLeast(30.dp)

                val barLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                }

                val spacer = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
                    )
                }

                val barContainer = FrameLayout(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        24.dp,
                        if (isSelected) (visibleHeight + 10.dp) else visibleHeight // selected bar taller
                    )
                }

                // Track
                val trackView = View(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(24.dp, visibleHeight, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL)
                    background = GradientDrawable().apply {
                        setColor(Color.parseColor("#40FFFFFF"))
                        cornerRadius = 12.dp.toFloat()
                    }
                }

                // Fill
                val fillView = View(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(4.dp, visibleHeight, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL)
                    background = GradientDrawable().apply {
                        setColor(if (isSelected) Color.parseColor("#0A84FF") else Color.WHITE) // highlight
                        cornerRadius = 2.dp.toFloat()
                    }
                }

                // Bubble on top
                val bubble = TextView(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(24.dp, 24.dp, Gravity.TOP or Gravity.CENTER_HORIZONTAL)
                    text = value.toInt().toString()
                    setTextColor(if (isSelected) Color.WHITE else Color.parseColor("#0A84FF"))
                    textSize = 11f
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    background = GradientDrawable().apply {
                        setColor(if (isSelected) Color.parseColor("#0A84FF") else Color.WHITE)
                        shape = GradientDrawable.OVAL
                    }
                    elevation = 2.dp.toFloat()
                }

                val weekdayInitial = entry.dayName.first().toString()
                val weekdayBubble = TextView(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(20.dp, 20.dp, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL)
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

                val parts = entry.date.split("-")
                val dayLabel = TextView(requireContext()).apply {
                    text = if (parts.size == 3) "${parts[2]}/${parts[1]}" else entry.date
                    setTextColor(Color.parseColor("#80FFFFFF"))
                    textSize = 10f
                    gravity = Gravity.CENTER
                }

                val baseLine = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 2.dp
                    ).apply {
                        topMargin = 6.dp
                        bottomMargin = 6.dp
                    }
                    setBackgroundColor(Color.parseColor("#40FFFFFF"))
                }

                // ✅ Click listener to select bar
                barContainer.setOnClickListener {
                    if (selectedIndex != index) { // Only update if new selection
                        selectedIndex = index
                        selectedValue = entry.avgValue.toInt()
                        binding.tvScore.text = selectedValue.toString()
                        setData(entries) // redraw bars with new selection
                    }
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
    @RequiresApi(Build.VERSION_CODES.O)
    private fun selectCurrentDate(entries: List<WeeklyMapGraph>) {
        if (entries.isEmpty()) return

        val today = LocalDate.now()
        val todayStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        val todayIndex = entries.indexOfFirst { it.date == todayStr }
        selectedIndex = if (todayIndex != -1) todayIndex else 0
        selectedValue = entries[selectedIndex].mapValue.toInt()

        // Update UI
        binding.tvScore.text = selectedValue.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
