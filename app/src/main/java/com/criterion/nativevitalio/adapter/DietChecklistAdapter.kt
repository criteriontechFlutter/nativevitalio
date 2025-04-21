package com.critetiontech.ctvitalio.adapter

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.model.DietItemModel
import com.critetiontech.ctvitalio.model.DietListItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class DietChecklistAdapter(
    private val context: Context,
    private val onDietGiven: ((dietId: String, givenAt: String) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<DietListItem>()

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    fun setData(data: List<DietListItem>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is DietListItem.Header -> TYPE_HEADER
            is DietListItem.Item -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_diet_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_diet_entry, parent, false)
            ItemViewHolder(view)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is DietListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is DietListItem.Item -> (holder as ItemViewHolder).bind(item.diet)
        }
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(header: DietListItem.Header) {
            itemView.findViewById<TextView>(R.id.tvHeader).text = header.title
        }
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFoodName = itemView.findViewById<TextView>(R.id.tvFoodName)
        private val tvQtyUnit = itemView.findViewById<TextView>(R.id.tvQtyUnit)
        private val imgGivenStatus = itemView.findViewById<ImageView>(R.id.imgGivenStatus)

        fun bind(model: DietItemModel) {
            tvFoodName.text = model.foodName.trim()
            tvQtyUnit.text = "${model.foodQty} ${model.unitName}"

            imgGivenStatus.setImageResource(
                if (model.isGiven == 1  )
                    R.drawable.ic_checkbox_checked
                else
                    R.drawable.ic_checkbox_square
            )

            imgGivenStatus.setOnClickListener {
                if (model.isGiven == 2) {
                    val calendar = Calendar.getInstance()
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    val minute = calendar.get(Calendar.MINUTE)
                    Toast.makeText(itemView.context, "Clicked!", Toast.LENGTH_SHORT).show()
                    // ✅ SAFER context source
                    TimePickerDialog(itemView.context, { _, selectedHour, selectedMinute ->
                        calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                        calendar.set(Calendar.MINUTE, selectedMinute)

                        val formattedTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
                            .format(calendar.time)

                        AlertDialog.Builder(itemView.context)
                            .setTitle("Confirm Time")
                            .setMessage("Mark intake at $formattedTime?")
                            .setPositiveButton("Save") { _, _ ->
                                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
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