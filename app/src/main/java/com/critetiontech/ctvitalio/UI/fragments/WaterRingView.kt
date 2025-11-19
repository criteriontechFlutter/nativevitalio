package com.critetiontech.ctvitalio.UI.fragments

import android.R.attr.centerY
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.toColorInt
import kotlin.math.sin

class WaterRingView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var volumeChangeListener: OnVolumeChangeListener? = null
    private var progress = 0f
    private var waveOffset = 0f

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = "#E2E8F0".toColorInt()
    }

    private val waterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val innerCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private var fillColor = "#3B82F6".toColorInt()
    private var minMl = 0
    private var maxMl = 500

    private val wavePath = Path()
    private val clipPath = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = minOf(width, height) / 2f - 20f

        // Draw background circle
        canvas.drawCircle(centerX, centerY, radius, circlePaint)

        // Create circular clip path
        clipPath.reset()
        clipPath.addCircle(centerX, centerY, radius, Path.Direction.CW)

        // Save canvas state and apply clip
        canvas.save()
        canvas.clipPath(clipPath)

        // Calculate water level
        val waterHeight = radius * 2 * progress
        val waterY = centerY + radius - waterHeight

        // Draw water with wave effect
        waterPaint.color = fillColor
        drawWater(canvas, centerX, waterY, radius)

        canvas.restore()

        // Draw inner circle that doesn't get filled
        val innerRadius = radius * 0.3f // 30% of outer radius
        canvas.drawCircle(centerX, centerY, innerRadius, innerCirclePaint)

        // Draw centered percentage text
        val filledMl = getFilledMl()
        val percentText = "${(progress * 100).toInt()}%"
        val mlText = "$filledMl ml / $maxMl ml"

        val percentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#334155")
            textSize = 72f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        val mlPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 28f
            textAlign = Paint.Align.CENTER
        }

        // Draw percentage
        val percentBounds = Rect()
        percentPaint.getTextBounds(percentText, 0, percentText.length, percentBounds)
        canvas.drawText(percentText, centerX, centerY + percentBounds.height() / 2f, percentPaint)

        // Draw ml text below
        canvas.drawText(mlText, centerX, centerY + percentBounds.height() + 40f, mlPaint)

        // Animate wave
        waveOffset += 0.05f
        if (waveOffset > 2 * Math.PI) waveOffset = 0f
        postInvalidateOnAnimation()
    }

    private fun drawWater(canvas: Canvas, centerX: Float, waterY: Float, radius: Float) {
        wavePath.reset()

        val waveAmplitude = 15f
        val waveLength = radius * 0.8f
        val startX = centerX - radius
        val endX = centerX + radius

        wavePath.moveTo(startX, waterY)

        // Create wave effect
        var x = startX
        while (x <= endX) {
            val relativeX = x - startX
            val angle = (relativeX / waveLength * 2 * Math.PI + waveOffset).toFloat()
            val y = waterY + sin(angle.toDouble()).toFloat() * waveAmplitude
            wavePath.lineTo(x, y)
            x += 5f
        }

        // Complete the path to fill the bottom
        wavePath.lineTo(endX, centerY + radius)
        wavePath.lineTo(startX, centerY + radius)
        wavePath.close()

        canvas.drawPath(wavePath, waterPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            val centerY = height / 2f
            val radius = minOf(width, height) / 2f - 20f

            // Calculate progress based on vertical touch position
            val touchY = event.y
            val topY = centerY - radius
            val bottomY = centerY + radius

            progress = ((bottomY - touchY) / (radius * 2)).coerceIn(0f, 1f)

            volumeChangeListener?.onVolumeChanged(getFilledMl(), progress)
            invalidate()
        }
        return true
    }

    fun setVolumeRange(min: Int, max: Int) {
        minMl = min
        maxMl = max
        invalidate()
    }

    fun setFillColor(color: Int) {
        fillColor = color
        invalidate()
    }

    fun setProgress(value: Float) {
        progress = value.coerceIn(0f, 1f)
        volumeChangeListener?.onVolumeChanged(getFilledMl(), progress)
        invalidate()
    }

    fun setOnVolumeChangeListener(listener: OnVolumeChangeListener) {
        this.volumeChangeListener = listener
    }

    fun getFilledMl(): Int {
        return (minMl + progress * (maxMl - minMl)).toInt()
    }

    interface OnVolumeChangeListener {
        fun onVolumeChanged(filledMl: Int, progress: Float)
    }
}