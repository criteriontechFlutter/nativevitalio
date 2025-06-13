package com.critetiontech.ctvitalio.UI.ui.signupFragment


import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentSignUp2Binding
import com.critetiontech.ctvitalio.viewmodel.RegistrationViewModel


class SignUpFragment2 : Fragment() {
    private lateinit var binding: FragmentSignUp2Binding
    private lateinit var progressViewModel: ProgressViewModel
    private lateinit var viewModel: RegistrationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSignUp2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]
        progressViewModel = ViewModelProvider(requireActivity())[ProgressViewModel::class.java]
        // ✅ Restore existing values if already entered
        binding.etFirstName.setText(viewModel.firstName.value ?: "")
        binding.etLastName.setText(viewModel.lastName.value ?: "")
        val nameFilter = InputFilter { source, _, _, _, _, _ ->
            val regex = Regex("[a-zA-Z ]") // Allow letters and space only
            if (source.isEmpty()) return@InputFilter null // Allow backspace

            val filtered = source.filter { it.toString().matches(regex) }
            if (filtered == source) null else filtered
        }
        binding.etFirstName.filters = arrayOf(nameFilter)
        binding.etLastName.filters = arrayOf(nameFilter)
        binding.btnNext.setOnClickListener {
            val first = binding.etFirstName.text.toString().trim()
            val last = binding.etLastName.text.toString().trim()

            if (first.isEmpty()) {

                showTooltip( binding.etFirstName, "Please enter your name\nEnter your full legal name as it appears on official documents.");
                return@setOnClickListener
            }

            // ✅ Save values in ViewModel
            viewModel.firstName.value = first
            viewModel.lastName.value = last

            // ✅ Update progress
            progressViewModel.updateProgress(1)
            progressViewModel.updatepageNo(1)

            // ✅ Navigate to next fragment
            findNavController().navigate(R.id.action_nameFragment_to_genderFragment)
        }
    }

    private fun showTooltip(anchorView: View, message: String) {
        // Create a TextView to serve as the tooltip content
        val tooltipView = TextView(context)
        tooltipView.text = message
        tooltipView.setTextColor(Color.WHITE)
        tooltipView.setBackgroundResource(R.drawable.tooltip_background)
        tooltipView.textSize = 14f
        tooltipView.maxWidth = 600
        tooltipView.setPadding(20, 20, 20, 20)

        // Create the PopupWindow
        val popup = PopupWindow(
            tooltipView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Optional: Remove default elevation/shadow, or adjust it
        popup.elevation = 8f

        // Get position of anchor and display above
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)
        val xOffset = 0
        val yOffset = -anchorView.height * 2

        popup.showAtLocation(
            anchorView,
            Gravity.NO_GRAVITY,
            location[0] + xOffset,
            location[1] + yOffset
        )
    }


}