package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentMedicationBinding

import com.critetiontech.ctvitalio.utils.VitalioCalendarView


class MedicationFragment : Fragment() {

    private var _binding: FragmentMedicationBinding? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMedicationBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

       // _binding?.calendarContainer?.background = ContextCompat.getDrawable(requireContext(), R.drawable.bottom_curve_bg)

        // Create and add custom calendar view
        val calendarView = VitalioCalendarView(requireContext())

        _binding?.calendarContainer?.addView(calendarView)


// Mark dates with different progress levels
        calendarView.setDateProgressMap(mapOf(
            10 to 0.25f,  // 25% complete
            14 to 0.75f,  // 75% complete
            20 to 1.0f    // 100% complete
        ))

// Update single date progress
        calendarView.setDateProgress(14, 0.85f) // Update to 85%
        // Highlight specific goal dates
        calendarView.setGoalDates(listOf(8, 9, 10, 13, 15))

        _binding?.backIV?.setOnClickListener {
            findNavController().popBackStack()
        }


        _binding?.fabAdd?.setOnClickListener(){

            findNavController().navigate(R.id.action_medicationFragment_to_addMedicineReminderFragment)
        }

    }

}


