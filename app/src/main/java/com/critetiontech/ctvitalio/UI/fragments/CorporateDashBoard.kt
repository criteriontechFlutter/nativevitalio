package com.critetiontech.ctvitalio.UI.fragments

import MoodData
import PrefsManager
import Vital
import android.Manifest
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.media.AudioRecord
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.DashboardAdapter
import com.critetiontech.ctvitalio.adapter.MedicineAdapter
import com.critetiontech.ctvitalio.adapter.NewChallengedAdapter
import com.critetiontech.ctvitalio.databinding.FragmentCorporateDashBoardBinding
import com.critetiontech.ctvitalio.model.Medicine
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.utils.showRetrySnackbar
import com.critetiontech.ctvitalio.viewmodel.ChallengesViewModel
import com.critetiontech.ctvitalio.viewmodel.DashboardViewModel
import com.critetiontech.ctvitalio.viewmodel.PillsReminderViewModal
import com.google.android.material.snackbar.Snackbar
import okhttp3.WebSocket


class CorporateDashBoard : Fragment() {
    private lateinit var binding: FragmentCorporateDashBoardBinding
    private lateinit var viewModel: DashboardViewModel
    private lateinit var challengesViewModel: ChallengesViewModel
    private lateinit var pillsViewModel: PillsReminderViewModal
    private lateinit var adapter: DashboardAdapter
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
    private val tabLabels = listOf("Home", "Streaks", "Triggers", "Challenges")
    private val tabIcons = listOf(R.drawable.home, R.drawable.vitals_icon_home, R.drawable.pill,R.drawable.challenges_icon)
    private lateinit var navItems: List<View>

    private val moods = listOf(
        MoodData(5,"Spectacular", "#FFA4BA", R.drawable.spectulor_mood,  "#611829"),
        MoodData(3,"Upset", "#88A7FF",  R.drawable.upset_mood,  "#2A4089"),
        MoodData(1, "Stressed", "#FF9459",  R.drawable.stressed_mood, "#782E04"),
        MoodData(4,"Happy", "#9ABDFF",  R.drawable.happy_mood,"#505D87"),
        MoodData(7,"Good", "#F9C825",  R.drawable.good_mood, "#664F00"),
        MoodData(6,"Sad",   "#7DE7EE",  R.drawable.sad_mood,  "#3A7478")

    )
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

        navItems = listOf(
            view.findViewById(R.id.nav_home),
            view.findViewById(R.id.nav_vitals),
            view.findViewById(R.id.nav_medicine),
            view.findViewById(R.id.nav_goals)
        )
        viewModel.selectedMoodId.observe(viewLifecycleOwner) { moodId ->
            Log.d("TAG", "onViewCreated: "+moodId.toString());
            if (moodId==null || moodId.toString().trim()=="null") {
                binding.ivIllustration.setImageResource(R.drawable.moods)
                binding.tFeelingBelow.visibility=View.VISIBLE
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
            } else {

                val drawableRes = moods.find { it.id.toString() == moodId.toString() }?.emojiRes
                val feeling = moods.find { it.id.toString() == moodId.toString() }?.name
                binding.tFeelingBelow.visibility=View.GONE
                binding.tFeeling.text= "Feeling $feeling"
                if (drawableRes != null) {
                    binding.ivIllustration.setImageResource(drawableRes)
                    val layoutParams = binding.ivIllustration.layoutParams
                    layoutParams.width = dpToPx(344, requireContext())   // 400dp → pixels
                    layoutParams.height = dpToPx(140, requireContext())
                    binding.ivIllustration.layoutParams = layoutParams

                       
                }
            }
        }
        setupNav()
        setupBottomNav(view)
        viewModel.getMoodByPid()
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

        challengesViewModel = ViewModelProvider(this)[ChallengesViewModel::class.java]


        challengesViewModel.getNewChallenge()
        challengesViewModel.newChallengeList.observe(viewLifecycleOwner) { list ->
            binding.newChallengedRecyclerView.adapter = NewChallengedAdapter(
                list,
                onItemClick =  { challenge ->
                    challengesViewModel.insertChallengeparticipants( challenge.id.toString())
                },
                onItemClick1 =  { challenge ->
                    val bundle = Bundle().apply {
                        putSerializable("challenges", challenge)
                    }
                   // findNavController().navigate(R.id.action_dashboard_to_challengeDetailsFragment, bundle)

                }
            )

        }

        val stepsGoal = PrefsManager().getEmployeeGoals().find { it.vmId == 234 }
        val waterGoal = PrefsManager().getEmployeeGoals().find { it.vmId == 245 }
        val sleepGoal = PrefsManager().getEmployeeGoals().find { it.vmId == 243 }

        stepsGoal?.let {
           // binding.stepsGoalId.text = "/"+it.targetValue.toString()+" Steps"
        }

        waterGoal?.let {
            binding.waterGoalId.text = "/"+ (it.targetValue*1000).toString()+" ml"
        }

        val animation = AnimationUtils.loadAnimation(requireActivity(), R.anim.item_animation_from_bottom)

        // Start animations with delay one by one
        binding.tFeeling.startAnimation(animation)

