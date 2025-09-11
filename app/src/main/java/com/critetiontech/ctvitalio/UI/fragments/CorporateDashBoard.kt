package com.critetiontech.ctvitalio.UI.fragments

import PrefsManager
import Vital
import android.Manifest
import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.media.AudioRecord
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.DashboardAdapter
import com.critetiontech.ctvitalio.adapter.MedicineAdapter
import com.critetiontech.ctvitalio.adapter.ToTakeAdapter
import com.critetiontech.ctvitalio.databinding.FragmentCorporateDashBoardBinding
import com.critetiontech.ctvitalio.databinding.FragmentDashboardBinding
import com.critetiontech.ctvitalio.model.Medicine
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.utils.showRetrySnackbar
import com.critetiontech.ctvitalio.viewmodel.ChallengesViewModel
import com.critetiontech.ctvitalio.viewmodel.DashboardViewModel
import com.critetiontech.ctvitalio.viewmodel.PillsReminderViewModal
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import okhttp3.WebSocket


class CorporateDashBoard : Fragment() {
    private lateinit var binding: FragmentCorporateDashBoardBinding
    private lateinit var viewModel: DashboardViewModel
    private lateinit var challengesViewModel: ChallengesViewModel
    private lateinit var pillsViewModel: PillsReminderViewModal
    private lateinit var adapter: DashboardAdapter
    private lateinit var toTakeAdapter: ToTakeAdapter
    private var voiceDialog: Dialog? = null
    private var snackbar: Snackbar? = null
    private var currentPage = 0
    private val slideDelay: Long = 2100
    private val handler = Handler(Looper.getMainLooper())
    private var sliderRunnable: Runnable? = null
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var webSocket: WebSocket? = null
    private val RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    private val PERMISSION_REQUEST_CODE = 101
    var fragmentOpened = false
    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCorporateDashBoardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        pillsViewModel = ViewModelProvider(this)[PillsReminderViewModal::class.java]

        pillsViewModel.getAllPatientMedication()

        viewModel.isConnected.observe(viewLifecycleOwner) { isConnected ->
            if (isConnected) {
                snackbar?.dismiss()
                viewModel.getVitals()
            } else {
                showRetrySnackbar(
                    ""
                ) { viewModel.getVitals() }
            }
        }
        Glide.with(MyApplication.appContext)
            .load("http://182.156.200.177:5082/"+PrefsManager().getPatient()?.imageURL.toString())
            .placeholder(R.drawable.baseline_person_24)
            .circleCrop()
            .into(binding.avatar)
        binding.avatar.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_drawer4)
        }


        val text = "How are you feeling now?"
        val spannable = SpannableString(text)

        // Change only the word "feeling" to orange
        val start = text.indexOf("feeling")
        val end = start + "feeling".length
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#FFA500")), // Orange color
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

       binding.tFeeling.text = spannable
        val stepsGoal = PrefsManager().getEmployeeGoals().find { it.vmId == 234 }
        val waterGoal = PrefsManager().getEmployeeGoals().find { it.vmId == 245 }
        val sleepGoal = PrefsManager().getEmployeeGoals().find { it.vmId == 243 }

        stepsGoal?.let {
            binding.stepsGoalId.text = "/"+it.targetValue.toString()+" Steps"
        }

        waterGoal?.let {
            binding.waterGoalId.text = "/"+ it.targetValue.toString()+" ml"
        }





        viewModel.fluidIntake(requireContext() , "245", "414")
        viewModel.getFoodIntake()
        val typeface = ResourcesCompat.getFont(requireActivity(), R.font.source_serif_pro)
        binding.tFeeling.setTypeface(typeface, Typeface.BOLD)
        binding.tFeelingBelow.setTypeface(typeface )

        binding.greeting.text = "Good Morning,\n${PrefsManager().getPatient()?.patientName ?: ""}"

        viewModel.vitalList.observe(viewLifecycleOwner) { vitals ->
            // `vitals` is the latest value emitted by LiveData
            // Example: submit to RecyclerView adapter
            val vitalMovementIndex = vitals.find { it.vitalName.equals("MovementIndex", ignoreCase = true) }
            val vitalRecoveryIndex = vitals.find { it.vitalName.equals("RecoveryIndex", ignoreCase = true) }
            val vitalStepsIndex = vitals.find { it.vitalName.equals("Steps", ignoreCase = true) }
            binding.tvMovementIndex.text= vitalMovementIndex?.vitalValue.toString()
            binding.tvRecoveryIndex.text= vitalRecoveryIndex?.vitalValue.toString()
            binding.tvSteps.text= vitalStepsIndex?.vitalValue.toString()
        }

        viewModel.quickMetricListList.observe(viewLifecycleOwner) { vitals ->
            val efficiencyMetric = viewModel.quickMetricListList.value
                ?.firstOrNull { it.Title.equals("EFFICIENCY", ignoreCase = true) }

            binding.sleepGoalId.text =  "Sleep Efficiency "+efficiencyMetric?.DisplayText ?: "-"

            val sleepId = viewModel.quickMetricListList.value
                ?.firstOrNull { it.Title.equals("TOTAL SLEEP", ignoreCase = true) }
            binding.sleepId.text =  sleepId?.DisplayText ?: "-"
        }


        // Animate to 80%
       binding.WellnessProgres.setProgress(80f, animate = true)

        binding.ivIllustration.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_moodFragment)
        }

        binding.moodLayout.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(
                motionLayout: MotionLayout?, startId: Int, endId: Int
            ) {}

            override fun onTransitionChange(
                motionLayout: MotionLayout?, startId: Int, endId: Int, progress: Float
            ) {
                // progress goes from 0.0 → 1.0
//                if (!fragmentOpened && progress > 0.5f) {
//                   // openNewFragment()
//                }
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {}
            override fun onTransitionTrigger(
                motionLayout: MotionLayout?, triggerId: Int, positive: Boolean, progress: Float
            ) {}
        })




