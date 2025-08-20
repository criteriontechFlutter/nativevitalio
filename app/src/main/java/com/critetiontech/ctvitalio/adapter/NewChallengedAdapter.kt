package com.critetiontech.ctvitalio.adapter



import PrefsManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.databinding.NewChallengedJoinedBinding
import com.critetiontech.ctvitalio.model.NewChallengeModel
import java.util.Random

class NewChallengedAdapter (
    private val items: List<NewChallengeModel>,
    private val onItemClick: (NewChallengeModel) -> Unit,
    private val onItemClick1: (NewChallengeModel) -> Unit

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
        binding.startText.text="Starts in "+item.startsIn

        // Generate darker color for the button
        val darkColor = getDarkerShade(pastelColor)


        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.cornerRadius = 48f
        shape.setColor(darkColor)
        binding.joinNowButton.background = shape

        if(item.getPeopleJoinedList().any { it.empId.toString() == PrefsManager().getPatient()?.empId.toString() }){
            binding.joinNowButton.isEnabled = false
            binding.joinNowButton.text = "Joined"
            binding.joinNowButton.setTextColor(Color.WHITE)
        }

        binding.joinNowButton.setOnClickListener {
            it.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    it.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }.start()
            onItemClick(item)
        }

        holder.itemView.setOnClickListener {
            onItemClick1(item)
        }




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