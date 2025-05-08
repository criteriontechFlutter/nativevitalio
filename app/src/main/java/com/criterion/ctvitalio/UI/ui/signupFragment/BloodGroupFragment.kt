package com.critetiontech.ctvitalio.UI.ui.signupFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.BloodGroupAdapter
import com.critetiontech.ctvitalio.databinding.FragmentBloodGroupBinding
import com.critetiontech.ctvitalio.utils.ToastUtils

class BloodGroupFragment : Fragment() {
//    private lateinit var selectedButton: Button
    private lateinit var binding : FragmentBloodGroupBinding
    private lateinit var selectedBloodGroup : String
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
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

        val adapter = BloodGroupAdapter(bloodGroups) { selected ->

            ToastUtils.showSuccessPopup(requireContext(),"Selected: $selected")
        }

        binding.rvBloodGroups.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.rvBloodGroups.adapter = adapter



        binding.btnNext.setOnClickListener {
//            val selectedBloodGroup = selectedButton.text.toString()
            findNavController().navigate(R.id.action_bloodGroupFragment_to_adressFragment);
        }
    }
}