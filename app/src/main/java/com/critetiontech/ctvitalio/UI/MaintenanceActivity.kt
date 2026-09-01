package com.critetiontech.ctvitalio.UI

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.OmronActivity.SplashScreen
import com.critetiontech.ctvitalio.databinding.ActivityMaintenanceBinding
import com.critetiontech.ctvitalio.remoteconfig.RemoteConfigManager
import kotlinx.coroutines.launch

/**
 * MaintenanceActivity.kt
 * ─────────────────────────────────────────────────────────────────────────────
 * Shown when Remote Config flag [RemoteConfigManager.isMaintenanceMode] == true.
 *
 * • Displays the custom title, message and contact email from Remote Config.
 * • "Check Again" button re-fetches config; if maintenance is lifted, it
 *   restarts the Splash screen so the user resumes normal flow.
 * • Back button is disabled so the user cannot navigate past this screen.
 * ─────────────────────────────────────────────────────────────────────────────
 */
class MaintenanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMaintenanceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_maintenance)

        bindRemoteConfigValues()
        setupClickListeners()
    }

    // ─────────────────────────────────────────────────────────────────────────

    private fun bindRemoteConfigValues() {
        binding.title   = RemoteConfigManager.maintenanceTitle
        binding.message = RemoteConfigManager.maintenanceMessage
        binding.email   = RemoteConfigManager.maintenanceContactEmail
        binding.executePendingBindings()
    }

    private fun setupClickListeners() {
        // Retry: re-fetch config and check if maintenance is still on
        binding.btnRetry.setOnClickListener {
            binding.btnRetry.isEnabled = false
            lifecycleScope.launch {
                RemoteConfigManager.fetchAndActivate()

                if (!RemoteConfigManager.isMaintenanceMode) {
                    // Maintenance lifted — go back to Splash
                    val intent = Intent(this@MaintenanceActivity, SplashScreen::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    // Still in maintenance — refresh text (title/message may have changed)
                    bindRemoteConfigValues()
                    binding.btnRetry.isEnabled = true
                }
            }
        }

        // Email tap — open mail client
        binding.tvContactEmail.setOnClickListener {
            val email = RemoteConfigManager.maintenanceContactEmail
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
            startActivity(Intent.createChooser(intent, "Contact Support"))
        }
    }

    /** Disable back navigation during maintenance. */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        // intentionally blocked
    }
}
