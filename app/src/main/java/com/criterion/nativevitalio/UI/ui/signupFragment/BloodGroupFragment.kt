package com.criterion.nativevitalio.UI.ui.signupFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.FragmentBloodGroupBinding

class BloodGroupFragment : Fragment() {
//    private lateinit var selectedButton: Button
    private lateinit var binding : FragmentBloodGroupBinding
    private lateinit var selectedBloodGroup : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBloodGroupBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*buttons.forEach { button ->
            button.setOnClickListener {
                selectedButton = button
                context?.let { it1 -> ContextCompat.getDrawable(it1, R.drawable.gen_selected_bg) }
                    ?.let { it2 -> selectedButton.setBackgroundDrawable(it2) }
                context?.let { it1 -> ContextCompat.getDrawable(it1, R.drawable.gen_selected_bg) }
                    ?.let { it2 -> selectedButton.setBackgroundDrawable(it2) }
                binding.btnNext.isEnabled = true
            }
        }*/

        binding.btnAplus.setOnClickListener({
            binding.btnAplus.setBackgroundResource(R.drawable.blood_selected)
            binding.btnAminus.setBackgroundResource(R.drawable.blood_group)
            binding.btnBplus.setBackgroundResource(R.drawable.blood_group)
            binding.btnBminus.setBackgroundResource(R.drawable.blood_group)
            binding.btnABplus.setBackgroundResource(R.drawable.blood_group)
            binding.btnABminus.setBackgroundResource(R.drawable.blood_group)
            binding.btnOplus.setBackgroundResource(R.drawable.blood_group)
            binding.btnOminus.setBackgroundResource(R.drawable.blood_group)
            binding.btnAplus.setTextColor(resources.getColor(R.color.white))
            binding.btnAminus.setTextColor(resources.getColor(R.color.black))
            binding.btnBplus.setTextColor(resources.getColor(R.color.black))
            binding.btnBminus.setTextColor(resources.getColor(R.color.black))
            binding.btnABplus.setTextColor(resources.getColor(R.color.black))
            binding.btnABminus.setTextColor(resources.getColor(R.color.black))
            binding.btnOplus.setTextColor(resources.getColor(R.color.black))
            binding.btnOminus.setTextColor(resources.getColor(R.color.black))
            selectedBloodGroup="A"
        })
        binding.btnAminus.setOnClickListener({
            binding.btnAplus.setBackgroundResource(R.drawable.blood_group)
            binding.btnAminus.setBackgroundResource(R.drawable.blood_selected)
            binding.btnBplus.setBackgroundResource(R.drawable.blood_group)
            binding.btnBminus.setBackgroundResource(R.drawable.blood_group)
            binding.btnABplus.setBackgroundResource(R.drawable.blood_group)
            binding.btnABminus.setBackgroundResource(R.drawable.blood_group)
            binding.btnOplus.setBackgroundResource(R.drawable.blood_group)
            binding.btnOminus.setBackgroundResource(R.drawable.blood_group)
            binding.btnAplus.setTextColor(resources.getColor(R.color.black))
            binding.btnAminus.setTextColor(resources.getColor(R.color.white))
            binding.btnBplus.setTextColor(resources.getColor(R.color.black))
            binding.btnBminus.setTextColor(resources.getColor(R.color.black))
            binding.btnABplus.setTextColor(resources.getColor(R.color.black))
            binding.btnABminus.setTextColor(resources.getColor(R.color.black))
            binding.btnOplus.setTextColor(resources.getColor(R.color.black))
            binding.btnOminus.setTextColor(resources.getColor(R.color.black))
        })
        binding.btnBplus.setOnClickListener({
            binding.btnAplus.setBackgroundResource(R.drawable.blood_group)
            binding.btnAminus.setBackgroundResource(R.drawable.blood_group)
            binding.btnBplus.setBackgroundResource(R.drawable.blood_selected)
            binding.btnBminus.setBackgroundResource(R.drawable.blood_group)
            binding.btnABplus.setBackgroundResource(R.drawable.blood_group)
            binding.btnABminus.setBackgroundResource(R.drawable.blood_group)
            binding.btnOplus.setBackgroundResource(R.drawable.blood_group)
            binding.btnOminus.setBackgroundResource(R.drawable.blood_group)
            binding.btnAplus.setTextColor(resources.getColor(R.color.black))
            binding.btnAminus.setTextColor(resources.getColor(R.color.black))
            binding.btnBplus.setTextColor(resources.getColor(R.color.white))
            binding.btnBminus.setTextColor(resources.getColor(R.color.black))
            binding.btnABplus.setTextColor(resources.getColor(R.color.black))
            binding.btnABminus.setTextColor(resources.getColor(R.color.black))
            binding.btnOplus.setTextColor(resources.getColor(R.color.black))
            binding.btnOminus.setTextColor(resources.getColor(R.color.black))
        })
        binding.btnBminus.setOnClickListener({
            binding.btnAplus.setBackgroundResource(R.drawable.blood_group)
            binding.btnAminus.setBackgroundResource(R.drawable.blood_group)
            binding.btnBplus.setBackgroundResource(R.drawable.blood_group)
            binding.btnBminus.setBackgroundResource(R.drawable.blood_selected)
            binding.btnABplus.setBackgroundResource(R.drawable.blood_group)
            binding.btnABminus.setBackgroundResource(R.drawable.blood_group)
            binding.btnOplus.setBackgroundResource(R.drawable.blood_group)
            binding.btnOminus.setBackgroundResource(R.drawable.blood_group)
            binding.btnAplus.setTextColor(resources.getColor(R.color.black))
            binding.btnAminus.setTextColor(resources.getColor(R.color.black))
            binding.btnBplus.setTextColor(resources.getColor(R.color.black))
            binding.btnBminus.setTextColor(resources.getColor(R.color.white))
            binding.btnABplus.setTextColor(resources.getColor(R.color.black))
            binding.btnABminus.setTextColor(resources.getColor(R.color.black))
            binding.btnOplus.setTextColor(resources.getColor(R.color.black))
            binding.btnOminus.setTextColor(resources.getColor(R.color.black))
        })

