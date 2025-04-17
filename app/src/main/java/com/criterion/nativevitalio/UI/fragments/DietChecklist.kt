package com.criterion.nativevitalio.UI.fragments

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
import com.criterion.nativevitalio.model.DietListItem
import com.criterion.nativevitalio.viewmodel.DietChecklistViewModel
import java.text.SimpleDateFormat
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
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val fullDateTime = "$date $time" // e.g., 2025-04-17 07:34 PM
            viewModel.intakeByDietID(dietId, fullDateTime)
        }
        binding.recyclerView.adapter = adapter

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.getFoodIntake()

        viewModel.dietList.observe(viewLifecycleOwner) { dietList ->
            val grouped = dietList
                .groupBy { it.foodGivenAt ?: "Others" }
                .flatMap { (timeSlot, items) ->
                    listOf(DietListItem.Header(timeSlot)) + items.map { DietListItem.Item(it) }
                }

            adapter.setData(grouped)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            // You can show a toast or log here
        }
    }
}