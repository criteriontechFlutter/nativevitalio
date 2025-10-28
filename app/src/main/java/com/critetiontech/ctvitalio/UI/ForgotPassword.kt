package com.critetiontech.ctvitalio.UI

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.ActivityForgotPasswordBinding
import com.critetiontech.ctvitalio.databinding.ActivityLoginBinding
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.viewmodel.ForgotPasswordViewModel
import com.critetiontech.ctvitalio.viewmodel.LoginViewModel
import com.critetiontech.ctvitalio.viewmodel.RegistrationViewModel

class ForgotPassword : BaseActivity() {


    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var viewModel: ForgotPasswordViewModel

    val context = MyApplication.appContext


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSystemBarsColor(
            statusBarColor = R.color.forgotPassword,  // or custom color
            navBarColor = android.R.color.white,
            lightIcons = true
        )
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[ForgotPasswordViewModel::class.java]

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.inputField.addTextChangedListener(textWatcher)
        binding.sendResetLink.setOnClickListener(){
            viewModel.forgotPassword(context, username = binding.inputField.text.toString())

        }

    }
    private fun updateButtonState() {
        val employeeId = binding.inputField.text.toString().trim()
        val isValid = employeeId.isNotEmpty()
        binding.sendResetLink.isEnabled = isValid
        binding.sendResetLink.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(this, if (isValid) R.color.primaryBlue else R.color.greyText)
        )
    }
}