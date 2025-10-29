package com.critetiontech.ctvitalio.utils


import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.critetiontech.ctvitalio.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs


class VitalioCalendarView(context: Context) : LinearLayout(context) {

    private val calendar = Calendar.getInstance()
    private val goalDates = mutableListOf<Int>()
    private val gridLayout = GridLayout(context)
    private val selectedDateText = TextView(context)
    private val bottomDateScroller = HorizontalScrollView(context)
    private val dateScrollLayout = LinearLayout(context)
    private lateinit var monthLabel: TextView
    private lateinit var dropdownArrow: TextView
    private lateinit var calendarContainer: LinearLayout
    private var selectedDay: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    private var isExpanded = true

    // Modern color scheme matching the image
    private val primaryColor = Color.parseColor("#2196F3")
    private val goalColor = Color.parseColor("#4CAF50")
    private val todayColor = Color.parseColor("#FFA726")
    private val backgroundColor = Color.WHITE
    private val textPrimaryColor = Color.parseColor("#212121")
    private val textSecondaryColor = Color.parseColor("#BDBDBD")
    private val textLightColor = Color.parseColor("#E8E8E8")

    init {
        orientation = VERTICAL
        setPadding(0, 16, 0, 16)
        setBackgroundColor(backgroundColor)

        setupCalendarContainer()
        setupSelectedDateView()
//        setupBottomDateScroller()

        renderCalendar()
    }

    /** CALENDAR CONTAINER (collapsible) **/
    private fun setupCalendarContainer() {
        calendarContainer = LinearLayout(context).apply {
            orientation = VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            setPadding(10, 0, 10, 0)
        }

        setupHeader()
        setupWeekDayLabels()
        val separator = View(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                5  // thickness of line in pixels
            ).apply {
                topMargin = 8
                bottomMargin = 8
            }
            setBackgroundColor(Color.parseColor("#FFFFFF")) // light gray
        }
        calendarContainer.addView(separator)
        setupCalendarGrid()

