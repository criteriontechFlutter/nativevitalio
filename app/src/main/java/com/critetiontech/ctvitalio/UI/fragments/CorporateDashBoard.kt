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
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.Typeface
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.NoiseSuppressor
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.transition.Visibility
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.BaseActivity
import com.critetiontech.ctvitalio.UI.UltraHumanActivity
import com.critetiontech.ctvitalio.adapter.DailyTipAdapter
import com.critetiontech.ctvitalio.adapter.DashboardAdapter
import com.critetiontech.ctvitalio.adapter.IndicatorAdapter
import com.critetiontech.ctvitalio.adapter.MedicationReminderAdapter
import com.critetiontech.ctvitalio.adapter.NewChallengedAdapter
import com.critetiontech.ctvitalio.adapter.PriorityAction
import com.critetiontech.ctvitalio.adapter.ProgressCard
import com.critetiontech.ctvitalio.adapter.TabMedicineAdapter
import com.critetiontech.ctvitalio.databinding.DailyChecklistWedgetBinding
import com.critetiontech.ctvitalio.databinding.FragmentCorporateDashBoardBinding
import com.critetiontech.ctvitalio.databinding.SleepLayoutBinding
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.LoaderUtils.hideLoading
import com.critetiontech.ctvitalio.utils.LoaderUtils.showLoading
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.utils.ToastUtils
import com.critetiontech.ctvitalio.utils.applyStatusStyle
import com.critetiontech.ctvitalio.utils.bindVitalView
import com.critetiontech.ctvitalio.utils.bindVitalViewVitalValue
import com.critetiontech.ctvitalio.utils.showRetrySnackbar
import com.critetiontech.ctvitalio.viewmodel.ChallengesViewModel
import com.critetiontech.ctvitalio.viewmodel.DashboardViewModel
import com.critetiontech.ctvitalio.viewmodel.PillsReminderViewModal
import com.critetiontech.ctvitalio.viewmodel.WebSocketState
import com.google.android.material.snackbar.Snackbar
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString.Companion.toByteString
import kotlin.math.max
import kotlin.math.min


class CorporateDashBoard : Fragment() {
    private lateinit var binding: FragmentCorporateDashBoardBinding
    private lateinit var viewModel: DashboardViewModel
    private var voiceDialog: Dialog? = null
    private lateinit var voiceButton: ImageView
   private lateinit var closeButton: ImageView
    private lateinit var testv: TextView
    private lateinit var challengesViewModel: ChallengesViewModel
    private lateinit var pillsViewModel: PillsReminderViewModal
    private lateinit var adapter: DashboardAdapter
    private lateinit var medicationReminderAdapter: MedicationReminderAdapter
    private lateinit var dailyTipAdapter: DailyTipAdapter
    private lateinit var indicatorAdapter: IndicatorAdapter
    private var snackbar: Snackbar? = null
    private val handler = Handler(Looper.getMainLooper())
    private var sliderRunnable: Runnable? = null
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var silenceStartTime = 0L
    private var backgroundNoise = 0.0
    private var noiseMeasured = false
    private var webSocket: WebSocket? = null
    private val RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    private val PERMISSION_REQUEST_CODE = 101
    var fragmentOpened = false
    private val tabLabels = listOf("Home", "Snaps", "Reminders", "Challenges")
    private val tabIcons = listOf(R.drawable.home, R.drawable.vitals_icon_home, R.drawable.pill,R.drawable.challenges_icon)
    private lateinit var navItems: List<View>
    private val micStatusHandler = Handler(Looper.getMainLooper())
    private var micStatusRunnable: Runnable? = null

    private val moods = listOf(
        MoodData(5,"Spectacular", "#FFA4BA", R.drawable.spectulor_mood,  "#611829"),
        MoodData(6,"Upset", "#88A7FF",  R.drawable.upset_mood,  "#2A4089"),
        MoodData(1, "Stressed", "#FF9459",  R.drawable.stressed_mood, "#782E04"),
        MoodData(2,"Happy", "#9ABDFF",  R.drawable.happy,"#505D87"),
        MoodData(4,"Good", "#F9C825",  R.drawable.good_mood, "#664F00"),
        MoodData(3,"Sad",   "#7DE7EE",  R.drawable.sad_mood,  "#3A7478")

    )
    private var isFabOpen = false
    private var audioRecords: AudioRecord? = null
    private var isRecordings = false

    private var lastVoiceTime =  0L
    private   val SILENCE_TIMEOUT = 5000L // 5 seconds

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
        binding.notificationIcon.setOnClickListener {   }
        binding.ringIcon.setOnClickListener {

            startActivity(Intent(requireActivity(), UltraHumanActivity::class.java))
//



        }

