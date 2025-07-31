package com.critetiontech.ctvitalio.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import com.critetiontech.ctvitalio.R
import com.google.android.material.button.MaterialButton


class CustomRoundedButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.materialButtonStyle
) : MaterialButton(context, attrs, defStyleAttr) {

    init {
        // Default appearance
        cornerRadius = 20
        strokeWidth = 2 // fallback default

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CustomRoundedButton,
            0, 0
        ).apply {
            try {
                val strokeColor = getColor(R.styleable.CustomRoundedButton_customStrokeColor, Color.BLACK)
                setStrokeColor(ColorStateList.valueOf(strokeColor))

                val strokeWidthPx = getDimensionPixelSize(R.styleable.CustomRoundedButton_customStrokeWidth, 2)
                strokeWidth = strokeWidthPx
            } finally {
                recycle()
            }
        }
    }
}