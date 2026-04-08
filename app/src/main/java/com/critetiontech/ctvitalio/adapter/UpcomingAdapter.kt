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
    private val list: List<PendingChallenge>
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

        // ✅ Title
        holder.binding.tvTitle.text = item.title

        // ✅ Description (remove HTML)
//        holder.binding.tvDesc.text = android.text.Html
//            .fromHtml(item.description, android.text.Html.FROM_HTML_MODE_LEGACY)
//            .toString()

        // ✅ Duration
        holder.binding.tvDuration.text = "w/ Care Team | ${item.duration} days"

        // ✅ People Joined
        val count = item.peopleJoined.size
        holder.binding.tvPeople.text =
            if (count == 0) "Be first to join"
            else "$count+ joined"

        // ✅ Days (static for now)
        holder.binding.tvDays.text = "Upcoming"

        // ✅ Load Image (Fix broken URL issue)
        if (item.peopleJoined.isNotEmpty()) {

            val fixedUrl = item.peopleJoined[0].imageURL.replace("\\", "/")

            Glide.with(context)
                .load(fixedUrl)
//                .placeholder(android.R.drawable.sym_def_app_icon)
//                .error(android.R.drawable.c)
                .into(holder.binding.imgIcon)

        } else {
//            holder.binding.imgIcon.setImageResource(R.drawable.chaalle)
        }

        // ✅ Join Button Click
        holder.binding.btnJoin.setOnClickListener {
            Toast.makeText(context, "Join ${item.title}", Toast.LENGTH_SHORT).show()
        }
    }
}