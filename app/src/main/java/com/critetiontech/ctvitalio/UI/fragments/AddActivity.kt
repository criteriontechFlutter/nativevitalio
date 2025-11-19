package com.example.vitalio_pragya

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentAddActivityBinding
import com.example.vitalio_pragya.adapter.ActivityChipAdapter
import com.example.vitalio_pragya.fragment.BikingFragment
import com.example.vitalio_pragya.viewmodel.AddActivityViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class AddActivityFragment : Fragment() {

    // ✅ ViewBinding setup
    private var _binding: FragmentAddActivityBinding? = null
    private val binding get() = _binding!!

    // ✅ Adapters for both RecyclerViews
    private lateinit var recentAdapter: ActivityChipAdapter
    private lateinit var allActivitiesAdapter: ActivityChipAdapter

    // ✅ ViewModel for logic
    private val viewModel: AddActivityViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

   viewModel.getAllEmployeeActivity()
        // ✅ Setup Flexbox layout managers (for chips)
        val flexRecent = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            justifyContent = JustifyContent.FLEX_START
        }
        val flexAll = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            justifyContent = JustifyContent.FLEX_START
        }

        // ✅ Initialize adapters with click listener
        recentAdapter = ActivityChipAdapter(emptyList()) { activity ->
            onActivitySelected(activity.activityName.toString())
        }
        allActivitiesAdapter = ActivityChipAdapter(emptyList()) { activity ->
            onActivitySelected(activity.activityName.toString())
        }

        // ✅ Set adapters and layout managers
        binding.recentRecyclerView.layoutManager = flexRecent
        binding.recentRecyclerView.adapter = recentAdapter

        binding.allActivitiesRecyclerView.layoutManager = flexAll
        binding.allActivitiesRecyclerView.adapter = allActivitiesAdapter

        // ✅ Observe ViewModel LiveData
        viewModel.recentActivities.observe(viewLifecycleOwner) { recent ->
            recentAdapter.updateList(recent)
        }
        viewModel.filteredActivities.observe(viewLifecycleOwner) { recent ->
            allActivitiesAdapter.updateList(recent)
        }

        // ✅ Handle SearchView text
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.filterActivities(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterActivities(newText.orEmpty())
                return true
            }
        })

        // ✅ Back button handling
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    /**
     * Handles when an activity chip is selected
     */
    private fun onActivitySelected(activity: String) {
        if (activity.equals("Biking", ignoreCase = true)) {

            findNavController().navigate(R.id.action_addActivityFragment_to_bikingFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
