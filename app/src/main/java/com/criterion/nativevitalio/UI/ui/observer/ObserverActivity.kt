package com.criterion.nativevitalio.UI.ui.observer

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.ActivityObserverBinding

class ObserverActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityObserverBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityObserverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.bottomNav

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_observer) as NavHostFragment
        navController = navHostFragment.navController
        binding.backButton.setOnClickListener{
            super.onBackPressed()
        }

        navView.setupWithNavController(navController)
        /*binding.bottomNav.setOnItemSelectedListener {menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
//                    findNavController(R.id.nav_home).navigate(R.id.action_dashboard_to_chatFragment)
                    true
                }
                R.id.nav_vitals -> {
                    navController.navigate(R.id.action_homeFragment_to_vitalsFragment)
                    true
                }
                R.id.nav_fluid -> {
                    navController.navigate(R.id.action_homeFragment_to_fluidIntakeObserverFragment)
                    true
                }
                R.id.nav_medication -> {
                    navController.navigate(R.id.action_homeFragment_to_medicationFragment)
                    true
                }
                R.id.nav_diet -> {
                    navController.navigate(R.id.action_homeFragment_to_dietFragment)
                    true
                }
                else -> false
            }
        }*/

        /*val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.vitalsFragment,
                R.id.fluidIntakeObserverFragment,
                R.id.medicationFragment,
                R.id.dietFragment
            )
        )*/
        binding.bottomNav.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}