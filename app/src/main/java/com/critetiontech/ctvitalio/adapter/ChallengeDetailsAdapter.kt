

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
import com.critetiontech.ctvitalio.databinding.NewChallengedJoinedBinding
import com.critetiontech.ctvitalio.model.NewChallengeModel
import com.critetiontech.ctvitalio.model.Person
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Challenge
import java.util.Random
class ChallengeDetailsAdapter(private val items: List<Person>) :
        RecyclerView.Adapter<ChallengeDetailsAdapter.ViewHolder>() {

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
                items.size <= 6 -> items.size
                else -> 6 // first 2 images + 1 for +N
            }
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            if (position < 5 || items.size <= 6) {
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
                val remainingCount = items.size - 5
                holder.countText.text = "$remainingCount+"
            }

            // Adjust margin: first image = no negative margin
            val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.marginStart = if (position == 0) 0 else -12 // adjust as per _12sdp
            holder.itemView.layoutParams = layoutParams
        }
    }
