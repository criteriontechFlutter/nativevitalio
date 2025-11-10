package com.critetiontech.ctvitalio.UI.fragments

import MoodData
import PrefsManager
import Vital
import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.media.AudioRecord
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.DailyTip
import com.critetiontech.ctvitalio.adapter.DailyTipAdapter
import com.critetiontech.ctvitalio.adapter.DashboardAdapter
import com.critetiontech.ctvitalio.adapter.IndicatorAdapter
import com.critetiontech.ctvitalio.adapter.NewChallengedAdapter
import com.critetiontech.ctvitalio.adapter.ProgressCard
import com.critetiontech.ctvitalio.adapter.TabMedicineAdapter
import com.critetiontech.ctvitalio.databinding.FragmentCorporateDashBoardBinding
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.utils.ToastUtils
import com.critetiontech.ctvitalio.utils.showRetrySnackbar
import com.critetiontech.ctvitalio.viewmodel.ChallengesViewModel
import com.critetiontech.ctvitalio.viewmodel.DashboardViewModel
import com.critetiontech.ctvitalio.viewmodel.PillsReminderViewModal
import com.google.android.material.snackbar.Snackbar
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.io.IOException


class CorporateDashBoard : Fragment() {
    private lateinit var binding: FragmentCorporateDashBoardBinding
    private lateinit var viewModel: DashboardViewModel
    private lateinit var challengesViewModel: ChallengesViewModel
    private lateinit var pillsViewModel: PillsReminderViewModal
    private lateinit var adapter: DashboardAdapter
    private lateinit var dailyTipAdapter: DailyTipAdapter
    private lateinit var indicatorAdapter: IndicatorAdapter
    private var voiceDialog: Dialog? = null
    private var snackbar: Snackbar? = null
    private val slideDelay: Long = 2100
    private val handler = Handler(Looper.getMainLooper())
    private var sliderRunnable: Runnable? = null
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var webSocket: WebSocket? = null
    private val RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    private val PERMISSION_REQUEST_CODE = 101
    var fragmentOpened = false
    private val tabLabels = listOf("Home", "Snaps", "Reminders", "Challenges")
    private val tabIcons = listOf(R.drawable.home, R.drawable.vitals_icon_home, R.drawable.pill,R.drawable.challenges_icon)
    private lateinit var navItems: List<View>

