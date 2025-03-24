package com.criterion.nativevitalio.UI

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.adapter.MovieAdapter
import com.criterion.nativevitalio.databinding.ActivityDashboardBinding

class Dashboard :  AppCompatActivity() {

    private lateinit var dashboardAdopter : ActivityDashboardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        dashboardAdopter= ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(dashboardAdopter.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
//
        dashboardAdopter. profileSection.setOnClickListener {
            val intent = Intent(this, drawer::class.java)
            startActivity(intent)
        }

        dashboardAdopter.vitalsDetails .setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

    }
}