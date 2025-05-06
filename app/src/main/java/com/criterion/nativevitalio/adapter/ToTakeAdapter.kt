package com.criterion.nativevitalio.adapter

import PillReminderModel
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.utils.MyApplication

class ToTakeAdapter(
    private var items: MutableList<PillReminderModel>,
//    private val onCheckChanged: (PillReminderModel, Boolean) -> Unit,
    private val onItemClick: (PillReminderModel) -> Unit
) : RecyclerView.Adapter<ToTakeAdapter.ToTakeViewHolder>() {

    inner class ToTakeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val medName: TextView = itemView.findViewById(R.id.medName)
        val quantity: TextView = itemView.findViewById(R.id.quantityBadge)
        val instruction: TextView = itemView.findViewById(R.id.medInstruction)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
//        val menuIcon: ImageView = itemView.findViewById(R.id.menuIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToTakeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.medication_card, parent, false)
        return ToTakeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToTakeViewHolder, position: Int) {
        val item = items[position]

        holder.medName.text = item.drugName
        holder.quantity.text = "${item.jsonTime.size} ${item.dosageForm.lowercase()}"
        holder.instruction.text = item.remark.ifBlank { "No instruction" }

        holder.checkBox.setOnCheckedChangeListener(null)
//        holder.checkBox.isChecked = false

        // ✅ Set tinted icon for checkbox
        val sampleIconType = item.jsonTime.firstOrNull()?.icon
        holder.checkBox.buttonDrawable = getTintedIcon(sampleIconType)

//        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
//            onCheckChanged(item, isChecked)
//        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<PillReminderModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    private fun getTintedIcon(icon: String?): Drawable? {
        val context = MyApplication.appContext
        val (iconRes, colorRes) = when (icon?.lowercase()) {
            "taken" -> Pair(com.google.android.material.R.drawable.ic_mtrl_checked_circle, R.color.primaryBlue)
            "missed" -> Pair(com.google.android.material.R.drawable.mtrl_ic_error, R.color.black)
            "upcoming" -> Pair(com.google.android.material.R.drawable.ic_clock_black_24dp, R.color.darkYellow)
            "late" -> Pair(com.google.android.material.R.drawable.ic_mtrl_checked_circle, R.color.primaryBlue)
            "check" -> Pair(com.google.android.material.R.drawable.ic_mtrl_checked_circle, R.color.primaryBlue)
            "exclamation" -> Pair(com.google.android.material.R.drawable.mtrl_ic_error, R.color.red)
            else -> Pair(com.google.android.material.R.drawable.abc_list_pressed_holo_dark, R.color.white)
        }

        return AppCompatResources.getDrawable(context, iconRes)?.apply {
            mutate()
            setTint(ContextCompat.getColor(context, colorRes))
        }
    }

}