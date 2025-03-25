package com.criterion.nativevitalio.UI

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.ActivityLoginBinding

class Login : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }


        binding.sendOtpBtn.isEnabled = false

        binding.inputField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString().trim()
                val isValid = input.length >= 8

                binding.sendOtpBtn.isEnabled = isValid
                binding.sendOtpBtn.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(this@Login, if (isValid) R.color.primaryBlue
                    else R.color.greyText)
                )
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.sendOtpBtn.setOnClickListener {
            val phoneOrUHID = binding.inputField.text.toString().trim()
            val intent = Intent(this, otp::class.java)
            intent.putExtra("user_input", phoneOrUHID)
            startActivity(intent)
        }
    }



}