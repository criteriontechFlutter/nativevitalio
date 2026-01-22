package com.critetiontech.ctvitalio.UI.fragments

import AllMedicine
import Medicine
import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
 import com.critetiontech.ctvitalio.adapter.LoggedMedicineAdapter
import com.critetiontech.ctvitalio.adapter.MedicineAdapter
import com.critetiontech.ctvitalio.databinding.FragmentMedicationBinding
import com.critetiontech.ctvitalio.utils.VitalioCalendarView
import com.critetiontech.ctvitalio.viewmodel.MedicationViewModel
import org.json.JSONObject

class MedicationFragment : Fragment() {

    private var _binding: FragmentMedicationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MedicationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create Calendar UI
        val calendarView = VitalioCalendarView(requireContext())
        binding.calendarContainer.addView(calendarView)

        // RecyclerViews Setup
        binding.rvMedicines.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        binding.rvLoggedMedicines.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)



        val today = calendarView.getSelectedDateString("yyyy-MM-dd")
        viewModel.getEmployeeMedicineIntakeByDate(today)

        // 🔥 Listen Date Click from Calendar -> Run API
        calendarView.onDateSelected = { date ->
            viewModel.getEmployeeMedicineIntakeByDate(date)
        }

        // Observe both lists
        observeMedicationData()
        observeMedicationLoggedData()

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(
                com.critetiontech.ctvitalio.R.id.action_medicationFragment_to_addMedicineReminderFragment
            )
        }
    }

    private fun observeMedicationData() {
        viewModel.allMedicineListLiveData.observe(viewLifecycleOwner) { list ->
            binding.rvMedicines.adapter = MedicineAdapter(requireContext(), list)
        }
    }

    private fun observeMedicationLoggedData() {
        viewModel.employeeMedicineIntakeLiveData.observe(viewLifecycleOwner) { list ->
            binding.rvLoggedMedicines.adapter = LoggedMedicineAdapter(list)
            Toast.makeText(requireContext(), "Logs for Selected Day: ${list.size}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}