package com.critetiontech.ctvitalio.UI.fragments.dashboard_pages

import PrefsManager
import SleepVital
import Vital
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.ColorUtils
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.UltraHumanActivity
import com.critetiontech.ctvitalio.adapter.DailyTip
import com.critetiontech.ctvitalio.adapter.DailyTipAdapter
import com.critetiontech.ctvitalio.adapter.IndicatorAdapter
import com.critetiontech.ctvitalio.adapter.MedicationReminderAdapter
import com.critetiontech.ctvitalio.adapter.NewChallengedAdapter
import com.critetiontech.ctvitalio.databinding.DailyChecklistWedgetBinding
import com.critetiontech.ctvitalio.databinding.FragmentHomeBinding
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.utils.showRetrySnackbar
import com.critetiontech.ctvitalio.viewmodel.ChallengesViewModel
import com.critetiontech.ctvitalio.viewmodel.DashboardViewModel
import com.critetiontech.ctvitalio.viewmodel.PillsReminderViewModal
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Home : Fragment() {


    /** ---------------------- */
    /** Binding + ViewModels   */
    /** ---------------------- */
    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: DashboardViewModel
    private lateinit var pillsViewModel: PillsReminderViewModal
    private lateinit var challengesViewModel: ChallengesViewModel

    /** ---------------------- */
    /** Adapters               */
    /** ---------------------- */
    private lateinit var dailyTipAdapter: DailyTipAdapter
    private lateinit var indicatorAdapter: IndicatorAdapter
    private var snackbar: Snackbar? = null


    /** ---------------------- */
    /** Inflate XML            */
    /** ---------------------- */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }


    /** ---------------------- */
    /** Lifecycle              */
    /** ---------------------- */
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewModels()
        initNavigationClicks()
        setupTipsSlider()
        setupMedicationList()

        observeVitals()
        observeVitalInsights()
        observeEnergy()
        observeNetworkStatus()
        observeChallenges()

        initHydrationControls()
        updateHydrationTitle()
        updateProgress()

        loadSummaryChecklist()
    }


    /** ---------------------- */
    /** SUMMARY CHECKLIST      */
    /** ---------------------- */
    private fun loadSummaryChecklist() {

        binding.checklistContainer.removeAllViews()

        viewModel.vitalList.observe(viewLifecycleOwner) { vitalList ->

            val summaryVital = vitalList.find { it.vitalName == "Summary" }
            val json = summaryVital?.vmValueText ?: return@observe
            if (json.isEmpty()) return@observe

            val listType = object : TypeToken<List<SleepVital>>() {}.type
            val sleepList: List<SleepVital> = Gson().fromJson(json, listType)

            sleepList.forEach { item ->

                val itemBinding = DailyChecklistWedgetBinding.inflate(
                    layoutInflater, binding.checklistContainer, false
                )

                itemBinding.progressSteps.progress = item.Score.toInt()
                itemBinding.tvStepsLabel.text = item.Title
                itemBinding.tvStepsValue.text = item.Score.toInt().toString()

                when (item.State) {
                    "good" -> itemBinding.ivStepsIcon.setColorFilter(Color.GREEN)
                    "optimal" -> itemBinding.ivStepsIcon.setColorFilter(Color.BLUE)
                    "warning" -> itemBinding.ivStepsIcon.setColorFilter(Color.RED)
                }

                binding.checklistContainer.addView(itemBinding.root)
            }
        }
    }


    /** ---------------------- */
    /** MEDICATION LIST        */
    /** ---------------------- */
    private fun setupMedicationList() {
        pillsViewModel.pillList.observe(viewLifecycleOwner) { list ->

            val adapter =
                MedicationReminderAdapter(list.toMutableList()) { /*TODO*/ }

            binding.medicationsId.apply {
                layoutManager = LinearLayoutManager(requireContext())
                this.adapter = adapter
            }
        }

        pillsViewModel.getAllPatientMedication()
    }


    /** ---------------------- */
    /** TIPS SLIDER            */
    /** ---------------------- */
    private fun setupTipsSlider() {

        val tips = listOf(
            DailyTip(
                R.drawable.start_session,
                "Stress slightly high!",
                "A 3-min breathing break can help you reset.",
                "Start Session"
            ),
            DailyTip(
                R.drawable.log_glucose,
                "No glucose reading yet today",
                "Logging helps track stability.",
                "Log Glucose"
            ),
            DailyTip(
                R.drawable.log_glucose,
                "Haven't checked your BP today!",
                "A quick reading keeps your heart in check.",
                "Log BP"
            ),
            DailyTip(
                R.drawable.log_glucose,
                "Hydration dropped since yesterday",
                "A refill can maintain energy.",
                "Add Intake"
            ),
        )

        dailyTipAdapter = DailyTipAdapter(tips) { tip ->
            Toast.makeText(requireContext(), "Clicked: ${tip.title}", Toast.LENGTH_SHORT).show()
        }

        binding.recyclerSlider.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = dailyTipAdapter
            PagerSnapHelper().attachToRecyclerView(this)
        }

        indicatorAdapter = IndicatorAdapter(tips.size)
        binding.layoutIndicators.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = indicatorAdapter
        }

        binding.recyclerSlider.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(rv: RecyclerView, state: Int) {
                if (state == RecyclerView.SCROLL_STATE_IDLE) {
                    val lm = rv.layoutManager as LinearLayoutManager
                    indicatorAdapter.updateSelectedPosition(lm.findFirstVisibleItemPosition())
                }
            }
        })
    }


    /** ---------------------- */
    /** VIEWMODEL INIT         */
    /** ---------------------- */
    private fun initViewModels() {
        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        pillsViewModel = ViewModelProvider(this)[PillsReminderViewModal::class.java]
        challengesViewModel = ViewModelProvider(this)[ChallengesViewModel::class.java]

        challengesViewModel.getNewChallenge()
        viewModel.getVitals()
        viewModel.getAllEnergyTankMaster()
        viewModel.getFoodIntake()
        viewModel.fetchManualFluidIntake(PrefsManager().getPatient()?.empId.toString())
    }


    /** ---------------------- */
    /** NAVIGATION CLICKS      */
    /** ---------------------- */
    private fun initNavigationClicks() {

        binding.myHealthCard.setOnClickListener {
            findNavController().navigate(R.id.action_new_corporate_dashboard_to_wellnessMetrics)
        }

        binding.goalCard.setOnClickListener {
            findNavController().navigate(R.id.action_new_corporate_dashboard_to_smartGoalFragment)
        }

        binding.energyCard.setOnClickListener {
            findNavController().navigate(R.id.action_new_corporate_dashboard_to_energyTank)
        }

        binding.sleepProgressIds.sleepContainerId.setOnClickListener {
            findNavController().navigate(R.id.action_new_corporate_dashboard_to_waterIntakeFragment)
        }

        binding.sleepProgressIds.SleepWelnessId.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_sleepDetails)
        }
    }


    /** ---------------------- */
    /** OBSERVE VITALS         */
    /** ---------------------- */
    private fun observeVitals() {

        viewModel.vitalList.observe(viewLifecycleOwner) { vitalList ->

            fun List<Vital>.findVital(name: String): Vital? =
                firstOrNull { it.vitalName.equals(name, ignoreCase = true) }

            fun Vital?.asText(): String =
                this?.vitalValue?.toInt()?.toString() ?: "--"

            binding.sleepProgressIds.apply {
                wellnessScoreNumber.text = vitalList.findVital("Wellness Score").asText()
                sleepValue.text = vitalList.findVital("Sleep").asText()
                stressValue.text = vitalList.findVital("StressScore").asText()
                movementValue.text = vitalList.findVital("MovementIndex").asText()
                recoveryValue.text = vitalList.findVital("RecoveryIndex").asText()
            }

        }
        viewModel.vitalInsights.observe(viewLifecycleOwner) { vitals ->
            val RecoveryIndex = vitals?.find { it.vitalID == 240 }
            val MovementIndex = vitals?.find { it.vitalID == 241 }
            val StressScore = vitals?.find { it.vitalID == 252 }
            setInsightText(binding.sleepProgressIds.sleepContainertextId, "",binding.sleepProgressIds.sleepContainerId)
            setInsightText(binding.sleepProgressIds.movementContainertextId, MovementIndex?.insight,binding.sleepProgressIds.movementContainerId)
            setInsightText(binding.sleepProgressIds.recoveryContainertextId, RecoveryIndex?.insight,binding.sleepProgressIds.recoveryContainerId)
            setInsightText(binding.sleepProgressIds.stressContainertextId, StressScore?.insight,binding.sleepProgressIds.stressContainerId)

    }
    }


    /** ---------------------- */
    /** VITAL INSIGHTS         */
    /** ---------------------- */
    private fun observeVitalInsights() {

        viewModel.vitalInsights.observe(viewLifecycleOwner) { vitals ->

            fun bindInsight(
                tv: TextView,
                container: LinearLayout,
                colorCode: String?,
                severity: String?,
                insight: String?
            ) {
                tv.text = severity ?: "--"

                val textColor = Color.parseColor(colorCode ?: "#EF4444")
                tv.setTextColor(textColor)

                val bgColor = ColorUtils.setAlphaComponent(
                    textColor,
                    (0.74 * 255).toInt()
                )

                container.setBackgroundColor(bgColor)
                container.visibility = if (insight.isNullOrEmpty()) View.GONE else View.VISIBLE
            }

            val recoveryVital = vitals?.find { it.vitalID == 240 }

            bindInsight(
                binding.sleepProgressIds.recoverystatusId,
                binding.sleepProgressIds.recoveryContainerId,
                recoveryVital?.colourCode,
                recoveryVital?.severityLevel,
                recoveryVital?.insight
            )
        }
    }


    /** ---------------------- */
    /** ENERGY OBSERVER        */
    /** ---------------------- */
    private fun observeEnergy() {
        viewModel.latestEnergy.observe(viewLifecycleOwner) { energy ->

            if (energy == null) {
                binding.energyTitle.text = "Your energy story awaits"
                binding.energySubtitle.text = "Log your energy for today ⚡"
                binding.energyImage.setImageResource(R.drawable.emtyp_energy)
                binding.energyid.setBackgroundResource(R.drawable.rounded_card_bg)
            } else {
                binding.energyTitle.text = "You're feeling $energy% energized today ⚡"
                binding.energySubtitle.text = "Stay positive and powerful!"
                binding.energyImage.setImageResource(R.drawable.ic_meditation)
                binding.energyid.setBackgroundResource(R.drawable.bg_energy_gradient)
            }
        }
    }


    /** ---------------------- */
    /** NETWORK OBSERVER       */
    /** ---------------------- */
    private fun observeNetworkStatus() {
        viewModel.isConnected.observe(viewLifecycleOwner) { connected ->
            if (!connected) showRetrySnackbar("") { viewModel.getVitals() }
        }
    }


    /** ---------------------- */
    /** CHALLENGES OBSERVER    */
    /** ---------------------- */
    private fun observeChallenges() {

        challengesViewModel.newChallenges.observe(viewLifecycleOwner) { list ->

            binding.newChallengedRecyclerView.apply {
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                adapter = NewChallengedAdapter(
                    list.toMutableList(),
                    { challenge -> challengesViewModel.joinChallenge(challenge.id.toString()) },
                    { challenge ->
                        val bundle = Bundle().apply { putSerializable("challenges", challenge) }
                        findNavController().navigate(
                            R.id.action_home2_to_challengeDetailsFragment,
                            bundle
                        )
                    }
                )
                PagerSnapHelper().attachToRecyclerView(this)
            }

            binding.activechalgesId.text = "Active Challenges (${list.size})"
            setupChallengeIndicators(list.size)
        }
    }


    /** ---------------------- */
    /** CHALLENGE INDICATORS   */
    /** ---------------------- */
    private fun setupChallengeIndicators(size: Int) {

        val adapter = IndicatorAdapter(size)

        binding.activechalgesDotId.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            this.adapter = adapter
        }

        try {
            PagerSnapHelper().attachToRecyclerView(binding.newChallengedRecyclerView)
        } catch (_: Exception) {}

        binding.newChallengedRecyclerView.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(rv: RecyclerView, state: Int) {
                    if (state == RecyclerView.SCROLL_STATE_IDLE) {
                        val pos =
                            (rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        adapter.updateSelectedPosition(pos)
                    }
                }
            }
        )
    }


    /** ---------------------- */
    /** HYDRATION CONTROLS     */
    /** ---------------------- */
    private fun initHydrationControls() {

        var amount = 200

        binding.hydrationCardId.btnPlus.setOnClickListener {
            amount += 50
            binding.hydrationCardId.tvAmount.text = "$amount ml"
        }

        binding.hydrationCardId.btnMinus.setOnClickListener {
            if (amount > 0) {
                amount -= 50
                binding.hydrationCardId.tvAmount.text = "$amount ml"
            }
        }

        binding.hydrationCardId.btnAddIntake.setOnClickListener {
            viewModel.fluidIntake(amount.toString())
        }
    }


    /** ---------------------- */
    /** HYDRATION TITLE        */
    /** ---------------------- */
    private fun updateHydrationTitle() {
        viewModel.lastDrinkInfo.observe(viewLifecycleOwner) { lastDrinkInfo ->
            binding.hydrationCardId.tvHydrationTitle.text =
                "Hydration due - $lastDrinkInfo"
        }
    }


    /** ---------------------- */
    /** HYDRATION PROGRESS     */
    /** ---------------------- */
    private fun updateProgress() {

        val goalEntry = PrefsManager().getEmployeeGoals().find { it.goalId == 13 }
        val goal = goalEntry?.targetValue?.times(1000) ?: 0

        viewModel.totalQuantity.observe(viewLifecycleOwner) { total ->

            val remaining = goal - total

            binding.hydrationCardId.tvHydrationProgress.text =
                if (remaining > 0)
                    "$total ml consumed — $remaining ml to go"
                else
                    "$total ml consumed — target reached $goal ml"
        }
    }


    /** ---------------------- */
    /** UI Helper              */
    /** ---------------------- */
    private fun setInsightText(view: TextView, insight: String?, container: LinearLayout) {
        container.visibility = if (insight.isNullOrBlank()) View.GONE else View.VISIBLE
        view.text = insight ?: ""
    }
}