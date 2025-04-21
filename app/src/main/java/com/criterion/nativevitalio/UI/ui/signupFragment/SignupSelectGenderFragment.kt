package com.critetiontech.ctvitalio.UI.ui.signupFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentSignupSelectGenderBinding

class SignupSelectGenderFragment : Fragment() {
    private lateinit var binding : FragmentSignupSelectGenderBinding
    private var selectedGender: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignupSelectGenderBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnMale.setOnClickListener {
            selectedGender = "Male"
            binding.btnMale.setBackgroundResource(R.drawable.gen_selected_bg)
            binding.btnFemale.setBackgroundResource(R.drawable.gen_bg)
            binding.btnNext.isEnabled = true
        }
        binding.btnFemale.setOnClickListener {
            selectedGender = "Female"
            binding.btnMale.setBackgroundResource(R.drawable.gen_bg)
            binding.btnFemale.setBackgroundResource(R.drawable.gen_selected_bg)
            binding.btnNext.isEnabled = true
        }
        binding.btnNext.setOnClickListener {

        }
    }
}