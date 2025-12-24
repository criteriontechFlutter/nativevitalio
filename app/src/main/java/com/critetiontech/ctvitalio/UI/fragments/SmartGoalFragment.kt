package com.critetiontech.ctvitalio.UI.fragments

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.GoalsAdapter
import com.critetiontech.ctvitalio.databinding.FragmentSmartGoalBinding
import com.critetiontech.ctvitalio.databinding.LayoutGoalMenuBinding
import com.critetiontech.ctvitalio.utils.GoalMenuHandler
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
    ): View {
        binding = FragmentSmartGoalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[SmartGoalViewModel::class.java]

        setupRecyclerView()

        viewModel.getAddedSmartGoal()
        viewModel.getAllGoalList()

        /* --------------------- ADDED SMART GOALS LIST (MAIN SCREEN) --------------------- */
        viewModel.vitalList.observe(viewLifecycleOwner) { categoryList ->
            if (!categoryList.isNullOrEmpty()) {

                val finalList = mutableListOf<Any>()

                categoryList.forEach { category ->
                    finalList.add(category)             // ✔ Add full category object
                    finalList.addAll(category.goals)    // ✔ Add goals
                }

                adapter.updateData(finalList, isAllGoal = false)
            }
        }



        binding.wellnessImageArrow.setOnClickListener {
            findNavController().popBackStack()
        }

        /* --------------------- ADD NEW GOALS BUTTON --------------------- */
        binding.btnAdd.setOnClickListener {
            viewModel.allGoalList.observe(viewLifecycleOwner) { categoryList ->
                if (!categoryList.isNullOrEmpty()) {

                    val finalList = mutableListOf<Any>()

                    categoryList.forEach { category ->
                        finalList.add(category)         // ✔ object
                        finalList.addAll(category.goals)
                    }

                    showFullScreenBottomSheet(requireContext(), finalList)
                    adapter.updateData(finalList, isAllGoal = true)
                }
            }
        }
    }

    /* ------------------------------------------------------------- */
    /* RECYCLER VIEW                                                 */
    /* ------------------------------------------------------------- */
    private fun setupRecyclerView() {
        adapter = GoalsAdapter(
            emptyList(),
            isAllGoal = true,
            onGoalClick = { goal, category ->


            },
            onPinClick = { goal, category ->
                if (category != null) {
                    viewModel.updatePinStatus(
                        goal.id,
                        goal.isPinned
                    )
                }

            },
            onThreeDotClick = { goal, category,ivMenu ->
                val binding = LayoutGoalMenuBinding.inflate(layoutInflater)

                val popupWindow = PopupWindow(
                    binding.root,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
                )

                binding.handler = GoalMenuHandler(
                    onEditClick = {
                        popupWindow.dismiss()
                        // Edit logic
                    },
                    onRemoveClick = {
                        viewModel.removeGoal(goal)
                        popupWindow.dismiss()
                        // Remove logic
                    }
                )

                popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                popupWindow.elevation = 12f
                popupWindow.animationStyle = R.style.PopupFadeScaleAnimation

                showPopupAligned(popupWindow, ivMenu)


            },
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }


    fun showPopupAligned(
        popupWindow: PopupWindow,
        anchor: View,
        yOffsetDp: Int = 8
    ) {
        val location = IntArray(2)
        anchor.getLocationOnScreen(location)

        val screenWidth = anchor.resources.displayMetrics.widthPixels
        popupWindow.contentView.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED
        )
        val popupWidth = popupWindow.contentView.measuredWidth

        val anchorCenterX = location[0] + anchor.width / 2
        val halfPopup = popupWidth / 2


        val xOffset = (-120).dpToPx(anchor)  // move left
        val yOffset = (8).dpToPx(anchor)
        popupWindow.showAsDropDown(anchor, xOffset, yOffset)
    }


    fun Int.dpToPx(view: View): Int {
        return (this * view.resources.displayMetrics.density).toInt()
    }


    /* ------------------------------------------------------------- */
    /* FULL SCREEN BOTTOM SHEET                                      */
    /* ------------------------------------------------------------- */
    fun showFullScreenBottomSheet(context: Context, finalList: MutableList<Any>) {
        val dialog = BottomSheetDialog(context, R.style.FullScreenBottomSheetDialog)
        val view = LayoutInflater.from(context).inflate(R.layout.set_goal_layout, null)
        dialog.setContentView(view)

        val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            val behavior = BottomSheetBehavior.from(it)
            behavior.skipCollapsed = true
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = false
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val backIV = view.findViewById<ImageView>(R.id.wellnessImageArrow)

        backIV.setOnClickListener { dialog.dismiss() }

        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = GoalsAdapter(
            finalList,
            isAllGoal = true,

            /* -------------------- CLICK: OPEN SET GOAL SCREEN -------------------- */
            onGoalClick = { goal, category ->

                val bundle = Bundle().apply {

                    Log.d("SmartGoal", "Selected categoryId = ${category?.categoryId}")

                    putString("categoryId", (category?.categoryId ?: 0).toString()) // ✔ FIXED
                    putString("goalId", goal.goalId.toString()) // ✔ FIXED
                    putString("vmID", goal.vmId.toString())
                }

                findNavController().navigate(
                    R.id.action_smartGoalFragment_to_setGoal,
                    bundle
                )

                dialog.dismiss()
            },

            /* -------------------- CLICK: PIN -------------------- */
            onPinClick = { goal, category ->

            },
            onThreeDotClick = { goal, category,ivMenu ->

            }
        )

        adapter.updateData(finalList, isAllGoal = false)
        recyclerView.adapter = adapter

        dialog.show()
    }
}


