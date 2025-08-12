package com.critetiontech.ctvitalio.UI

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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

class ForgotPassword : AppCompatActivity() {


    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var viewModel: ForgotPasswordViewModel

    val context = MyApplication.appContext


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[ForgotPasswordViewModel::class.java]


        binding.sendResetLink.setOnClickListener(){
            viewModel.forgotPassword(context, username = binding.inputField.text.toString())

        }

    }  
}