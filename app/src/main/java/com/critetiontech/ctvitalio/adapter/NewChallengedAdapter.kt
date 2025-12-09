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
import com.critetiontech.ctvitalio.model.NewChallengeModel
import java.util.Random
class NewChallengedAdapter(
    private val challenges: MutableList<NewChallengeModel>,
    private val onJoinClick: (NewChallengeModel) -> Unit,
    private val onDetailsClick: (NewChallengeModel) -> Unit
) : RecyclerView.Adapter<NewChallengedAdapter.ChallengeViewHolder>() {

    inner class ChallengeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardViewId: CardView = view.findViewById(R.id.cardViewid)
        val iconGoal: ImageView = view.findViewById(R.id.iconGoal)
        val titleText: TextView = view.findViewById(R.id.titleText)
        val subtitleText: TextView = view.findViewById(R.id.subtitleText)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        val labelPercent: TextView = view.findViewById(R.id.labelPercent)
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
        holder.subtitleText.text = "w/Coach Sarah | 7 Day"

        val progress = challenge.rewardPoints % 100
        holder.progressBar.progress = progress
        holder.labelPercent.text = "$progress%"
        holder.labelCurrent.text = "Current"

        holder.reminderText.text =
            "Tiny push needed — one smooth glucose day completes your streak."

        holder.btnLogReading.setOnClickListener { onJoinClick(challenge) }
        holder.cardViewId.setOnClickListener { onDetailsClick(challenge) }
    }

    override fun getItemCount(): Int = challenges.size

    fun updateList(newList: List<NewChallengeModel>) {
        challenges.clear()
        challenges.addAll(newList.toMutableList())  // <-- Fix mismatch
        notifyDataSetChanged()
    }
}
//class NewChallengedAdapter (
//    private val items: List<NewChallengeModel>,
//    private val onItemClick: (NewChallengeModel) -> Unit,
//    private val onItemClick1: (NewChallengeModel) -> Unit
//
//) : RecyclerView.Adapter<NewChallengedAdapter.ViewHolder>() {
//
//    inner class ViewHolder(val binding: NewChallengedJoinedBinding) :
//        RecyclerView.ViewHolder(binding.root)
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val binding = NewChallengedJoinedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return ViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int)
//    {
//        val item = items[position]
//        val binding = holder.binding
//
//        // Generate pastel color for card
//        val pastelColor = getRandomPastelColor()
//        holder.itemView.backgroundTintList = ColorStateList.valueOf(pastelColor)
//
//        // Set pastel color to card
//        binding.textView3.text = item.title
//        binding.textView4.text = item.description
//        binding.scoreText.text = item.rewardPoints.toString()
//        binding.participantsRecyclerView.adapter = ParticipantsAdapter(item.getPeopleJoinedList())
//        binding.startText.text="Starts in "+item.startsIn
//
//        // Generate darker color for the button
//        val darkColor = getDarkerShade(pastelColor)
//
//
//        val shape = GradientDrawable()
//        shape.shape = GradientDrawable.RECTANGLE
//        shape.cornerRadius = 48f
//        shape.setColor(darkColor)
//       // binding.joinNowButton.background = shape
//
//        if(item.getPeopleJoinedList().any { it.empId == PrefsManager().getPatient()?.empId.toString() }){
//            binding.joinNowButton.isEnabled = false
//            binding.joinNowButton.text = "Joined"
//            binding.joinNowButton.setTextColor(Color.WHITE)
//        }
//
//        binding.joinNowButton.setOnClickListener {
//            it.animate()
//                .scaleX(0.95f)
//                .scaleY(0.95f)
//                .setDuration(100)
//                .withEndAction {
//                    it.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
//                }.start()
//            onItemClick(item)
//        }
//
//        holder.itemView.setOnClickListener {
//            onItemClick1(item)
//        }
//
//
//
//
//    }
//
//
//
//
//
//    private fun getRandomPastelColor(): Int {
//        val rnd = Random()
//        val red = 200 + rnd.nextInt(56)  // 200-255
//        val green = 200 + rnd.nextInt(56)
//        val blue = 200 + rnd.nextInt(56)
//        val alpha = 255  // Same opacity as #F5F0F5
//
//        return Color.argb(alpha, red, green, blue)
//    }
//    private fun getDarkerShade(color: Int): Int {
//        val factor = 0.8f // darker by 20%
//        val r = ((Color.red(color) * factor).toInt()).coerceAtMost(255)
//        val g = ((Color.green(color) * factor).toInt()).coerceAtMost(255)
//        val b = ((Color.blue(color) * factor).toInt()).coerceAtMost(255)
//        return Color.rgb(r, g, b)
//    }
//
//
//
//    override fun getItemCount(): Int {
//        return items.size
//    }
//
//}