package com.critetiontech.ctvitalio.UI

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class BoxBreathingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val squarePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3F6B8F")
        style = Paint.Style.STROKE
    }

    private val dotInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00E5FF")
        style = Paint.Style.FILL
    }

    private val dotOuterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4D00E5FF")
        style = Paint.Style.FILL
    }

    private var progress = 0.0f
    private var squareMargin = 120f
    private var strokeWidth = 8f
    private var dotInnerRadius = 14f
    private var dotOuterRadius = 32f

    init {
        val density = context.resources.displayMetrics.density
        squareMargin = 40f * density
        strokeWidth = 5f * density
        dotInnerRadius = 8f * density
        dotOuterRadius = 18f * density

        squarePaint.strokeWidth = strokeWidth
    }

    fun setProgress(progress: Float) {
        this.progress = progress % 1.0f
        if (this.progress < 0) {
            this.progress += 1.0f
        }
        invalidate()
    }

    fun getProgress(): Float = progress

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width
        val height = height

        val size = Math.min(width, height) - (2 * squareMargin)
        if (size <= 0) return

        val left = (width - size) / 2f
        val top = (height - size) / 2f
        val right = left + size
        val bottom = top + size

        // Draw the square outline
        canvas.drawRect(left, top, right, bottom, squarePaint)

        // Calculate position of the glowing dot
        var dotX = left
        var dotY = top

        if (progress < 0.25f) {
            val fraction = progress / 0.25f
            dotX = left + fraction * size
            dotY = top
        } else if (progress < 0.50f) {
            val fraction = (progress - 0.25f) / 0.25f
            dotX = right
            dotY = top + fraction * size
        } else if (progress < 0.75f) {
            val fraction = (progress - 0.50f) / 0.25f
            dotX = right - fraction * size
            dotY = bottom
        } else {
            val fraction = (progress - 0.75f) / 0.25f
            dotX = left
            dotY = bottom - fraction * size
        }

        // Draw glowing dot
        canvas.drawCircle(dotX, dotY, dotOuterRadius, dotOuterPaint)
        canvas.drawCircle(dotX, dotY, dotInnerRadius, dotInnerPaint)
    }
}
