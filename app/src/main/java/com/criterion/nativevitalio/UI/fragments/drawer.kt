package com.criterion.nativevitalio.UI.fragments

import PrefsManager
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.FragmentDrawerBinding
import com.criterion.nativevitalio.utils.MyApplication


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
        binding.darkModeRow.root.setOnClickListener {
            //PrefsManager().clearPatient()
            findNavController().navigate(R.id.action_drawer4_to_settingsFragmentVitalio)

        }

        binding.userName.text = PrefsManager().getPatient()!!.patientName
        binding.userUhid.text = PrefsManager().getPatient()!!.uhID
        Glide.with(MyApplication.appContext) // or `this` if inside Activity
            .load(PrefsManager().getPatient()!!.profileUrl) // or R.drawable.image
            .placeholder(com.criterion.nativevitalio.R.drawable.baseline_person_24)
            .circleCrop() // optional: makes it circular
            .into(binding.userImage)

        binding.backDrawer.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.logoutMenu.setOnClickListener {
            val popupView: View =
                LayoutInflater.from(context).inflate(R.layout.layout_logout_popup, null)
            val popupWindow = PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )

            popupWindow.elevation = 10f


            // Optional: handle logout click
            popupView.findViewById<View>(R.id.logoutText).setOnClickListener { view: View? ->
                popupWindow.dismiss()

                val dialogView =
                    LayoutInflater.from(context).inflate(R.layout.dialog_logout_app, null)

                val dialog = context?.let { it1 ->
                    AlertDialog.Builder(it1, R.style.BottomDialogTheme)
                        .setView(dialogView)
                        .create()
                }

                dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()

// ⚙ Fix width and gravity
                dialog.window?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                dialog.window?.setGravity(Gravity.BOTTOM)

// Button listeners
                dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener {
                    dialog.dismiss()
                }
                dialogView.findViewById<View>(R.id.btnRemove).setOnClickListener {
                    dialog.dismiss()
                    // Your logout logic
                }


            }


            // Show below the menu icon
            popupWindow.showAsDropDown(binding.logoutMenu, -200, -75)

        }

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