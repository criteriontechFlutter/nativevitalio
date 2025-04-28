package com.critetiontech.ctvitalio.UI

import android.os.Bundle
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.fragments.FluidFragment

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.progressBar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        navController = navHostFragment.navController

        setupActionBarWithNavController(navController)

        // Observe progress changes and update progress bar

        val destination = intent?.getStringExtra("navigate_to")
        if (destination == "voice_socket") {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FluidFragment())
                .commit()
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}