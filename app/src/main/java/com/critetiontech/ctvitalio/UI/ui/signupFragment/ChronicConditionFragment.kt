package com.critetiontech.ctvitalio.UI.ui.signupFragment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.viewmodel.RegistrationViewModel
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentChronicConditionBinding
import com.critetiontech.ctvitalio.utils.LoaderUtils.hideLoading
import com.critetiontech.ctvitalio.utils.LoaderUtils.showLoading

class ChronicConditionFragment : Fragment() {
    private lateinit var binding: FragmentChronicConditionBinding
    private lateinit var viewModel: RegistrationViewModel
    private lateinit var progressViewModel: ProgressViewModel
    private var latestSuggestions: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChronicConditionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel = ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]
        progressViewModel = ViewModelProvider(requireActivity())[ProgressViewModel::class.java]
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString().trim()
                if (input.isNotEmpty()) {
                    viewModel.getProblemList(input)
                    binding.chronicDis.setDropDownBackgroundResource(android.R.color.white)
                    binding.chronicDis.showDropDown()
                }
            }
        }
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) showLoading() else hideLoading()
        }

        binding.chronicDis.addTextChangedListener(textWatcher)
        binding.chronicDis.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Hide keyboard when the AutoCompleteTextView has focus and shows the dropdown
                hideKeyboard()
            }
        }
        binding.chronicDis.setOnItemClickListener { parent, _, position, _ ->
            val selectedName = parent.getItemAtPosition(position).toString()
            val selectedProblem = viewModel.problemList.value?.find {capitalizeFirstLetter(it.problemName) == selectedName }

            if (selectedProblem != null) {
                viewModel.addSelectedDisease(selectedProblem, requireContext())
                binding.chronicDis.removeTextChangedListener(textWatcher)
                binding.chronicDis.setText("", false)
                binding.chronicDis.addTextChangedListener(textWatcher)
                binding.chronicDis.dismissDropDown()
            }
        }

        viewModel.problemList.observe(viewLifecycleOwner) { problemList ->
            if (!problemList.isNullOrEmpty()) {
                latestSuggestions = problemList.map { capitalizeFirstLetter(it.problemName)  }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, latestSuggestions)
                binding.chronicDis.setAdapter(adapter)
            }
        }

        viewModel.selectedDiseaseList.observe(viewLifecycleOwner) { list ->
            binding.selectedListContainer.removeAllViews()
            list.distinctBy { it["detailID"] }.forEach { disease ->
                addRemovableChip(disease)
            }
        }

        binding.btnNext.setOnClickListener {
            val summary = viewModel.selectedDiseaseList.value
                ?.joinToString(", ") { it["details"] ?: "" } ?: ""

            progressViewModel.updateProgress(7)
            progressViewModel.updatepageNo(8)
            progressViewModel.updateotherChronical(101)

            viewModel.chronicDisease.value = summary
            findNavController().navigate(R.id.action_chronicConditionFragment_to_otherChronicDisease)
        }
    }
    fun capitalizeFirstLetter(sentence: String): String {
        return sentence.trim().replaceFirstChar {
            if (it.isLowerCase()) it.titlecaseChar() else it
        }
    }
    private fun addRemovableChip(disease: Map<String, String>) {
        val inflater = LayoutInflater.from(requireContext())
        val chipView = inflater.inflate(R.layout.chip_removable, binding.selectedListContainer, false)

        val chipText = chipView.findViewById<TextView>(R.id.chipText)
        val chipRemove = chipView.findViewById<ImageView>(R.id.chipRemove)

        chipText.text = disease["details"] ?: ""

        chipRemove.setOnClickListener {
            viewModel.removeSelectedDisease(disease["detailID"].orEmpty())
        }

        binding.selectedListContainer.addView(chipView)
    }

    private fun hideKeyboard() {
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusView = activity?.currentFocus
        if (currentFocusView != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocusView.windowToken, 0)
        }
    }
}