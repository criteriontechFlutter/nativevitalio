package com.critetiontech.ctvitalio.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R

data class WaterRecord(
    val id: Int,      // ml
    val amount: Int,      // ml
    val time: String      // "08:10 AM"
)
class WaterRecordAdapter(
    private val items: MutableList<WaterRecord>,
    private val onDeleteClick: (WaterRecord) -> Unit
) : RecyclerView.Adapter<WaterRecordAdapter.RecordViewHolder>() {

    inner class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtAmount: TextView = itemView.findViewById(R.id.txtAmount)
        val txtUnit: TextView = itemView.findViewById(R.id.txtUnit)
        val txtTime: TextView = itemView.findViewById(R.id.txtTime)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_water_record, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val item = items[position]

        // ⭐ BIND
        holder.txtAmount.text = item.amount.toString()
        holder.txtUnit.text = "ml"
        holder.txtTime.text = item.time

        // ⭐ DELETE
        holder.btnDelete.setOnClickListener {
            onDeleteClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    // ----------------------------
    // ⭐ PUBLIC FUNCTIONS
    // ----------------------------

    fun updateData(newList: List<WaterRecord>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    fun remove(item: WaterRecord) {
        val index = items.indexOf(item)
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun add(item: WaterRecord) {
        items.add(0, item)
        notifyItemInserted(0)
    }

    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }
}