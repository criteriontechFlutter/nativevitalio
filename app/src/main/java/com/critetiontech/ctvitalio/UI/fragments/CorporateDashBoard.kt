package com.critetiontech.ctvitalio.UI.fragments

import DailyCheckItem
import MoodData
import PrefsManager
import SleepValue
import SleepVital
import Vital
import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.Shader
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
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
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
import com.critetiontech.ctvitalio.UI.UltraHumanActivity
 import com.critetiontech.ctvitalio.adapter.DashboardAdapter
import com.critetiontech.ctvitalio.adapter.IndicatorAdapter
import com.critetiontech.ctvitalio.adapter.MedicationReminderAdapter
import com.critetiontech.ctvitalio.adapter.NewChallengedAdapter
import com.critetiontech.ctvitalio.adapter.ProgressCard
import com.critetiontech.ctvitalio.adapter.TabMedicineAdapter
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.utils.ToastUtils
import com.critetiontech.ctvitalio.utils.showRetrySnackbar
import com.critetiontech.ctvitalio.viewmodel.ChallengesViewModel
import com.critetiontech.ctvitalio.viewmodel.DashboardViewModel
import com.critetiontech.ctvitalio.viewmodel.PillsReminderViewModal
import com.google.android.material.snackbar.Snackbar
import net.openid.appauth.AuthorizationService
import okhttp3.WebSocket
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.critetiontech.ctvitalio.UI.BaseActivity
import com.critetiontech.ctvitalio.adapter.DailyTipAdapter
import com.critetiontech.ctvitalio.adapter.PriorityAction
import com.critetiontech.ctvitalio.databinding.DailyChecklistWedgetBinding
import com.critetiontech.ctvitalio.databinding.FragmentCorporateDashBoardBinding
import com.critetiontech.ctvitalio.databinding.SleepLayoutBinding


class CorporateDashBoard : Fragment() {
    private lateinit var binding: FragmentCorporateDashBoardBinding
    private lateinit var viewModel: DashboardViewModel
    private lateinit var challengesViewModel: ChallengesViewModel
    private lateinit var pillsViewModel: PillsReminderViewModal
    private lateinit var adapter: DashboardAdapter
    private lateinit var medicationReminderAdapter: MedicationReminderAdapter
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
    private var isFabOpen = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCorporateDashBoardBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SuspiciousIndentation", "SetTextI18n", "UseKtx")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val screenHeight = resources.displayMetrics.heightPixels
        val halfHeight = screenHeight / 2f
        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        pillsViewModel = ViewModelProvider(this)[PillsReminderViewModal::class.java]
        pillsViewModel.getAllPatientMedication()
        binding.notificationIcon.setOnClickListener {

        }

        binding.headerContainer.setOnClickListener {

            findNavController().navigate(R.id.action_dashboard_to_new_corporate_dashboard)

        }
        binding.progressCircler.animateProgress(10f)
        animatePageLoad()
        (requireActivity() as? BaseActivity)?.setSystemBarsColor(
            statusBarColor = R.color.primaryBlue,
            navBarColor = R.color.white,
            lightIcons = true
        )
//        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
//            if (isLoading) showLoading() else hideLoading()
//        }

        binding.fabIcon.setOnClickListener {
            if (!isFabOpen) {
                // +  →  X
                binding.fabIcon.animate().rotation(135f).setDuration(300).start()
                binding.fabIcon.setImageResource(R.drawable.raddimg)
                isFabOpen = true
                showPopup()
            } else {
                // X  →  +
                binding.fabIcon.animate().rotation(0f).setDuration(300).start()
                binding.fabIcon.setImageResource(R.drawable.raddimg)
                isFabOpen = false
                hidePopup()
            }

        }

//        binding.blurOverlay.setOnClickListener {
//
//        }


        navItems = listOf(
            view.findViewById(R.id.nav_home),
            view.findViewById(R.id.nav_vitals),
            view.findViewById(R.id.nav_medicine),
            view.findViewById(R.id.nav_goals)
        )
        viewModel.selectedMoodId.observe(viewLifecycleOwner) { moodId ->
            Log.d("TAG", "onViewCreated: $moodId")
            if (moodId.isNullOrBlank() || moodId.equals("null", ignoreCase = true)) {
                binding.ivIllustration.setImageResource(R.drawable.moods)
                binding.tFeelingBelow.visibility = View.VISIBLE
                val text = "How are you feeling now?"
                val spannable = SpannableString(text)
                val start = text.indexOf("feeling")
                val end = start + "feeling".length
                spannable.setSpan(
                    ForegroundColorSpan(Color.parseColor("#FFA500")),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                binding.tFeeling.text = spannable
            } else {
                val mood = moods.find { it.id.toString() == moodId.toString() }
                if (mood != null) {
                    binding.tFeelingBelow.visibility = View.GONE
                    binding.tFeeling.text = "Feeling ${mood.name}"
                    binding.tFeeling.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34f)
                    (binding.tFeeling.layoutParams as ConstraintLayout.LayoutParams).apply {
                        verticalBias = 0.1f
                        binding.tFeeling.layoutParams = this
                    }
                    binding.ivIllustration.setImageResource(mood.emojiRes)
                    (binding.ivIllustration.layoutParams as ConstraintLayout.LayoutParams).apply {
                        verticalBias = -0.14f
                        binding.ivIllustration.layoutParams = this
                    }
                    binding.ivIllustration.layoutParams.apply {
                        width = dpToPx(374, requireContext())
                        height = dpToPx(203, requireContext())
                        binding.ivIllustration.layoutParams = this
                    }
                }
            }
        }
        setupBottomNav(view)
        viewModel.getMoodByPid()
        viewModel.getAllEnergyTankMaster()
        setupNav()
        viewModel.fetchManualFluidIntake(uhid = PrefsManager().getPatient()?.empId.toString())
        binding.recyclerView.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_medicationFragment)
        }

        binding.myHealthCard.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_wellnessMetrics)
        }

        binding.goalCard.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_smartGoalFragment)
        }

        binding.mindfulness.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_mindfulnessFragment)
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
//            binding.sleepProgressIds.dashboardAnimatedCard.setWaveColors(
//                backgroundColor = "#DFFFE9".toColorInt(),
//                backWaveColor = "#DFFFE9".toColorInt(),
//                frontWaveColor = "#DFFFE9".toColorInt()
//            )



