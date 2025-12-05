package com.critetiontech.ctvitalio.utils
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class RulerSeekBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val dp = resources.displayMetrics.density

    var minValue = 12
    var maxValue = 52
    var currentValue = 32

    private val rulerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B0C7E6")
        strokeWidth = 4f
    }

    private val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1E88E5")
        strokeWidth = 6f
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1E88E5")
        textSize = 12f * dp
        textAlign = Paint.Align.CENTER
    }

    private val topLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFAAAA")
        strokeWidth = 2f
    }

    private val bottomLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1E88E5")
        strokeWidth = 3f
    }

    private val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1E88E5")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val stepWidth = width.toFloat() / (54)
        val px = (currentValue - minValue) * stepWidth

        /* TOP DOTTED LINE */
        canvas.drawLine(
            0f,
            height * 0.15f,
            width.toFloat(),
            height * 0.15f,
            topLinePaint
        )

        /* BOTTOM LINE */
        canvas.drawLine(
            0f,
            height * 0.67f,
            width.toFloat(),
            height * 0.67f,
            bottomLinePaint
        )

        /* POINTER TRIANGLE */
        val pointerWidth = 6f * dp
        val pointerTop = height * 0.58f
        val pointerBottom = height * 0.77f

        val path = Path().apply {
            moveTo(px, pointerTop)
            lineTo(px - pointerWidth, pointerBottom)
            lineTo(px + pointerWidth, pointerBottom)
            close()
        }
          val majorTickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E88E5")
            strokeWidth = 8f   // thicker highlight
        }
        canvas.drawPath(path, pointerPaint)

        /* TICKS */
        /* TICKS */
        for (i in minValue..124) {

            val x = (i - minValue) * stepWidth

            val isSelected = i == currentValue
            val isMajor = (i - 9) % 10 == 0

            val paint = when {
                isSelected -> selectedPaint
                isMajor -> majorTickPaint
                else -> rulerPaint
            }

            val lineHeight = when {
                isSelected -> 67f * dp
                isMajor -> 32f * dp
                else -> 20f * dp
            }

            // draw tick
            canvas.drawLine(
                x,
                height * 0.20f,
                x,
                height * 0.20f + lineHeight,
                paint
            )

            if (isMajor) {
                // number under tick
                canvas.drawText(
                    i.toString(),
                    x,
                    height * 0.82f,
                    textPaint
                )

                // small line above number (upper line)
                val lineWidth = 10f * dp
                canvas.drawLine(
                    7f,
                    height * 0.78f,
                    7f  ,
                    height * 0.78f,
                    majorTickPaint
                )
            }
        }
        }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                val stepWidth = width.toFloat() / (maxValue - minValue)
                val index = (event.x / stepWidth).toInt() + minValue

                if (index in minValue..maxValue) {
                    currentValue = index
                    invalidate()
                }
            }
        }
        return true
    }
}