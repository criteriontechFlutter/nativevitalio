package com.critetiontech.ctvitalio.utils



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

class MilkView (
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var fillPercent = 0f
    private var glassSize = 150
    private var onValueChange: ((Int, Int) -> Unit)? = null

    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.parseColor("#EEEEEE")
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
        val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        val shader = LinearGradient(
            0f, height * (1f - fillPercent), 0f, height.toFloat(),
            Color.parseColor("#BFDBFE"),
            Color.parseColor("#60A5FA"),
            Shader.TileMode.CLAMP
        )
        gradientPaint.shader = shader
        return gradientPaint
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        val glassRect = RectF(0f, 0f, w, h)

        // Draw glass outline
        canvas.drawRoundRect(glassRect, 30f, 30f, outlinePaint)

        // Clip to glass shape and fill gradient only if fillPercent > 0
        if (fillPercent > 0f) {
            canvas.save()
            val fillTop = h * (1f - fillPercent)
            canvas.clipRect(0f, fillTop, w, h)
            canvas.drawRect(0f, fillTop, w, h, getGradientPaint())
            canvas.restore()
        }

        // Draw percentage and ml text inside the glass
        val centerX = w / 2f
        val centerY = h / 2f

        val percentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#334155")
            textSize = 60f
            textAlign = Paint.Align.CENTER
        }

        val mlPaint = Paint(percentPaint).apply {
            textSize = 36f
        }

        val percentageText = "${(fillPercent * 100).toInt()}%"
        val mlText = "${getCurrentMl()} ml of $glassSize ml"

        canvas.drawText(percentageText, centerX, centerY - 20, percentPaint)
        canvas.drawText(mlText, centerX, centerY + 30, mlPaint)
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
        val animator = ValueAnimator.ofFloat(fillPercent, targetPercent).apply {
            duration = 10
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                fillPercent = animation.animatedValue as Float
                val percentInt = (fillPercent * 100).toInt()
                val ml = ((percentInt / 100f) * glassSize).toInt()
                onValueChange?.invoke(percentInt, ml)
                invalidate()
            }
        }
        animator.start()
    }
}
