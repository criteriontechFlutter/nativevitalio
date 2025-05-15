package com.critetiontech.ctvitalio.UI

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.ui.signupFragment.ProgressViewModel
import com.critetiontech.ctvitalio.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {


    private lateinit var navController: NavController
    private lateinit var binding: ActivitySignupBinding
    private lateinit var progressViewModel: ProgressViewModel

    override fun onCreate (savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // ViewModel
        progressViewModel = ViewModelProvider(this)[ProgressViewModel::class.java]

        // Observe LiveData
        progressViewModel.progress.observe(this, { step ->
            Log.d("UploadSuccess", "updateProgress: $step")
            updateProgress(step)
        })
    }

    private fun updateProgress(step: Int) {
        val totalSteps = 5
        val progressPercent = (step * 100) / totalSteps
        binding.progressBar.progress = progressPercent
        binding.tvProgressPercent.text = "$progressPercent%"

        when (step) {
            1 -> {
                binding.tvStepTitle.text = "Getting Started"
                binding.tvStepSubtitle.text = "Great start! You’re just beginning—let’s keep going!"
            }
            2 -> {
                binding.tvStepTitle.text = "Personal Details"
                binding.tvStepSubtitle.text = "Let’s get to know you better!"
            }
            3 -> {
                binding.tvStepTitle.text = "Contact Information"
                binding.tvStepSubtitle.text = "Almost there, keep going!"
            }
            4 -> {
                binding.tvStepTitle.text = "Health Info"
                binding.tvStepSubtitle.text = "Help us personalize your care."
            }
            5 -> {
                binding.tvStepTitle.text = "Review & Submit"
                binding.tvStepSubtitle.text = "You're almost done!"
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}