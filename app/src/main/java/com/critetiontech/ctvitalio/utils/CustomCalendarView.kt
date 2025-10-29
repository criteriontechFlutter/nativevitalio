package com.critetiontech.ctvitalio.utils


import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.critetiontech.ctvitalio.R
import java.text.SimpleDateFormat
import java.util.*

class CustomCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val headerView: View
    private val monthTitle: TextView
    private val arrowButton: ImageView
    private val fullCalendarView: View
    private val compactCalendarView: View

    private var isExpanded = false
    private var currentMonth = Calendar.getInstance()

    init {
        orientation = VERTICAL
        setWillNotDraw(false)
        background = ContextCompat.getDrawable(context, R.drawable.bg_calendar_rounded)

        LayoutInflater.from(context).inflate(R.layout.view_custom_calendar, this, true)

        headerView = findViewById(R.id.headerView)
        monthTitle = findViewById(R.id.monthTitle)
        arrowButton = findViewById(R.id.arrowButton)
        fullCalendarView = findViewById(R.id.fullCalendarLayout)
        compactCalendarView = findViewById(R.id.compactCalendarLayout)

        updateMonthTitle()

        arrowButton.setOnClickListener {
            toggleCalendar()
        }
        monthTitle.setOnClickListener {
            toggleCalendar()
        }
    }

    private fun updateMonthTitle() {
        val format = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        monthTitle.text = format.format(currentMonth.time)
    }

    private fun toggleCalendar() {
        val startHeight = if (isExpanded) fullCalendarView.height else compactCalendarView.height
        val endHeight = if (isExpanded) compactCalendarView.height else fullCalendarView.height

        val animator = ValueAnimator.ofInt(startHeight, endHeight)
        animator.duration = 400
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            layoutParams.height = value
            requestLayout()
        }
        animator.start()

        isExpanded = !isExpanded
        fullCalendarView.visibility = if (isExpanded) View.VISIBLE else View.GONE
        compactCalendarView.visibility = if (isExpanded) View.GONE else View.VISIBLE
        arrowButton.rotation = if (isExpanded) 180f else 0f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val paint = Paint()
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true

        val path = Path()
        val width = width.toFloat()
        val height = height.toFloat()
        val curveHeight = 80f

        path.moveTo(0f, 0f)
        path.lineTo(0f, height - curveHeight)
        path.quadTo(width / 2, height + curveHeight, width, height - curveHeight)
        path.lineTo(width, 0f)
        path.close()

        canvas.drawPath(path, paint)
    }
}