//            binding.intakeWaterId.text=waterQty.toString()

//binding.healthTrackId.healthGoalAchived.setOnClickListener {
//    findNavController().navigate(R.id.action_dashboard_to_smartGoalFragment)
//}

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
                list.toMutableList(),
                onJoinClick  =  { challenge ->
                    challengesViewModel.joinChallenge( challenge.id.toString())
                },
                onDetailsClick  =  { challenge ->
                    val bundle = Bundle().apply {
                        putSerializable("challenges", challenge)
                    }
                     findNavController().navigate(R.id.action_dashboard_to_challengeDetailsFragment, bundle)

                }
            )
            binding.challengedId.adapter = NewChallengedAdapter(
                list.toMutableList(),
                onJoinClick =  { challenge ->
                    challengesViewModel.joinChallenge( challenge.id.toString())
                },
                onDetailsClick =  { challenge ->
                    val bundle = Bundle().apply {
                        putSerializable("challenges", challenge)
                    }
                    findNavController().navigate(R.id.action_dashboard_to_challengeDetailsFragment, bundle)

                }
            )

            binding.ringIcon.setOnClickListener {

                startActivity(Intent(requireActivity(), UltraHumanActivity::class.java))
//



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
           // findNavController().navigate(R.id.action_dashboard_to_waterIntakeFragment)
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

        binding.bpPopupId.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_connection )
        }
         binding.glucosePopupId.setOnClickListener {
             val bundle = Bundle().apply {
                 putString("vitalType", "Glucose")
             }
             findNavController().navigate(R.id.action_dashboard_to_connection, bundle)
        }

           binding.popupActivityId.setOnClickListener {
               findNavController().navigate(R.id.action_dashboard_to_addActivityFragment)
        }


        binding.popupAddMedicineId.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_addMedicineReminderFragment)
        }


