package com.criterion.nativevitalio.UI

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.ActivityLoginBinding
import com.criterion.nativevitalio.viewmodel.LoginViewModel

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    companion object {
        lateinit var storedUHID: String

    }
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

//            binding.sendOtpBtn.isEnabled = false
//            binding. progressBar.visibility = View.VISIBLE

            viewModel.getPatientDetailsByUHID(phoneOrUHID)
//            binding.sendOtpBtn.isEnabled  = true
//            binding. progressBar.visibility  = View.GONE
        }
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



    private fun showVitalDialog(title: String) {
        if (isFinishing || isDestroyed) return
        val dialogView = LayoutInflater.from(this).inflate(R.layout.login_multiple_dialog, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<TextView>(R.id.title)?.text = title
        dialogView.findViewById<Button>(R.id.btnLogoutAll)?.setOnClickListener {
            dialog.dismiss()
            // Handle logout from all devices
            viewModel.sentLogInOTPForSHFCApp(storedUHID, "1")
        }
        dialogView.findViewById<Button>(R.id.btnCancel)?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()




    }


    override fun onPause() {
        Log.d("TAG111111", "onPause: ")
        super.onPause()
    }

    override fun onStart() {
        Log.d("TAG111111", "onStart: ")
        super.onStart()
    }

    override fun onResume() {
        Log.d("TAG111111", "onResume: ")
        super.onResume()
    }

    override fun onRestart() {
        Log.d("TAG111111", "onRestart: ")
        super.onRestart()
    }

}

