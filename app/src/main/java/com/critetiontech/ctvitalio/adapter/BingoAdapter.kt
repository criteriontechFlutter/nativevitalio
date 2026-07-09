package com.critetiontech.ctvitalio.adapter

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.databinding.ItemBingoBinding
import com.critetiontech.ctvitalio.model.BingoTask

class BingoAdapter(
    private var tasks: List<BingoTask>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<BingoAdapter.BingoViewHolder>() {

    fun updateTasks(newTasks: List<BingoTask>) {
        this.tasks = newTasks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BingoViewHolder {
        val binding = ItemBingoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BingoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BingoViewHolder, position: Int) {
        holder.bind(tasks[position], position)
    }

    override fun getItemCount(): Int = tasks.size

    inner class BingoViewHolder(private val binding: ItemBingoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: BingoTask, position: Int) {
            binding.tvEmoji.text = task.emoji
            binding.tvTitle.text = task.title

            if (task.completed) {
                binding.cardBingoItem.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#006CFF")))
                binding.tvTitle.setTextColor(Color.WHITE)
                binding.cardBingoItem.cardElevation = binding.root.context.resources.displayMetrics.density * 4
            } else {
                binding.cardBingoItem.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#EAF3FF")))
                binding.tvTitle.setTextColor(Color.parseColor("#1B2430"))
                binding.cardBingoItem.cardElevation = 0f
            }

            binding.root.contentDescription = "${task.title}, ${if (task.completed) "completed" else "not completed"}"

            binding.root.setOnClickListener {
                if (task.id == 5) return@setOnClickListener // Don't trigger for free space

                animateClick(binding.root) {
                    onItemClick(position)
                }
            }
        }

        private fun animateClick(view: View, onEnd: () -> Unit) {
            val scaleXDown = ObjectAnimator.ofFloat(view, "scaleX", 0.9f)
            val scaleYDown = ObjectAnimator.ofFloat(view, "scaleY", 0.9f)
            scaleXDown.duration = 100
            scaleYDown.duration = 100

            val scaleXUp = ObjectAnimator.ofFloat(view, "scaleX", 1.0f)
            val scaleYUp = ObjectAnimator.ofFloat(view, "scaleY", 1.0f)
            scaleXUp.duration = 100
            scaleYUp.duration = 100

            val animatorSet = AnimatorSet()
            animatorSet.playTogether(scaleXDown, scaleYDown)
            animatorSet.addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    val animatorSetUp = AnimatorSet()
                    animatorSetUp.playTogether(scaleXUp, scaleYUp)
                    animatorSetUp.addListener(object : android.animation.AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: android.animation.Animator) {
                            onEnd()
                        }
                    })
                    animatorSetUp.start()
                }
            })
            animatorSet.start()
        }
    }
}
