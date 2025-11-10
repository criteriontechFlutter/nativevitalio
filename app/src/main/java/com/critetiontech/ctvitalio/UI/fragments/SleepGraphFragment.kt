package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.SleepStageBarView

class SleepGraphFragment : Fragment() {

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

        // Set data for each stage
        awakeBar.setData("Awake", "4h 57m", 43, 0xFFFFA726.toInt())
        remBar.setData("REM Sleep", "1h 44m", 15, 0xFF64B5F6.toInt())
        lightBar.setData("Light Sleep", "4h 15m", 37, 0xFF1976D2.toInt())
        deepBar.setData("Deep Sleep", "35m", 5, 0xFF0D47A1.toInt())
    }
}