package com.critetiontech.ctvitalio.UI.ui.signupFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.critetiontech.ctvitalio.adapter.SetPreferencesAdapter
import com.critetiontech.ctvitalio.model.FrequencyModel
import com.critetiontech.ctvitalio.model.VitalReminder
import com.critetiontech.ctvitalio.viewmodel.RegistrationViewModel
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentSetPreferencesBinding

class SetPreferences : Fragment() {


    private lateinit var binding: FragmentSetPreferencesBinding
    private lateinit var viewModel: RegistrationViewModel
    private lateinit var progressViewModel: ProgressViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSetPreferencesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]
        progressViewModel = ViewModelProvider(requireActivity())[ProgressViewModel::class.java]

        // Observe both lists independently and trigger rendering only when both are ready
        viewModel.setVitalList.observe(viewLifecycleOwner) {
            tryRenderRecycler()
        }

        viewModel.frequencyDataList.observe(viewLifecycleOwner) {
            tryRenderRecycler()
        }

        if (viewModel.frequencyDataList.value.isNullOrEmpty()) {
            viewModel.getFrequencyList()
        }

        binding.btnNext.setOnClickListener {
            progressViewModel.updateProgress(10)
            progressViewModel.updatepageNo(13)

//            val selected = viewModel.setVitalList.value?.filter { it.isCheck } ?: emptyList()
            findNavController().navigate(R.id.action_setPreferences_to_setPreferenseFluidItake)
        }

    }

    private fun tryRenderRecycler() {
        val vitals = viewModel.setVitalList.value
        val freqs = viewModel.frequencyDataList.value

        if (!vitals.isNullOrEmpty() && !freqs.isNullOrEmpty()) {
            renderVitalRecycler(vitals, freqs)
        }
    }

    private fun renderVitalRecycler(vitalList: List<VitalReminder>, frequencies: List<FrequencyModel>) {
        val frequencyNames = frequencies.map { it.frequencyName }

        val adapter = SetPreferencesAdapter(
            vitalList,
            frequencyNames
        ) { index, selected ->
            viewModel.updateVitalFrequency(index, selected)
            binding.vitalRecyclerView.adapter?.notifyItemChanged(index)
        }

        binding.vitalRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.vitalRecyclerView.adapter = adapter
    }
}
