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

    fun getCurrentWeekRange(): Pair<String, String> {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val start = calendar.time

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        val end = calendar.time

        return Pair(sdf.format(start), sdf.format(end))
    }

    fun getCurrentMonthRange(): Pair<String, String> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val start = calendar.time

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val end = calendar.time

        return Pair(sdf.format(start), sdf.format(end))
    }


}
