package com.criterion.nativevitalio.UI

import PrefsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.UI.fragments.Dashboard
import com.criterion.nativevitalio.Utils.MyApplication
import java.util.prefs.Preferences
class Splash : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
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