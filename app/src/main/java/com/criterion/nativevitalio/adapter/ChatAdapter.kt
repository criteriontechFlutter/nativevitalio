import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.ItemMessageBinding
import com.critetiontech.ctvitalio.model.ChatMessage

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = messages[position]
        with(holder.binding) {
            messageText.text = chat.message
            messageTime.text = chat.chatTime

            val params = bubbleLayout.layoutParams as LinearLayout.LayoutParams

            if (chat.isPatient) {
                bubbleLayout.setBackgroundResource(R.drawable.bubble_right)
                params.gravity = Gravity.END
                messageText.setTextColor(Color.BLACK)
            } else {
                bubbleLayout.setBackgroundResource(R.drawable.bubble_left)
                params.gravity = Gravity.START
                messageText.setTextColor(Color.BLACK)
            }
            bubbleLayout.layoutParams = params
        }
    }
}
