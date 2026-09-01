package com.critetiontech.ctvitalio.utils

import android.app.Activity
import android.app.Dialog
import androidx.fragment.app.Fragment
import com.critetiontech.ctvitalio.R

/**
 * LoaderUtils.kt
 *
 * Extension functions for showing and hiding a full-screen loading dialog
 * from Activities and Fragments.
 *
 * Fragment variant: stores the dialog internally so callers can use
 * showLoading() / hideLoading() without tracking Dialog references.
 */
object LoaderUtils {

    // ─── Fragment variants ────────────────────────────────────────────────────

    // Each Fragment stores its own dialog reference using tags
    private val fragmentDialogs = mutableMapOf<Int, Dialog?>()

    fun Fragment.showLoading(): Dialog? {
        if (!isAdded) return null
        return Dialog(requireContext()).apply {
            setContentView(R.layout.dialog_loader)
            setCancelable(false)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            show()
        }.also { fragmentDialogs[hashCode()] = it }
    }

    fun Fragment.hideLoading(dialog: Dialog? = fragmentDialogs.remove(hashCode())) {
        dialog?.dismiss()
    }

    // ─── Activity variants ────────────────────────────────────────────────────

    fun Activity.showLoading(): Dialog {
        return Dialog(this).apply {
            setContentView(R.layout.dialog_loader)
            setCancelable(false)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            show()
        }
    }

    fun Activity.hideLoading(dialog: Dialog? = null) {
        dialog?.dismiss()
    }
}
