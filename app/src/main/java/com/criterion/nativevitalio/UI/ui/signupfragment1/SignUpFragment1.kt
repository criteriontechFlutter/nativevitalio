package com.critetiontech.ctvitalio.UI.ui.signupfragment1

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.ui.signupFragment.SignUpFragment1ViewModel


class SignUpFragment1 : Fragment() {

    companion object {
        fun newInstance() = SignUpFragment1()
    }

    private val viewModel: SignUpFragment1ViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }
}