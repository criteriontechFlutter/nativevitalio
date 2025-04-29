package com.criterion.nativevitalio.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.criterion.nativevitalio.databinding.ItemWatchBinding
import com.criterion.nativevitalio.model.WatchModel

class WatchAdapter(
    private val items: List<WatchModel>,
    private val onRemoveClick: (WatchModel) -> Unit
) : RecyclerView.Adapter<WatchAdapter.WatchViewHolder>() {

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
            batteryLevel.text = "91"

            btnRemove.setOnClickListener {
                onRemoveClick(item)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
