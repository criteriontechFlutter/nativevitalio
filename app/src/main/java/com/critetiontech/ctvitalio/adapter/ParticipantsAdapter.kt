package com.critetiontech.ctvitalio.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.model.Person


class ParticipantsAdapter(private val items: List<Person>) :
    RecyclerView.Adapter<ParticipantsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.profileImage)
        val countText: TextView = view.findViewById(R.id.extraCountText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_participant, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return when {
            items.size <= 3 -> items.size
            else -> 3 // first 2 images + 1 for +N
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (position < 2 || items.size <= 3) {
            // Show image
            holder.countText.visibility = View.GONE
            holder.profileImage.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(items[position].imageURL)
                .circleCrop()
                .into(holder.profileImage)
        } else {
            // Show "+N"
            holder.profileImage.visibility = View.GONE
            holder.countText.visibility = View.VISIBLE
            val remainingCount = items.size - 2
            holder.countText.text = "$remainingCount+"
        }

        // Adjust margin: first image = no negative margin
        val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.marginStart = if (position == 0) 0 else -12 // adjust as per _12sdp
        holder.itemView.layoutParams = layoutParams
    }
}
