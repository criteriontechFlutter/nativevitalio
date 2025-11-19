package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.SleepStageBarView
import com.critetiontech.ctvitalio.viewmodel.DashboardViewModel

class SleepGraphFragment : Fragment() {


    private lateinit var viewModel: DashboardViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sleep_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        viewModel.getVitals()
        setupSleepStages(view)

        // The custom view will automatically draw itself
        // No additional setup needed
    }
   private fun setupSleepStages(view: View) {
        // Find views
        val awakeBar = view.findViewById<SleepStageBarView>(R.id.awakeBar)
        val remBar = view.findViewById<SleepStageBarView>(R.id.remBar)
        val lightBar = view.findViewById<SleepStageBarView>(R.id.lightBar)
        val deepBar = view.findViewById<SleepStageBarView>(R.id.deepBar)



       viewModel.sleepValueList.observe(viewLifecycleOwner) { sleepValue ->

           val awake = sleepValue.SleepStages
               ?.firstOrNull { it.Title.equals("Awake", ignoreCase = true) }

           val remSleep = sleepValue.SleepStages
               ?.firstOrNull { it.Title.equals("REM Sleep", ignoreCase = true) }

           val lightSleep = sleepValue.SleepStages
               ?.firstOrNull { it.Title.equals("Light Sleep", ignoreCase = true) }

           val deepSleep = sleepValue.SleepStages
               ?.firstOrNull { it.Title.equals("Deep Sleep", ignoreCase = true) }

           // Set data for each stage
           awake?.let {
               awakeBar.setData("Awake", it.StageTimeText, it.Percentage.toInt(), 0xFFFFA726.toInt())
           }

           remSleep?.let {
               remBar.setData("REM Sleep", it.StageTimeText, it.Percentage.toInt(), 0xFF64B5F6.toInt())
           }

           lightSleep?.let {
               lightBar.setData("Light Sleep", it.StageTimeText, it.Percentage.toInt(), 0xFF1976D2.toInt())
           }

           deepSleep?.let {
               deepBar.setData("Deep Sleep", it.StageTimeText, it.Percentage.toInt(), 0xFF0D47A1.toInt())
           }
       }
}
}