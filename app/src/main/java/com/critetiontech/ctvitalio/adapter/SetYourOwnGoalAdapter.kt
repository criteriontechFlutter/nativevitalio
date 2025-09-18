package com.critetiontech.ctvitalio.adapter

import android.graphics.Color
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
        private val checkMark: ImageView = itemView.findViewById(R.id.checkMark)

        fun bind(goal: SetGoalModel) {
            iconImage.setImageResource(goal.icon)
            labelText.text = goal.name

            if (goal.isSelected) {
                iconImage.setBackgroundResource(R.drawable.circle_primary_background) // solid primary
                iconImage.setColorFilter(Color.WHITE) // icon white
                labelText.setTextColor(Color.BLACK)
                checkMark.visibility = View.VISIBLE
            } else {
                iconImage.setBackgroundResource(R.drawable.circle_light_blue_background) // default bg
                iconImage.setColorFilter(Color.parseColor("#739BE3")) // default icon tint
                labelText.setTextColor(itemView.context.getColor(R.color.setgoaltext))
                checkMark.visibility = View.GONE
            }

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