        binding.headerContainer.setOnClickListener {

           // findNavController().navigate(R.id.action_dashboard_to_new_corporate_dashboard)

        }
        binding.progressCircler.animateProgress(10f)
        animatePageLoad()
        (requireActivity() as? BaseActivity)?.setSystemBarsColor(
            statusBarColor = R.color.primaryBlue,
            navBarColor = R.color.white,
            lightIcons = true
        )
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) showLoading() else hideLoading()
        }

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
                        verticalBias = -0.34f
                        binding.ivIllustration.layoutParams = this
                    }
                    binding.ivIllustration.layoutParams.apply {
                        width = dpToPx(350, requireContext())
                        height = dpToPx(190, requireContext())

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
            .load(RetrofitInstance.StaggingbaseUrl.toString()+":5082/"+PrefsManager().getPatient()?.imageURL.toString())
            .placeholder(R.drawable.baseline_person_24)
            .circleCrop()
            .into(binding.avatar)
        binding.avatar.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_drawer4)
        }
        challengesViewModel = ViewModelProvider(this)[ChallengesViewModel::class.java]
      //  challengesViewModel.getNewChallenge()
        viewModel.fluidList.observe(viewLifecycleOwner) { list ->
            val waterQty = list
                .firstOrNull { it.id.toString() == "97694" }
                ?.amount?.toFloat() ?: 0f  // convert safely to Float

        }




        viewModel.activeChallenges.observe(viewLifecycleOwner) { list ->
            binding.newChallengedRecyclerView.adapter = NewChallengedAdapter(
                list.toMutableList(),
                onJoinClick  =  { challenge ->
                    challengesViewModel.joinChallenge( challenge.challengeId.toString())
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
                    challengesViewModel.joinChallenge( challenge.challengeId.toString())
                },
                onDetailsClick =  { challenge ->
                    val bundle = Bundle().apply {
                        putSerializable("challenges", challenge)
                    }
                    findNavController().navigate(R.id.action_dashboard_to_challengeDetailsFragment, bundle)

                }
            )



            binding.activechalgesId.text="Active Challenges ("+list.size.toString()+")"
            binding.activeChalleTextId.text="Active Challenges ("+list.size.toString()+")"

            val visible = list.isNotEmpty()

            binding.activechalgesId.visibility = if (visible) View.VISIBLE else View.GONE
            binding.activeChalleTextId.visibility = if (visible) View.VISIBLE else View.GONE
            setupActiveChallenges(list.size)
        }



        viewModel.getFoodIntake()
        val typeface = ResourcesCompat.getFont(requireActivity(), R.font.source_serif_pro_regular)
        binding.tFeeling.setTypeface(typeface, Typeface.BOLD)
        binding.tFeelingBelow.setTypeface(typeface )

        var greetings= if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ToastUtils.getSimpleGreeting()
        } else {
            TODO("VERSION.SDK_INT < O")
        };

        binding.greetingHi.text = greetings
        binding.greetingName.text = PrefsManager().getPatient()?.patientName ?: ""


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



        binding.bpPopupId.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_BPHistory)
        }
        
        binding.glucosePopupId.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_glucoseHistory)
        }

        binding.voiceAssistantId.setOnClickListener {
             binding.fabIcon.animate().rotation(0f).setDuration(300).start()
            binding.fabIcon.setImageResource(R.drawable.raddimg)
            isFabOpen = false
            hidePopup()
            showVoiceOverlay()
            connectWebSocket()
            checkAndStartAudio()        }

           binding.popupActivityId.setOnClickListener {
               findNavController().navigate(R.id.action_dashboard_to_addActivityFragment)
        }


        binding.popupAddMedicineId.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_addMedicineReminderFragment)
        }

        observeVitalList()
        observeSleepValues()
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



