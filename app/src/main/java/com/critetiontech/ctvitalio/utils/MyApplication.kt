package com.critetiontech.ctvitalio.utils

import NetworkUtils
import android.app.Application
import android.content.Context
import android.preference.PreferenceManager

class MyApplication : Application() {
    companion object {
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val themePref = prefs.getString("theme_pref", NetworkUtils.ThemeHelper.MODE_SYSTEM)
        NetworkUtils.ThemeHelper.applyTheme(themePref ?: NetworkUtils.ThemeHelper.MODE_SYSTEM)
    }

}