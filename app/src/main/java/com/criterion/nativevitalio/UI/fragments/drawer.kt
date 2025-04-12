package com.criterion.nativevitalio.UI.fragments

import PrefsManager
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.FragmentDashboardBinding
import com.criterion.nativevitalio.databinding.FragmentDrawerBinding


class drawer : Fragment() {


    private lateinit var binding: FragmentDrawerBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDrawerBinding.inflate(inflater, container, false)
        return binding.root
        // Inflate the layout for this fragment

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.allergiesRow.root.setOnClickListener {
            findNavController().navigate(R.id.action_drawer4_to_allergies3)

        }
//        binding.btnEditProfile.setOnClickListener {
//           val intent=Intent()
//
//        }


        initDrawerLayout()

    }

    @SuppressLint("SetTextI18n")
    private fun initDrawerLayout() {
        // Personal Info
        binding.personalInfoRow.title.text = getString(R.string.personal_info)
        binding.personalInfoRow.icon.setImageResource(R.drawable.ic_personal_info)

        binding.allergiesRow.title.text = getString(R.string.allergies)
        binding.allergiesRow.icon.setImageResource(R.drawable.ic_allergies)
        binding.allergiesRow.count.text = "2"

        // Observer & Smartwatch
        binding.myObserverRow.title.text = getString(R.string.my_observer)
        binding.myObserverRow.count.text = "4"
        binding.myObserverRow.icon.setImageResource(R.drawable.ic_myobserver)

        binding.sharedAccountRow.title.text = getString(R.string.shared_accounts)
        binding.sharedAccountRow.icon.setImageResource(R.drawable.ic_shared)

        binding.connectSmartWatchRow.title.text = getString(R.string.connect_smart_watch)
        binding.connectSmartWatchRow.icon.setImageResource(R.drawable.ic_smartwatch)

        binding.emergencyContactRow.title.text = getString(R.string.emergency_contacts)
        binding.emergencyContactRow.icon.setImageResource(R.drawable.ic_emergency_contact)

        binding.familyHealthHistoryRow.title.text = getString(R.string.family_health_history)
        binding.familyHealthHistoryRow.icon.setImageResource(R.drawable.ic_health_history)

        // Settings Section
        binding.languageRow.title.text = getString(R.string.language)
        binding.languageRow.count.text = "English"
        binding.languageRow.icon.setImageResource(R.drawable.ic_language)

        binding.darkModeRow.title.text = getString(R.string.dark_mode)
        binding.darkModeRow.icon.setImageResource(R.drawable.ic_theme)

        binding.FAQsRow.title.text = getString(R.string.f_q)
        binding.FAQsRow.icon.setImageResource(R.drawable.ic_faqs)

        binding.feedbackRow.title.text = getString(R.string.feedback)
        binding.feedbackRow.icon.setImageResource(R.drawable.ic_feedback)
    }

}