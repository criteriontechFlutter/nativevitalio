package com.critetiontech.ctvitalio.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.ItemGlassSizeBinding
import com.critetiontech.ctvitalio.model.GlassSize

class GlassSizeAdapter(
    private val items: List<GlassSize>,
    private val onSelected: (GlassSize) -> Unit
) : RecyclerView.Adapter<GlassSizeAdapter.GlassViewHolder>() {

    inner class GlassViewHolder(val binding: ItemGlassSizeBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GlassViewHolder {
        val binding = ItemGlassSizeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GlassViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onBindViewHolder(holder: GlassViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            label.visibility= if (item.volume==0) View.GONE else View.VISIBLE
            label.text = "${item.volume} ml"
            icon.visibility = if (item.volume==0) View.VISIBLE else View.GONE
            label.setTextColor(
                ContextCompat.getColor(root.context,
                if (item.isSelected) R.color.white else R.color.gray
            ))
            container.setBackgroundResource(
                if (item.isSelected || position==0) R.drawable.bg_glass_selected else R.drawable.bg_glass_unselected
            )

            container.setOnClickListener {
                items.forEach { it.isSelected = false }
                item.isSelected = true
                notifyDataSetChanged()
                onSelected(item)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}