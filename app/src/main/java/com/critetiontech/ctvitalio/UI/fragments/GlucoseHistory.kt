package com.critetiontech.ctvitalio.UI.fragments

import DateUtils.formatDate
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
import com.critetiontech.ctvitalio.adapter.BPLog
import com.critetiontech.ctvitalio.adapter.BPLogAdapter
import com.critetiontech.ctvitalio.adapter.GlucoseAdapter
import com.critetiontech.ctvitalio.databinding.FragmentGlucoseHistoryBinding
import com.critetiontech.ctvitalio.model.WeeklyMapGraph
import com.critetiontech.ctvitalio.model.dp
import com.critetiontech.ctvitalio.viewmodel.BloosPresureHistoryViewModel
import com.critetiontech.ctvitalio.viewmodel.GlucoseHistoryViewModel
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.collections.forEach

class GlucoseHistory : Fragment() {

    private var _binding: FragmentGlucoseHistoryBinding? = null
    private val binding get() = _binding!!

    private var selectedIndex = -1  // -1 = nothing selected yet
    private var selectedValue = 0
    private lateinit var viewModel: GlucoseHistoryViewModel
    private lateinit var adapter: GlucoseAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGlucoseHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[GlucoseHistoryViewModel::class.java]

        viewModel.getGlucoseDetailsByPid()
        adapter = GlucoseAdapter(emptyList())
        binding.recyclerTodayLog.adapter = adapter

        viewModel.logs.observe(viewLifecycleOwner) { logs ->
            adapter.updateData(logs)
        }

        binding.wellnessImageArrow.setOnClickListener {

            findNavController().popBackStack()
        }
        binding.logglucoselevel.setOnClickListener {
            val bundle = Bundle().apply {
                putString("vitalType", "Glucose")
            }
            findNavController().navigate(R.id.action_glucoseHistory_to_connection, bundle)
        }
        val formatter = SimpleDateFormat("dd/MM", Locale.getDefault())
        val date = formatter.format(Date())
//        binding.wellnessText.text = date
        viewModel.weeklyGraph.observe(viewLifecycleOwner) { list ->
            if (!list.isNullOrEmpty()) {
                setData(list)



                // Format date range
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd", Locale.getDefault())      // for day
                val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())    // for full month name

                val startDate = inputFormat.parse(list.first().date)
                val endDate = inputFormat.parse(list.last().date)

                val startDay = outputFormat.format(startDate!!)
                val endDay = outputFormat.format(endDate!!)
                val month = monthFormat.format(startDate)  // assuming start and end in same month

                val totalDays = list.size

               // binding.tvDateRange.text = "$startDay-$endDay $month ($totalDays days records)"
            }
        }

        binding.logglucoselevel

        viewModel.summary.observe(viewLifecycleOwner) { summary ->

            summary?.let {

                binding.tvMinValue.text = it.dayMin.toInt().toString()
                binding.tvMaxValue.text = it.dayMax.toInt().toString()

                showMinMaxRange(
                    it.dayMin.toInt(),
                    it.dayMax.toInt()
                )

                val status = getGlucoseStatus(it.weekAvg.toInt())

            } ?: run {
                // optional fallback UI
                binding.tvMinValue.text = "--"
                binding.tvMaxValue.text = "--"
            }
        }
        viewModel.trendGraph.observe(viewLifecycleOwner) { list ->

            val minValues = list.map { it.minValue.toFloat() }
            val maxValues = list.map { it.maxValue.toFloat() }
            val days = list.map {
                it.date.substring(8, 10)
            }

                binding.glucoseGraph.setData(minValues, maxValues, days)
            // Average glucose calculation
            val avgGlucose = list.map { (it.minValue + it.maxValue) / 2.0 }
                .average()

            //binding.tvAverage.text = avgGlucose.toInt().toString()+" mg/dl"
        }
        // RecyclerView setup
        binding.recyclerTodayLog.layoutManager =
            LinearLayoutManager(requireContext())

//        // Weekly graph observer
//        viewModel.weeklyMapGraph.observe(viewLifecycleOwner) { weeklyData ->
//            setData(weeklyData)
//        }

        // Chart demo data

    }

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


    private fun formatDates(dateStr: String): String {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val output = SimpleDateFormat("dd MMM", Locale.getDefault())

        return try {
            val date = input.parse(dateStr)
            output.format(date!!)
        } catch (e: Exception) {
            dateStr
        }
    }
    private fun getGlucoseStatus(value: Int): Pair<String, Int> {

        return when {
            value < 70 -> Pair("Low", Color.parseColor("#E53935"))   // Red
            value in 70..140 -> Pair("Normal", Color.parseColor("#1BAA60")) // Green
            else -> Pair("High", Color.parseColor("#FB8C00"))  // Orange
        }
    }
    private fun showMinMaxRange(minValue: Int, maxValue: Int) {

        val totalRange = 200f   // glucose max scale

        binding.rangeIndicator.post {

            val parentWidth =
                (binding.rangeIndicator.parent as View).width

            val startX = (minValue / totalRange) * parentWidth
            val endX = (maxValue / totalRange) * parentWidth
            val greenWidth = (endX - startX).coerceAtLeast(8f)

            // Move green bar
            binding.rangeIndicator.translationX = startX

            // Resize green bar
            val params = binding.rangeIndicator.layoutParams
            params.width = greenWidth.toInt()
            binding.rangeIndicator.layoutParams = params
        }
    }

    // Weekly Graph
