package com.criterion.nativevitalio.UI.fragments

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.criterion.nativevitalio.utils.LoaderUtils.hideLoading
import com.criterion.nativevitalio.utils.LoaderUtils.showLoading
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.adapter.SymptomHistoryAdapter
import com.criterion.nativevitalio.databinding.FragmentSymptomHistoryBinding
import com.criterion.nativevitalio.utils.FilterType
import com.criterion.nativevitalio.viewmodel.SymptomsHistoryViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SymptomHistory : Fragment() {

    private lateinit var binding: FragmentSymptomHistoryBinding
    private lateinit var viewModel: SymptomsHistoryViewModel
    private lateinit var adapter: SymptomHistoryAdapter
    @RequiresApi(Build.VERSION_CODES.O)
    private var currentAnchorDate: LocalDate = LocalDate.now()
    private var currentFilter: FilterType = FilterType.WEEKLY
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSymptomHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = SymptomHistoryAdapter()
        binding.symptomRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.symptomRecyclerView.adapter = adapter

        viewModel = ViewModelProvider(this)[SymptomsHistoryViewModel::class.java]
        viewModel.getSymptoms()
        updateToggleStyles(binding.tabToggleGroup.checkedButtonId)
        viewModel.symptomList.observe(viewLifecycleOwner) {
            applyFilter(FilterType.WEEKLY) // default
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) showLoading() else hideLoading()
        }

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.backdate.setOnClickListener {
            val newDate = when (currentFilter) {
                FilterType.DAILY -> currentAnchorDate.minusDays(1)
                FilterType.WEEKLY -> currentAnchorDate.minusWeeks(1)
                FilterType.MONTHLY -> currentAnchorDate.minusMonths(1)
            }
            applyFilter(currentFilter, newDate)
        }

        binding.forwarddate.setOnClickListener {
            val newDate = when (currentFilter) {
                FilterType.DAILY -> currentAnchorDate.plusDays(1)
                FilterType.WEEKLY -> currentAnchorDate.plusWeeks(1)
                FilterType.MONTHLY -> currentAnchorDate.plusMonths(1)
            }
            applyFilter(currentFilter, newDate)
        }
        binding.tabToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            updateToggleStyles(checkedId)
            if (!isChecked) return@addOnButtonCheckedListener
            when (checkedId) {
                R.id.btn_daily -> applyFilter(FilterType.DAILY)
                R.id.btn_weekly -> applyFilter(FilterType.WEEKLY)
                R.id.btn_monthly -> applyFilter(FilterType.MONTHLY)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun applyFilter(type: FilterType, anchorDate: LocalDate = currentAnchorDate) {
        currentFilter = type
        currentAnchorDate = anchorDate

        val fullList = viewModel.symptomList.value.orEmpty()
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

        val filtered = fullList.filter {
            val rawDate = it.detailsDate
            if (rawDate.isNullOrEmpty() || rawDate.length < 10) return@filter false
            val date = try {
                LocalDate.parse(rawDate.substring(0, 10))
            } catch (e: Exception) {
                return@filter false
            }

            when (type) {
                FilterType.DAILY -> date.isEqual(anchorDate)
                FilterType.WEEKLY -> {
                    val start = anchorDate.with(java.time.DayOfWeek.MONDAY)
                    val end = anchorDate.with(java.time.DayOfWeek.SUNDAY)
                    date in start..end
                }
                FilterType.MONTHLY -> date.month == anchorDate.month && date.year == anchorDate.year
            }
        }

        adapter.submitGroupedList(filtered)

        // Update date range text
        when (type) {
            FilterType.DAILY -> binding.dateRange.text = formatter.format(anchorDate)
            FilterType.WEEKLY -> {
                val start = anchorDate.with(java.time.DayOfWeek.MONDAY)
                val end = anchorDate.with(java.time.DayOfWeek.SUNDAY)
                binding.dateRange.text = "${formatter.format(start)} - ${formatter.format(end)}"
            }
            FilterType.MONTHLY -> {
                val month = anchorDate.month.name.lowercase().replaceFirstChar { it.uppercase() }
                binding.dateRange.text = "$month ${anchorDate.year}"
            }
        }
    }


    private fun updateToggleStyles(checkedId: Int) {
        val buttons = listOf(binding.btnDaily, binding.btnWeekly,binding.btnMonthly)
        for (button in buttons) {
            if (button.id == checkedId) {
                button.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.blue))
                button.setTextColor(Color.WHITE)
            } else {
                button.setBackgroundColor(Color.WHITE)
                button.setTextColor(ContextCompat.getColor(requireActivity(), R.color.gray))
            }
        }
    }
}