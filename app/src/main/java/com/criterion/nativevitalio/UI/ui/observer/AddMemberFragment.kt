package com.criterion.nativevitalio.UI.ui.observer

import android.os.Bundle
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.FragmentAddMemberBinding
import com.criterion.nativevitalio.databinding.FragmentDobBinding
import com.criterion.nativevitalio.databinding.FragmentObserverSharedDashboardBinding

class AddMemberFragment : Fragment() {
    private lateinit var binding : FragmentAddMemberBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddMemberBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.edtCode.doAfterTextChanged {
            if(binding.edtCode.text.isEmpty()){
                !binding.btnSubmit.isEnabled
            }else binding.btnSubmit.isEnabled
        }

        binding.btnSubmit.setOnClickListener {

        }
    }

}