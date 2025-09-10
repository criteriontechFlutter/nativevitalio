package com.critetiontech.ctvitalio.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.critetiontech.ctvitalio.R
import com.google.android.material.button.MaterialButton
import androidx.core.graphics.toColorInt

class CustomRoundedButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.materialButtonStyle
) : MaterialButton(context, attrs, defStyleAttr) {

    init {
        // Default appearance
        cornerRadius = 20
        strokeWidth = 0 // Remove stroke for gradient button

        // Set default gradient background
        setGradientBackground()

        // Set text color to white for better contrast
        setTextColor(Color.WHITE)

        // Handle custom attributes
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CustomRoundedButton,
            0, 0
        ).apply {
            try {
                val hasGradient = getBoolean(R.styleable.CustomRoundedButton_useGradient, true)

                if (hasGradient) {
                    val startColor = getColor(R.styleable.CustomRoundedButton_gradientStartColor,
                        Color.parseColor("#1E88E5")) // Blue
                    val endColor = getColor(R.styleable.CustomRoundedButton_gradientEndColor,
                        Color.parseColor("#26C6DA")) // Teal

                    setGradientBackground(startColor, endColor)
                } else {
                    // Fallback to stroke style
                    val strokeColor = getColor(R.styleable.CustomRoundedButton_customStrokeColor, Color.BLACK)
                    setStrokeColor(ColorStateList.valueOf(strokeColor))

                    val strokeWidthPx = getDimensionPixelSize(R.styleable.CustomRoundedButton_customStrokeWidth, 2)
                    strokeWidth = strokeWidthPx
                }

                val customCornerRadius = getDimensionPixelSize(R.styleable.CustomRoundedButton_customCornerRadius, 20)
                cornerRadius = customCornerRadius

            } finally {
                recycle()
            }
        }
    }

    private fun setGradientBackground(
        startColor: Int = "#1E88E5".toColorInt(), // Blue
        endColor: Int = "#26C6DA".toColorInt()     // Teal
    ) {
        val gradientDrawable = GradientDrawable().apply {
            orientation = GradientDrawable.Orientation.LEFT_RIGHT
            colors = intArrayOf(startColor, endColor)
            cornerRadius = this@CustomRoundedButton.cornerRadius.toFloat()
            shape = GradientDrawable.RECTANGLE
        }

        background = gradientDrawable

        // Remove the default background tint to show gradient
        backgroundTintList = null
    }

    // Method to programmatically set gradient colors
    fun setGradientColors(startColor: Int, endColor: Int) {
        setGradientBackground(startColor, endColor)
    }
}