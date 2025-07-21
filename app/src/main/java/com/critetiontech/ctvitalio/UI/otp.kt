package com.critetiontech.ctvitalio.UI

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.critetiontech.ctvitalio.viewmodel.OtpViewModal
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.ActivityOtpBinding
import com.critetiontech.ctvitalio.utils.LoaderUtils.hideLoading
import com.critetiontech.ctvitalio.utils.LoaderUtils.showLoading

class otp : AppCompatActivity() {

    private lateinit var binding : ActivityOtpBinding
    private lateinit var viewModel: OtpViewModal
    lateinit var storedUHID: String
    lateinit var mobileNo: String
    lateinit var isRegistered: String
    lateinit var otptext: String
    var allFilled: Boolean=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=  ActivityOtpBinding.inflate(layoutInflater)
       setContentView(binding.root)

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

//


        viewModel = ViewModelProvider(this)[OtpViewModal::class.java]

        viewModel.loading.observe(this) { isLoading ->
            if (isLoading) showLoading() else hideLoading()
        }
        storedUHID = intent.getStringExtra("UHID").toString()
        mobileNo = intent.getStringExtra("mobileNo").toString()
        isRegistered = intent.getStringExtra("isRegistered").toString()

        Log.d("RESPONSE", "phoneOrUHID4$mobileNo")
        setupOtpInputs(storedUHID)

        binding.verify.setOnClickListener {
            if (allFilled){

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                viewModel.getPatientDetailsByUHID(uhid=storedUHID,
                    deviceToken="deviceToken",isRegistered=isRegistered
                    ,otp=otptext ,context=applicationContext,
                    mNo = mobileNo.toString())

            }
        }

        binding.legalLinks.setOnClickListener {
            viewModel.sentLogInOTPForSHFCApp(storedUHID)
        }


        binding.loginSubtitle.text= "Verification code sent to your Mobile $mobileNo"

    }

    private fun setupOtpInputs(uhid: String) {
        allFilled = false
        val otpFields = listOf(
            binding.otp1, binding.otp2, binding.otp3,
            binding.otp4, binding.otp5, binding.otp6
        )

        for (i in otpFields.indices) {
            val editText = otpFields[i]

            // Handle backspace key explicitly
            editText.setOnKeyListener { v, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (editText.text.isEmpty() && i > 0) {
                        otpFields[i - 1].setText("") // Clear previous field
                        otpFields[i - 1].requestFocus()
                        return@setOnKeyListener true
                    }
                }
                false
            }

            // Regular text watcher for forward focus and button state
            editText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && i < otpFields.lastIndex) {
                        otpFields[i + 1].requestFocus()
                    }

                    allFilled = otpFields.all { it.text.toString().trim().length == 1 }
                    binding.verify.isEnabled = allFilled
                    binding.verify.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            binding.root.context,
                            if (allFilled) R.color.primaryBlue else R.color.greyText
                        )
                    )

                    otptext = otpFields.joinToString("") { it.text.toString() }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }


}