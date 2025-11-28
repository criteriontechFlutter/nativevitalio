package com.critetiontech.ctvitalio.common

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.content.Context
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.bumptech.glide.Glide
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.BottomsheetMessageBinding
import com.critetiontech.ctvitalio.model.HapticType
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class GlobalMessageBottomSheet(
    private val iconRes: Int,
    private val title: String,
    private val message: String,
    private val buttonText: String = "OK",
    private val haptic: HapticType = HapticType.MEDIUM,
    private val onButtonClick: (() -> Unit)? = null
) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomsheetMessageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomsheetMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            setOnShowListener { dialog ->
                val bottomSheet = (dialog as BottomSheetDialog)
                    .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

                bottomSheet?.let {
                    val behavior = BottomSheetBehavior.from(it)
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED

                    it.startAnimation(
                        AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
                    )
                }
            }
        }
    }
    private fun vibrate(type: HapticType) {
        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = when (type) {
                HapticType.LIGHT -> VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)
                HapticType.MEDIUM -> VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE)
                HapticType.STRONG -> VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE)
            }
            vibrator.vibrate(effect)
        } else {
            vibrator.vibrate(40)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(this).asGif().load(R.raw.ic_success_anim).into(binding.lottieAnim)
        // Trigger haptic vibration
        vibrate(haptic)

        binding.tvTitle.text = title
        binding.tvMessage.text = message
        binding.btnOk.text = buttonText

        binding.btnOk.setOnClickListener {
            onButtonClick?.invoke()
            dismiss()
        }
        binding.ivClose.setOnClickListener { dismiss() }
    }


    override fun getTheme(): Int = com.google.android.material.R.style.Theme_Material3_Light_BottomSheetDialog
}