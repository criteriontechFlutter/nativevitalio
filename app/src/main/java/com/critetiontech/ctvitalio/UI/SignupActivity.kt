package com.critetiontech.ctvitalio.UI

import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.ui.signupFragment.ProgressViewModel
import com.critetiontech.ctvitalio.databinding.ActivitySignupBinding
import com.critetiontech.ctvitalio.viewmodel.RegistrationViewModel
import androidx.navigation.NavOptions
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
        progressViewModel.pageNo.observe(this) { step ->



            if (  progressViewModel.pageNo.value!! == 10 ||
                progressViewModel.pageNo.value!! == 11 ||
                progressViewModel.pageNo.value!! >= 14
            ) {
                binding.skipButton.visibility=View.GONE
            }
            else   if (
                progressViewModel.pageNo.value!! <3
            ) {
                binding.skipButton.visibility=View.VISIBLE
                binding.skipButton.isEnabled = false
                binding.skipButton.setTextColor(getColor(R.color.greyText))

            }
            else {

                binding.skipButton.visibility=View.VISIBLE
                binding.skipButton.isEnabled = true
                binding.skipButton.setTextColor(getColor(R.color.primaryBlue))
            }


            if ( progressViewModel.pageNo.value == 11 ||
                progressViewModel.pageNo.value!! >= 14
                ) {
                binding.progressBarId.visibility = View.GONE
            }
            else{
                binding.progressBarId.visibility = View.VISIBLE

            }

            if (
                progressViewModel.pageNo.value!! == 11
            ) {

                binding.backButton.visibility=View.GONE
            }
            else{
                binding.backButton.visibility=View.VISIBLE

            }

            if (
                progressViewModel.pageNo.value!! <= 10
            ) {

//                binding.createAccount.text=progressViewModel.pageNo.value.toString()
                binding.createAccount.text="Create Account "
            }
            else{
                binding.createAccount.text="Set Preferences"
//                binding.createAccount.text=progressViewModel.pageNo.value.toString()

            }





            updateProgress(progressViewModel.progress.value,step )
        }

        // Back button listener to go back based on progress
        binding.backButton.setOnClickListener {
            if(progressViewModel.pageNo.value!! >=0){

            }

            else{
                val intent = Intent(this@SignupActivity, Login::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }

            onBackPressedDispatcher.onBackPressed()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                if (progressViewModel.pageNo.value == 11) {

                }
                else  if(progressViewModel.pageNo.value> 0){

                val navHostFragment = supportFragmentManager
                    .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                val navController = navHostFragment.navController
                navController.popBackStack()

                    backbtnFun()
                }

                 else{
                    val intent = Intent(this@SignupActivity, Login::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
        })
        // Skip button listener to handle skipping through the steps
        binding.skipButton.setOnClickListener {
            handleSkipButtonClick()
        }
    }

  fun  backbtnFun(){

        if (progressViewModel.pageNo.value == 11) {
            // Go to the Home Activity if at the first step
//                val intent = Intent(this@SignupActivity, Home::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                startActivity(intent)
        } else {
            if ( progressViewModel.pageNo.value!! != 8

            ) {

                progressViewModel.updateProgress((progressViewModel.progress.value ?: 1) - 1)

            }



            if ( progressViewModel.pageNo.value!! == 11 ||
                progressViewModel.pageNo.value!! >= 15  ) {

                binding.progressBarId.visibility = View.GONE
            }
            else{
                binding.progressBarId.visibility = View.VISIBLE

            }


            if (  progressViewModel.pageNo.value!! == 11 ||
                progressViewModel.pageNo.value!! == 12 ||
                progressViewModel.pageNo.value!! >  15
            ) {
                binding.skipButton.visibility=View.GONE
            }
            else   if (
                progressViewModel.pageNo.value!! <3 ||
                progressViewModel.pageNo.value!! == 13 ||
                progressViewModel.pageNo.value!! == 15
            ) {
                binding.skipButton.visibility=View.VISIBLE
                binding.skipButton.isEnabled = false
                binding.skipButton.setTextColor(getColor(R.color.greyText))

            }
            else {
                binding.skipButton.visibility=View.VISIBLE
                binding.skipButton.isEnabled = true
                binding.skipButton.setTextColor(getColor(R.color.primaryBlue))
            }


            progressViewModel.updatepageNo((progressViewModel.pageNo.value ?: 1) - 1)

        }

    }
    // Function to handle skip button click and navigate based on progress
    private fun handleSkipButtonClick() {
        val step = progressViewModel.pageNo .value ?: return

        if (step >= 3) {

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
//    private fun getNavigationActionForStep(step: Int): Int? {
//        return when (step) {
//            3 -> {  progressViewModel.updatepageNo(4)
//                progressViewModel.updateProgress(4)
//                R.id.action_bloodGroupFragment_to_adressFragment
//
//            }
//            4 -> {  progressViewModel.updatepageNo(5)
//                progressViewModel.updateProgress(5)
//                R.id.action_adressFragment_to_weightFragment
//
//            }
//            5 -> {  progressViewModel.updatepageNo(6)
//                progressViewModel.updateProgress(6)
//                R.id.action_weightFragment_to_heightFragment
//
//            }
//            6 -> {  progressViewModel.updatepageNo(7)
//                progressViewModel.updateProgress(7)
//                R.id.action_heightFragment_to_chronicConditionFragment
//            }
//            7 -> {  progressViewModel.updatepageNo(8)
//                progressViewModel.updateProgress(7)
//                R.id.action_chronicConditionFragment_to_otherChronicDisease2
//            }
//            8 -> {  progressViewModel.updatepageNo(9)
//                progressViewModel.updateProgress(8)
//                R.id.action_otherChronicDisease_to_familyDiseaseFragment2
//            }
//            9 -> { progressViewModel.updatepageNo(10)
//                progressViewModel.updateProgress(9)
//                R.id.action_familyDiseaseFragment_to_createAccount2
//            }
//            10 -> { progressViewModel.updatepageNo(11)
//                progressViewModel.updateProgress(9)
//                R.id.action_createAccount2_to_accountSuccess
//            }
//            11 -> {  progressViewModel.updatepageNo(12)
//                progressViewModel.updateProgress(9)
//
//                R.id.action_accountSuccess_to_setPreferences
//            }
//            12 -> {  progressViewModel.updatepageNo(13)
//                progressViewModel.updateProgress(10)
//                R.id.action_setPreferences_to_setPreferenseFluidItake
//            }
//            13 -> { progressViewModel.updatepageNo(14)
//                progressViewModel.updateProgress(11)
//                R.id.action_setPreferenseFluidItake_to_completionScreen
//            }
//            else -> {
//                null
//            }
//        }
//    }


    private fun getNavigationActionForStep(step: Int): Int? {
        return when (step) {
            3 -> {
                updateVM(4, 4)
                R.id.action_bloodGroupFragment_to_adressFragment
            }
            4 -> {
                updateVM(5, 5)
                R.id.action_adressFragment_to_weightFragment
            }
            5 -> {
                updateVM(6, 6)
                R.id.action_weightFragment_to_heightFragment
            }
            6 -> {
                updateVM(7, 7)
                R.id.action_heightFragment_to_chronicConditionFragment
            }
            7 -> {
                updateVM(8, 7) // keep progress as 7
                R.id.action_chronicConditionFragment_to_otherChronicDisease2
            }
            8 -> {
                updateVM(9, 8)
                R.id.action_otherChronicDisease_to_familyDiseaseFragment2
            }
            9 -> {
                updateVM(10, 9)
                R.id.action_familyDiseaseFragment_to_createAccount2
            }
            10 -> {
                updateVM(11, 9)
                R.id.action_createAccount2_to_accountSuccess
            }
            11 -> {
                updateVM(12, 9)
                R.id.action_accountSuccess_to_setPreferences
            }
            12 -> {
                updateVM(13, 10)
                R.id.action_setPreferences_to_setPreferenseFluidItake
            }
            13 -> {
                updateVM(14, 11)
                R.id.action_setPreferenseFluidItake_to_completionScreen
            }
            else -> null
        }
    }

    private fun updateVM(pageNo: Int, progress: Int) {
        progressViewModel.updateProgress(progress)
        progressViewModel.updatepageNo(pageNo)
    }
    // Update the progress bar and step titles based on the current progress
    private fun updateProgress(step: Int,progressTitle: Int) {
        val totalSteps = 11
        val progressPercent = (step * 100) / totalSteps
        binding.progressBar.progress = progressPercent
        binding.tvProgressPercent.text = "$progressPercent%"

        when (progressTitle+1) {
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
                binding.tvStepTitle.text = "One Step Away "
                binding.tvStepSubtitle.text = "You're nearly done-just one final step!"
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