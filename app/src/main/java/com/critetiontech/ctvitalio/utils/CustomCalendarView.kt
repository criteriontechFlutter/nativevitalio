package com.critetiontech.ctvitalio.utils


import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import androidx.core.graphics.toColorInt


class VitalioCalendarView(context: Context) : LinearLayout(context) {

    private val calendar = Calendar.getInstance()
    private val goalDates = mutableListOf<Int>()
    private val dateProgressMap = mutableMapOf<Int, Float>() // Store progress for each date
    private val gridLayout = GridLayout(context)
    private val selectedDateText = TextView(context)
    private val bottomDateScroller = HorizontalScrollView(context)
    private val dateScrollLayout = LinearLayout(context)
    private lateinit var monthLabel: TextView
    private lateinit var dropdownArrow: TextView
    private lateinit var calendarContainer: LinearLayout
    private var selectedDay: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    private var selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH)
    private var selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR)
    private var isExpanded = false
    var onDateSelected: ((String) -> Unit)? = null
    // Pixel-perfect color scheme
    private val primaryColor = "#2196F3".toColorInt()
    private val goalColor = "#4CAF50".toColorInt()
    private val todayColor = "#FFA726".toColorInt()
    private val backgroundColor = Color.WHITE
    private val textPrimaryColor = "#212121".toColorInt()
    private val textSecondaryColor = "#9E9E9E".toColorInt()
    private val textLightColor = "#E0E0E0".toColorInt()
    private val separatorColor = "#F5F5F5".toColorInt()

    init {
        orientation = VERTICAL
        setPadding(0, 0, 0, 0)
        setBackgroundColor(backgroundColor)

        // Add rounded corners to the entire view
        clipToOutline = true
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                val width = view.width
                val height = view.height
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(0f, height - 60f)
                    quadTo(width / 2f, height + 60f, width.toFloat(), height - 60f)
                    lineTo(width.toFloat(), 0f)
                    close()
                }

                outline.setConvexPath(path)
            }
        }


        setupCalendarContainer()
        setupSelectedDateView()
        setupBottomDateScroller()

        renderCalendar()

    }

    /** CALENDAR CONTAINER (collapsible) **/
    private fun setupCalendarContainer() {
        calendarContainer = LinearLayout(context).apply {
            orientation = VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            setPadding(16, 0, 16, 0)
        }

        setupHeader()
        setupWeekDayLabels()

        val separator = View(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                2
            ).apply {
                topMargin = 12
                bottomMargin = 16
            }
            setBackgroundColor(separatorColor)
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
            setPadding(16, 20, 16, 16)
        }

        // Left spacer for centering
        val leftSpacer = View(context).apply {
            layoutParams = LayoutParams(0, 1, 1f)
        }

        // Center container with month and arrows
        val centerContainer = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
        }

        val prevButton = TextView(context).apply {
            text = "❮"
            textSize = 18f
            setTextColor(primaryColor)
            setPadding(12, 0, 16, 0)
            setTypeface(null, Typeface.BOLD)
            setOnClickListener {
                calendar.add(Calendar.MONTH, -1)
                renderCalendar()
            }
        }

        monthLabel = TextView(context).apply {
            text = getMonthYearString()
            textSize = 18f
            setTypeface(null, Typeface.NORMAL)
            setTextColor(primaryColor)
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
        }

        val nextButton = TextView(context).apply {
            text = "❯"
            textSize = 18f
            setTextColor(primaryColor)
            setPadding(16, 0, 12, 0)
            setTypeface(null, Typeface.BOLD)
            setOnClickListener {
                calendar.add(Calendar.MONTH, 1)
                renderCalendar()
            }
        }

        centerContainer.addView(prevButton)
        centerContainer.addView(monthLabel)
        centerContainer.addView(nextButton)

        // Right spacer for centering
        val rightSpacer = View(context).apply {
            layoutParams = LayoutParams(0, 1, 1f)
        }

        headerLayout.addView(leftSpacer)
        headerLayout.addView(centerContainer)
        headerLayout.addView(rightSpacer)

        calendarContainer.addView(headerLayout)
    }

    /** WEEK DAY LABELS **/
    private fun setupWeekDayLabels() {
        val weekLayout = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            setPadding(0, 16, 0, 12)
        }

        val daysOfWeek = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

        for (day in daysOfWeek) {
            val dayLabel = TextView(context).apply {
                text = day
                textSize = 12f
                setTextColor(textSecondaryColor)
                gravity = Gravity.CENTER
                layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
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
            setPadding(0, 16, 0, 16)
            setOnClickListener {
                toggleCalendar()
            }
        }

        selectedDateText.apply {
            gravity = Gravity.CENTER
            setTextColor(textPrimaryColor)
            text = formatSelectedDateStyled()
        }

        dropdownArrow = TextView(context).apply {
            text = " ˄"
            textSize = 16f
            setTextColor(textSecondaryColor)
            setPadding(6, 0, 0, 0)
        }

        dateContainer.addView(selectedDateText)
        dateContainer.addView(dropdownArrow)
        addView(dateContainer)
    }


    /** HORIZONTAL SCROLLER - Wheel Style **/
    private fun setupBottomDateScroller() {
        val scrollerContainer = FrameLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 200)
            setPadding(0, 8, 0, 24)
        }

        dateScrollLayout.orientation = HORIZONTAL
        dateScrollLayout.gravity = Gravity.CENTER_VERTICAL
        dateScrollLayout.setPadding(180, 0, 180, 0)

        bottomDateScroller.apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            isHorizontalScrollBarEnabled = false
            overScrollMode = OVER_SCROLL_NEVER
        }

        bottomDateScroller.addView(dateScrollLayout)
        scrollerContainer.addView(bottomDateScroller)
        addView(scrollerContainer)

        var isUserScrolling = false
        var scrollerRunnable: Runnable? = null
        var lastSnapPosition = 0

        bottomDateScroller.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    isUserScrolling = true
                    scrollerRunnable?.let { bottomDateScroller.removeCallbacks(it) }
                    lastSnapPosition = bottomDateScroller.scrollX
                }
                android.view.MotionEvent.ACTION_MOVE -> {
                    // Snap to items while scrolling for one-by-one effect
                    val currentScrollX = bottomDateScroller.scrollX
                    val scrollDelta = abs(currentScrollX - lastSnapPosition)

                    // Snap every 82 pixels (item width)
                    if (scrollDelta >= 82) {
                        snapToNearestDateWhileScrolling()
                        lastSnapPosition = bottomDateScroller.scrollX
                    }
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    isUserScrolling = false
                    // Final snap after user stops scrolling
                    scrollerRunnable = Runnable {
                        snapToNearestDate()
                    }
                    bottomDateScroller.postDelayed(scrollerRunnable!!, 100)
                }
            }
            false
        }

        bottomDateScroller.viewTreeObserver.addOnScrollChangedListener {
            updateScrollWheelEffect()
        }


    }

    private fun snapToNearestDateWhileScrolling() {
        val centerX = bottomDateScroller.scrollX + bottomDateScroller.width / 2f
        var closestDay = selectedDay
        var minDistance = Float.MAX_VALUE
        var targetScrollX = bottomDateScroller.scrollX

        for (i in 0 until dateScrollLayout.childCount) {
            val child = dateScrollLayout.getChildAt(i)
            val day = child.tag as? Int ?: continue
            val childCenterX = (child.left + child.right) / 2f
            val distance = abs(centerX - childCenterX)

            if (distance < minDistance) {
                minDistance = distance
                closestDay = day
                targetScrollX = (childCenterX - bottomDateScroller.width / 2f).toInt()
            }
        }

        // Only update if we found a different date
        if (closestDay != selectedDay) {
            selectedDay = closestDay
            selectedMonth = calendar.get(Calendar.MONTH)
            selectedYear = calendar.get(Calendar.YEAR)
            selectedDateText.text = formatSelectedDateStyled()

            // Update calendar without re-rendering wheel to avoid scroll jump
            updateCalendarSelection()
        }
    }

    private fun updateCenteredDate() {
        val centerX = bottomDateScroller.scrollX + bottomDateScroller.width / 2f
        var closestDay = selectedDay
        var minDistance = Float.MAX_VALUE

        for (i in 0 until dateScrollLayout.childCount) {
            val child = dateScrollLayout.getChildAt(i)
            val day = child.tag as? Int ?: continue
            val childCenterX = (child.left + child.right) / 2f
            val distance = abs(centerX - childCenterX)

            if (distance < minDistance) {
                minDistance = distance
                closestDay = day
            }
        }

        // Update selection if closest date changed
        if (closestDay != selectedDay) {
            selectedDay = closestDay
            selectedMonth = calendar.get(Calendar.MONTH)
            selectedYear = calendar.get(Calendar.YEAR)
            selectedDateText.text = formatSelectedDateStyled()
            renderCalendar()
        }
    }

    private fun updateCalendarSelection() {
        // Only update calendar grid without re-rendering wheel
        for (i in 0 until gridLayout.childCount) {
            val child = gridLayout.getChildAt(i) as? TextView ?: continue
            val dayText = child.text.toString().toIntOrNull() ?: continue

            // Reset all backgrounds and colors first
            if (dayText == selectedDay && isDateSelected()) {
                child.background = createCircleDrawable(primaryColor)
                child.setTextColor(Color.WHITE)
                child.setTypeface(null, Typeface.BOLD)
            } else {
                child.background = null
                if (child.currentTextColor == Color.WHITE) {
                    child.setTextColor(textPrimaryColor)
                }
            }
        }
    }

    private fun snapToNearestDate() {
        val centerX = bottomDateScroller.scrollX + bottomDateScroller.width / 2f
        var closestDay = selectedDay
        var minDistance = Float.MAX_VALUE
        var targetScrollX = bottomDateScroller.scrollX

        for (i in 0 until dateScrollLayout.childCount) {
            val child = dateScrollLayout.getChildAt(i)
            val day = child.tag as? Int ?: continue
            val childCenterX = (child.left + child.right) / 2f
            val distance = abs(centerX - childCenterX)

            if (distance < minDistance) {
                minDistance = distance
                closestDay = day
                targetScrollX = (childCenterX - bottomDateScroller.width / 2f).toInt()
            }
        }

        // Snap to the centered date
        bottomDateScroller.smoothScrollTo(targetScrollX.coerceAtLeast(0), 0)

        // Update selection
        if (closestDay != selectedDay) {
            selectedDay = closestDay
            selectedMonth = calendar.get(Calendar.MONTH)
            selectedYear = calendar.get(Calendar.YEAR)
            selectedDateText.text = formatSelectedDateStyled()
            renderCalendar()
        }
    }

    private fun updateScrollWheelEffect() {
        val centerX = bottomDateScroller.scrollX + bottomDateScroller.width / 2f
        val maxOffset = 80f // Maximum vertical offset for the curve
        val visibleRange = bottomDateScroller.width / 1.5f // Wider range for smoother curve

        for (i in 0 until dateScrollLayout.childCount) {
            val child = dateScrollLayout.getChildAt(i)
            val childCenterX = (child.left + child.right) / 2f
            val distanceFromCenter = abs(childCenterX - centerX)

            // Calculate normalized distance (0 at center, 1 at edge of visible range)
            val normalizedDistance = (distanceFromCenter / visibleRange).coerceIn(0f, 1f)

            // Parabolic curve for smooth arc effect - REVERSED (center goes up, edges go down)
            val curveValue = normalizedDistance * normalizedDistance
            val offsetY = -curveValue * maxOffset // Negative to reverse the curve

            // Apply transformations
            child.translationY = offsetY

            // Scale effect - shrink items at the edges
            val scale = 1f - (normalizedDistance * 0.35f)
            child.scaleX = scale.coerceAtLeast(0.65f)
            child.scaleY = scale.coerceAtLeast(0.65f)

            // Fade effect - make edge items more transparent
            val alpha = 1f - (normalizedDistance * 0.5f)
            child.alpha = alpha.coerceAtLeast(0.5f)
        }
    }

    /** TOGGLE CALENDAR EXPAND/COLLAPSE **/
    private fun toggleCalendar() {
        isExpanded = !isExpanded

        if (isExpanded) {
            calendarContainer.translationY = -calendarContainer.height.toFloat()
            calendarContainer.visibility = VISIBLE
            calendarContainer.alpha = 0f

            calendarContainer.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(300)
                .start()
        } else {
            calendarContainer.animate()
                .translationY(-calendarContainer.height.toFloat())
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    calendarContainer.translationY = 0f
                    calendarContainer.visibility = GONE
                }
                .start()
        }

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

        val prevMonthCal = calendar.clone() as Calendar
        prevMonthCal.add(Calendar.MONTH, -1)
        val prevMonthDays = prevMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (i in 0 until firstDayOfWeek) {
            val day = prevMonthDays - firstDayOfWeek + i + 1
            gridLayout.addView(createDayView(day, false, today, daysInMonth))
        }

        for (day in 1..daysInMonth) {
            gridLayout.addView(createDayView(day, true, today, daysInMonth))
        }

        val totalCells = firstDayOfWeek + daysInMonth
        val remainingCells = if (totalCells <= 35) 35 - totalCells else 42 - totalCells
        for (day in 1..remainingCells) {
            gridLayout.addView(createDayView(day, false, today, daysInMonth))
        }

        populateBottomScroller(daysInMonth)
    }

    private fun createDayView(day: Int, isCurrentMonth: Boolean, today: Calendar, daysInMonth: Int): TextView {
        return TextView(context).apply {
            text = String.format("%02d", day)
            gravity = Gravity.CENTER
            textSize = 15f

            val screenWidth = context.resources.displayMetrics.widthPixels
            val size = (screenWidth - 64) / 7
            layoutParams = GridLayout.LayoutParams().apply {
                width = size
                height = size
                setMargins(2, 3, 2, 3)
            }

            if (!isCurrentMonth) {
                setTextColor(textLightColor)
            } else {
                setTextColor(textPrimaryColor)

                when {
                    day == selectedDay && isDateSelected() -> {
                        // Create a smaller, more proportional circle
                        val circleSize = (size * 0.7f).toInt()
                        setPadding(
                            (size - circleSize) / 2,
                            (size - circleSize) / 2,
                            (size - circleSize) / 2,
                            (size - circleSize) / 2
                        )
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
                    selectedMonth = calendar.get(Calendar.MONTH)
                    selectedYear = calendar.get(Calendar.YEAR)
                    selectedDateText.text = formatSelectedDateStyled()
                    renderCalendar()
                    scrollToSelectedDate()

                    // 🔥 Notify Fragment
                    onDateSelected?.invoke(getSelectedDateString("yyyy-MM-dd"))
                }
//                setOnClickListener {
//                    selectedDay = day
//                    selectedMonth = calendar.get(Calendar.MONTH)
//                    selectedYear = calendar.get(Calendar.YEAR)
//                    selectedDateText.text = formatSelectedDate()
//                    renderCalendar()
//                    scrollToSelectedDate()
//                }
            }
        }
    }

    private fun isDateSelected(): Boolean {
        return calendar.get(Calendar.YEAR) == selectedYear &&
                calendar.get(Calendar.MONTH) == selectedMonth
    }

    private fun createCircleDrawable(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }
    }

    private fun createRingDrawable(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setStroke(4, color)
            setColor(Color.TRANSPARENT)
        }
    }

    private fun createProgressRingView(day: Int, progress: Float): FrameLayout {
        return FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setPadding(4, 4, 4, 4)

            // Add canvas for progress ring
            val progressView = object : View(context) {
                private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 5f
                    strokeCap = Paint.Cap.ROUND
                    color = todayColor
                }

                private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 5f
                    color = Color.parseColor("#EEEEEE")
                }

                override fun onDraw(canvas: Canvas) {
                    super.onDraw(canvas)
                    if (width == 0 || height == 0) return

                    val centerX = width / 2f
                    val centerY = height / 2f
                    val strokeOffset = 6f
                    val radius = (width.coerceAtMost(height) / 2f) - strokeOffset

                    val rect = RectF(
                        centerX - radius,
                        centerY - radius,
                        centerX + radius,
                        centerY + radius
                    )

                    // Draw background circle (full ring)
                    canvas.drawCircle(centerX, centerY, radius, bgPaint)

                    // Draw progress arc (starts from top, goes clockwise)
                    if (progress > 0) {
                        val sweepAngle = 360f * progress
                        canvas.drawArc(rect, -90f, sweepAngle, false, progressPaint)
                    }
                }
            }

            addView(progressView, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ))

            // Add date text on top
            val dateText = TextView(context).apply {
                text = day.toString()
                gravity = Gravity.CENTER
                textSize = 22f
                setTextColor(todayColor)
                setTypeface(null, Typeface.BOLD)
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }
            addView(dateText)
        }
    }

    private fun formatSelectedDate(): String {
        val c = Calendar.getInstance()
        c.set(Calendar.YEAR, selectedYear)
        c.set(Calendar.MONTH, selectedMonth)
        c.set(Calendar.DAY_OF_MONTH, selectedDay)
        val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(c.time)
        val monthName = SimpleDateFormat("MMM", Locale.getDefault()).format(c.time)
        return "$dayName •  $selectedDay  • $monthName"
    }

    private fun formatSelectedDateStyled(): SpannableStringBuilder {
        val c = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.MONTH, selectedMonth)
            set(Calendar.DAY_OF_MONTH, selectedDay)
        }

        val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(c.time)
        val monthName = SimpleDateFormat("MMM", Locale.getDefault()).format(c.time)
        val dot = "  •  "

        val builder = SpannableStringBuilder()

        fun appendStyled(text: String, sizeSp: Float, style: Int) {
            val start = builder.length
            builder.append(text)
            builder.setSpan(
                AbsoluteSizeSpan(sizeSp.toInt(), true),
                start,
                builder.length,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
            builder.setSpan(
                StyleSpan(style),
                start,
                builder.length,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
        }

        appendStyled(dayName, 14f, Typeface.NORMAL,)
        builder.append(dot)

        appendStyled(selectedDay.toString(), 22f, Typeface.BOLD)
        builder.append(dot)

        appendStyled(monthName, 14f, Typeface.NORMAL)

        return builder
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
        val today = Calendar.getInstance()

        for (d in 1..daysInMonth) {
            val isGoal = goalDates.contains(d)
            val isTodayDate = isToday(today, d)
            val progress = dateProgressMap[d] ?: 0f

            val dateContainer = FrameLayout(context).apply {
                layoutParams = LayoutParams(70, 70).apply {
                    setMargins(6, 0, 6, 0)
                }
                tag = d
            }

            // Create the appropriate view based on conditions
            val contentView = when {
                d == selectedDay && isDateSelected() && progress > 0f -> {
                    // Selected date with progress ring
                    createProgressRingView(d, progress)
                }
                d == selectedDay && isDateSelected() -> {
                    // Selected date without progress (solid blue circle)
                    TextView(context).apply {
                        text = d.toString()
                        gravity = Gravity.CENTER
                        textSize = 20f
                        background = createCircleDrawable(primaryColor)
                        setTextColor(Color.WHITE)
                        setTypeface(null, Typeface.BOLD)
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                    }
                }
                isTodayDate && progress > 0f -> {
                    // Today's date with progress ring
                    createProgressRingView(d, progress)
                }
                isTodayDate -> {
                    // Today's date without progress (orange ring)
                    TextView(context).apply {
                        text = d.toString()
                        gravity = Gravity.CENTER
                        textSize = 20f
                        background = createRingDrawable(todayColor)
                        setTextColor(todayColor)
                        setTypeface(null, Typeface.BOLD)
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                    }
                }
                isGoal -> {
                    // Goal date (green text)
                    TextView(context).apply {
                        text = d.toString()
                        gravity = Gravity.CENTER
                        textSize = 20f
                        setTextColor(goalColor)
                        setTypeface(null, Typeface.BOLD)
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                    }
                }
                else -> {
                    // Regular date
                    TextView(context).apply {
                        text = d.toString()
                        gravity = Gravity.CENTER
                        textSize = 20f
                        setTextColor(textSecondaryColor)
                        setTypeface(null, Typeface.NORMAL)
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                    }
                }
            }

            dateContainer.addView(contentView)
            dateContainer.setOnClickListener {
                selectedDay = d
                selectedMonth = calendar.get(Calendar.MONTH)
                selectedYear = calendar.get(Calendar.YEAR)
                selectedDateText.text = formatSelectedDateStyled()
                renderCalendar()
                scrollToSelectedDate()
            }

            dateScrollLayout.addView(dateContainer)
        }

        // Ensure the selected date is valid for current month
        if (selectedDay > daysInMonth && isDateSelected()) {
            selectedDay = daysInMonth
            selectedDateText.text = formatSelectedDateStyled()
        }

        post {
            scrollToSelectedDate()
            // Trigger initial curve effect
            updateScrollWheelEffect()
        }
    }

    private fun scrollToSelectedDate() {
        bottomDateScroller.post {
            // Find the actual child view for selected day
            for (i in 0 until dateScrollLayout.childCount) {
                val child = dateScrollLayout.getChildAt(i)
                val day = child.tag as? Int ?: continue

                if (day == selectedDay) {
                    // Calculate scroll position to center this child
                    val childCenterX = (child.left + child.right) / 2f
                    val scrollerCenterX = bottomDateScroller.width / 2f
                    val targetScrollX = (childCenterX - scrollerCenterX).toInt()

                    bottomDateScroller.smoothScrollTo(targetScrollX.coerceAtLeast(0), 0)
                    break
                }
            }
        }
    }

    fun setGoalDates(dates: List<Int>) {
        goalDates.clear()
        goalDates.addAll(dates)
        renderCalendar()
    }

    fun setDateProgress(day: Int, progress: Float) {
        dateProgressMap[day] = progress.coerceIn(0f, 1f)
        renderCalendar()
    }

    fun setDateProgressMap(progressMap: Map<Int, Float>) {
        dateProgressMap.clear()
        progressMap.forEach { (day, progress) ->
            dateProgressMap[day] = progress.coerceIn(0f, 1f)
        }
        renderCalendar()
    }

    fun clearDateProgress() {
        dateProgressMap.clear()
        renderCalendar()
    }

    /**
     * Get the currently selected date as a Calendar instance
     */
    fun getSelectedDate(): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.MONTH, selectedMonth)
            set(Calendar.DAY_OF_MONTH, selectedDay)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    /**
     * Get the selected date as a formatted string
     */
    fun getSelectedDateString(format: String = "yyyy-MM-dd"): String {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        return dateFormat.format(getSelectedDate().time)
    }

    /**
     * Get the selected day of month (1-31)
     */
    fun getSelectedDay(): Int = selectedDay

    /**
     * Get the selected month (0-11, where 0 is January)
     */
    fun getSelectedMonth(): Int = selectedMonth

    /**
     * Get the selected year
     */
    fun getSelectedYear(): Int = selectedYear

    /**
     * Get selected date components as a Triple (day, month, year)
     */
    fun getSelectedDateComponents(): Triple<Int, Int, Int> {
        return Triple(selectedDay, selectedMonth, selectedYear)
    }
}



