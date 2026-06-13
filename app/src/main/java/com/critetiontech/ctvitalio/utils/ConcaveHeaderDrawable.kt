package com.critetiontech.ctvitalio.utils

import android.graphics.*
import android.graphics.drawable.Drawable

/**
 * Draws a filled rectangle with CONCAVE (inward) arcs at the bottom-left and bottom-right corners.
 * The center of each arc sits at the outer corner, so the curve bites inward — creating the
 * "reverse rounded / drop" transition into the content below.
 */
class ConcaveHeaderDrawable(private val fillColor: Int, private val cornerRadius: Float) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = fillColor
        style = Paint.Style.FILL
    }
    private val path = Path()

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        val r = cornerRadius
        val l = bounds.left.toFloat()
        val t = bounds.top.toFloat()
        val w = bounds.right.toFloat()
        val h = bounds.bottom.toFloat()

        path.reset()
        path.moveTo(l, t)
        path.lineTo(w, t)                    // top edge →
        path.lineTo(w, h - r)               // right edge ↓

        // Concave bottom-right corner
        // Arc center = (w, h), radius r, sweep 270°→180° CCW (−90°)
        path.arcTo(RectF(w - r, h - r, w + r, h + r), 270f, -90f)

        path.lineTo(l + r, h)               // bottom edge ←

        // Concave bottom-left corner
        // Arc center = (l, h), radius r, sweep 0°→270° CCW (−90°)
        path.arcTo(RectF(l - r, h - r, l + r, h + r), 0f, -90f)

        path.lineTo(l, t)                   // left edge ↑
        path.close()
    }

    override fun draw(canvas: Canvas) = canvas.drawPath(path, paint)

    override fun setAlpha(alpha: Int) { paint.alpha = alpha; invalidateSelf() }
    override fun setColorFilter(cf: ColorFilter?) { paint.colorFilter = cf; invalidateSelf() }
    @Deprecated("Deprecated in Java")
    override fun getOpacity() = PixelFormat.TRANSLUCENT
}