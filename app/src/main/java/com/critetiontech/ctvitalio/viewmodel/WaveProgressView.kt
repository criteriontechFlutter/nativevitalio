package com.critetiontech.ctvitalio.viewmodel

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.critetiontech.ctvitalio.R
import kotlin.math.sin
import androidx.core.graphics.toColorInt

class WaveLiquidProgressView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var progress = 60f
    private var animatedProgress = 0f
    private var waveShift = 0f

    private val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val wavePaint1 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val wavePaint2 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val clipCircle = Path()
    private val path1 = Path()
    private val path2 = Path()

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        // get XML attribute app:wavePercent="XX"
//        attrs?.let {
//            val t = context.obtainStyledAttributes(it, R.styleable.WaveLiquidProgressView)
//            progress = t.getInt(R.styleable.WaveLiquidProgressView_wavePercent, 60).toFloat()
//            animatedProgress = progress
//            t.recycle()
//        }

        // ================================
        // ✔ WAVE PAINT (ExACT smooth blue)
        // ================================
        wavePaint1.shader = LinearGradient(
            0f, 0f, 0f, 1000f,
            "#5ABAFE".toColorInt(),
            "#0A76E9".toColorInt(),
            Shader.TileMode.CLAMP
        )
        wavePaint2.shader = LinearGradient(
            0f, 0f, 0f, 1000f,
            "#5ABAFE".toColorInt(),
            "#0A76E9".toColorInt(),
            Shader.TileMode.CLAMP
        )

        // ================================
        // ✔ INNER SOFT WHITE CIRCLE
        // ================================
        innerPaint.color = "#F8FAFF".toColorInt()
        innerPaint.setShadowLayer(35f,0f,0f, "#30000000".toColorInt())

        // ================================
        // ✔ CENTER TEXT
        // ================================
        textPaint.apply {
            color = "#4A5568".toColorInt()
            textSize = 60f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val radius = width / 2f

        // 🌥 OUTER RING (chrome-like)
        val outerGradient = RadialGradient(
            cx, cy, radius,
            intArrayOf(
                "#FFFFFF".toColorInt(),
                "#D6D6D6".toColorInt(),
                "#BEBEBE".toColorInt()
            ),
            floatArrayOf(0.15f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )
        outerPaint.shader = outerGradient
        canvas.drawCircle(cx, cy, radius, outerPaint)

        // ========= CLIP CIRCLE FOR WATER =======
        clipCircle.reset()
        clipCircle.addCircle(cx, cy, radius, Path.Direction.CW)
        canvas.save()
        canvas.clipPath(clipCircle)

        // ========= DYNAMIC WATER LEVEL =========
        val top = height * (1 - animatedProgress / 100f)

        // 🌊 WAVE 1
        path1.reset()
        for(i in 0..width){
            val y = (12 * sin((i + waveShift) * 0.028)) + top
            if(i==0) path1.moveTo(i.toFloat(), y.toFloat())
            else     path1.lineTo(i.toFloat(), y.toFloat())
        }
        path1.lineTo(width.toFloat(), height.toFloat())
        path1.close()

        // 🌊 WAVE 2 slight phase offset
        path2.reset()
        for(i in 0..width){
            val y = (9 * sin((i + waveShift * 1.3) * 0.032) + 8) + top
            if(i==0) path2.moveTo(i.toFloat(), y.toFloat())
            else     path2.lineTo(i.toFloat(), y.toFloat())
        }
        path2.lineTo(width.toFloat(), height.toFloat())
        path2.close()

        canvas.drawPath(path2, wavePaint2)
        canvas.drawPath(path1, wavePaint1)
        canvas.restore()

        // INNER WHITE CIRCLE
        canvas.drawCircle(cx, cy, radius*0.55f, innerPaint)

        // Percentage text
        canvas.drawText("${progress.toInt()}%", cx, cy + 20f, textPaint)

        waveShift += 3
        invalidate()
    }

    fun setWavePercent(p: Int){
        progress = p.toFloat()
        ValueAnimator.ofFloat(animatedProgress, progress).apply {
            duration = 1200
            addUpdateListener {
                animatedProgress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }
}
