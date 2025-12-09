package com.critetiontech.ctvitalio.UI.fragments.dashboard_pages

import SleepValue
import Vital
import android.health.connect.datatypes.units.Temperature
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.viewModels
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentHealthSnapsBinding
import com.critetiontech.ctvitalio.databinding.SleepLayoutBinding
import com.critetiontech.ctvitalio.viewmodel.DashboardViewModel

class HealthSnaps : Fragment() {
    private var _binding: FragmentHealthSnapsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHealthSnapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.getVitals()
        observeVitalList()
        observeSleepValues()
binding.showId.showHideId.setOnClickListener{
            binding.viewAllSleepDataaId.visibility=View.VISIBLE
            binding.showId.showHideId.visibility=View.GONE
        }
        binding.hideId.showHideId.setOnClickListener{
            binding.viewAllSleepDataaId.visibility=View.GONE
            binding.showId.showHideId.visibility=View.VISIBLE
        }
    }

    // ---------------------------------------------------------------------
    //  OBSERVE: VITAL LIST
    // ---------------------------------------------------------------------
    private fun observeVitalList() {
        viewModel.vitalList.observe(viewLifecycleOwner) { vitalList ->

            updateCard(binding.sleepScoreId, "Sleep Score", vitalList.getVital("SleepScore"))
            updateCard(binding.totalSleepId, "Total Sleep", vitalList.getVital("TotalSleep"))
            updateCard(binding.timeInBedId, "Time In Bed", vitalList.getVital("TimeInBed"))
            updateCard(binding.fulSleepCycleId, "Sleep Cycles", vitalList.getVital("SleepCycles"))
            updateCard(binding.restorativeSleepId, "Restorative Sleep", vitalList.getVital("RestorativeSleep"))
            updateCard(binding.morningAlertnessId, "Morning Alertness", vitalList.getVital("MorningAlertness"))
            updateCard(binding.tossesAndTurnsId, "Tosses and Turns", vitalList.getVital("TossTurn"))

            updateCard(binding.averageBodyTempId, "Temperature", vitalList.getVital("Temperature"))
            updateCard(binding.activieHoursId, "Active Hours", vitalList.getVital("ActiveHours"))
            updateCard(binding.StepsId, "Steps", vitalList.getVital("Steps"))
            updateCard(binding.ActiveminutesId, "Active Minutes", vitalList.getVital("ActiveMinutes"))

            updateCard(binding.recoveryScoreId, "Recovery Index", vitalList.getVital("RecoveryIndex"))
            updateCard(binding.lastNightHrvId, "Last Night's HRV", vitalList.getVital("HRV"))
            updateCard(binding.SleepStageHrvId, "Sleep Stage HRV", vitalList.getVital("HRV"))
            updateCard(binding.StressRhythmScoreId, "Stress Rhythm Score", vitalList.getVital("StressScore"))

            // Temperature Deviation (special formatting)
            val temp = vitalList.getVital("Temperature")?.vitalValue ?: 0.0
            binding.tempDeviationId.title.text = "Temperature Deviation"
            binding.tempDeviationId.value.text = "%.1f".format(temp)
            binding.tempDeviationId.statusCardId.visibility = View.GONE

            updateCard(binding.movementsId, "Movement Index", vitalList.getVital("MovementIndex"))
        }
    }

    // ---------------------------------------------------------------------
    //  OBSERVE: SLEEP VALUE DETAILS
    // ---------------------------------------------------------------------
    private fun observeSleepValues() {
        viewModel.sleepValueList.observe(viewLifecycleOwner) { sleepValue ->

            val efficiency = sleepValue.QuickMetrics
                ?.firstOrNull { it.Title.equals("EFFICIENCY", true) }

            binding.sleepEfficiencyId.title.text = "Sleep Efficiency"
            binding.sleepEfficiencyId.value.text = efficiency?.DisplayText ?: "--"
            binding.sleepEfficiencyId.statusCardId.visibility = View.GONE


            updateStage(binding.remSleepId, "REM Sleep", sleepValue, "REM Sleep")
            updateStage(binding.deepSleepId, "Deep Sleep", sleepValue, "Deep Sleep")
            updateStage(binding.lightSleepId, "Light Sleep", sleepValue, "Light Sleep")


            binding.movementsId.title.text = "Movements"
            binding.movementsId.value.text = sleepValue.MovementGraph?.Data?.size.toString()
            binding.movementsId.statusCardId.visibility = View.GONE


            binding.inactiveHoursId.title.text = "Inactive Time"
            binding.inactiveHoursId.value.text = "_"
            binding.inactiveHoursId.statusCardId.visibility = View.GONE
        }
    }

    // ---------------------------------------------------------------------
    //  UI HELPERS
    // ---------------------------------------------------------------------
    private fun updateCard(card: SleepLayoutBinding, title: String, vital: Vital?) {
        card.title.text = title
        card.value.text = vital.toText()
        card.statusCardId.visibility = View.GONE
        binding.tempDeviationId.title.text = "Temperature Devoatoion"
        binding.tempDeviationId.value.text = "${"%.1f".format(vital?.vitalValue ?: 0.0)}"
        binding.tempDeviationId.statusCardId.visibility = View.GONE

    }

    private fun updateStage(card: SleepLayoutBinding, title: String, sleepValue: SleepValue, stageName: String) {
        val stage = sleepValue.SleepStages?.firstOrNull { it.Title.equals(stageName, true) }
        card.title.text = title
        card.value.text = stage?.StageTimeText ?: "--"
        card.statusCardId.visibility = View.GONE
    }

    // ---------------------------------------------------------------------
    //  EXTENSIONS
    // ---------------------------------------------------------------------
    fun List<Vital>.getVital(name: String): Vital? {
        return firstOrNull { it.vitalName.equals(name, ignoreCase = true) }
    }

    fun Vital?.toText(): String =
        this?.vitalValue?.toInt()?.toString() ?: "--"

    // ---------------------------------------------------------------------
    //  CLEANUP
    // ---------------------------------------------------------------------
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}