package com.critetiontech.ctvitalio.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.PriorityActionCardBinding

class DailyTipAdapter(
    private var tips: List<PriorityAction>,
    private val onButtonClick: (PriorityAction) -> Unit
) : RecyclerView.Adapter<DailyTipAdapter.TipViewHolder>() {

    inner class TipViewHolder(val binding: PriorityActionCardBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipViewHolder {
        val binding = PriorityActionCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TipViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TipViewHolder, position: Int) {
        val tip = tips[position]

        holder.binding.apply {
            tvMessage.text = tip.title
            tvDescription.text = tip.message
            btnStartSession.text = tip.actionId  // Button label

            btnStartSession.setOnClickListener {
                onButtonClick(tip)
            }
        }
    }

    override fun getItemCount(): Int = tips.size

    fun updateData(newList: List<PriorityAction>) {
        tips = newList
        notifyDataSetChanged()
    }
}

data class PriorityAction(
    val actionId: String,
    val title: String,
    val message: String
)
data class PriorityActionWrapper(
    val actions: String   // JSON string containing an array of PriorityAction
)