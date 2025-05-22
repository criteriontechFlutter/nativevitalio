package com.critetiontech.ctvitalio.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.critetiontech.ctvitalio.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ConfirmationBottomSheet(
    private val message: String,
    private val onConfirm: () -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.confirmation_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.tvMessage).text = message
        view.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            onConfirm.invoke()
            dismiss()
        }

        view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dismiss()
        }
    }
}