package com.critetiontech.ctvitalio.UI

import Patient
import PrefsManager
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.R.*
import com.critetiontech.ctvitalio.UI.ui.ConfirmUpdateDialog
import com.critetiontech.ctvitalio.databinding.ActivityLoginBinding
import com.critetiontech.ctvitalio.utils.LoaderUtils.hideLoading
import com.critetiontech.ctvitalio.utils.LoaderUtils.showLoading
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.viewmodel.LoginViewModel
import com.critetiontech.ctvitalio.viewmodel.RegistrationViewModel

class Login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var regestrationViewModel: RegistrationViewModel
    val context = MyApplication.appContext

        companion object {
            var storedUHID: Patient? = null
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainScrollView) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                systemBarsInsets.bottom // add bottom inset so scrolling works
            )
            insets
        }
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        regestrationViewModel = ViewModelProvider(this)[RegistrationViewModel::class.java]
        // Disable button initially

        viewModel.loading.observe(this) { isLoading ->
            if (isLoading) showLoading() else hideLoading()
        }


        binding.sendOtpBtn.isEnabled = false
        updateButtonState() // I

        binding.sendOtpBtn.isEnabled = false
        // Enable button only when text is valid
//        binding.inputField.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun afterTextChanged(s: Editable?) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                updateButtonState()
//                val input = s.toString().trim()
//                val isValid = input.length >= 2
//                binding.sendOtpBtn.isEnabled = isValid
//                binding.sendOtpBtn.backgroundTintList = ColorStateList.valueOf(
//                    ContextCompat.getColor(this@Login,
//                        if (isValid) color.primaryBlue else color.greyText)
//                )
//            }
//        })


        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.inputField.addTextChangedListener(textWatcher)
        binding.passField.addTextChangedListener(textWatcher)

        // ✅ Observe once — not on every button click
//        observeViewModel()
        binding.inputField.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                scrollToView(binding.mainScrollView, binding.inputField)
            }
        }
        var isNewPasswordVisible = false

        binding.validationId.visibility=View.GONE


        binding.passField.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2  // Index for drawableRight
                val drawableWidth = binding.passField.compoundDrawables[drawableEnd]?.bounds?.width() ?: 0
                if (event.rawX >= (binding.passField.right - drawableWidth)) {
                    isNewPasswordVisible = !isNewPasswordVisible

                    // Preserve typeface before changing inputType
                    val typeface = binding.passField.typeface

                    // Change input type
                    binding.passField.inputType = if (isNewPasswordVisible) {
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    } else {
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    }

                    // Reapply typeface to maintain font appearance
                    binding.passField.typeface = typeface

                    // Preserve cursor position
                    binding.passField.setSelection(binding.passField.text.length)

                    // Toggle eye icon
                    val icon = if (isNewPasswordVisible) R.drawable.close_eye else R.drawable.open_eye
                    binding.passField.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, icon, 0)

                    return@setOnTouchListener true
                }
            }
            false
        }

        binding.passField.setBackgroundResource(R.drawable.bg_edittext_rounded)
        binding.inputField.setBackgroundResource(R.drawable.bg_edittext_rounded)
//        happygif angergif
        viewModel.loginSuccess.observe(this) { success ->
            if (success) {
                binding.passField.setBackgroundResource(R.drawable.bg_edittext_rounded)
                binding.inputField.setBackgroundResource(R.drawable.bg_edittext_rounded)
                binding.doctorsImage.setImageResource(R.drawable.happygif)
                binding.upperground.setBackgroundColor(Color.parseColor("#FFDD00"))
                binding.validationId.visibility=View.GONE
                ConfirmUpdateDialog(
                    title = "Login Successful",
                    message = "Hello "+ PrefsManager().getPatient()?.patientName.toString()+"." +
                            " Welcome to Vitalio.\nLet's secure your account with a new password.",
                    btnText = " Change Password",
                    onConfirm = {
                        if( PrefsManager().getPatient()?.isFirstLoginCompleted.toString()=="1"){
                            val intent = Intent(context, Home::class.java)
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }
                        else{
                            val intent = Intent(context, ResetPassword::class.java)
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }
                    },

                ).show(supportFragmentManager, ConfirmUpdateDialog.TAG)
            } else {
                binding.passField.setBackgroundResource(R.drawable.error_feild)
                binding.inputField.setBackgroundResource(R.drawable.error_feild)
                binding.validationId.visibility=View.VISIBLE
                binding.doctorsImage.setImageResource(R.drawable.angergif)
                binding.upperground.setBackgroundColor(Color.parseColor("#FE1E09"))

            }
        }
        // Button click triggers API call
        binding.sendOtpBtn.setOnClickListener {

            viewModel.corporateEmployeeLogin(
                context,
                username = binding.inputField.text.toString(),
                password = binding.passField.text.toString()
            )


//            val intent =
//                Intent(MyApplication.appContext, SignupActivity::class.java).apply {
//                    putExtra("UHID", "uhid")
//                    putExtra("mobileNo", "mNo")
//                }
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            MyApplication.appContext.startActivity(intent)
         }

        binding.forgotPasswordBtn.setOnClickListener(){
            val intent =
                Intent(MyApplication.appContext, ForgotPassword::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            MyApplication.appContext.startActivity(intent)
        }




//        binding.privacyPolicy.setOnClickListener {
//            val intent = Intent(applicationContext, SignupActivity::class.java)
//            startActivity(intent)
//        }
    }
    private fun updateButtonState() {
        val employeeId = binding.inputField.text.toString().trim()
        val password = binding.passField.text.toString().trim()

        val isValid = employeeId.isNotEmpty() && password.isNotEmpty()

        binding.sendOtpBtn.isEnabled = isValid
        binding.sendOtpBtn.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(this, if (isValid) R.color.primaryBlue else R.color.greyText)
        )
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
        val dialogView = LayoutInflater.from(this).inflate(layout.login_multiple_dialog, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
//        dialogView.findViewById<TextView>(id.title)?.text = title
        dialogView.findViewById<Button>(id.btnLogoutAll)?.setOnClickListener {
            dialog.dismiss()
            // Handle logout from all devices
            viewModel.sentLogInOTPForSHFCApp(uhid= storedUHID?.uhID.toString(),
                mobileNo= storedUHID?.mobileNo.toString(),ifLoggedOutFromAllDevices="1")
        }
        dialogView.findViewById<Button>(id.btnCancel)?.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()




    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }


    override fun onStart() {
        super.onStart()
        val bottomCard = findViewById<CardView>(id.bottomCard)
        val animation = AnimationUtils.loadAnimation(this, anim.zoom_in)
        Handler(Looper.getMainLooper()).postDelayed({
            bottomCard.startAnimation(animation)
        }, 200)
    }

    private fun scrollToView(scrollView: NestedScrollView, view: View) {
        scrollView.post {
            scrollView.smoothScrollTo(0, view.top)
        }
    }
}

