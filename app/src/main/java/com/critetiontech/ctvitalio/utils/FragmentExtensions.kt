package com.critetiontech.ctvitalio.utils

import NetworkUtils
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

fun Fragment.showRetrySnackbar(
    message: String = "No Internet Connection",
    retryAction: (() -> Unit)? = null
) {
    var snackbar: Snackbar? = null

    fun showConnectedMessage() {
        val quality = NetworkUtils.getConnectionQuality(requireContext())
        val speed = String.format("%.2f Mbps", NetworkUtils.getDownlinkSpeedMbps(requireContext()))
        val message = "✅ Internet Connected ($quality, $speed)"

        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(Color.parseColor("#4CAF50")) // Green background
            .setTextColor(Color.WHITE)
            .show()
    }

    snackbar = Snackbar.make(requireView(), message, Snackbar.LENGTH_INDEFINITE)
        .setAction("Retry") {
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                snackbar?.dismiss()
                showConnectedMessage()
                retryAction?.invoke()
            } else {
                this.showRetrySnackbar(message, retryAction)
            }
        }

    snackbar.show()
}