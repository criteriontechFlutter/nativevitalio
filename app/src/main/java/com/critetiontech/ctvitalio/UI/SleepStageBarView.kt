package com.critetiontech.ctvitalio.UI
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class SleepStageBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var stageName: String = ""
    private var duration: String = ""
    private var percentage: Int = 0
    private var filledBars: Int = 0
    private var totalBars: Int = 30
    private var barColor: Int = 0xFF1976D2.toInt()

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF757575.toInt()
        textSize = 36f
    }

    private val durationPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF757575.toInt()
        textSize = 36f
        textAlign = Paint.Align.RIGHT
    }

    private val filledBarPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val emptyBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE0E0E0.toInt()
    }

    fun setData(name: String, durationText: String, percent: Int, color: Int) {
        stageName = name
        duration = durationText
         barColor = color
        filledBars = (totalBars * percent / 100f).toInt().coerceAtMost(totalBars)
        filledBarPaint.color = color
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        // Draw stage name
        canvas.drawText(stageName, 0f, 30f, labelPaint)

        // Draw duration and percentage
        val durationText = "$duration "
        canvas.drawText(durationText, width, 30f, durationPaint)

        // Draw bars
        val barTop = 45f
        val barHeight = 108f
        val barWidth = 18f
        val barSpacing = 6f
        val cornerRadius = 3f

        for (i in 0 until totalBars) {
            val x = i * (barWidth + barSpacing)
            val paint = if (i < filledBars) filledBarPaint else emptyBarPaint

            val rect = RectF(x, barTop, x + barWidth, barTop + barHeight)
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 700
        val desiredHeight = 80

        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)

        setMeasuredDimension(width, height)
    }
}