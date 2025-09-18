package com.critetiontech.ctvitalio.UI.fragments

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.SymptomHistoryAdapter
import com.critetiontech.ctvitalio.databinding.FragmentSymptomHistoryBinding
import com.critetiontech.ctvitalio.utils.FilterType
import com.critetiontech.ctvitalio.viewmodel.SymptomsHistoryViewModel
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

        viewModel.symptomList.observe(viewLifecycleOwner) {
            val filterType = getSelectedFilterType() // Get the selected filter type
            adapter.submitGroupedList(it, filterType) // Pass the symptoms and filter type to the adapter
            updateDateRangeText(filterType) // Update the date range text
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

        // Add toggle buttons click listeners
        binding.btnDaily.setOnClickListener {
            if (currentFilter != FilterType.DAILY) {
                binding.tabToggleGroup.check(R.id.btn_daily)
            }
            applyFilter(FilterType.DAILY)
            updateToggleStyles(R.id.btn_daily)
        }

        binding.btnWeekly.setOnClickListener {
            if (currentFilter != FilterType.WEEKLY) {
                binding.tabToggleGroup.check(R.id.btn_weekly)
            }
            applyFilter(FilterType.WEEKLY)
            updateToggleStyles(R.id.btn_weekly)
        }

        binding.btnMonthly.setOnClickListener {
            if (currentFilter != FilterType.MONTHLY) {
                binding.tabToggleGroup.check(R.id.btn_monthly)
            }
            applyFilter(FilterType.MONTHLY)
            updateToggleStyles(R.id.btn_monthly)
        }
    }

    private fun getSelectedFilterType(): FilterType {
        return when (binding.tabToggleGroup.checkedButtonId) {
            R.id.btn_daily -> FilterType.DAILY
            R.id.btn_weekly -> FilterType.WEEKLY
            R.id.btn_monthly -> FilterType.MONTHLY
            else -> FilterType.DAILY // Default to DAILY if no button is selected
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun applyFilter(type: FilterType, anchorDate: LocalDate = currentAnchorDate) {
        currentFilter = type
        currentAnchorDate = anchorDate

        val fullList = viewModel.symptomList.value.orEmpty()
        val filtered = fullList.filter {
            val rawDate = it.detailsDate
            if (rawDate.isEmpty() || rawDate.length < 10) return@filter false
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            val date = try {
                LocalDate.parse(rawDate, formatter)
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

        // Log the filtered data to check if the filter works
        Log.d("SymptomHistory", "Filtered data: $filtered")

        // Pass both filtered data and filterType to the adapter
        adapter.submitGroupedList(filtered, type)

        // Update date range text
        updateDateRangeText(type)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateDateRangeText(filterType: FilterType) {
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

        when (filterType) {
            FilterType.DAILY -> {
                binding.dateRange.text = formatter.format(currentAnchorDate)
            }
            FilterType.WEEKLY -> {
                val start = currentAnchorDate.with(java.time.DayOfWeek.MONDAY)
                val end = currentAnchorDate.with(java.time.DayOfWeek.SUNDAY)
                binding.dateRange.text = "${formatter.format(start)} - ${formatter.format(end)}"
            }
            FilterType.MONTHLY -> {
                val month = currentAnchorDate.month.name.lowercase().replaceFirstChar { it.uppercase() }
                binding.dateRange.text = "$month ${currentAnchorDate.year}"
            }
        }
    }

    private fun updateToggleStyles(checkedId: Int) {
        val buttons = listOf(binding.btnDaily, binding.btnWeekly, binding.btnMonthly)
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