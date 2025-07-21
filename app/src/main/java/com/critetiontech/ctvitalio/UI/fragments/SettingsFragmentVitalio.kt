package com.critetiontech.ctvitalio.UI.fragments

import NetworkUtils
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentSettingsBinding


class SettingsFragmentVitalio : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var prefs: SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        prefs = PreferenceManager.getDefaultSharedPreferences(activity)

        // Restore current selection
        when (prefs.getString("theme_pref", NetworkUtils.ThemeHelper.MODE_SYSTEM)) {
            NetworkUtils.ThemeHelper.MODE_LIGHT ->  binding.themeRadioGroup.check(R.id.radioLight)
            NetworkUtils.ThemeHelper.MODE_DARK ->  binding.themeRadioGroup.check(R.id.radioDark)
            NetworkUtils.ThemeHelper.MODE_SYSTEM ->  binding.themeRadioGroup.check(R.id.radioSystem)
        }

        // Listen for change
        binding.themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedTheme = when (checkedId) {
                R.id.radioLight -> NetworkUtils.ThemeHelper.MODE_LIGHT
                R.id.radioDark -> NetworkUtils.ThemeHelper.MODE_DARK
                else -> NetworkUtils.ThemeHelper.MODE_SYSTEM
            }

            // Save & apply
            prefs.edit().putString("theme_pref", selectedTheme).apply()
            NetworkUtils.ThemeHelper.applyTheme(selectedTheme)

            // Restart to apply
//            val intent = Intent(activity, Dashboard::class.java)
//          //  intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            startActivity(intent)
        }
    }


}