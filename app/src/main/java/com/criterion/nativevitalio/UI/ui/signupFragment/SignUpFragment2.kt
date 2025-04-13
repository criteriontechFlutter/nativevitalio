package com.criterion.nativevitalio.UI.ui.signupFragment

import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.FragmentSignUp2Binding

class SignUpFragment2 : Fragment() {
    private lateinit var binding : FragmentSignUp2Binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUp2Binding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.edtFirstName.addTextChangedListener { text: Editable? -> if (binding.edtFirstName.text?.isNotEmpty() == true){
            binding.nextButton.isEnabled=true
        }else binding.nextButton.isEnabled=false
        }
        binding.nextButton.setOnClickListener {
            findNavController().navigate(R.id.action_nameFragment_to_genderFragment);

        }
    }
}