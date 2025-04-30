package com.criterion.nativevitalio.UI.ui.signupfragment1

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.criterion.nativevitalio.UI.ui.signupFragment.SignUpFragment1ViewModel


class SignUpFragments1 : Fragment() {

    companion object {
        fun newInstance() = SignUpFragments1()
    }

    private val viewModel: SignUpFragment1ViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }
}