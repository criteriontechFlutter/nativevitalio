package com.critetiontech.ctvitalio.UI

import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.ActivityForgotPasswordBinding
import com.critetiontech.ctvitalio.databinding.ActivityResetPasswordBinding
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.viewmodel.ForgotPasswordViewModel
import com.critetiontech.ctvitalio.viewmodel.ResetPasswordViewModel

class ResetPassword : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var viewModel: ResetPasswordViewModel

    val context = MyApplication.appContext


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[ResetPasswordViewModel::class.java]

        val context = MyApplication.appContext

        var isNewPasswordVisible = false


        binding.inputField.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2  // Index for drawableRight
                val drawableWidth = binding.inputField.compoundDrawables[drawableEnd]?.bounds?.width() ?: 0
                if (event.rawX >= (binding.inputField.right - drawableWidth)) {
                    isNewPasswordVisible = !isNewPasswordVisible

                    // Preserve typeface before changing inputType
                    val typeface = binding.inputField.typeface

                    // Change input type
                    binding.inputField.inputType = if (isNewPasswordVisible) {
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    } else {
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    }

                    // Reapply typeface to maintain font appearance
                    binding.inputField.typeface = typeface

                    // Preserve cursor position
                    binding.inputField.setSelection(binding.inputField.text.length)

                    // Toggle eye icon
                    val icon = if (isNewPasswordVisible) R.drawable.close_eye else R.drawable.open_eye
                    binding.inputField.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, icon, 0)

                    return@setOnTouchListener true
                }
            }
            false
        }
        binding.submitBtn.setOnClickListener(){
            viewModel.resetPassword(
                context = context,
                newPassword = binding.inputField.text.toString(),
                confirmNewPassword = binding.confirmPasswordField.text.toString(),

            )
        }


    }
}