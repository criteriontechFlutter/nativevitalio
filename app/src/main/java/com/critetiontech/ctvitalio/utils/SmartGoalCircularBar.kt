package com.critetiontech.ctvitalio.utils


import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt

class SmartGoalCircularBar(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#D9D9D9".toColorInt() // default grey
        style = Paint.Style.STROKE
        strokeWidth = 16f
        strokeCap = Paint.Cap.ROUND
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#1E90FF".toColorInt() // default blue
        style = Paint.Style.STROKE
        strokeWidth = 16f
        strokeCap = Paint.Cap.ROUND
    }

    private val rect = RectF()
    private var progressValue = 0f // 0–100

    // Icon related
    private var icon: Bitmap? = null
    private var iconSize = 40 // dp

    // Convert dp to px
    private fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()

    /** ---------------------- PUBLIC FUNCTIONS ------------------------ **/

    fun setProgress(value: Float) {
        progressValue = value.coerceIn(0f, 100f)
        invalidate()
    }

    fun setProgressColor(colorString: String) {
        progressPaint.color = colorString.toColorInt()
        invalidate()
    }

    fun setRemainingColor(colorString: String) {
        backgroundPaint.color = colorString.toColorInt()
        invalidate()
    }

    fun setIcon(drawable: Drawable) {
        icon = drawableToBitmap(drawable)
        invalidate()
    }

    fun setIconResource(resId: Int) {
        val drawable = ContextCompat.getDrawable(context, resId)
        icon = drawableToBitmap(drawable!!)
        invalidate()
    }

    fun setIconSize(dp: Int) {
        iconSize = dp
        invalidate()
    }

    /** Convert drawable → bitmap */
    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bmp = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bmp
    }

    /** ---------------------- DRAWING ------------------------ **/

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val padding = 20f
        rect.set(padding, padding, width - padding, height - padding)

        val sweepAngle = (progressValue / 100f) * 360f

        // Draw complete grey circle
        canvas.drawArc(rect, -90f, 360f, false, backgroundPaint)

        // Draw blue progress arc
        canvas.drawArc(rect, -90f, sweepAngle, false, progressPaint)

        // Draw icon in center
        icon?.let {
            val sizePx = iconSize.dp()
            val left = (width - sizePx) / 2f
            val top = (height - sizePx) / 2f
            val right = left + sizePx
            val bottom = top + sizePx

            val rectF = RectF(left, top, right, bottom)
            canvas.drawBitmap(it, null, rectF, null)
        }
    }
}