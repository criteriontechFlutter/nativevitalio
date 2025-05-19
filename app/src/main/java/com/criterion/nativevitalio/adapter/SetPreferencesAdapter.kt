package com.criterion.nativevitalio.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.model.VitalReminder

class SetPreferencesAdapter(
    private val vitalList: List<VitalReminder>,
    private val frequencyOptions: List<String>,
    private val onItemUpdated: (position: Int, frequency: String) -> Unit
) : RecyclerView.Adapter<SetPreferencesAdapter.VitalViewHolder>() {

    inner class VitalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val label: TextView = view.findViewById(R.id.labelVital)
        val dropdown: TextView = view.findViewById(R.id.dropdownVital)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VitalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.set_preference_dropdown, parent, false)
        return VitalViewHolder(view)
    }

    override fun onBindViewHolder(holder: VitalViewHolder, position: Int) {
        val item = vitalList[position]
        holder.label.text = item.name
        holder.dropdown.text = item.frequencyType

        holder.dropdown.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Select Frequency")
                .setItems(frequencyOptions.toTypedArray()) { _, which ->
                    val selected = frequencyOptions[which]
                    holder.dropdown.text = selected
                    onItemUpdated(position, selected)
                }.show()
        }
    }

    override fun getItemCount(): Int = vitalList.size
}