package com.critetiontech.ctvitalio.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.LinearLayout
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.ItemCenterDatePickerBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CenterDatePickerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val binding = ItemCenterDatePickerBinding.inflate(
        android.view.LayoutInflater.from(context), this, true
    )

    private val dateFormat = SimpleDateFormat("EEE dd MMM", Locale.getDefault())
    private var currentCalendar = Calendar.getInstance()

    var onDateChanged: ((Calendar) -> Unit)? = null

    init {
        updateDates()
        setupSwipe()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSwipe() {
        val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onDown(e: MotionEvent): Boolean {
                return true // must return true for onFling to be triggered
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null || e2 == null) return false
                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y

                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) moveToPreviousDateAnimated()
                        else moveToNextDateAnimated()
                        return true
                    }
                }
                return false
            }
        })

        setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true // consume the event
        }
    }


    private fun moveToNextDateAnimated() {
        animateDateChange(isNext = true) {
            currentCalendar.add(Calendar.DAY_OF_MONTH, 1)
            updateDates()
            onDateChanged?.invoke(currentCalendar)
        }
    }

    private fun moveToPreviousDateAnimated() {
        animateDateChange(isNext = false) {
            currentCalendar.add(Calendar.DAY_OF_MONTH, -1)
            updateDates()
            onDateChanged?.invoke(currentCalendar)
        }
    }

    private fun animateDateChange(isNext: Boolean, onAnimationEnd: () -> Unit) {
        val fadeOut = AlphaAnimation(1f, 0f).apply {
            duration = 150
            fillAfter = true
        }

        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 150
            fillAfter = true
        }

        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                onAnimationEnd()
                binding.dateContainer.startAnimation(fadeIn)
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        binding.dateContainer.startAnimation(fadeOut)
    }

    private fun updateDates() {
        val prev = (currentCalendar.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -1) }
        val next = (currentCalendar.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
        val today = Calendar.getInstance()
        val isToday = isSameDay(currentCalendar, today)
        binding.tvPrevDate.text = dateFormat.format(prev.time)
        binding.tvNextDate.text = dateFormat.format(next.time)


// Current date label
        binding.tvCurrentDate.text =
            if (isToday) "Today" else dateFormat.format(currentCalendar.time)

// Show arrows only when not "Today"
        if (isToday) {
            // Hide both arrows
            binding.tvPrevDate.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            binding.tvNextDate.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        } else {
            // Show left arrow for previous date
            binding.tvPrevDate.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_arrow_left, 0, 0, 0
            )
            // Show right arrow for next date
            binding.tvNextDate.setCompoundDrawablesWithIntrinsicBounds(
                0, 0, R.drawable.ic_arrow_right, 0
            )
            binding.tvPrevDate.compoundDrawablePadding = 8
            binding.tvNextDate.compoundDrawablePadding = 8
        }

    }
        private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun setDate(calendar: Calendar) {
        currentCalendar = calendar
        updateDates()
    }
}