package com.criterion.nativevitalio.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.critetiontech.ctvitalio.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody


object ToastUtils {

    fun showSuccess(context: Context, message: String) {
        showCustomToast(context, message, "#4CAF50") // Green
    }

    fun showFailure(context: Context, message: String) {
        showCustomToast(context, message, "#F44336") // Red
    }

    fun showInfo(context: Context, message: String) {
        showCustomToast(context, message, "#2196F3") // Blue
    }


    fun showSuccessPopup(context: Context, message: String) {
        val dialog = Dialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.custom_success_popup, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
      //  dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setCancelable(false)

        val textView = view.findViewById<TextView>(R.id.tvPopupMessage)
        textView.text = message

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) dialog.dismiss()
        }, 2000) // auto-hide in 2 seconds
    }

    @SuppressLint("InflateParams")
    private fun showCustomToast(context: Context, message: String, bgColor: String) {
        val toast = Toast(context)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.custom_toast_layout, null)
        val textView = view.findViewById<TextView>(R.id.toastText)
        textView.text = message
        textView.setBackgroundColor(Color.parseColor(bgColor))

        toast.duration = Toast.LENGTH_SHORT
        toast.view = view
        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
        toast.show()
    }
}

object ErrorUtils {
    fun parseErrorMessage(errorBody: ResponseBody?): String {
        return try {
            val gson = Gson()
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val errorMap: Map<String, Any> = gson.fromJson(errorBody?.charStream(), type)
            errorMap["message"]?.toString() ?: "Something went wrong"
        } catch (e: Exception) {
            "Unable to parse error"
        }
    }
}
