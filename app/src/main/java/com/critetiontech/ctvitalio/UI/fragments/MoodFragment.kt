package com.critetiontech.ctvitalio.UI.fragments

import MoodData
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.GestureDetector
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.res.ResourcesCompat
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentMoodBinding
import kotlin.math.abs
import androidx.core.graphics.toColorInt


class MoodFragment : Fragment() {
    private lateinit var binding: FragmentMoodBinding
    private var currentMoodIndex = 0
    private lateinit var gestureDetector: GestureDetector

    private val moods = listOf(
        MoodData("Spectacular", "#FFA4BA", "🤩"),
        MoodData("Upset", "#88A7FF", "😰"),
        MoodData("Stressed", "#FF9459", "😤"),
        MoodData("Happy", "#9ABDFF", "😄"),
        MoodData("Good", "#FFC107", "😊"),
        MoodData("Sad",   "#7DE7EE", "😢")

    )



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMoodBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGestureDetector()
        setupUI()
        updateMoodDisplay()
    }

    private fun setupGestureDetector() {
        gestureDetector = GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false

                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y

                if (abs(diffX) > abs(diffY)) {
                    if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            // Swipe right - previous mood
                            onSwipeRight()
                        } else {
                            // Swipe left - next mood
                            onSwipeLeft()
                        }
                        return true
                    }
                }
                return false
            }
        })

        // Set touch listener on the card container
        binding.cardContainer.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun setupUI() {
        binding.apply {
            // Set user info
            userName.text = "Katherine Prokes"

            val text = "How are you feeling today?"
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

            val typeface = ResourcesCompat.getFont(requireActivity(), R.font.source_serif_pro)
            binding.questionText.setTypeface(typeface, Typeface.BOLD)
            binding.questionText.text = spannable
            // Setup mood indicators
            setupMoodIndicators()

            // Setup button click
            selectMoodButton.setOnClickListener {
                onMoodSelected(moods[currentMoodIndex])
            }

            // Setup navigation arrows (optional)
            leftArrow.setOnClickListener { onSwipeRight() }
            rightArrow.setOnClickListener { onSwipeLeft() }
        }
    }

    private fun setupMoodIndicators() {
        binding.indicatorContainer.removeAllViews()

        moods.forEachIndexed { index, _ ->
            val indicator = View(requireContext()).apply {
                layoutParams = ViewGroup.MarginLayoutParams(24, 8).apply {
                    marginEnd = 8
                }
                setBackgroundResource(R.drawable.indicator_background)
                isSelected = index == currentMoodIndex
            }
            binding.indicatorContainer.addView(indicator)
        }
    }

    private fun updateMoodDisplay() {
        val currentMood = moods[currentMoodIndex]

        binding.apply {
            // Animate card color change
            cardContainer.animate()
                .alpha(0.8f)
                .setDuration(150)
                .withEndAction {
                    cardContainer.setCardBackgroundColor(currentMood.color.toColorInt())
                    moodEmoji.text = currentMood.emoji
                    moodTitle.text = currentMood.name
                    cardContainer.animate()
                        .alpha(1f)
                        .setDuration(150)
                        .start()
                }
                .start()

            // Update indicators
            updateIndicators()

            // Update arrow visibility
            leftArrow.alpha = if (currentMoodIndex > 0) 1f else 0.3f
            rightArrow.alpha = if (currentMoodIndex < moods.size - 1) 1f else 0.3f
        }
    }

    private fun updateIndicators() {
        for (i in 0 until binding.indicatorContainer.childCount) {
            val indicator = binding.indicatorContainer.getChildAt(i)
            indicator.isSelected = i == currentMoodIndex
        }
    }

    private fun onSwipeLeft() {
        if (currentMoodIndex < moods.size - 1) {
            currentMoodIndex++
            animateSlideTransition(isNext = true)
        }
    }

    private fun onSwipeRight() {
        if (currentMoodIndex > 0) {
            currentMoodIndex--
            animateSlideTransition(isNext = false)
        }
    }

    private fun animateSlideTransition(isNext: Boolean) {
        val slideDistance = if (isNext) -600f else 300f

        binding.cardContainer.animate()
            .translationX(slideDistance)
            .alpha(0.7f)
            .setDuration(200)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                updateMoodDisplay()
                binding.cardContainer.translationX = 0f // ✅ correct reset
                binding.cardContainer.animate()
                    .translationX(0f)
                    .alpha(1f)
                    .setDuration(200)
                    .start()
            }
            .start()
    }


    private fun onMoodSelected(mood: MoodData) {
        // Handle mood selection
        // You can implement your logic here:
        // - Save to database
        // - Navigate to next screen
        // - Show confirmation

        // Example: Show a simple feedback
        binding.selectMoodButton.text = "Selected: ${mood.name}"
        binding.selectMoodButton.isEnabled = false

        // Re-enable button after 2 seconds
        binding.selectMoodButton.postDelayed({
            binding.selectMoodButton.text = "Select mood"
            binding.selectMoodButton.isEnabled = true
        }, 2000)
    }

}