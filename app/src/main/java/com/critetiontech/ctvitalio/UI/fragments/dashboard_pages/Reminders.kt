package com.critetiontech.ctvitalio.UI.fragments.dashboard_pages

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.TabMedicineAdapter
import com.critetiontech.ctvitalio.databinding.FragmentRemindersBinding
import com.critetiontech.ctvitalio.viewmodel.PillsReminderViewModal

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Reminders.newInstance] factory method to
 * create an instance of this fragment.
 */
class Reminders : Fragment() {
    private var _binding: FragmentRemindersBinding? = null
    private val binding get() = _binding!!

    // Shared ViewModel (or use viewModels() if not shared)
    private val pillsViewModel: PillsReminderViewModal by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRemindersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pillsViewModel.getAllPatientMedication()

        binding.recyclerView.layoutManager =
            LinearLayoutManager(requireContext())
        // -----------------------------
        // Initialize Adapter
        // -----------------------------
        // -----------------------------
        // Observe pill list updates
        // -----------------------------
        pillsViewModel.pillList.observe(viewLifecycleOwner) { list ->
            Log.d("TAG", "selectItem: ${list.size}")


                val adapter = TabMedicineAdapter(mutableListOf()) { selectedMedicine ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        pillsViewModel.insertPatientMedication(selectedMedicine)
                    }
                }

                binding.recyclerView.adapter = adapter
            adapter.updateList(list)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}