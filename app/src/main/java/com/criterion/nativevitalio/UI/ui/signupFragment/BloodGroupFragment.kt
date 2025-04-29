package com.criterion.nativevitalio.UI.ui.signupFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.FragmentBloodGroupBinding

class BloodGroupFragment : Fragment() {
    private lateinit var selectedButton: Button
    private lateinit var binding : FragmentBloodGroupBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBloodGroupBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val buttons = listOf(
            binding.btnAplus,
            binding.btnAminus,
            binding.btnBplus,
            binding.btnBminus,
            binding.btnOplus,
            binding.btnOminus,
            binding.btnABplus,
            binding.btnABminus,
        )

        buttons.forEach { button ->
            button.setOnClickListener {
                context?.let { it1 -> ContextCompat.getColor(it1, R.color.greyText2) }
                    ?.let { it2 -> selectedButton.setBackgroundColor(it2) }
                selectedButton = button
                context?.let { it1 -> ContextCompat.getColor(it1, R.color.primaryBlue) }
                    ?.let { it2 -> button.setBackgroundColor(it2) }
                binding.btnNext.isEnabled = true
            }
        }
        binding.btnNext.setOnClickListener {
            val selectedBloodGroup = selectedButton.text.toString()
        }
    }
}