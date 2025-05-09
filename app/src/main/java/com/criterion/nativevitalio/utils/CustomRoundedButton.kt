package com.criterion.nativevitalio.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import com.criterion.nativevitalio.R
import com.google.android.material.button.MaterialButton


class CustomRoundedButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.materialButtonStyle
) : MaterialButton(context, attrs, defStyleAttr) {

    init {
        // Default appearance
        cornerRadius = 20
        strokeWidth = 2
        // Apply custom attributes if any
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CustomRoundedButton,
            0, 0
        ).apply {
            try {
                val strokeColor = getColor(R.styleable.CustomRoundedButton_customStrokeColor, Color.BLACK)
                setStrokeColor(ColorStateList.valueOf(strokeColor))
            } finally {
                recycle()
            }
        }
    }
}
