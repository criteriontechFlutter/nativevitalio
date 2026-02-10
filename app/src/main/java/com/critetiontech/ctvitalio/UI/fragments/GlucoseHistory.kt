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
import androidx.recyclerview.widget.LinearLayoutManager
import com.critetiontech.ctvitalio.adapter.BPLog
import com.critetiontech.ctvitalio.adapter.BPLogAdapter
import com.critetiontech.ctvitalio.adapter.GlucoseAdapter
import com.critetiontech.ctvitalio.databinding.FragmentGlucoseHistoryBinding
import com.critetiontech.ctvitalio.model.WeeklyMapGraph
import com.critetiontech.ctvitalio.model.dp
import com.critetiontech.ctvitalio.viewmodel.BloosPresureHistoryViewModel
import com.critetiontech.ctvitalio.viewmodel.GlucoseHistoryViewModel
import kotlin.collections.forEach

class GlucoseHistory : Fragment() {

    private var _binding: FragmentGlucoseHistoryBinding? = null
    private val binding get() = _binding!!
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
        // RecyclerView setup
        binding.recyclerTodayLog.layoutManager =
            LinearLayoutManager(requireContext())

//        // Weekly graph observer
//        viewModel.weeklyMapGraph.observe(viewLifecycleOwner) { weeklyData ->
//            setData(weeklyData)
//        }

        // Chart demo data
        binding.bpChart.setDataa(
            systolic = listOf(120,130,140,115,128,132,118),
            diastolic = listOf(80,75,85,78,77,82,70),
            days = listOf("9","10","11","12","13","14","15")
        )
    }

    // Weekly Graph
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun setData(entries: List<WeeklyMapGraph>) {

        binding.barsContainer.removeAllViews()
        if (entries.isEmpty()) return

        val maxValue = entries.maxOf { it.mapValue }
        val avg = entries.map { it.mapValue }.average().toInt()

        binding.tvScore.text = avg.toString()
        binding.tvLabel.text = "MAP Average"

        binding.barsContainer.post {

            val containerHeight = binding.barsContainer.height.takeIf { it > 0 } ?: 300.dp
            val maxBarHeight = containerHeight - 60.dp

            entries.forEach { entry ->

                val fillRatio = entry.mapValue.toFloat() / maxValue.toFloat()
                val fillHeight = (maxBarHeight * fillRatio).toInt().coerceAtLeast(30.dp)

                val barLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1f
                    )
                }

                val spacer = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1f
                    )
                }

                val barContainer = FrameLayout(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        24.dp,
                        fillHeight
                    )
                }

                val trackView = View(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        24.dp,
                        fillHeight,
                        Gravity.BOTTOM
                    )
                    background = GradientDrawable().apply {
                        setColor(Color.parseColor("#40FFFFFF"))
                        cornerRadius = 12.dp.toFloat()
                    }
                }

                val fillView = View(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        6.dp,
                        fillHeight,
                        Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    )
                    background = GradientDrawable().apply {
                        setColor(Color.WHITE)
                        cornerRadius = 3.dp.toFloat()
                    }
                }

                // Value bubble
                val bubble = TextView(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        26.dp,
                        26.dp,
                        Gravity.TOP or Gravity.CENTER_HORIZONTAL
                    )
                    text = entry.mapValue.toInt().toString()
                    setTextColor(Color.parseColor("#0A84FF"))
                    textSize = 11f
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    background = GradientDrawable().apply {
                        setColor(Color.WHITE)
                        shape = GradientDrawable.OVAL
                    }
                    elevation = 4.dp.toFloat()
                }

                // Weekday bubble
                val weekdayBubble = TextView(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        20.dp,
                        20.dp,
                        Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    )
                    text = entry.dayName.take(1)
                    setTextColor(Color.parseColor("#0A84FF"))
                    textSize = 10f
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    background = GradientDrawable().apply {
                        setColor(Color.WHITE)
                        shape = GradientDrawable.OVAL
                    }
                }

                // Safe date formatting
                val dayLabel = TextView(requireContext()).apply {
                    val parts = entry.date.split("-")
                    text = if (parts.size >= 3) "${parts[2]}/${parts[1]}" else entry.date
                    setTextColor(Color.parseColor("#80FFFFFF"))
                    textSize = 10f
                    gravity = Gravity.CENTER
                }

                val baseLine = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2.dp
                    ).apply {
                        topMargin = 6.dp
                        bottomMargin = 6.dp
                    }
                    setBackgroundColor(Color.parseColor("#40FFFFFF"))
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}