package com.criterion.nativevitalio.UI

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.ActivityLoginBinding
import com.criterion.nativevitalio.viewmodel.LoginViewModel

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        // Disable button initially
        binding.sendOtpBtn.isEnabled = false

        // Enable button only when text is valid
        binding.inputField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString().trim()
                val isValid = input.length >= 8

                binding.sendOtpBtn.isEnabled = isValid
                binding.sendOtpBtn.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(this@Login,
                        if (isValid) R.color.primaryBlue else R.color.greyText)
                )
            }
        })

        // ✅ Observe once — not on every button click
        observeViewModel()

        // Button click triggers API call
        binding.sendOtpBtn.setOnClickListener {
            val phoneOrUHID = binding.inputField.text.toString().trim()
            viewModel.getPatientDetailsByUHID(phoneOrUHID)
        }
    }

    private fun observeViewModel() {
        viewModel.loading.observe(this) { isLoading ->
            binding.sendOtpBtn.isEnabled = !isLoading
            // Show/hide loader if needed
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.movieResults.observe(this) { resultList ->
            if (!resultList.isNullOrEmpty()) {
                val userInput = binding.inputField.text.toString().trim()
                val intent = Intent(this, otp::class.java)
                intent.putExtra("user_input", userInput)
                startActivity(intent)
            } else {
                Toast.makeText(this, "No patient data found.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}