viewModel.vitalList.observe(viewLifecycleOwner) { vitalList ->
    val vitalStepsIndex = vitalList.find { it.vitalName.equals("TotalSteps", ignoreCase = true) }
    binding.StepsId.title.text = "Steps"
    binding.StepsId.value.text = vitalStepsIndex?.vitalValue.toString()
    binding.StepsId.statusCardId.visibility = View.VISIBLE
    binding.StepsId.status.applyStatusStyle(
        rawColor = vitalStepsIndex?.vitalColor,
        getPastelColor = ::getPastelColor
    )
    binding.StepsId.status.text = vitalStepsIndex?.severityLevel.toString()


    val totalSleepIndex = vitalList.find { it.vitalName.equals("TotalSleep", ignoreCase = true) }
    binding.totalSleepId.title.text = "Total Sleep"
    binding.totalSleepId.value.text = totalSleepIndex?.vmValueText.toString()
    binding.totalSleepId.statusCardId.visibility = View.VISIBLE
    binding.totalSleepId.status.applyStatusStyle(
        rawColor = totalSleepIndex?.vitalColor,
        getPastelColor = ::getPastelColor
    )
    binding.totalSleepId.status.text = totalSleepIndex?.severityLevel.toString()


    val sleepEfficiency = vitalList.find { it.vitalName.equals("Sleep efficiency", ignoreCase = true) }
    binding.sleepEfficiencyId.title.text = "Sleep Efficiency"
    binding.sleepEfficiencyId.value.text = sleepEfficiency?.vmValueText.toString()
    binding.sleepEfficiencyId.statusCardId.visibility = View.VISIBLE
    binding.sleepEfficiencyId.status.applyStatusStyle(
        rawColor = sleepEfficiency?.vitalColor,
        getPastelColor = ::getPastelColor
    )
    binding.sleepEfficiencyId.status.text = sleepEfficiency?.severityLevel.toString()



    val timeinBedIndex = vitalList.find {
        it.vitalName.equals("TimeInBed", ignoreCase = true)
    }
    bindVitalView(
        vitalName = "Time in Bed",
        vital = timeinBedIndex,
        titleView = binding.timeInBedId.title,
        valueView = binding.timeInBedId.value,
        statusView = binding.timeInBedId.status,
        statusCardView = binding.timeInBedId.statusCardId,
        getPastelColor = ::getPastelColor
    )

    val sleepCycleIndex = vitalList.find {
        it.vitalName.equals("SleepCycles", ignoreCase = true)
    }
    bindVitalView(
        vitalName = "Full Sleep Cycle",
        vital = sleepCycleIndex,
        titleView = binding.fulSleepCycleId.title,
        valueView = binding.fulSleepCycleId.value,
        statusView = binding.fulSleepCycleId.status,
        statusCardView = binding.fulSleepCycleId.statusCardId,
        getPastelColor = ::getPastelColor
    )

    val RemSleepIndex = vitalList.find {
        it.vitalName.equals("REM Sleep", ignoreCase = true)
    }
    bindVitalView(
        vitalName = "REM Sleep",
        vital = RemSleepIndex,
        titleView = binding.remSleepId.title,
        valueView = binding.remSleepId.value,
        statusView = binding.remSleepId.status,
        statusCardView = binding.remSleepId.statusCardId,
        getPastelColor = ::getPastelColor
    )

    val deepsleepIndex = vitalList.find {
        it.vitalName.equals("Deep Sleep", ignoreCase = true)
    }
    bindVitalView(
        vitalName = "Deep Sleep",
        vital = deepsleepIndex,
        titleView = binding.deepSleepId.title,
        valueView = binding.deepSleepId.value,
        statusView = binding.deepSleepId.status,
        statusCardView = binding.deepSleepId.statusCardId,
        getPastelColor = ::getPastelColor
    )


    val awakeIndex = vitalList.find {
        it.vitalName.equals("Awake", ignoreCase = true)
    }
    bindVitalView(
        vitalName = "Awake",
        vital = awakeIndex,
        titleView = binding.awakeSleepId.title,
        valueView = binding.awakeSleepId.value,
        statusView = binding.awakeSleepId.status,
        statusCardView = binding.awakeSleepId.statusCardId,
        getPastelColor = ::getPastelColor
    )

    val lightSleepIndex = vitalList.find {
        it.vitalName.equals("Light Sleep", ignoreCase = true)
    }
    bindVitalView(
        vitalName = "Light Sleep",
        vital = lightSleepIndex,
        titleView = binding.lightSleepId.title,
        valueView = binding.lightSleepId.value,
        statusView = binding.lightSleepId.status,
        statusCardView = binding.lightSleepId.statusCardId,
        getPastelColor = ::getPastelColor
    )


    val restorativeSleepIndex = vitalList.find {
        it.vitalName.equals("RestorativeSleep", ignoreCase = true)
    }
    bindVitalView(
        vitalName = "Restorative Sleep",
        vital = restorativeSleepIndex,
        titleView = binding.restorativeSleepId.title,
        valueView = binding.restorativeSleepId.value,
        statusView = binding.restorativeSleepId.status,
        statusCardView = binding.restorativeSleepId.statusCardId,
        getPastelColor = ::getPastelColor
    )
    val activeHoursIndex = vitalList.find {
        it.vitalName.equals("ActiveHours", ignoreCase = true)
    }
    bindVitalViewVitalValue(
        vitalName = "Active Hours",
        vital = activeHoursIndex,
        titleView = binding.activieHoursId.title,
        valueView = binding.activieHoursId.value,
        statusView = binding.activieHoursId.status,
        statusCardView = binding.activieHoursId.statusCardId,
        getPastelColor = ::getPastelColor
    )

//    val timeinBedIndex = vitalList.find {
//        it.vitalName.equals("TimeInBed", ignoreCase = true)
//    }
//    bindVitalView(
//        vitalName = "Movements",
//        vital = timeinBedIndex,
//        titleView = binding.timeInBedId.title,
//        valueView = binding.timeInBedId.value,
//        statusView = binding.timeInBedId.status,
//        statusCardView = binding.timeInBedId.statusCardId,
//        getPastelColor = ::getPastelColor
//    )


    val morningalertnessId = vitalList.find {
        it.vitalName.equals("Morningalertness", ignoreCase = true)
    }
    bindVitalView(
        vitalName = "Morning Alertness",
        vital = morningalertnessId,
        titleView = binding.morningAlertnessId.title,
        valueView = binding.morningAlertnessId.value,
        statusView = binding.morningAlertnessId.status,
        statusCardView = binding.morningAlertnessId.statusCardId,
        getPastelColor = ::getPastelColor
    )


    val tossandTurnIndex = vitalList.find {
        it.vitalName.equals("TossTurn", ignoreCase = true)
    }
    bindVitalView(
        vitalName = "Tosses and Turn",
        vital = tossandTurnIndex,
        titleView = binding.tossesAndTurnsId.title,
        valueView = binding.tossesAndTurnsId.value,
        statusView = binding.tossesAndTurnsId.status,
        statusCardView = binding.tossesAndTurnsId.statusCardId,
        getPastelColor = ::getPastelColor
    )

//    val timeinBedIndex = vitalList.find {
//        it.vitalName.equals("TimeInBed", ignoreCase = true)
//    }
//    bindVitalView(
//        vitalName = "Avg Body Temp",
//        vital = timeinBedIndex,
//        titleView = binding.averageBodyTempId.title,
//        valueView = binding.averageBodyTempId.value,
//        statusView = binding.averageBodyTempId.status,
//        statusCardView = binding.averageBodyTempId.statusCardId,
//        getPastelColor = ::getPastelColor
//    )





    val sleepScoreIndex = vitalList.find { it.vitalName.equals("SleepScore", ignoreCase = true) }
    binding.sleepScoreId.title.text = "Sleep Score"
    binding.sleepScoreId.value.text = sleepScoreIndex?.vmValueText.toString()
    binding.sleepScoreId.statusCardId.visibility = View.VISIBLE
    binding.sleepScoreId.status.applyStatusStyle(
        rawColor = sleepScoreIndex?.vitalColor,
        getPastelColor = ::getPastelColor
    )
    binding.sleepScoreId.status.text = sleepScoreIndex?.severityLevel.toString()





    val TemperatureBody = vitalList
        ?.firstOrNull { it.vitalName.equals("Temperature", ignoreCase = true) }
    binding.averageBodyTempId.title.text = "Average Body Temp."
    binding.averageBodyTempId.value.text = "%.1f".format(TemperatureBody?.vitalValue ?: 0.0)
    binding.averageBodyTempId.statusCardId.visibility = View.VISIBLE

    binding.averageBodyTempId.status.applyStatusStyle(
        rawColor = TemperatureBody?.vitalColor,
        getPastelColor = ::getPastelColor
    )
    binding.averageBodyTempId.status.text = TemperatureBody?.severityLevel.toString()

    val activeMinutes = vitalList
        ?.firstOrNull { it.vitalName.equals("ActiveMinutes", ignoreCase = true) }
    binding.ActiveminutesId.title.text = "Active Minutes"
    binding.ActiveminutesId.value.text = activeMinutes?.vitalValue.toString()
    binding.ActiveminutesId.statusCardId.visibility = View.VISIBLE

    binding.ActiveminutesId.status.applyStatusStyle(
        rawColor = activeMinutes?.vitalColor,
        getPastelColor = ::getPastelColor
    )
    binding.ActiveminutesId.status.text = activeMinutes?.severityLevel.toString()



    val Temperature = vitalList
        ?.firstOrNull { it.vitalName.equals("TemperatureDeviation", ignoreCase = true) }
    binding.tempDeviationId.title.text = "Temperature Deviation"
    binding.tempDeviationId.value.text = "${"%.1f".format(Temperature?.vitalValue ?: 0.0)}"
    binding.tempDeviationId.statusCardId.visibility = View.VISIBLE
    binding.tempDeviationId.status.applyStatusStyle(
        rawColor = Temperature?.vitalColor,
        getPastelColor = ::getPastelColor
    )
    binding.tempDeviationId.status.text = Temperature?.severityLevel.toString()


    val recoveryIndex = vitalList
        ?.firstOrNull { it.vitalName.equals("RecoveryIndex", ignoreCase = true) }
    binding.recoveryScoreId.title.text = "Recovery Score"
    binding.recoveryScoreId.value.text = recoveryIndex?.vitalValue.toString()
    binding.recoveryScoreId.statusCardId.visibility = View.VISIBLE

    binding.recoveryScoreId.status.applyStatusStyle(
        rawColor = recoveryIndex?.vitalColor,
        getPastelColor = ::getPastelColor
    )
    binding.recoveryScoreId.status.text = recoveryIndex?.severityLevel.toString()

    val HRV = vitalList
        ?.firstOrNull { it.vitalName.equals("HRV", ignoreCase = true) }
    binding.lastNightHrvId.title.text = "Last Night's HRV"
    binding.lastNightHrvId.value.text = HRV?.vitalValue.toString()
    binding.lastNightHrvId.statusCardId.visibility = View.VISIBLE
    binding.lastNightHrvId.status.applyStatusStyle(
        rawColor = HRV?.vitalColor,
        getPastelColor = ::getPastelColor
    )
    binding.lastNightHrvId.status.text = HRV?.severityLevel.toString()



    binding.SleepStageHrvId.title.text = "Sleep Stage' HRV"
    binding.SleepStageHrvId.value.text = HRV?.vitalValue.toString()
    binding.SleepStageHrvId.statusCardId.visibility = View.VISIBLE
    binding.SleepStageHrvId.status.applyStatusStyle(
        rawColor = HRV?.vitalColor,
        getPastelColor = ::getPastelColor
    )
    binding.SleepStageHrvId.status.text = HRV?.severityLevel.toString()
    val movementIndex = vitalList
        ?.firstOrNull { it.vitalName.equals("MovementIndex", ignoreCase = true) }

    binding.movementIndexId.title.text = "Movement Index"
    binding.movementIndexId.value.text = movementIndex?.vitalValue.toString()
    binding.movementIndexId.statusCardId.visibility = View.VISIBLE
    binding.movementIndexId.status.applyStatusStyle(
        rawColor = movementIndex?.vitalColor,
        getPastelColor = ::getPastelColor
    )
    binding.movementIndexId.status.text = movementIndex?.severityLevel.toString()

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

                pillsViewModel.insertPatientMedication(medicine)             }

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
        updateProgress(unit =  "ml")
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


