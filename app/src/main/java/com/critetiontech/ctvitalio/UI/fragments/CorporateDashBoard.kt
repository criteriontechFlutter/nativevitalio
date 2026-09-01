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
import android.graphics.Typeface
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.NoiseSuppressor
import android.os.Build
import android.os.Bundle
import java.util.Locale
import kotlin.math.roundToInt
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
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
import com.critetiontech.ctvitalio.adapter.UpcomingAdapter
import com.critetiontech.ctvitalio.adapter.VitalAdapter
import com.critetiontech.ctvitalio.databinding.DailyChecklistWedgetBinding
import com.critetiontech.ctvitalio.databinding.FragmentCorporateDashBoardBinding
import com.critetiontech.ctvitalio.databinding.SleepLayoutBinding
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.DashboardAnimationHelper
import com.critetiontech.ctvitalio.utils.LoaderUtils.hideLoading
import com.critetiontech.ctvitalio.utils.LoaderUtils.showLoading
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.utils.ToastUtils
import com.critetiontech.ctvitalio.utils.showRetrySnackbar
import com.critetiontech.ctvitalio.viewmodel.ChallengesViewModel
import com.critetiontech.ctvitalio.viewmodel.DashboardViewModel
import com.critetiontech.ctvitalio.viewmodel.PillsReminderViewModal
import com.critetiontech.ctvitalio.viewmodel.WebSocketState
import com.critetiontech.ctvitalio.widgets.GymOfferBottomSheet
import com.google.android.material.snackbar.Snackbar
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString.Companion.toByteString


class CorporateDashBoard : Fragment() {
    private lateinit var binding: FragmentCorporateDashBoardBinding
    private lateinit var viewModel: DashboardViewModel
    private var voiceDialog: Dialog? = null
    private lateinit var voiceButton: ImageView
    private lateinit var closeButton: ImageView
    private lateinit var challengesViewModel: ChallengesViewModel
    private lateinit var pillsViewModel: PillsReminderViewModal
    private lateinit var dailyTipAdapter: DailyTipAdapter
    private lateinit var indicatorAdapter: IndicatorAdapter
    private lateinit var tabMedicineAdapter: TabMedicineAdapter
    private var snackbar: Snackbar? = null
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var silenceStartTime = 0L
    private var backgroundNoise = 0.0
    private var noiseMeasured = false
    private var webSocket: WebSocket? = null
    private val RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    private val PERMISSION_REQUEST_CODE = 101
    private val tabLabels = listOf("Home", "Snaps", "Reminders", "Challenges")
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
    private var lastVoiceTime = 0L
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
        challengesViewModel = ViewModelProvider(this)[ChallengesViewModel::class.java]
        pillsViewModel.getAllPatientMedication()


        binding.upcomingchallengesID.visibility=  View.GONE
        challengesViewModel.getJoinedChallenge()
        challengesViewModel.pendingChallenges.observe(viewLifecycleOwner) { list ->
binding.upcomingchallengesID.visibility=if(list.isEmpty()) View.GONE else View.VISIBLE
            binding.upcomingRecycler.layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )

            binding.upcomingRecycler.adapter =
                UpcomingAdapter(list) { challenge ->

                    // ✅ correct
                    challengesViewModel.joinChallenge(challenge.challengeId.toString())

                }

        }
        binding.notificationIcon.setOnClickListener {


            findNavController().navigate(R.id.action_dashboard_to_notificationFragment)
        }
        binding.ringIcon.setOnClickListener {
            val intent = Intent(requireActivity(), UltraHumanActivity::class.java)
            intent.putExtra("source", "HOME")   // or any identifier
            startActivity(intent)
        }
        Handler(Looper.getMainLooper()).postDelayed({

            GymOfferBottomSheet().show(parentFragmentManager, "GymOffer")

        }, 5000) // 3000 = 3 seconds delay
        challengesViewModel.getJoinedChallenge()

        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            101
        )
        binding.headerContainer.setOnClickListener {

           // findNavController().navigate(R.id.action_dashboard_to_new_corporate_dashboard)

        }
        binding.progressCircler.animateProgress(10f)

        // Hide real content, show shimmer while first load happens
        binding.homeId.visibility = View.INVISIBLE
        binding.shimmerLayout.visibility = View.VISIBLE
        binding.shimmerLayout.startShimmer()

        // Header elements animate in immediately
        animatePageLoad()

        (requireActivity() as? BaseActivity)?.setSystemBarsColor(
            statusBarColor = R.color.primaryBlue,
            navBarColor = R.color.white,
            lightIcons = true
        )

        var shimmerDismissed = false
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (!isLoading && !shimmerDismissed) {
                shimmerDismissed = true
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.animate()
                    .alpha(0f)
                    .setDuration(250)
                    .withEndAction {
                        binding.shimmerLayout.visibility = View.GONE
                        binding.shimmerLayout.alpha = 1f
                        binding.homeId.visibility = View.VISIBLE
                        binding.homeId.alpha = 0f
                        binding.homeId.translationY = 40f
                        binding.homeId.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(500)
                            .setInterpolator(DecelerateInterpolator(2.0f))
                            .start()
                    }
                    .start()
            }
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
//
//        binding.mindfulness.setOnClickListener {
//            findNavController().navigate(R.id.action_dashboard_to_mindfulnessFragment)
//        }


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



