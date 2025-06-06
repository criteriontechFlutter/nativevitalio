package com.critetiontech.ctvitalio.UI

import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.ui.signupFragment.ProgressViewModel
import com.critetiontech.ctvitalio.databinding.ActivitySignupBinding
import com.critetiontech.ctvitalio.viewmodel.RegistrationViewModel

class SignupActivity : AppCompatActivity() {


    private lateinit var navController: NavController
    private lateinit var binding: ActivitySignupBinding
    private lateinit var progressViewModel: ProgressViewModel
    private lateinit var viewModel: RegistrationViewModel
    lateinit var mobileNo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up NavController for Fragment navigation
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Set up progress view models
        progressViewModel = ViewModelProvider(this).get(ProgressViewModel::class.java)
        viewModel = ViewModelProvider(this).get(RegistrationViewModel::class.java)

        // Get mobile number and pass it to the view model
        mobileNo = intent.getStringExtra("mobileNo").toString()
        viewModel.mobileNo.value = mobileNo
        Log.d("RESPONSE", "phoneOrUHID6: $mobileNo")


        // Observe the progress to update UI dynamically
        progressViewModel.progress.observe(this) { step ->

            binding.createAccount.text=step.toString()
            if (step <= 5 || step == 11 || step == 15) {
                // Disable skip button and change its text color to a disabled state
                binding.skipButton.isEnabled = false
                binding.skipButton.setTextColor(getColor(R.color.greyText))  // Disabled color
            }  else {
            // Enable skip button and change its text color to active state
            binding.skipButton.isEnabled = true
            binding.skipButton.setTextColor(getColor(R.color.primaryBlue))  // Active color
        }

// Update createAccount button text based on progress step
//            if (step >= 13) {
//                binding.createAccount.text = "Set Preferences"
//            } else {
//                binding.createAccount.text = "Create Account"
//            }
            if ( progressViewModel.progress.value == 11) {
                binding.progressBarId.visibility = View.GONE

            }
            updateProgress(step)
        }
        progressViewModel.progressPage.observe(this, { step ->

            if (step == 1) {
                binding.progressBarId.visibility = View.GONE
            } else{
                binding.progressBarId.visibility = View.VISIBLE

            }
            if ( progressViewModel.progress.value == 13) {
                binding.progressBarId.visibility = View.VISIBLE

            }
        })

        // Back button listener to go back based on progress
        binding.backButton.setOnClickListener {

            if (progressViewModel.progress.value == 12 && progressViewModel.progressPage.value == 2) {
                progressViewModel.updateProgressPage(  1)
            }
            else  if (progressViewModel.progressPage.value == 1) {
                // Go to the Home Activity if at the first step
                val intent = Intent(this@SignupActivity, Home::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            } else {
                // Otherwise, go to the previous fragment and update progress
                progressViewModel.updateProgress((progressViewModel.progress.value ?: 1) - 1)
                onBackPressedDispatcher.onBackPressed()
            }

        }

        // Skip button listener to handle skipping through the steps
        binding.skipButton.setOnClickListener {
            handleSkipButtonClick()
        }
    }

    // Function to handle skip button click and navigate based on progress
    private fun handleSkipButtonClick() {
        val step = progressViewModel.progress.value ?: return

        if (step >= 6) {

            // Navigate to the appropriate fragment based on the current progress step
            val actionId = getNavigationActionForStep(step)
            if (actionId != null) {
                navController.navigate(actionId)
            } else {
             }
            // For steps less than 6, update progress and go back


        } else {

        }
    }

    // Return the appropriate navigation action ID for the given step

    private fun getNavigationActionForStep(step: Int): Int? {
        return when (step) {
            6 -> {  progressViewModel.updateProgress(7)
                R.id.action_weightFragment_to_heightFragment

            }
            7 -> {  progressViewModel.updateProgress(8)
                R.id.action_heightFragment_to_chronicConditionFragment
            }
            8 -> {  progressViewModel.updateProgress(9)
                R.id.action_chronicConditionFragment_to_otherChronicDisease2
            }
            9 -> {  progressViewModel.updateProgress(10)
                R.id.action_otherChronicDisease_to_familyDiseaseFragment2
            }
            10 -> { progressViewModel.updateProgress(11)
                R.id.action_familyDiseaseFragment_to_createAccount2
            }
            12 -> {  progressViewModel.updateProgress(13)
                progressViewModel.updateProgressPage(  2)
                R.id.action_accountSuccess_to_setPreferences
            }
            13 -> {  progressViewModel.updateProgress(14)
                progressViewModel.updateProgressPage(  2)
                R.id.action_setPreferences_to_setPreferenseFluidItake
            }
            14 -> { progressViewModel.updateProgress(15)
                progressViewModel.updateProgressPage(  2)
                R.id.action_setPreferenseFluidItake_to_completionScreen
            }
            else -> {
                null
            }
        }
    }

    // Update the progress bar and step titles based on the current progress
    private fun updateProgress(step: Int) {
        val totalSteps = 14
        val progressPercent = (step * 100) / totalSteps
        binding.progressBar.progress = progressPercent
        binding.tvProgressPercent.text = "$progressPercent%"

        when (step) {
            1 -> {
                binding.tvStepTitle.text = "Getting Started"
                binding.tvStepSubtitle.text = "Great start! You’re just beginning—let’s keep going!"
            }
            2 -> {
                binding.tvStepTitle.text = "Moving Forward"
                binding.tvStepSubtitle.text = "You're gaining momentum - keep moving forward!"
            }
            3 -> {
                binding.tvStepTitle.text = "Staying on Track"
                binding.tvStepSubtitle.text = "Nice work! You're a quarter of the way there!"
            }
            4 -> {
                binding.tvStepTitle.text = "One-Third Complete"
                binding.tvStepSubtitle.text = "Nice work! You're a third of the way there!"
            }
            5 -> {
                binding.tvStepTitle.text = "Almost There to Halfway"
                binding.tvStepSubtitle.text = "You're getting closer—just a little more to reach halfway!"
            }
            6 -> {
                binding.tvStepTitle.text = "Staying on Track"
                binding.tvStepSubtitle.text = "You're getting closer—just a little more to reach halfway!"
            }
            7 -> {
                binding.tvStepTitle.text = "Moving Ahead"
                binding.tvStepSubtitle.text = "You're past halfway—great job so far!"
            }
            8 -> {
                binding.tvStepTitle.text = "Final Stretch in Sight"
                binding.tvStepSubtitle.text = "You're making great progress—just a little more to go!"
            }
            9 -> {
                binding.tvStepTitle.text = "Just a Little Further to Go"
                binding.tvStepSubtitle.text = "You've come so far—just a final push!"
            }
            10 -> {
                binding.tvStepTitle.text = "Final Push Ahead"
                binding.tvStepSubtitle.text = "So close! Only a few steps remain!"
            }
            11 -> {
                binding.tvStepTitle.text = "All Done!"
                binding.tvStepSubtitle.text = "You're just one step away from completing the process!"
            }
            13 -> {
                binding.tvStepTitle.text = "All Done!"
                binding.tvStepSubtitle.text = "You're just one step away from completing the process!"
            }
            14 -> {
                binding.tvStepTitle.text = "All Done!"
                binding.tvStepSubtitle.text = "Almost done—just complete the final step!"
            }
        }
    }

    // Handle back navigation within the NavController
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}