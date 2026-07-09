package com.critetiontech.ctvitalio.UI.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.BottomSheetEndSessionBinding
import com.critetiontech.ctvitalio.viewmodel.MindfulnessViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class EndSessionBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetEndSessionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MindfulnessViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetEndSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            // Remove the default background color to preserve our custom drawable background and round corners
            bottomSheet?.setBackgroundResource(android.R.color.transparent)
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set custom title and description if provided
        arguments?.getString(ARG_TITLE)?.let {
            binding.tvTitle.text = it
        }
        arguments?.getString(ARG_DESCRIPTION)?.let {
            binding.tvDescription.text = it
        }

        // Close Button Clicked
        binding.btnClose.setOnClickListener {
            if (viewModel.isLoading.value != true) {
                dismiss()
            }
        }

        // Continue Button Clicked (Trigger API Call)
        binding.btnContinue.setOnClickListener {
            val exerciseId = arguments?.getInt(ARG_EXERCISE_ID) ?: 0
            val duration = arguments?.getInt(ARG_DURATION) ?: 0
            val totalSteps = arguments?.getInt(ARG_TOTAL_STEPS) ?: 0
            val mindfulnessJson = arguments?.getString(ARG_MINDFULNESS_JSON).orEmpty()

            viewModel.insertMindfulness(
                exerciseId = exerciseId,
                duration = duration,
                totalSteps = totalSteps,
                mindfulnessJson = mindfulnessJson
            )
        }

        // End Session Button Clicked (Confirm end without saving)
        binding.btnEndSession.setOnClickListener {
            if (viewModel.isLoading.value != true) {
                sendResultAndDismiss("end")
            }
        }

        setupObservers()
    }

    private fun setupObservers() {
        // Observe loading state to show/hide loading indicator and enable/disable inputs
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnContinue.isEnabled = !isLoading
            binding.btnEndSession.isEnabled = !isLoading
            binding.btnClose.isEnabled = !isLoading
            isCancelable = !isLoading
        }

        // Observe API result
        viewModel.apiResult.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = { response ->
                    if (response.status == 1) {
                        Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                        sendResultAndDismiss("success")
                    } else {
                        Toast.makeText(context, response.message ?: "Failed to save progress", Toast.LENGTH_LONG).show()
                    }
                },
                onFailure = { throwable ->
                    Toast.makeText(context, throwable.message ?: "Network error occurred", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun sendResultAndDismiss(action: String) {
        setFragmentResult(
            REQUEST_KEY,
            bundleOf(EXTRA_ACTION to action)
        )
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "EndSessionBottomSheet"
        const val REQUEST_KEY = "EndSessionRequest"
        const val EXTRA_ACTION = "action"

        private const val ARG_EXERCISE_ID = "arg_exercise_id"
        private const val ARG_DURATION = "arg_duration"
        private const val ARG_TOTAL_STEPS = "arg_total_steps"
        private const val ARG_MINDFULNESS_JSON = "arg_mindfulness_json"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_DESCRIPTION = "arg_description"

        fun newInstance(
            exerciseId: Int,
            duration: Int,
            totalSteps: Int,
            mindfulnessJson: String,
            title: String? = null,
            description: String? = null
        ): EndSessionBottomSheet {
            return EndSessionBottomSheet().apply {
                arguments = Bundle().apply {
                    putInt(ARG_EXERCISE_ID, exerciseId)
                    putInt(ARG_DURATION, duration)
                    putInt(ARG_TOTAL_STEPS, totalSteps)
                    putString(ARG_MINDFULNESS_JSON, mindfulnessJson)
                    putString(ARG_TITLE, title)
                    putString(ARG_DESCRIPTION, description)
                }
            }
        }
    }
}
