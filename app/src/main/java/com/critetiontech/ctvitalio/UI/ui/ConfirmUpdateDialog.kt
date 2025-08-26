package com.critetiontech.ctvitalio.UI.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.critetiontech.ctvitalio.databinding.DialogConfirmUpdateBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ConfirmUpdateDialog(
    private val title: String,
    private val message: String,
    private val btnText: String,
    private val onConfirm: () -> Unit,
    private val onCancel: () -> Unit = {}
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val binding = DialogConfirmUpdateBinding.inflate(inflater)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
        dialog.setCanceledOnTouchOutside(false)
        isCancelable = false
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)


        // ✅ Set message dynamically
        binding.message.text = title
        binding.message.text = message
        binding.changePassBtn.text = btnText

        // Confirm button
        binding.changePassBtn.setOnClickListener {
            onConfirm()
            dismiss()
        }

        return dialog
    }

    companion object {
        const val TAG = "ConfirmUpdateDialog"
    }
}
