package com.critetiontech.ctvitalio.UI.fragments

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.widget.NestedScrollView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentCorporateDashBoardBinding
import com.critetiontech.ctvitalio.databinding.FragmentDashboardBinding
import com.google.android.material.appbar.AppBarLayout


class CorporateDashBoard : Fragment() {
    private lateinit var binding: FragmentCorporateDashBoardBinding


    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCorporateDashBoardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val text = "How are you feeling now?"
        val spannable = SpannableString(text)

        // Change only the word "feeling" to orange
        val start = text.indexOf("feeling")
        val end = start + "feeling".length
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#FFA500")), // Orange color
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

       binding.tFeeling.text = spannable

        val typeface = ResourcesCompat.getFont(requireActivity(), R.font.source_serif_pro)
        binding.tFeeling.setTypeface(typeface, Typeface.BOLD)
        binding.tFeelingBelow.setTypeface(typeface )


        // Animate to 80%
        binding.WellnessProgres.setProgress(80f, animate = true)

// Change color dynamically
        binding.WellnessProgres.setProgressColor(Color.GREEN)

// Update multiple metrics
        //updateWellnessData(78f, 92f, 85f, 82f)

    }
}