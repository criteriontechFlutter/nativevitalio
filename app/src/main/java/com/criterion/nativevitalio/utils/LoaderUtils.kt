package com.criterion.nativevitalio.utils

import android.app.Activity
import android.app.Dialog
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.critetiontech.ctvitalio.R

object LoaderUtils {
    private var dialog: Dialog? = null

    fun Fragment.showLoading(message: String = "Loading, please wait...") {
        if (dialog?.isShowing == true) return
        dialog = Dialog(requireContext()).apply {
            setContentView(R.layout.dialog_loader)
            setCancelable(false)
            window?.setBackgroundDrawableResource(android.R.color.transparent)

            findViewById<TextView>(R.id.loadingText)?.text = message
            show()
        }
    }

    fun Fragment.hideLoading() {
        dialog?.dismiss()
        dialog = null
    }

    fun Activity.showLoading(message: String = "Loading, please wait...") {
        if (dialog?.isShowing == true) return
        dialog = Dialog(this).apply {
            setContentView(R.layout.dialog_loader)
            setCancelable(false)
            window?.setBackgroundDrawableResource(android.R.color.transparent)

            findViewById<TextView>(R.id.loadingText)?.text = message
            show()
        }
    }

    fun Activity.hideLoading() {
        dialog?.dismiss()
        dialog = null
    }
}
