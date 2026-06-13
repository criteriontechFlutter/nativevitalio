package com.critetiontech.ctvitalio.utils

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible
import com.critetiontech.ctvitalio.databinding.FragmentCorporateDashBoardBinding

object DashboardAnimationHelper {

    fun animatePageLoad(binding: FragmentCorporateDashBoardBinding) {
        val offset = 60f
        val dur = 700L
        val interp = DecelerateInterpolator(2.0f)

        listOf(binding.tFeeling, binding.tFeelingBelow, binding.ivIllustration)
            .forEach { it.alpha = 0f; it.translationY = offset }

        fun animItem(view: View, delay: Long) {
            view.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(dur)
                .setStartDelay(delay)
                .setInterpolator(interp)
                .start()
        }

        animItem(binding.tFeeling, 100L)
        animItem(binding.tFeelingBelow, 220L)
        animItem(binding.ivIllustration, 360L)
    }

    fun showPopup(binding: FragmentCorporateDashBoardBinding) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            binding.contentScroll.setRenderEffect(
                RenderEffect.createBlurEffect(25f, 25f, Shader.TileMode.CLAMP)
            )
        }
        binding.blurOverlay.isVisible = true
        binding.popupContainer.isVisible = true

        binding.blurOverlay.alpha = 0f
        binding.blurOverlay.animate().alpha(1f).setDuration(300).start()

        binding.popupContainer.alpha = 0f
        binding.popupContainer.scaleX = 0.8f
        binding.popupContainer.scaleY = 0.8f
        binding.popupContainer.animate()
            .alpha(1f).scaleX(1f).scaleY(1f)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    fun hidePopup(binding: FragmentCorporateDashBoardBinding) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            binding.contentScroll.setRenderEffect(null)
        }
        binding.popupContainer.animate()
            .alpha(0f).scaleX(0.8f).scaleY(0.8f)
            .setDuration(250)
            .start()

        binding.blurOverlay.animate()
            .alpha(0f)
            .setDuration(250)
            .withEndAction { binding.blurOverlay.isVisible = false }
            .start()
    }
}