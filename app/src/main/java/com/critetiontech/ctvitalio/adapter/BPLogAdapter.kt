package com.critetiontech.ctvitalio.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.model.BPLog

data class BPLog(
    val systolic: Int,
    val diastolic: Int,
    val map: Int,
    val time: String,
    val position: String
)
class BPLogAdapter(private var list: List<BPLog>) :
    RecyclerView.Adapter<BPLogAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvBP: TextView = view.findViewById(R.id.tvBP)
        val tvMap: TextView = view.findViewById(R.id.tvMap)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvPosition: TextView = view.findViewById(R.id.tvPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bp_log, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.tvBP.text = "${item.systolic}/${item.diastolic}"
        holder.tvMap.text = "MAP : ${item.map} mmHg"
        holder.tvTime.text = item.time
        holder.tvPosition.text = item.status
    }

    override fun getItemCount() = list.size

  fun updateList(newList: List<BPLog>) {
        list = newList
        notifyDataSetChanged()
    }
}