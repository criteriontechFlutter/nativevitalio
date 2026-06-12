package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentGroundingTecniqueViewBinding

class GroundingTecniqueView : Fragment() {

    private var _binding: FragmentGroundingTecniqueViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroundingTecniqueViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        val exerciseName = arguments?.getString("exerciseName")
        val exerciseDescription = arguments?.getString("exerciseDescription")
        val benefits = arguments?.getStringArrayList("benefits") ?: arrayListOf()

        exerciseName?.let { binding.tvExerciseTitle.text = it }
        exerciseDescription?.let { binding.tvExerciseDescription.text = it }

        populateBenefits(benefits)

        binding.btnStart.setOnClickListener {
            findNavController().navigate(R.id.action_groundingTecniqueView_to_fivetoOneTechniqueProgress)
        }
    }

    private fun populateBenefits(benefits: List<String>) {
        binding.benefitsContainer.removeAllViews()
        benefits.forEachIndexed { index, benefit ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                if (index < benefits.lastIndex) params.bottomMargin = dpToPx(12)
                layoutParams = params
            }

            val icon = ImageView(requireContext()).apply {
                val iconParams = LinearLayout.LayoutParams(dpToPx(20), dpToPx(20))
                iconParams.marginEnd = dpToPx(10)
                layoutParams = iconParams
                setImageResource(R.drawable.ic_check_circle_green)
            }

            val text = TextView(requireContext()).apply {
                text = benefit
                textSize = 15f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.themeTextColorBW))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            row.addView(icon)
            row.addView(text)
            binding.benefitsContainer.addView(row)
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}