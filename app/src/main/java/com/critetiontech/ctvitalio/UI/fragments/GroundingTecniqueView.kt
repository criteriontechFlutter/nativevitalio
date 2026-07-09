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
        val exerciseId = arguments?.getString("exerciseId")
        val exerciseDescription = arguments?.getString("exerciseDescription")
        val benefits = arguments?.getStringArrayList("benefits") ?: arrayListOf()

        exerciseName?.let {
            binding.tvExerciseTitle.text = it
            val iconRes = when (it) {
                "5-4-3-2-1 Technique" -> R.drawable.grounding_icon_gradient
                "Color Scavenger Hunt" -> R.drawable.ic_color_scavenger
                "3x3 BINGO Card" -> R.drawable.ic_bingo_grid
                "Deep Breathing" -> R.drawable.deep_breathing
                "Shamanic Breathing" -> R.drawable.shamanic_breathing
                "Box Breathing" -> R.drawable.box_breathing
                "Criss-Cross Focus" -> R.drawable.criss_cross_focus
                "Figure 8 Flow" -> R.drawable.figure_8_flow
                "Clock Circle" -> R.drawable.clock_circle
                else -> R.drawable.grounding_icon_gradient
            }
            binding.ivExerciseIcon.setImageResource(iconRes)
        }
        exerciseDescription?.let { binding.tvExerciseDescription.text = it }

        populateBenefits(benefits)

        val bundle = Bundle().apply {
            putString("exerciseId", exerciseId)
        }

        binding.btnStart.setOnClickListener {

            when (exerciseName) {

                "5-4-3-2-1 Technique" -> {
            findNavController().navigate(
//                R.id.action_groundingTecniqueView_to_boxBreathingFragment,
//
                R.id.action_groundingTecniqueView_to_fivetoOneTechniqueProgress,
                bundle
            )
                }

                "Color Scavenger Hunt" -> {
            findNavController().navigate(
                R.id.action_groundingTecniqueView_to_scavengerHunt,
                bundle
            )
                }

                "3x3 BINGO Card" -> {
            findNavController().navigate(
                R.id.action_groundingTecniqueView_to_bingo,
                bundle
            )
                }

                "Deep Breathing" -> {
//            findNavController().navigate(
//                R.id.action_to_deepBreathingFragment,
//                bundle
//            )
                }

                "Shamanic Breathing" -> {
//            findNavController().navigate(
//                R.id.action_to_shamanicBreathingFragment,
//                bundle
//            )
                }

                "Box Breathing" -> {
                    findNavController().navigate(
                        R.id.action_groundingTecniqueView_to_boxBreathingFragment,
                        bundle
                    )
                }

                "Criss-Cross Focus" -> {
                    findNavController().navigate(
                        R.id.action_groundingTecniqueView_to_crissCrossFragment,
                        bundle
                    )
                }

                "Focus Shift" -> {
                    findNavController().navigate(
                        R.id.action_groundingTecniqueView_to_focusShiftView,
                        bundle
                    )
                }

                "Figure 8 Flow" -> {
                    findNavController().navigate(
                        R.id.action_groundingTecniqueView_to_figure8FlowFragment,
                        bundle
                    )
                }

                "Clock Circle" -> {
                    findNavController().navigate(
                        R.id.action_groundingTecniqueView_to_clockCircleFragment,
                        bundle
                    )
                }

                else -> {
                    findNavController().navigate(
                        R.id.action_groundingTecniqueView_to_fivetoOneTechniqueProgress,
                        bundle
                    )
                }
    }
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