//    @RequiresApi(Build.VERSION_CODES.O)
//    @SuppressLint("SetTextI18n")
//    private fun setData(entries: List<WeeklyMapGraph>) {
//
//        binding.barsContainer.removeAllViews()
//        if (entries.isEmpty()) return
//
//        val maxValue = entries.maxOf { it.mapValue }
//        val avg = entries.map { it.mapValue }.average().toInt()
//
//        binding.tvScore.text = avg.toString()
//        binding.tvLabel.text = "MAP Average"
//
//        binding.barsContainer.post {
//
//            val containerHeight = binding.barsContainer.height.takeIf { it > 0 } ?: 300.dp
//            val maxBarHeight = containerHeight - 60.dp
//
//            entries.forEach { entry ->
//
//                val fillRatio = entry.mapValue.toFloat() / maxValue.toFloat()
//                val fillHeight = (maxBarHeight * fillRatio).toInt().coerceAtLeast(30.dp)
//
//                val barLayout = LinearLayout(requireContext()).apply {
//                    orientation = LinearLayout.VERTICAL
//                    gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
//                    layoutParams = LinearLayout.LayoutParams(
//                        0,
//                        LinearLayout.LayoutParams.MATCH_PARENT,
//                        1f
//                    )
//                }
//
//                val spacer = View(requireContext()).apply {
//                    layoutParams = LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.MATCH_PARENT,
//                        0,
//                        1f
//                    )
//                }
//
//                val barContainer = FrameLayout(requireContext()).apply {
//                    layoutParams = LinearLayout.LayoutParams(
//                        24.dp,
//                        fillHeight
//                    )
//                }
//
//                val trackView = View(requireContext()).apply {
//                    layoutParams = FrameLayout.LayoutParams(
//                        24.dp,
//                        fillHeight,
//                        Gravity.BOTTOM
//                    )
//                    background = GradientDrawable().apply {
//                        setColor(Color.parseColor("#40FFFFFF"))
//                        cornerRadius = 12.dp.toFloat()
//                    }
//                }
//
//                val fillView = View(requireContext()).apply {
//                    layoutParams = FrameLayout.LayoutParams(
//                        6.dp,
//                        fillHeight,
//                        Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
//                    )
//                    background = GradientDrawable().apply {
//                        setColor(Color.WHITE)
//                        cornerRadius = 3.dp.toFloat()
//                    }
//                }
//
//                // Value bubble
//                val bubble = TextView(requireContext()).apply {
//                    layoutParams = FrameLayout.LayoutParams(
//                        26.dp,
//                        26.dp,
//                        Gravity.TOP or Gravity.CENTER_HORIZONTAL
//                    )
//                    text = entry.mapValue.toInt().toString()
//                    setTextColor(Color.parseColor("#0A84FF"))
//                    textSize = 11f
//                    typeface = Typeface.DEFAULT_BOLD
//                    gravity = Gravity.CENTER
//                    background = GradientDrawable().apply {
//                        setColor(Color.WHITE)
//                        shape = GradientDrawable.OVAL
//                    }
//                    elevation = 4.dp.toFloat()
//                }
//
//                // Weekday bubble
//                val weekdayBubble = TextView(requireContext()).apply {
//                    layoutParams = FrameLayout.LayoutParams(
//                        20.dp,
//                        20.dp,
//                        Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
//                    )
//                    text = entry.dayName.take(1)
//                    setTextColor(Color.parseColor("#0A84FF"))
//                    textSize = 10f
//                    typeface = Typeface.DEFAULT_BOLD
//                    gravity = Gravity.CENTER
//                    background = GradientDrawable().apply {
//                        setColor(Color.WHITE)
//                        shape = GradientDrawable.OVAL
//                    }
//                }
//
//                // Safe date formatting
//                val dayLabel = TextView(requireContext()).apply {
//                    val parts = entry.date.split("-")
//                    text = if (parts.size >= 3) "${parts[2]}/${parts[1]}" else entry.date
//                    setTextColor(Color.parseColor("#80FFFFFF"))
//                    textSize = 10f
//                    gravity = Gravity.CENTER
//                }
//
//                val baseLine = View(requireContext()).apply {
//                    layoutParams = LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.MATCH_PARENT,
//                        2.dp
//                    ).apply {
//                        topMargin = 6.dp
//                        bottomMargin = 6.dp
//                    }
//                    setBackgroundColor(Color.parseColor("#40FFFFFF"))
//                }
//
//                barContainer.addView(trackView)
//                barContainer.addView(fillView)
//                barContainer.addView(bubble)
//                barContainer.addView(weekdayBubble)
//
//                barLayout.addView(spacer)
//                barLayout.addView(barContainer)
//                barLayout.addView(baseLine)
//                barLayout.addView(dayLabel)
//
//                binding.barsContainer.addView(barLayout)
//            }
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}