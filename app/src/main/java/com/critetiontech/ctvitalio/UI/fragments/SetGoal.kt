package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentSetGoalBinding
import com.critetiontech.ctvitalio.model.UnitConfig
import com.critetiontech.ctvitalio.viewmodel.SetGoalViewModel
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

        binding.unitLabel.text=arguments?.getString("unit")

        binding.wellnessImageArrow.setOnClickListener {

            findNavController().popBackStack()
        }




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
            val isAllSelected = viewModel.selectedDays.containsAll(allDays)


            if (viewModel.selectedDays.containsAll(allDays)) {
                binding.selectedAll.setImageResource(R.drawable.rounded_circle)
            } else {
                binding.selectedAll.setImageResource(R.drawable.rounded_check)
            }
            viewModel.selectedDays = if (viewModel.selectedDays.containsAll(allDays)) {
                mutableSetOf()        // Clear all
            } else {
                allDays               // Select all
            }

            setupWeekSelector()
        }

        binding.btnMinus.setOnClickListener {
            updateTargetValue(isIncrement = false)
        }

        binding.btnPlus.setOnClickListener {
            updateTargetValue(isIncrement = true)
        }


        binding.btnSave.setOnClickListener {
            val categoryId = arguments?.getString("categoryId")
            val goalId = arguments?.getString("goalId")
            val vmId = arguments?.getString("vmID")
            val unit = arguments?.getString("unit")

            viewModel.updateUserData(
                requireContext(),
                categoryId = categoryId.toString(),
                goalId = goalId.toString(),
                targetValue = binding.tvStepsValue.text.toString(),
                unit = unit.toString(),
                vmId
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

    private fun updateTargetValue(isIncrement: Boolean) {

        val unit = arguments?.getString("unit")
        val config = getUnitConfig(unit)

        val currentValue =
            binding.tvStepsValue.text.toString().toDoubleOrNull() ?: 0.0

        val newValue = if (isIncrement) {
            currentValue + config.step
        } else {
            currentValue - config.step
        }

        // validation
        if (newValue < config.min) return
        if (config.max != null && newValue > config.max) return

        binding.tvStepsValue.text =
            if (config.allowDecimal) {
                String.format("%.1f", newValue)
            } else {
                newValue.toInt().toString()
            }
    }

    private fun getUnitConfig(unit: String?): UnitConfig {
        return when (unit?.lowercase()) {

            "steps" -> UnitConfig(
                step = 500.0,
                min = 0.0,
                allowDecimal = false
            )

            "liters", "ltr", "liter" -> UnitConfig(
                step = 1.0,
                min = 0.0,
                max = 10.0,
                allowDecimal = false
            )

            "minutes" -> UnitConfig(
                step = 5.0,
                min = 0.0,
                max = 300.0,
                allowDecimal = false
            )

            "hours" -> UnitConfig(
                step = 0.5,
                min = 0.0,
                max = 24.0,
                allowDecimal = true
            )

            "times/day", "count", "cycles" -> UnitConfig(
                step = 1.0,
                min = 0.0,
                allowDecimal = false
            )

            "time" -> UnitConfig(
                step = 1.0, // handled separately (HH:mm)
                allowDecimal = false
            )

            else -> UnitConfig(
                step = 1.0
            )
        }
    }

}