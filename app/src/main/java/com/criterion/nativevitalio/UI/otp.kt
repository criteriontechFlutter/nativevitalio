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
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.UI.fragments.Dashboard
import com.criterion.nativevitalio.databinding.ActivityLoginBinding
import com.criterion.nativevitalio.databinding.ActivityOtpBinding
import com.criterion.nativevitalio.viewmodel.LoginViewModel
import com.criterion.nativevitalio.viewmodel.OtpViewModal

class otp : AppCompatActivity() {

    private lateinit var binding : ActivityOtpBinding
    private lateinit var viewModel: OtpViewModal
    lateinit var storedUHID: String
    lateinit var otptext: String
    var allFilled: Boolean=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=  ActivityOtpBinding.inflate(layoutInflater)
       setContentView(binding.root)

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

//


        viewModel = ViewModelProvider(this)[OtpViewModal::class.java]
        storedUHID = intent.getStringExtra("UHID").toString()
        setupOtpInputs(storedUHID)

        binding.verify.setOnClickListener {
            if (allFilled){
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                viewModel.getPatientDetailsByUHID(storedUHID,"deviceToken",otptext )

            }
        }

    }

    private fun setupOtpInputs(uhid:String) {
        allFilled=false
        val otpFields = listOf(
            binding.otp1, binding.otp2, binding.otp3,
            binding.otp4, binding.otp5, binding.otp6
        )

        for (i in otpFields.indices) {
            otpFields[i].addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && i < otpFields.lastIndex) {
                        otpFields[i + 1].requestFocus()
                    } else if (s?.isEmpty() == true && i > 0) {
                        otpFields[i - 1].requestFocus()
                    }

                    // ✅ Check all fields to enable/disable the button
                      allFilled = otpFields.all { it.text.toString().trim().length == 1
                    }
                    binding.verify.isEnabled = allFilled
                    binding.verify.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this@otp,
                            if (allFilled) R.color.primaryBlue else R.color.greyText
                        ) )

                    otptext = otpFields.joinToString("") { it.text.toString() }

                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
    }

}