    private var authService: AuthorizationService? = null
    private var currentIndex = 0
    private val totalIndicators = 3
    private val moods = listOf(
        MoodData(5,"Spectacular", "#FFA4BA", R.drawable.spectulor_mood,  "#611829"),
        MoodData(6,"Upset", "#88A7FF",  R.drawable.upset_mood,  "#2A4089"),
        MoodData(1, "Stressed", "#FF9459",  R.drawable.stressed_mood, "#782E04"),
        MoodData(2,"Happy", "#9ABDFF",  R.drawable.happy_mood,"#505D87"),
        MoodData(4,"Good", "#F9C825",  R.drawable.good_mood, "#664F00"),
        MoodData(3,"Sad",   "#7DE7EE",  R.drawable.sad_mood,  "#3A7478")

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
        val screenHeight = resources.displayMetrics.heightPixels
        val halfHeight = screenHeight / 2f
        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        pillsViewModel = ViewModelProvider(this)[PillsReminderViewModal::class.java]

        pillsViewModel.getAllPatientMedication()


        binding.notificationIcon.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_wellnessMetrics)
        }

        navItems = listOf(
            view.findViewById(R.id.nav_home),
            view.findViewById(R.id.nav_vitals),
            view.findViewById(R.id.nav_medicine),
            view.findViewById(R.id.nav_goals)
        )
        viewModel.selectedMoodId.observe(viewLifecycleOwner) { moodId ->
            Log.d("TAG", "onViewCreated: $moodId");
            if (moodId==null || moodId.trim()=="null") {
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

                binding.tFeeling.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34f)

                val params = binding.tFeeling.layoutParams as ConstraintLayout.LayoutParams
                params.verticalBias = 0.1f  // move it down
                binding.tFeeling.layoutParams = params

                if (drawableRes != null) {
                    binding.ivIllustration.setImageResource(drawableRes)


                    val params = binding.ivIllustration.layoutParams as ConstraintLayout.LayoutParams
                    params.verticalBias = -0.14f  // move it down
                    binding.ivIllustration.layoutParams = params
                    val layoutParams = binding.ivIllustration.layoutParams
                    layoutParams.width = dpToPx(374, requireContext())   // 400dp → pixels
                    layoutParams.height = dpToPx(203, requireContext())
                    binding.ivIllustration.layoutParams = layoutParams


                }
            }
        }
        setupNav()
        setupBottomNav(view)
        viewModel.getMoodByPid()
        viewModel.getAllEnergyTankMaster()



        viewModel.fetchManualFluidIntake(uhid = PrefsManager().getPatient()?.empId.toString())
        binding.recyclerView.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_medicationFragment)
        }



        viewModel.getVitals()
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
        viewModel.fluidList.observe(viewLifecycleOwner) { list ->
            val waterQty = list
                .firstOrNull { it.id.toString() == "97694" }
                ?.amount?.toFloat() ?: 0f  // convert safely to Float


//            binding.intakeWaterId.text=waterQty.toString()



            val waterGoal = PrefsManager().getEmployeeGoals().find { it.vmId == 245 }


//            waterGoal?.let {
//                binding.waterGoalId.text = "/"+ (it.targetValue*1000).toString()+" ml"
//
//                val progress = (waterQty * 100f) / (it.targetValue * 1000f)
//                binding.intakeWaterId.text=waterQty.toString()
//// Now set progress
//                binding.waterproGress.setProgress(progress)
//
//
//            }


        }




        challengesViewModel.newChallenges.observe(viewLifecycleOwner) { list ->
            binding.newChallengedRecyclerView.adapter = NewChallengedAdapter(
                list,
                onItemClick =  { challenge ->
                    challengesViewModel.joinChallenge( challenge.id.toString())
                },
                onItemClick1 =  { challenge ->
                    val bundle = Bundle().apply {
                        putSerializable("challenges", challenge)
                    }
                     findNavController().navigate(R.id.action_dashboard_to_challengeDetailsFragment, bundle)

                }
            )
            binding.challengedId.adapter = NewChallengedAdapter(
                list,
                onItemClick =  { challenge ->
                    challengesViewModel.joinChallenge( challenge.id.toString())
                },
                onItemClick1 =  { challenge ->
                    val bundle = Bundle().apply {
                        putSerializable("challenges", challenge)
                    }
                    findNavController().navigate(R.id.action_dashboard_to_challengeDetailsFragment, bundle)

                }
            )

            binding.ringIcon.setOnClickListener {

                initializeAuth()
                handleAuthRedirectIntent(requireActivity().intent)




            }


binding.activechalgesId.text="Active Challenges ("+list.size.toString()+")"
            binding.activeChalleTextId.text="Active Challenges ("+list.size.toString()+")"

            setupActiveChallenges(list.size)
        }

        val stepsGoal = PrefsManager().getEmployeeGoals().find { it.vmId == 234 }
        val waterGoal = PrefsManager().getEmployeeGoals().find { it.vmId == 245 }
        val sleepGoal = PrefsManager().getEmployeeGoals().find { it.vmId == 243 }

        stepsGoal?.let {
           // binding.stepsGoalId.text = "/"+it.targetValue.toString()+" Steps"
        }

//        waterGoal?.let {
//            binding.waterGoalId.text = "/"+ (it.targetValue*1000).toString()+" ml"
//
//
//
//
//
//        }
//        binding.waterContId.setOnClickListener(){
//            findNavController().navigate(R.id.action_dashboard_to_fluidFragment ,null,)
//        }





        viewModel.fluidIntake(requireContext() , "245", "414")
        viewModel.getFoodIntake()
        val typeface = ResourcesCompat.getFont(requireActivity(), R.font.source_serif_pro)
        binding.tFeeling.setTypeface(typeface, Typeface.BOLD)
        binding.tFeelingBelow.setTypeface(typeface )

        var greetings= if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ToastUtils.getSimpleGreeting()
        } else {
            TODO("VERSION.SDK_INT < O")
        };

        binding.greetingHi.text = "${greetings}"
        binding.greetingName.text = "${PrefsManager().getPatient()?.patientName ?: ""}"

        viewModel.vitalList.observe(viewLifecycleOwner) { vitals ->
            // `vitals` is the latest value emitted by LiveData
            // Example: submit to RecyclerView adapter
            val vitalMovementIndex = vitals.find { it.vitalName.equals("MovementIndex", ignoreCase = true) }
            val vitalRecoveryIndex = vitals.find { it.vitalName.equals("RecoveryIndex", ignoreCase = true) }
            val activeMinutes = vitals.find { it.vitalName.equals("ActiveMinutes", ignoreCase = true) }
            val vitalStepsIndex = vitals.find { it.vitalName.equals("TotalSteps", ignoreCase = true) }
//            binding.tvMovementIndex.text=String.format("%.0f", vitalMovementIndex?.vitalValue)
//            binding.tvRecoveryIndex.text= String.format("%.0f", vitalRecoveryIndex?.vitalValue)
//            binding.tvStepss.text= String.format("%.0f", vitalStepsIndex?.vitalValue)+" steps"
//            binding.activeMinutess.text= String.format("%.0f", activeMinutes?.vitalValue)+" mins"
        }

        viewModel.quickMetricListList.observe(viewLifecycleOwner) { quickMetricListList ->
            val efficiencyMetric = quickMetricListList
                ?.firstOrNull { it.Title.equals("EFFICIENCY", ignoreCase = true) }

//            binding.sleepGoalId.text =  "Sleep Efficiency "+efficiencyMetric?.DisplayText ?: "-"

            val sleepId = quickMetricListList
                ?.firstOrNull { it.Title.equals("TOTAL SLEEP", ignoreCase = true) }
//            binding.sleepId.text =  sleepId?.DisplayText ?: "-"
        }


        // Animate to 80%
         val extras = FragmentNavigatorExtras(
            binding.avatar to "heroImageTransition",
            binding.greetingHi to "heroGreetingtextTransition"
        )
        binding.ivIllustration.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_moodFragment ,null,
                null)
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

        binding.energyCard.setOnClickListener{

            findNavController().navigate(R.id.action_dashboard_to_energyTank)
        }
