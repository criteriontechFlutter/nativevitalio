package com.critetiontech.ctvitalio.UI.fragments

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.GoalsAdapter
import com.critetiontech.ctvitalio.databinding.FragmentSleepDetailsBinding
import com.critetiontech.ctvitalio.databinding.FragmentSmartGoalBinding
import com.critetiontech.ctvitalio.databinding.FragmentSymptomHistoryBinding
import com.critetiontech.ctvitalio.model.GoalItem
import com.critetiontech.ctvitalio.viewmodel.SmartGoalViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog


class SmartGoalFragment : Fragment() {
    private lateinit var binding: FragmentSmartGoalBinding
    private lateinit var viewModel: SmartGoalViewModel
    private lateinit var adapter: GoalsAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSmartGoalBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[SmartGoalViewModel::class.java]

        setupRecyclerView()

        viewModel.getAddedSmartGoal()

        viewModel.vitalList.observe(viewLifecycleOwner) { categoryList ->
            if (!categoryList.isNullOrEmpty()) {

                val finalList = mutableListOf<Any>()

                categoryList.forEach { category ->
                    finalList.add(category.categoryName)   // Add header
                    finalList.addAll(category.goals)       // Add its goals
                }

                adapter.updateData(finalList)
            }
        }

        binding.wellnessImageArrow.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnAdd.setOnClickListener {
            showFullScreenBottomSheet(requireContext())
        }
    }

    private fun setupRecyclerView() {
        adapter = GoalsAdapter(emptyList())
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    fun showFullScreenBottomSheet(context: Context) {
        val dialog = BottomSheetDialog(context, R.style.FullScreenBottomSheetDialog)
        val view = LayoutInflater.from(context).inflate(R.layout.set_goal_layout, null)

        dialog.setContentView(view)

        // FULL SCREEN
        val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        // 🔥 BIND RECYCLER VIEW HERE
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = GoalsAdapter(emptyList())
        recyclerView.adapter = adapter

        dialog.show()
    }

}