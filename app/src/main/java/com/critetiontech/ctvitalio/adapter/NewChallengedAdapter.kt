package com.critetiontech.ctvitalio.adapter



import PrefsManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.NewChallengedJoinedBinding
import com.critetiontech.ctvitalio.model.DashboardActiveChallenges
import com.critetiontech.ctvitalio.model.NewChallengeModel
import java.util.Random
class NewChallengedAdapter(
    private val challenges: MutableList<DashboardActiveChallenges>,
    private val onJoinClick: (DashboardActiveChallenges) -> Unit,
    private val onDetailsClick: (DashboardActiveChallenges) -> Unit
) : RecyclerView.Adapter<NewChallengedAdapter.ChallengeViewHolder>() {

    inner class ChallengeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardViewId: CardView = view.findViewById(R.id.cardViewid)
        val iconGoal: ImageView = view.findViewById(R.id.iconGoal)
        val titleText: TextView = view.findViewById(R.id.titleText)
        val subtitleText: TextView = view.findViewById(R.id.subtitleText)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
//        val labelPercent: TextView = view.findViewById(R.id.labelPercent)
        val labelCurrent: TextView = view.findViewById(R.id.labelCurrent)
        val reminderText: TextView = view.findViewById(R.id.reminderText)
        val btnLogReading: Button = view.findViewById(R.id.btn_log_reading)
        val btnMessageCoach: Button = view.findViewById(R.id.btn_message_coach)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.create_challenge_card_slider, parent, false)
        return ChallengeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        val challenge = challenges[position]

        holder.titleText.text = challenge.title
        holder.subtitleText.text = "${challenge.duration} days"

       // val progress = challenge.rewardPoints % 100
         holder.progressBar.progress = 86
//        holder.labelPercent.text = "$progress%"
        holder.labelCurrent.text = "Progress 86 %"

        holder.reminderText.text =
            "Tiny push needed — one smooth glucose day completes your streak."

        holder.btnLogReading.setOnClickListener { onJoinClick(challenge) }
        holder.cardViewId.setOnClickListener { onDetailsClick(challenge) }
    }

    override fun getItemCount(): Int = challenges.size

    fun updateList(newList: List<DashboardActiveChallenges>) {
        challenges.clear()
        challenges.addAll(newList.toMutableList())  // <-- Fix mismatch
        notifyDataSetChanged()
    }
}
