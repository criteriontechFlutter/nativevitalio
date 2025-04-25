package com.critetiontech.ctvitalio.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class OvalFillMeterView @JvmOverloads constructor (context: Context ,attrs: AttributeSet? = null) : View(context,attrs) {
    private var progress = 0f

    fun setProgress(value: Float) {
        progress = value.coerceIn(0f, 1f)
        invalidate()
    }

    fun getProgressValueInDouble(): Double {
        return (progress * 1000).toDouble()
    }

    private val borderPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#BABFC9")
        strokeWidth = 15f
        isAntiAlias = true
    }

    private val linePaint = Paint().apply {
        color = Color.parseColor("#BABFC9")
        strokeWidth = 2f
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    private val fillPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FFF8E1")
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val ovalRect = RectF(0f, 0f, width.toFloat(), height.toFloat())

        canvas.save()
        canvas.clipPath(Path().apply { addOval(ovalRect, Path.Direction.CW) })
        val fillTop = height * (1 - progress)
        canvas.drawRect(0f, fillTop, width.toFloat(), height.toFloat(), fillPaint)
        canvas.restore()

        val divisions = 10
        for (i in 1 until divisions) {
            val y = i * height / divisions.toFloat()
            canvas.drawLine(0f, y, width.toFloat(), y, linePaint)
        }

        canvas.drawOval(ovalRect, borderPaint)
    }
}






