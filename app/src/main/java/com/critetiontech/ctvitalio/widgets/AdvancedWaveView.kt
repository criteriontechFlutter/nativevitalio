package com.critetiontech.ctvitalio.widgets


import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.PI

class AdvancedWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val waterPaint = Paint().apply {
        color = 0xFF5DADE2.toInt()
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val wavePaint = Paint().apply {
        color = 0xFF2E86DE.toInt()
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val wavePaint2 = Paint().apply {
        color = 0xFF1E5CB8.toInt()
        style = Paint.Style.FILL
        isAntiAlias = true
        alpha = 150
    }

    private var waveProgress = 0f // 0 to 1
    private var waveOffset = 0f
    private var waveOffset2 = 0f
    private var animationRunning = false
    private var targetProgress = 0.48f

    private val waveAmplitude = 12f
    private val waveFrequency = 0.08f
    private val waveSpeed = 2.5f
    private val waveSpeed2 = 1.8f

    init {
        startAnimation()
    }

    fun setProgress(progress: Float) {
        targetProgress = progress.coerceIn(0f, 1f)
    }

    fun getProgress(): Float = waveProgress

    private fun startAnimation() {
        if (animationRunning) return
        animationRunning = true
        post(animationRunnable)
    }

    private val animationRunnable = object : Runnable {
        override fun run() {
            // Smooth progress animation
            val delta = targetProgress - waveProgress
            if (delta.compareTo(0f) != 0) {
                waveProgress += delta * 0.05f
            }

            // Wave animations with different speeds
            waveOffset += waveSpeed
            waveOffset2 += waveSpeed2

            invalidate()

            if (animationRunning) {
                postDelayed(this, 16) // ~60fps
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val waterLevel = height * (1f - waveProgress)

        // Draw base water fill
        canvas.drawRect(0f, waterLevel, width, height, waterPaint)

        // Draw dual waves for realistic effect
        drawWave(canvas, waterLevel, width, height, waveOffset, wavePaint)
        drawWave(canvas, waterLevel, width, height, waveOffset2, wavePaint2)
    }

    private fun drawWave(
        canvas: Canvas,
        waterLevel: Float,
        width: Float,
        height: Float,
        offset: Float,
        paint: Paint
    ) {
        val path = Path()
        path.moveTo(0f, waterLevel)

        for (x in 0..width.toInt() step 2) {
            val xFloat = x.toFloat()
            val angle = (xFloat * waveFrequency + offset * 0.05f) * PI
            val y = waterLevel - waveAmplitude * sin(angle).toFloat()

            if (x == 0) {
                path.moveTo(xFloat, y)
            } else {
                path.lineTo(xFloat, y)
            }
        }

        path.lineTo(width, height)
        path.lineTo(0f, height)
        path.close()

        canvas.drawPath(path, paint)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animationRunning = false
        removeCallbacks(animationRunnable)
    }
}