//        binding.addvitalBtn.setOnClickListener()
//        {
//
//            findNavController().navigate(R.id.action_dashboard_to_connection )
//        }
//        binding.addGlucoseBtn.setOnClickListener()
//        {
//            val bundle = Bundle().apply {
//                putString("vitalType", "Glucose")
//            }
//            findNavController().navigate(R.id.action_dashboard_to_connection, bundle)
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
            val Glucose = vitalList.find { it.vitalName.equals("Glucose", ignoreCase = true) }

            val filtered = vitalList.filterNot {
                it.vitalName.equals("BP_Sys", ignoreCase = true) ||
                        it.vitalName.equals("BP_Dias", ignoreCase = true)
            }.toMutableList()

            val finalVitalList = mutableListOf<Vital>()

            if (bpSys != null && bpDia != null) {
                val bpVital = Vital().apply {
                    vitalName = "Blood Pressure"
                    vitalValue = 0.0 // Optional placeholder
                    unit = "${bpSys.vitalValue?.toInt()}/${bpDia.vitalValue?.toInt()}  "
                    vitalDateTime = bpSys.vitalDateTime
                }

//                binding.bpDataId.text = "${bpSys.vitalValue?.toInt()}/${bpDia.vitalValue.toString()}  "
//                binding.glucoseDataId.text = Glucose?.vitalValue.toString()
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
        initializeSwipeRefresh()

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
            binding.sleepScoreId.title.text = "Sleep Score"
            //    binding.sleepScoreId.cardValue.text=sleepValue.SleepScore.Score.toString()
            binding.sleepScoreId.statusCardId.visibility = View.GONE


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
        binding.lightSleepId.title.text="Light Sleep"
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
            Glide.with(this).asGif().load(R.raw.celebrate).into(binding.celebrationId.trophyIcon)


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



viewModel.vitalList.observe(viewLifecycleOwner) { vitalList ->
    val vitalStepsIndex = vitalList.find { it.vitalName.equals("TotalSteps", ignoreCase = true) }


    binding.StepsId.title.text = "Steps"
    binding.StepsId.value.text = vitalStepsIndex?.vitalValue.toString()
    binding.StepsId.statusCardId.visibility = View.GONE

    val TemperatureBody = vitalList
        ?.firstOrNull { it.vitalName.equals("Temperature", ignoreCase = true) }
    binding.averageBodyTempId.title.text = "Average Body Temp."
    binding.averageBodyTempId.value.text = "${"%.1f".format(TemperatureBody?.vitalValue ?: 0.0)}"
    binding.averageBodyTempId.statusCardId.visibility = View.GONE

    val activeMinutes = vitalList
        ?.firstOrNull { it.vitalName.equals("ActiveMinutes", ignoreCase = true) }
    binding.ActiveminutesId.title.text = "Active Minutes"
    binding.ActiveminutesId.value.text = activeMinutes?.vitalValue.toString()
    binding.ActiveminutesId.statusCardId.visibility = View.GONE
    val Temperature = vitalList
        ?.firstOrNull { it.vitalName.equals("TemperatureTemperature", ignoreCase = true) }
    binding.tempDeviationId.title.text = "Temperature Devoatoion"
    binding.tempDeviationId.value.text = "${"%.1f".format(Temperature?.vitalValue ?: 0.0)}"
    binding.tempDeviationId.statusCardId.visibility = View.GONE

    val recoveryIndex = vitalList
        ?.firstOrNull { it.vitalName.equals("RecoveryIndex", ignoreCase = true) }
    binding.recoveryScoreId.title.text = "Recovery Score"
    binding.recoveryScoreId.value.text = recoveryIndex?.vitalValue.toString()
    binding.recoveryScoreId.statusCardId.visibility = View.GONE

    val HRV = vitalList
        ?.firstOrNull { it.vitalName.equals("HRV", ignoreCase = true) }
    binding.lastNightHrvId.title.text = "Last Night's HRV"
    binding.lastNightHrvId.value.text = HRV?.vitalValue.toString()
    binding.lastNightHrvId.statusCardId.visibility = View.GONE


    binding.SleepStageHrvId.title.text = "Sleep Stage' HRV"
    binding.SleepStageHrvId.value.text = HRV?.toString()
    binding.SleepStageHrvId.statusCardId.visibility = View.GONE

    val movementIndex = vitalList
        ?.firstOrNull { it.vitalName.equals("MovementIndex", ignoreCase = true) }

    binding.movementIndexId.title.text = "Movement Index"
    binding.movementIndexId.value.text = movementIndex?.vitalValue.toString()
    binding.movementIndexId.statusCardId.visibility = View.GONE


//    val sleepValue = vitalList
//        ?.firstOrNull { it.vitalName.equals("Sleep Score", ignoreCase = true) }
//    val movementValue = vitalList
//        ?.firstOrNull { it.vitalName.equals("MovementIndex", ignoreCase = true) }
//    val recoveryValue = vitalList
//        ?.firstOrNull { it.vitalName.equals("RecoveryIndex", ignoreCase = true) }
//    val stressValue = vitalList
//        ?.firstOrNull { it.vitalName.equals("StressScore", ignoreCase = true) }
//    val sleep = sleepValue?.vitalValue?.toString()?.toFloatOrNull() ?: 0f
//    val stress = stressValue?.vitalValue?.toString()?.toFloatOrNull() ?: 0f
//    val movement = movementValue?.vitalValue?.toString()?.toFloatOrNull() ?: 0f
//    val recovery = recoveryValue?.vitalValue?.toString()?.toFloatOrNull() ?: 0f
//    val WellnessScore = vitalList
//        ?.firstOrNull { it.vitalName.equals("Wellness Score", ignoreCase = true) }
//    binding.sleepProgressIds.wellnessScoreNumber.text= WellnessScore?.vitalValue?.roundToInt().toString()
// Call the function safely
//   val wellnessscore= calculateWellnessScore(
//        sleep = sleep.toInt(),
//        stress = stress.toInt(),
//        movement = movement.toInt(),
//        recovery = recovery.toInt()
//    )
//    binding.sleepProgressIds.wellnessScoreNumber.text=wellnessscore.toString()

// Update UI safely
//    binding.sleepProgressIds.sleepValue.text = if (sleep > 0) sleep.toInt().toString() else "--"
//    binding.sleepProgressIds.stressValue.text = if (stress > 0) stress.toInt().toString() else "--"
//    binding.sleepProgressIds.movementValue.text = if (movement > 0) movement.toInt().toString() else "--"
//    binding.sleepProgressIds.recoveryValue.text = if (recovery > 0) recovery.toInt().toString() else "--"
}


    viewModel.vitalInsights.observe(viewLifecycleOwner) { vitals ->
        val RecoveryIndex = vitals?.find { it.vitalID == 240 }
        val MovementIndex = vitals?.find { it.vitalID == 241 }
         val StressScore = vitals?.find { it.vitalID == 252 }
        val Glucose = vitals?.find { it.vitalID == 249 }

        // Text colors
        binding.sleepProgressIds.recoverystatusId.apply {
            text = RecoveryIndex?.severityLevel ?: "--"
            setTextColor(Color.parseColor(RecoveryIndex?.colourCode ?: "#EF4444"))
        }

        binding.sleepProgressIds.sleepstatusId.apply {
            text = RecoveryIndex?.severityLevel ?: "--"
            setTextColor(Color.parseColor(RecoveryIndex?.colourCode ?: "#EF4444"))
        }

        binding.sleepProgressIds.movementstatusId.apply {
            text = MovementIndex?.severityLevel ?: "--"
            setTextColor(Color.parseColor(MovementIndex?.colourCode ?: "#EF4444"))
        }

        binding.sleepProgressIds.stressstatusId.apply {
            text = StressScore?.severityLevel ?: "--"
            setTextColor(Color.parseColor(StressScore?.colourCode ?: "#EF4444"))
        }

        // Helper function for background color with opacity
        fun getColorWithOpacity(hexColor: String?, alphaPercent: Int = 74): Int {
            val color = Color.parseColor(hexColor ?: "#FFFFFF")
            val alpha = (alphaPercent / 100f * 255).toInt()
            return ColorUtils.setAlphaComponent(color, alpha)
        }

        // Background colors with 74% opacity
//        binding.sleepProgressIds.sleepstatusId.setBackgroundColor(
//            getColorWithOpacity(RecoveryIndex?.colourCode)
//        )
        binding.sleepProgressIds.movementstatusId.setBackgroundColor(
            getColorWithOpacity(MovementIndex?.colourCode)
        )
        binding.sleepProgressIds.recoverystatusId.setBackgroundColor(
            getColorWithOpacity(RecoveryIndex?.colourCode)
        )
        binding.sleepProgressIds.stressstatusId.setBackgroundColor(
            getColorWithOpacity(StressScore?.colourCode)
        )

    }

        binding.sleepProgressIds.addSleepActivityBtn.setOnClickListener(){

            findNavController().navigate(R.id.action_dashboard_to_sleepDetails)

        }
        binding.sleepProgressIds.addMovementActivityBtn.setOnClickListener(){

            findNavController().navigate(R.id.action_dashboard_to_sleepDetails)
        }
        binding.sleepProgressIds.addStressActivityBtn.setOnClickListener(){
            findNavController().navigate(R.id.action_dashboard_to_sleepDetails)

        }
        binding.sleepProgressIds.addRecoveryActivityBtn.setOnClickListener(){
            findNavController().navigate(R.id.action_dashboard_to_sleepDetails)

        }












        setupRecyclerAndIndicators()
//        binding.sleepProgressIds.sleepContainerId.setOnClickListener(){
//            findNavController().navigate(R.id.action_dashboard_to_sleepDetails)
//        }
        binding.sleepProgressIds.sleepContainerId.setOnClickListener(){
           findNavController().navigate(R.id.action_dashboard_to_waterIntakeFragment)
        }


        pillsViewModel.pillList.observe(viewLifecycleOwner) { list ->
            // Initialize the adapter with your list and callback
            val medicationReminderAdapter = MedicationReminderAdapter(list.toMutableList()) { medicine ->
                // Handle Mark Taken click
                // Example: Update database or notify ViewModel
             }

            binding.medicationsId.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = medicationReminderAdapter
            }
        }



        binding.activeChalleTextId.setOnClickListener {

            findNavController().navigate(R.id.action_dashboard_to_newChallengeFragment)
        }
        binding.activechalgesId.setOnClickListener {

            findNavController().navigate(R.id.action_dashboard_to_newChallengeFragment)
        }
        binding.sleepProgressIds.SleepWelnessId.setOnClickListener {

            findNavController().navigate(R.id.action_dashboard_to_sleepDetails)
        }
        initHydrationControls()
        updateProgress(consumed =  2, target =  22, unit =  "ml")
        updateHydrationTitle()
