package com.critetiontech.ctvitalio.UI.fragments


import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.critetiontech.ctvitalio.R

class WaterRingView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var percentage = 60
    private val paintBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        style = Paint.Style.STROKE
        strokeWidth = 25f
    }
    private val paintProgress = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.primaryColor)
        style = Paint.Style.STROKE
        strokeWidth = 25f
        strokeCap = Paint.Cap.ROUND
    }

    fun setPercentage(p: Int) {
        percentage = p
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val rect = RectF(50f, 50f, width - 50f, height - 50f)
        canvas.drawArc(rect, 0f, 360f, false, paintBackground)
        canvas.drawArc(rect, -90f, 360f * (percentage / 100f), false, paintProgress)
    }
}