package com.criterion.nativevitalio.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.criterion.nativevitalio.R

class BloodGroupAdapter(
    private val items: List<String>,
    private var selected: String?,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<BloodGroupAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.blood_group_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = items[position]
        holder.bind(group, group == selected)
        holder.itemView.setOnClickListener {
            selected = group
            notifyDataSetChanged()
            onItemClick(group)
        }
    }

    override fun getItemCount() = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView: TextView = view.findViewById(R.id.tvBlood)

        fun bind(group: String, isSelected: Boolean) {
            textView.text = group
            textView.setTextColor(
                itemView.context.getColor(
                    if (isSelected) R.color.white else R.color.black
                )
            )
            textView.setBackgroundResource(
                if (isSelected) R.drawable.progress_selected else R.drawable.progress_unselected
            )
            itemView.findViewById<View>(R.id.ivCheck)?.visibility =
                if (isSelected) View.VISIBLE else View.GONE
        }
    }
}