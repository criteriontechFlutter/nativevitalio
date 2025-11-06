package com.critetiontech.ctvitalio.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.PriorityActionCardBinding

class DailyTipAdapter(
    private val tips: List<DailyTip>,
    private val onButtonClick: (DailyTip) -> Unit
) : RecyclerView.Adapter<DailyTipAdapter.TipViewHolder>() {

    inner class TipViewHolder(val binding: PriorityActionCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipViewHolder {
        val binding = PriorityActionCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TipViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TipViewHolder, position: Int) {
        val tip = tips[position]
        holder.binding.apply {
            ivIcon.setImageResource(tip.iconRes)
            tvMessage.text = tip.title
            tvDescription.text = tip.description
            btnStartSession.text = tip.buttonText

            btnStartSession.setOnClickListener {
                onButtonClick(tip)
            }
        }
    }

    override fun getItemCount() = tips.size
}
data class DailyTip(
    val iconRes: Int,
    val title: String,
    val description: String,
    val buttonText: String
)