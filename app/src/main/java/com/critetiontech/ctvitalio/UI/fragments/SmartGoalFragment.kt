package com.critetiontech.ctvitalio.UI.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.GoalsAdapter
import com.critetiontech.ctvitalio.databinding.FragmentSmartGoalBinding
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
        viewModel.getAllGoalList()

        viewModel.vitalList.observe(viewLifecycleOwner) { categoryList ->
            if (!categoryList.isNullOrEmpty()) {

                val finalList = mutableListOf<Any>()

                categoryList.forEach { category ->
                    finalList.add(category.categoryName)   // Add header
                    finalList.addAll(category.goals)       // Add its goals
                }

                adapter.updateData(finalList,isAllGoal=false)
            }
        }



        binding.wellnessImageArrow.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnAdd.setOnClickListener {
            viewModel.allGoalList.observe(viewLifecycleOwner) { categoryList ->
                if (!categoryList.isNullOrEmpty()) {

                    val finalList = mutableListOf<Any>()

                    categoryList.forEach { category ->
                        finalList.add(category.categoryName)   // Add header
                        finalList.addAll(category.goals)       // Add its goals
                    }
                    showFullScreenBottomSheet(requireContext(),finalList)
                    adapter.updateData(finalList, isAllGoal = true)
                }
            }

        }
    }

    private fun setupRecyclerView() {
          adapter = GoalsAdapter(emptyList(),  isAllGoal = true,
            onGoalClick = { goal ->
                Toast.makeText(requireContext(), "Clicked on: ${goal.goalName}", Toast.LENGTH_SHORT).show()
            },
            onPinClick = { goal ->
                Toast.makeText(requireContext(), "Pinned: ${goal.goalName}", Toast.LENGTH_SHORT).show()
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    fun showFullScreenBottomSheet(context: Context, finalList: MutableList<Any>) {
        val dialog = BottomSheetDialog(context, R.style.FullScreenBottomSheetDialog)
        val view = LayoutInflater.from(context).inflate(R.layout.set_goal_layout, null)

        dialog.setContentView(view)

        val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {

            // Make layout full height
            it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

            val behavior = BottomSheetBehavior.from(it)
            behavior.skipCollapsed = true
            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            // Prevent hide on drag
            behavior.isDraggable = false
        }

        // Recycler
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val backIV = view.findViewById<ImageView>(R.id.wellnessImageArrow)
        backIV.setOnClickListener {
            dialog.dismiss()
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
         adapter = GoalsAdapter(finalList, isAllGoal = true,
        onGoalClick = { goal ->
            findNavController().navigate(
                 R.id.action_smartGoalFragment_to_setGoal
            )
            dialog.dismiss()
        },
        onPinClick = { goal ->
           viewModel.updateUserData(context  ,
                   categoryId =  goal.id,
            goalId = goal.goalId,
               targetValue =  goal.goalId,
            unit =  goal.goalId,

               )
        }
        )

        adapter.updateData(finalList,isAllGoal=false)
        recyclerView.adapter = adapter

        dialog.show()
    }


}