package com.critetiontech.ctvitalio.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.Random
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withTranslation

class ConfettiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val particles = mutableListOf<Particle>()
    private val random = Random()
    private var isAnimating = false

    private val colors = intArrayOf(
        "#FFC107".toColorInt(), // Yellow
        "#FF5722".toColorInt(), // Orange
        "#E91E63".toColorInt(), // Pink
        "#00BCD4".toColorInt(), // Cyan
        "#4CAF50".toColorInt(), // Green
        "#9C27B0".toColorInt()  // Purple
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun startConfetti() {
        particles.clear()
        val width = if (width > 0) width else 1080
        val height = if (height > 0) height else 1920

        (0 until 150)
            .forEach { _ ->
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
                
                canvas.withTranslation(particle.x, particle.y) {
                    rotate(particle.rotation)

                    drawRect(
                        -particle.size / 2f,
                        -particle.size / 4f,
                        particle.size / 2f,
                        particle.size / 4f,
                        paint
                    )

                }
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
