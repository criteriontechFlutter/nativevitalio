package com.critetiontech.ctvitalio.adapter



import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.databinding.NewChallengedJoinedBinding
import com.critetiontech.ctvitalio.model.NewChallengeModel
import java.util.Random

class NewChallengedAdapter (
    private val items: List<NewChallengeModel>,
) : RecyclerView.Adapter<NewChallengedAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: NewChallengedJoinedBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NewChallengedJoinedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val item = items[position]
        val binding = holder.binding

        // Generate pastel color for card
        val pastelColor = getRandomPastelColor()
        holder.itemView.backgroundTintList = ColorStateList.valueOf(pastelColor)

        // Set pastel color to card
        binding.textView3.text = item.title
        binding.textView4.text = item.description
        binding.scoreText.text = item.rewardPoints.toString()
        binding.participantsRecyclerView.adapter = ParticipantsAdapter(item.getPeopleJoinedList())
        val startsIn = item.startsIn // e.g., "01d:05h:20m"

// Extract day, hour, and minute parts
        val dayPart = startsIn.substringBefore("d").toIntOrNull() ?: 0
        val hourPart = startsIn.substringAfter("d:").substringBefore("h").toIntOrNull() ?: 0
        val minutePart = startsIn.substringAfter("h:").substringBefore("m").toIntOrNull() ?: 0

// Create smart display text
        val displayText = when {
            dayPart > 0 -> "Starts in $dayPart day${if (dayPart > 1) "s" else ""}"
            hourPart > 0 -> "Starts in $hourPart hour${if (hourPart > 1) "s" else ""}"
            minutePart > 0 -> "Starts in $minutePart minute${if (minutePart > 1) "s" else ""}"
            else -> "Already Started!"
        }

// Set text to view
        binding.startText.text = displayText


//        object : CountDownTimer(timeLeft, 1000) {
//            override fun onTick(millisUntilFinished: Long) {
//                val seconds = millisUntilFinished / 1000 % 60
//                val minutes = millisUntilFinished / (1000 * 60) % 60
//                val hours = millisUntilFinished / (1000 * 60 * 60) % 24
//                val days = millisUntilFinished / (1000 * 60 * 60 * 24)
//
//                val timeStr = String.format("%02dd:%02dh:%02dm:%02ds", days, hours, minutes, seconds)
//                holder.binding.startText.text = "Starts in $timeStr"
//            }
//
//            override fun onFinish() {
//                holder.binding.startText.text = "Started"
//            }
//        }.start()
        // Generate darker color for the button
        val darkColor = getDarkerShade(pastelColor)


        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.cornerRadius = 48f
        shape.setColor(darkColor)
        binding.joinNowButton.background = shape

        binding.joinNowButton.setOnClickListener {
            it.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    it.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }.start()
        }






    }

    fun parseDuration(duration: String): Long {
        val regex = Regex("""(\d+)d:(\d+)h:(\d+)m""")
        val match = regex.find(duration) ?: return 0L

        val (days, hours, minutes) = match.destructured
        val totalMillis =
            days.toLong() * 24 * 60 * 60 * 1000 +
                    hours.toLong() * 60 * 60 * 1000 +
                    minutes.toLong() * 60 * 1000

        return totalMillis
    }


    private fun getRandomPastelColor(): Int {
        val rnd = Random()
        val red = 200 + rnd.nextInt(56)  // 200-255
        val green = 200 + rnd.nextInt(56)
        val blue = 200 + rnd.nextInt(56)
        val alpha = 255  // Same opacity as #F5F0F5

        return Color.argb(alpha, red, green, blue)
    }
    private fun getDarkerShade(color: Int): Int {
        val factor = 0.8f // darker by 20%
        val r = ((Color.red(color) * factor).toInt()).coerceAtMost(255)
        val g = ((Color.green(color) * factor).toInt()).coerceAtMost(255)
        val b = ((Color.blue(color) * factor).toInt()).coerceAtMost(255)
        return Color.rgb(r, g, b)
    }



    override fun getItemCount(): Int {
        return items.size
    }

}