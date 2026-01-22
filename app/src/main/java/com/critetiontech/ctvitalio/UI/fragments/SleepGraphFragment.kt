package com.critetiontech.ctvitalio.UI.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModelProvider
import androidx.privacysandbox.tools.core.model.Type
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.SleepStageBarView
import com.critetiontech.ctvitalio.databinding.FragmentSleepChartBinding
import com.critetiontech.ctvitalio.viewmodel.DashboardViewModel

class SleepGraphFragment : Fragment() {



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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        viewModel.getVitals()

        observeSleepStages()
        observeSleepData()
    }

    private fun observeSleepData() {
        viewModel.sleepValueList.observe(viewLifecycleOwner) { sleepValue ->

            val stageList = sleepValue.SleepStages ?: return@observe

            val chartData = stageList.map {
                when (it.Type.lowercase()) {
                    "deep_sleep" -> 0
                    "light_sleep" -> 1
                    "rem_sleep" -> 2
                    else -> 3
                }
            }
            binding.sleepChartView.setSleepStages(chartData)
        }
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