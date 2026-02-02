package com.critetiontech.ctvitalio.utils



import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.graphics.toColorInt
import kotlin.math.sin

class WaterWidgetWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val waterPaint = Paint().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            color = 0xFF5DADE2.toColorInt()
        }
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val wavePaint = Paint().apply {
        color = 0xFF5DADE2.toColorInt()
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private var waveProgress = 0f // 0 to 1
    private var waveOffset = 0f
    private var animationRunning = false
    private var targetProgress = 0.48f

    private val waveAmplitude = 15f
    private val waveFrequency = 0.05f
    private val waveSpeed = 3f

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
            if (waveProgress < targetProgress) {
                waveProgress = (waveProgress + 0.01f).coerceAtMost(targetProgress)
            } else if (waveProgress > targetProgress) {
                waveProgress = (waveProgress - 0.01f).coerceAtLeast(targetProgress)
            }

            // Wave animation
            waveOffset += waveSpeed

            invalidate()

            if (animationRunning) {
                postDelayed(this, 16) // ~60fps
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val waterLevel = height * (1f - waveProgress)

        // Draw water fill
        canvas.drawRect(0f, waterLevel, width, height, waterPaint)

        // Draw wave
        drawWave(canvas, waterLevel, width, height)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun drawWave(canvas: Canvas, waterLevel: Float, width: Float, height: Float) {
        val path = Path()
        path.moveTo(0f, waterLevel)

        for (x in 0..width.toInt()) {
            val y = waterLevel - waveAmplitude * sin((x * waveFrequency + waveOffset * 0.05f) * Math.PI).toFloat()
            path.lineTo(x.toFloat(), y)
        }

        path.lineTo(width, height)
        path.lineTo(0f, height)
        path.close()

        canvas.drawPath(path, wavePaint)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animationRunning = false
        removeCallbacks(animationRunnable)
    }
}