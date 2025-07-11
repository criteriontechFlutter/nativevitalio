package com.critetiontech.ctvitalio.UI

import Patient
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.ActivityLoginBinding
import com.critetiontech.ctvitalio.utils.LoaderUtils.hideLoading
import com.critetiontech.ctvitalio.utils.LoaderUtils.showLoading
import com.critetiontech.ctvitalio.viewmodel.LoginViewModel
import com.critetiontech.ctvitalio.viewmodel.RegistrationViewModel

class Login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var regestrationViewModel: RegistrationViewModel
    companion object {
        lateinit var storedUHID: Patient

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        regestrationViewModel = ViewModelProvider(this)[RegistrationViewModel::class.java]
        // Disable button initially

        viewModel.loading.observe(this) { isLoading ->
            if (isLoading) showLoading() else hideLoading()
        }



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
        binding.inputField.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                scrollToView(binding.mainScrollView, binding.inputField)
            }
        }
        // Button click triggers API call
        binding.sendOtpBtn.setOnClickListener {
            val phoneOrUHID = binding.inputField.text.toString().trim()
            if (phoneOrUHID.lowercase().contains("uhid")) {
                // Handle UHID case here
            } else {

                // Proceed to send OTP or next step
            }
            Log.d("RESPONSE", "phoneOrUHID"+phoneOrUHID.toString())

            viewModel.getPatientDetailsByUHID(phoneOrUHID)
//            binding.sendOtpBtn.isEnabled  = true
//            binding. progressBar.visibility  = View.GONE
        }


//        binding.privacyPolicy.setOnClickListener {
//            val intent = Intent(applicationContext, SignupActivity::class.java)
//            startActivity(intent)
//        }
    }

    private fun observeViewModel() {
        viewModel.loading.observe(this) { isLoading ->
            binding.sendOtpBtn.isEnabled = !isLoading
            // Show/hide loader if needed
        }
        viewModel.showDialog.observe(this) { title ->
            title?.let { showVitalDialog(it) }
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }


    }



    @SuppressLint("MissingInflatedId")
    private fun showVitalDialog(title: String) {
        if (isFinishing || isDestroyed) return
        val dialogView = LayoutInflater.from(this).inflate(R.layout.login_multiple_dialog, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<TextView>(R.id.title)?.text = title
        dialogView.findViewById<Button>(R.id.btnLogoutAll)?.setOnClickListener {
            dialog.dismiss()
            var uhid = ""
            var mo = ""

            val input = binding.inputField.text.toString()
            if (input.contains("UHID")) {
                uhid = input
            } else {
                mo = input
            }
            // Handle logout from all devices
            viewModel.sentLogInOTPForSHFCApp(uhid=uhid.toString(),
                mobileNo=mo.toString(),ifLoggedOutFromAllDevices="1")
        }
        dialogView.findViewById<Button>(R.id.btnCancel)?.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()




    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }


    override fun onPause() {
        super.onPause()
    }

    override fun onStart() {
        super.onStart()
        val bottomCard = findViewById<CardView>(R.id.bottomCard)
        val animation = AnimationUtils.loadAnimation(this, R.anim.zoom_in)
        Handler(Looper.getMainLooper()).postDelayed({
            bottomCard.startAnimation(animation)
        }, 200)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onRestart() {
        super.onRestart()
    }
    private fun scrollToView(scrollView: NestedScrollView, view: View) {
        scrollView.post {
            scrollView.smoothScrollTo(0, view.top)
        }
    }
}

