package com.criterion.nativevitalio.UI.ui.signupFragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.FragmentSignupSelectGenderBinding

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
            binding.llMale.setBackgroundResource(R.drawable.gen_selected_bg)
            binding.llFemale.setBackgroundResource(R.drawable.gen_bg)
            binding.txtMale.setTextColor(resources.getColor(R.color.white))
            binding.txtFemale.setTextColor(resources.getColor(R.color.black))
            binding.btnNext.isEnabled = true
        }
        binding.btnFemale.setOnClickListener {
            selectedGender = "Female"
            binding.llMale.setBackgroundResource(R.drawable.gen_bg)
            binding.llFemale.setBackgroundResource(R.drawable.gen_selected_bg)
            binding.txtFemale.setTextColor(resources.getColor(R.color.white))
            binding.txtMale.setTextColor(resources.getColor(R.color.black))
            binding.btnNext.isEnabled = true
        }
        binding.btnNext.setOnClickListener {
            findNavController().navigate(R.id.action_genderFragment_to_dobFragment);
        }
    }
}