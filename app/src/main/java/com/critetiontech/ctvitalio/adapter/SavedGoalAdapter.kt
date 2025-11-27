package com.critetiontech.ctvitalio.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.ItemGoalBinding
import com.critetiontech.ctvitalio.databinding.ItemSavedgoalHeaderBinding
import com.critetiontech.ctvitalio.model.GoalItem

class GoalsAdapter(private var items: List<Any>,private var isAllGoal: Boolean) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_GOAL = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is String -> TYPE_HEADER
            is GoalItem -> TYPE_GOAL
            else -> TYPE_GOAL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {

            TYPE_HEADER -> {
                val binding = ItemSavedgoalHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                HeaderViewHolder(binding)
            }

            else -> {
                val binding = ItemGoalBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                GoalViewHolder(binding,isAllGoal)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<Any>, isAllGoal: Boolean) {
        items = newItems
        this@GoalsAdapter.isAllGoal = isAllGoal
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (val item = items[position]) {
            is String -> (holder as HeaderViewHolder).bind(item)
            is GoalItem -> (holder as GoalViewHolder).bind(item)
        }
    }

    // ----------------- VIEW HOLDERS -----------------

    class HeaderViewHolder(private val binding: ItemSavedgoalHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(categoryName: String) {
            binding.tvHeader.text = categoryName
        }
    }

    class GoalViewHolder(private val binding: ItemGoalBinding,private val isAllGoal: Boolean) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(goal: GoalItem) {

            binding.tvTitle.text = goal.goalName
           // binding.tvProgress.text = "${goal.isActive}/${goal.targetValue}"
            binding.tvProgress.text =
                if (isAllGoal) goal.description else "${goal.isActive}/${goal.targetValue}"


            val color = ContextCompat.getColor(
                binding.root.context,
                if (goal.isPinned == 1) R.color.blue else R.color.greyText
            )
            binding.progressView.setProgress(65f)
            binding.progressView.setProgressColor("#1281FD")
            binding.progressView.setRemainingColor("#D0D0D0") // grey


            binding.ivPin.setColorFilter(color)
        }
    }
}
