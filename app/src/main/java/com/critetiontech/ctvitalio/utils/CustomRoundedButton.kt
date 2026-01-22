package com.critetiontech.ctvitalio.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import com.critetiontech.ctvitalio.R
import com.google.android.material.button.MaterialButton


class CustomRoundedButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.materialButtonStyle
) : MaterialButton(context, attrs, defStyleAttr) {

    private var startColor = 0
    private var endColor = 0
    private var radius = 40f

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CustomRoundedButton)
        radius = a.getDimension(R.styleable.CustomRoundedButton_customCornerRadius, 40f)
        startColor = a.getColor(R.styleable.CustomRoundedButton_gradientStartColor, 0xFF1E88E5.toInt())
        endColor = a.getColor(R.styleable.CustomRoundedButton_gradientEndColor, 0xFF26C6DA.toInt())
        a.recycle()

        applyGradient()
    }

    private fun applyGradient() {
        // Create gradient drawable
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(startColor, endColor)
        ).apply {
            cornerRadius = radius
        }

        // Ripple color
        val rippleColor = ColorStateList.valueOf(0x33FFFFFF)

        // Wrap inside RippleDrawable
        val rippleDrawable = RippleDrawable(rippleColor, gradient, null)

        // Apply safely
        super.setBackground(rippleDrawable)

        // Turn off Material tinting
        backgroundTintList = null
    }

    fun setGradientColors(start: Int, end: Int) {
        startColor = start
        endColor = end
        applyGradient()
    }
}