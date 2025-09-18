package com.critetiontech.ctvitalio.UI.fragments

import Mood
import MoodAdapter
import MoodData
import PrefsManager
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentMoodBinding
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.viewmodel.MoodViewModel

class MoodFragment : Fragment() {
    private lateinit var binding: FragmentMoodBinding
    private var currentMoodIndex = 0
    private lateinit var gestureDetector: GestureDetector
    private lateinit var moodAdapter: MoodAdapter

    private lateinit var viewModel: MoodViewModel

    private val moods = listOf(
        MoodData(5,"Spectacular", "#FFA4BA", R.drawable.spectulor_mood,  "#611829"),
        MoodData(6,"Upset", "#88A7FF",  R.drawable.upset_mood,  "#2A4089"),
        MoodData(1, "Stressed", "#FF9459",  R.drawable.stressed_mood, "#782E04"),
        MoodData(2,"Happy", "#9ABDFF",  R.drawable.happy_mood,"#505D87"),
        MoodData(4,"Good", "#F9C825",  R.drawable.good_mood, "#664F00"),
        MoodData(3,"Sad",   "#7DE7EE",  R.drawable.sad_mood,  "#3A7478"),
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

        viewModel = ViewModelProvider(this)[MoodViewModel::class.java]



        viewModel.onMoodClicked("5")
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
        viewModel.getMoodByPid()
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.moodsLiveData.observe(viewLifecycleOwner) { moodsFromApi ->
            val moodDataList = moodsFromApi.map { apiMood ->
                val drawableRes = moods.find { it.id == apiMood.id }?.emojiRes
                val color = moods.find { it.id == apiMood.id }?.color

                    Mood (
                        id = apiMood.id,
                        label = apiMood.label,
                        color =color?: "#FFA4BA",
                        emojiRes = drawableRes ?: R.drawable.spectulor_mood,
                        description = apiMood.description,
                    )

            }

            val adapter = MoodAdapter(moodDataList)
            binding.moodEmoji.adapter = adapter
        }
        binding.userName2.text =   PrefsManager().getPatient()?.patientName ?: ""
        Glide.with(MyApplication.appContext)
            .load("http://182.156.200.177:5082/"+PrefsManager().getPatient()?.imageURL.toString())
            .placeholder(R.drawable.baseline_person_24)
            .circleCrop()
            .into(binding.userAvatar)
        binding.selectMoodButton.setOnClickListener{
            viewModel.insertMood(requireContext())
        }
        binding.moodEmoji.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        val mood = viewModel.moodsLiveData.value?.get(position)

                        viewModel.onMoodClicked(mood?.id.toString())
                        val color = moods.find { it.id.toString()  == mood?.id.toString() }?.color


                        // Animate background
                        val currentColor = (binding.rootMoodLayout.background as? ColorDrawable)?.color ?: Color.WHITE
                        ValueAnimator.ofObject(ArgbEvaluator(), currentColor, Color.parseColor(color)).apply {
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
