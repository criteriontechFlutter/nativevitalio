package com.criterion.nativevitalio.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.criterion.nativevitalio.R


class GlassView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var volumeChangeListener: OnVolumeChangeListener? = null
    private var progress = 0f // 0.0 to 1.0

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var glassBitmap: Bitmap? = null
    private var glassMaskBitmap: Bitmap? = null

    private var fillColor = Color.BLUE
    private var fillGradient: LinearGradient? = null

    private var minMl = 0
    private var maxMl = 500

    init {
        glassBitmap = BitmapFactory.decodeResource(resources, R.drawable.watergllass)
        glassMaskBitmap = BitmapFactory.decodeResource(resources, R.drawable.juiceglasss)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (glassBitmap == null || glassMaskBitmap == null) return

        val scaledGlass = Bitmap.createScaledBitmap(glassBitmap!!, width, height, true)
        val scaledMask = Bitmap.createScaledBitmap(glassMaskBitmap!!, width, height, true)

        val tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)

        val fillHeight = height * progress
        val top = height - fillHeight

        // Fill color or gradient
        paint.shader = fillGradient ?: run {
            paint.color = fillColor
            null
        }

        // Step 1: Draw colored rect
        tempCanvas.drawRect(0f, top, width.toFloat(), height.toFloat(), paint)

        // Step 2: Mask with inner fill shape only
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        tempCanvas.drawBitmap(scaledMask, 0f, 0f, paint)
        paint.xfermode = null
        paint.shader = null

        // Step 3: Draw fill result
        canvas.drawBitmap(tempBitmap, 0f, 0f, null)

        // Step 4: Draw full glass outline
        canvas.drawBitmap(scaledGlass, 0f, 0f, null)

        // Step 5: Draw percentage and ml text
        val filledMl = getFilledMl()
        val centerX = width / 2f
        val centerY = height / 2f

        val percentText = "${(progress * 100).toInt()}%"
        val mlText = "$filledMl ml / $maxMl ml"

        val percentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#334155")
            textSize = 50f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        val mlPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1F2937")
            textSize = 32f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        canvas.drawText(percentText, centerX, centerY - 20f, percentPaint)
        canvas.drawText(mlText, centerX, centerY + 30f, mlPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN) {
            val y = event.y
            progress = 1 - (y / height).coerceIn(0f, 1f)
            volumeChangeListener?.onVolumeChanged(getFilledMl(), progress)
            invalidate()
        }
        return true
    }

    // Set 4-color gradient fill
    fun setFillGradient(color1: Int, color2: Int, color3: Int, color4: Int) {
        fillGradient = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            intArrayOf(color1, color2, color3, color4),
            floatArrayOf(0f, 0.33f, 0.66f, 1f),
            Shader.TileMode.CLAMP
        )
        invalidate()
    }

    fun setVolumeRange(min: Int, max: Int) {
        minMl = min
        maxMl = max
        invalidate()
    }

    fun setFillColor(color: Int) {
        fillGradient = null
        fillColor = color
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

//class GlassFillView @JvmOverloads constructor(
//    context: Context,
//    attrs: AttributeSet? = null
//) : View(context, attrs) {
//
//    private var fillPercent = 0f
//    private var glassSize = 150
//    private var onValueChange: ((Int, Int) -> Unit)? = null
//
//    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
//        style = Paint.Style.STROKE
//        strokeWidth = 4f
//        color = Color.parseColor("#D1D5DB")
//    }
//
//    fun setGlassSize(size: Int) {
//        glassSize = size
//        fillPercent = 0f
//        invalidate()
//    }
//
//    fun setOnFillChangedListener(callback: (percent: Int, ml: Int) -> Unit) {
//        onValueChange = callback
//    }
//
//    fun getCurrentMl(): Int = (fillPercent * glassSize).toInt()
//
//    private fun getGradientPaint(): Paint {
//        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
//            style = Paint.Style.FILL
//            shader = LinearGradient(
//                0f, height * (1f - fillPercent), 0f, height.toFloat(),
//                Color.parseColor("#E0F2FE"),
//                Color.parseColor("#BFDBFE"),
//                Shader.TileMode.CLAMP
//            )
//        }
//    }
//
//    @SuppressLint("DrawAllocation")
//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//
//        val w = width.toFloat()
//        val h = height.toFloat()
//        val padding = 40f
//        val cornerRadius = 40f
//
//        val left = padding
//        val right = w - padding
//        val glassRect = RectF(left, 0f, right, h)
//
//        // Draw outline
//        canvas.drawRoundRect(glassRect, cornerRadius, cornerRadius, outlinePaint)
//
//        // Fill level
//        val fillTop = h * (1f - fillPercent)
//        val fillRect = RectF(left, fillTop, right, h)
//        canvas.drawRect(fillRect, getGradientPaint())
//
//        // Center text
//        val centerX = (left + right) / 2f
//        val centerY = h / 2f
//
//        val percentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
//            color = Color.parseColor("#1F2937")
//            textSize = 70f
//            textAlign = Paint.Align.CENTER
//            isFakeBoldText = true
//        }
//
//        val mlPaint = Paint(percentPaint).apply {
//            textSize = 38f
//            isFakeBoldText = false
//        }
//
//        canvas.drawText("${(fillPercent * 100).toInt()}%", centerX, centerY - 20f, percentPaint)
//        canvas.drawText("${getCurrentMl()} ml of $glassSize ml", centerX, centerY + 40f, mlPaint)
//    }
//
//    @SuppressLint("ClickableViewAccessibility")
//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
//            val y = event.y.coerceIn(0f, height.toFloat())
//            val newPercent = 1f - (y / height)
//            animateFillTo(newPercent)
//            return true
//        }
//        return super.onTouchEvent(event)
//    }
//
//    private fun animateFillTo(targetPercent: Float) {
//        ValueAnimator.ofFloat(fillPercent, targetPercent).apply {
//            duration = 50
//            interpolator = DecelerateInterpolator()
//            addUpdateListener {
//                fillPercent = it.animatedValue as Float
//                val percentInt = (fillPercent * 100).toInt()
//                val ml = ((percentInt / 100f) * glassSize).toInt()
//                onValueChange?.invoke(percentInt, ml)
//                invalidate()
//            }
//        }.start()
//    }
//}