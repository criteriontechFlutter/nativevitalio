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
import androidx.viewbinding.ViewBinding
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentSetGoalBinding

class SetGoal : Fragment() {
    private var _binding: FragmentSetGoalBinding? = null
    private val binding get() = _binding!!

    // store selected day index (0–6)
    private val selectedDays = mutableSetOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetGoalBinding.inflate(inflater, container, false)

        setupWeekSelector()

        return binding.root
    }

    private fun setupWeekSelector() {
        val days = listOf("S", "M", "T", "W", "T", "F", "S")
        val containerLayout = binding.weekSelectorContainer
        containerLayout.removeAllViews()

        days.forEachIndexed { index, symbol ->

            val dayView = layoutInflater.inflate(R.layout.day_item, containerLayout, false) as LinearLayout
            val label = dayView.findViewById<TextView>(R.id.dayLabel)
            val check = dayView.findViewById<ImageView>(R.id.checkId)

            label.text = symbol

            // initial state: selected by default
            val isSelected = selectedDays.contains(index).also {
                if (!it) selectedDays.add(index)
            }
            applyDayUi(dayView, label, check, isSelected)

            dayView.setOnClickListener {
                val nowSelected = !selectedDays.contains(index)

                if (nowSelected) {
                    selectedDays.add(index)
                } else {
                    selectedDays.remove(index)
                }

                applyDayUi(dayView, label, check, nowSelected)
            }

            containerLayout.addView(dayView)
        }
    }

    private fun applyDayUi(
        dayView: View,
        label: TextView,
        check: ImageView,
        isSelected: Boolean
    ) {
        if (isSelected) {
            label.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue))
              check.setImageResource(R.drawable.rounded_check)
            check.visibility = View.VISIBLE
        } else {
             label.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue))
            check.setImageResource(R.drawable.rounded_circle)
            check.visibility = View.VISIBLE   // or INVISIBLE if you prefer only border
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}