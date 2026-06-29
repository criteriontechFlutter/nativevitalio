package com.critetiontech.ctvitalio.utils

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.critetiontech.ctvitalio.databinding.BottomSheetFocusShiftEndBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FocusShiftEndBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFocusShiftEndBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFocusShiftEndBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheet?.setBackgroundResource(android.R.color.transparent)
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnClose.setOnClickListener {
            sendResultAndDismiss("continue")
        }

        binding.btnContinue.setOnClickListener {
            sendResultAndDismiss("continue")


        }

        binding.btnEndSession.setOnClickListener {
            sendResultAndDismiss("end")
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
        const val TAG = "FocusShiftEndBottomSheet"
        const val REQUEST_KEY = "FocusShiftEndRequest"
        const val EXTRA_ACTION = "action"
    }
}
