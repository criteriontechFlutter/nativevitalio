package com.critetiontech.ctvitalio.utils

import android.graphics.Color
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar

fun DialogFragment.showSafe(manager: FragmentManager, tag: String? = null) {
    if (manager.isStateSaved || manager.isDestroyed) return
    try {
        val tagName = tag ?: this::class.java.simpleName
        val existingFragment = manager.findFragmentByTag(tagName)
        if (existingFragment != null && existingFragment.isAdded) {
            return
        }
        val ft = manager.beginTransaction()
        ft.add(this, tagName)
        ft.commitAllowingStateLoss()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * FragmentExtensions.kt
 *
 * Extension functions for Fragment — UI feedback helpers.
 */

/**
 * Shows a persistent "No Internet" snackbar with a Retry action.
 * When the user taps Retry:
 *  - If internet is available, dismisses the snackbar and calls [retryAction].
 *  - If still offline, re-shows the snackbar.
 */
fun Fragment.showRetrySnackbar(
    message: String = AppConstants.ERROR_NO_INTERNET,
    retryAction: (() -> Unit)? = null
) {
    var snackbar: Snackbar? = null

    snackbar = Snackbar.make(requireView(), message, Snackbar.LENGTH_INDEFINITE)
        .setAction("Retry") {
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                snackbar?.dismiss()
                showConnectedSnackbar()
                retryAction?.invoke()
            } else {
                showRetrySnackbar(message, retryAction)
            }
        }

    snackbar.show()
}

/** Shows a brief "Connected" confirmation snackbar. */
fun Fragment.showConnectedSnackbar() {
    val quality = NetworkUtils.getConnectionQuality(requireContext())
    val speed   = "%.2f Mbps".format(NetworkUtils.getDownlinkSpeedMbps(requireContext()))
    Snackbar.make(requireView(), "Internet Connected ($quality, $speed)", Snackbar.LENGTH_SHORT)
        .setBackgroundTint(Color.parseColor("#4CAF50"))
        .setTextColor(Color.WHITE)
        .show()
}
