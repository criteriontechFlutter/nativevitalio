package com.critetiontech.ctvitalio.UI.fragments

import SleepGraphData
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModelProvider
import com.critetiontech.ctvit.SleepKind
import com.critetiontech.ctvit.SleepSegmentData
import com.critetiontech.ctvitalio.databinding.FragmentSleepChartBinding
import com.critetiontech.ctvitalio.viewmodel.DashboardViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SleepGraphFragment : Fragment() {


    private lateinit var sleepStagesFromJson: String
    private var _binding: FragmentSleepChartBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSleepChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        viewModel.getVitals()

        observeSleepStages()
        observeSleepData()
    }




    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeSleepData() {

        viewModel.sleepValueList.observe(viewLifecycleOwner) { sleepValue ->

            sleepValue.SleepGraph?.Data?.let { graphList ->
                bindSleepGraph(graphList)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun bindSleepGraph(graphList: List<SleepGraphData>) {

        if (graphList.isEmpty()) return

        val baseTime = graphList.first().Start

        val segments = graphList.map { segment ->

            SleepSegmentData(   // ✅ correct model

                kind = when (segment.Type.lowercase()) {
                    "awake" -> SleepKind.AWAKE
                    "rem_sleep" -> SleepKind.REM
                    "light_sleep" -> SleepKind.LIGHT
                    "deep_sleep" -> SleepKind.DEEP
                    else -> SleepKind.LIGHT
                },

                start = convertToMinutes(baseTime, segment.Start),
                end = convertToMinutes(baseTime, segment.End),
                labelValue = segment.TossTurn ?: 0
            )
        }

        val totalTime = segments.maxOf { it.end }

        binding.sleepChartView.setSegments(
            data = segments,
            totalTime = totalTime
        )
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertToMinutes(base: String, target: String): Double {

        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

        val baseTime = LocalDateTime.parse(base, formatter)
        val targetTime = LocalDateTime.parse(target, formatter)

        val diff = java.time.Duration.between(baseTime, targetTime)

        return diff.toMinutes().toDouble()
    }

    private fun observeSleepStages() {


        viewModel.sleepValueList.observe(viewLifecycleOwner) { sleepValue  ->

            val timeinBed = sleepValue.QuickMetricsTiled
                ?.firstOrNull { it.Title.equals("TIME IN BED", ignoreCase = true) }
            binding.tvDuration .text=
                HtmlCompat.fromHtml(timeinBed?.Value.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)

        }
        viewModel.sleepValueList.observe(viewLifecycleOwner) { sleepValue ->

            val stages = sleepValue.SleepStages ?: return@observe

            val awake = stages.find { it.Type.equals("awake", true) }
            val rem = stages.find { it.Type.equals("rem_sleep", true) }
            val light = stages.find { it.Type.equals("light_sleep", true) }
            val deep = stages.find { it.Type.equals("deep_sleep", true) }

            awake?.let {
                binding.awakeBar.setData(
                    name = it.Title,
                    durationText = it.StageTimeText,
                    percent = it.Percentage,
                    color = Color.parseColor("#FFA726")
                )
            }

            rem?.let {
                binding.remBar.setData(
                    name = it.Title,
                    durationText = it.StageTimeText,
                    percent = it.Percentage,
                    color = Color.parseColor("#64B5F6")
                )
            }

            light?.let {
                binding.lightBar.setData(
                    name = it.Title,
                    durationText = it.StageTimeText,
                    percent = it.Percentage,
                    color = Color.parseColor("#1976D2")
                )
            }

            deep?.let {
                binding.deepBar.setData(
                    name = it.Title,
                    durationText = it.StageTimeText,
                    percent = it.Percentage,
                    color = Color.parseColor("#0D47A1")
                )
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}