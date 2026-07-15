package com.critetiontech.ctvitalio.widgets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.LayoutBottomGymOfferBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class GymOfferBottomSheet : BottomSheetDialogFragment() {

    private var _binding: LayoutBottomGymOfferBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = LayoutBottomGymOfferBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*
        binding.txtRemindLater.setOnClickListener {
        dismiss()
        }
        */

        binding.imgClose.setOnClickListener {
            dismiss()
        }
        binding.txtRemindLater.setOnClickListener {
            dismiss()
        }

        binding.btnJoinGym.setOnClickListener {
            dismiss()
            findNavController().navigate(R.id.action_dashboard_to_gymListFragment)
        }
        /*
        binding.btnJoinGym.setOnClickListener {

        Toast.makeText(requireContext(),"Opening Gym List",Toast.LENGTH_SHORT).show()

        dismiss()
        }
        */
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}