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
import android.widget.LinearLayout
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
                latestSuggestions = problemList.map { capitalizeFirstLetter(it.problemName) }
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
            progressViewModel.updateProgress(9)
            progressViewModel.updatepageNo(10)
            progressViewModel.setNottoSkipButtonVisibility(true)
            findNavController().navigate(R.id.action_familyDiseaseFragment_to_createAccount2)
        }
    }
    private fun capitalizeFirstLetter(sentence: String): String {
        return sentence.trim().replaceFirstChar {
            if (it.isLowerCase()) it.titlecaseChar() else it
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
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corners)

        val btnSubmit = dialogView.findViewById<Button>(R.id.btnSubmit)

        val relations = listOf(
            Triple("Mother", R.id.checkboxMother, R.id.ivCheckMother),
            Triple("Father", R.id.checkboxFather, R.id.ivCheckFather),
            Triple("Brother", R.id.checkboxBrother, R.id.ivCheckBrother),
            Triple("Sister", R.id.checkboxSister, R.id.ivCheckSister),
            Triple("Grand Parent", R.id.checkboxGrandParent, R.id.ivCheckGrandParent)
        )

        // 🔄 Always start with empty selection
        val selectedRelations = mutableSetOf<String>()

        // 🔁 Initialize each item with unchecked state and click behavior
        relations.forEach { (name, layoutId, iconId) ->
            val layout = dialogView.findViewById<LinearLayout>(layoutId)
            val icon = dialogView.findViewById<ImageView>(iconId)

            // ✅ Set icon to unchecked initially
            icon.setImageResource(R.drawable.check_box)

            // 🧠 Listener to toggle selection state
            layout.setOnClickListener {
                val isSelected = selectedRelations.contains(name)

                if (isSelected) {
                    selectedRelations.remove(name)
                    icon.setImageResource(R.drawable.check_box)// unchecked
                } else {
                    selectedRelations.add(name)
                    icon.setImageResource(R.drawable.check_icon_box)
                }

                // Enable submit if any selected
                btnSubmit.isEnabled = selectedRelations.isNotEmpty()
                val tintColor = if (selectedRelations.isNotEmpty()) R.color.primaryColor else R.color.white
                btnSubmit.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), tintColor))
            }
        }

        // Submit button logic
        btnSubmit.setOnClickListener {
            selectedRelations.forEach { relation ->
                viewModel.addDiseaseForRelation(relation, disease)
            }
            dialog.dismiss()
        }

        // ❗ Start with disabled button
        btnSubmit.isEnabled = false
        btnSubmit.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))

        dialog.show()
    }
}