        binding.btnABplus.setOnClickListener({
            binding.btnAplus.setBackgroundResource(R.drawable.blood_group)
            binding.btnAminus.setBackgroundResource(R.drawable.blood_group)
            binding.btnBplus.setBackgroundResource(R.drawable.blood_group)
            binding.btnBminus.setBackgroundResource(R.drawable.blood_group)
            binding.btnABplus.setBackgroundResource(R.drawable.blood_selected)
            binding.btnABminus.setBackgroundResource(R.drawable.blood_group)
            binding.btnOplus.setBackgroundResource(R.drawable.blood_group)
            binding.btnOminus.setBackgroundResource(R.drawable.blood_group)
            binding.btnAplus.setTextColor(resources.getColor(R.color.black))
            binding.btnAminus.setTextColor(resources.getColor(R.color.black))
            binding.btnBplus.setTextColor(resources.getColor(R.color.black))
            binding.btnBminus.setTextColor(resources.getColor(R.color.black))
            binding.btnABplus.setTextColor(resources.getColor(R.color.white))
            binding.btnABminus.setTextColor(resources.getColor(R.color.black))
            binding.btnOplus.setTextColor(resources.getColor(R.color.black))
            binding.btnOminus.setTextColor(resources.getColor(R.color.black))
        })
        binding.btnABminus.setOnClickListener({
            binding.btnAplus.setBackgroundResource(R.drawable.blood_group)
            binding.btnAminus.setBackgroundResource(R.drawable.blood_group)
            binding.btnBplus.setBackgroundResource(R.drawable.blood_group)
            binding.btnBminus.setBackgroundResource(R.drawable.blood_group)
            binding.btnABplus.setBackgroundResource(R.drawable.blood_group)
            binding.btnABminus.setBackgroundResource(R.drawable.blood_selected)
            binding.btnOplus.setBackgroundResource(R.drawable.blood_group)
            binding.btnOminus.setBackgroundResource(R.drawable.blood_group)
            binding.btnAplus.setTextColor(resources.getColor(R.color.black))
            binding.btnAminus.setTextColor(resources.getColor(R.color.black))
            binding.btnBplus.setTextColor(resources.getColor(R.color.black))
            binding.btnBminus.setTextColor(resources.getColor(R.color.black))
            binding.btnABplus.setTextColor(resources.getColor(R.color.black))
            binding.btnABminus.setTextColor(resources.getColor(R.color.white))
            binding.btnOplus.setTextColor(resources.getColor(R.color.black))
            binding.btnOminus.setTextColor(resources.getColor(R.color.black))
        })
        binding.btnOplus.setOnClickListener({
            binding.btnAplus.setBackgroundResource(R.drawable.blood_group)
            binding.btnAminus.setBackgroundResource(R.drawable.blood_group)
            binding.btnBplus.setBackgroundResource(R.drawable.blood_group)
            binding.btnBminus.setBackgroundResource(R.drawable.blood_group)
            binding.btnABplus.setBackgroundResource(R.drawable.blood_group)
            binding.btnABminus.setBackgroundResource(R.drawable.blood_group)
            binding.btnOplus.setBackgroundResource(R.drawable.blood_selected)
            binding.btnOminus.setBackgroundResource(R.drawable.blood_group)
            binding.btnAplus.setTextColor(resources.getColor(R.color.black))
            binding.btnAminus.setTextColor(resources.getColor(R.color.black))
            binding.btnBplus.setTextColor(resources.getColor(R.color.black))
            binding.btnBminus.setTextColor(resources.getColor(R.color.black))
            binding.btnABplus.setTextColor(resources.getColor(R.color.black))
            binding.btnABminus.setTextColor(resources.getColor(R.color.black))
            binding.btnOplus.setTextColor(resources.getColor(R.color.white))
            binding.btnOminus.setTextColor(resources.getColor(R.color.black))
        })
        binding.btnOminus.setOnClickListener({
            binding.btnAplus.setBackgroundResource(R.drawable.blood_group)
            binding.btnAminus.setBackgroundResource(R.drawable.blood_group)
            binding.btnBplus.setBackgroundResource(R.drawable.blood_group)
            binding.btnBminus.setBackgroundResource(R.drawable.blood_group)
            binding.btnABplus.setBackgroundResource(R.drawable.blood_group)
            binding.btnABminus.setBackgroundResource(R.drawable.blood_group)
            binding.btnOplus.setBackgroundResource(R.drawable.blood_group)
            binding.btnOminus.setBackgroundResource(R.drawable.blood_selected)
            binding.btnAplus.setTextColor(resources.getColor(R.color.black))
            binding.btnAminus.setTextColor(resources.getColor(R.color.black))
            binding.btnBplus.setTextColor(resources.getColor(R.color.black))
            binding.btnBminus.setTextColor(resources.getColor(R.color.black))
            binding.btnABplus.setTextColor(resources.getColor(R.color.black))
            binding.btnABminus.setTextColor(resources.getColor(R.color.black))
            binding.btnOplus.setTextColor(resources.getColor(R.color.black))
            binding.btnOminus.setTextColor(resources.getColor(R.color.white))
        })

        binding.btnNext.setOnClickListener {
//            val selectedBloodGroup = selectedButton.text.toString()
            findNavController().navigate(R.id.action_bloodGroupFragment_to_weightFragment);
        }
    }
}