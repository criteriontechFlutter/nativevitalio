package com.critetiontech.ctvitalio.UI.ui.signupFragment

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.viewmodel.RegistrationViewModel
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentCreateAccountBinding
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale

class CreateAccount : Fragment() {



    private lateinit var binding: FragmentCreateAccountBinding
    private lateinit var viewModel: RegistrationViewModel
    private lateinit var progressViewModel: ProgressViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]
        progressViewModel = ViewModelProvider(requireActivity())[ProgressViewModel::class.java]

        // Initial static setup for always-visible summary items
        addSummaryItem("Name", getFullName())
        addSummaryItem("Gender", viewModel.gender.value ?: "")
        addSummaryItem("Date of Birth", "")
        addSummaryItem("Weight", "")
        addSummaryItem("Height", "")
        addSummaryItem("Blood Group", viewModel.bgId.value ?: "")
        addSummaryItem("Address", "")
        addSummaryItem("Chronic Disease", viewModel.chronicDisease.value ?: "")
        addSummaryItem("Other Chronic Diseases", viewModel.otherChronicDiseases.value ?: "")
        addSummaryItem("Family History", viewModel.familyDiseases.value ?: "")

        // Observers for dynamic updates
        viewModel.firstName.observe(viewLifecycleOwner) {
            addSummaryItem("Name", getFullName())
        }
        viewModel.lastName.observe(viewLifecycleOwner) {
            addSummaryItem("Name", getFullName())
        }

        viewModel.gender.observe(viewLifecycleOwner) {
            addSummaryItem("Gender", it)
        }

        viewModel.bg.observe(viewLifecycleOwner) {
            addSummaryItem("Blood Group", it)
        }

        viewModel.chronicDisease.observe(viewLifecycleOwner) {
            addSummaryItem("Chronic Disease", it)
        }

        viewModel.otherChronicDiseases.observe(viewLifecycleOwner) {
            addSummaryItem("Other Chronic Diseases", it)
        }

        viewModel.familyDiseases.observe(viewLifecycleOwner) {
            addSummaryItem("Family History", it)
        }


        viewModel.wt.observe(viewLifecycleOwner) { weight ->
            // Check if weight is not empty or null, and if it is, display "kg"
            val weightText = if (!weight.isNullOrEmpty()) {
                "$weight kg"
            } else {
                "" // Display just "kg" if the weight is empty or null
            }
            addSummaryItem("Weight", weightText)
        }
        viewModel.ht.observe(viewLifecycleOwner) {
            addSummaryItem("Height", it)
        }

        viewModel.selectedCountryName.observe(viewLifecycleOwner) { updateAddressSummary() }
        viewModel.selectedStateName.observe(viewLifecycleOwner) { updateAddressSummary() }
        viewModel.selectedCityName.observe(viewLifecycleOwner) { updateAddressSummary() }
        viewModel.pinCode.observe(viewLifecycleOwner) { updateAddressSummary() }
        viewModel.streetAddress.observe(viewLifecycleOwner) { updateAddressSummary() }

        // DOB format & display
        viewModel.dob.observe(viewLifecycleOwner) { rawDate ->
            val formatted = try {
                val inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                val dob = LocalDate.parse(rawDate, inputFormatter)
                val outputFormatter = DateTimeFormatter.ofPattern("dd. MMMM yyyy", Locale.ENGLISH)
                val formattedDate = dob.format(outputFormatter)

                val period = Period.between(dob, LocalDate.now())
                val years = period.years
                val months = period.months
                val days = period.days

                "$formattedDate ($years Years, $months Months, $days Days)"
            } catch (e: Exception) {
                rawDate
            }
            addSummaryItem("Date of Birth", formatted)
        }

        // Trigger DOB display if already filled
        viewModel.dob.value?.let {
            viewModel.dob.postValue(it)
        }

        binding.btnNext.setOnClickListener(){
            viewModel.patientSignUp()
            progressViewModel.updateProgress(9)
            progressViewModel.updatepageNo(11)
            progressViewModel.setNottoSkipButtonVisibility(true)
            progressViewModel.updateisNotBack(true)

            progressViewModel.updateIsDashboard(true)
            progressViewModel.updateIsHideProgressBar(true)

            findNavController().navigate(R.id.accountSuccess)
        }
//        binding.s.setOnClickListener(){
//        val intent = Intent(context, Home::class.java)
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        context.startActivity(intent)   }
    }
    private fun getFullName(): String {
        val firstName = viewModel.firstName.value ?: ""
        val lastName = viewModel.lastName.value ?: ""
        return "$firstName $lastName".trim()
    }

    private fun updateAddressSummary() {
        val country = viewModel.selectedCountryName.value ?: ""
        val state = viewModel.selectedStateName.value ?: ""
        val city = viewModel.selectedCityName.value ?: ""
        val pin = viewModel.pinCode.value ?: ""
        val street = viewModel.streetAddress.value ?: ""

        val fullAddress = listOf(street, city, state, pin, country)
            .filter { it.isNotBlank() }
            .joinToString(", ")
            .ifBlank { "Not provided" }

        addSummaryItem("Address", fullAddress)
    }

