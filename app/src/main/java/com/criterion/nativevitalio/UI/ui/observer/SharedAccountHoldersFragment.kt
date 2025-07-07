package com.criterion.nativevitalio.UI.ui.observer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.FragmentDobBinding
import com.criterion.nativevitalio.databinding.FragmentObserverSharedDashboardBinding
import com.criterion.nativevitalio.databinding.FragmentSharedAccountHoldersBinding

class SharedAccountHoldersFragment : Fragment() {
    private lateinit var binding : FragmentSharedAccountHoldersBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSharedAccountHoldersBinding.inflate(inflater, container, false)
        return binding.root
    }
}