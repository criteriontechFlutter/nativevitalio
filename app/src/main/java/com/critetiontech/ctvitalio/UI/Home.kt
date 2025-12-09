package com.critetiontech.ctvitalio.UI

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.fragments.CorporateDashBoard
import com.critetiontech.ctvitalio.databinding.ActivityDashboardBinding
import com.critetiontech.ctvitalio.logging.LoggingManager
import com.google.android.material.snackbar.Snackbar

class Home :  BaseActivity() {
    private lateinit var binding : ActivityDashboardBinding
    private var lastBackPressTime: Long = 0
    private var backPressSnackbar:    Snackbar? = null
    private var lastScreen: String? = null
    private var screenEnterTime: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSystemBarsColor(
            statusBarColor = R.color.primaryColor,  // or custom color
            navBarColor = android.R.color.white,
            lightIcons = true
        )
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.my_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val now = System.currentTimeMillis()
            // 🔹 log when leaving previous screen
            lastScreen?.let { screen ->
                val duration = now - screenEnterTime
                LoggingManager.logEvent(
                    event = "page_exit",
                    screen = screen,
                    extras = mapOf(
                        "duration_ms" to duration,
                        "os" to "Android ${Build.VERSION.RELEASE}",
                        "deviceModel" to Build.MODEL
                    )
                )
            }
            // 🔹 log entering new screen
            val screenName = destination.label?.toString() ?: destination.id.toString()
            LoggingManager.logEvent(
                event = "page_view",
                screen = screenName,
                extras = mapOf(
                    "os" to "Android ${Build.VERSION.RELEASE}",
                    "deviceModel" to Build.MODEL
                )
            )
            lastScreen = screenName
            screenEnterTime = now
        }

    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val navController = findNavController(R.id.my_nav_host_fragment)
        val currentDestination = navController.currentDestination?.id

        if (currentDestination == R.id.dashboard) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackPressTime < 2000) {
                backPressSnackbar?.dismiss() // Hide any snack bar if already showing
                finishAffinity() // Exit app
            } else {
                lastBackPressTime = currentTime
                showExitSnackbar()
            }
        } else {
            super.onBackPressed()
        }
    }
    private fun showExitSnackbar() {
        backPressSnackbar = Snackbar.make(
            findViewById(android.R.id.content),
            "Press back again to exit",
            Snackbar.LENGTH_SHORT
        )
        backPressSnackbar?.setBackgroundTint(getColor(R.color.primaryColor))
        backPressSnackbar?.setTextColor(getColor(R.color.white))
        backPressSnackbar?.setActionTextColor(getColor(R.color.white))
        backPressSnackbar?.setAction("Exit") {
            finishAffinity()
        }
        backPressSnackbar?.show()
    }
}