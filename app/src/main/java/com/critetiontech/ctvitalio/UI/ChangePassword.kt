package com.critetiontech.ctvitalio.UI

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.ActivityChangePasswordBinding
import com.critetiontech.ctvitalio.databinding.ActivityForgotPasswordBinding
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.viewmodel.ChangePasswordViewModel
import com.critetiontech.ctvitalio.viewmodel.ForgotPasswordViewModel

class ChangePassword : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var viewModel: ChangePasswordViewModel

    val context = MyApplication.appContext


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[ChangePasswordViewModel::class.java]
        var isCurrentPasswordVisible = false

        binding.currentPassField.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2  // Index for drawableRight
                val drawableWidth = binding.currentPassField.compoundDrawables[drawableEnd]?.bounds?.width() ?: 0
                if (event.rawX >= (binding.currentPassField.right - drawableWidth)) {
                    isCurrentPasswordVisible = !isCurrentPasswordVisible

                    // Save current typeface
                    val typeface = binding.currentPassField.typeface

                    // Change input type
                    binding.currentPassField.inputType = if (isCurrentPasswordVisible) {
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    } else {
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    }

                    // Re-apply typeface to prevent font shrinking
                    binding.currentPassField.typeface = typeface

                    // Preserve cursor position
                    binding.currentPassField.setSelection(binding.currentPassField.text.length)

                    // Toggle icon
                    val icon = if (isCurrentPasswordVisible) R.drawable.close_eye else R.drawable.open_eye
                    binding.currentPassField.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, icon, 0)

                    return@setOnTouchListener true
                }
            }
            false
        }

        var isNewPasswordVisible = false


        binding.newPassField.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2  // Index for drawableRight
                val drawableWidth = binding.newPassField.compoundDrawables[drawableEnd]?.bounds?.width() ?: 0
                if (event.rawX >= (binding.newPassField.right - drawableWidth)) {
                    isNewPasswordVisible = !isNewPasswordVisible

                    // Preserve typeface before changing inputType
                    val typeface = binding.newPassField.typeface

                    // Change input type
                    binding.newPassField.inputType = if (isNewPasswordVisible) {
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    } else {
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    }

                    // Reapply typeface to maintain font appearance
                    binding.newPassField.typeface = typeface

                    // Preserve cursor position
                    binding.newPassField.setSelection(binding.newPassField.text.length)

                    // Toggle eye icon
                    val icon = if (isNewPasswordVisible) R.drawable.close_eye else R.drawable.open_eye
                    binding.newPassField.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, icon, 0)

                    return@setOnTouchListener true
                }
            }
            false
        }


        binding.submitBtn.setOnClickListener(){
            viewModel.changePassword(
                context = context,
                oldPassword = binding.currentPassField.text.toString(),
                newPassword = binding.newPassField.text.toString(),
                confirmNewPassword = binding.confirmPassField.text.toString(),
            )
        }

    }
}