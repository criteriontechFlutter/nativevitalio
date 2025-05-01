
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.NumberPicker
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.adapter.BottomSheetListAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    // Convert date from one format to another
    fun formatDate(input: String, inputFormat: String, outputFormat: String): String {
        return try {
            val parser = SimpleDateFormat(inputFormat, Locale.getDefault())
            val formatter = SimpleDateFormat(outputFormat, Locale.getDefault())
            val date = parser.parse(input)
            date?.let { formatter.format(it) } ?: input
        } catch (e: Exception) {
            input // fallback if parsing fails
        }
    }

    // Convert Date to String
    fun format(date: Date, format: String): String {
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(date)
    }

    // Convert timestamp to date string
    fun fromTimestamp(timestamp: Long, format: String = "dd MMM yyyy, hh:mm a"): String {
        return format(Date(timestamp), format)
    }

    // Get today in desired format
    fun today(format: String = "dd MMM yyyy"): String {
        return format(Date(), format)
    }

    // Convert to "Today", "Yesterday", etc.
    fun getRelativeDay(date: Date): String {
        val cal = Calendar.getInstance()
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }

        cal.time = date

        return when {
            cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Today"

            cal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                    cal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "Yesterday"

            else -> format(date, "dd MMM yyyy")
        }
    }



    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

//    fun getLastWeekRange(): Pair<String, String> {
//        val calendar = Calendar.getInstance()
//        calendar.firstDayOfWeek = Calendar.MONDAY
//
//        // Move back 7 days to last week
//        calendar.add(Calendar.DAY_OF_YEAR, -7)
//
//        // Set to last week's Monday
//        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
//        val start = calendar.time
//
//        // Set to last week's Sunday
//        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
//        val end = calendar.time
//
//        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//        return Pair(sdf.format(start), sdf.format(end))
//    }


    fun getLastWeekRange(): Pair<String, String> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val toCalendar = Calendar.getInstance()
        val toDate = sdf.format(toCalendar.time)

        val fromCalendar = Calendar.getInstance()
        fromCalendar.add(Calendar.DAY_OF_YEAR, -7)
        val fromDate = sdf.format(fromCalendar.time)

        return Pair(fromDate, toDate)
    }

    fun getLastMonthRange(): Pair<String, String> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val toCalendar = Calendar.getInstance()
        val toDate = sdf.format(toCalendar.time)

        val fromCalendar = Calendar.getInstance()
        fromCalendar.add(Calendar.DAY_OF_YEAR, -31)
        val fromDate = sdf.format(fromCalendar.time)

        return Pair(fromDate, toDate)
    }


//    fun getLastMonthRange(): Pair<String, String> {
//        val calendar = Calendar.getInstance()
//        calendar.add(Calendar.MONTH, -1) // Move to last month
//
//        calendar.set(Calendar.DAY_OF_MONTH, 1)
//        val start = calendar.time
//
//        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
//        val end = calendar.time
//
//        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//        return Pair(sdf.format(start), sdf.format(end))
//    }


    fun getTodayDate(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Date())
    }

    fun getTodayDateTime(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(Date())
    }

    fun toCamelCase(input: String): String {
        return input.split(" ").joinToString(" ") {
            it.lowercase().replaceFirstChar(Char::uppercaseChar)
        }
    }


    fun showListBottomSheet(context: Context, title: String, list: List<String>, onSelected: (String) -> Unit) {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_list, null)
        dialog.setContentView(view)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = BottomSheetListAdapter(list) {
            onSelected(it)
            dialog.dismiss()
        }

        dialog.show()
    }



    fun showHeightPicker(context: Context, onSelected: (String) -> Unit) {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.height_picker_bottom_sheet, null)
        dialog.setContentView(view)

        val npFeet = view.findViewById<NumberPicker>(R.id.npFeet)
        val npInch = view.findViewById<NumberPicker>(R.id.npInch)
        val npCm = view.findViewById<NumberPicker>(R.id.npCm)
        val npUnit = view.findViewById<NumberPicker>(R.id.npUnit)
        val imperialContainer = view.findViewById<LinearLayout>(R.id.containerImperial)
        val btnDone = view.findViewById<Button>(R.id.btnDone)

        val unitList = arrayOf("ft", "in", "cm")

        npFeet.minValue = 3
        npFeet.maxValue = 8
        npFeet.value = 5

        npInch.minValue = 0
        npInch.maxValue = 11
        npInch.value = 7

        npCm.minValue = 30
        npCm.maxValue = 250
        npCm.value = 170

        npUnit.minValue = 0
        npUnit.maxValue = unitList.size - 1
        npUnit.displayedValues = unitList
        npUnit.value = 0 // ft default

        npUnit.setOnValueChangedListener { _, _, newVal ->
            when (unitList[newVal]) {
                "cm" -> {
                    npCm.visibility = View.VISIBLE
                    imperialContainer.visibility = View.GONE
                }
                else -> {
                    npCm.visibility = View.GONE
                    imperialContainer.visibility = View.VISIBLE
                }
            }
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
