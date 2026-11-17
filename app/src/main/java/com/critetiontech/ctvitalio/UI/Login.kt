package com.critetiontech.vitalioCorporate.UI

import android.content.Intent
import android.os.*
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import com.critetiontech.vitalioCorporate.R
import com.critetiontech.vitalioCorporate.UI.ui.ConfirmUpdateBottomSheet
import com.critetiontech.vitalioCorporate.databinding.ActivityLoginBinding
import com.critetiontech.vitalioCorporate.model.Patient
import com.critetiontech.vitalioCorporate.utils.PrefsManager
import com.critetiontech.vitalioCorporate.viewmodel.LoginViewModel
import com.critetiontech.vitalioCorporate.viewmodel.RegistrationViewModel
import com.critetiontech.vitalioCorporate.utils.showSafe

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
        // Android 15+ (targetSdk 35) forces edge-to-edge, so adjustResize no longer shrinks
        // the window for the keyboard. Pad for status bar, nav bar and IME ourselves.
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainScrollView) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.updatePadding(
                left = bars.left,
                top = bars.top,
                right = bars.right,
                bottom = maxOf(bars.bottom, ime.bottom)
            )
            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                scrollFormIntoView()
            }
            WindowInsetsCompat.CONSUMED
        }
    }

    /** Form sits at the bottom of the scroll content, so scrolling to the end shows fields + Login button. */
    private fun scrollFormIntoView() {
        val scroll = binding.mainScrollView
        scroll.post {
            val bottom = scroll.getChildAt(0)?.height ?: return@post
            scroll.smoothScrollTo(0, bottom)
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
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateButtonState()
            }
            override fun afterTextChanged(s: Editable?) = Unit
        }

        binding.inputField.addTextChangedListener(watcher)
        binding.passField.addTextChangedListener(watcher)

        // Scroll when either input gains focus (IME may already be open)
        val focusScroll = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) scrollFormIntoView()
        }
        binding.inputField.onFocusChangeListener = focusScroll
        binding.passField.onFocusChangeListener = focusScroll

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
        val isVisible =
            binding.passField.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

        val cursor = binding.passField.selectionEnd
        val typeface = binding.passField.typeface

        binding.passField.inputType =
            if (isVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            } else {
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            }

        binding.passField.typeface = typeface
        binding.passField.setSelection(cursor)

        binding.passField.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.lock,
            0,
            if (isVisible) R.drawable.open_eye else R.drawable.close_eye,
            0
        )
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

        val isFirstLogin = (prefs?.isFirstLoginCompleted ?: 1) == 0

        if (isFirstLogin) {
            ConfirmUpdateBottomSheet(
                title = "Login Successfully",
                message = "Hello $name, Welcome to Vitalio.\nLet's secure your account with a new password.",
                btnText = "Change Password",
                onConfirm = {
                    val intent = Intent(this, ResetPassword::class.java)
                    intent.putExtra("oldPassword", binding.passField.text.toString())
                    startActivity(intent)
                }
            ).showSafe(supportFragmentManager,  ConfirmUpdateBottomSheet.TAG)
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

    override fun onStart() {
        super.onStart()
        val card = findViewById<CardView>(R.id.bottomCard)
        val anim = AnimationUtils.loadAnimation(this, R.anim.zoom_in)
        card?.startAnimation(anim)
    }

}

fun onBackPressed(login: Login) {
    login.finishAffinity()
}
