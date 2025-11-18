package com.critetiontech.ctvitalio.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.animation.addListener


class WaveView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var offsetBack = -120f   // start above screen
    private var offsetFront = -160f  // start above screen

    private val pathBack = Path()
    private val pathFront = Path()

    private val paintBack = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintFront = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintBackground = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        // Background (NOT white)
        paintBackground.color = Color.parseColor("#FDEEEE")

        // Back curve
        paintBack.color = Color.parseColor("#F6D9D9")

        // Front curve
        paintFront.color = Color.parseColor("#F3CACA")

        startAnimationSequence()
    }

    // ------------------------------------------------------
    // TWO-PHASE ANIMATION
    // ------------------------------------------------------
    private fun startAnimationSequence() {

        // ---------- PHASE 1: FAST DROP (0.5 seconds) ----------
        val dropBack = ObjectAnimator.ofFloat(this, "offsetBack", -120f, 0f).apply {
            duration = 500
        }

        val dropFront = ObjectAnimator.ofFloat(this, "offsetFront", -160f, 0f).apply {
            duration = 500
        }

        // ---------- PHASE 2: SLOW FLOATING ANIMATION ----------
        val floatBack = ValueAnimator.ofFloat(-20f, 20f).apply {
            duration = 4500
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener {
                offsetBack = it.animatedValue as Float
                invalidate()
            }
        }

        val floatFront = ValueAnimator.ofFloat(-15f, 15f).apply {
            duration = 3800
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener {
                offsetFront = it.animatedValue as Float
                invalidate()
            }
        }

        // CHAIN: Fast drop → Slow float
        AnimatorSet().apply {
            playTogether(dropBack, dropFront)
            addListener(onEnd = {
                floatBack.start()
                floatFront.start()
            })
            start()
        }
    }

    // setters so ObjectAnimator can modify them
    private fun setOffsetBack(value: Float) {
        offsetBack = value
        invalidate()
    }

    private fun setOffsetFront(value: Float) {
        offsetFront = value
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        // ---------- Background ----------
        canvas.drawRect(0f, 0f, w, h, paintBackground)

        // ---------- BACK CURVE ----------
        pathBack.reset()
        pathBack.moveTo(0f, h * 0.32f + offsetBack)
        pathBack.quadTo(
            w * 0.45f,
            h * (0.50f + offsetBack / 50),
            w,
            h * 0.35f + offsetBack
        )
        pathBack.lineTo(w, h)
        pathBack.lineTo(0f, h)
        pathBack.close()
        canvas.drawPath(pathBack, paintBack)

        // ---------- FRONT CURVE ----------
        pathFront.reset()
        pathFront.moveTo(0f, h * 0.52f + offsetFront)
        pathFront.quadTo(
            w * 0.55f,
            h * (0.70f + offsetFront / 45),
            w,
            h * 0.50f + offsetFront
        )
        pathFront.lineTo(w, h)
        pathFront.lineTo(0f, h)
        pathFront.close()
        canvas.drawPath(pathFront, paintFront)
    }
}