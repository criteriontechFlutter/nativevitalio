package com.critetiontech.ctvitalio.UI

import android.content.Intent
import android.graphics.BlurMaskFilter
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.ui.ConfirmUpdateBottomSheet
import com.critetiontech.ctvitalio.databinding.ActivityResetPasswordBinding
import com.critetiontech.ctvitalio.utils.MyApplication
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


        binding.doctorsImage.apply {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null) // disable hardware accel for blur

            val paint = Paint()
            paint.color = Color.WHITE
            paint.maskFilter = BlurMaskFilter(30f, BlurMaskFilter.Blur.NORMAL)

            setLayerType(View.LAYER_TYPE_SOFTWARE, paint)
        }

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

        binding.confirmPasswordField.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2  // Index for drawableRight
                val drawableWidth = binding.inputField.compoundDrawables[drawableEnd]?.bounds?.width() ?: 0
                if (event.rawX >= (binding.inputField.right - drawableWidth)) {
                    isNewPasswordVisible = !isNewPasswordVisible

                    // Preserve typeface before changing inputType
                    val typeface = binding.inputField.typeface

                    // Change input type
                    binding.confirmPasswordField.inputType = if (isNewPasswordVisible) {
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    } else {
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    }

                    // Reapply typeface to maintain font appearance
                    binding.confirmPasswordField.typeface = typeface

                    // Preserve cursor position
                    binding.confirmPasswordField.setSelection(binding.inputField.text.length)

                    // Toggle eye icon
                    val icon = if (isNewPasswordVisible) R.drawable.close_eye else R.drawable.open_eye
                    binding.confirmPasswordField.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, icon, 0)

                    return@setOnTouchListener true
                }
            }
            false
        }




        viewModel.loginSuccess.observe(this) { success ->
            if (success) {
                ConfirmUpdateBottomSheet(
                    title = "Password updated successfully.",
                    message = "Next, let's set up your profile to get started.",
                    btnText = "Start Profile Setup",
                    onConfirm = {

                        val intent = Intent(context, UltraHumanActivity::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)

                    },

                    ).show(supportFragmentManager, ConfirmUpdateBottomSheet.TAG)
            } else {
            }
        }

        binding.submitBtn.setOnClickListener(){
            val oldPassword = intent.getStringExtra("oldPassword")

            viewModel.resetPassword(
                context = context,
                oldPassword,
                newPassword = binding.inputField.text.toString(),
                confirmNewPassword = binding.confirmPasswordField.text.toString(),
            )
        }


    }
}