//        wellnessDataBind()


binding.healthGoalAchived.healthGoalAchived.setOnClickListener {
    findNavController().navigate(R.id.action_dashboard_to_smartGoalFragment)
}
        viewModel.dailyCheckList.observe(viewLifecycleOwner) { list ->
            if (list.isNotEmpty()) {
                bindDailyChecklistSummary(list)
                bindDailyChecklistGoals(list)
                bindDailyChecklistProgress(list)
            }
        }


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
         wellnessDataBind()
    }

    private fun wellnessDataBind() {

        viewModel.insightWrapperList.observe(viewLifecycleOwner) { insight ->

            insight ?: return@observe

            val wellness = binding.sleepProgressIds

            // =======================
            // HEADER SECTION
            // =======================
            wellness.wellnessScoreNumber.text = insight.wellnessScore.toString()
            wellness.wellnessDescriptions.text = insight.wellnessMessage


            // =======================
            // SLEEP SECTION
            // =======================
            val sleep = insight.insights.sleep
            val sleepColor = sleep.colorCode.toColorInt()

            wellness.sleepstatusId.text = sleep.quality
            wellness.sleepValue.text =insight.scores.sleepScore.toString().toInt().toString()
            wellness.sleepContainertextId.setTextOrHide(sleep.message)

            wellness.sleepstatusId.setTextColor(sleepColor)
            wellness.sleepstatusId.backgroundTintList =
                ColorStateList.valueOf(sleepColor.withAlpha(0.15f))


            // =======================
            // MOVEMENT SECTION
            // =======================
            val movement = insight.insights.movement
            val movementColor = movement.colorCode.toColorInt()

            wellness.movementstatusId.text = movement.progress
            wellness.movementValue.text = movement.score.toInt().toString()
            wellness.movementContainertextId.setTextOrHide(movement.message)

            wellness.movementstatusId.setTextColor(movementColor)
            wellness.movementstatusId.backgroundTintList =
                ColorStateList.valueOf(movementColor.withAlpha(0.15f))


            // =======================
            // STRESS SECTION
            // =======================
            val stress = insight.insights.stress
            val stressColor = Color.parseColor(stress.colorCode)

            wellness.stressstatusId.text = stress.level
            wellness.stressValue.text = stress.score.toInt().toString()
            wellness.stressContainertextId.setTextOrHide(stress.message)

            wellness.stressstatusId.setTextColor(stressColor)
            wellness.stressstatusId.backgroundTintList =
                ColorStateList.valueOf(stressColor.withAlpha(0.15f))


            // =======================
            // RECOVERY SECTION
            // =======================
            val recovery = insight.insights.recovery
            val recoveryColor = Color.parseColor(recovery.colorCode)

            wellness.recoverystatusId.text = recovery.status
            wellness.recoveryValue.text = recovery.score.toInt().toString()
            wellness.recoveryContainertextId.setTextOrHide(recovery.message)

            wellness.recoverystatusId.setTextColor(recoveryColor)
            wellness.recoverystatusId.backgroundTintList =
                ColorStateList.valueOf(recoveryColor.withAlpha(0.15f))
        }
    }


    fun TextView.setTextOrHide(value: String?) {
        if (value.isNullOrBlank()) {
            this.visibility = View.GONE
        } else {
            this.visibility = View.VISIBLE
            this.text = value
        }
    }

