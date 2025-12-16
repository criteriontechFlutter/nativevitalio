package com.critetiontech.ctvitalio.utils



import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.graphics.toColorInt
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


class CircularProgressView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    // ---------------------------------------------------------
    // Public progress value (0–100)
    // ---------------------------------------------------------
    var progress = 0f
        set(value) {
            field = value.coerceIn(0f, 100f)
            invalidate()
        }

    // ---------------------------------------------------------
    // Stroke sizes (adjust here)
    // ---------------------------------------------------------
    private val trackStroke = 12f            // thin grey ring
    private val progressStroke = 12f         // thin green ring
    private val capMultiplier = 0.55f        // knob size multiplier

    // ---------------------------------------------------------
    // Colors
    // ---------------------------------------------------------
    private val trackColor = "#E5E7EB".toColorInt()
    private val progressColor = "#1EC35A".toColorInt()

    // ---------------------------------------------------------
    // Paint objects
    // ---------------------------------------------------------
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = trackColor
        style = Paint.Style.STROKE
        strokeWidth = trackStroke
        strokeCap = Paint.Cap.ROUND
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = progressColor
        style = Paint.Style.STROKE
        strokeWidth = progressStroke
        strokeCap = Paint.Cap.ROUND
    }

    private val capPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = progressColor
        style = Paint.Style.FILL
    }

    // Arc rectangle bounds
    private val rect = RectF()

    // Start angle for bottom-left
    private val startAngle = -225f

    // Cached center & radius
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f


    // ---------------------------------------------------------
    // Animate progress smoothly
    // ---------------------------------------------------------
    fun animateProgress(toValue: Float, duration: Long = 1000L) {
        val animator = ValueAnimator.ofFloat(progress, toValue.coerceIn(0f, 100f))
        animator.duration = duration
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener {
            progress = it.animatedValue as Float
        }
        animator.start()
    }


    // ---------------------------------------------------------
    // Ensures perfect circle layout
    // ---------------------------------------------------------
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = min(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec)
        )
        setMeasuredDimension(size, size)
    }


    // ---------------------------------------------------------
    // Compute bounds on layout
    // ---------------------------------------------------------
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        centerX = w / 2f
        centerY = h / 2f

        radius = (min(w, h) / 2f) - progressStroke

        rect.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )
    }


    // ---------------------------------------------------------
    // Draw everything
    // ---------------------------------------------------------
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1️⃣ Draw thin grey track
        canvas.drawOval(rect, trackPaint)

        // 2️⃣ Draw green progress arc
        val sweep = (progress / 100f) * 360f
        if (sweep > 0) {
            canvas.drawArc(rect, startAngle, sweep, false, progressPaint)
        }

        // 3️⃣ Draw green start cap
        drawStartCap(canvas)

        // 4️⃣ Optional: Draw end cap (disabled)
        // drawEndCap(canvas, sweep)
    }


    // ---------------------------------------------------------
    // Draw thick knob at start
    // ---------------------------------------------------------
    private fun drawStartCap(canvas: Canvas) {
        val capRadius = progressStroke * capMultiplier

        val rad = Math.toRadians(startAngle.toDouble())
        val cx = (centerX + radius * cos(rad)).toFloat()
        val cy = (centerY + radius * sin(rad)).toFloat()

        canvas.drawCircle(cx, cy, capRadius, capPaint)
    }


    // ---------------------------------------------------------
    // OPTIONAL: End Cap
    // ---------------------------------------------------------
    private fun drawEndCap(canvas: Canvas, sweep: Float) {
        if (sweep <= 0f) return

        val capRadius = progressStroke * capMultiplier

        val endAngle = startAngle + sweep
        val rad = Math.toRadians(endAngle.toDouble())

        val ex = (centerX + radius * cos(rad)).toFloat()
        val ey = (centerY + radius * sin(rad)).toFloat()

        canvas.drawCircle(ex, ey, capRadius, capPaint)
    }
}



