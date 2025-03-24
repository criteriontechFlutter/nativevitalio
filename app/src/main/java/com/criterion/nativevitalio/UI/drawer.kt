package com.criterion.nativevitalio.UI

import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.ActivityDrawerBinding
import com.google.android.material.navigation.NavigationView

open class drawer : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var activityDrawerBinding: ActivityDrawerBinding
    private lateinit var navigationView: NavigationView

    override fun setContentView(layoutResID: Int) {
        val fullView = LayoutInflater.from(this).inflate(R.layout.activity_drawer, null)
        val content = fullView.findViewById<FrameLayout>(R.id.base_content)

        LayoutInflater.from(this).inflate(layoutResID, content, true)
        super.setContentView(fullView)

//        drawerLayout = fullView.findViewById(R.id.drawer_layout)
//        navigationView = fullView.findViewById(R.id.navigation_view)

        // 🔹 Get header view and find the inner clickable section
        val headerView = activityDrawerBinding.navigationView.getHeaderView(0)
        val profileSection = headerView.findViewById<LinearLayout>(R.id.header_profile_section)

        profileSection.setOnClickListener {
            // Perform action and close drawer
            closeDrawer()
        }

        setupNavigation()
    }
    private fun setupNavigation() {
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
                    // Handle profile navigation
                    true
                }
                R.id.nav_settings -> {
                    // Handle settings
                    true
                }
                R.id.nav_logout -> {
                    // Handle logout
                    true
                }
                else -> false
            }.also {
                activityDrawerBinding.drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
    }

    fun openDrawer() {
        activityDrawerBinding.drawerLayout.openDrawer(GravityCompat.START)
    }
    fun closeDrawer() {
        activityDrawerBinding.drawerLayout.closeDrawer(GravityCompat.START)
    }
}