package com.critetiontech.ctvitalio.UI

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.ActivityDashboardBinding
import com.google.android.material.snackbar.Snackbar

class Home :  AppCompatActivity() {
    private lateinit var binding : ActivityDashboardBinding
    private var lastBackPressTime: Long = 0
    private var backPressSnackbar:    Snackbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //val profileSection = findViewById<ImageView>(R.id.profile_image)
//        profileSection.setOnClickListener {
//            val intent = Intent(this, drawer::class.java)
//            startActivity(intent)
//        }
    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val navController = findNavController(R.id.my_nav_host_fragment)
        val currentDestination = navController.currentDestination?.id

        if (currentDestination == R.id.dashboard) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackPressTime < 2000) {
                backPressSnackbar?.dismiss() // Hide any snackbar if already showing
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