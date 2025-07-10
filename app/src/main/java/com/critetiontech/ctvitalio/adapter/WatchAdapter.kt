package com.critetiontech.ctvitalio.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.databinding.ItemWatchBinding
import com.critetiontech.ctvitalio.model.WatchModel

class WatchAdapter(
    private val onRemoveClick: (WatchModel) -> Unit
) : RecyclerView.Adapter<WatchAdapter.WatchViewHolder>() {

    private val items = mutableListOf<WatchModel>()

    fun setData(newList: List<WatchModel>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    inner class WatchViewHolder(val binding: ItemWatchBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemWatchBinding.inflate(inflater, parent, false)
        return WatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WatchViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            watchName.text = item.brand
            batteryLevel.text = "91" // you may want to fetch real value
            btnRemove.setOnClickListener {
                onRemoveClick(item )
            }
        }
    }

    override fun getItemCount(): Int = items.size
}