        addView(calendarContainer)
    }

    /** HEADER (Month + arrows) **/
    private fun setupHeader() {
        val headerLayout = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            setPadding(8, 16, 8, 20)
        }

        val prevButton = TextView(context).apply {
            text = "‹"
            textSize = 35f
            setTextColor(primaryColor)

            setPadding(8, 0, 8, 0)
            setOnClickListener {
                calendar.add(Calendar.MONTH, -1)
                renderCalendar()
            }
        }

        monthLabel = TextView(context).apply {
            text = getMonthYearString()
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(primaryColor)
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        }

        val nextButton = TextView(context).apply {
            text = "›"
            textSize = 35f
            setTextColor(primaryColor)
            setPadding(8, 0, 8, 0)
            setOnClickListener {
                calendar.add(Calendar.MONTH, 1)
                renderCalendar()
            }
        }

        headerLayout.addView(prevButton)
        headerLayout.addView(monthLabel)
        headerLayout.addView(nextButton)

        calendarContainer.addView(headerLayout)
    }

    /** WEEK DAY LABELS **/
    private fun setupWeekDayLabels() {
        val weekLayout = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            setPadding(0, 0, 0, 8)
        }

        val daysOfWeek = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

        for (day in daysOfWeek) {
            val dayLabel = TextView(context).apply {
                text = day
                textSize = 12f
                setTextColor(textSecondaryColor)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            }
            weekLayout.addView(dayLabel)
        }

        calendarContainer.addView(weekLayout)
    }

    /** GRID **/
    private fun setupCalendarGrid() {
        gridLayout.apply {
            columnCount = 7
            rowCount = 6
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        }
        calendarContainer.addView(gridLayout)
    }

    /** SELECTED DATE TEXT WITH DROPDOWN **/
    private fun setupSelectedDateView() {
        val dateContainer = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 16)
            setOnClickListener {
                toggleCalendar()
            }
        }

        selectedDateText.apply {
            textSize = 16f
            setTypeface(null, Typeface.NORMAL)
            gravity = Gravity.CENTER
            setTextColor(textPrimaryColor)
            text = formatSelectedDate()
        }

        dropdownArrow = TextView(context).apply {
            text = " ˄"
            textSize = 16f
            setTextColor(textSecondaryColor)
        }

        dateContainer.addView(selectedDateText)
        dateContainer.addView(dropdownArrow)
        addView(dateContainer)
    }

    /** HORIZONTAL SCROLLER - Wheel Style **/
    private fun setupBottomDateScroller() {
        val scrollerContainer = FrameLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 100)
            setPadding(0, 0, 0, 16)
        }

        dateScrollLayout.orientation = LinearLayout.HORIZONTAL
        dateScrollLayout.gravity = Gravity.CENTER_VERTICAL
        dateScrollLayout.setPadding(200, 0, 200, 0) // Padding to center first/last items

        bottomDateScroller.apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            isHorizontalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
        }

        bottomDateScroller.addView(dateScrollLayout)
        scrollerContainer.addView(bottomDateScroller)

        // Add scroll listener for wheel effect
        bottomDateScroller.viewTreeObserver.addOnScrollChangedListener {
            updateScrollerDates()
        }

        addView(scrollerContainer)
    }

    /** TOGGLE CALENDAR EXPAND/COLLAPSE **/
    private fun toggleCalendar() {
        isExpanded = !isExpanded

        if (isExpanded) {
            // Make visible before animation

            calendarContainer.translationY = -calendarContainer.height.toFloat() // start slightly above
            calendarContainer.visibility = View.VISIBLE
            calendarContainer.alpha = 0f
            // Animate downwards (expand)
            calendarContainer.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(400)
                .start()
        } else {
            // Animate upwards (collapse)
            calendarContainer.animate()
                .translationY(-calendarContainer.height.toFloat())
                .alpha(0f)
                .setDuration(400)
                .withEndAction {

                    calendarContainer.translationY = 0f // reset position
                    calendarContainer.visibility = View.GONE
                }
                .start()
        }

        // Rotate arrow text
        dropdownArrow.text = if (isExpanded) " ˄" else " ˅"
    }


    /** MAIN RENDER **/
    private fun renderCalendar() {
        gridLayout.removeAllViews()

        monthLabel.text = getMonthYearString()

        val tempCal = calendar.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val today = Calendar.getInstance()

        // Previous month's trailing days
        val prevMonthCal = calendar.clone() as Calendar
        prevMonthCal.add(Calendar.MONTH, -1)
        val prevMonthDays = prevMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (i in 0 until firstDayOfWeek) {
            val day = prevMonthDays - firstDayOfWeek + i + 1
            gridLayout.addView(createDayView(day, false, today, daysInMonth))
        }

        // Current month days
        for (day in 1..daysInMonth) {
            gridLayout.addView(createDayView(day, true, today, daysInMonth))
        }

        // Next month's leading days
        val totalCells = firstDayOfWeek + daysInMonth
        val remainingCells = if (totalCells <= 35) 35 - totalCells else 42 - totalCells
        for (day in 1..remainingCells) {
            gridLayout.addView(createDayView(day, false, today, daysInMonth))
        }

        // Update bottom scroller
        populateBottomScroller(daysInMonth)
    }

    private fun createDayView(day: Int, isCurrentMonth: Boolean, today: Calendar, daysInMonth: Int): TextView {
        return TextView(context).apply {
            text = String.format("%02d", day)
            gravity = Gravity.CENTER
            textSize = 12f

            val screenWidth = context.resources.displayMetrics.widthPixels
            val size = (screenWidth - 65) / 7
            layoutParams = GridLayout.LayoutParams().apply {
                width = size
                height = (size * 1.0).toInt()
                setMargins(0, 0, 0, 0)
            }

            if (!isCurrentMonth) {
                setTextColor(textLightColor)
            } else {
                setTextColor(textPrimaryColor)

                when {
                    day == selectedDay && isCurrentMonthSelected() -> {
                        background = createCircleDrawable(primaryColor)
                        setTextColor(Color.WHITE)
                        setTypeface(null, Typeface.BOLD)
                    }
                    goalDates.contains(day) -> {
                        setTextColor(goalColor)
                        setTypeface(null, Typeface.BOLD)
                    }
                    isToday(today, day) -> {
                        setTextColor(todayColor)
                        setTypeface(null, Typeface.BOLD)
                    }
                }

                setOnClickListener {
                    selectedDay = day
                    selectedDateText.text = formatSelectedDate()
                    renderCalendar()
                    scrollToSelectedDate()
                }
            }
        }
    }

    private fun isCurrentMonthSelected(): Boolean {
        val now = Calendar.getInstance()
        return calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == now.get(Calendar.MONTH)
    }

    private fun createCircleDrawable(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }
    }

    private fun formatSelectedDate(): String {
        val c = calendar.clone() as Calendar
        c.set(Calendar.DAY_OF_MONTH, selectedDay)
        val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(c.time)
        val monthName = SimpleDateFormat("MMM", Locale.getDefault()).format(c.time)
        return "$dayName • $selectedDay • $monthName"
    }

    private fun getMonthYearString(): String =
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)

    private fun isToday(today: Calendar, day: Int): Boolean {
        return today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && today.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                && today.get(Calendar.DAY_OF_MONTH) == day
    }

    private fun populateBottomScroller(daysInMonth: Int) {
        dateScrollLayout.removeAllViews()

        for (d in 1..daysInMonth) {
            val isGoal = goalDates.contains(d)

            val dateView = TextView(context).apply {
                text = String.format("%02d", d)
                gravity = Gravity.CENTER
                textSize = 18f

                tag = d

                val size = 65
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    setMargins(8, 0, 8, 0)


                }

                when {
                    isGoal -> {
                        setTextColor(goalColor)
                        setTypeface(null, Typeface.BOLD)
                    }
                    else -> {
                        setTextColor(textSecondaryColor)
                        setTypeface(null, Typeface.NORMAL)
                    }
                }

                setOnClickListener {
                    selectedDay = d
                    selectedDateText.text = formatSelectedDate()
                    renderCalendar()
                    scrollToSelectedDate()
                }
            }
            dateScrollLayout.addView(dateView)
        }

        post {
            scrollToSelectedDate()
            updateScrollerDates()
        }
    }

    private fun scrollToSelectedDate() {
        bottomDateScroller.post {
            val selectedIndex = selectedDay - 1
            val itemWidth = 72
            val scrollX = selectedIndex * itemWidth - (bottomDateScroller.width / 2) + 36
            bottomDateScroller.smoothScrollTo(scrollX.coerceAtLeast(0), 0)
        }
    }

    private fun updateScrollerDates() {
        val centerX = bottomDateScroller.scrollX + bottomDateScroller.width / 2
        var closestDay = 1
        var minDistance = Int.MAX_VALUE

        for (i in 0 until dateScrollLayout.childCount) {
            val child = dateScrollLayout.getChildAt(i) as? TextView ?: continue
            val day = child.tag as? Int ?: continue

            val childCenterX = child.left + child.width / 2
            val distance = abs(centerX - childCenterX)

            // Scale effect based on distance from center
            val scale = 1f - (distance / 300f).coerceAtMost(0.4f)
            child.scaleX = scale
            child.scaleY = scale

            // Update alpha
            child.alpha = 0.3f + (0.7f * scale)

            // Check if selected
            if (day == selectedDay) {
                child.background = createCircleDrawable(primaryColor)
                child.setTextColor(Color.WHITE)
                child.setTypeface(null, Typeface.BOLD)
            } else if (goalDates.contains(day)) {
                child.background = null
                child.setTextColor(goalColor)
                child.setTypeface(null, Typeface.BOLD)
            } else {
                child.background = null
                child.setTextColor(textSecondaryColor)
                child.setTypeface(null, Typeface.NORMAL)
            }

            // Find closest to center
            if (distance < minDistance) {
                minDistance = distance
                closestDay = day
            }
        }

        // Auto-snap to closest when scrolling stops
        if (!bottomDateScroller.isPressed && minDistance > 36) {
            bottomDateScroller.postDelayed({
                if (closestDay != selectedDay) {
                    selectedDay = closestDay
                    selectedDateText.text = formatSelectedDate()
                    renderCalendar()
                }
            }, 100)
        }
    }

    fun setGoalDates(dates: List<Int>) {
        goalDates.clear()
        goalDates.addAll(dates)
        renderCalendar()
    }
}