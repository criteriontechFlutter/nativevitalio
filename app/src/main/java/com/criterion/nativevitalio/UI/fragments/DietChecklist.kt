package com.criterion.nativevitalio.UI.fragments

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.criterion.nativevitalio.adapter.DietChecklistAdapter
import com.criterion.nativevitalio.databinding.FragmentDietChecklistBinding

import com.criterion.nativevitalio.viewmodel.DietChecklistViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DietChecklist : Fragment() {

    private lateinit var binding: FragmentDietChecklistBinding
    private lateinit var viewModel: DietChecklistViewModel
    private lateinit var adapter: DietChecklistAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentDietChecklistBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[DietChecklistViewModel::class.java]
        adapter = DietChecklistAdapter(requireContext()) { dietId, time ->

            viewModel.intakeByDietID(dietId, time)
        }
        binding.recyclerView.adapter = adapter

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.getFoodIntake("")

        viewModel.dietList.observe(viewLifecycleOwner) { dietList ->
            val flatList = mutableListOf<com.criterion.nativevitalio.adapter.DietListItem>()

            dietList
                .groupBy { it.foodGivenAt ?: "Others" }
                .forEach { (timeSlot, items) ->
                    items.forEachIndexed { index, item ->
                        flatList.add(
                            com.criterion.nativevitalio.adapter.DietListItem(
                                diet = item,
                                timeSlot = timeSlot,
                                showHeader = index == 0
                            )
                        )
                    }
                }

            adapter.setData(flatList)
        }



        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        binding.datePickerField.text = today
        viewModel.getFoodIntake(today)


        binding.datePickerField.setOnClickListener {
            val calendar = Calendar.getInstance()

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val selectedDate = "$year-${(month + 1).toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
                    binding.datePickerField.text = selectedDate
                    viewModel.getFoodIntake(selectedDate) // Pass selected date
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            // ✅ Disable future dates
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            // You can show a toast or log here
        }
    }
}