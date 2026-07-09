package com.critetiontech.ctvitalio.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.Random

class ConfettiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val particles = mutableListOf<Particle>()
    private val random = Random()
    private var isAnimating = false

    private val colors = intArrayOf(
        Color.parseColor("#FFC107"), // Yellow
        Color.parseColor("#FF5722"), // Orange
        Color.parseColor("#E91E63"), // Pink
        Color.parseColor("#00BCD4"), // Cyan
        Color.parseColor("#4CAF50"), // Green
        Color.parseColor("#9C27B0")  // Purple
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun startConfetti() {
        particles.clear()
        val width = if (width > 0) width else 1080
        val height = if (height > 0) height else 1920

        for (i in 0 until 150) {
            particles.add(
                Particle(
                    x = random.nextFloat() * width,
                    y = -random.nextFloat() * height * 0.5f,
                    vx = (random.nextFloat() - 0.5f) * 10f,
                    vy = random.nextFloat() * 15f + 10f,
                    color = colors[random.nextInt(colors.size)],
                    size = random.nextFloat() * 15f + 10f,
                    rotation = random.nextFloat() * 360f,
                    rotationSpeed = (random.nextFloat() - 0.5f) * 10f
                )
            )
        }
        isAnimating = true
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isAnimating) return

        var hasActiveParticles = false

        for (particle in particles) {
            if (particle.y < height) {
                hasActiveParticles = true
                
                particle.x += particle.vx
                particle.y += particle.vy
                particle.rotation += particle.rotationSpeed
                particle.vx += (random.nextFloat() - 0.5f) * 0.5f

                paint.color = particle.color
                
                canvas.save()
                canvas.translate(particle.x, particle.y)
                canvas.rotate(particle.rotation)
                
                canvas.drawRect(
                    -particle.size / 2f,
                    -particle.size / 4f,
                    particle.size / 2f,
                    particle.size / 4f,
                    paint
                )
                
                canvas.restore()
            }
        }

        if (hasActiveParticles) {
            postInvalidateOnAnimation()
        } else {
            isAnimating = false
        }
    }

    private data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        val color: Int,
        val size: Float,
        var rotation: Float,
        val rotationSpeed: Float
    )
}