//    private fun addSummaryItem(label: String, value: String) {
//        val existingView = binding.summaryContainer.findViewWithTag<View>(label)
//        if (existingView != null) {
//            existingView.findViewById<TextView>(R.id.value).text = value.ifBlank { "Not provided" }
//        } else {
//            val itemView = LayoutInflater.from(requireContext())
//                .inflate(R.layout.item_summary_field, binding.summaryContainer, false)
//
//            itemView.tag = label
//            itemView.findViewById<TextView>(R.id.label).text = label
//            itemView.findViewById<TextView>(R.id.value).text = value.ifBlank { "Not provided" }
//
//            itemView.findViewById<ImageView>(R.id.editIcon)?.setOnClickListener {
//                when (label) {
//                    "Name" -> findNavController().navigate(R.id.action_createAccount2_to_nameFragment)
//                    "Gender" -> findNavController().navigate(R.id.action_createAccount2_to_genderFragment)
//                    "Date of Birth" -> findNavController().navigate(R.id.action_createAccount2_to_dobFragment)
//                    "Blood Group" -> findNavController().navigate(R.id.action_createAccount2_to_bloodGroupFragment)
//                    "Address" -> findNavController().navigate(R.id.action_createAccount2_to_adressFragment)
//                    "Chronic Disease" -> findNavController().navigate(R.id.action_createAccount2_to_chronicConditionFragment)
//                    "Other Chronic Diseases" -> findNavController().navigate(R.id.action_createAccount2_to_otherChronicDisease)
//                    "Family History" -> findNavController().navigate(R.id.action_createAccount2_to_familyDiseaseFragment)
//                    "Weight" -> findNavController().navigate(R.id.action_createAccount2_to_weightFragment)
//                    "Height" -> findNavController().navigate(R.id.action_createAccount2_to_heightFragment)
//                    else -> Toast.makeText(requireContext(), "No edit path for $label", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            binding.summaryContainer.addView(itemView)
//        }
//    }\
private fun addSummaryItem(label: String, value: String) {
    // Check if the summary item already exists
    val existingView = binding.summaryContainer.findViewWithTag<View>(label)
    val valueTextView: TextView
    val iconImageView: ImageView

    // If the item exists, retrieve references to the views
    if (existingView != null) {
        valueTextView = existingView.findViewById(R.id.value)
        iconImageView = existingView.findViewById(R.id.createeditIcon)

        // Update the text and color based on whether the value is blank or not
        updateSummaryItem(valueTextView, iconImageView, label, value)
    } else {
        // Inflate new item if it doesn't exist
        val itemView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_summary_field, binding.summaryContainer, false)

        // Set the label and tag for the item
        itemView.tag = label
        itemView.findViewById<TextView>(R.id.label).text = label

        // Get references to the views
        valueTextView = itemView.findViewById(R.id.value)
        iconImageView = itemView.findViewById(R.id.createeditIcon)

        // Update the text and color based on whether the value is blank or not
        updateSummaryItem(valueTextView, iconImageView, label, value)

        // Set click listener for the edit icon
        itemView.findViewById<ImageView>(R.id.createeditIcon)?.setOnClickListener {
            navigateToEditFragment(label)
        }


        // Add the new item to the container
        binding.summaryContainer.addView(itemView)
    }
}

    private fun updateSummaryItem(valueTextView: TextView, iconImageView: ImageView, label: String, value: String) {
        // Set the value and color for the text
        valueTextView.text = if (value.isBlank()) {
            "--No $label provided.--"
        } else {
            value
        }
        valueTextView.setTextColor(if (value.isBlank()) Color.RED else Color.BLACK)

        // Decrease font size and make italic if value is blank
        if (value.isBlank()) {
            valueTextView.setTextSize(12f)  // Smaller font size
            valueTextView.setTypeface(null, android.graphics.Typeface.ITALIC)  // Italic font
            iconImageView.setImageResource(R.drawable.edit_icon)  // Show "edit" icon when value is blank
        } else {
            valueTextView.setTextSize(16f)  // Default font size
            valueTextView.setTypeface(null, android.graphics.Typeface.NORMAL)  // Normal font style
            iconImageView.setImageResource(R.drawable.edit)  // Show "checked" icon when value is provided
        }
    }

    private fun navigateToEditFragment(label: String) {
        when (label) {
            "Name" -> findNavController().navigate(R.id.action_createAccount2_to_nameFragment)
            "Gender" -> findNavController().navigate(R.id.action_createAccount2_to_genderFragment)
            "Date of Birth" -> findNavController().navigate(R.id.action_createAccount2_to_dobFragment)
            "Blood Group" -> findNavController().navigate(R.id.action_createAccount2_to_bloodGroupFragment)
            "Address" -> findNavController().navigate(R.id.action_createAccount2_to_adressFragment)
            "Chronic Disease" -> findNavController().navigate(R.id.action_createAccount2_to_chronicConditionFragment)
            "Other Chronic Diseases" -> findNavController().navigate(R.id.action_createAccount2_to_otherChronicDisease)
            "Family History" -> findNavController().navigate(R.id.action_createAccount2_to_familyDiseaseFragment)
            "Weight" -> findNavController().navigate(R.id.action_createAccount2_to_weightFragment)
            "Height" -> findNavController().navigate(R.id.action_createAccount2_to_heightFragment)
            else -> Toast.makeText(requireContext(), "No edit path for $label", Toast.LENGTH_SHORT).show()
        }
    }
}