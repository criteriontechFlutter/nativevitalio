package com.critetiontech.ctvitalio.utils

import MovementItem
import android.app.ActionBar
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout

class SleepMovementGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var movementList: List<MovementItem> = emptyList()

    private val barPaint = Paint().apply {
        color = Color.parseColor("#5DA9F6")
        strokeWidth = 12f
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private val centerLinePaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 2f
    }

    fun setData(list: List<MovementItem>) {
        movementList = list
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (movementList.isEmpty()) return

        val centerY = height / 2f

        // Draw center horizontal line
        canvas.drawLine(0f, centerY, width.toFloat(), centerY, centerLinePaint)

        val spacePerItem = width / (movementList.size + 1)

        movementList.forEachIndexed { index, item ->

            val x = spacePerItem * (index + 1)

            val barHeight = getHeightFromType(item.Type)

            canvas.drawLine(
                x.toFloat(),
                centerY - barHeight,
                x.toFloat(),
                centerY + barHeight,
                barPaint
            )
        }
    }

    private fun getHeightFromType(type: String): Float {
        return when (type.lowercase()) {
            "light" -> 20f
            "medium" -> 40f
            "hard" -> 60f
            "vigorous" -> 80f
            else -> 15f
        }
    }
}