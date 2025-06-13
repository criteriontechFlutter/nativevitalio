

import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.utils.SyncedHorizontalScrollView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.google.android.material.R as MaterialR

class PillReminderAdapter(
    private val fragment: Fragment,
    private val items: List<PillReminderModel>,
    private val headerScrollView: SyncedHorizontalScrollView,
    private val scrollViewsList: MutableList<SyncedHorizontalScrollView>,
    private val onIconClicked: (pill: PillReminderModel, timeObj: PillTime, iconView: ImageView) -> Unit
) : RecyclerView.Adapter<PillReminderAdapter.PillViewHolder>() {

    private var headerTimes: List<String> = emptyList()

    fun setHeaderTimes(times: List<String>) {
        headerTimes = times
        notifyDataSetChanged()
    }

    inner class PillViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDrugName: TextView = view.findViewById(R.id.tvDrugName)
        val tvDoseFrequency: TextView = view.findViewById(R.id.tvDoseFrequency)
        val rowScrollView: SyncedHorizontalScrollView = view.findViewById(R.id.rowScrollView)
        val dynamicIconsLayout: LinearLayout = view.findViewById(R.id.dynamicIconsLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PillViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pill_row_metoprolol, parent, false)
        return PillViewHolder(view)
    }

    override fun onBindViewHolder(holder: PillViewHolder, position: Int) {
        val item = items[position]

        holder.itemView.setBackgroundColor(
            ColorUtils.setAlphaComponent(
                if (position % 2 == 0) Color.GRAY else Color.WHITE,
                16
            )
        )

        holder.tvDrugName.apply {
            text = item.drugName
            isSelected = true
        }
        holder.tvDoseFrequency.text = item.doseFrequency

        holder.dynamicIconsLayout.removeAllViews()
        if (!scrollViewsList.contains(holder.rowScrollView)) {
            scrollViewsList.add(holder.rowScrollView)
        }
        holder.rowScrollView.linkedScrollViews = scrollViewsList

        headerTimes.forEach { time ->
            val timeObj = item.jsonTime.find { it.time == time }
            val timeBlock = createTimeBlock()
            val iconView = createIconView(timeObj?.icon)

            val icon = timeObj?.icon?.lowercase() ?: ""
            if (icon !in listOf("taken", "late", "check")) {
                iconView.setOnClickListener {
                    timeObj?.let { tObj ->
                        val now = Calendar.getInstance()
                        TimePickerDialog(
                            fragment.requireContext(),
                            { _, hourOfDay, minute ->
                                val cal = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                                    set(Calendar.MINUTE, minute)
                                }

                                val selectedTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)

                                AlertDialog.Builder(fragment.requireContext())
                                    .setTitle("Confirm Intake")
                                    .setMessage("Mark as taken at $selectedTime?")
                                    .setPositiveButton("Yes") { _, _ ->
                                        tObj.time = selectedTime
                                        iconView.setImageDrawable(getTintedIcon(tObj.icon))
                                        onIconClicked(item, tObj, iconView)
                                    }
                                    .setNegativeButton("Cancel", null)
                                    .show()
                            },
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            false
                        ).show()
                    }
                }
            }

            timeBlock.addView(iconView)
            holder.dynamicIconsLayout.addView(timeBlock)
        }
    }

    override fun getItemCount(): Int = items.size

    private fun createTimeBlock(): LinearLayout {
        val context = MyApplication.appContext
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                100.dpToPx(context),
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun createIconView(iconState: String?): ImageView {
        val context = MyApplication.appContext
        return ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                20.dpToPx(context),
                20.dpToPx(context)
            )
            setImageDrawable(getTintedIcon(iconState))
        }
    }

    private fun getTintedIcon(icon: String?): Drawable? {
        val context = MyApplication.appContext
        val (iconRes, colorRes) = when (icon?.lowercase()) {
            "taken" -> Pair(R.drawable.pills_check, null)
            "missed" -> Pair(MaterialR.drawable.mtrl_ic_error, R.color.red)
            "upcoming" -> Pair(R.drawable.late_dose,null)
            "late" -> Pair(R.drawable.pills_check, null)
            "check" -> Pair(R.drawable.pills_check, null)
            "exclamation" ->  Pair(R.drawable.late_dose,null)
            else -> Pair(R.drawable.no_dose, R.color.white)
        }

        return AppCompatResources.getDrawable(context, iconRes)?.apply {
            mutate()
            colorRes?.let { setTint(ContextCompat.getColor(context, it)) }
        }
    }

    private fun Int.dpToPx(context: Context): Int =
        (this * context.resources.displayMetrics.density).toInt()
}

