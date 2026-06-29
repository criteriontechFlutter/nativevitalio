package com.critetiontech.ctvitalio.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class CrissCrossLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(3f)
        color = Color.parseColor("#4DFFFFFF") // Semi-transparent white
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FF5A79") // Pink/Red target
    }

    private val glowOuterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#33FF5A79")
    }

    private val glowInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#80FF5A79")
    }

    var startPointIndex: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var endPointIndex: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    private fun getDotCenter(index: Int, w: Float, h: Float): Pair<Float, Float> {
        val margin = dpToPx(18f)
        return when (index) {
            0 -> Pair(margin, margin) // Top Left
            1 -> Pair(w - margin, margin) // Top Right
            2 -> Pair(margin, h - margin) // Bottom Left
            3 -> Pair(w - margin, h - margin) // Bottom Right
            else -> Pair(margin, margin)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val startCenter = getDotCenter(startPointIndex, w, h)
        val endCenter = getDotCenter(endPointIndex, w, h)

        // Calculate moving pink dot coordinates
        val dotX = startCenter.first + (endCenter.first - startCenter.first) * progress
        val dotY = startCenter.second + (endCenter.second - startCenter.second) * progress

        // Draw connecting line from start point up to the current position of the moving dot
        if (startPointIndex != endPointIndex) {
            canvas.drawLine(
                startCenter.first, startCenter.second,
                dotX, dotY,
                linePaint
            )
        }

        // Draw glowing outer/inner circles
        canvas.drawCircle(dotX, dotY, dpToPx(16f), glowOuterPaint)
        canvas.drawCircle(dotX, dotY, dpToPx(12f), glowInnerPaint)
        // Draw main dot
        canvas.drawCircle(dotX, dotY, dpToPx(8f), dotPaint)
    }
}
