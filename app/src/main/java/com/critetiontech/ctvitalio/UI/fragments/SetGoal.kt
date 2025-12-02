package com.critetiontech.ctvitalio.UI.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat.getColor
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentSetGoalBinding
import com.critetiontech.ctvitalio.viewmodel.SetGoalViewModel
import com.critetiontech.ctvitalio.viewmodel.SmartGoalViewModel
import java.util.Calendar

class SetGoal : Fragment() {

    private var _binding: FragmentSetGoalBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SetGoalViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetGoalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[SetGoalViewModel::class.java]

        initDefaultSelectedDay()
        setupWeekSelector()
        setupClicks()
    }

    /** ----------------------------------------------------------
     *  AUTO SELECT TODAY (only first time)
     *  ---------------------------------------------------------- */
    private fun initDefaultSelectedDay() {
        if (viewModel.selectedDays.isEmpty()) {
            val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            val todayIndex = today - 1
            viewModel.selectedDays.add(todayIndex)
        }
    }

    /** ----------------------------------------------------------
     *  CLICK HANDLERS
     *  ---------------------------------------------------------- */
    private fun setupClicks() {

        binding.selectAllDaysId.setOnClickListener {
            val allDays = (0..6).toMutableSet()

            viewModel.selectedDays = if (viewModel.selectedDays.containsAll(allDays)) {
                mutableSetOf()        // Clear all
            } else {
                allDays               // Select all
            }

            setupWeekSelector()
        }

        binding.btnMinus.setOnClickListener {
            val value = binding.tvStepsValue.text.toString().toInt()
            if (value > 0) {
                binding.tvStepsValue.text = (value - 1).toString()
            }
        }

        binding.btnPlus.setOnClickListener {
            val value = binding.tvStepsValue.text.toString().toInt()
            binding.tvStepsValue.text = (value + 1).toString()
        }

        binding.btnSave.setOnClickListener {
            val categoryId = arguments?.getString("categoryId")
            val goalId = arguments?.getString("goalId")

            viewModel.updateUserData(
                requireContext(),
                categoryId = categoryId.toString(),
                goalId = goalId.toString(),
                targetValue = binding.tvStepsValue.text.toString(),
                unit = " "
            )
        }
    }

    /** ----------------------------------------------------------
     *  WEEK SELECTOR UI
     *  ---------------------------------------------------------- */
    private fun setupWeekSelector() {

        val days = listOf("S", "M", "T", "W", "T", "F", "S")
        val containerLayout = binding.weekSelectorContainer
        containerLayout.removeAllViews()

        days.forEachIndexed { index, symbol ->

            val dayView = layoutInflater.inflate(
                R.layout.day_item,
                containerLayout,
                false
            ) as LinearLayout

            val label = dayView.findViewById<TextView>(R.id.dayLabel)
            val check = dayView.findViewById<ImageView>(R.id.checkId)

            label.text = symbol

            val isSelected = viewModel.selectedDays.contains(index)
            applyDayUi(label, check, isSelected)

            dayView.setOnClickListener {
                val nowSelected = !viewModel.selectedDays.contains(index)

                if (nowSelected) viewModel.selectedDays.add(index)
                else viewModel.selectedDays.remove(index)

                applyDayUi(label, check, nowSelected)
            }

            containerLayout.addView(dayView)
        }
    }

    private fun applyDayUi(
        label: TextView,
        check: ImageView,
        isSelected: Boolean
    ) {
        if (isSelected) {
            label.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue))
            check.setImageResource(R.drawable.rounded_check)
        } else {
            label.setTextColor(ContextCompat.getColor(requireContext(), R.color.greyText))
            check.setImageResource(R.drawable.rounded_circle)
        }
        check.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}