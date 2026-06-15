package com.critetiontech.ctvitalio.utils


import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import com.critetiontech.ctvitalio.R
import kotlin.math.min

class ArcGaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private val filledPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
        color = Color.parseColor("#E9893D")       // Orange
        strokeWidth = 65f
    }

    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
        color = Color.parseColor("#D1D3D4")       // Light gray
        strokeWidth = 65f
    }

    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeWidth = 65f
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color =  ContextCompat.getColor(context, R.color.themeTextColorBW)
        textSize = 110f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3A3F43")
        textSize = 42f
        textAlign = Paint.Align.CENTER
    }

    private var progress = 65f
    private var animatedSweep = 0f
    private var labelText = "Moderate"
    private val arcRect = RectF()
    private var sweepAnimator: ValueAnimator? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val cx = w / 2f
        val cy = h / 1.4f
        val r = min(w, h) / 2.0f
        arcRect.set(cx - r, cy - r, cx + r, cy + r)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 1.4f
        val rect = arcRect

        // FULL arc background (gray)
        canvas.drawArc(rect, 180f, 180f, false, emptyPaint)

        // Filled arc (orange)
        val sweepAngle = (animatedSweep / 100f) * 180f
        canvas.drawArc(rect, 180f, sweepAngle, false, filledPaint)

        // Divider gap
        canvas.drawArc(rect, 180f + sweepAngle - 2f, 4f, false, dividerPaint)

        // Center number
        canvas.drawText(
            progress.toInt().toString(),
            centerX,
            centerY - 50f,
            textPaint
        )

        // Label
        canvas.drawText(
            labelText,
            centerX,
            centerY + 40f,
            labelPaint
        )
    }

    /* ===================== PUBLIC API ===================== */

    fun setProgressAnimated(value: Float, label: String = "") {
        progress = value.coerceIn(0f, 100f)
        if (label.isNotEmpty()) labelText = label

        sweepAnimator?.cancel()
        sweepAnimator = ValueAnimator.ofFloat(animatedSweep, progress).apply {
            duration = 1200
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                animatedSweep = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        sweepAnimator?.cancel()
    }

    fun setProgress(value: Float, label: String = "") {
        progress = value.coerceIn(0f, 100f)
        if (label.isNotEmpty()) labelText = label
        animatedSweep = progress
        invalidate()
    }
}
