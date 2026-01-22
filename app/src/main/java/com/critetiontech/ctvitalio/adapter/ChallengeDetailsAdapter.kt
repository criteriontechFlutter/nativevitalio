

package com.critetiontech.ctvitalio.adapter



import PrefsManager
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.ItemParticipantBinding
import com.critetiontech.ctvitalio.databinding.NewChallengedJoinedBinding
import com.critetiontech.ctvitalio.model.NewChallengeModel
import com.critetiontech.ctvitalio.model.Person
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Challenge
import java.util.Random
class ChallengeDetailsAdapter(
    private val items: List<Person>
) : RecyclerView.Adapter<ChallengeDetailsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemParticipantBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemParticipantBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int =
        if (items.size <= 6) items.size else 6

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val b = holder.binding
        val showImage = (position < 5 || items.size <= 6)
        val remaining = items.size - 5
        b.showImage = showImage
        b.showCount = !showImage
        b.person = items.getOrNull(position)
        b.extraCount = "+$remaining"
        // Load image only if visible
        if (showImage) {
            Glide.with(b.root.context)
                .load(items[position].imageURL)
                .circleCrop()
                .into(b.profileImage)
        }
        // overlap margin
        (b.root.layoutParams as ViewGroup.MarginLayoutParams).apply {
            marginStart = if (position == 0) 0 else -12
        }
    }
}

