package com.critetiontech.ctvitalio.UI.fragments

import MoodAdapter
import MoodData
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.transition.TransitionInflater
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentMoodBinding
import kotlin.math.abs

class MoodFragment : Fragment() {
    private lateinit var binding: FragmentMoodBinding
    private var currentMoodIndex = 0
    private lateinit var gestureDetector: GestureDetector
    private lateinit var moodAdapter: MoodAdapter

    private val moods = listOf(
        MoodData("Spectacular", "#FFA4BA", R.drawable.spectulor_mood,  "#611829"),
        MoodData("Upset", "#88A7FF",  R.drawable.upset_mood,  "#2A4089"),
        MoodData("Stressed", "#FF9459",  R.drawable.stressed_mood, "#782E04"),
        MoodData("Happy", "#9ABDFF",  R.drawable.happy_mood,"#505D87"),
        MoodData("Good", "#F9C825",  R.drawable.good_mood, "#664F00"),
        MoodData("Sad",   "#7DE7EE",  R.drawable.sad_mood,  "#3A7478")

    )


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = MoodAdapter(moods)
        binding.moodEmoji.adapter = adapter

        // Make it behave like pager (one item per swipe)
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.moodEmoji)
        sharedElementEnterTransition =
            TransitionInflater.from(requireContext())
                .inflateTransition(R.transition.change_image_transform)
        sharedElementReturnTransition =
            TransitionInflater.from(requireContext())
                .inflateTransition(R.transition.change_image_transform)
        // Listen for page change


        binding.moodEmoji.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        val mood = moods[position]



                        // Animate background
                        val currentColor = (binding.rootMoodLayout.background as? ColorDrawable)?.color ?: Color.WHITE
                        ValueAnimator.ofObject(ArgbEvaluator(), currentColor, Color.parseColor(mood.color)).apply {
                            duration = 500
                            addUpdateListener { animator ->
                                binding.rootMoodLayout.setBackgroundColor(animator.animatedValue as Int)
                            }
                            start()
                        }
                    }
                }
            }
        })
    }

}
