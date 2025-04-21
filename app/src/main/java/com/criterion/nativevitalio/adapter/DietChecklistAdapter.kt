package com.critetiontech.ctvitalio.adapter

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.model.DietItemModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Combined model for row: food + header visibility
data class DietListItem(
    val diet: DietItemModel,
    val timeSlot: String,
    val showHeader: Boolean
)

class DietChecklistAdapter(
    private val context: Context,
    private val onDietGiven: ((dietId: String, givenAt: String) -> Unit)? = null
) : RecyclerView.Adapter<DietChecklistAdapter.ItemViewHolder>() {

    private val items = mutableListOf<DietListItem>()

    fun setData(data: List<DietListItem>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_diet_entry, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFoodName: TextView = itemView.findViewById(R.id.tvFoodName)
        private val tvQtyUnit: TextView = itemView.findViewById(R.id.tvQtyUnit)
        private val tvHeader: TextView = itemView.findViewById(R.id.tvHeader)
        private val imgGivenStatus: ImageView = itemView.findViewById(R.id.imgGivenStatus)

        fun bind(item: DietListItem) {
            val model = item.diet

            tvFoodName.text = model.foodName.trim()
            tvQtyUnit.text = "${model.foodQty} ${model.unitName}"

            if (item.showHeader) {
                tvHeader.visibility = View.VISIBLE
                tvHeader.text = item.timeSlot
            } else {
                tvHeader.visibility = View.INVISIBLE
            }

            imgGivenStatus.setImageResource(
                if (model.isGiven == 1)
                    R.drawable.ic_checkbox_checked
                else
                    R.drawable.ic_checkbox_square
            )

            imgGivenStatus.setOnClickListener {
                if (model.isGiven == 2 || model.isGiven == 0) {
                    val calendar = Calendar.getInstance()
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    val minute = calendar.get(Calendar.MINUTE)

                    TimePickerDialog(itemView.context, { _, selectedHour, selectedMinute ->
                        calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                        calendar.set(Calendar.MINUTE, selectedMinute)

                        val formattedTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
                            .format(calendar.time)

                        AlertDialog.Builder(itemView.context)
                            .setTitle("Confirm Time")
                            .setMessage("Mark intake at $formattedTime?")
                            .setPositiveButton("Save") { _, _ ->
                                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    .format(Date())
                                val fullDateTime = "$date $formattedTime"
                                onDietGiven?.invoke(model.dietId.toString(), fullDateTime)
                            }
                            .setNegativeButton("Cancel", null)
                            .show()

                    }, hour, minute, false).show()
                }
            }
        }
    }
}
