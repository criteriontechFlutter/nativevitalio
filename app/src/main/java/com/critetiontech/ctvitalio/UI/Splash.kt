package com.critetiontech.ctvitalio.UI

import PrefsManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.Login
import com.critetiontech.ctvitalio.databinding.ActivitySplashBinding
import com.critetiontech.ctvitalio.utils.FCMHelper
import com.critetiontech.ctvitalio.utils.MyApplication
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Splash : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        try {

            FirebaseApp.initializeApp(MyApplication.appContext)
            FCMHelper.initializeFCM(
                onTokenReceived = { token ->
                    // You can send this token to your backend
                    Log.d("MainActivity", "Device token: $token")
                },
                onError = { error ->
                    Log.e("MainActivity", "Error getting token: ${error.message}")
                }
            )
            playSplashVideo()

        } catch (e: Exception) {
            Log.e("SplashActivity", "Error during Splash Screen Navigation: ${e.message}")
            startActivity(Intent(this, Login::class.java)) // Safe fallback to login
        }

    }

    private fun playSplashVideo() {
        try {
            // Set the video URI from the raw folder
            val videoUri = Uri.parse("android.resource://${packageName}/${R.raw.microsoft_teams}")
            binding.videoView.setVideoURI(videoUri)
            binding.videoView.setOnCompletionListener {
                navigateNextScreen()
            }
            binding.videoView.setOnErrorListener { mp, what, extra ->
                Log.e("SplashActivity", "Video error: $what")
                navigateNextScreen() // fallback
                true
            }
            binding.videoView.start()
        } catch (e: Exception) {
            Log.e("SplashActivity", "Error playing video: ${e.message}")
            navigateNextScreen()
        }
    }

    private fun navigateNextScreen() {
        try {
            val currentPatientUHID = PrefsManager().getPatient()?.patientName.toString()

            if (currentPatientUHID.isNotEmpty() && currentPatientUHID != "" && currentPatientUHID!="null") {
                startActivity(Intent(this, Home::class.java))
            } else {
                startActivity(Intent(this, Login::class.java))
            }
            finish()
        } catch (e: Exception) {
            Log.e("SplashActivity", "Navigation error: ${e.message}")
            startActivity(Intent(this, Login::class.java))
            finish()
        }
    }
}