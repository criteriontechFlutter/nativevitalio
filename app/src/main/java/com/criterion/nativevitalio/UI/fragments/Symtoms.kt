package com.criterion.nativevitalio.UI.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.FragmentAllergiesBinding
import com.criterion.nativevitalio.databinding.FragmentPillsReminderBinding
import com.criterion.nativevitalio.databinding.FragmentSymtomsBinding
import com.criterion.nativevitalio.viewmodel.PillsReminderViewModal
import com.criterion.nativevitalio.viewmodel.SymptomsViewModel

class Symtoms : Fragment() {
    // TODO: Rename and change types of parameters


    private lateinit var binding : FragmentSymtomsBinding
    private lateinit var viewModel: SymptomsViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSymtomsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel = ViewModelProvider(this)[SymptomsViewModel::class.java]

        viewModel.getAllPatientMedication()
    }


}