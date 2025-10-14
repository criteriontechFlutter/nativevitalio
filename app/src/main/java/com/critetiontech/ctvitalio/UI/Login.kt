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
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.viewmodel.LoginViewModel
import com.critetiontech.ctvitalio.viewmodel.RegistrationViewModel

/**
 * Login Activity
 *
 * Handles user authentication, UI interactions, and navigation to the next screen.
 * Uses ViewModel architecture for business logic separation.
 */
class Login : AppCompatActivity() {

    // View binding for UI access
    private lateinit var binding: ActivityLoginBinding

    // ViewModels for API interaction and business logic
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var registrationViewModel: RegistrationViewModel

    // Local context
    private val context = MyApplication.appContext

    companion object {
        var storedUHID: Patient? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupInsets()
        initializeViewModels()
        setupListeners()
        observeViewModel()
    }

    /**
     * Ensures layout handles system bar insets (especially bottom navigation overlap)
     */
    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainScrollView) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                systemBarsInsets.bottom
            )
            insets
        }
    }

    /**
     * Initializes all ViewModels
     */
    private fun initializeViewModels() {
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        registrationViewModel = ViewModelProvider(this)[RegistrationViewModel::class.java]

        loginViewModel.loading.observe(this) { isLoading ->
            if (isLoading) showLoading() else hideLoading()
        }
    }

    /**
     * Sets up click listeners, text watchers, and field behaviors
     */
    private fun setupListeners() {

        binding.sendOtpBtn.isEnabled = false
        binding.validationId.visibility = View.GONE

        // Enable login button dynamically
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.inputField.addTextChangedListener(textWatcher)
        binding.passField.addTextChangedListener(textWatcher)

        // Scroll to input field when focused
        binding.inputField.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) scrollToView(binding.mainScrollView, binding.inputField)
        }

        // Toggle password visibility
        setupPasswordVisibilityToggle()

        // Handle login button click
        binding.sendOtpBtn.setOnClickListener {
            performLogin()
        }

        // Handle Forgot Password
        binding.forgotPasswordBtn.setOnClickListener {
            startActivity(Intent(context, ForgotPassword::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }

    /**
     * Handles password show/hide functionality
     */
    private fun setupPasswordVisibilityToggle() {
        var isVisible = false
        binding.passField.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2
                val drawableWidth = binding.passField.compoundDrawables[drawableEnd]?.bounds?.width() ?: 0

                if (event.rawX >= (binding.passField.right - drawableWidth)) {
                    isVisible = !isVisible

                    val typeface = binding.passField.typeface
                    binding.passField.inputType = if (isVisible)
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    else
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

                    binding.passField.typeface = typeface
                    binding.passField.setSelection(binding.passField.text.length)

                    val icon = if (isVisible) R.drawable.close_eye else R.drawable.open_eye
                    binding.passField.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, icon, 0)
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    /**
     * Validates input fields and performs login API call
     */
    private fun performLogin() {
        try {
            val username = binding.inputField.text.toString().trim()
            val password = binding.passField.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return
            }

            loginViewModel.corporateEmployeeLogin(context, username, password)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Observes ViewModel LiveData for API results and UI updates
     */
    private fun observeViewModel() {
        loginViewModel.loginSuccess.observe(this) { success ->
            try {
                if (success) handleLoginSuccess()
                else handleLoginFailure()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Unexpected error occurred", Toast.LENGTH_SHORT).show()
            }
        }

        loginViewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Handles successful login behavior
     */
    private fun handleLoginSuccess() {
        binding.passField.setBackgroundResource(R.drawable.bg_edittext_rounded)
        binding.inputField.setBackgroundResource(R.drawable.bg_edittext_rounded)
        binding.doctorsImage.setImageResource(R.drawable.happygif)
        binding.upperground.setBackgroundColor(Color.parseColor("#FFDD00"))
        binding.validationId.visibility = View.GONE

        val prefs = PrefsManager().getPatient()
        val name = prefs?.patientName ?: "User"

        if (prefs?.isFirstLoginCompleted.toString() == "1") {
            ConfirmUpdateDialog(
                title = "Login Successful",
                message = "Hello $name, welcome to Vitalio.\nLet's secure your account with a new password.",
                btnText = "Change Password",
                onConfirm = {
                    val intent = if (prefs?.isFirstLoginCompleted.toString() == "1")
                        Intent(context, Home::class.java)
                    else
                        Intent(context, ResetPassword::class.java)

                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }
            ).show(supportFragmentManager, ConfirmUpdateDialog.TAG)
        } else {
            val intent = Intent(context, Home::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    /**
     * Handles login failure behavior
     */
    private fun handleLoginFailure() {
        binding.passField.setBackgroundResource(R.drawable.error_feild)
        binding.inputField.setBackgroundResource(R.drawable.error_feild)
        binding.validationId.visibility = View.VISIBLE
        binding.doctorsImage.setImageResource(R.drawable.angergif)
        binding.upperground.setBackgroundColor(Color.parseColor("#FE1E09"))
    }

    /**
     * Enables/disables login button based on field validation
     */
    private fun updateButtonState() {
        val employeeId = binding.inputField.text.toString().trim()
        val password = binding.passField.text.toString().trim()
        val isValid = employeeId.isNotEmpty() && password.isNotEmpty()

        binding.sendOtpBtn.isEnabled = isValid
        binding.sendOtpBtn.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(this, if (isValid) R.color.primaryBlue else R.color.greyText)
        )
    }

    /**
     * Smoothly scrolls to a given view inside a NestedScrollView
     */
    private fun scrollToView(scrollView: NestedScrollView, view: View) {
        scrollView.post {
            scrollView.smoothScrollTo(0, view.top)
        }
    }

    /**
     * Adds a nice animation to bottom card when Activity starts
     */
    override fun onStart() {
        super.onStart()
        try {
            val bottomCard = findViewById<CardView>(R.id.bottomCard)
            val animation = AnimationUtils.loadAnimation(this, R.anim.zoom_in)
            Handler(Looper.getMainLooper()).postDelayed({
                bottomCard.startAnimation(animation)
            }, 200)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Prevents accidental back navigation by closing all previous activities
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}
