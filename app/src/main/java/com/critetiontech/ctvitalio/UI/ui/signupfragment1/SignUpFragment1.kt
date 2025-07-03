package com.critetiontech.ctvitalio.UI.ui.signupfragment1

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.critetiontech.ctvitalio.UI.ui.signupFragment.SignUpFragment1ViewModel


class SignUpFragments1 : Fragment() {

    companion object {
        fun newInstance() = SignUpFragments1()
    }

    private val viewModel: SignUpFragment1ViewModel by viewModels()

}