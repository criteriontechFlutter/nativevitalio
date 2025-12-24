package com.critetiontech.ctvitalio.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.ItemGoalBinding
import com.critetiontech.ctvitalio.databinding.ItemSavedgoalHeaderBinding
import com.critetiontech.ctvitalio.model.GoalCategoryResponse
import com.critetiontech.ctvitalio.model.GoalItem

class GoalsAdapter(
    private var items: List<Any>,
    private var isAllGoal: Boolean,
    private val onGoalClick: (GoalItem, GoalCategoryResponse?) -> Unit,
    private val onPinClick: (GoalItem, GoalCategoryResponse?) -> Unit,
    private val onThreeDotClick: (GoalItem, GoalCategoryResponse?,anchorView: View) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_GOAL = 1
    }

    /*-------------------------------------------------------------*/
    /* TYPE HANDLING                                                */
    /*-------------------------------------------------------------*/
    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is GoalCategoryResponse -> TYPE_HEADER     // ✔ CHANGED
            is GoalItem -> TYPE_GOAL
            else -> TYPE_GOAL
        }
    }

    override fun getItemCount(): Int = items.size

    /*-------------------------------------------------------------*/
    /* VIEW HOLDER CREATION                                         */
    /*-------------------------------------------------------------*/
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(
                ItemSavedgoalHeaderBinding.inflate(inflater, parent, false)
            )
            else -> GoalViewHolder(
                ItemGoalBinding.inflate(inflater, parent, false),
                isAllGoal,
                onGoalClick,
                onPinClick,
                onThreeDotClick
            )
        }
    }


    /*-------------------------------------------------------------*/
    /* CATEGORY LOOKUP (REAL TYPE)                                 */
    /*-------------------------------------------------------------*/
    private fun getCategoryForPosition(position: Int): GoalCategoryResponse? {
        for (i in position downTo 0) {
            val item = items[i]
            if (item is GoalCategoryResponse) return item
        }
        return null
    }


    /*-------------------------------------------------------------*/
    /* BINDING                                                      */
    /*-------------------------------------------------------------*/
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {

            is GoalCategoryResponse -> {
                (holder as HeaderViewHolder).bind(item)
            }

            is GoalItem -> {
                val cat = getCategoryForPosition(position)
                (holder as GoalViewHolder).bind(item, cat)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<Any>, isAllGoal: Boolean) {
        this.items = newItems
        this.isAllGoal = isAllGoal
        notifyDataSetChanged()
    }

    /*-------------------------------------------------------------*/
    /* VIEW HOLDERS                                                 */
    /*-------------------------------------------------------------*/

    class HeaderViewHolder(
        private val binding: ItemSavedgoalHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: GoalCategoryResponse) {
            binding.tvHeader.text = category.categoryName
        }
    }


    class GoalViewHolder(
        private val binding: ItemGoalBinding,
        private val isAllGoal: Boolean,
        private val onGoalClick: (GoalItem, GoalCategoryResponse?) -> Unit,
        private val onPinClick: (GoalItem, GoalCategoryResponse?) -> Unit,
        private val onThreeDotClick: (GoalItem, GoalCategoryResponse?,anchorView: View) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(goal: GoalItem, category: GoalCategoryResponse?) {

            /* Basic binding */
            binding.isGoal = isAllGoal
            binding.isActive = goal.isActive
            binding.tvTitle.text = goal.goalName

            binding.tvProgress.text = if (isAllGoal) {
                goal.description
            } else {
                "${goal.isActive}/${goal.targetValue}"
            }

            /* Pin color */
            val pinColor = ContextCompat.getColor(
                binding.root.context,
                if (goal.isPinned == 1) R.color.blue else R.color.greyText
            )
            binding.ivPin.setColorFilter(pinColor)

            /* Progress view */
            binding.progressView.setProgress(0f)
            binding.progressView.setProgressColor("#1281FD")
            binding.progressView.setRemainingColor("#D0D0D0")


            /* ----------------------- ITEM CLICK ----------------------- */
            binding.root.setOnClickListener {
                onGoalClick(goal, category)
            }


            binding.ivMenu.setOnClickListener {
                onThreeDotClick(goal, category,binding.ivMenu)
            }


            /* ----------------------- PIN CLICK ------------------------ */
            binding.ivPin.setOnClickListener {

                // Toggle pin state


                // Update pin UI
//                val newColor = ContextCompat.getColor(
//                    binding.root.context,
//                    if (goal.isPinned == 1) R.color.blue else R.color.greyText
//                )
//                binding.ivPin.setColorFilter(newColor)

                // Return updated data
                onPinClick(goal, category)
                //goal.isPinned = if (goal.isPinned == 1) 0 else 1
            }
        }
    }
}