//
//// Change color dynamically
//        binding.WellnessProgres.setProgressColor(Color.GREEN)

// Update multiple metrics
        //updateWellnessData(78f, 92f, 85f, 82f)


        viewModel.vitalList.observe(viewLifecycleOwner) { vitalList ->

            val adapter: DashboardAdapter
            val bpSys = vitalList.find { it.vitalName.equals("BP_Sys", ignoreCase = true) }
            val bpDia = vitalList.find { it.vitalName.equals("BP_Dias", ignoreCase = true) }

            val filtered = vitalList.filterNot {
                it.vitalName.equals("BP_Sys", ignoreCase = true) ||
                        it.vitalName.equals("BP_Dias", ignoreCase = true)
            }.toMutableList()

            val finalVitalList = mutableListOf<Vital>()

            if (bpSys != null && bpDia != null) {
                val bpVital = Vital().apply {
                    vitalName = "Blood Pressure"
                    vitalValue = 0.0 // Optional placeholder
                    unit = "${bpSys.vitalValue.toInt()}/${bpDia.vitalValue.toInt()}  "
                    vitalDateTime = bpSys.vitalDateTime
                }
                finalVitalList.add(bpVital)
            }

//            finalVitalList.addAll(filtered)

            adapter = DashboardAdapter(requireContext(), finalVitalList) { vitalType ->
                val bundle = Bundle().apply {
                    putString("vitalType", vitalType)
                }
                findNavController().navigate(R.id.action_dashboard_to_connection, bundle)

            }
            binding.vitalsSlider.adapter = adapter

        }






        binding.recyclerMedicines.layoutManager = LinearLayoutManager(requireContext())

// Sample data
        val medicineList = listOf(
            Medicine("Metformin 500mg", "8:00 AM", "Taken"),
            Medicine("Omega-3", "6:00 AM", "Taken"),
            Medicine("Vitamin D", "9:00 AM", "Missed")
        )

        val adapter = MedicineAdapter(medicineList)
        binding.recyclerMedicines.adapter = adapter
    }
    private fun openNewFragment() {
        findNavController().navigate(R.id.moodFragment)
    }



}