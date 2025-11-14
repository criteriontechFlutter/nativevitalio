package com.critetiontech.ctvitalio.UI.fragments


import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.critetiontech.ctvitalio.R
import androidx.core.graphics.toColorInt


class WaterRingView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var volumeChangeListener: OnVolumeChangeListener? = null
    private var progress = 0f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var glassBitmap: Bitmap? = null
    private var glassMaskBitmap: Bitmap? = null

    private var fillColor = "#6F4E37".toColorInt()
    private var fillGradient: LinearGradient? = null

    private var minMl = 0
    private var maxMl = 500

    init {
        glassBitmap = BitmapFactory.decodeResource(resources, R.drawable.water_big_circle)
        glassMaskBitmap = BitmapFactory.decodeResource(resources, R.drawable.water_transparentcircle)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (glassBitmap == null || glassMaskBitmap == null) return

        val scaledGlass = Bitmap.createScaledBitmap(glassBitmap!!, width, height, true)
        val scaledMask = Bitmap.createScaledBitmap(glassMaskBitmap!!, width, height, true)

        val tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)

        val fillHeight = height * progress
        val fillTop = height - fillHeight

        // Use gradient or solid fill
        paint.shader = fillGradient ?: run {
            paint.color = fillColor
            null
        }

        // Step 1: Draw fill
        tempCanvas.drawRect(0f, fillTop, width.toFloat(), height.toFloat(), paint)

        // Step 2: Apply mask
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        tempCanvas.drawBitmap(scaledMask, 0f, 0f, paint)
        paint.xfermode = null
        paint.shader = null

        // Step 3: Draw fill result
        canvas.drawBitmap(tempBitmap, 0f, 0f, null)

        // Step 4: Draw cup outline
        canvas.drawBitmap(scaledGlass, 0f, 0f, null)

        // Step 5: Draw centered percentage and ml text
        val filledMl = getFilledMl()
        val centerX = width / 2f
        val centerY = height / 2f
        val percentText = "${(progress * 100).toInt()}%"
        val mlText = "$filledMl ml / $maxMl ml"

        // Determine text color: white if within fill, else dark
        val isInFill = centerY >= fillTop
        val textColor = if (isInFill) Color.WHITE else Color.parseColor("#334155")

        val percentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = 48f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        val mlPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = 28f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        canvas.drawText(percentText, centerX, centerY - 10f, percentPaint)
        canvas.drawText(mlText, centerX, centerY + 30f, mlPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            val y = event.y
            progress = 1 - (y / height).coerceIn(0f, 1f)
            volumeChangeListener?.onVolumeChanged(getFilledMl(), progress)
            invalidate()
        }
        return true
    }

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