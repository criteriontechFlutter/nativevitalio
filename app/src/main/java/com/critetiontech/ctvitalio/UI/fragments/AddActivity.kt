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
import com.critetiontech.ctvitalio.adapter.EmployeeActivityAdapter
import com.critetiontech.ctvitalio.adapter.MasterActivityAdapter
import com.critetiontech.ctvitalio.databinding.FragmentAddActivityBinding
import com.example.vitalio_pragya.adapter.ActivityChipAdapter
import com.example.vitalio_pragya.fragment.BikingFragment
import com.example.vitalio_pragya.viewmodel.AddActivityViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent


class AddActivityFragment : Fragment() {

    private var _binding: FragmentAddActivityBinding? = null
    private val binding get() = _binding!!

    private lateinit var employeeAdapter: EmployeeActivityAdapter
    private lateinit var masterAdapter: MasterActivityAdapter

    private val viewModel: AddActivityViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getAllEmployeeActivity()
        viewModel.getAllActivityMaster()

        setRecyclerViews()
        observeData()
        setupSearch()

        binding.backButton.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
    }

    private fun setRecyclerViews() {

        binding.recentRecyclerView.layoutManager = flexLayout()
        binding.allActivitiesRecyclerView.layoutManager = flexLayout()

        employeeAdapter = EmployeeActivityAdapter(emptyList()) { onActivitySelected(it.activityName,it.id,) }
        masterAdapter = MasterActivityAdapter(emptyList()) { onActivitySelected(it.activityName,it.id,) }

        binding.recentRecyclerView.adapter = employeeAdapter
        binding.allActivitiesRecyclerView.adapter = masterAdapter
    }

    private fun observeData() {
        viewModel.employeeActivityList.observe(viewLifecycleOwner) {
            employeeAdapter.updateList(it)
        }

        viewModel.activityMasterList.observe(viewLifecycleOwner) {
            masterAdapter.updateList(it)
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true.apply { filter(query) }
            override fun onQueryTextChange(newText: String?) = true.apply { filter(newText) }
        })
    }

    private fun filter(search: String?) {
        val list = viewModel.activityMasterList.value ?: return
        masterAdapter.updateList(list.filter { it.activityName.contains(search ?: "", true) })
    }

    private fun flexLayout() = FlexboxLayoutManager(requireContext()).apply {
        flexDirection = FlexDirection.ROW
        justifyContent = JustifyContent.FLEX_START
    }

    private fun onActivitySelected(name: String,Id: Int) {
        val bundle = Bundle().apply {
            putString("activityName", name)
            putInt("activityId", Id)  // Later dynamic from list
        }
        findNavController().navigate(R.id.action_addActivityFragment_to_bikingFragment,bundle)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}