        binding.tFeelingBelow.postDelayed({
            binding.tFeelingBelow.startAnimation(animation)
        }, 300)

        binding.ivIllustration.postDelayed({
            binding.ivIllustration.startAnimation(animation)
        }, 2000)
        binding.contentScroll.postDelayed({
            binding.contentScroll.startAnimation(animation)
        }, 5000)




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
            binding.tvStepss.text= vitalStepsIndex?.vitalValue.toString()
        }

        viewModel.quickMetricListList.observe(viewLifecycleOwner) { quickMetricListList ->
            val efficiencyMetric = quickMetricListList
                ?.firstOrNull { it.Title.equals("EFFICIENCY", ignoreCase = true) }

            binding.sleepGoalId.text =  "Sleep Efficiency "+efficiencyMetric?.DisplayText ?: "-"

            val sleepId = quickMetricListList
                ?.firstOrNull { it.Title.equals("TOTAL SLEEP", ignoreCase = true) }
            binding.sleepId.text =  sleepId?.DisplayText ?: "-"
        }


        // Animate to 80%
       binding.WellnessProgres.setProgress(80f, animate = true)
        val extras = FragmentNavigatorExtras(
            binding.avatar to "heroImageTransition",
            binding.greeting to "heroGreetingtextTransition"
        )
        binding.ivIllustration.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_moodFragment ,null,
                null,
                extras)
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

        binding.energyLevel.setOnClickListener{
            findNavController().navigate(R.id.action_dashboard_to_energyTank)
        }



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

//            adapter = DashboardAdapter(requireContext(), finalVitalList) { vitalType ->
//                val bundle = Bundle().apply {
//                    putString("vitalType", vitalType)
//                }
//                findNavController().navigate(R.id.action_dashboard_to_connection, bundle)
//
//            }
//            binding.vitalsSlider.adapter = adapter

        }






//        binding.recyclerMedicines.layoutManager = LinearLayoutManager(requireContext())
//
//// Sample data
//        val medicineList = listOf(
//            Medicine("Metformin 500mg", "8:00 AM", "Taken"),
//            Medicine("Omega-3", "6:00 AM", "Taken"),
//            Medicine("Vitamin D", "9:00 AM", "Missed")
//        )
//
//        val adapter = MedicineAdapter(medicineList)
//        binding.recyclerMedicines.adapter = adapter
    }
    private fun openNewFragment() {
        findNavController().navigate(R.id.moodFragment)
    }

    private fun setupBottomNav(view1: View) {
        // Home
        val homeItem = view1.findViewById<View>(R.id.nav_home)
        homeItem.findViewById<ImageView>(R.id.navIcon).setImageResource(R.drawable.home)
        homeItem.findViewById<TextView>(R.id.navText).text = "Home"

        // Vitals
        val vitalsItem =  view1.findViewById<View>(R.id.nav_vitals)
        vitalsItem.findViewById<ImageView>(R.id.navIcon).setImageResource(R.drawable.vitals_icon_home)
        vitalsItem.findViewById<TextView>(R.id.navText).text = "Vitals"

        // Medicine
        val medicineItem =  view1.findViewById<View>(R.id.nav_medicine)
        medicineItem.findViewById<ImageView>(R.id.navIcon).setImageResource(R.drawable.reminders)
        medicineItem.findViewById<TextView>(R.id.navText).text = "Medicine"

        // Goals
        val goalsItem =  view1.findViewById<View>(R.id.nav_goals)
        goalsItem.findViewById<ImageView>(R.id.navIcon).setImageResource(R.drawable.challenges_icon)
        goalsItem.findViewById<TextView>(R.id.navText).text = "Goals"
    }

    private fun setupNav() {
        navItems.forEachIndexed { index, view ->
            val icon = view.findViewById<ImageView>(R.id.navIcon)
            val text = view.findViewById<TextView>(R.id.navText)
            view.setOnClickListener {
                text.text = tabLabels[index]
               // icon.setBackgroundResource( tabIcons[index])
                selectItem(index)
            }
        }

        // Default select Home
        selectItem(0)
    }

    private fun selectItem(index: Int) {
        navItems.forEachIndexed { i, view ->
            val text = view.findViewById<TextView>(R.id.navText)
            val icon = view.findViewById<ImageView>(R.id.navIcon)

            if (i == index) {
                view.isSelected = true
                text.visibility = View.VISIBLE
                icon.setColorFilter(ContextCompat.getColor(requireActivity(), android.R.color.white))

                // Load fragment based on index
//                val fragment = when (i) {
//                    0 -> HomeFragment()
//                    1 -> VitalsFragment()
//                    2 -> MedicineFragment()
//                    3 -> GoalsFragment()
//                    else -> HomeFragment()
//                }
//                supportFragmentManager.beginTransaction()
//                    .replace(R.id.fragmentContainer, fragment)
//                    .commit()

            } else {
                view.isSelected = false
                text.visibility = View.GONE
                icon.setColorFilter(ContextCompat.getColor(requireActivity(), R.color.blue))
            }
        }
    }
    fun dpToPx(dp: Int, context: Context): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}