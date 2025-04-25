package com.critetiontech.ctvitalio.UI

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.ActivityDashboardBinding

class Home :  AppCompatActivity() {
    private lateinit var binding : ActivityDashboardBinding
    private var lastBackPressTime = 0L
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
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime < 2000) {
            super.onBackPressed()
            finishAffinity() // Closes all activities
        } else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
            lastBackPressTime = currentTime
        }
    }
}