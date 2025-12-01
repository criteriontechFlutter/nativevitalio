package com.critetiontech.ctvitalio.UI.ui.signupFragment

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.viewmodel.RegistrationViewModel
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.Home
import com.critetiontech.ctvitalio.databinding.FragmentCompletionScreenBinding
import com.critetiontech.ctvitalio.utils.LoaderUtils.hideLoading
import com.critetiontech.ctvitalio.utils.LoaderUtils.showLoading
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class CompletionScreen : Fragment() {


        private lateinit var binding: FragmentCompletionScreenBinding
        private lateinit var viewModel: RegistrationViewModel

    private lateinit var progressViewModel: ProgressViewModel
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            binding = FragmentCompletionScreenBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            viewModel = ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]
            viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) showLoading() else hideLoading()
            }

            startJumpingAnimation(binding.dotsContainer)
            progressViewModel = ViewModelProvider(requireActivity())[ProgressViewModel::class.java]
            lifecycleScope.launch {
                progressViewModel.updateProgress(11)
                progressViewModel.updatepageNo(14)
                delay(2000) // 2000 ms = 2 seconds
//                findNavController().navigate(R.id.action_completionScreen_to_completionDashboardReady)

                val intent = Intent(requireContext(), Home::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                requireContext().startActivity(intent)
            }
        }
    private fun startJumpingAnimation(dotsContainer: LinearLayout) {
        val activeColor = ContextCompat.getColor(dotsContainer.context, R.color.primaryColor) // Define in colors.xml
        val originalColor = ContextCompat.getColor(dotsContainer.context, R.color.greyBG) // Define in colors.xml

        for (i in 0 until dotsContainer.childCount) {
            val dot = dotsContainer.getChildAt(i)

            // Jump animator
            val jumpAnim = ObjectAnimator.ofFloat(dot, "translationY", 0f, -20f).apply {
                duration = 300
                repeatMode = ObjectAnimator.REVERSE
                repeatCount = ObjectAnimator.INFINITE
                startDelay = i * 100L
            }

            // Color animator
            val colorAnim = ValueAnimator.ofObject(ArgbEvaluator(), originalColor, activeColor, originalColor).apply {
                duration = 600
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.RESTART
                startDelay = i * 100L
                addUpdateListener { animator ->
                    val color = animator.animatedValue as Int
                    dot.background.setTint(color)
                }
            }

            jumpAnim.start()
            colorAnim.start()
        }
    }
    }