//        binding.energyCard.setOnClickListener{
//            findNavController().navigate(R.id.action_dashboard_to_energyTank)
//        }
//         viewModel.latestEnergy.observe(viewLifecycleOwner) { energy ->
//
//
//             binding.energyTitle.text="You're feeling "+ viewModel.latestEnergy.value.toString() +"% energized today ⚡"
//
//
//
//        }
        viewModel.latestEnergy.observe(viewLifecycleOwner) { energy ->

            if (energy == null) {
                binding.energyTitle.text = "Your energy story awaits"
                binding.energySubtitle.text = "Log your energy for today ⚡"
                binding.energyImage.setImageResource(R.drawable.emtyp_energy)
                binding.energyid.setBackgroundResource(R.drawable.rounded_card_bg)
            } else {
                binding.energyTitle.text = "You're feeling $energy% energized today ⚡"
                binding.energySubtitle.text = "Ready to take on the day!"
                binding.energyImage.setImageResource(R.drawable.ic_meditation)
                binding.energyid.setBackgroundResource(R.drawable.bg_energy_gradient)
            }
        }


//        binding.linearLayout3.setOnClickListener()
//        {
//
//            findNavController().navigate(R.id.action_dashboard_to_wellnessMetrics )
//        }
//        binding.addvitalBtn.setOnClickListener()
//        {
//
//            findNavController().navigate(R.id.action_dashboard_to_connection )
//        }
//        binding.sleepContainerId.setOnClickListener()
//        {
//
//            findNavController().navigate(R.id.action_dashboard_to_sleepDetails)
//        }
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

//                binding.bpDataId.text = "${bpSys.vitalValue.toInt()}/${bpDia.vitalValue.toInt()}  "
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
//        binding.swipeRefreshLayout.setOnRefreshListener {
//            Handler().postDelayed({
//                 viewModel.getVitals()
//                binding.swipeRefreshLayout.isRefreshing = false // Stop the refresh animation
//            }, 2000)
//        }

// Vertical orientation


        val cards = listOf(
            ProgressCard("👍", "Progress", "Last night's sleep fuels today."),
            ProgressCard("🔥", "Energy!", "Your activity keeps momentum."),
            ProgressCard("💧", "Hydrate", "Water keeps focus sharp.")
        )

//        binding.progressViewPager.adapter = ProgressCardAdapter(cards)
//        binding.progressViewPager.orientation = ViewPager2 cv v
//        .ORIENTATION_VERTICAL

// Optional: smooth scale effect
        val transformer = ViewPager2.PageTransformer { page, position ->
            val r = 1 - kotlin.math.abs(position)
        }
//        binding.progressViewPager.setPageTransformer(transformer)
//        binding.dotsIndicator.setViewPager2(binding.progressViewPager)
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
        var isBoxVisible = false
        binding.viewAllSleepDataaId.visibility=View.GONE
