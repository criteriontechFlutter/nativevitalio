package com.critetiontech.ctvitalio.UI

import PrefsManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.critetiontech.ctvitalio.UI.Login
import com.critetiontech.ctvitalio.databinding.ActivitySplashBinding
import com.critetiontech.ctvitalio.utils.FCMHelper
import com.critetiontech.ctvitalio.utils.MyApplication
import com.google.firebase.FirebaseApp

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

            // Check if user data is saved locally using PrefsManager
            val currentPatientUHID = PrefsManager().currentPatientUHID

            Log.d("RESPONSE", "responseValue: $currentPatientUHID")

            // Navigate to the appropriate screen
            if (!currentPatientUHID.isNullOrEmpty()) {
                startActivity(Intent(this, Home::class.java))
            } else {
                startActivity(Intent(this, Login::class.java))
            }

        } catch (e: Exception) {
            Log.e("SplashActivity", "Error during Splash Screen Navigation: ${e.message}")
            startActivity(Intent(this, Login::class.java)) // Safe fallback to login
        }

        finish() // Prevent going back to splash
    }
}