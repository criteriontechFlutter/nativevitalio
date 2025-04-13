package com.criterion.nativevitalio.Utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class GlassFillView (
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    public var fillPercent = 0f // from 0 to 1
    private var glassSize = 150 // in ml
    private var onValueChange: ((Int, Int) -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#93C5FD") // water color
    }

    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.parseColor("#60A5FA")
    }

    fun setGlassSize(size: Int) {
        glassSize = 250
        fillPercent = 0f
        invalidate()
    }

    fun setOnFillChangedListener(callback: (percent: Int, ml: Int) -> Unit) {
        onValueChange = callback
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        // Define trapezoid shape (glass outline)
        val topWidth = w * 0.6f
        val bottomWidth = w * 0.9f
        val topLeftX = (w - topWidth) / 2
        val bottomLeftX = (w - bottomWidth) / 2

        val glassPath = Path().apply {
            moveTo(topLeftX, 0f)
            lineTo(w - topLeftX, 0f)
            lineTo(w - bottomLeftX, h)
            lineTo(bottomLeftX, h)
            close()
        }

        // Clip inside glass
        canvas.save()
        canvas.clipPath(glassPath)
        val fillTop = h * (1f - fillPercent)
        canvas.drawRect(0f, fillTop, w, h, paint)
        canvas.restore()
        // Draw the glass outline
        canvas.drawPath(glassPath, outlinePaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            val y = event.y.coerceIn(0f, height.toFloat())
            fillPercent = 1f - (y / height)
            invalidate()

            val percentInt = (fillPercent * 100).toInt()
            val ml = ((percentInt / 100f) * glassSize).toInt()

            onValueChange?.invoke(percentInt, ml)
            return true
        }
        return super.onTouchEvent(event)
    }
}
