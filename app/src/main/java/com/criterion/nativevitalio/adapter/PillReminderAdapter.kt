import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.UI.customviews.SyncedHorizontalScrollView
import com.criterion.nativevitalio.Utils.MyApplication

import com.google.android.material.R as MaterialR

class PillReminderAdapter(
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
        val context = holder.itemView.context

        // Set alternating row background
        holder.itemView.setBackgroundColor(
            ColorUtils.setAlphaComponent(
                if (position % 2 == 0) Color.GRAY else Color.WHITE,
                16
            )
        )

        // Bind basic data
        holder.tvDrugName.apply {
            text = item.drugName
            isSelected = true // For marquee effect
        }
        holder.tvDoseFrequency.text = item.doseFrequency

        // Setup scroll synchronization
        holder.dynamicIconsLayout.removeAllViews()
        if (!scrollViewsList.contains(holder.rowScrollView)) {
            scrollViewsList.add(holder.rowScrollView)
        }
        holder.rowScrollView.linkedScrollViews = scrollViewsList

        // Create time blocks for each header time
        headerTimes.forEach { time ->
            val timeObj = item.jsonTime.find { it.time == time }
            val timeBlock = createTimeBlock( )
            val iconView = createIconView(  timeObj?.icon)

            iconView.setOnClickListener {
                timeObj?.let {


                    onIconClicked(item, it, iconView)
                }
            }

            timeBlock.addView(iconView)
            holder.dynamicIconsLayout.addView(timeBlock)
        }
    }

    override fun getItemCount(): Int = items.size

    private fun createTimeBlock( ): LinearLayout {

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

    private fun createIconView(  iconState: String?): ImageView {

        val context = MyApplication.appContext
        return ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                16.dpToPx(context),
                16.dpToPx(context)
            )
            setImageDrawable(getTintedIcon(  iconState))
        }
    }

    private fun getNextState(currentState: String?): String {
        return when (currentState?.lowercase()) {
            "taken" -> "missed"
            "missed", "upcoming", "exclamation", null -> "taken"
            else -> "taken"
        }
    }

    private fun getTintedIcon(  icon: String?): Drawable? {

        val context = MyApplication.appContext
        val (iconRes, colorRes) = when (icon?.lowercase()) {
            "taken" -> Pair(
                MaterialR.drawable.ic_mtrl_checked_circle,
                R.color.black
            )
            "missed" -> Pair(
                MaterialR.drawable.mtrl_ic_error,
                R.color.black
            )
            "upcoming" -> Pair(
                MaterialR.drawable.ic_clock_black_24dp,
                R.color.black
            )
            "late" -> Pair(
                MaterialR.drawable.ic_mtrl_checked_circle,
                R.color.black
            )
            "exclamation" -> Pair(
                MaterialR.drawable.mtrl_ic_error,
                R.color.black
            )
            else -> Pair(
                MaterialR.drawable.mtrl_checkbox_button,
                R.color.white
            )
        }

        return AppCompatResources.getDrawable(context, iconRes)?.apply {
            mutate()
            setTint(ContextCompat.getColor(context, colorRes))
        }
    }

    private fun Int.dpToPx(context:Context): Int = (this * context.resources.displayMetrics.density).toInt()
}