package com.critetiontech.ctvitalio.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.model.SetGoalModel


class SetYourOwnGoalAdapter(private val items: List<SetGoalModel>,
                            private val onItemClick: (SetGoalModel) -> Unit // click handler
     ) :
    RecyclerView.Adapter<SetYourOwnGoalAdapter.SetGoalViewHolder>() {

    inner class SetGoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconImage: ImageView = itemView.findViewById(R.id.iconImage)
        private val labelText: TextView = itemView.findViewById(R.id.labelText)

        fun bind(goal: SetGoalModel) {
            iconImage.setImageResource(goal.iconRes)
            labelText.text = goal.label
            itemView.setOnClickListener {
                onItemClick(goal)
            }
        }
    }
     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetGoalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_health_metric, parent, false)
        return SetGoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: SetGoalViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}