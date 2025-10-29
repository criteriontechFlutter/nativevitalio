package com.critetiontech.ctvitalio.UI.fragments

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.utils.CustomCalendarView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class MedicationFragment : Fragment() {
    private lateinit var dateContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var txtMonth: TextView

    private var selectedPosition = -1
    private val calendar = Calendar.getInstance()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_medication, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        dateContainer = view.findViewById(R.id.date_container)
//        progressBar = view.findViewById(R.id.date_progress)
//        txtMonth = view.findViewById(R.id.txt_month)

        generateDateItems()

    }
    private fun generateDateItems() {
        val inflater = LayoutInflater.from(requireActivity())
        val today = Calendar.getInstance()

        // Get Monday as starting point of week
        val weekStart = today.clone() as Calendar
        weekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd", Locale.getDefault())

        dateContainer.removeAllViews()

        for (i in 0..6) {
            val dateView = inflater.inflate(R.layout.item_date_picker, dateContainer, false)
            val tvDay = dateView.findViewById<TextView>(R.id.tv_day)
            val tvDate = dateView.findViewById<TextView>(R.id.tv_date)

            val date = weekStart.clone() as Calendar
            date.add(Calendar.DAY_OF_MONTH, i)

            tvDay.text = dayFormat.format(date.time)
            tvDate.text = dateFormat.format(date.time)

            // Highlight today's date
            if (isSameDay(date, today)) {
                setDateSelected(tvDate, true)
                selectedPosition = i
                updateMonth(date)
                progressBar.progress = (i + 1) * 15 // starting position progress
            }

            // On click listener
            dateView.setOnClickListener {
                selectDate(i, tvDate, date)
            }

            dateContainer.addView(dateView)
        }
    }
    private fun selectDate(position: Int, tvDate: TextView, date: Calendar) {
        // Reset previous selection
        if (selectedPosition != -1) {
            val prevView = dateContainer.getChildAt(selectedPosition)
            val prevDateText = prevView.findViewById<TextView>(R.id.tv_date)
            setDateSelected(prevDateText, false)
        }

        // Highlight new selection
        setDateSelected(tvDate, true)
        selectedPosition = position

        // Animate progress bar
        val progressValue = ((position + 1) / 7f * 100).toInt()
        ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, progressValue)
            .setDuration(400)
            .start()

        updateMonth(date)
    }

    private fun setDateSelected(tvDate: TextView, selected: Boolean) {
        if (selected) {
            tvDate.setBackgroundResource(R.drawable.date_circle_bg_active)
            tvDate.setTextColor(Color.WHITE)
            tvDate.animate().scaleX(1.1f).scaleY(1.1f).setDuration(150).start()
        } else {
            tvDate.setBackgroundResource(R.drawable.date_circle_bg_grey)
            tvDate.setTextColor(Color.BLACK)
            tvDate.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
        }
    }

    private fun updateMonth(date: Calendar) {
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        txtMonth.text = monthFormat.format(date.time)
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

}