package com.critetiontech.ctvitalio.sync.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.critetiontech.ctvitalio.R
import kotlin.math.abs

/**
 * The drawing, used everywhere and drawn once: the usual is a line inside a soft band; nights are
 * dots at even spacing with the last one larger; unworn nights are gaps; a dashed line with no band
 * before night 7. Horizontal for nights, vertical for the day's hours. No axes, no gridlines, and
 * one colour whichever way a dot sits.
 *
 * A dot's value is its distance from the usual, where 1 is about the edge of the band.
 * [playReveal] lets the usual draw across and then pops the dots in along it.
 */
class SyncDrawingView(context: Context) : View(context) {
    data class Spec(
        val dots: List<Float?>,
        val vertical: Boolean = false,
        val dashed: Boolean = false,
        val emphasizeLast: Boolean = true,
        /** Slots to lay out when dots stop early, as while baselining. */
        val slots: Int = dots.size,
        val captionStart: String? = null,
        val captionEnd: String? = null,
        /** Caption above the band, for the read-back, where dots rise above it at the end. */
        val captionTopStart: String? = null,
        val labels: List<String>? = null,
        /** Draws a dashed marker before this slot, for an experiment's start. */
        val dividerBeforeSlot: Int? = null,
        /** Text beside a dot, for a vertical drawing's hot hours. */
        val dotNotes: Map<Int, String> = emptyMap(),
        val rowHeightDp: Float = 44f,
        /** Where the usual sits across a vertical drawing, as a fraction of its width. */
        val usualAtFraction: Float = 0.35f,
    )

    private val density = resources.displayMetrics.density
    private val scaled = resources.displayMetrics.scaledDensity
    private fun dp(v: Float) = v * density

    private val muted = ContextCompat.getColor(context, R.color.sync_muted)
    private val accent = ContextCompat.getColor(context, R.color.sync_accent)

