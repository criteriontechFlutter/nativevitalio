package com.critetiontech.ctvitalio.UI.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.critetiontech.ctvitalio.databinding.DialogConfirmUpdateBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ConfirmUpdateBottomSheet(
    private val title: String,
    private val message: String,
    private val btnText: String,
    private val onConfirm: () -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: DialogConfirmUpdateBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogConfirmUpdateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set data
        binding.title.text = title
        binding.message.text = message
        binding.changePassBtn.text = btnText

        binding.changePassBtn.setOnClickListener {
            onConfirm()
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()

        // Expand full width & show from bottom
        dialog?.let {
            val bottomSheet =
                it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }

    companion object {
        const val TAG = "ConfirmUpdateBottomSheet"
    }
}
