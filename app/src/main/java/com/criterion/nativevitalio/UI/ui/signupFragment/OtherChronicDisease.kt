package com.criterion.nativevitalio.UI.ui.signupFragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.FragmentOtherChronicDiseaseBinding
import com.criterion.nativevitalio.viewmodel.RegistrationViewModel


class OtherChronicDisease : Fragment() {




    private lateinit var binding: FragmentOtherChronicDiseaseBinding
    private lateinit var viewModel: RegistrationViewModel
    private var latestSuggestions: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOtherChronicDiseaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString().trim()
                if (input.isNotEmpty()) {
                    viewModel.getProblemList(input.first().toString())
                    binding.chronicDis.showDropDown()
                }
            }
        }

        binding.chronicDis.addTextChangedListener(textWatcher)

        viewModel.problemList.observe(viewLifecycleOwner) { problemList ->
            if (!problemList.isNullOrEmpty()) {
                latestSuggestions = problemList.map { it.problemName }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, latestSuggestions)
                binding.chronicDis.setAdapter(adapter)
            }
        }

        viewModel.otherChronicDiseaseList.observe(viewLifecycleOwner) { list ->
            binding.selectedListContainer.removeAllViews()
            list.toSet().forEach { diseaseName ->
                addRemovableChip(diseaseName)
            }
        }

        binding.chronicDis.setOnItemClickListener { parent, _, position, _ ->
            val selectedName = parent.getItemAtPosition(position).toString()
            binding.chronicDis.removeTextChangedListener(textWatcher)
            binding.chronicDis.setText("", false)
            binding.chronicDis.addTextChangedListener(textWatcher)

            viewModel.addOtherChronicDisease(selectedName)
            binding.chronicDis.dismissDropDown()
        }

        binding.btnNext.setOnClickListener {
            viewModel.otherChronicDiseases.value =
                viewModel.otherChronicDiseaseList.value?.joinToString(", ") ?: ""
            findNavController().navigate(R.id.action_otherChronicDisease_to_familyDiseaseFragment)
        }
    }

    private fun addRemovableChip(text: String) {
        val inflater = LayoutInflater.from(requireContext())
        val chipView = inflater.inflate(R.layout.chip_removable, binding.selectedListContainer, false)

        val chipText = chipView.findViewById<TextView>(R.id.chipText)
        val chipRemove = chipView.findViewById<ImageView>(R.id.chipRemove)

        chipText.text = text

        chipRemove.setOnClickListener {
            viewModel.removeOtherChronicDisease(text)
        }

        binding.selectedListContainer.addView(chipView)
    }
}