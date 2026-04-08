package com.critetiontech.ctvitalio.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.browser.R
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.critetiontech.ctvitalio.databinding.ItemUpcomingBinding
import com.critetiontech.ctvitalio.model.PendingChallenge

class UpcomingAdapter(
    private val list: List<PendingChallenge>,
    private val onJoinClick: (PendingChallenge) -> Unit
) : RecyclerView.Adapter<UpcomingAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemUpcomingBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUpcomingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list[position]
        val context = holder.itemView.context

        // title
        holder.binding.tvTitle.text = item.title

        // duration
        holder.binding.tvDuration.text = "w/ Care Team | ${item.duration} days"

        // people joined
        val count = item.peopleJoined.size
        holder.binding.tvPeople.text =
            if (count == 0) "Be first to join"
            else "$count+ joined"

        holder.binding.tvDays.text = "Upcoming"

        // image
        if (item.peopleJoined.isNotEmpty()) {

            val fixedUrl = item.peopleJoined[0].imageURL.replace("\\", "/")

            Glide.with(context)
                .load(fixedUrl)
                .into(holder.binding.imgIcon)
        }

        // join click
        holder.binding.btnJoin.setOnClickListener {
            onJoinClick(item)
        }
    }
}