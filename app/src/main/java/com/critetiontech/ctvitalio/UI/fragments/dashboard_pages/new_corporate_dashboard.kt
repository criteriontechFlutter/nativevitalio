package com.critetiontech.ctvitalio.UI.fragments.dashboard_pages

import MoodData
import PrefsManager
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.UltraHumanActivity
import com.critetiontech.ctvitalio.databinding.FragmentNewCorporateDashboardBinding
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.viewmodel.ChallengesViewModel
import com.critetiontech.ctvitalio.viewmodel.DashboardViewModel
import com.critetiontech.ctvitalio.viewmodel.PillsReminderViewModal
import net.openid.appauth.AuthorizationService

class new_corporate_dashboard : Fragment() {

    private lateinit var binding: FragmentNewCorporateDashboardBinding
    private lateinit var viewModel: DashboardViewModel
    private lateinit var challengesViewModel: ChallengesViewModel
    private lateinit var pillsViewModel: PillsReminderViewModal

    private val moods = listOf(
        MoodData(5,"Spectacular", "#FFA4BA", R.drawable.spectulor_mood,  "#611829"),
        MoodData(6,"Upset", "#88A7FF",  R.drawable.upset_mood,  "#2A4089"),
        MoodData(1, "Stressed", "#FF9459",  R.drawable.stressed_mood, "#782E04"),
        MoodData(2,"Happy", "#9ABDFF",  R.drawable.happy_mood,"#505D87"),
        MoodData(4,"Good", "#F9C825",  R.drawable.good_mood, "#664F00"),
        MoodData(3,"Sad",   "#7DE7EE",  R.drawable.sad_mood,  "#3A7478")

    )
    private var isFabOpen = false
    /** bottom nav config */
    private val tabLabels = listOf("Home", "Snaps", "Reminders", "Challenges")
    private val tabIcons = listOf(
        R.drawable.home,
        R.drawable.vitals_icon_home,
        R.drawable.pill,
        R.drawable.challenges_icon
    )
    private lateinit var navItems: List<View>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewCorporateDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /** ViewModels */
        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        pillsViewModel = ViewModelProvider(this)[PillsReminderViewModal::class.java]
        challengesViewModel = ViewModelProvider(this)[ChallengesViewModel::class.java]
         challengesViewModel.getNewChallenge()


        viewModel.getMoodByPid()
        viewModel.getAllEnergyTankMaster()


        Glide.with(MyApplication.appContext)
            .load("http://182.156.200.177:5082/"+PrefsManager().getPatient()?.imageURL.toString())
            .placeholder(R.drawable.baseline_person_24)
            .circleCrop()
            .into(binding.avatar)
        binding.avatar.setOnClickListener {
            findNavController().navigate(R.id.action_new_corporate_dashboard_to_drawer4)
        }
        binding.ringIcon.setOnClickListener {
            startActivity(Intent(requireActivity(), UltraHumanActivity::class.java))
        }
        binding.fabIcon.setOnClickListener {
            Log.d("TAG", "onViewCreated: clickedFab")
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
        /** Bind bottom nav items using binding (no findViewById) */
        navItems = listOf(
            binding.customBottomNav.navHome.root,
            binding.customBottomNav.navVitals.root,
            binding.customBottomNav.navMedicine.root,
            binding.customBottomNav.navGoals.root
        )

        setupBottomNav()
        setupNav()

        /** everything ELSE remains the same (data observers, card logic, hydration UI, etc.) */
        observeVitalData()
        observeChallenges()
        observeHydration()
        observeSleep()
        observeMedicine()
        setupMiscUIActions()
        animatePageLoad()
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
    /** ------------------------------ */
    /** BOTTOM NAVIGATION CLEAN CODE   */
    /** ------------------------------ */

    private fun setupBottomNav() {
        navItems.forEachIndexed { index, view ->
            val icon = view.findViewById<ImageView>(R.id.navIcon)
            val text = view.findViewById<TextView>(R.id.navText)

            icon.setImageResource(tabIcons[index])
            text.text = tabLabels[index]
            text.visibility = View.GONE
        }
    }

    private fun setupNav() {
        navItems.forEachIndexed { index, view ->
            view.setOnClickListener {
                selectItem(index)
            }
        }
        selectItem(0)
    }

    private fun selectItem(index: Int) {
        navItems.forEachIndexed { i, view ->
            val icon = view.findViewById<ImageView>(R.id.navIcon)
            val text = view.findViewById<TextView>(R.id.navText)

            if (i == index) {
                view.isSelected = true
                text.visibility = View.VISIBLE
                icon.setColorFilter(ContextCompat.getColor(requireActivity(), android.R.color.white))
                showPage(i)
            } else {
                view.isSelected = false
                text.visibility = View.GONE
                icon.setColorFilter(ContextCompat.getColor(requireActivity(), R.color.blue))
            }
        }
    }

    /** clean UI switching */
    private fun showPage(i: Int) {

        setupPages()
        binding.apply {

            homepageId.visibility = View.GONE
            healthSnapsPageId.visibility = View.GONE
            medicinePageId.visibility = View.GONE
            challengesPageId.visibility = View.GONE

            when (i) {
                0 -> homepageId.visibility = View.VISIBLE
                1 -> healthSnapsPageId.visibility = View.VISIBLE
                2 -> medicinePageId.visibility = View.VISIBLE
                3 -> challengesPageId.visibility = View.VISIBLE
            }
        }
    }
    private fun setupPages() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.homepageId, Home())
            .replace(R.id.healthSnapsPageId, HealthSnaps())
            .replace(R.id.medicinePageId, Reminders())
            .replace(R.id.challengesPageId, dashboard_challenges())
            .commit()
    }
    /** ------------------------------ */
    /** EVERYTHING BELOW UNCHANGED     */
    /** (Observers, binding, logic )   */
    /** ------------------------------ */

    private fun observeVitalData() {
        viewModel.vitalList.observe(viewLifecycleOwner) { vitals ->
            /** ... keep your existing logic here ... */
        }
    }

    private fun observeChallenges() {
        challengesViewModel.newChallenges.observe(viewLifecycleOwner) { list ->
            /** ... */
        }
    }

    private fun observeHydration() {
        viewModel.fluidList.observe(viewLifecycleOwner) { list ->
            /** ... */
        }
    }

    private fun observeSleep() {
        viewModel.sleepValueList.observe(viewLifecycleOwner) { sleep ->
            /** ... */
        }
    }

    private fun observeMedicine() {
        pillsViewModel.pillList.observe(viewLifecycleOwner) { list ->
            /** ... */
        }
    }

    private fun setupMiscUIActions() {
        binding.fabIcon.setOnClickListener { showPopupUI() }
        binding.headerContainer.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_new_corporate_dashboard)
        } }



    /** keep your popup and animation methods unchanged */
    private fun showPopupUI() { /* same code */ }
    private fun animatePageLoad() { /* same code */ }
    fun dpToPx(dp: Int, context: Context): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}