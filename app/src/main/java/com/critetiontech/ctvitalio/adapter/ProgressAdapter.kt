import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R

class ProgressAdapter(private var states: List<DotState>) :
    RecyclerView.Adapter<ProgressAdapter.ProgressViewHolder>() {

    fun updateDots(newDotStates: List<DotState>) {
        states = newDotStates
        notifyDataSetChanged()
    }
    enum class DotState {
        FILLED,
        HIGHLIGHTED,
        EMPTY
    }

    inner class ProgressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val circleView: View = itemView.findViewById(R.id.circleView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_progress_circle, parent, false)
        return ProgressViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProgressViewHolder, position: Int) {
        val drawableRes = when (states[position]) {
            DotState.FILLED -> R.drawable.filled_circle_blue
            DotState.HIGHLIGHTED -> R.drawable.highlighted_circle
            DotState.EMPTY -> R.drawable.empty_circle
        }
        holder.circleView.setBackgroundResource(drawableRes)
    }

    override fun getItemCount(): Int = states.size
}