//        observeVitalList()
//        observeSleepValues()
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
    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }
//    private fun monitorSilence() {
//        Thread {
//            val buffer = ShortArray(1024)
//
//            while (isRecording && audioRecord != null) {
//
//                val read = audioRecord!!.read(buffer, 0, buffer.size)
//
//                if (read > 0) {
//                    var sum = 0.0
//                    for (i in 0 until read) {
//                        sum += buffer[i] * buffer[i]
//                    }
//                    val rms = kotlin.math.sqrt(sum / read)
//
//                    // 🗣 User is speaking
//                    if (rms > 2000) {
//                        lastVoiceTime = System.currentTimeMillis()
//                    }
//                }
//
//                // ⏱ No speech for 5 sec → OFF
//                if (System.currentTimeMillis() - lastVoiceTime >= SILENCE_TIMEOUT) {
//                    stopMic()
//                    break
//                }
//
//                Thread.sleep(200)
//            }
//        }.start()
//    }
    private fun stopMic() {
        if (!isRecording) return

        isRecording = false

        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}

        audioRecord = null
    }
    private fun startMicStatusMonitor() {
        micStatusRunnable = object : Runnable {
            override fun run() {
                val isOn = isMicOn()

                // 🔄 Use status here
                Log.d("MIC_STATUS", if (isOn) "MIC ON" else "MIC OFF")

                // Example UI update
//
//                testv = voiceDialog?.findViewById(R.id.voice_hinteest)!!
//
//                 testv.text =
//                    if (isOn) "🎙 Mic Active" else "Mic Off"

                micStatusHandler.postDelayed(this, 500) // check every 500ms
            }
        }
        micStatusHandler.post(micStatusRunnable!!)
    }

    private fun monitorSilence() {
        Thread {
            val buffer = ShortArray(1024)

            while (isRecording && audioRecord != null) {

                val read = audioRecord!!.read(buffer, 0, buffer.size)

                if (read > 0) {
                    var sum = 0.0
                    for (i in 0 until read) {
                        sum += buffer[i] * buffer[i]
                    }
                    val rms = kotlin.math.sqrt(sum / read)

                    if (rms > 500 && rms < 15000) {
                        lastVoiceTime = System.currentTimeMillis()
                    }
                }

                // ⏱️ Stop mic after 5 sec silence
                if (System.currentTimeMillis() - lastVoiceTime >= SILENCE_TIMEOUT) {
                    stopAudioStreaming()
                    break
                }

                Thread.sleep(200)
            }
        }.start()
    }

    private fun isMicOn(): Boolean {
        return audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING
    }


    fun getPastelColor(hexColor: String): Int {
        val color = Color.parseColor(hexColor)
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)

        // Reduce saturation & increase brightness
        hsv[1] *= 0.35f   // saturation
        hsv[2] = 1.0f    // brightness

        return Color.HSVToColor(hsv)
    }
    private fun wellnessDataBind() {

        viewModel.insightWrapperList.observe(viewLifecycleOwner) { insight ->
            insight ?: return@observe

            val wellness = binding.sleepProgressIds
            val wellnessStatus = insight.wellnessStatus.toIntOrNull() ?: 0

            when (insight.wellnessScore) {
                in 90..100 -> {
                    wellness.dashboardAnimatedCard.setDefaultWaveColors(
                        backgroundColor = "#F7FDF9".toColorInt(),
                        backWaveColor =   "#F3FAF5".toColorInt(),
                        frontWaveColor =  "#EBF5EE".toColorInt()
                    )
                }
                in 80..89 -> {
                    wellness.dashboardAnimatedCard.setDefaultWaveColors(
                        backgroundColor = "#F4F8FD".toColorInt(),
                        backWaveColor = "#ECF3FA".toColorInt(),
                        frontWaveColor = "#E3EBF4".toColorInt()
                    )
                }

                in 60..79 -> {
                    wellness.dashboardAnimatedCard.setDefaultWaveColors(
                        backgroundColor = "#FDF8F0".toColorInt(),
                        backWaveColor = "#F9F1E6".toColorInt(),
                        frontWaveColor = "#F5EBDD".toColorInt()
                    )
                }

                else -> {
                    wellness.dashboardAnimatedCard.setDefaultWaveColors(
                        backgroundColor = "#FDF6F6".toColorInt(),
                        backWaveColor = "#F9EEEE".toColorInt(),
                        frontWaveColor = "#F5E7E7".toColorInt()
                    )
                }
            }

            // =======================
            // HEADER
            // =======================
            wellness.wellnessScoreNumber.text = insight.wellnessScore.toString()
            binding.progressCircler.animateProgress(insight.wellnessScore.toFloat())
            wellness.wellnessDescriptions.text = insight.wellnessMessage

            val scores = insight.scores

            // =======================
            // SLEEP
            // =======================
            val sleep = insight.insights.sleep
            val sleepColor = sleep.colorCode.toColorInt()
            wellness.sleepIndex = sleep.message
            wellness.sleepstatusId.text = sleep.quality
            wellness.sleepValue.text = scores.sleepScore.toInt().toString()
            wellness.sleepContainertextId.setTextOrHide(sleep.message)
            wellness.sleepstatusId.setTextColor(sleepColor)
            wellness.sleepstatusId.backgroundTintList =
                ColorStateList.valueOf(sleepColor.withAlpha(0.15f))

            // =======================
            // MOVEMENT
            // =======================
            val movement = insight.insights.movement
            val movementColor = movement.colorCode.toColorInt()
            wellness.movementIndex = movement.message
            wellness.movementstatusId.text = movement.progress
            wellness.movementValue.text = scores.movementScore.toInt().toString()
            wellness.movementContainertextId.setTextOrHide(movement.message)
            wellness.movementstatusId.setTextColor(movementColor)
            wellness.movementstatusId.backgroundTintList =
                ColorStateList.valueOf(movementColor.withAlpha(0.15f))

            // =======================
            // STRESS
            // =======================
            val stress = insight.insights.stress
            val stressColor = stress.colorCode.toColorInt()
            wellness.stressIndex = stress.message
            wellness.stressstatusId.text = stress.level
            wellness.stressValue.text = scores.stressScore.toInt().toString()
            wellness.stressContainertextId.setTextOrHide(stress.message)
            wellness.stressstatusId.setTextColor(stressColor)
            wellness.stressstatusId.backgroundTintList =
                ColorStateList.valueOf(stressColor.withAlpha(0.15f))

            // =======================
            // RECOVERY
            // =======================
            val recovery = insight.insights.recovery
            val recoveryColor = recovery.colorCode.toColorInt()
            wellness.recoveryIndex = recovery.message
            wellness.recoverystatusId.text = recovery.status
            wellness.recoveryValue.text = scores.recoveryScore.toInt().toString()
            wellness.recoveryContainertextId.setTextOrHide(recovery.message)
            wellness.recoverystatusId.setTextColor(recoveryColor)
            wellness.recoverystatusId.backgroundTintList =
                ColorStateList.valueOf(recoveryColor.withAlpha(0.15f))
        }
    }

    private fun showVoiceOverlay() {startMicStatusMonitor()
        if (voiceDialog == null) {
            voiceDialog = Dialog(requireContext(), android.R.style.Theme_DeviceDefault_NoActionBar)
            voiceDialog?.setContentView(R.layout.dialog_voice_input)
            voiceDialog?.setCancelable(true)
            voiceDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
         }
                voiceButton = voiceDialog?.findViewById(R.id.voice_button)!!
        closeButton = voiceDialog?.findViewById(R.id.ic_close)!!

        // ✅ SET CLICK LISTENER HERE
        voiceButton?.setOnClickListener {
            stopAudioStreaming()
            disconnectWebSocket()
            voiceDialog?.dismiss()
        }
        closeButton?.setOnClickListener {
            voiceDialog?.dismiss()
        }
        voiceDialog?.show()
    }

    private fun hideVoiceOverlay() {
        voiceDialog?.dismiss()
    }
