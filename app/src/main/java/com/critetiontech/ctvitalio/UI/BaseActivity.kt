package com.critetiontech.ctvitalio.UI

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Allow drawing behind system bars
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    }

    /**
     * Set status and navigation bar colors dynamically
     */
    fun setSystemBarsColor(statusBarColor: Int, navBarColor: Int, lightIcons: Boolean = true) {
        // Set colors
        window.statusBarColor = ContextCompat.getColor(this, statusBarColor)
        window.navigationBarColor = ContextCompat.getColor(this, navBarColor)

        // Handle icon colors
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = lightIcons
        insetsController.isAppearanceLightNavigationBars = lightIcons
    }

    fun setSystemBarsColorInt(statusColorInt: Int, navColorInt: Int, lightIcons: Boolean = true) {
        window.statusBarColor = statusColorInt
        window.navigationBarColor = navColorInt

        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = lightIcons
        insetsController.isAppearanceLightNavigationBars = lightIcons
    }

}