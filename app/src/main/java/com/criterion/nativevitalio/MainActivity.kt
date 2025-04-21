package com.critetiontech.ctvitalio

import NetworkUtils
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.critetiontech.ctvitalio.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val themePref = PreferenceManager.getDefaultSharedPreferences(this)
            .getString("theme_pref", NetworkUtils.ThemeHelper.MODE_DARK) ?: NetworkUtils.ThemeHelper.MODE_DARK

        NetworkUtils.ThemeHelper.applyTheme(themePref)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }




    }
}