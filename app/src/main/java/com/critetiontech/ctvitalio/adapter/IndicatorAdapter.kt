package com.critetiontech.ctvitalio.adapter


import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R

class IndicatorAdapter(private val count: Int) :
    RecyclerView.Adapter<IndicatorAdapter.IndicatorViewHolder>() {

    private var selectedPosition = 0

    inner class IndicatorViewHolder(val imageView: ImageView) :
        RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IndicatorViewHolder {
        val imageView = ImageView(parent.context).apply {
            val size = (parent.context.resources.displayMetrics.density * 8).toInt()
            val margin = (parent.context.resources.displayMetrics.density * 2).toInt()
            layoutParams = ViewGroup.MarginLayoutParams(size, size).apply {
                setMargins(margin, margin, margin, margin)
            }
        }
        return IndicatorViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: IndicatorViewHolder, position: Int) {
        val drawableRes =
            if (position == selectedPosition) R.drawable.dot_active else R.drawable.dot_inactive


        holder.imageView.setImageDrawable(
            ContextCompat.getDrawable(holder.imageView.context, drawableRes)
        )

    }

    override fun getItemCount() = count

    fun updateSelectedPosition(newPosition: Int) {
        val previousPosition = selectedPosition
        selectedPosition = newPosition
        notifyItemChanged(previousPosition)
        notifyItemChanged(newPosition)
    }
}