binding.showId.showHideId.setOnClickListener{
    binding.viewAllSleepDataaId.visibility=View.VISIBLE
    binding.showId.showHideId.visibility=View.GONE
}
        binding.hideId.showHideId.setOnClickListener{
            binding.viewAllSleepDataaId.visibility=View.GONE
            binding.showId.showHideId.visibility=View.VISIBLE
        }


        viewModel.sleepValueList.observe(viewLifecycleOwner) { sleepValue  ->
        binding.sleepScoreId.title.text="Sleep Score"
    //    binding.sleepScoreId.cardValue.text=sleepValue.SleepScore.Score.toString()
        binding.sleepScoreId.statusCardId.visibility=View.GONE


    val totalSleep = sleepValue.QuickMetricsTiled
        ?.firstOrNull { it.Title.equals("TOTAL SLEEP", ignoreCase = true) }

    binding.totalSleepId.title.text="Total Sleep"
        binding.totalSleepId.value.text= HtmlCompat.fromHtml(totalSleep?.Value.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
//         binding.totalSleepId.cardStatus6.text= totalSleep?.Tag.toString()


    val efficiencyMetric = sleepValue.QuickMetrics
        ?.firstOrNull { it.Title.equals("EFFICIENCY", ignoreCase = true) }

        binding.sleepEfficiencyId.title.text="Sleep Efficiency"
        binding.sleepEfficiencyId.value.text= efficiencyMetric?.DisplayText.toString()
         binding.sleepEfficiencyId.statusCardId.visibility=View.GONE

    val timeinBed = sleepValue.QuickMetricsTiled
        ?.firstOrNull { it.Title.equals("TIME IN BED", ignoreCase = true) }
        binding.timeInBedId.title.text="Time in Bed"
        binding.timeInBedId.value.text=HtmlCompat.fromHtml(timeinBed?.Value.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
//        binding.timeInBedId.cardStatus6.text= timeinBed?.Tag.toString()




        binding.fulSleepCycleId.title.text="Full Sleep Cycle"
        binding.fulSleepCycleId.value.text="_"
         binding.fulSleepCycleId.statusCardId.visibility=View.GONE




    val rem_sleep = sleepValue.SleepStages
        ?.firstOrNull { it.Title.equals("REM Sleep", ignoreCase = true) }
        binding.remSleepId.title.text="REM Sleep"
        binding.remSleepId.value.text= rem_sleep?.StageTimeText.toString()
        binding.remSleepId.statusCardId.visibility=View.GONE


    val deep_sleep = sleepValue.SleepStages
        ?.firstOrNull { it.Title.equals("Deep Sleep", ignoreCase = true) }
        binding.deepSleepId.title.text="Deep Sleep"
        binding.deepSleepId.value.text=deep_sleep?.StageTimeText.toString()
        binding.deepSleepId.statusCardId.visibility=View.GONE


    val light_sleep = sleepValue.SleepStages
        ?.firstOrNull { it.Title.equals("Light Sleep", ignoreCase = true) }
        binding.lightSleepId.title.text="Llght Sleep"
        binding.lightSleepId.value.text=light_sleep?.StageTimeText.toString()
        binding.lightSleepId.statusCardId.visibility=View.GONE


    val restorative = sleepValue.QuickMetricsTiled
        ?.firstOrNull { it.Title.equals("RESTORATIVE SLEEP", ignoreCase = true) }
        binding.restorativeSleepId.title.text="Restorative Sleep"
        binding.restorativeSleepId.value.text==HtmlCompat.fromHtml(restorative?.Value.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
//         binding.restorativeSleepId.cardStatus6.text=restorative?.Tag.toString()



        binding.movementsId.title.text="Movements"
        binding.movementsId.value.text= sleepValue.MovementGraph?.Data?.size.toString()
         binding.movementsId.statusCardId.visibility=View.GONE


            val morningAlertness = sleepValue.MorningAlertness
                ?.Minutes
            binding.morningAlertnessId.title.text="Morning Alertness"
        binding.morningAlertnessId.value.text=morningAlertness
        binding.morningAlertnessId.hrId.text="mins"
        binding.morningAlertnessId.statusCardId.visibility=View.GONE

        binding.tossesAndTurnsId.title.text="Tosses and Turns"
        binding.tossesAndTurnsId.value.text="_"
        binding.tossesAndTurnsId.statusCardId.visibility=View.GONE




            binding.hideId.cardTitlse.text="Hide"




//            Activity



            binding.inactiveHoursId.title.text="Inactive Time"
            binding.inactiveHoursId.value.text="_"
            binding.inactiveHoursId.statusCardId.visibility=View.GONE



            binding.activieHoursId.title.text="Active Hours"
            binding.activieHoursId.value.text="_"
            binding.activieHoursId.statusCardId.visibility=View.GONE




            binding.WeeklyActiveMinutesId.title.text="Weekly Active Minutes"
            binding.WeeklyActiveMinutesId.value.text="_"
            binding.WeeklyActiveMinutesId.statusCardId.visibility=View.GONE


//            Recovery



            binding.StressRhythmScoreId.title.text="Stress Rhythm Score"
            binding.StressRhythmScoreId.value.text= "_"
            binding.StressRhythmScoreId.statusCardId.visibility=View.GONE

        }

        binding.stepsProgressId.ivStepsIcon.setImageResource(R.drawable.step_progress)
        binding.sleepProgressId.ivStepsIcon.setImageResource(R.drawable.sleep_progress)
        binding.waterProgressId.ivStepsIcon.setImageResource(R.drawable.water_progress)
        binding.glucoseProgressId.ivStepsIcon.setImageResource(R.drawable.glucose_progress)
        binding.bpProgressId.ivStepsIcon.setImageResource(R.drawable.bp_progress)
        binding.medicineProgressId.ivStepsIcon.setImageResource(R.drawable.medicine_progress)


        binding.stepsProgressId.progressSteps.progressTintList= ColorStateList.valueOf(Color.parseColor("#1281FD"))  // Blue
        binding.sleepProgressId.progressSteps.progressTintList=ColorStateList.valueOf(Color.parseColor("#00C67A"))  // Green
        binding.waterProgressId.progressSteps.progressTintList=ColorStateList.valueOf(Color.parseColor("#FEA33C"))  // Aqua
        binding.glucoseProgressId.progressSteps.progressTintList=ColorStateList.valueOf(Color.parseColor("#00C67A")) // Orange
        binding.bpProgressId.progressSteps.progressTintList=ColorStateList.valueOf(Color.parseColor("#1281FD"))     // Red
        binding.medicineProgressId.progressSteps.progressTintList=ColorStateList.valueOf(Color.parseColor("#FF3737")) // Purple


        binding.stepsProgressId.progressSteps.progress=60
        binding.sleepProgressId.progressSteps.progress=100
        binding.waterProgressId.progressSteps.progress=30
        binding.glucoseProgressId.progressSteps.progress=100
        binding.bpProgressId.progressSteps.progress=50
        binding.medicineProgressId.progressSteps.progress=10




        binding.stepsProgressId.tvStepsValue.text="6,040"
        binding.sleepProgressId.tvStepsValue.text="8h 14m"
        binding.waterProgressId.tvStepsValue.text="900ml"
        binding.glucoseProgressId.tvStepsValue.text="1/1"
        binding.bpProgressId.tvStepsValue.text="1/2"
        binding.medicineProgressId.tvStepsValue.text="1/7"

        binding.stepsProgressId.tvStepsLabel.text="Steps 60.4%"
        binding.sleepProgressId.tvStepsLabel.text="Sleep"
        binding.waterProgressId.tvStepsLabel.text="Water 30%"
        binding.glucoseProgressId.tvStepsLabel.text="Glucose 100%"
        binding.bpProgressId.tvStepsLabel.text="Blood Pressure 50%"
        binding.medicineProgressId.tvStepsLabel.text="Medicine 10%"



viewModel.vitalList.observe(viewLifecycleOwner) { vitalList ->
    val vitalStepsIndex = vitalList.find { it.vitalName.equals("TotalSteps", ignoreCase = true) }


    binding.StepsId.title.text="Steps"
    binding.StepsId.value.text= vitalStepsIndex?.vitalValue.toString()
    binding.StepsId.statusCardId.visibility=View.GONE

    val TemperatureBody = vitalList
        ?.firstOrNull { it.vitalName.equals("Temperature", ignoreCase = true) }
    binding.averageBodyTempId.title.text="Average Body Temp."
    binding.averageBodyTempId.value.text=  "${"%.1f".format(TemperatureBody?.vitalValue ?: 0.0)}"
    binding.averageBodyTempId.statusCardId.visibility=View.GONE

    val activeMinutes = vitalList
        ?.firstOrNull { it.vitalName.equals("ActiveMinutes", ignoreCase = true) }
    binding.ActiveminutesId.title.text="Active Minutes"
    binding.ActiveminutesId.value.text= activeMinutes?.vitalValue.toString()
    binding.ActiveminutesId.statusCardId.visibility=View.GONE
    val Temperature = vitalList
        ?.firstOrNull { it.vitalName.equals("Temperature", ignoreCase = true) }
    binding.tempDeviationId.title.text="Temperature Devoatoion"
    binding.tempDeviationId.value.text=   "${"%.1f".format(Temperature?.vitalValue ?: 0.0)}"
    binding.tempDeviationId.statusCardId.visibility=View.GONE

    val recoveryIndex = vitalList
        ?.firstOrNull { it.vitalName.equals("RecoveryIndex", ignoreCase = true) }
    binding.recoveryScoreId.title.text="Recovery Score"
    binding.recoveryScoreId.value.text= recoveryIndex?.vitalValue.toString()
    binding.recoveryScoreId.statusCardId.visibility=View.GONE

    val HRV = vitalList
        ?.firstOrNull { it.vitalName.equals("HRV", ignoreCase = true) }
    binding.lastNightHrvId.title.text="Last Night's HRV"
    binding.lastNightHrvId.value.text= HRV?.vitalValue.toString()
    binding.lastNightHrvId.statusCardId.visibility=View.GONE


    binding.SleepStageHrvId.title.text="Sleep Stage' HRV"
    binding.SleepStageHrvId.value.text=HRV?.vitalValue.toString()
    binding.SleepStageHrvId.statusCardId.visibility=View.GONE

    val movementIndex = vitalList
        ?.firstOrNull { it.vitalName.equals("MovementIndex", ignoreCase = true) }

    binding.movementIndexId.title.text="Movement Index"
    binding.movementIndexId.value.text=  movementIndex?.vitalValue.toString()
    binding.movementIndexId.statusCardId.visibility=View.GONE


    val sleepValue = vitalList
        ?.firstOrNull { it.vitalName.equals("Sleep Score", ignoreCase = true) }
    val movementValue = vitalList
        ?.firstOrNull { it.vitalName.equals("MovementIndex", ignoreCase = true) }
    val recoveryValue = vitalList
        ?.firstOrNull { it.vitalName.equals("RecoveryIndex", ignoreCase = true) }
    val stressValue = vitalList
        ?.firstOrNull { it.vitalName.equals("StressScore", ignoreCase = true) }


    binding.sleepProgressIds.sleepValue.text = sleepValue?.vitalValue?.toInt()?.toString() ?: "--"
    binding.sleepProgressIds.movementValue.text = movementValue?.vitalValue?.toInt()?.toString() ?: "--"
    binding.sleepProgressIds.recoveryValue.text = recoveryValue?.vitalValue?.toInt()?.toString() ?: "--"
    binding.sleepProgressIds.stressValue.text = stressValue?.vitalValue?.toInt()?.toString() ?: "--"
}









//        val tips = listOf(
//            DailyTip(R.drawable.ic_breathing, "Stress slightly high!", "A 3-min breathing break can help you reset.", "Start"),
//            DailyTip(R.drawable.vitals_icon_home, "Low Sleep Detected", "Try to sleep at least 7 hours tonight.", "Improve"),
//            DailyTip(R.drawable.ic_water, "Hydration Low", "Drink 2 more glasses of water today.", "Track")
//        )
//
//        dailyTipAdapter = DailyTipAdapter(tips)
//
//        binding.recyclerSlider.apply {
//            layoutManager = LinearLayoutManager(requireContext(),
// LinearLayoutManager.HORIZONTAL, false)
//            adapter = dailyTipAdapter
//
//            PagerSnapHelper().attachToRecyclerView(this)
//        }
//
//
//        setupIndicators(tips.size)
//
//
//
//        binding.recyclerSlider.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
//                    val currentPosition = layoutManager?.findFirstVisibleItemPosition() ?: 0
//                    updateIndicators(currentPosition)
//                }
//            }
//        })
//        binding.layoutIndicators.visibility = View.VISIBLE


        celebrationFun()
//        val tips = listOf(
//            DailyTip(R.drawable.ic_breathing, "Stress slightly high!", "A 3-min breathing break can help you reset.", "Start"),
//            DailyTip(R.drawable.vitals_icon_home, "Low Sleep Detected", "Try to sleep at least 7 hours tonight.", "Improve"),
//            DailyTip(R.drawable.ic_water, "Hydration Low", "Drink 2 more glasses of water today.", "Track")
//        )
//
//        dailyTipAdapter = DailyTipAdapter(tips)
//
//        binding.recyclerSlider.apply {
//            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
//            adapter = dailyTipAdapter
//            PagerSnapHelper().attachToRecyclerView(this)
//        }
//        binding.layoutIndicators.post {
//            setupIndicators(tips.size)
//        }
//// ✅ Call this AFTER setting adapter
//        setupIndicators(tips.size)
//
//// ✅ Listener to update dots when page changes
//        binding.recyclerSlider.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
//                    val currentPosition = layoutManager?.findFirstVisibleItemPosition() ?: 0
//                    updateIndicators(currentPosition)
//                }
//            }
//        })

        setupRecyclerAndIndicators()
        binding.sleepProgressIds.sleepContainerId.setOnClickListener(){
            findNavController().navigate(R.id.action_dashboard_to_sleepDetails)
        }
        binding.sleepProgressIds.sleepContainerId.setOnClickListener(){
            findNavController().navigate(R.id.action_dashboard_to_waterIntakeFragment)
        }
    }

     private fun setupRecyclerAndIndicators() {
        val tips = listOf(
            DailyTip(R.drawable.start_session, "Stress slightly high!", "A 3-min breathing break can help you reset.", "Start Session"),
            DailyTip(R.drawable.log_glucose, "No glucose reading  yet today", "Logging helps track stability.", "Log Glucose"),
            DailyTip(R.drawable.log_glucose, "Haven't checked your BP today!", "A quick reading, keeps your heart in check.", "Log BP"),
             DailyTip(R.drawable.log_glucose, "Hydration dropped since yesterday", " A quick refill can maintain energy.", "Add Intake"),

         )

        // Main tips RecyclerView




         dailyTipAdapter = DailyTipAdapter(tips) { tip ->
             // 👇 handle button clicks for each item
             when (tip.title) {
                 "Stress slightly high!" -> {
                     // Open breathing session
                     Toast.makeText(requireContext(), "Starting breathing exercise", Toast.LENGTH_SHORT).show()
                 }
                 "Low Sleep Detected" -> {
                     // Navigate to sleep tracker
                     Toast.makeText(requireContext(), "Improving sleep habits", Toast.LENGTH_SHORT).show()
                 }
                 "Hydration Low" -> {
                     // Log water intake
                     Toast.makeText(requireContext(), "Tracking water intake", Toast.LENGTH_SHORT).show()
                 }
             }
         }


        binding.recyclerSlider.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = dailyTipAdapter
            PagerSnapHelper().attachToRecyclerView(this)
        }

        // Indicators RecyclerView
        indicatorAdapter = IndicatorAdapter(tips.size)
        binding.layoutIndicators.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = indicatorAdapter
        }

        // Scroll listener to update indicators
        binding.recyclerSlider.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val currentPosition = layoutManager.findFirstVisibleItemPosition()
                    indicatorAdapter.updateSelectedPosition(currentPosition)
                }
            }
        })
    }

    private fun setupActiveChallenges(sizes: Int) {

        // Setup dots


        val challengeIndicatorAdapter = IndicatorAdapter(sizes)
        binding.activechalgesDotId.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = challengeIndicatorAdapter
        }


        // Listen for scroll changes
        // Declare once outside listener
        try {
            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(binding.newChallengedRecyclerView)
        }catch (e: Exception){

        }


