package com.critetiontech.ctvitalio.utils

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import com.critetiontech.ctvitalio.R
import kotlin.math.min

class CircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Paint objects for drawing
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Progress properties
    private var progress = 0f
    private var maxProgress = 100f
    private var currentProgress = 0f

    // Visual properties
    private var strokeWidth = 24f
    private var progressColor = Color.parseColor("#10B981") // Green color
    private var backgroundColor = Color.parseColor("#E5E7EB") // Light gray
    private var textColor = Color.parseColor("#1F2937") // Dark gray
    private var textSize = 48f

    // Drawing bounds
    private val bounds = RectF()

    // Animation
    private var animator: ValueAnimator? = null
    private val animationDuration = 1000L // 1 second

    init {
        setupPaints()
        setupCustomAttributes(attrs)
    }

    private fun setupPaints() {
        // Background circle paint
        backgroundPaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = this@CircularProgressView.strokeWidth
            color = backgroundColor
            strokeCap = Paint.Cap.ROUND
        }

        // Progress arc paint
        progressPaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = this@CircularProgressView.strokeWidth
            color = progressColor
            strokeCap = Paint.Cap.ROUND
        }

        // Text paint
        textPaint.apply {
            textAlign = Paint.Align.CENTER
            textSize = this@CircularProgressView.textSize
            color = textColor
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    private fun setupCustomAttributes(attrs: AttributeSet?) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.CircularProgressView, 0, 0)

            progress = typedArray.getFloat(R.styleable.CircularProgressView_progress, 0f)
            maxProgress = typedArray.getFloat(R.styleable.CircularProgressView_maxProgress, 100f)
            strokeWidth = typedArray.getDimension(R.styleable.CircularProgressView_strokeWidth, 24f)
            progressColor = typedArray.getColor(R.styleable.CircularProgressView_progressColor, progressColor)
            backgroundColor = typedArray.getColor(R.styleable.CircularProgressView_backgroundColor, backgroundColor)
            textColor = typedArray.getColor(R.styleable.CircularProgressView_textColor, textColor)
            textSize = typedArray.getDimension(R.styleable.CircularProgressView_textSize, 48f)

            typedArray.recycle()
            setupPaints()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val size = min(w, h)
        val padding = (strokeWidth / 2).toInt()

        bounds.set(
            padding.toFloat(),
            padding.toFloat(),
            (size - padding).toFloat(),
            (size - padding).toFloat()
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw background circle
        canvas.drawCircle(
            bounds.centerX(),
            bounds.centerY(),
            bounds.width() / 2 - strokeWidth / 2,
            backgroundPaint
        )

        // Draw progress arc
        val sweepAngle = (currentProgress / maxProgress) * 360f
        canvas.drawArc(bounds, -90f, sweepAngle, false, progressPaint)

        // Draw progress text
        val progressText = "${currentProgress.toInt()}%"
        val textBounds = Rect()
        textPaint.getTextBounds(progressText, 0, progressText.length, textBounds)

        canvas.drawText(
            progressText,
            bounds.centerX(),
            bounds.centerY() + textBounds.height() / 2,
            textPaint
        )
    }

    // Public methods to control the progress
    fun setProgress(progress: Float, animate: Boolean = true) {
        val clampedProgress = progress.coerceIn(0f, maxProgress)

        if (animate) {
            animateProgress(this.progress, clampedProgress)
        } else {
            currentProgress = clampedProgress
            invalidate()
        }

        this.progress = clampedProgress
    }

    fun getProgress(): Float = progress

    fun setMaxProgress(maxProgress: Float) {
        this.maxProgress = maxProgress
        invalidate()
    }

    fun setProgressColor(color: Int) {
        progressColor = color
        progressPaint.color = color
        invalidate()
    }

    override fun setBackgroundColor(color: Int) {
        backgroundColor = color
        backgroundPaint.color = color
        invalidate()
    }

    fun setStrokeWidth(width: Float) {
        strokeWidth = width
        backgroundPaint.strokeWidth = width
        progressPaint.strokeWidth = width
        invalidate()
    }

    private fun animateProgress(from: Float, to: Float) {
        animator?.cancel()

        animator = ValueAnimator.ofFloat(from, to).apply {
            duration = animationDuration
            addUpdateListener { animation ->
                currentProgress = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }
}