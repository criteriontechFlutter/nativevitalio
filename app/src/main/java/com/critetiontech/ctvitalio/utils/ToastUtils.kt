package com.critetiontech.ctvitalio.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.critetiontech.ctvitalio.R
import Vital
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import java.time.LocalTime

/**
 * ToastUtils.kt
 *
 * UI feedback utilities: custom toasts, success popups, greeting helpers.
 * Also provides [ErrorUtils] for parsing HTTP error responses.
 *
 * View binding helpers (bindVitalView, applyStatusStyle, dpToPx) have been
 * consolidated here but should ideally move to a dedicated ViewExtensions.kt
 * as the codebase matures.
 */
object ToastUtils {

    fun showSuccess(context: Context, message: String) =
        showCustomToast(context, message, "#4CAF50") // Green

    fun showFailure(context: Context, message: String) =
        showCustomToast(context, message, "#F44336") // Red

    fun showInfo(context: Context, message: String) =
        showCustomToast(context, message, "#2196F3") // Blue

    @RequiresApi(Build.VERSION_CODES.O)
    fun getSimpleGreeting(): String {
        return when (LocalTime.now().hour) {
            in 5..11  -> "Good Morning"
            in 12..17 -> "Good Afternoon"
            in 18..21 -> "Good Evening"
            else      -> "Good Night"
        }
    }

    fun showSuccessPopup(context: Context, message: String) {
        val dialog = Dialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.custom_success_popup, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        view.findViewById<TextView>(R.id.tvPopupMessage).text = message
        dialog.show()

        // Auto-dismiss after 2 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) dialog.dismiss()
        }, 2000)
    }

    @SuppressLint("InflateParams")
    private fun showCustomToast(context: Context, message: String, bgColor: String) {
        val toast = Toast(context)
        val view = LayoutInflater.from(context).inflate(R.layout.custom_toast_layout, null)
        view.findViewById<TextView>(R.id.toastText).apply {
            text = message
            setBackgroundColor(Color.parseColor(bgColor))
        }
        toast.duration = Toast.LENGTH_SHORT
        @Suppress("DEPRECATION")
        toast.view = view
        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
        toast.show()
    }
}

// ─── View extension helpers ───────────────────────────────────────────────────

fun TextView.applyStatusStyle(
    rawColor: String?,
    getPastelColor: (String) -> Int,
    cornerRadiusDp: Float = 12f,
    paddingHorizontalDp: Float = 10f,
    paddingVerticalDp: Float = 4f
) {
    if (rawColor.isNullOrEmpty()) return
    val colorCode = if (rawColor.startsWith("#")) rawColor else "#$rawColor"

    background = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(getPastelColor(colorCode))
        cornerRadius = dpToPx(cornerRadiusDp)
    }
    setTextColor(Color.parseColor(colorCode))
    val hPad = dpToPx(paddingHorizontalDp).toInt()
    val vPad = dpToPx(paddingVerticalDp).toInt()
    setPadding(hPad, vPad, hPad, vPad)
}

fun dpToPx(dp: Float): Float =
    dp * android.content.res.Resources.getSystem().displayMetrics.density

/**
 * Binds vital data to a standard vital card (title + formatted text value + severity badge).
 * Use this overload when displaying [Vital.vmValueText].
 */
fun bindVitalView(
    vitalName: String,
    vital: Vital?,
    titleView: TextView,
    valueView: TextView,
    statusView: TextView,
    statusCardView: android.view.View,
    getPastelColor: (String) -> Int
) {
    titleView.text = vitalName
    valueView.text = vital?.vmValueText.orEmpty()
    applyVitalStatusBadge(vital, statusView, statusCardView, getPastelColor)
}

/**
 * Binds vital data using [Vital.vitalValue] (raw numeric) instead of [Vital.vmValueText].
 */
fun bindVitalViewWithRawValue(
    vitalName: String,
    vital: Vital?,
    titleView: TextView,
    valueView: TextView,
    statusView: TextView,
    statusCardView: android.view.View,
    getPastelColor: (String) -> Int
) {
    titleView.text = vitalName
    valueView.text = vital?.vitalValue?.toString().orEmpty()
    applyVitalStatusBadge(vital, statusView, statusCardView, getPastelColor)
}

/** Shared severity-badge logic for both bindVitalView overloads. */
private fun applyVitalStatusBadge(
    vital: Vital?,
    statusView: TextView,
    statusCardView: android.view.View,
    getPastelColor: (String) -> Int
) {
    if (!vital?.severityLevel.isNullOrEmpty()) {
        statusCardView.visibility = android.view.View.VISIBLE
        statusView.applyStatusStyle(vital!!.vitalColor, getPastelColor)
        statusView.text = vital.severityLevel
    } else {
        statusCardView.visibility = android.view.View.GONE
    }
}

// ─── HTTP Error parsing ───────────────────────────────────────────────────────

object ErrorUtils {
    fun parseErrorMessage(errorBody: ResponseBody?): String {
        return try {
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val errorMap: Map<String, Any> = Gson().fromJson(errorBody?.charStream(), type)
            errorMap["message"]?.toString() ?: AppConstants.ERROR_UNKNOWN
        } catch (e: Exception) {
            AppConstants.ERROR_UNABLE_TO_PARSE
        }
    }
}
