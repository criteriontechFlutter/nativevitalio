package com.criterion.nativevitalio.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class AudioWaveView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var waveHeight = 0
    private val paint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.FILL
    }

    fun setWaveHeight(height: Int) {
        waveHeight = min(height, height.coerceAtMost(3000))
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val barHeight = waveHeight / 10f
        val centerY = height / 2f
        canvas.drawRect(0f, centerY - barHeight, width.toFloat(), centerY + barHeight, paint)
    }
}
