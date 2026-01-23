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
import com.critetiontech.ctvitalio.utils.MyApplication
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

        val tvMonday: TextView = view.findViewById(R.id.tvMonday)
        val tvTuesday: TextView = view.findViewById(R.id.tvTuesday)
        val tvWednesday: TextView = view.findViewById(R.id.tvWednesday)
        val tvThursday: TextView = view.findViewById(R.id.tvThursday)
        val tvFriday: TextView = view.findViewById(R.id.tvFriday)
        val tvFire: TextView = view.findViewById(R.id.tvFireIcon)
        val tvSaturday: TextView = view.findViewById(R.id.tvSaturday)

        val dayViews by lazy {
            listOf(
                tvMonday,
                tvTuesday,
                tvWednesday,
                tvThursday,
                tvFriday,
                tvFire,
                tvSaturday
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.create_challenge_card_slider, parent, false)
        return ChallengeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        val challenge = challenges[position]
        bindStreakDays(
            streakDays = challenge.streakDays,
            dayViews = holder.dayViews,

        )
        holder.titleText.text = challenge.title
        holder.subtitleText.text = "${challenge.duration} days"

       // val progress = challenge.rewardPoints % 100
         holder.progressBar.progress = challenge.progress
//        holder.labelPercent.text = "$progress%"
        holder.labelCurrent.text = "Progress ${challenge.progress} %"

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

    private fun getTodayKey(): String {
        return when (java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> "M"
            java.util.Calendar.TUESDAY -> "T"
            java.util.Calendar.WEDNESDAY -> "W"
            java.util.Calendar.THURSDAY -> "T"
            java.util.Calendar.FRIDAY -> "F"
            java.util.Calendar.SATURDAY -> "S"
            else -> "" // Sunday ignored in your UI
        }
    }
    private fun createDayBg(color: String): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 20f
            setColor(Color.parseColor(color))
        }
    }


    private fun bindStreakDays(
        streakDays: String?,
        dayViews: List<TextView>
    ) {
        // Reset all
        dayViews.forEach {
            it.setTextColor(Color.parseColor("#999999"))
            it.background = null
            it.textSize = 14f
        }

        if (streakDays.isNullOrEmpty()) return

        val activeDays = streakDays.split(",").map { it.trim().uppercase() }
        val todayKey = getTodayKey()

        val dayMap = listOf("M", "T", "W", "T", "F", "🔥", "S")

        // 🔥 Last streak index (fire should represent this)
        val lastStreakIndex = activeDays.size - 1

        dayViews.forEachIndexed { index, textView ->
            val dayKey = dayMap[index]

            // 1️⃣ Highlight streak days
            if (dayKey != "🔥" && activeDays.contains(dayKey)) {
                textView.setTextColor(Color.parseColor("#FF6B35"))
                textView.background = createDayBg("#FFE5D9")

                // 2️⃣ Highlight current day
                if (dayKey == todayKey) {
                    textView.setTextColor(Color.parseColor("#E65100")) // darker
                    textView.textSize = 16f
                }
            }

            // 3️⃣ Fire highlights LAST streak day
            if (dayKey == "🔥" && index == 5 && lastStreakIndex >= 0) {
                val fireView = textView
                fireView.background = createDayBg("#FFE5D9")
                fireView.setTextColor(Color.parseColor("#FF6B35"))
                fireView.textSize = 18f
            }
        }
    }


}
