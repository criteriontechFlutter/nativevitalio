package com.criterion.nativevitalio.utils

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator


class GlassFillView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var fillPercent = 0f
    private var glassSize = 150
    private var onValueChange: ((Int, Int) -> Unit)? = null

    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.parseColor("#D1D5DB")
    }

    fun setGlassSize(size: Int) {
        glassSize = size
        fillPercent = 0f
        invalidate()
    }

    fun setOnFillChangedListener(callback: (percent: Int, ml: Int) -> Unit) {
        onValueChange = callback
    }

    fun getCurrentMl(): Int = (fillPercent * glassSize).toInt()

    private fun getGradientPaint(): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            shader = LinearGradient(
                0f, height * (1f - fillPercent), 0f, height.toFloat(),
                Color.parseColor("#E0F2FE"),
                Color.parseColor("#BFDBFE"),
                Shader.TileMode.CLAMP
            )
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val padding = 40f
        val cornerRadius = 40f

        val left = padding
        val right = w - padding
        val glassRect = RectF(left, 0f, right, h)

        // Draw outline
        canvas.drawRoundRect(glassRect, cornerRadius, cornerRadius, outlinePaint)

        // Fill level
        val fillTop = h * (1f - fillPercent)
        val fillRect = RectF(left, fillTop, right, h)
        canvas.drawRect(fillRect, getGradientPaint())

        // Center text
        val centerX = (left + right) / 2f
        val centerY = h / 2f

        val percentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1F2937")
            textSize = 70f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        val mlPaint = Paint(percentPaint).apply {
            textSize = 38f
            isFakeBoldText = false
        }

        canvas.drawText("${(fillPercent * 100).toInt()}%", centerX, centerY - 20f, percentPaint)
        canvas.drawText("${getCurrentMl()} ml of $glassSize ml", centerX, centerY + 40f, mlPaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            val y = event.y.coerceIn(0f, height.toFloat())
            val newPercent = 1f - (y / height)
            animateFillTo(newPercent)
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun animateFillTo(targetPercent: Float) {
        ValueAnimator.ofFloat(fillPercent, targetPercent).apply {
            duration = 50
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                fillPercent = it.animatedValue as Float
                val percentInt = (fillPercent * 100).toInt()
                val ml = ((percentInt / 100f) * glassSize).toInt()
                onValueChange?.invoke(percentInt, ml)
                invalidate()
            }
        }.start()
    }
}