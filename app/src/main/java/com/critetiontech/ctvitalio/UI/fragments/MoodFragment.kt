package com.critetiontech.ctvitalio.UI.fragments

import Mood
import MoodAdapter
import MoodData
import PrefsManager
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.BaseActivity
import com.critetiontech.ctvitalio.databinding.FragmentMoodBinding
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.utils.ToastUtils
import com.critetiontech.ctvitalio.viewmodel.MoodViewModel
import androidx.core.graphics.toColorInt

class MoodFragment : Fragment() {

    private var _binding: FragmentMoodBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MoodViewModel
    private lateinit var moodAdapter: MoodAdapter
    private var colorAnimator: ValueAnimator? = null

    private val moods = listOf(
        MoodData(5, "Spectacular", "#FFA4BA", R.drawable.spectulor_mood, "#611829"),
        MoodData(6, "Upset", "#88A7FF", R.drawable.upset_mood, "#2A4089"),
        MoodData(1, "Stressed", "#FF9459", R.drawable.stressed_mood, "#782E04"),
        MoodData(2, "Happy", "#9ABDFF", R.drawable.happy_mood, "#505D87"),
        MoodData(4, "Good", "#F9C825", R.drawable.good_mood, "#664F00"),
        MoodData(3, "Sad", "#7DE7EE", R.drawable.sad_mood, "#3A7478")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[MoodViewModel::class.java]

        // Default system UI color
        (requireActivity() as? BaseActivity)?.setSystemBarsColor(
            statusBarColor = R.color.stressed,
            navBarColor = R.color.white,
            lightIcons = true
        )

        binding.rootMoodLayout.transitionToEnd()

        setupRecyclerView()
        observeViewModel()
        setClickListeners()
        setUserInfo()
    }

    private fun setupRecyclerView() {
        moodAdapter = MoodAdapter(emptyList())
        binding.moodEmoji.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = moodAdapter
            PagerSnapHelper().attachToRecyclerView(this)
        }

        // Scroll listener for background color animation
        binding.moodEmoji.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState != RecyclerView.SCROLL_STATE_IDLE) return

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val position = layoutManager.findFirstCompletelyVisibleItemPosition()

                if (position == RecyclerView.NO_POSITION) return

                val moodList = viewModel.moodsLiveData.value ?: return
                if (position !in moodList.indices) return

                val mood = moodList[position]
                viewModel.onMoodClicked(mood.id.toString())

                val localMood = moods.find { it.id == mood.id }
                val targetColorHex = localMood?.color ?: "#FFFFFF"
                val targetColor = runCatching { targetColorHex.toColorInt() }.getOrDefault(Color.WHITE)

                val currentColor =
                    (binding.rootMoodLayout.background as? ColorDrawable)?.color ?: Color.WHITE

                colorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), currentColor, targetColor).apply {
                    duration = 500

                    addUpdateListener { animator ->
                        if (view == null || !isAdded) return@addUpdateListener   // prevents crash

                        val animatedColor = animator.animatedValue as Int
                        binding.rootMoodLayout.setBackgroundColor(animatedColor)

                        (activity as? BaseActivity)?.setSystemBarsColorInt(
                            statusColorInt = animatedColor,
                            navColorInt = ContextCompat.getColor(requireContext(), R.color.white),
                            lightIcons = true
                        )
                    }

                    start()
                }

            }
        })
    }

    private fun observeViewModel() {
        viewModel.getMoodByPid()

        viewModel.moodsLiveData.observe(viewLifecycleOwner) { apiMoods ->
            if (apiMoods.isNullOrEmpty()) return@observe

            val mapped = apiMoods.map { api ->
                val localMood = moods.find { it.id == api.id }
                Mood(
                    id = api.id,
                    label = api.label,
                    color = localMood?.color ?: "#FFA4BA",
                    emojiRes = localMood?.emojiRes ?: R.drawable.spectulor_mood,
                    description = api.description
                )
            }

            moodAdapter.updateList(mapped)
        }
    }

    private fun setUserInfo() {
        binding.userName2.text = PrefsManager().getPatient()?.patientName ?: ""

        Glide.with(this)
            .load("http://182.156.200.177:5082/" + (PrefsManager().getPatient()?.imageURL ?: ""))
            .placeholder(R.drawable.baseline_person_24)
            .circleCrop()
            .into(binding.userAvatar)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.userName.text = ToastUtils.getSimpleGreeting()
        }
    }

    private fun setClickListeners() {
        binding.ivBack.setOnClickListener { findNavController().popBackStack() }

        binding.selectMoodButton.setOnClickListener {
            viewModel.insertMood(requireContext())
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        colorAnimator?.cancel()
        colorAnimator = null
        _binding = null
    }
}
