package com.critetiontech.ctvitalio.UI.fragments

import PrefsManager
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentEnergyTankBinding
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.utils.ToastUtils
import com.critetiontech.ctvitalio.viewmodel.EnergyTankViewModel
import androidx.core.graphics.toColorInt

class EnergyTank : Fragment() {

    private lateinit var binding: FragmentEnergyTankBinding
    private var energyLevel = 0 // start at 0%
    private lateinit var gestureDetector: GestureDetector
    private lateinit var viewModel: EnergyTankViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEnergyTankBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[EnergyTankViewModel::class.java]

        // Set initial alpha to 0 for animations
        setInitialVisibility()

        binding.lightningIcon.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true // must return true to consume event
        }

        var greetings = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ToastUtils.getSimpleGreeting()
        } else {
            "Good Morning"
        }
        binding.greetingText.text = greetings

        val text = "How's your energy tank this morning?"
        val spannable = SpannableString(text)

        // Change only the word "energy tank" to yellow
        val start = text.indexOf("energy tank")
        val end = start + "energy tank".length
        spannable.setSpan(
            ForegroundColorSpan("#DCF629".toColorInt()),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val typefaceStatus = ResourcesCompat.getFont(requireContext(), R.font.oswald_bold)
        binding.statusText.typeface = typefaceStatus

        binding.questionText.text = spannable
        val typeface = ResourcesCompat.getFont(requireActivity(), R.font.source_serif_pro)
        binding.questionText.setTypeface(typeface, Typeface.BOLD)

        gestureDetector = GestureDetector(requireContext(), GestureListener())
        binding.lightningIcon.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }

        binding.userName.text = PrefsManager().getPatient()?.patientName ?: ""
        Glide.with(MyApplication.appContext)
            .load("http://182.156.200.177:5082/" + PrefsManager().getPatient()?.imageURL.toString())
            .placeholder(R.drawable.baseline_person_24)
            .circleCrop()
            .into(binding.userAvatar)

        binding.actionButton.setOnClickListener() {
            viewModel.insertEnergyTankMaster(
                context = requireContext(),
                status = binding.statusText.text.toString(),
                energyPercentage = binding.percentageText.text.toString().replace("%", "")
            )
            findNavController().popBackStack()
        }

        binding.ivBack.setOnClickListener() {
            findNavController().popBackStack()
        }

        updateEnergyUI(energyLevel)

        // Start animations after a slight delay
        binding.root.postDelayed({
            startEntryAnimations()
        }, 100)
    }

    /**
     * Set initial visibility for animation
     */
    private fun setInitialVisibility() {
        binding.apply {
            ivBack.alpha = 0f
            //tvSkip.alpha = 0f
            userAvatar.alpha = 0f
            userAvatar.scaleX = 0.5f
            userAvatar.scaleY = 0.5f
            greetingText.alpha = 0f
            userName.alpha = 0f
            questionText.alpha = 0f
            questionText.translationY = 30f
            lightningContainer.alpha = 0f
            lightningContainer.scaleX = 0.8f
            lightningContainer.scaleY = 0.8f
            actionButton.alpha = 0f
            actionButton.translationY = 50f
        }
    }

    /**
     * Start smooth entry animations
     */
    private fun startEntryAnimations() {
        binding.apply {
            // Animate back button and skip
            animateFadeIn(ivBack, 0, 300)
           // animateFadeIn(tvSkip, 0, 300)

            // Animate avatar with scale
            animateAvatarEntry(userAvatar, 200)

            // Animate greeting and name
            animateFadeIn(greetingText, 400, 400)
            animateFadeIn(userName, 500, 400)

            // Animate question text with slide up
            animateSlideUpFadeIn(questionText, 700, 500)

            // Animate lightning container with scale
            animateLightningEntry(lightningContainer, 900)

            // Animate button with slide up
            animateSlideUpFadeIn(actionButton, 1100, 500)
        }
    }

    /**
     * Simple fade in animation
     */
    private fun animateFadeIn(view: View, startDelay: Long, duration: Long) {
        ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
            this.startDelay = startDelay
            this.duration = duration
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    /**
     * Avatar entry with scale and fade
     */
    private fun animateAvatarEntry(view: View, startDelay: Long) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.5f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.5f, 1f)
        val alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            this.startDelay = startDelay
            duration = 600
            interpolator = OvershootInterpolator(1.5f)
            start()
        }
    }

    /**
     * Slide up with fade in
     */
    private fun animateSlideUpFadeIn(view: View, startDelay: Long, duration: Long) {
        val translateY = ObjectAnimator.ofFloat(view, "translationY", view.translationY, 0f)
        val alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)

        AnimatorSet().apply {
            playTogether(translateY, alpha)
            this.startDelay = startDelay
            this.duration = duration
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    /**
     * Lightning container entry with scale
     */
    private fun animateLightningEntry(view: View, startDelay: Long) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.8f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.8f, 1f)
        val alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            this.startDelay = startDelay
            duration = 700
            interpolator = OvershootInterpolator(0.8f)
            start()
        }
    }

    /**
     * Update UI based on energy level
     */
    private fun updateEnergyUI(level: Int) {
        animatePercentage(binding.percentageText.text.toString().replace("%", "").toIntOrNull() ?: 0, level)

        if (level == 0) {
            binding.dragToFill.visibility = View.VISIBLE
        } else {
            binding.dragToFill.visibility = View.GONE
        }

        val colorRes = when {
            level == 0 -> {
                binding.statusText.text = "UPDATE\nYOUR\nENERGY\nLEVEL!"
                android.R.color.background_light
            }
            level <= 25 -> {
                binding.statusText.text = "RUNNING\nLOW"
                android.R.color.holo_red_light
            }
            level in 26..60 -> {
                binding.statusText.text = "NEED\nA BOOST"
                android.R.color.holo_orange_light
            }
            level in 61..90 -> {
                binding.statusText.text = "PRETTY\nGOOD"
                android.R.color.holo_blue_light
            }
            else -> {
                binding.statusText.text = "FULLY\nCHARGED"
                android.R.color.holo_green_light
            }
        }

        val color = ContextCompat.getColor(requireContext(), colorRes)
        binding.statusText.setTextColor(color)

        // 🔥 fill lightning
        setLightningFillLevel(level, color)
    }

    private fun animatePercentage(start: Int, end: Int) {
        ValueAnimator.ofInt(start, end).apply {
            duration = 233
            addUpdateListener { animator ->
                binding.percentageText.text = "${animator.animatedValue}%"
            }
            start()
        }
    }

    private fun setLightningFillLevel(levelPercent: Int, tintColor: Int) {
        val drawable = binding.lightningIcon.drawable
        if (drawable is LayerDrawable) {
            val clip = drawable.findDrawableByLayerId(R.id.item_clip)
            clip.level = levelPercent.coerceIn(0, 100) * 100
            clip.setTint(tintColor)
        }
        binding.lightningIcon.invalidate()
    }

    inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true // very important to receive scroll events
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            // distanceY is how much finger moved **since last event**
            if (distanceY > 0) {
                // finger moved **up** → increase energy
                energyLevel = (energyLevel + 1).coerceAtMost(100)
            } else if (distanceY < 0) {
                // finger moved **down** → decrease energy
                energyLevel = (energyLevel - 1).coerceAtLeast(0)
            }
            updateEnergyUI(energyLevel)
            return true
        }
    }
}