//    private fun connectWebSocket() {
//        viewModel.setWebSocketState( WebSocketState.CONNECTING)
//        voiceDialog?.findViewById<TextView>(R.id.voice_transcript)?.text = ""
//
//        val request = Request.Builder().url(RetrofitInstance.holdSpeakWsUrl+ PrefsManager().getPatient()?.pid.toString()).build()
//        val client = OkHttpClient()
//
//        webSocket = client.newWebSocket(request, object : WebSocketListener() {
//            override fun onOpen(webSocket: WebSocket, response: Response) {
//                viewModel.setWebSocketState(WebSocketState.CONNECTED)
//            }
//
//            override fun onMessage(webSocket: WebSocket, text: String) {
//                requireActivity().runOnUiThread {
//                    voiceDialog?.findViewById<TextView>(R.id.voice_transcript)?.append(" $text")
//                }
//            }
//
//            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
//                viewModel.setWebSocketState(WebSocketState.DISCONNECTED)
//                requireActivity().runOnUiThread {
//                    val transcriptText = voiceDialog?.findViewById<TextView>(R.id.voice_transcript)?.text.toString()
//                    if (transcriptText.isNotBlank()) {
//                        val navController = findNavController()
//                        navigateFromDashboard(navController, transcriptText)
//                        viewModel.postAnalyzedVoiceData(requireContext(), transcriptText)
//                    } else {
//                        Toast.makeText(requireContext(), "No speech input found", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//
//            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
//                viewModel.setWebSocketState(WebSocketState.ERROR)
//            }
//        })
//    }
    private fun connectWebSocket() {

        viewModel.setWebSocketState(WebSocketState.CONNECTING)
        Log.d("WebSocketState", "CONNECTING"+" ws://182.156.200.177:8002/listen?token=132" )

        voiceDialog?.findViewById<TextView>(R.id.voice_transcript)?.text = ""

        val request = Request.Builder()
            .url(RetrofitInstance.holdSpeakWsUrl +"132")
            .build()

        val client = OkHttpClient()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocketState", "CONNECTED")
                viewModel.setWebSocketState(WebSocketState.CONNECTED)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocketState", "MESSAGE: $text")

                requireActivity().runOnUiThread {
                    voiceDialog?.findViewById<TextView>(R.id.voice_transcript)
                        ?.append(" $text")
                }
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocketState", "DISCONNECTED: $code | $reason")
                viewModel.setWebSocketState(WebSocketState.DISCONNECTED)

                requireActivity().runOnUiThread {
                    val transcriptText =
                        voiceDialog?.findViewById<TextView>(R.id.voice_transcript)?.text.toString()

                    if (transcriptText.isNotBlank()) {
                        navigateFromDashboard(findNavController(), transcriptText)
                        viewModel.postAnalyzedVoiceData(requireContext(), transcriptText)
                    } else {
                        Toast.makeText(requireContext(), "No speech input found", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocketState", "ERROR", t)
                viewModel.setWebSocketState(WebSocketState.ERROR)
            }
        })
    }
    private fun checkAndStartAudio() {
        if (ContextCompat.checkSelfPermission(requireContext(), RECORD_AUDIO_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
            startAudioStreaming()
        } else {
            requestPermissions(arrayOf(RECORD_AUDIO_PERMISSION), PERMISSION_REQUEST_CODE)
        }
    }



    fun startAudioStreaming() {

        if (isRecording) return
        silenceStartTime = 0L
        noiseMeasured = false
        backgroundNoise = 0.0

        val bufferSize = AudioRecord.getMinBufferSize(
            16000,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            16000,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        if (NoiseSuppressor.isAvailable()) {
            NoiseSuppressor.create(audioRecord!!.audioSessionId)
        }

        audioRecord?.startRecording()
        isRecording = true

        val buffer = ShortArray(bufferSize / 2)

        val rmsHistory = mutableListOf<Double>()
        val smoothingWindow = 5

        val noiseMeasureEnd = System.currentTimeMillis() + 2000
        var noiseSum = 0.0
        var noiseCount = 0

        Thread {
            while (isRecording && audioRecord != null) {

                val read = audioRecord!!.read(buffer, 0, buffer.size)
                if (read <= 0) continue

// STREAM AUDIO
                val byteBuffer = ByteArray(read * 2)
                for (i in 0 until read) {
                    byteBuffer[i * 2] = (buffer[i].toInt() and 0x00FF).toByte()
                    byteBuffer[i * 2 + 1] = ((buffer[i].toInt() shr 8) and 0x00FF).toByte()
                }

                webSocket?.send(byteBuffer.toByteString())
                // ---- Calculate RMS correctly (IMPORTANT) ----
                var sum = 0.0
                for (i in 0 until read) {
                    sum += buffer[i] * buffer[i]
                }
                val rms = kotlin.math.sqrt(sum / read)

                // ---- Measure background noise for first 2 sec ----
                if (!noiseMeasured) {
                    if (System.currentTimeMillis() < noiseMeasureEnd) {
                        noiseSum += rms
                        noiseCount++
                        continue
                    } else {
                        backgroundNoise = if (noiseCount > 0) noiseSum / noiseCount else 30.0
                        noiseMeasured = true
                        Log.d("Audio", "Background noise measured = $backgroundNoise")
                    }
                }

                // ---- Smooth RMS ----
                rmsHistory.add(rms)
                if (rmsHistory.size > smoothingWindow)
                    rmsHistory.removeAt(0)

                val smoothedRms = rmsHistory.average()

                // ---- Dynamic threshold (IMPORTANT FIX) ----
                val speakingThreshold = backgroundNoise * 1.4   // 40% above noise

                if (smoothedRms > speakingThreshold) {

                    // USER SPEAKING
                    silenceStartTime = 0L

                    Log.d("Audio", "Speaking RMS=$smoothedRms Noise=$backgroundNoise")

                } else {

                    if (silenceStartTime == 0L)
                        silenceStartTime = System.currentTimeMillis()

                    val silenceDuration =
                        System.currentTimeMillis() - silenceStartTime

                    Log.d("Audio", "Silent for $silenceDuration ms RMS=$smoothedRms")

                    if (silenceDuration >= 5000) {
                        Log.d("Audio", "5 sec silence → stopping mic")

                        requireActivity().runOnUiThread {
                            stopAudioStreaming()
                            disconnectWebSocket()
                            voiceDialog?.dismiss()
                        }
                        break
                    }
                }
            }
        }.start()
    }



    fun reconnectWebSocket() {
        try {
            webSocket?.cancel()
             connectWebSocket() // implement your WebSocket creation logic
            Log.d("Audio", "WebSocket reconnected")
        } catch (e: Exception) {
            Log.e("Audio", "WebSocket reconnect failed: ${e.message}")
        }
    }

    private fun disconnectWebSocket() {
        webSocket?.close(1000, "Closing")
        webSocket = null
    }
    private fun stopAudioStreaming() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }
    fun navigateFromDashboard(navController: NavController, destinationRaw: String) {
        val destination = destinationRaw.lowercase()

        when {
            listOf(
                "vital page", "vital screen", "vital view", "open vital",
                "open vital page", "open vital screen", "open vital view"
            ).any { it in destination } -> {
                navController.navigate(R.id.action_dashboard_to_voiceFragment)
            }

            listOf(
                "symptom page", "symptom screen", "symptom view", "open symptom", "open symptom page", "open symptom screen", "open symptom view"
            ).any { it in destination } -> {
                navController.navigate(R.id.action_dashboard_to_symptomsFragment)
            }

            listOf(
                "pills reminder page", "pills page", "pills screen", "pills view",
                "open pills", "open pills reminder", "open pills page", "open pills screen", "open pills view"
            ).any { it in destination } -> {
                //navController.navigate(R.id.action_dashboard_to_pillsReminder)
            }

            listOf(
                "diet intake", "diet page", "diet screen", "diet view",
                "open diet", "open diet intake", "open diet page", "open diet screen", "open diet view"
            ).any { it in destination } -> {
                navController.navigate(R.id.action_dashboard_to_dietChecklist)
            }

            listOf(
                "fluid intake page", "fluid page", "fluid screen", "fluid view",
                "open fluid", "open fluid intake", "open fluid page", "open fluid screen", "open fluid view"
            ).any { it in destination } -> {
                navController.navigate(R.id.action_dashboard_to_fluidFragment)
            }

            listOf(
                "fluid history page", "fluid history screen", "fluid history view",
                "open fluid history", "open fluid history page", "open fluid history screen", "open fluid history view"
            ).any { it in destination } -> {
                navController.navigate(R.id.action_dashboard_to_fluidInputHistoryFragment)
            }

            listOf(
                "output history", "output page", "output screen", "output view",
                "open output", "open output history", "open output page", "open output screen", "open output view"
            ).any { it in destination } -> {
                navController.navigate(R.id.action_dashboard_to_fluidOutputFragment)
            }

            listOf(
                "upload report page", "upload report screen", "upload report view",
                "open upload report", "open upload report page", "open upload report screen", "open upload report view"
            ).any { it in destination } -> {
                navController.navigate(R.id.action_dashboard_to_uploadReportHistory)
            }

            listOf(
                "allergies page", "allergies screen", "allergies view",
                "open allergies", "open allergies page", "open allergies screen", "open allergies view"
            ).any { it in destination } -> {
                navController.navigate(R.id.action_dashboard_to_allergies3)
            }

            // Optional: Enable text/notes navigation
            // listOf("text", "notes", "open text", "open notes").any { it in destination } -> {
            //     navController.navigate(R.id.action_dashboard_to_notesFragment)
            // }

            else -> {
                // Optionally log or toast for unrecognized command
            }
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

        updateCardTitle(binding.sleepScoreId, "Sleep Score",  )
        updateCardTitle(binding.totalSleepId, "Total Sleep",  )
        updateCardTitle(binding.timeInBedId, "Time In Bed",  )
        updateCardTitle(binding.fulSleepCycleId, "Sleep Cycles",  )
        updateCardTitle(binding.restorativeSleepId, "Restorative Sleep", )
        updateCardTitle(binding.morningAlertnessId, "Morning Alertness",  )
        updateCardTitle(binding.tossesAndTurnsId, "Tosses and Turns", )
        updateCardTitle(binding.averageBodyTempId, "Temperature", )
        updateCardTitle(binding.activieHoursId, "Active Hours",  )
        updateCardTitle(binding.StepsId, "Steps", )
        updateCardTitle(binding.ActiveminutesId, "Active Minutes",  )
        updateCardTitle(binding.WeeklyActiveMinutesId, "Weekly Active Minutes",  )
        updateCardTitle(binding.recoveryScoreId, "Recovery Index",  )
        updateCardTitle(binding.lastNightHrvId, "Last Night's HRV", )
        updateCardTitle(binding.SleepStageHrvId, "Sleep Stage HRV", )
        updateCardTitle(binding.StressRhythmScoreId, "Stress Rhythm Score",  )
            // Temperature Deviation (special formatting)
            binding.tempDeviationId.title.text = "Temperature Deviation"


    }
    private fun updateCardTitle(card: SleepLayoutBinding, title: String, ) {
        card.title.text = title


    }

    // ---------------------------------------------------------------------
    //  OBSERVE: SLEEP VALUE DETAILS
    // ---------------------------------------------------------------------
    private fun observeSleepValues() {


            binding.sleepEfficiencyId.title.text = "Sleep Efficiency"
            binding.sleepEfficiencyId.value.text =  "--"


        updateStageTitle(binding.remSleepId, "REM Sleep",  )
        updateStageTitle(binding.deepSleepId, "Deep Sleep",  )
        updateStageTitle(binding.lightSleepId, "Light Sleep", )




            binding.inactiveHoursId.title.text = "Inactive Time"
            binding.inactiveHoursId.value.text = "_"
        }
    private fun updateStageTitle(card: SleepLayoutBinding, title: String,  ) {

        card.title.text = title
    }

    private fun updateCard(card: SleepLayoutBinding, title: String, vital: Vital?) {
        card.title.text = title
        card.value.text = vital.toText()
        card.statusCardId.visibility = View.VISIBLE
        if (vital != null) {
            card.  status.text = vital.severityLevel
        }
        binding.tempDeviationId.title.text = "Temperature Devoatoion"
        binding.tempDeviationId.value.text = "${"%.1f".format(vital?.vitalValue ?: 0.0)}"
        binding.tempDeviationId.status.text = title

    }

    private fun updateStage(card: SleepLayoutBinding, title: String, sleepValue: SleepValue, stageName: String) {
        val stage = sleepValue.SleepStages?.firstOrNull { it.Title.equals(stageName, true) }
        card.title.text = title
        card.value.text = stage?.StageTimeText ?: "--"
        card.statusCardId.visibility = View.VISIBLE
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

private fun getProgressColor(progress: Int): Int {
        return when {
            progress <=  10 ->  Color.parseColor("#FF3737")
            progress <= 30 -> Color.parseColor("#FEA33C") // Orange
            progress <= 60 -> Color.parseColor("#1281FD") // Blue
            progress >= 80 ->  Color.parseColor("#00C67A")
            else ->  Color.parseColor("#2196F3")
        }
    }
//    private fun bindDailyChecklistProgress(list: List<DailyCheckItem>) {
//
//        binding.checklistContainer.removeAllViews()
//
//        list.forEach { item ->
//
//            val itemBinding = DailyChecklistWedgetBinding.inflate(
//                layoutInflater,
//                binding.checklistContainer,
//                false
//            )
//
//            val progress =
//                ((item.vitalValue / item.targetValue.toFloat()) * 100).toInt()
//
//            itemBinding.progressSteps.progress = 40
//
//            itemBinding.tvStepsLabel.text = item.goalName
//            itemBinding.tvStepsValue.text =
//                "${item.vitalValue.toInt()} / ${item.targetValue}"
//
//            when (item.isGoalAchieved) {
//                1 -> itemBinding.ivStepsIcon.setColorFilter(Color.GREEN)
//                0 -> itemBinding.ivStepsIcon.setColorFilter(Color.RED)
//            }
//
//            // 🔥 IMPORTANT PART
//            itemBinding.progressSteps.post {
//
//                val progressBarWidth = itemBinding.progressSteps.width
//                val filledWidth = progressBarWidth * progress / 100f
//
//                val labelX =
//                    itemBinding.tvStepsLabel.x + (itemBinding.tvStepsLabel.width / 2f)
//
//                if (filledWidth >= labelX) {
//                    itemBinding.tvStepsLabel.setTextColor(Color.WHITE)
//                } else {
//                    itemBinding.tvStepsLabel.setTextColor(Color.BLACK)
//                }
//            }
//
//            binding.checklistContainer.addView(itemBinding.root)
//        }
//
//    }
private fun bindDailyChecklistProgress(list: List<DailyCheckItem>) {

    binding.checklistContainer.removeAllViews()

    list.forEach { item ->

        val itemBinding = DailyChecklistWedgetBinding.inflate(
            layoutInflater,
            binding.checklistContainer,
            false
        )

        // ✅ SAFE progress calculation
        val progress = if (item.targetValue.toInt() > 0) {
            if(item.vmId.toString()=="298"){
                (((item.vitalValue/  1000) / item.targetValue.toFloat() ) * 100).toInt()
            }else{

                ((item.vitalValue / item.targetValue.toFloat()) * 100).toInt()
            }
        } else {
            0
        }



        if(progress.toString()=="0"){
        itemBinding.progressSteps.progress = 1
    }else{
        itemBinding.progressSteps.progress = progress

    }
        // ✅ Set texts
        itemBinding.tvStepsLabel?.text = item.goalName+" "+progress+"% "
        if(item.vmId.toString()=="298"){

            itemBinding.tvStepsValue.text =
                "${item.vitalValue.toInt()} / ${item.targetValue.toInt()*1000} ml"
        }else{

            itemBinding.tvStepsValue.text =
                "${item.vitalValue.toInt()} / ${item.targetValue}"
        }
        if(item.vmId.toString()=="248"){


            itemBinding.ivStepsIcon.setImageResource(R.drawable.steps_p)
        }
        else if(item.vmId.toString()=="298"){


            itemBinding.ivStepsIcon.setImageResource(R.drawable.water_p)

        }
        else if(item.vmId.toString()=="300"){



            itemBinding.ivStepsIcon.setImageResource(R.drawable.sleep_p)
        }
        else if(item.vmId.toString()=="301"){



            itemBinding.ivStepsIcon.setImageResource(R.drawable.glucose_p)
        }
        else if(item.vmId.toString()=="302"){



            itemBinding.ivStepsIcon.setImageResource(R.drawable.bp_p)
        }


        // ✅ Progress-based color
        val progressColor = getProgressColor(progress)

        // ✅ Apply colors
        itemBinding.progressSteps.progressTintList =
            ColorStateList.valueOf(progressColor)

        itemBinding.ivStepsIcon.setColorFilter(progressColor)


        // ✅ Goal achieved icon color (optional override)
        when (item.isGoalAchieved) {
            1 -> itemBinding.ivStepsIcon.setColorFilter(Color.GREEN)
            0 -> itemBinding.ivStepsIcon.setColorFilter(Color.BLACK)
        }

        // ✅ Text contrast logic (label over progress bar)
        itemBinding.progressSteps.post {

            val progressBarWidth = itemBinding.progressSteps.width
            val filledWidth = progressBarWidth * progress / 100f

            val labelCenterX =
                itemBinding.tvStepsLabel.x + (itemBinding.tvStepsLabel.width / 2f)

            if (filledWidth >= labelCenterX) {
                itemBinding.tvStepsLabel.setTextColor(Color.WHITE)
            } else {
                itemBinding.tvStepsLabel.setTextColor(Color.BLACK)
            }
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
private fun updateProgress(unit: String) {
    val goalEntry = PrefsManager().getEmployeeGoals().find { it.goalId == 13 }

    viewModel.totalQuantity.observe(viewLifecycleOwner) { totalValue  ->
        val goal = goalEntry?.targetValue ?: 0  // safe
        val remaining = goal?.minus(totalValue)
        if(totalValue< remaining!!){
            binding.hydrationCardId.tvHydrationProgress.text =
                "${totalValue} $unit consumed — ${remaining*1000} $unit to go"
        }
        else{
            binding.hydrationCardId.tvHydrationProgress.text =
                "${totalValue} $unit consumed — targed ${goal*1000} $unit "
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
//        binding.customLoaderContainer.visibility = if (show) View.VISIBLE else View.GONE
       // binding.customLoaderContainer.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun refreshDashboardData() {
       viewModel.getVitals()
    }



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
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
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

                        binding.medicineTitleID.setOnClickListener {
                            findNavController().navigate(R.id.action_dashboard_to_medicationFragment)
                        }

                        // Initialize adapter once with an empty list
                        val adapter = TabMedicineAdapter(mutableListOf()) { selectedMedicine ->

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

