package com.criterion.nativevitalio.UI.ui.signupFragment;

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.FragmentDobBinding

class DobFragment : Fragment() {
    private lateinit var binding : FragmentDobBinding

    private lateinit var progressViewModel: ProgressViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDobBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressViewModel = ViewModelProvider(this)[ProgressViewModel::class.java]

        binding.btnNext.setOnClickListener {

            progressViewModel.updateProgress(3)
            findNavController().navigate(R.id.action_dobFragment_to_bloodGroupFragment);
        }
    }
}