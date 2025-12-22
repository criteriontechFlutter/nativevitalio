package com.criterion.nativevitalio.UI.ui.signupFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.FragmentAdressBinding
import com.criterion.nativevitalio.databinding.FragmentHeightBinding
import com.criterion.nativevitalio.databinding.FragmentVitalReminderBinding

class FragmentVitalReminder : Fragment() {
    private lateinit var binding : FragmentVitalReminderBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVitalReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.btnNext.setOnClickListener {
//            findNavController().navigate(R.id.action_fragmentVitalReminder_to_fluidFragment2)
//        }
    }
}