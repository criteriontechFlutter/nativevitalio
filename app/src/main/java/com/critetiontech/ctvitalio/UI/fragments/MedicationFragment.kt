package com.critetiontech.ctvitalio.UI.fragments

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.AttributeSet
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentMedicationBinding

import com.critetiontech.ctvitalio.utils.VitalioCalendarView
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


class MedicationFragment : Fragment() {

    private var _binding: FragmentMedicationBinding? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMedicationBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

       // _binding?.calendarContainer?.background = ContextCompat.getDrawable(requireContext(), R.drawable.bottom_curve_bg)

        // Create and add custom calendar view
        val calendarView = VitalioCalendarView(requireContext())

        _binding?.calendarContainer?.addView(calendarView)


        // Highlight specific goal dates
        calendarView.setGoalDates(listOf(8, 9, 10, 13, 15))

    }

}


class CurvedNumberView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val numbers = (10..18).toList()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 48f
        textAlign = Paint.Align.CENTER
    }

    private var selectedIndex = numbers.indexOf(14)
    private var touchStartX = 0f
    private var totalScrollX = 0f

    // --- Set fixed height and width ---
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 600 // px or use dp conversion if needed
        val desiredHeight = 200
        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val topY = 0f // arc should appear upward (numbers above the curve)
        val radius = width / 1.5f
        val angleStep = 20f // controls spacing and curvature

        for (i in numbers.indices) {
            val offsetFromCenter = i - selectedIndex

            // 🔁 Reverse the curve: flip vertical direction by subtracting angle
            val angle = 90 + (offsetFromCenter * angleStep)
            val rad = Math.toRadians(angle.toDouble())

            val x = (centerX + radius * cos(rad)).toFloat()
            val y = (topY + radius * sin(rad)).toFloat()

            val scale = if (i == selectedIndex) 1.3f else 1.0f
            paint.textSize = 48f * scale
            paint.color = if (i == selectedIndex) Color.parseColor("#FFC107") else Color.GRAY
            paint.isFakeBoldText = (i == selectedIndex)

            canvas.drawText(numbers[i].toString(), x, y, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = event.x
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.x - touchStartX
                totalScrollX += deltaX
                touchStartX = event.x

                if (abs(totalScrollX) > 100) { // swipe threshold
                    if (totalScrollX < 0 && selectedIndex < numbers.lastIndex) {
                        selectedIndex++
                    } else if (totalScrollX > 0 && selectedIndex > 0) {
                        selectedIndex--
                    }
                    totalScrollX = 0f
                    invalidate()
                }
            }
        }
        return true
    }

    fun getSelectedNumber(): Int = numbers[selectedIndex]
}
