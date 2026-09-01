package com.critetiontech.ctvitalio.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.NumberPicker
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.BottomSheetMapAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * DateUtils.kt
 *
 * Utility functions for date formatting and date-range calculations.
 *
 * UI helpers (showListBottomSheet, showHeightPicker) are kept here
 * temporarily for backward compatibility but should migrate to a
 * dedicated UiPickerUtils.kt file in future.
 *
 * Removed: the old commented-out implementations of getLastWeekRange(),
 * getLastMonthRange(), and showListBottomSheet(). See ARCHIVED_CODE.kt
 * if you need to review historical implementations.
 */
object DateUtils {

    private val defaultSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // ─── Format helpers ───────────────────────────────────────────────────────

    /** Converts a date string from one format to another. Returns input on failure. */
    fun formatDate(input: String, inputFormat: String, outputFormat: String): String = try {
        val parser = SimpleDateFormat(inputFormat, Locale.getDefault())
        val formatter = SimpleDateFormat(outputFormat, Locale.getDefault())
        parser.parse(input)?.let { formatter.format(it) } ?: input
    } catch (e: Exception) {
        input
    }

    /** Formats a [Date] object to a string using the given pattern. */
    fun format(date: Date, format: String): String =
        SimpleDateFormat(format, Locale.getDefault()).format(date)

    /** Converts a Unix timestamp to a formatted date string. */
    fun fromTimestamp(timestamp: Long, format: String = "dd MMM yyyy, hh:mm a"): String =
        format(Date(timestamp), format)

    /** Returns today's date as a string in the given format. */
    fun today(format: String = "dd MMM yyyy"): String = format(Date(), format)

    /** Returns "Today", "Yesterday", or the formatted date for older dates. */
    fun getRelativeDay(date: Date): String {
        val cal       = Calendar.getInstance().apply { time = date }
        val today     = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }

        return when {
            cal.isSameDay(today)     -> "Today"
            cal.isSameDay(yesterday) -> "Yesterday"
            else                     -> format(date, "dd MMM yyyy")
        }
    }

    private fun Calendar.isSameDay(other: Calendar): Boolean =
        get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
        get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)

    // ─── Range helpers ────────────────────────────────────────────────────────

    /** Returns (fromDate, toDate) spanning the last 7 days. */
    fun getLastWeekRange(): Pair<String, String> {
        val to   = Calendar.getInstance()
        val from = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }
        return Pair(defaultSdf.format(from.time), defaultSdf.format(to.time))
    }

    /** Returns (fromDate, toDate) spanning the last 31 days. */
    fun getLastMonthRange(): Pair<String, String> {
        val to   = Calendar.getInstance()
        val from = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -31) }
        return Pair(defaultSdf.format(from.time), defaultSdf.format(to.time))
    }

    /** Returns today's date as "yyyy-MM-dd". */
    fun getTodayDate(): String = defaultSdf.format(Date())

    /** Returns today's datetime as "yyyy-MM-dd HH:mm:ss". */
    fun getTodayDateTime(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

    // ─── String helpers ───────────────────────────────────────────────────────

    /** Converts "hello world" → "Hello World". */
    fun toCamelCase(input: String): String =
        input.split(" ").joinToString(" ") {
            it.lowercase().replaceFirstChar(Char::uppercaseChar)
        }

    // ─── UI Pickers (to be moved to UiPickerUtils.kt) ────────────────────────

    fun showListBottomSheet(
        context: Context,
        title: String,
        list: List<Map<String, Any>>,
        displayKey: String,
        onSelected: (Map<String, Any>) -> Unit
    ) {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_list, null)
        dialog.setContentView(view)

        view.findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = BottomSheetMapAdapter(list, displayKey) {
                onSelected(it)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    fun showHeightPicker(context: Context, onSelected: (String) -> Unit) {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.height_picker_bottom_sheet, null)
        dialog.setContentView(view)

        val npFeet = view.findViewById<NumberPicker>(R.id.npFeet)
        val npInch = view.findViewById<NumberPicker>(R.id.npInch)
        val npCm   = view.findViewById<NumberPicker>(R.id.npCm)
        val npUnit = view.findViewById<NumberPicker>(R.id.npUnit)
        val imperialContainer = view.findViewById<LinearLayout>(R.id.containerImperial)
        val btnDone = view.findViewById<Button>(R.id.btnDone)

        val unitList = arrayOf("ft", "in", "cm")

        npFeet.apply { minValue = 3; maxValue = 8; value = 5 }
        npInch.apply { minValue = 0; maxValue = 11; value = 7 }
        npCm.apply   { minValue = 30; maxValue = 250; value = 170 }
        npUnit.apply {
            minValue = 0; maxValue = unitList.size - 1
            displayedValues = unitList; value = 0
        }

        npUnit.setOnValueChangedListener { _, _, newVal ->
            val isCm = unitList[newVal] == "cm"
            npCm.visibility          = if (isCm) View.VISIBLE else View.GONE
            imperialContainer.visibility = if (isCm) View.GONE else View.VISIBLE
        }

        btnDone.setOnClickListener {
            val unit = unitList[npUnit.value]
            val result = when (unit) {
                "cm" -> "${npCm.value} cm"
                "in" -> "${(npFeet.value * 12) + npInch.value} in"
                else -> "${npFeet.value}' ${npInch.value}\" ft"
            }
            onSelected(result)
            dialog.dismiss()
        }

        dialog.show()
    }
}
