package com.critetiontech.ctvitalio.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.View.resolveSize

class ProfilePictureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val profilePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val badgeStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val chevronPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val statusPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var profileBitmap: Bitmap? = null
    private var progress: Float = 75f // Progress percentage (0-100)
    private var badgeLevel: Int = 3 // Badge level (1-5)

    private val profileRect = RectF()
    private val borderRect = RectF()
    private val progressRect = RectF()

    init {
        // Background paint (blue)
        backgroundPaint.color = Color.parseColor("#0D8FFF")

        // Border paint (white)
        borderPaint.apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 16f
        }

        // Progress paint (green arc)
        progressPaint.apply {
            color = Color.parseColor("#00E676")
            style = Paint.Style.STROKE
            strokeWidth = 12f
            strokeCap = Paint.Cap.ROUND
        }

        // Badge background
        badgePaint.color = Color.parseColor("#2196F3")

        // Badge border
        badgeStrokePaint.apply {
            color = Color.parseColor("#FFC107")
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }

        // Star color
        starPaint.color = Color.parseColor("#FFC107")

        // Chevron color
        chevronPaint.apply {
            color = Color.parseColor("#FFC107")
            style = Paint.Style.FILL
        }

        // Status indicator
        statusPaint.color = Color.parseColor("#00E676")
    }

    fun setProfileImage(bitmap: Bitmap) {
        profileBitmap = bitmap
        invalidate()
    }

    fun setProgress(value: Float) {
        progress = value.coerceIn(0f, 100f)
        invalidate()
    }

    fun setBadgeLevel(level: Int) {
        badgeLevel = level.coerceIn(1, 5)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = Math.min(width, height) / 2.5f

        // Draw background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        // Draw white border
        borderRect.set(
            centerX - radius - 20,
            centerY - radius - 20,
            centerX + radius + 20,
            centerY + radius + 20
        )
        canvas.drawCircle(centerX, centerY, radius + 20, borderPaint)

        // Draw progress arc
        progressRect.set(
            centerX - radius - 26,
            centerY - radius - 26,
            centerX + radius + 26,
            centerY + radius + 26
        )
        val sweepAngle = (progress / 100f) * 360f
        canvas.drawArc(progressRect, -90f, sweepAngle, false, progressPaint)

        // Draw profile image
        profileBitmap?.let { bitmap ->
            val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            val matrix = Matrix()

            val scale = (radius * 2) / Math.max(bitmap.width, bitmap.height)
            matrix.setScale(scale, scale)
            matrix.postTranslate(
                centerX - (bitmap.width * scale) / 2,
                centerY - (bitmap.height * scale) / 2
            )

            shader.setLocalMatrix(matrix)
            profilePaint.shader = shader
            canvas.drawCircle(centerX, centerY, radius, profilePaint)
        } ?: run {
            // Draw placeholder if no image
            profilePaint.shader = null
            profilePaint.color = Color.GRAY
            canvas.drawCircle(centerX, centerY, radius, profilePaint)
        }

        // Draw status indicator (green dot)
        val statusX = centerX - radius * 0.6f
        val statusY = centerY + radius * 0.7f
        canvas.drawCircle(statusX, statusY, 18f, statusPaint)
        canvas.drawCircle(statusX, statusY, 14f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 6f
        })

        // Draw badge
        drawBadge(canvas, centerX, centerY + radius + 40, badgeLevel)
    }

    private fun drawBadge(canvas: Canvas, x: Float, y: Float, level: Int) {
        val badgeSize = 120f
        val hexRadius = badgeSize / 2

        // Draw hexagon background
        val hexPath = createHexagonPath(x, y, hexRadius)
        canvas.drawPath(hexPath, badgePaint)
        canvas.drawPath(hexPath, badgeStrokePaint)

        // Draw star
        val starPath = createStarPath(x, y - 15, 25f)
        canvas.drawPath(starPath, starPaint)

        // Draw chevrons based on level
        val chevronSpacing = 12f
        val startY = y + 15
        for (i in 0 until level) {
            val chevronPath = createChevronPath(x, startY + i * chevronSpacing, 30f, 8f)
            canvas.drawPath(chevronPath, chevronPaint)
        }
    }

    private fun createHexagonPath(cx: Float, cy: Float, radius: Float): Path {
        val path = Path()
        for (i in 0..5) {
            val angle = Math.toRadians((60 * i - 30).toDouble())
            val px = cx + radius * Math.cos(angle).toFloat()
            val py = cy + radius * Math.sin(angle).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        return path
    }

    private fun createStarPath(cx: Float, cy: Float, radius: Float): Path {
        val path = Path()
        val innerRadius = radius * 0.4f
        for (i in 0..9) {
            val angle = Math.toRadians((36 * i - 90).toDouble())
            val r = if (i % 2 == 0) radius else innerRadius
            val x = cx + r * Math.cos(angle).toFloat()
            val y = cy + r * Math.sin(angle).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return path
    }

    private fun createChevronPath(cx: Float, cy: Float, width: Float, height: Float): Path {
        val path = Path()
        path.moveTo(cx - width / 2, cy)
        path.lineTo(cx, cy + height)
        path.lineTo(cx + width / 2, cy)
        return path
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredSize = 500
        val width = resolveSize(desiredSize, widthMeasureSpec)
        val height = resolveSize(desiredSize, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }
}