    private val bandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.sync_band)
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accent
        alpha = 110
        strokeWidth = dp(1f)
        style = Paint.Style.STROKE
    }
    private val dashedPaint = Paint(linePaint).apply {
        pathEffect = DashPathEffect(floatArrayOf(dp(3f), dp(3f)), 0f)
    }
    private val dividerPaint = Paint(dashedPaint).apply { alpha = 200 }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent }
    private val captionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = muted
        textSize = 12f * scaled
        typeface = ResourcesCompat.getFont(context, R.font.lato_italic)
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = muted
        textSize = 12f * scaled
        textAlign = Paint.Align.CENTER
        typeface = ResourcesCompat.getFont(context, R.font.lato_medium)
    }
    private val notePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accent
        textSize = 12f * scaled
        typeface = ResourcesCompat.getFont(context, R.font.lato_bold)
    }
    private val rect = RectF()
    private val overshoot = OvershootInterpolator(2.2f)

    /** 0 to 1 across the whole reveal. Starts at 1 so a drawing is complete unless asked to play. */
    private var progress = 1f
    private var animator: ValueAnimator? = null

    var spec: Spec = Spec(emptyList())
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    /** Readers hear the caption text rather than an unlabelled picture. */
    fun describe(description: String) {
        contentDescription = description
    }

    fun playReveal(delayMs: Long = 0) {
        animator?.cancel()
        progress = 0f
        invalidate()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = REVEAL_MS
            startDelay = delayMs
            interpolator = LinearInterpolator()
            addUpdateListener {
                progress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        progress = 1f
        super.onDetachedFromWindow()
    }

    private val bandThickness get() = dp(BAND_DP)
    private val smallRadius get() = dp(SMALL_DOT_DP)
    private val largeRadius get() = dp(LARGE_DOT_DP)

    /** Room either side of the band for dots that have slid out of it. */
    private val travel get() = dp(TRAVEL_DP)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = if (spec.vertical) {
            (spec.slots * dp(spec.rowHeightDp)).toInt()
        } else {
            var h = travel * 2 + bandThickness
            if (spec.captionStart != null || spec.captionEnd != null) h += dp(CAPTION_DP)
            if (spec.captionTopStart != null) h += dp(CAPTION_DP)
            if (spec.labels != null) h += dp(LABEL_DP)
            h.toInt()
        }
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (spec.vertical) drawVertical(canvas) else drawHorizontal(canvas)
    }

    /** The first part of the reveal draws the usual across. */
    private val lineProgress get() = (progress / LINE_SHARE).coerceIn(0f, 1f)

    /** How far along a dot is, staggered in slot order, from 0 to just past 1 for a small pop. */
    private fun dotScale(index: Int, count: Int): Float {
        if (progress >= 1f) return 1f
        val start = LINE_SHARE * 0.6f + (1f - LINE_SHARE * 0.6f - DOT_SHARE) * index / count.coerceAtLeast(1)
        val t = ((progress - start) / DOT_SHARE).coerceIn(0f, 1f)
        return if (t == 0f) 0f else overshoot.getInterpolation(t)
    }

    private fun drawHorizontal(canvas: Canvas) {
        val w = width.toFloat()
        val top = if (spec.captionTopStart != null) dp(CAPTION_DP) else 0f
        val center = top + travel + bandThickness / 2
        val drawn = w * lineProgress

        if (spec.dashed) {
            canvas.drawLine(0f, center, drawn, center, dashedPaint)
        } else {
            val half = bandThickness / 2
            rect.set(0f, center - half, drawn, center + half)
            canvas.drawRoundRect(rect, half, half, bandPaint)
            canvas.drawLine(0f, center, drawn, center, linePaint)
        }

        val step = w / spec.slots.coerceAtLeast(1)
        val lastIndex = spec.dots.indexOfLast { it != null }
        spec.dots.forEachIndexed { i, value ->
            if (value == null) return@forEachIndexed
            val limit = travel + bandThickness / 2 - largeRadius
            val y = center - (compress(value) * bandThickness / 2).coerceIn(-limit, limit)
            val radius = if (spec.emphasizeLast && i == lastIndex) largeRadius else smallRadius
            val scale = dotScale(i, spec.dots.size)
            if (scale > 0f) canvas.drawCircle(step * i + step / 2, y, radius * scale, dotPaint)
        }

        spec.dividerBeforeSlot?.let { slot ->
            val x = step * slot
            if (x <= drawn) canvas.drawLine(x, 0f, x, center + travel + bandThickness / 2, dividerPaint)
        }

        var below = center + bandThickness / 2 + travel
        spec.captionTopStart?.let {
            captionPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(it, 0f, top - dp(4f), captionPaint)
        }
        if (spec.captionStart != null || spec.captionEnd != null) {
            val baseline = below + dp(CAPTION_DP) - dp(5f)
            spec.captionStart?.let {
                captionPaint.textAlign = Paint.Align.LEFT
                canvas.drawText(it, 0f, baseline, captionPaint)
            }
            spec.captionEnd?.let {
                captionPaint.textAlign = Paint.Align.RIGHT
                canvas.drawText(it, w, baseline, captionPaint)
            }
            below += dp(CAPTION_DP)
        }
        spec.labels?.forEachIndexed { i, label ->
            canvas.drawText(label, step * i + step / 2, below + dp(LABEL_DP) - dp(7f), labelPaint)
        }
    }

    /** Linear inside the band; further out, distance is eased so far-out nights still read apart. */
    private fun compress(value: Float): Float {
        val size = abs(value)
        val eased = if (size <= 1f) size else 1f + (size - 1f) * OUTSIDE_BAND_EASE
        return if (value < 0) -eased else eased
    }

    private fun drawVertical(canvas: Canvas) {
        val drawnHeight = height * lineProgress
        val center = width * spec.usualAtFraction
        val half = bandThickness / 2
        rect.set(center - half, 0f, center + half, drawnHeight)
        canvas.drawRoundRect(rect, half, half, bandPaint)
        canvas.drawLine(center, 0f, center, drawnHeight, linePaint)
        val step = dp(spec.rowHeightDp)
        spec.dots.forEachIndexed { i, value ->
            if (value == null) return@forEachIndexed
            val x = center + value * half
            val y = step * i + step / 2
            val scale = dotScale(i, spec.dots.size)
            if (scale <= 0f) return@forEachIndexed
            canvas.drawCircle(x, y, smallRadius * scale, dotPaint)
            spec.dotNotes[i]?.let { note ->
                canvas.drawText(note, x + smallRadius + dp(4f), y + notePaint.textSize / 3, notePaint)
            }
        }
    }

    private companion object {
        const val BAND_DP = 14f
        const val SMALL_DOT_DP = 3.5f
        const val LARGE_DOT_DP = 5.5f
        const val TRAVEL_DP = 14f
        const val CAPTION_DP = 20f
        const val LABEL_DP = 26f
        const val OUTSIDE_BAND_EASE = 0.12f
        const val REVEAL_MS = 820L
        const val LINE_SHARE = 0.4f
        const val DOT_SHARE = 0.22f
    }
}