fun Int.withAlpha(alpha: Float): Int {
        val a = (alpha * 255).toInt().coerceIn(0, 255)
        return (this and 0x00FFFFFF) or (a shl 24)
    }
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
    private fun bindIndividualGoals(sleepList: List<SleepVital>) {

        binding.healthGoalAchived.goalsContainer.removeAllViews()

        sleepList.forEach { item ->

            val goalView = layoutInflater.inflate(
                R.layout.goal_item,
                binding.healthGoalAchived.goalsContainer,
                false
            )

            val icon = goalView.findViewById<ImageView>(R.id.goalIcon)
            val label = goalView.findViewById<TextView>(R.id.goalLabel)

            // Set Label
            label.text = item.Title

            // Set Icon Based on State
            when (item.State.lowercase()) {
                "good", "optimal" -> {
                    icon.setImageResource(R.drawable.check_green)
                    label.setTextColor("#1A1A1A".toColorInt())
                }
                else -> {
                    icon.setImageResource(R.drawable.check_grey)
                    label.setTextColor("#AAAAAA".toColorInt())
                }
            }

            // Add the goal view
            binding.healthGoalAchived.goalsContainer.addView(goalView)
        }
    }
    private fun bindDailyChecklistSummary(list: List<DailyCheckItem>) {

        val totalGoals = list.size
        val achievedGoals = list.count { it.isGoalAchieved == 1 }
        val percentage = ((achievedGoals.toDouble() / totalGoals) * 100).toInt()

        binding.healthGoalAchived.apply {

            title.text = when {
                percentage == 100 -> "Excellent!"
                percentage >= 50 -> "Nice start!"
                percentage > 0 -> "Keep going!"
                else -> "Let's begin!"
            }

            subtitle.text = "$achievedGoals of $totalGoals goals achieved"
            tvPercentage.text = "$percentage%"
            progressBar.progress = percentage
        }
    }
    private fun bindDailyChecklistGoals(list: List<DailyCheckItem>) {

        binding.healthGoalAchived.goalsContainer.removeAllViews()

        list.forEach { item ->

            val view = layoutInflater.inflate(
                R.layout.goal_item,
                binding.healthGoalAchived.goalsContainer,
                false
            )

            val icon = view.findViewById<ImageView>(R.id.goalIcon)
            val label = view.findViewById<TextView>(R.id.goalLabel)

            label.text = item.goalName

            if (item.isGoalAchieved == 1) {
                icon.setImageResource(R.drawable.check_green)
                label.setTextColor("#1A1A1A".toColorInt())
            } else {
                icon.setImageResource(R.drawable.check_grey)
                label.setTextColor("#AAAAAA".toColorInt())
            }

            binding.healthGoalAchived.goalsContainer.addView(view)
        }
    }
    private fun bindDailyChecklistProgress(list: List<DailyCheckItem>) {

        binding.checklistContainer.removeAllViews()

        list.forEach { item ->

            val itemBinding = DailyChecklistWedgetBinding.inflate(
                layoutInflater, binding.checklistContainer, false
            )

            val progress = ((item.vitalValue / item.targetValue.toInt()) * 100).toInt()
            itemBinding.progressSteps.progress = progress

            itemBinding.tvStepsLabel.text ="${item.goalName} $progress"
            itemBinding.tvStepsValue.text = "${item.vitalValue.toInt()} / ${item.targetValue}"

            when (item.isGoalAchieved) {
                1 -> itemBinding.ivStepsIcon.setColorFilter(Color.GREEN)
                0 -> itemBinding.ivStepsIcon.setColorFilter(Color.RED)
            }

            binding.checklistContainer.addView(itemBinding.root)
        }
    }
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("SuspiciousIndentation")
private fun initHydrationControls() {
      var currentAmount = 200
      val unit = "ml"
    viewModel.getDailyEmployeeFluidIntake()
        // PLUS

        binding.hydrationCardId.btnPlus.setOnClickListener {
            currentAmount += 50
            binding.hydrationCardId.tvAmount.text = "$currentAmount $unit"
        }

        // MINUS
        binding.hydrationCardId.btnMinus.setOnClickListener {
            if (currentAmount > 0) {
                currentAmount -= 50
                binding.hydrationCardId.tvAmount.text = "$currentAmount $unit"
            }
        }

    binding.hydrationCardId.mainId.setOnClickListener {

        findNavController().navigate(R.id.action_dashboard_to_waterIntakeFragment)
    }
        // ADD
        binding.hydrationCardId.btnAddIntake.setOnClickListener {

            viewModel.fluidIntake(  currentAmount.toString())

        }
    }

    private fun animatePageLoad() {

        // Slide constraintLayout from top
//        binding.constraintLayout.apply {
//            translationY = -300f  // start above screen
//            alpha = 0f
//            animate()
//                .translationY(0f)
//                .alpha(1f)
//                .setDuration(1200)
//                .setInterpolator(DecelerateInterpolator())
//                .start()
//        }

        // Slide swipeRefreshLayout from bottom
        binding.tFeeling.apply {
            translationY = 700f  // start below screen
            alpha = 0f
            animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(1400)
                .setStartDelay(250)  // slight stagger looks smooth
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
        binding.tFeelingBelow.apply {
            translationY = 700f  // start below screen
            alpha = 0f
            animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(1900)
                .setStartDelay(250)  // slight stagger looks smooth
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
        binding.ivIllustration.apply {
            translationY = 700f  // start below screen
            alpha = 0f
            animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(2700)
                .setStartDelay(250)  // slight stagger looks smooth
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
        binding.swipeRefreshLayout.apply {
            translationY = 700f  // start below screen
            alpha = 0f
            animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(2100)
                .setStartDelay(250)  // slight stagger looks smooth
                .setInterpolator(DecelerateInterpolator())
                .start()
        }

        binding.constraintLayout.apply {
            translationY = 700f  // start below screen
            alpha = 0f
            animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(2100)
                .setStartDelay(250)  // slight stagger looks smooth
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun updateHydrationTitle() {

    viewModel.lastDrinkInfo.observe(viewLifecycleOwner) { lastDrinkInfo  ->
     binding.hydrationCardId.tvHydrationTitle.text =
            "Hydration due - $lastDrinkInfo ."

    }
    }
private fun updateProgress(consumed: Int, target: Int, unit: String) {
    val goalEntry = PrefsManager().getEmployeeGoals().find { it.goalId == 13 }

    viewModel.totalQuantity.observe(viewLifecycleOwner) { totalValue  ->
        val goal = goalEntry?.targetValue    // safe
        val remaining = goal?.minus(totalValue)

        if(totalValue< remaining!!){
            binding.hydrationCardId.tvHydrationProgress.text =
                "${totalValue} $unit consumed — $remaining $unit to go"
        }
        else{

            binding.hydrationCardId.tvHydrationProgress.text =
                "${totalValue} $unit consumed — targed $goal $unit "
        }
    }
     binding.hydrationCardId.tvHydrationProgress.setTextColor(Color.parseColor("#808C9A"))

}


    private fun initializeSwipeRefresh() {


        // Configure SwipeRefreshLayout colors and size
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.primaryBlue)
        binding.swipeRefreshLayout.setProgressBackgroundColorSchemeResource(android.R.color.white)
        binding.swipeRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT)

        // Set refresh listener
        binding.swipeRefreshLayout.setOnRefreshListener {
            performRefresh()
        }

        // Optional: Configure distance to trigger refresh
        binding.swipeRefreshLayout.setDistanceToTriggerSync(200)
    }

    private fun performRefresh() {
        // Show custom loader
        showCustomLoader(true)

        // Simulate network call with delay
        Handler(Looper.getMainLooper()).postDelayed({
            // Your actual refresh logic here
            refreshDashboardData()

            // Hide custom loader and refresh indicator
            showCustomLoader(false)
            binding.swipeRefreshLayout.isRefreshing = false

        }, 2000) // 2 second delay - replace with actual API call
    }

    private fun showCustomLoader(show: Boolean) {
        binding.customLoaderContainer.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun refreshDashboardData() {
       viewModel.getVitals()
    }
//    fun wellnessDataBind() {
//
//        // -------------------------------------------------------
//        // Default Icons, Colors and Initial Values
//        // -------------------------------------------------------
//
//
//        binding.stepsProgressId.dailyChecklistID.visibility = View.GONE
//        binding.sleepProgressId.dailyChecklistID.visibility = View.GONE
//        binding.waterProgressId.dailyChecklistID.visibility = View.GONE
//        binding.glucoseProgressId.dailyChecklistID.visibility = View.GONE
//        binding.bpProgressId.dailyChecklistID.visibility = View.GONE
//        binding.medicineProgressId.dailyChecklistID.visibility = View.GONE
//        binding.stepsProgressId.ivStepsIcon.setImageResource(R.drawable.step_progress)
//        binding.sleepProgressId.ivStepsIcon.setImageResource(R.drawable.sleep_progress)
//        binding.waterProgressId.ivStepsIcon.setImageResource(R.drawable.water_progress)
//        binding.glucoseProgressId.ivStepsIcon.setImageResource(R.drawable.glucose_progress)
//        binding.bpProgressId.ivStepsIcon.setImageResource(R.drawable.bp_progress)
//        binding.medicineProgressId.ivStepsIcon.setImageResource(R.drawable.medicine_progress)
//
//        binding.stepsProgressId.progressSteps.progressTintList = ColorStateList.valueOf(Color.parseColor("#1281FD"))
//        binding.sleepProgressId.progressSteps.progressTintList = ColorStateList.valueOf(Color.parseColor("#00C67A"))
//        binding.waterProgressId.progressSteps.progressTintList = ColorStateList.valueOf(Color.parseColor("#FEA33C"))
//        binding.glucoseProgressId.progressSteps.progressTintList = ColorStateList.valueOf(Color.parseColor("#00C67A"))
//        binding.bpProgressId.progressSteps.progressTintList = ColorStateList.valueOf(Color.parseColor("#1281FD"))
//        binding.medicineProgressId.progressSteps.progressTintList = ColorStateList.valueOf(Color.parseColor("#FF3737"))
//
//        binding.stepsProgressId.progressSteps.progress = 0
//        binding.sleepProgressId.progressSteps.progress = 0
//        binding.waterProgressId.progressSteps.progress = 0
//        binding.glucoseProgressId.progressSteps.progress = 0
//        binding.bpProgressId.progressSteps.progress = 0
//        binding.medicineProgressId.progressSteps.progress = 0
//
//        binding.stepsProgressId.tvStepsValue.text = "--"
//        binding.sleepProgressId.tvStepsValue.text = "__h __m"
//        binding.waterProgressId.tvStepsValue.text = "__ml"
//        binding.glucoseProgressId.tvStepsValue.text = "-/-"
//        binding.bpProgressId.tvStepsValue.text = "-/-"
//        binding.medicineProgressId.tvStepsValue.text = "-/-"
//
//        binding.stepsProgressId.tvStepsLabel.text = "Steps 0.0%"
//        binding.sleepProgressId.tvStepsLabel.text = "Sleep"
//        binding.waterProgressId.tvStepsLabel.text = "Water 0.0%"
//        binding.glucoseProgressId.tvStepsLabel.text = "Glucose 0.0%"
//        binding.bpProgressId.tvStepsLabel.text = "Blood Pressure 0.0%"
//        binding.medicineProgressId.tvStepsLabel.text = "Medicine 0.0%"
//
//        binding.stepsProgressId.tvStepsLabel.setTextColor(Color.WHITE)
//        binding.sleepProgressId.tvStepsLabel.setTextColor(Color.WHITE)
//        binding.waterProgressId.tvStepsLabel.setTextColor(Color.WHITE)
//        binding.glucoseProgressId.tvStepsLabel.setTextColor(Color.WHITE)
//        binding.bpProgressId.tvStepsLabel.setTextColor(Color.WHITE)
//        binding.medicineProgressId.tvStepsLabel.setTextColor(Color.WHITE)
//
//        viewModel.sleepValueList.observe(viewLifecycleOwner) { sleepValue  ->
//
//
//            val totalSleep = sleepValue.QuickMetricsTiled
//                ?.firstOrNull { it.Title.equals("TOTAL SLEEP", ignoreCase = true) }
//
//            binding.sleepProgressId.tvStepsValue.text = HtmlCompat.fromHtml(totalSleep?.Value.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
//        }
//        // -------------------------------------------------------
//        // VITAL LIST OBSERVER (BP + STEPS + SLEEP)
//        // -------------------------------------------------------
//        viewModel.vitalList.observe(viewLifecycleOwner) { vitalList ->
//
//            // ---------------- BP ----------------
//            val bpSys = vitalList.find { it.vitalName.equals("BP_Sys", ignoreCase = true) }
//            val bpDia = vitalList.find { it.vitalName.equals("BP_Dias", ignoreCase = true) }
//
//            val sysValue = bpSys?.vitalValue?.toString()?.toDoubleOrNull() ?: 0.0
//            val diaValue = bpDia?.vitalValue?.toString()?.toDoubleOrNull() ?: 0.0
//            val bpPercent = if (sysValue == 0.0 && diaValue == 0.0) 0 else 100
//
//            if (bpPercent == 0) {
//                binding.bpProgressId.dailyChecklistID.visibility = View.GONE
//                binding.bpProgressId.tvStepsValue.text = "-/-"
//                binding.bpProgressId.tvStepsLabel.text = "Blood Pressure 0%"
//            } else {
//                binding.bpProgressId.dailyChecklistID.visibility = View.VISIBLE
//                binding.bpProgressId.progressSteps.progress = bpPercent
//                binding.bpProgressId.tvStepsValue.text = "1/1"
//                binding.bpProgressId.tvStepsLabel.text = "Blood Pressure ${bpPercent}%"
//            }
//
//
//            // ---------------- STEPS ----------------
//            val steps = vitalList.firstOrNull { it.vitalName.equals("TotalSteps", ignoreCase = true) }
//            val goalSteps = 11000.0
//            val currentSteps = steps?.vitalValue?.toString()?.toDoubleOrNull() ?: 0.0
//            val stepsPercent = ((currentSteps / goalSteps) * 100).toInt()
//
//            if (stepsPercent == 0) {
//                binding.stepsProgressId.dailyChecklistID.visibility = View.GONE
//                binding.stepsProgressId.tvStepsValue.text = "0"
//                "Steps 0%".also { binding.stepsProgressId.tvStepsLabel.text = it }
//            } else {
//                binding.stepsProgressId.dailyChecklistID.visibility = View.VISIBLE
//                binding.stepsProgressId.progressSteps.progress = stepsPercent
//                binding.stepsProgressId.tvStepsValue.text = currentSteps.toInt().toString()
//                binding.stepsProgressId.tvStepsLabel.text = "Steps $stepsPercent%"
//            }
//
//
//            // ---------------- SLEEP ----------------
//            val totalSleep = vitalList.firstOrNull { it.vitalName.equals("SleepScore", ignoreCase = true) }
//            val sleepScore = totalSleep?.vmValueText?.toString()?.toIntOrNull() ?: 0
//
//            if (totalSleep?.vitalValue?.toInt()   == 0) {
//                binding.sleepProgressId.dailyChecklistID.visibility = View.GONE
//            } else {
//                binding.sleepProgressId.dailyChecklistID.visibility = View.VISIBLE
//                binding.sleepProgressId.progressSteps.progress = totalSleep?.vitalValue?.toInt() ?: 0
//            }
//
//            binding.sleepProgressId.tvStepsLabel.text = "Sleep"
//            // ---------------- Glucose ----------------
//            val glucose = vitalList.firstOrNull { it.vitalName.equals("Glucose", ignoreCase = true) }
//             if (glucose?.vitalValue?.toInt()   == 0) {
//                binding.glucoseProgressId.dailyChecklistID.visibility = View.GONE
//            } else {
//                binding.glucoseProgressId.dailyChecklistID.visibility = View.VISIBLE
//                binding.glucoseProgressId.progressSteps.progress = glucose?.vitalValue?.toInt() ?: 0
//                 binding.glucoseProgressId.tvStepsValue.text = "1/1"
//                 binding.glucoseProgressId.tvStepsLabel.text = "Glucose"
//            }
//        }
//
//
//
//        // -------------------------------------------------------
//        // WATER OBSERVER
//        // -------------------------------------------------------
//        viewModel.fluidList.observe(viewLifecycleOwner) { list ->
//
//            val waterQty = list.firstOrNull { it.id == 97694 }?.amount ?: 0f
//            val waterGoal = PrefsManager().getEmployeeGoals().find { it.vmId == 245 }
//
//            val goalMl = (waterGoal?.targetValue ?: 0) * 1000f
//            val progress = if (goalMl > 0) (waterQty.toFloat() / goalMl) * 100f else 0f
//
//            if (progress.toDouble() == 0.0) {
//                binding.waterProgressId.dailyChecklistID.visibility = View.GONE
//            } else {
//                binding.waterProgressId.dailyChecklistID.visibility = View.VISIBLE
//                binding.waterProgressId.progressSteps.progress = progress.toInt()
//                binding.waterProgressId.tvStepsValue.text = "${waterQty} ml"
//                binding.waterProgressId.tvStepsLabel.text = "Water ${progress }%"
//            }
//        }
//
//
//        // -------------------------------------------------------
//        // MEDICINE OBSERVER
//        // -------------------------------------------------------
//        pillsViewModel.pillList.observe(viewLifecycleOwner) { list ->
//
//            val totalMeds = list?.size ?: 0
//            val takenMeds = list?.count { it.isTaken == 1 } ?: 0
//            val percent = if (totalMeds > 0) ((takenMeds.toFloat() / totalMeds) * 100).toInt() else 0
//
//            if (percent == 0) {
//                binding.medicineProgressId.dailyChecklistID.visibility = View.GONE
//            } else {
//                binding.medicineProgressId.dailyChecklistID.visibility = View.VISIBLE
//                binding.medicineProgressId.progressSteps.progress = percent
//                binding.medicineProgressId.tvStepsValue.text = "$takenMeds / $totalMeds taken"
//                binding.medicineProgressId.tvStepsLabel.text = "Medicine ${percent}%"
//            }
//        }
//    }


    private fun showPopup() {
        // Apply blur effect to contentScroll only (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val blurEffect = RenderEffect.createBlurEffect(
                25f, 25f, Shader.TileMode.CLAMP
            )
            binding.contentScroll.setRenderEffect(blurEffect)
        }

        binding.blurOverlay.isVisible = true
        binding.popupContainer.isVisible = true

        // Animate blur overlay
        binding.blurOverlay.alpha = 0f
        binding.blurOverlay.animate()
            .alpha(1f)
            .setDuration(300)
            .start()

        // Animate popup with scale and fade from center
        binding.popupContainer.alpha = 0f
        binding.popupContainer.scaleX = 0.8f
        binding.popupContainer.scaleY = 0.8f
        binding.popupContainer.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun hidePopup() {
        // Remove blur effect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            binding.contentScroll.setRenderEffect(null)
        }

        // Animate popup out
        binding.popupContainer.animate()
            .alpha(0f)
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(250)
            .start()

        // Animate blur overlay
        binding.blurOverlay.animate()
            .alpha(0f)
            .setDuration(250)
            .withEndAction {
                binding.blurOverlay.isVisible = false
            }
            .start()
    }



    private fun setupRecyclerAndIndicators() {

        // Create adapter once
        dailyTipAdapter = DailyTipAdapter(emptyList()) { tip ->
            handleTipAction(tip)
        }

        binding.recyclerSlider.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = dailyTipAdapter
            PagerSnapHelper().attachToRecyclerView(this)
        }

        // Create indicators once
        indicatorAdapter = IndicatorAdapter(0)
        binding.layoutIndicators.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = indicatorAdapter
        }

        // Observe LiveData and update WITHOUT recreating adapters
        viewModel.priorityAction.observe(viewLifecycleOwner) { tipsOrNull ->
            val tips = tipsOrNull ?: emptyList()

            // update list
            dailyTipAdapter.updateData(tips)

            // update indicator count
            indicatorAdapter.setItemCount(tips.size)
        }

        // Scroll listener stays valid
        binding.recyclerSlider.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                super.onScrollStateChanged(rv, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val lm = rv.layoutManager as LinearLayoutManager
                    val pos = lm.findFirstVisibleItemPosition()
                    indicatorAdapter.updateSelectedPosition(pos)
                }
            }
        })
    }
    private fun handleTipAction(tip: PriorityAction) {
        when (tip.actionId) {

            "Start Session" -> {
                // breathing exercise
                Toast.makeText(requireContext(), "Starting breathing exercise", Toast.LENGTH_SHORT).show()
            }

            "Add Intake" -> {

                findNavController().navigate(R.id.action_dashboard_to_waterIntakeFragment)
            }

            else -> {
                Toast.makeText(requireContext(), "Action not implemented", Toast.LENGTH_SHORT).show()
            }
        }
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

                        binding.medicineTitleID.visibility = View.GONE
                        binding.recyclerView.visibility = View.GONE
                        binding.healthSnaps.visibility = View.GONE

                    }

                    1 -> {
                        binding.homeId.visibility = View.GONE
                        binding.challengedId.visibility = View.GONE
                        binding.activeChalleTextId.visibility = View.GONE

                        binding.medicineTitleID.visibility = View.GONE
                        binding.recyclerView.visibility = View.GONE
                        binding.healthSnaps.visibility = View.VISIBLE

                    }

                    2 -> {
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.medicineTitleID.visibility = View.VISIBLE
                        binding.recyclerView.layoutManager =
                            LinearLayoutManager(requireContext())

                        // Initialize adapter once with an empty list
                        val adapter = TabMedicineAdapter(mutableListOf()) { selectedMedicine ->
                            //findNavController().navigate(R.id.action_dashboard_to_medicationFragment)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                pillsViewModel.insertPatientMedication(selectedMedicine)
                            };
                        }
                        binding.recyclerView.adapter = adapter

                        // Observe pill list updates
                        pillsViewModel.pillList.observe(viewLifecycleOwner) { list ->
                            Log.d("TAG", "selectItem: "+list.size.toString())
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

                        binding.medicineTitleID.visibility = View.GONE
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

