package com.criterion.nativevitalio.UI.ui.signupFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.criterion.nativevitalio.adapter.SetPreferencesAdapter
import com.criterion.nativevitalio.databinding.FragmentSetPreferencesBinding
import com.criterion.nativevitalio.model.FrequencyModel
import com.criterion.nativevitalio.model.VitalReminder
import com.criterion.nativevitalio.viewmodel.RegistrationViewModel

class SetPreferences : Fragment() {


    private lateinit var binding: FragmentSetPreferencesBinding
    private lateinit var viewModel: RegistrationViewModel

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
            val selected = viewModel.setVitalList.value?.filter { it.isCheck } ?: emptyList()
            Toast.makeText(requireContext(), "${selected.size} reminders set", Toast.LENGTH_SHORT).show()
            // Proceed with navigation or saving
        }

        binding.btnSkip.setOnClickListener {
            findNavController().navigateUp()
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
