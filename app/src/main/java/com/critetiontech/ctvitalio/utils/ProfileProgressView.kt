package com.critetiontech.ctvitalio.utils

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class ProfileProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var progress = 80f
    private var strokeWidth = 12f
    private val startAngle = -90f

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E5E7EB")  // light grey background ring
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = this@ProfileProgressView.strokeWidth
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#28C76F") // green progress arc
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = this@ProfileProgressView.strokeWidth
    }

    fun setProgress(value: Float, animate: Boolean = false) {
        val newValue = value.coerceIn(0f, 100f)
        if (!animate) {
            progress = newValue
            invalidate()
            return
        }

        ValueAnimator.ofFloat(progress, newValue).apply {
            duration = 900
            addUpdateListener {
                progress = it.animatedValue as Float
                invalidate()
            }
        }.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val radius = (width.coerceAtMost(height) - strokeWidth) / 2
        val cx = width / 2f
        val cy = height / 2f

        val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

        canvas.drawArc(rect, 0f, 360f, false, bgPaint)

        val sweep = 360 * (progress / 100)
        canvas.drawArc(rect, startAngle, sweep, false, progressPaint)
    }
}