// Now just listen for scroll idle events
        binding.newChallengedRecyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val currentPosition = layoutManager.findFirstVisibleItemPosition()
                    challengeIndicatorAdapter.updateSelectedPosition(currentPosition)
                }
            }
        })
    }

        fun celebrationFun() {
            val flingAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.fling_up_to_down)

            // These are actual views (not IDs)
            val confettiViews = listOf(
                binding.celebrationId.confetti1,
                binding.celebrationId.confetti2,
                binding.celebrationId.confetti3,
                binding.celebrationId.confetti4,
                binding.celebrationId.confetti5,
                binding.celebrationId.star1,
                binding.celebrationId.star2,
                binding.celebrationId.star3
            )

            // Start animation directly on each view
            confettiViews.forEach { view ->
                view.startAnimation(flingAnim)
            }
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
            val vitalsItem = view1.findViewById<View>(R.id.nav_vitals)
            vitalsItem.findViewById<ImageView>(R.id.navIcon)
                .setImageResource(R.drawable.vitals_icon_home)
            vitalsItem.findViewById<TextView>(R.id.navText).text = "Vitals"

            // Medicine
            val medicineItem = view1.findViewById<View>(R.id.nav_medicine)
            medicineItem.findViewById<ImageView>(R.id.navIcon)
                .setImageResource(R.drawable.reminders)
            medicineItem.findViewById<TextView>(R.id.navText).text = "Medicine"

            // Goals
            val goalsItem = view1.findViewById<View>(R.id.nav_goals)
            goalsItem.findViewById<ImageView>(R.id.navIcon)
                .setImageResource(R.drawable.challenges_icon)
            goalsItem.findViewById<TextView>(R.id.navText).text = "Goals"
        }


        private fun handleAuthRedirectIntent(intent: Intent) {
            val data = intent.data
            if (data != null && data.scheme == "com.critetiontech.ctvitalio" && data.host == "callback") {
                val accessToken = data.getQueryParameter("accessToken")
                val refreshToken = data.getQueryParameter("refreshToken")
                val tokenType = data.getQueryParameter("tokenType")
                val expiry = data.getQueryParameter("expiresIn")

                if (accessToken != null) {
                    Log.d("OAuth", "Received code: $accessToken and state: $tokenType")
                    Log.d("OAuth", "Received refreshToken: $refreshToken")
                    Log.d("OAuth", "Received refreshToken: $tokenType")
                    Log.d("OAuth", "Received refreshToken: $expiry")
                    viewModel.insertUltraHumanToken(accessToken, refreshToken, tokenType, expiry)
                    Toast.makeText(context, "Check$refreshToken", Toast.LENGTH_LONG)
                    exchangeCodeForToken(accessToken)
                } else {
                    Log.e("OAuth", "No authorization code found in redirect URI")
                }
            }
        }

        fun onNewIntentReceived(intent: Intent) {
            handleAuthRedirectIntent(intent = intent)
        }

        private fun initializeAuth() {
            authService = AuthorizationService(requireContext())
            startOAuthFlow()
        }

        private fun startOAuthFlow() {
            val authUri = "https://auth.ultrahuman.com/authorise".toUri()
            val tokenUri = "https://partner.ultrahuman.com/oauth/token".toUri()
            val redirectUri = "https://vitalioapi.medvantage.tech:5082/callback".toUri()

            val serviceConfig = AuthorizationServiceConfiguration(authUri, tokenUri)

            val authRequest = AuthorizationRequest.Builder(
                serviceConfig,
                "W3hWLU2juogFGfgJBdpj3uuaI1n876CwvalFCIFEBKo",
                ResponseTypeValues.CODE,
                redirectUri
            ).setScope("profile ring_data cgm_data ring_extended_data").build()

            val intent = authService!!.getAuthorizationRequestIntent(authRequest)
            startActivity(intent)

        }

        private fun exchangeCodeForToken(authCode: String) {
            val tokenUrl = "https://partner.ultrahuman.com/oauth/token"
            val clientId = "W3hWLU2juogFGfgJBdpj3uuaI1n876CwvalFCIFEBKo"
            val redirectUri = "https://vitalioapi.medvantage.tech:5082/callback"

            val requestBody =
                "grant_type=authorization_code&code=$authCode&redirect_uri=$redirectUri&client_id=$clientId&state=${PrefsManager().getPatient()?.emailID}|1"

            val request = Request.Builder()
                .url(tokenUrl)
                .addHeader("Content-Type", "application/json")
                .post(okhttp3.RequestBody.create(null, requestBody))
                .build()

            OkHttpClient().newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    Log.e("OAuth", "Token request failed: ${e.message}")
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    response.use {
                        val body = it.body?.string()
                        Log.d("OAuth", "Token response: $body")
                    }
                }
            })
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
                    icon.setColorFilter(
                        ContextCompat.getColor(
                            requireActivity(),
                            android.R.color.white
                        )
                    )
                    val fragment = when (i) {
                        0 -> {
                            binding.homeId.visibility = View.VISIBLE
                            binding.challengedId.visibility = View.GONE
                            binding.activeChalleTextId.visibility = View.GONE

                            binding.recyclerView.visibility = View.GONE
                            binding.healthSnaps.visibility = View.GONE

                        }

                        1 -> {
                            binding.homeId.visibility = View.GONE
                            binding.challengedId.visibility = View.GONE
                            binding.activeChalleTextId.visibility = View.GONE

                            binding.recyclerView.visibility = View.GONE
                            binding.healthSnaps.visibility = View.VISIBLE

                        }

                        2 -> {
                            binding.recyclerView.visibility = View.VISIBLE
                            binding.recyclerView.layoutManager =
                                LinearLayoutManager(requireContext())

                            // Initialize adapter once with an empty list
                            val adapter = TabMedicineAdapter(mutableListOf()) { selectedMedicine ->
                                findNavController().navigate(R.id.action_dashboard_to_medicationFragment)
                            }
                            binding.recyclerView.adapter = adapter

                            // Observe pill list updates
                            pillsViewModel.pillList.observe(viewLifecycleOwner) { list ->
                                if (list.isNotEmpty()) {
                                    adapter.updateList(list) // Update existing adapter data
                                    binding.recyclerView.visibility = View.VISIBLE
                                    //  binding.tvNoData.visibility = View.GONE
                                } else {
                                    binding.recyclerView.visibility = View.GONE
                                    // binding.tvNoData.visibility = View.VISIBLE
                                }
                            }

                            // Hide other views
                            binding.homeId.visibility = View.GONE
                            binding.challengedId.visibility = View.GONE
                            binding.activeChalleTextId.visibility = View.GONE
                            binding.healthSnaps.visibility = View.GONE
                        }

                        3 -> {
                            binding.homeId.visibility = View.GONE
                            binding.challengedId.visibility = View.VISIBLE
                            binding.activeChalleTextId.visibility = View.VISIBLE

                            binding.recyclerView.visibility = View.GONE
                            binding.healthSnaps.visibility = View.GONE

                        }

//                    else -> HomeFragment()
//                    1 -> VitalsFragment()
//                    2 -> MedicineFragment()
//                    3 -> GoalsFragment()
                        else -> {}
                    }
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