package com.criterion.nativevitalio.UI.ui.signupFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.FragmentAccountSuccessBinding


class AccountSuccess : Fragment() {

    private lateinit var binding: FragmentAccountSuccessBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle the "Set Preferences" button click
        binding.btnSetPreferences.setOnClickListener {
            findNavController().navigate(R.id.action_accountSuccess_to_setPreferences)
        }
    }
}