package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.MindfulnessActivityAdapter
import com.critetiontech.ctvitalio.databinding.FragmentMindfullnessBinding
import com.critetiontech.ctvitalio.model.MindfulnessExercise
import com.critetiontech.ctvitalio.viewmodel.WellnessViewModel


class MindfulnessFragment : Fragment() {

    private lateinit var binding: FragmentMindfullnessBinding
    private val viewModel: WellnessViewModel by viewModels()
    private lateinit var groundingAdapter: MindfulnessActivityAdapter
    private lateinit var bingoAdapter: MindfulnessActivityAdapter
    private lateinit var breathingAdapter: MindfulnessActivityAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMindfullnessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.wellnessImageArrow.setOnClickListener {
            findNavController().popBackStack()
        }

        setupRecyclerViews()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        groundingAdapter = MindfulnessActivityAdapter { exercise -> navigateToDetail(exercise) }
        binding.groundingRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groundingAdapter
        }

        bingoAdapter = MindfulnessActivityAdapter { exercise -> navigateToDetail(exercise) }
        binding.bingoRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bingoAdapter
        }

        breathingAdapter = MindfulnessActivityAdapter { exercise -> navigateToDetail(exercise) }
        binding.breathingRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = breathingAdapter
        }
    }

    private fun navigateToDetail(exercise: MindfulnessExercise) {
        val args = Bundle().apply {
            putString("exerciseName", exercise.exerciseName)
            putString("exerciseDescription", exercise.description)
            putStringArrayList("benefits", ArrayList(exercise.benefits.map { it.benefit }))
        }
        findNavController().navigate(R.id.action_mindfulnessFragment_to_groundingTecniqueView, args)
    }

    private fun observeViewModel() {
        viewModel.progress.observe(viewLifecycleOwner) { progress ->
            binding.tvActivitiesCount.text = "${progress.activitiesCompleted} activities completed"
            binding.tvStreakCount.text = "🔥 ${progress.dayStreak}"
        }
        viewModel.groundingExercises.observe(viewLifecycleOwner) { items ->
            groundingAdapter.submitList(items)
        }
        viewModel.bingoActivities.observe(viewLifecycleOwner) { items ->
            bingoAdapter.submitList(items)
        }
        viewModel.breathingActivities.observe(viewLifecycleOwner) { items ->
            breathingAdapter.submitList(items)
        }
    }
}