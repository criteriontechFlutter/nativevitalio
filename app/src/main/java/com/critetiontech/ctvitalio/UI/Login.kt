package com.critetiontech.ctvitalio.UI

import Patient
import PrefsManager
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.*
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.ui.ConfirmUpdateDialog
import com.critetiontech.ctvitalio.databinding.ActivityLoginBinding
import com.critetiontech.ctvitalio.utils.LoaderUtils.hideLoading
import com.critetiontech.ctvitalio.utils.LoaderUtils.showLoading
import com.critetiontech.ctvitalio.viewmodel.LoginViewModel
import com.critetiontech.ctvitalio.viewmodel.RegistrationViewModel
import androidx.core.graphics.toColorInt

class Login : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var registrationViewModel: RegistrationViewModel

    companion object {
        var storedUHID: Patient? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSystemBarsColor(
            statusBarColor = R.color.darkYellow,
            navBarColor = android.R.color.white,
            lightIcons = true
        )

        setupInsets()
        initializeViewModels()
        setupListeners()
        observeViewModel()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainScrollView) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, sys.bottom)
            insets
        }
    }

    private fun initializeViewModels() {
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        registrationViewModel = ViewModelProvider(this)[RegistrationViewModel::class.java]

        loginViewModel.loading.observe(this) { loading ->
            if (loading) showLoading() else hideLoading()
        }
    }

    private fun setupListeners() {

        binding.sendOtpBtn.isEnabled = false
        binding.validationId.visibility = View.GONE

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.inputField.addTextChangedListener(watcher)
        binding.passField.addTextChangedListener(watcher)

        // Scroll when input gains focus
        binding.inputField.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) scrollToView(binding.mainScrollView, binding.inputField)
        }

        setupPasswordVisibilityToggle()

        // LOGIN
        binding.sendOtpBtn.setOnClickListener {
            performLogin()
        }

        // Forgot password
        binding.forgotPasswordBtn.setOnClickListener {
            startActivity(Intent(this, ForgotPassword::class.java))
        }
    }

    private fun setupPasswordVisibilityToggle() {
        var visible = false

        binding.passField.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {

                val drawableEnd = binding.passField.compoundDrawables[2]
                if (drawableEnd != null) {

                    val width = drawableEnd.bounds.width()
                    if (event.rawX >= (binding.passField.right - width - 30)) {

                        visible = !visible
                        val cursorPos = binding.passField.selectionEnd
                        val tf = binding.passField.typeface

                        binding.passField.inputType =
                            if (visible) InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                            else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

                        binding.passField.typeface = tf
                        binding.passField.setSelection(cursorPos)

                        val icon = if (visible) R.drawable.close_eye else R.drawable.open_eye
                        binding.passField.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.lock,
                            0,
                            icon,
                            0
                        )
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

    private fun performLogin() {
        val username = binding.inputField.text?.toString()?.trim() ?: ""
        val password = binding.passField.text?.toString()?.trim() ?: ""

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        loginViewModel.corporateEmployeeLogin(this, username, password)
    }

    private fun observeViewModel() {
        loginViewModel.loginSuccess.observe(this) { success ->
            if (success == true) handleLoginSuccess()
            else handleLoginFailure()
        }

        loginViewModel.errorMessage.observe(this) { err ->
            err?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun handleLoginSuccess() {

        binding.passField.setBackgroundResource(R.drawable.bg_edittext_rounded)
        binding.inputField.setBackgroundResource(R.drawable.bg_edittext_rounded)
        binding.doctorsImage.setImageResource(R.drawable.happygif)
        binding.validationId.visibility = View.GONE
        binding.upperground.setBackgroundColor("#FFDD00".toColorInt())

        val prefs = PrefsManager().getPatient()
        val name = prefs?.patientName ?: "User"

        val firstLogin = false

        if (firstLogin) {
            ConfirmUpdateDialog(
                title = "Login Successful",
                message = "Hello $name, welcome to Vitalio.\nLet's secure your account with a new password.",
                btnText = "Change Password",
                onConfirm = {
                    startActivity(Intent(this, ResetPassword::class.java))
                }
            ).show(supportFragmentManager, ConfirmUpdateDialog.TAG)
        } else {
            startActivity(Intent(this, Home::class.java))
        }
    }

    private fun handleLoginFailure() {
        binding.passField.setBackgroundResource(R.drawable.error_feild)
        binding.inputField.setBackgroundResource(R.drawable.error_feild)

        binding.doctorsImage.setImageResource(R.drawable.angergif)
        binding.validationId.visibility = View.VISIBLE

        setSystemBarsColor(
            statusBarColor = R.color.angerRed,
            navBarColor = android.R.color.white,
            lightIcons = true
        )
        binding.upperground.setBackgroundColor("#FE1E09".toColorInt())
    }

    private fun updateButtonState() {
        val id = binding.inputField.text?.toString()?.trim() ?: ""
        val password = binding.passField.text?.toString()?.trim() ?: ""
        val valid = id.isNotEmpty() && password.isNotEmpty()

        binding.sendOtpBtn.isEnabled = valid

        if (valid) {
            binding.sendOtpBtn.setGradientColors(
                ContextCompat.getColor(this, R.color.primaryBlue),
                ContextCompat.getColor(this, R.color.primaryBlue2)
            )
        } else {
            binding.sendOtpBtn.setGradientColors(
                ContextCompat.getColor(this, R.color.greyText),
                ContextCompat.getColor(this, R.color.greyText)
            )
        }
    }

    private fun scrollToView(scroll: NestedScrollView, v: View) {
        scroll.post { scroll.smoothScrollTo(0, v.top) }
    }

    override fun onStart() {
        super.onStart()
        val card = findViewById<CardView>(R.id.bottomCard)
        val anim = AnimationUtils.loadAnimation(this, R.anim.zoom_in)
        card?.startAnimation(anim)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}