//
//        viewModel.activeChallenges.observe(viewLifecycleOwner) { list ->
//            binding.newChallengedRecyclerView.adapter = NewChallengedAdapter(
//                list.toMutableList(),
//                onJoinClick  =  { challenge ->
//                    challengesViewModel.joinChallenge( challenge.challengeId.toString())
//                },
//                onDetailsClick  =  { challenge ->
//                    val bundle = Bundle().apply {
//                        putSerializable("challenges", challenge)
//                    }
//                     findNavController().navigate(R.id.action_dashboard_to_challengeDetailsFragment, bundle)
//
//                }
//            )
//            binding.challengedId.adapter = NewChallengedAdapter(
//                list.toMutableList(),
//                onJoinClick =  { challenge ->
//                    challengesViewModel.joinChallenge( challenge.challengeId.toString())
//                },
//                onDetailsClick =  { challenge ->
//                    val bundle = Bundle().apply {
//                        putSerializable("challenges", challenge)
//                    }
//                    findNavController().navigate(R.id.action_dashboard_to_challengeDetailsFragment, bundle)
//
//                }
//            )
//
//
//
//            binding.activechalgesId.text="Active Challenges ("+list.size.toString()+")"
//            binding.activeChalleTextId.text="Active Challenges ("+list.size.toString()+")"
//
//            val visible = list.isNotEmpty()
//
//            binding.activechalgesId.visibility = if (visible) View.VISIBLE else View.GONE
//            binding.activeChalleTextId.visibility = if (visible) View.VISIBLE else View.GONE
//            setupActiveChallenges(list.size)
//        }

        viewModel.notificationList.observe(viewLifecycleOwner) { list ->

            binding.notificationBadge.text=list.count().toString()
        }

        binding.mindfulness.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_mindfulnessFragment)
        }

        binding.challengesTab.visibility=View.GONE

        challengesViewModel.joinedChallenges.observe(viewLifecycleOwner) { list ->


            binding.activeChalleTextIdtab.visibility=if(list.isEmpty()) View.GONE else View.VISIBLE
            binding.newChallengedRecyclerView.adapter = NewChallengedAdapter(
                list.toMutableList(),
                onJoinClick  =  { challenge ->
//                    challengesViewModel.joinChallenge( challenge.challengeId.toString())

                    val title = challenge.title.lowercase()

                    when {
                        title.contains("glucose") -> {
                            findNavController().navigate(R.id.action_dashboard_to_glucoseHistory)
                        }

                        title.contains("water") -> {
                            findNavController().navigate(R.id.action_dashboard_to_fluidFragment)
                        }

                        title.contains("blood") || title.contains("hypertension") -> {
                            findNavController().navigate(R.id.action_dashboard_to_BPHistory)
                        }
                    }

                },
                onDetailsClick  =  { challenge ->
                    val bundle = Bundle().apply {
                        putSerializable("challenges", challenge)
                    }
                    findNavController().navigate(R.id.action_dashboard_to_newChallengeDetails, bundle)

                }
            )

            binding.challengedIdtab.adapter = NewChallengedAdapter(
                list.toMutableList(),
                onJoinClick  =  { challenge ->
//                    challengesViewModel.joinChallenge( challenge.challengeId.toString())



                    val title = challenge.title.lowercase()

                    when {
                        title.contains("glucose") -> {
                            findNavController().navigate(R.id.action_dashboard_to_glucoseHistory)
                        }

                        title.contains("water") -> {
                            findNavController().navigate(R.id.action_dashboard_to_fluidFragment)
                        }

                        title.contains("blood") || title.contains("hypertension") -> {
                            findNavController().navigate(R.id.action_dashboard_to_BPHistory)
                        }
                    }

                },
                onDetailsClick  =  { challenge ->
                    val bundle = Bundle().apply {
                        putSerializable("challenges", challenge)
                    }
                    findNavController().navigate(R.id.action_dashboard_to_newChallengeDetails, bundle)

                }
            )
            binding.challengedId.adapter = NewChallengedAdapter(
                list.toMutableList(),
                onJoinClick =  { challenge ->
//                    challengesViewModel.joinChallenge( challenge.challengeId.toString())



                    val title = challenge.title.lowercase()

                    when {
                        title.contains("glucose") -> {
                            findNavController().navigate(R.id.action_dashboard_to_glucoseHistory)
                        }

                        title.contains("water") -> {
                            findNavController().navigate(R.id.action_dashboard_to_fluidFragment)
                        }

                        title.contains("blood") || title.contains("hypertension") -> {
                            findNavController().navigate(R.id.action_dashboard_to_BPHistory)
                        }
                    }
                },
                onDetailsClick =  { challenge ->
                    val bundle = Bundle().apply {
                        putSerializable("challenges", challenge)
                    }
                    findNavController().navigate(R.id.action_dashboard_to_newChallengeDetails, bundle)

                }
            )



            binding.activechalgesId.text="Active Challenges ("+list.size.toString()+")"
            binding.activeChalleTextId.text="Active Challenges ("+list.size.toString()+")"

            val visible = list.isNotEmpty()

            binding.activechalgesId.visibility = if (visible) View.VISIBLE else View.GONE
            binding.activeChalleTextId.visibility =   View.GONE
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

        // Pull-to-refresh only works when mood section is fully collapsed (end state).
        // In start state, MotionLayout's OnSwipe intercepts the same touch events.
        binding.swipeRefreshLayout.isEnabled = false

        binding.moodLayout.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(
                motionLayout: MotionLayout?, startId: Int, endId: Int
            ) {
                binding.swipeRefreshLayout.isEnabled = false
            }

            override fun onTransitionChange(
                motionLayout: MotionLayout?, startId: Int, endId: Int, progress: Float
            ) {}

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                // Enable pull-to-refresh only in end state (content full-screen, mood hidden)
                binding.swipeRefreshLayout.isEnabled = (currentId == R.id.end)
            }

            override fun onTransitionTrigger(
                motionLayout: MotionLayout?, triggerId: Int, positive: Boolean, progress: Float
            ) {}
        })

        binding.energyCard.setOnClickListener{
            findNavController().navigate(R.id.action_dashboard_to_energyTank)
        }

        viewModel.latestEnergy.observe(viewLifecycleOwner) { energy ->

            if (energy == null) {
                binding.energyTitle.text = getString(R.string.energy_story_awaits)

                val typeface = ResourcesCompat.getFont(
                    requireContext(),
                    R.font.source_serif_pro
                )

                binding.energyTitle.setTypeface(typeface, Typeface.BOLD_ITALIC)
                binding.energyTitle.typeface = typeface
                binding.energySubtitle.text = getString(R.string.log_energy_today)
                binding.energyImage.setImageResource(R.drawable.emtyp_energy)
                binding.energyid.setBackgroundResource(R.drawable.rounded_card_bg)
            } else {
                binding.energyTitle.text = getString(R.string.energized_today, energy)
                binding.energySubtitle.text = getString(R.string.ready_take_day)
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
        binding.mindfulnessId.setOnClickListener {

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

//        observeVitalList()
//        observeSleepValues()
        viewModel.vitalList.observe(viewLifecycleOwner) { vitalList ->
            if (vitalList.isNullOrEmpty()) return@observe

            binding.vitalRecyclerView.apply {
                layoutManager = GridLayoutManager(requireContext(), 2)
                adapter = VitalAdapter(vitalList)
            }

            fun List<Vital>.findVital(vararg names: String): Vital? =
                firstOrNull { vital -> names.any { it.equals(vital.vitalName, ignoreCase = true) } }

            fun Vital?.asText(): String =
                this?.vitalValue?.toInt()?.toString() ?: "--"

            fun bindStatusBadge(tv: TextView, vital: Vital?, defaultColor: String) {
                val severity = vital?.severityLevel
                if (!severity.isNullOrBlank()) {
                    tv.text = severity
                    val color = try {
                        Color.parseColor(vital.vitalColor ?: defaultColor)
                    } catch (e: Exception) {
                        Color.parseColor(defaultColor)
                    }
                    tv.setTextColor(color)
                    tv.backgroundTintList = ColorStateList.valueOf(
                        ColorUtils.setAlphaComponent(color, (0.15f * 255).toInt())
                    )
                } else {
                    tv.text = "--"
                }
            }

            val wellnessVital = vitalList.findVital("Wellness Score", "WellnessScore", "Wellness")
            val sleepVital = vitalList.findVital("SleepScore", "Sleep", "TotalSleep")
            val movementVital = vitalList.findVital("MovementIndex", "Movement")
            val stressVital = vitalList.findVital("StressScore", "Stress")
            val recoveryVital = vitalList.findVital("RecoveryIndex", "Recovery")

            binding.sleepProgressIds.apply {
                // 1. Wellness Score
                wellnessScoreNumber.text = wellnessVital.asText()

                val wellnessSeverity = wellnessVital?.severityLevel
                if (!wellnessSeverity.isNullOrBlank()) {
                    attentionBadges.text = wellnessSeverity
                    attentionBadges.visibility = View.VISIBLE
                    val color = try {
                        Color.parseColor(wellnessVital?.vitalColor ?: "#FFD600")
                    } catch (e: Exception) {
                        Color.parseColor("#FFD600")
                    }
                    attentionBadges.setTextColor(color)
                    attentionBadges.backgroundTintList = ColorStateList.valueOf(
                        ColorUtils.setAlphaComponent(color, (0.15f * 255).toInt())
                    )
                } else {
                    attentionBadges.visibility = View.GONE
                }

                val score = wellnessVital?.vitalValue?.toInt() ?: 0
                if (score > 0) {
                    binding.progressCircler.animateProgress(score.toFloat())
                }

                when (score) {
                    in 90..100 -> {
                        dashboardAnimatedCard.setDefaultWaveColors(
                            backgroundColor = Color.parseColor("#F7FDF9"),
                            backWaveColor = Color.parseColor("#F3FAF5"),
                            frontWaveColor = Color.parseColor("#EBF5EE")
                        )
                    }
                    in 80..89 -> {
                        dashboardAnimatedCard.setDefaultWaveColors(
                            backgroundColor = Color.parseColor("#F4F8FD"),
                            backWaveColor = Color.parseColor("#ECF3FA"),
                            frontWaveColor = Color.parseColor("#E3EBF4")
                        )
                    }
                    in 60..79 -> {
                        dashboardAnimatedCard.setDefaultWaveColors(
                            backgroundColor = Color.parseColor("#FDF8F0"),
                            backWaveColor = Color.parseColor("#F9F1E6"),
                            frontWaveColor = Color.parseColor("#F5EBDD")
                        )
                    }
                    else -> {
                        dashboardAnimatedCard.setDefaultWaveColors(
                            backgroundColor = Color.parseColor("#FDF6F6"),
                            backWaveColor = Color.parseColor("#F9EEEE"),
                            frontWaveColor = Color.parseColor("#F5E7E7")
                        )
                    }
                }

                // 2. Sleep
                sleepValue.text = sleepVital.asText()
                bindStatusBadge(sleepstatusId, sleepVital, "#EA6C00")

                // 3. Movement
                movementValue.text = movementVital.asText()
                bindStatusBadge(movementstatusId, movementVital, "#00B77E")

                // 4. Stress
                stressValue.text = stressVital.asText()
                bindStatusBadge(stressstatusId, stressVital, "#0090F6")

                // 5. Recovery
                recoveryValue.text = recoveryVital.asText()
                bindStatusBadge(recoverystatusId, recoveryVital, "#EA6C00")
            }

            val bpSys = vitalList.find { it.vitalName.equals("BP_Sys", ignoreCase = true) }
            val bpDia = vitalList.find { it.vitalName.equals("BP_Dias", ignoreCase = true) }

            if (bpSys != null && bpDia != null) {
                val bpVital = Vital().apply {
                    vitalName = "Blood Pressure"
                    vitalValue = 0.0
                    unit = "${bpSys.vitalValue?.toInt()}/${bpDia.vitalValue?.toInt()}  "
                    vitalDateTime = bpSys.vitalDateTime
                }
            }
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
//        binding.viewAllSleepDataaId.visibility=View.GONE
//binding.showId.showHideId.setOnClickListener{
//    binding.viewAllSleepDataaId.visibility=View.VISIBLE
//    binding.showId.showHideId.visibility=View.GONE
//}
//        binding.hideId.showHideId.setOnClickListener{
//            binding.viewAllSleepDataaId.visibility=View.GONE
//            binding.showId.showHideId.visibility=View.VISIBLE
//        }



viewModel.vitalList.observe(viewLifecycleOwner) { vitalList ->
    val vitalStepsIndex = vitalList.find { it.vitalName.equals("TotalSteps", ignoreCase = true) }
//    binding.StepsId.title.text = "Steps"
//    binding.StepsId.value.text = vitalStepsIndex?.vitalValue.toString()
//    binding.StepsId.statusCardId.visibility = View.VISIBLE
//    binding.StepsId.status.applyStatusStyle(
//        rawColor = vitalStepsIndex?.vitalColor,
//        getPastelColor = ::getPastelColor
//    )
//    binding.StepsId.status.text = vitalStepsIndex?.severityLevel.toString()


//    val totalSleepIndex = vitalList.find { it.vitalName.equals("TotalSleep", ignoreCase = true) }
//    binding.totalSleepId.title.text = "Total Sleep"
//    binding.totalSleepId.value.text = totalSleepIndex?.vmValueText.toString()
//    binding.totalSleepId.statusCardId.visibility = View.VISIBLE
//    binding.totalSleepId.status.applyStatusStyle(
//        rawColor = totalSleepIndex?.vitalColor,
//        getPastelColor = ::getPastelColor
//    )
//    binding.totalSleepId.status.text = totalSleepIndex?.severityLevel.toString()
//
//
//    val sleepEfficiency = vitalList.find { it.vitalName.equals("Sleep efficiency", ignoreCase = true) }
//    binding.sleepEfficiencyId.title.text = "Sleep Efficiency"
//    binding.sleepEfficiencyId.value.text = sleepEfficiency?.vmValueText.toString()
//    binding.sleepEfficiencyId.statusCardId.visibility = View.VISIBLE
//    binding.sleepEfficiencyId.status.applyStatusStyle(
//        rawColor = sleepEfficiency?.vitalColor,
//        getPastelColor = ::getPastelColor
//    )
//    binding.sleepEfficiencyId.status.text = sleepEfficiency?.severityLevel.toString()



    val timeinBedIndex = vitalList.find {
        it.vitalName.equals("TimeInBed", ignoreCase = true)
    }
//    bindVitalView(
//        vitalName = "Time in Bed",
//        vital = timeinBedIndex,
//        titleView = binding.timeInBedId.title,
//        valueView = binding.timeInBedId.value,
//        statusView = binding.timeInBedId.status,
//        statusCardView = binding.timeInBedId.statusCardId,
//        getPastelColor = ::getPastelColor
//    )
//
//    val sleepCycleIndex = vitalList.find {
//        it.vitalName.equals("SleepCycles", ignoreCase = true)
//    }
//    bindVitalView(
//        vitalName = "Full Sleep Cycle",
//        vital = sleepCycleIndex,
//        titleView = binding.fulSleepCycleId.title,
//        valueView = binding.fulSleepCycleId.value,
//        statusView = binding.fulSleepCycleId.status,
//        statusCardView = binding.fulSleepCycleId.statusCardId,
//        getPastelColor = ::getPastelColor
//    )
//
//    val RemSleepIndex = vitalList.find {
//        it.vitalName.equals("REM Sleep", ignoreCase = true)
//    }
//    bindVitalView(
//        vitalName = "REM Sleep",
//        vital = RemSleepIndex,
//        titleView = binding.remSleepId.title,
//        valueView = binding.remSleepId.value,
//        statusView = binding.remSleepId.status,
//        statusCardView = binding.remSleepId.statusCardId,
//        getPastelColor = ::getPastelColor
//    )
//
//    val deepsleepIndex = vitalList.find {
//        it.vitalName.equals("Deep Sleep", ignoreCase = true)
//    }
//    bindVitalView(
//        vitalName = "Deep Sleep",
//        vital = deepsleepIndex,
//        titleView = binding.deepSleepId.title,
//        valueView = binding.deepSleepId.value,
//        statusView = binding.deepSleepId.status,
//        statusCardView = binding.deepSleepId.statusCardId,
//        getPastelColor = ::getPastelColor
//    )
//

    val awakeIndex = vitalList.find {
        it.vitalName.equals("Awake", ignoreCase = true)
    }
//    bindVitalView(
//        vitalName = "Awake",
//        vital = awakeIndex,
//        titleView = binding.awakeSleepId.title,
//        valueView = binding.awakeSleepId.value,
//        statusView = binding.awakeSleepId.status,
//        statusCardView = binding.awakeSleepId.statusCardId,
//        getPastelColor = ::getPastelColor
//    )

    val lightSleepIndex = vitalList.find {
        it.vitalName.equals("Light Sleep", ignoreCase = true)
    }
//    bindVitalView(
//        vitalName = "Light Sleep",
//        vital = lightSleepIndex,
//        titleView = binding.lightSleepId.title,
//        valueView = binding.lightSleepId.value,
//        statusView = binding.lightSleepId.status,
//        statusCardView = binding.lightSleepId.statusCardId,
//        getPastelColor = ::getPastelColor
//    )


    val restorativeSleepIndex = vitalList.find {
        it.vitalName.equals("RestorativeSleep", ignoreCase = true)
    }
//    bindVitalView(
//        vitalName = "Restorative Sleep",
//        vital = restorativeSleepIndex,
//        titleView = binding.restorativeSleepId.title,
//        valueView = binding.restorativeSleepId.value,
//        statusView = binding.restorativeSleepId.status,
//        statusCardView = binding.restorativeSleepId.statusCardId,
//        getPastelColor = ::getPastelColor
//    )
    val activeHoursIndex = vitalList.find {
        it.vitalName.equals("ActiveHours", ignoreCase = true)
    }
//    bindVitalViewVitalValue(
//        vitalName = "Active Hours",
//        vital = activeHoursIndex,
//        titleView = binding.activieHoursId.title,
//        valueView = binding.activieHoursId.value,
//        statusView = binding.activieHoursId.status,
//        statusCardView = binding.activieHoursId.statusCardId,
//        getPastelColor = ::getPastelColor
//    )

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
//    bindVitalView(
//        vitalName = "Morning Alertness",
//        vital = morningalertnessId,
//        titleView = binding.morningAlertnessId.title,
//        valueView = binding.morningAlertnessId.value,
//        statusView = binding.morningAlertnessId.status,
//        statusCardView = binding.morningAlertnessId.statusCardId,
//        getPastelColor = ::getPastelColor
//    )


    val tossandTurnIndex = vitalList.find {
        it.vitalName.equals("TossTurn", ignoreCase = true)
    }
//    bindVitalView(
//        vitalName = "Tosses and Turn",
//        vital = tossandTurnIndex,
//        titleView = binding.tossesAndTurnsId.title,
//        valueView = binding.tossesAndTurnsId.value,
//        statusView = binding.tossesAndTurnsId.status,
//        statusCardView = binding.tossesAndTurnsId.statusCardId,
//        getPastelColor = ::getPastelColor
//    )

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




//
//    val sleepScoreIndex = vitalList.find { it.vitalName.equals("SleepScore", ignoreCase = true) }
//    binding.sleepScoreId.title.text = "Sleep Score"
//    binding.sleepScoreId.value.text = sleepScoreIndex?.vmValueText.toString()
//    binding.sleepScoreId.statusCardId.visibility = View.VISIBLE
//    binding.sleepScoreId.status.applyStatusStyle(
//        rawColor = sleepScoreIndex?.vitalColor,
//        getPastelColor = ::getPastelColor
//    )
//    binding.sleepScoreId.status.text = sleepScoreIndex?.severityLevel.toString()
//
//
//
//
//
//    val TemperatureBody = vitalList
//        ?.firstOrNull { it.vitalName.equals("Temperature", ignoreCase = true) }
//    binding.averageBodyTempId.title.text = "Average Body Temp."
//    binding.averageBodyTempId.value.text = "%.1f".format(TemperatureBody?.vitalValue ?: 0.0)
//    binding.averageBodyTempId.statusCardId.visibility = View.VISIBLE
//
//    binding.averageBodyTempId.status.applyStatusStyle(
//        rawColor = TemperatureBody?.vitalColor,
//        getPastelColor = ::getPastelColor
//    )
//    binding.averageBodyTempId.status.text = TemperatureBody?.severityLevel.toString()
//
//    val activeMinutes = vitalList
//        ?.firstOrNull { it.vitalName.equals("ActiveMinutes", ignoreCase = true) }
//    binding.ActiveminutesId.title.text = "Active Minutes"
//    binding.ActiveminutesId.value.text = activeMinutes?.vitalValue.toString()
//    binding.ActiveminutesId.statusCardId.visibility = View.VISIBLE
//
//    binding.ActiveminutesId.status.applyStatusStyle(
//        rawColor = activeMinutes?.vitalColor,
//        getPastelColor = ::getPastelColor
//    )
//    binding.ActiveminutesId.status.text = activeMinutes?.severityLevel.toString()
//
//
//
//    val Temperature = vitalList
//        ?.firstOrNull { it.vitalName.equals("TemperatureDeviation", ignoreCase = true) }
//    binding.tempDeviationId.title.text = "Temperature Deviation"
//    binding.tempDeviationId.value.text = "${"%.1f".format(Temperature?.vitalValue ?: 0.0)}"
//    binding.tempDeviationId.statusCardId.visibility = View.VISIBLE
//    binding.tempDeviationId.status.applyStatusStyle(
//        rawColor = Temperature?.vitalColor,
//        getPastelColor = ::getPastelColor
//    )
//    binding.tempDeviationId.status.text = Temperature?.severityLevel.toString()
//
//
//    val recoveryIndex = vitalList
//        ?.firstOrNull { it.vitalName.equals("RecoveryIndex", ignoreCase = true) }
//    binding.recoveryScoreId.title.text = "Recovery Score"
//    binding.recoveryScoreId.value.text = recoveryIndex?.vitalValue.toString()
//    binding.recoveryScoreId.statusCardId.visibility = View.VISIBLE
//
//    binding.recoveryScoreId.status.applyStatusStyle(
//        rawColor = recoveryIndex?.vitalColor,
//        getPastelColor = ::getPastelColor
//    )
//    binding.recoveryScoreId.status.text = recoveryIndex?.severityLevel.toString()
//
//    val HRV = vitalList
//        ?.firstOrNull { it.vitalName.equals("HRV", ignoreCase = true) }
//    binding.lastNightHrvId.title.text = "Last Night's HRV"
//    binding.lastNightHrvId.value.text = HRV?.vitalValue.toString()
//    binding.lastNightHrvId.statusCardId.visibility = View.VISIBLE
//    binding.lastNightHrvId.status.applyStatusStyle(
//        rawColor = HRV?.vitalColor,
//        getPastelColor = ::getPastelColor
//    )
//    binding.lastNightHrvId.status.text = HRV?.severityLevel.toString()
//
//
//
//    binding.SleepStageHrvId.title.text = "Sleep Stage' HRV"
//    binding.SleepStageHrvId.value.text = HRV?.vitalValue.toString()
//    binding.SleepStageHrvId.statusCardId.visibility = View.VISIBLE
//    binding.SleepStageHrvId.status.applyStatusStyle(
//        rawColor = HRV?.vitalColor,
//        getPastelColor = ::getPastelColor
//    )
//    binding.SleepStageHrvId.status.text = HRV?.severityLevel.toString()
//    val movementIndex = vitalList
//        ?.firstOrNull { it.vitalName.equals("MovementIndex", ignoreCase = true) }
//
//    binding.movementIndexId.title.text = "Movement Index"
//    binding.movementIndexId.value.text = movementIndex?.vitalValue.toString()
//    binding.movementIndexId.statusCardId.visibility = View.VISIBLE
//    binding.movementIndexId.status.applyStatusStyle(
//        rawColor = movementIndex?.vitalColor,
//        getPastelColor = ::getPastelColor
//    )
//    binding.movementIndexId.status.text = movementIndex?.severityLevel.toString()

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
        if (vitals.isNullOrEmpty()) return@observe
        val RecoveryIndex = vitals.find { it.vitalID == 240 }
        val MovementIndex = vitals.find { it.vitalID == 241 }
        val StressScore = vitals.find { it.vitalID == 252 }
        val Glucose = vitals.find { it.vitalID == 249 }

        // Helper function for background color with opacity
        fun getColorWithOpacity(hexColor: String?, alphaPercent: Int = 15): Int {
            val color = Color.parseColor(hexColor ?: "#FFFFFF")
            val alpha = (alphaPercent / 100f * 255).toInt()
            return ColorUtils.setAlphaComponent(color, alpha)
        }

        RecoveryIndex?.let { r ->
            binding.sleepProgressIds.recoverystatusId.apply {
                text = r.severityLevel ?: "--"
                setTextColor(Color.parseColor(r.colourCode ?: "#EF4444"))
                backgroundTintList = ColorStateList.valueOf(getColorWithOpacity(r.colourCode))
            }
        }

        MovementIndex?.let { m ->
            binding.sleepProgressIds.movementstatusId.apply {
                text = m.severityLevel ?: "--"
                setTextColor(Color.parseColor(m.colourCode ?: "#EF4444"))
                backgroundTintList = ColorStateList.valueOf(getColorWithOpacity(m.colourCode))
            }
        }

        StressScore?.let { s ->
            binding.sleepProgressIds.stressstatusId.apply {
                text = s.severityLevel ?: "--"
                setTextColor(Color.parseColor(s.colourCode ?: "#EF4444"))
                backgroundTintList = ColorStateList.valueOf(getColorWithOpacity(s.colourCode))
            }
        }
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
        setupMedicineTab()
//        binding.sleepProgressIds.sleepContainerId.setOnClickListener(){
//            findNavController().navigate(R.id.action_dashboard_to_sleepDetails)
//        }
//        binding.sleepProgressIds.sleepContainerId.setOnClickListener(){
//           findNavController().navigate(R.id.action_dashboard_to_waterIntakeFragment)
//        }


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

        binding.dailyCheckListCard.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_smartGoalFragment)
        }

        viewModel.dailyCheckList.observe(viewLifecycleOwner) { list ->
            if (list.isNotEmpty()) {
                bindDailyChecklistSummary(list)
                bindDailyChecklistGoals(list)
                bindDailyChecklistProgress(list)
            }
        }

        viewModel.vitalList.observe(viewLifecycleOwner) {
            val list = viewModel.dailyCheckList.value
            if (!list.isNullOrEmpty()) {
                bindDailyChecklistSummary(list)
                bindDailyChecklistGoals(list)
                bindDailyChecklistProgress(list)
            }
        }

        viewModel.totalQuantity.observe(viewLifecycleOwner) {
            val list = viewModel.dailyCheckList.value
            if (!list.isNullOrEmpty()) {
                bindDailyChecklistSummary(list)
                bindDailyChecklistGoals(list)
                bindDailyChecklistProgress(list)
            }
        }


//        observeVitalList()
//        observeSleepValues()
//        binding.showId.showHideId.setOnClickListener{
//            binding.viewAllSleepDataaId.visibility=View.VISIBLE
//            binding.showId.showHideId.visibility=View.GONE
//        }
//        binding.hideId.showHideId.setOnClickListener{
//            binding.viewAllSleepDataaId.visibility=View.GONE
//            binding.showId.showHideId.visibility=View.VISIBLE
//        }
         wellnessDataBind()
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
            wellness.attentionBadges.text = insight.wellnessStatus.toString()
            wellness.attentionBadges.setTextColor(insight.colorCode.toColorInt())
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
//    private fun observeVitalList() {
//
//        updateCardTitle(binding.sleepScoreId, "Sleep Score",  )
//        updateCardTitle(binding.totalSleepId, "Total Sleep",  )
//        updateCardTitle(binding.timeInBedId, "Time In Bed",  )
//        updateCardTitle(binding.fulSleepCycleId, "Sleep Cycles",  )
//        updateCardTitle(binding.restorativeSleepId, "Restorative Sleep", )
//        updateCardTitle(binding.morningAlertnessId, "Morning Alertness",  )
//        updateCardTitle(binding.tossesAndTurnsId, "Tosses and Turns", )
//        updateCardTitle(binding.averageBodyTempId, "Temperature", )
//        updateCardTitle(binding.activieHoursId, "Active Hours",  )
//        updateCardTitle(binding.StepsId, "Steps", )
//        updateCardTitle(binding.ActiveminutesId, "Active Minutes",  )
//        updateCardTitle(binding.WeeklyActiveMinutesId, "Weekly Active Minutes",  )
//        updateCardTitle(binding.recoveryScoreId, "Recovery Index",  )
//        updateCardTitle(binding.lastNightHrvId, "Last Night's HRV", )
//        updateCardTitle(binding.SleepStageHrvId, "Sleep Stage HRV", )
//        updateCardTitle(binding.StressRhythmScoreId, "Stress Rhythm Score",  )
//            // Temperature Deviation (special formatting)
//            binding.tempDeviationId.title.text = "Temperature Deviation"
//
//
//    }
    private fun updateCardTitle(card: SleepLayoutBinding, title: String, ) {
        card.title.text = title


    }

    // ---------------------------------------------------------------------
    //  OBSERVE: SLEEP VALUE DETAILS
    // ---------------------------------------------------------------------
//    private fun observeSleepValues() {
//
//
//            binding.sleepEfficiencyId.title.text = "Sleep Efficiency"
//            binding.sleepEfficiencyId.value.text =  "--"
//
//
//        updateStageTitle(binding.remSleepId, "REM Sleep",  )
//        updateStageTitle(binding.deepSleepId, "Deep Sleep",  )
//        updateStageTitle(binding.lightSleepId, "Light Sleep", )
//
//
//
//
//            binding.inactiveHoursId.title.text = "Inactive Time"
//            binding.inactiveHoursId.value.text = "_"
//        }
    private fun updateStageTitle(card: SleepLayoutBinding, title: String,  ) {

        card.title.text = title
    }

//    private fun updateCard(card: SleepLayoutBinding, title: String, vital: Vital?) {
//        card.title.text = title
//        card.value.text = vital.toText()
//        card.statusCardId.visibility = View.VISIBLE
//        if (vital != null) {
//            card.  status.text = vital.severityLevel
//        }
//        binding.tempDeviationId.title.text = "Temperature Devoatoion"
//        binding.tempDeviationId.value.text = "${"%.1f".format(vital?.vitalValue ?: 0.0)}"
//        binding.tempDeviationId.status.text = title
//
//    }

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
    private data class ResolvedGoalInfo(
        val iconRes: Int,
        val progress: Int,
        val valueText: String,
        val isAchieved: Boolean
    )

    private fun resolveGoalInfo(item: DailyCheckItem): ResolvedGoalInfo {
        val goalName = item.goalName.orEmpty()
        val unit = item.unit.orEmpty().lowercase()
        val goalId = item.goalId
        val vmId = item.vmId
        val lastVitals = viewModel.vitalList.value

        return when {
            // 1. Water / Hydration Goal
            goalId == 13 || vmId == 298 || unit.contains("liter") || unit.contains("ltr") ||
            goalName.contains("water", true) || goalName.contains("fluid", true) || goalName.contains("hydration", true) -> {
                val targetL = item.targetValue?.toDoubleOrNull() ?: 3.0
                val targetMl = if (targetL <= 20.0) (targetL * 1000).roundToInt() else targetL.roundToInt()

                var currentMl = 0
                if (item.vitalValue > 0.0) {
                    currentMl = if (item.vitalValue <= 20.0) (item.vitalValue * 1000).roundToInt() else item.vitalValue.roundToInt()
                } else if (item.totalFluid_L > 0.0) {
                    currentMl = (item.totalFluid_L * 1000).roundToInt()
                } else {
                    val lastWater = lastVitals?.find { it.vitalId == 298 || it.vitalName.equals("WaterIntake", true) }?.vitalValue
                    if (lastWater != null && lastWater > 0.0) {
                        currentMl = if (lastWater <= 20.0) (lastWater * 1000).roundToInt() else lastWater.roundToInt()
                    } else {
                        val totalQty = viewModel.totalQuantity.value ?: 0
                        if (totalQty > 0) currentMl = totalQty
                    }
                }

                val progress = if (targetMl > 0) ((currentMl.toDouble() / targetMl.toDouble()) * 100).roundToInt().coerceIn(0, 100) else 0
                val achieved = item.isGoalAchieved == 1 || (targetMl > 0 && currentMl >= targetMl)

                ResolvedGoalInfo(
                    iconRes = R.drawable.water_p,
                    progress = if (achieved && progress < 100) 100 else progress,
                    valueText = "$currentMl / $targetMl ml",
                    isAchieved = achieved
                )
            }

            // 2. Steps / Activity Goal
            goalId == 1 || vmId == 248 || vmId == 295 || unit == "steps" ||
            goalName.contains("step", true) || goalName.contains("activity", true) || goalName.contains("walk", true) -> {
                val target = item.targetValue?.toIntOrNull() ?: 10000
                val current = if (item.vitalValue > 0.0) {
                    item.vitalValue.roundToInt()
                } else {
                    lastVitals?.find { it.vitalId == 248 || it.vitalName.equals("TotalSteps", true) }?.vitalValue?.roundToInt() ?: 0
                }

                val progress = if (target > 0) ((current.toDouble() / target.toDouble()) * 100).roundToInt().coerceIn(0, 100) else 0
                val achieved = item.isGoalAchieved == 1 || (target > 0 && current >= target)

                ResolvedGoalInfo(
                    iconRes = R.drawable.steps_p,
                    progress = if (achieved && progress < 100) 100 else progress,
                    valueText = "$current / $target",
                    isAchieved = achieved
                )
            }

            // 3. Sleep Goals (Sleep Early, Sleep Duration, etc.)
            goalId == 6 || vmId == 262 || vmId == 300 || vmId == 243 || vmId == 261 ||
            unit == "time" || goalName.contains("sleep", true) || goalName.contains("bedtime", true) -> {
                val isHourUnit = unit == "hrs" || unit == "hours" || (item.targetValue?.toDoubleOrNull() ?: 0.0) > 1.0

                if (isHourUnit) {
                    val target = item.targetValue?.toDoubleOrNull() ?: 8.0
                    val current = if (item.vitalValue > 0.0) {
                        item.vitalValue
                    } else {
                        lastVitals?.find { it.vitalId == 261 || it.vitalName.equals("TotalSleep", true) }?.vitalValue ?: 0.0
                    }

                    val progress = if (target > 0) ((current / target) * 100).roundToInt().coerceIn(0, 100) else 0
                    val achieved = item.isGoalAchieved == 1 || (target > 0 && current >= target)

                    ResolvedGoalInfo(
                        iconRes = R.drawable.sleep_p,
                        progress = if (achieved && progress < 100) 100 else progress,
                        valueText = "${String.format(Locale.US, "%.1f", current)} / ${String.format(Locale.US, "%.1f", target)} hrs",
                        isAchieved = achieved
                    )
                } else {
                    val target = item.targetValue?.toIntOrNull() ?: 1
                    val current = if (item.vitalValue > 0.0) item.vitalValue.roundToInt() else if (item.isGoalAchieved == 1) 1 else 0
                    val progress = if (target > 0) ((current.toDouble() / target.toDouble()) * 100).roundToInt().coerceIn(0, 100) else 0
                    val achieved = item.isGoalAchieved == 1 || (target > 0 && current >= target)

                    ResolvedGoalInfo(
                        iconRes = R.drawable.sleep_p,
                        progress = if (achieved && progress < 100) 100 else progress,
                        valueText = "$current / $target",
                        isAchieved = achieved
                    )
                }
            }

            // 4. Meditation / Mindfulness Goal
            goalId == 14 || vmId == 299 || goalName.contains("meditation", true) ||
            goalName.contains("mindfulness", true) || goalName.contains("breath", true) || goalName.contains("relax", true) -> {
                val target = item.targetValue?.toIntOrNull() ?: 20
                val current = item.vitalValue.roundToInt()
                val progress = if (target > 0) ((current.toDouble() / target.toDouble()) * 100).roundToInt().coerceIn(0, 100) else 0
                val achieved = item.isGoalAchieved == 1 || (target > 0 && current >= target)

                ResolvedGoalInfo(
                    iconRes = R.drawable.mindfullness,
                    progress = if (achieved && progress < 100) 100 else progress,
                    valueText = "$current / $target",
                    isAchieved = achieved
                )
            }

            // 5. Glucose / Blood Sugar Goal
            vmId == 249 || vmId == 301 || goalName.contains("glucose", true) || goalName.contains("sugar", true) -> {
                val target = item.targetValue?.toIntOrNull() ?: 1
                val current = if (item.glucoseCount > 0) {
                    item.glucoseCount
                } else if (item.vitalValue > 0.0) {
                    item.vitalValue.roundToInt()
                } else {
                    lastVitals?.find { it.vitalId == 249 || it.vitalName.equals("Glucose", true) }?.vitalValue?.roundToInt() ?: 0
                }

                val progress = if (target > 0) ((current.toDouble() / target.toDouble()) * 100).roundToInt().coerceIn(0, 100) else 0
                val achieved = item.isGoalAchieved == 1 || (target > 0 && current >= target)

                ResolvedGoalInfo(
                    iconRes = R.drawable.glucose_p,
                    progress = if (achieved && progress < 100) 100 else progress,
                    valueText = "$current / $target",
                    isAchieved = achieved
                )
            }

            // 6. Blood Pressure (BP) Goal
            vmId == 302 || goalName.contains("bp", true) || goalName.contains("blood pressure", true) -> {
                val target = item.targetValue?.toIntOrNull() ?: 1
                val current = if (item.bpCount > 0) {
                    item.bpCount
                } else if (item.vitalValue > 0.0) {
                    item.vitalValue.roundToInt()
                } else {
                    if (lastVitals?.any { it.vitalId == 4 || it.vitalId == 6 } == true) 1 else 0
                }

                val progress = if (target > 0) ((current.toDouble() / target.toDouble()) * 100).roundToInt().coerceIn(0, 100) else 0
                val achieved = item.isGoalAchieved == 1 || (target > 0 && current >= target)

                ResolvedGoalInfo(
                    iconRes = R.drawable.bp_p,
                    progress = if (achieved && progress < 100) 100 else progress,
                    valueText = "$current / $target",
                    isAchieved = achieved
                )
            }

            // 7. Fallback / Generic Goal
            else -> {
                val target = item.targetValue?.toIntOrNull() ?: 1
                val current = item.vitalValue.roundToInt()
                val progress = if (target > 0) ((current.toDouble() / target.toDouble()) * 100).roundToInt().coerceIn(0, 100) else 0
                val achieved = item.isGoalAchieved == 1 || (target > 0 && current >= target)

                ResolvedGoalInfo(
                    iconRes = R.drawable.medicine,
                    progress = if (achieved && progress < 100) 100 else progress,
                    valueText = "$current / $target",
                    isAchieved = achieved
                )
            }
        }
    }

    private fun bindDailyChecklistSummary(list: List<DailyCheckItem>) {
        val totalGoals = list.size
        val achievedGoals = list.count { item ->
            val info = resolveGoalInfo(item)
            info.isAchieved
        }
        val percentage = if (totalGoals > 0) ((achievedGoals.toDouble() / totalGoals) * 100).toInt() else 0

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

            val info = resolveGoalInfo(item)
            if (info.isAchieved) {
                icon.setImageResource(R.drawable.check_green)
                label.setTextColor(ContextCompat.getColor(requireContext(), R.color.themeTextColorBW))
            } else {
                icon.setImageResource(R.drawable.check_grey)
                label.setTextColor(ContextCompat.getColor(requireContext(), R.color.greyText))
            }

            binding.healthGoalAchived.goalsContainer.addView(view)
        }
    }

    private fun getProgressColor(progress: Int): Int {
        return when {
            progress <= 10 -> Color.parseColor("#FF3737") // Red
            progress <= 30 -> Color.parseColor("#FEA33C") // Orange
            progress <= 60 -> Color.parseColor("#1281FD") // Blue
            else -> Color.parseColor("#00C67A") // Green
        }
    }

    private fun bindDailyChecklistProgress(list: List<DailyCheckItem>) {
        binding.checklistContainer.removeAllViews()

        val darkTextColor = ContextCompat.getColor(requireContext(), R.color.themeTextColorBW)

        list.forEach { item ->
            val itemBinding = DailyChecklistWedgetBinding.inflate(
                layoutInflater,
                binding.checklistContainer,
                false
            )

            val info = resolveGoalInfo(item)
            val labelText = "${item.goalName.orEmpty()} ${info.progress}%"

            // Base Layer (Unfilled: Dark/Black text & icon)
            itemBinding.tvStepsLabel.text = labelText
            itemBinding.tvStepsLabel.setTextColor(darkTextColor)
            itemBinding.ivStepsIcon.setImageResource(info.iconRes)
            itemBinding.ivStepsIcon.setColorFilter(darkTextColor)

            // Filled Layer (Masked: White text & icon)
            itemBinding.tvStepsLabelFilled.text = labelText
            itemBinding.tvStepsLabelFilled.setTextColor(Color.WHITE)
            itemBinding.ivStepsIconFilled.setImageResource(info.iconRes)
            itemBinding.ivStepsIconFilled.setColorFilter(Color.WHITE)

            // Right-side Value text
            itemBinding.tvStepsValue.text = info.valueText

            // Progress pill color
            val progressColor = getProgressColor(info.progress)
            itemBinding.filledClipLayout.backgroundTintList = ColorStateList.valueOf(progressColor)

            // Masking & Clipping logic
            if (info.progress > 0) {
                itemBinding.filledClipLayout.visibility = View.VISIBLE
                itemBinding.progressContainer.post {
                    val totalWidth = itemBinding.progressContainer.width
                    if (totalWidth > 0) {
                        val filledWidth = (totalWidth * (info.progress / 100f)).roundToInt()

                        // Set the clipping frame to exact filled width
                        val clipParams = itemBinding.filledClipLayout.layoutParams
                        clipParams.width = filledWidth
                        itemBinding.filledClipLayout.layoutParams = clipParams

                        // Keep inner content layout pinned to total width so text & icon position matches base layer
                        val contentParams = itemBinding.filledContentLayout.layoutParams
                        contentParams.width = totalWidth
                        itemBinding.filledContentLayout.layoutParams = contentParams
                    }
                }
            } else {
                itemBinding.filledClipLayout.visibility = View.GONE
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

    private fun animatePageLoad() = DashboardAnimationHelper.animatePageLoad(binding)

    private fun updateHydrationTitle() {
        viewModel.lastDrinkInfo.observe(viewLifecycleOwner) { lastDrinkInfo ->
            binding.hydrationCardId.tvHydrationTitle.text =
                getString(R.string.hydration_due, lastDrinkInfo)
        }
    }

    private fun updateProgress(unit: String) {
        val goalEntry = PrefsManager().getEmployeeGoals().find { it.goalId == 13 }
        viewModel.totalQuantity.observe(viewLifecycleOwner) { totalValue ->
            val goal = goalEntry?.targetValue ?: 0
            val remaining = goal - totalValue
            binding.hydrationCardId.tvHydrationProgress.text =
                if (totalValue < remaining) {
                    getString(R.string.hydration_consumed_remaining, totalValue, remaining * 1000)
                } else {
                    getString(R.string.hydration_consumed_target, totalValue, goal * 1000)
                }
        }
        binding.hydrationCardId.tvHydrationProgress.setTextColor("#808C9A".toColorInt())
    }


    private fun initializeSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener { performRefresh() }
    }

    private fun performRefresh() {
        refreshDashboardData()
        // Stop the spinner once loading finishes; use observeForever + manual remove to avoid
        // stacking observers on repeated swipes
        val observer = object : androidx.lifecycle.Observer<Boolean> {
            override fun onChanged(isLoading: Boolean) {
                if (!isLoading) {
                    binding.swipeRefreshLayout.isRefreshing = false
                    viewModel.loading.removeObserver(this)
                }
            }
        }
        viewModel.loading.observe(viewLifecycleOwner, observer)
    }

    private fun refreshDashboardData() {
        viewModel.getVitals()
        viewModel.getMoodByPid()
        viewModel.getAllEnergyTankMaster()
        viewModel.fetchManualFluidIntake(uhid = PrefsManager().getPatient()?.empId.toString())
        challengesViewModel.getJoinedChallenge()
        pillsViewModel.getAllPatientMedication()
    }



    private fun setupMedicineTab() {
        tabMedicineAdapter = TabMedicineAdapter(mutableListOf()) { selectedMedicine ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                pillsViewModel.insertPatientMedication(selectedMedicine)
            }
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = tabMedicineAdapter
        binding.medicineTitleID.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_medicationFragment)
        }
        pillsViewModel.pillList.observe(viewLifecycleOwner) { list ->
            tabMedicineAdapter.updateList(list)
        }
    }

    private fun showPopup() = DashboardAnimationHelper.showPopup(binding)

    private fun hidePopup() = DashboardAnimationHelper.hidePopup(binding)



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
                        binding.challengesTab.visibility=View.GONE
                        binding.medicineTitleID.visibility = View.GONE
                        binding.recyclerView.visibility = View.GONE
                        binding.healthSnaps.visibility = View.GONE

                    }

                    1 -> {
                        binding.homeId.visibility = View.GONE
                        binding.challengedId.visibility = View.GONE
                        binding.activeChalleTextId.visibility = View.GONE
                        binding.challengesTab.visibility=View.GONE
                        binding.medicineTitleID.visibility = View.GONE
                        binding.recyclerView.visibility = View.GONE
                        binding.healthSnaps.visibility = View.VISIBLE

                    }

                    2 -> {
                        binding.recyclerView.visibility =
                            if (tabMedicineAdapter.itemCount > 0) View.VISIBLE else View.GONE
                        binding.medicineTitleID.visibility = View.VISIBLE
                        binding.challengesTab.visibility = View.GONE
                        binding.homeId.visibility = View.GONE
                        binding.challengedId.visibility = View.GONE
                        binding.activeChalleTextId.visibility = View.GONE
                        binding.healthSnaps.visibility = View.GONE
                    }

                    3 -> {
                        binding.homeId.visibility = View.GONE
                        binding.challengedId.visibility = View.GONE
                        binding.activeChalleTextId.visibility = View.GONE
                        binding.challengesTab.visibility=View.VISIBLE
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

