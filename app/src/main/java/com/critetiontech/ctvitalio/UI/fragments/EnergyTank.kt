package com.critetiontech.ctvitalio.UI.fragments

import PrefsManager
import android.animation.ValueAnimator
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentEnergyTankBinding
import com.critetiontech.ctvitalio.databinding.FragmentMoodBinding
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.viewmodel.EnergyTankViewModel
import com.critetiontech.ctvitalio.viewmodel.MoodViewModel

class EnergyTank : Fragment() {


    private lateinit var binding: FragmentEnergyTankBinding
    private var energyLevel =  0 // start at 50%
    private lateinit var gestureDetector: GestureDetector
    private lateinit var viewModel: EnergyTankViewModel
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEnergyTankBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[EnergyTankViewModel::class.java]

        binding.lightningIcon.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true // must return true to consume event
        }
        gestureDetector = GestureDetector(requireContext(), GestureListener())
        binding.lightningIcon.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
        binding.userName.text =   PrefsManager().getPatient()?.patientName ?: ""
        Glide.with(MyApplication.appContext)
            .load("http://182.156.200.177:5082/"+PrefsManager().getPatient()?.imageURL.toString())
            .placeholder(R.drawable.baseline_person_24)
            .circleCrop()
            .into(binding.userAvatar)
        binding.actionButton.setOnClickListener(){
            viewModel.insertEnergyTankMaster(
                context = requireContext(),
                energyPercentage = binding.percentageText.text.toString().replace("%", "")
            )
            findNavController().popBackStack()
        }

        binding.ivBack.setOnClickListener(){
            findNavController().popBackStack()
        }

        binding.tvSkip.setOnClickListener(){
            findNavController().popBackStack()
        }
        updateEnergyUI(energyLevel)
    }
    /**
     * Update UI based on energy level
     */
    private fun updateEnergyUI(level: Int) {
        animatePercentage(binding.percentageText.text.toString().replace("%", "").toIntOrNull() ?: 0, level)

        if(level==0){

            binding.dragToFill.visibility=View.VISIBLE
        }
        else{

            binding.dragToFill.visibility=View.GONE
        }

        val colorRes = when {
            level == 0 -> {
                binding.statusText.text = "UPDATE\nYOUR\nENERGY\nLEVEL!"
                android.R.color.background_light
            }
            level <= 25 -> {
                binding.statusText.text = "RUNNING\nLOW"
                android.R.color.holo_red_light
            }
            level in 26..60 -> {
                binding.statusText.text = "NEED\nA BOOST"
                android.R.color.holo_orange_light
            }
            level in 61..90 -> {
                binding.statusText.text = "PRETTY\nGOOD"
                android.R.color.holo_blue_light
            }
            else -> {
                binding.statusText.text = "FULLY\nCHARGED"
                android.R.color.holo_green_light
            }
        }

        val color = ContextCompat.getColor(requireContext(), colorRes)
        binding.statusText.setTextColor(color)

        // 🔥 fill lightning
        setLightningFillLevel(level, color)
    }

    private fun animatePercentage(start: Int, end: Int) {
        ValueAnimator.ofInt(start, end).apply {
            duration = 233
            addUpdateListener { animator ->
                binding.percentageText.text = "${animator.animatedValue}%"
            }
            start()
        }
    }

    private fun setLightningFillLevel(levelPercent: Int, tintColor: Int) {
        val drawable = binding.lightningIcon.drawable
        if (drawable is LayerDrawable) {
            val clip = drawable.findDrawableByLayerId(R.id.item_clip)
            clip.level = levelPercent.coerceIn(0, 100) * 100
            clip.setTint(tintColor)
        }
        binding.lightningIcon.invalidate()
    }
    inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true // very important to receive scroll events
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            // distanceY is how much finger moved **since last event**
            if (distanceY > 0) {
                // finger moved **up** → increase energy
                energyLevel = (energyLevel + 1).coerceAtMost(100)
            } else if (distanceY < 0) {
                // finger moved **down** → decrease energy
                energyLevel = (energyLevel - 1).coerceAtLeast(0)
            }
            updateEnergyUI(energyLevel)
            return true
        }
    }
}