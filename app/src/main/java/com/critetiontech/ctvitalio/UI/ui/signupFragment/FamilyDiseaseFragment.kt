package com.critetiontech.ctvitalio.UI.ui.signupFragment

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.viewmodel.RegistrationViewModel
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentFamilyDiseaseBinding

class FamilyDiseaseFragment : Fragment() {
    private lateinit var binding: FragmentFamilyDiseaseBinding
    private lateinit var viewModel: RegistrationViewModel
    private lateinit var progressViewModel: ProgressViewModel
    private var latestSuggestions: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFamilyDiseaseBinding.inflate(inflater, container, false)
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
                    viewModel.getProblemList(input.toString())
                    binding.chronicDis.setDropDownBackgroundResource(android.R.color.white)
                    binding.chronicDis.showDropDown()
                }
            }
        }

        binding.chronicDis.addTextChangedListener(textWatcher)

        viewModel.problemList.observe(viewLifecycleOwner) { problemList ->
            if (!problemList.isNullOrEmpty()) {
                latestSuggestions = problemList.map { it.problemName }
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    latestSuggestions
                )
                binding.chronicDis.setAdapter(adapter)
            }
        }

        viewModel.familyDiseaseMap.observe(viewLifecycleOwner) { map ->
            binding.selectedListContainer.removeAllViews()
            map.forEach { (relation, diseases) ->
                addFamilyDiseaseCard(relation, diseases)
            }
        }

        binding.chronicDis.setOnItemClickListener { parent, _, position, _ ->
            val selectedName = parent.getItemAtPosition(position).toString()
            binding.chronicDis.removeTextChangedListener(textWatcher)
            binding.chronicDis.setText("", false)
            binding.chronicDis.addTextChangedListener(textWatcher)
            binding.chronicDis.dismissDropDown()
            showSelectRelationDialog(selectedName)
        }

        binding.btnNext.setOnClickListener {
            val map = viewModel.familyDiseaseMap.value ?: mapOf()
            val joined = map.entries.joinToString("; ") { (relation, diseases) ->
                "$relation: ${diseases.joinToString(", ")}"
            }
            viewModel.familyDiseases.value = joined
            progressViewModel.updateProgress(11)
            findNavController().navigate(R.id.action_familyDiseaseFragment_to_createAccount2)
        }
    }

    private fun addFamilyDiseaseCard(relation: String, diseases: List<String>) {
        val inflater = LayoutInflater.from(requireContext())
        val cardView = inflater.inflate(R.layout.chip_family_disease, binding.selectedListContainer, false)

        val relationText = cardView.findViewById<TextView>(R.id.relationText)
        val diseasesText = cardView.findViewById<TextView>(R.id.diseasesText)
        val removeBtn = cardView.findViewById<ImageView>(R.id.btnRemove)

        relationText.text = relation
        diseasesText.text = diseases.joinToString(", ")

        removeBtn.setOnClickListener {
            viewModel.removeFamilyRelation(relation)
        }

        binding.selectedListContainer.addView(cardView)
    }

    private fun showSelectRelationDialog(disease: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_select_relation, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corners) // Apply rounded corners to dialog

        val checkBoxes = listOf(
            dialogView.findViewById<CheckBox>(R.id.checkboxMother),
            dialogView.findViewById<CheckBox>(R.id.checkboxFather),
            dialogView.findViewById<CheckBox>(R.id.checkboxBrother),
            dialogView.findViewById<CheckBox>(R.id.checkboxSister),
            dialogView.findViewById<CheckBox>(R.id.checkboxGrandParent)
        )

        val btnSubmit = dialogView.findViewById<Button>(R.id.btnSubmit)

        checkBoxes.forEach { cb ->
            cb.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Add a checkmark icon when checked
                    cb.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check, 0, 0, 0)  // Assuming you have a checkmark icon (ic_check)
                } else {
                    // Remove the checkmark when unchecked
                    cb.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)  // No icon
                }

                val anyChecked = checkBoxes.any { it.isChecked }

                // Change the background tint color depending on whether any checkbox is selected
                btnSubmit.backgroundTintList = ColorStateList.valueOf(
                    if (anyChecked) ContextCompat.getColor(requireContext(), R.color.primaryColor) // Selected tint
                    else ContextCompat.getColor(requireContext(), R.color.white) // Unselected tint
                )

                // Enable/Disable the button based on the selection
                btnSubmit.isEnabled = anyChecked
            }
        }

        btnSubmit.setOnClickListener {
            val selectedRelations = checkBoxes.filter { it.isChecked }.map { it.text.toString() }
            selectedRelations.forEach { relation ->
                viewModel.addDiseaseForRelation(relation, disease)
            }
            dialog.dismiss()
        }

        dialog.show()
    }
}