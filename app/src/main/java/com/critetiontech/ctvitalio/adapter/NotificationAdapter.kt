package com.critetiontech.ctvitalio.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R

data class NotificationItem(
    val pid: Int,
    val title: String,
    val message: String,
    val uiType: NotificationStatus,
    val sentTime: String
)

enum class NotificationStatus {
    WARNING,   // Orange circle with "!" — e.g. "Time to stretch"
    SUCCESS,   // Green check circle  — e.g. "Proceed as planned"
    ALERT      // Red circle with "!" — e.g. "Heart Rate dropped"
}

class NotificationAdapter(
    private var items: List<NotificationItem>
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {
    fun updateList(newItems: List<NotificationItem>) {
        items = newItems
        notifyDataSetChanged()
    }
    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivStatusIcon: ImageView = itemView.findViewById(R.id.ivStatusIcon)
        val tvTitle: TextView      = itemView.findViewById(R.id.tvTitle)
        val tvTime: TextView       = itemView.findViewById(R.id.tvTime)
        val tvBody: TextView       = itemView.findViewById(R.id.tvBody)



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_list_item, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val item = items[position]

        holder.tvTitle.text = item.title
        holder.tvTime.text  = item.sentTime.toString()
        holder.tvBody.text  = item.message

        // Set icon and tint based on status
        val iconRes = when (item.uiType) {
            NotificationStatus.WARNING -> R.drawable.ic_warning_notification
            NotificationStatus.SUCCESS -> R.drawable.ic_success_notification
            NotificationStatus.ALERT   -> R.drawable.ic_warning_notification
        }

        holder.ivStatusIcon.setImageResource(iconRes)
        holder.ivStatusIcon.imageTintList = null  // clears any existing tint
    }

    override fun getItemCount() = items.size
}