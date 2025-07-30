import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HorizontalItemSpacing(private val spaceInPx: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = parent.adapter?.itemCount ?: 0

        // Add start padding to the first item and end padding to the last
        outRect.left = if (position == 0) spaceInPx else spaceInPx / 2
        outRect.right = if (position == itemCount - 1) spaceInPx else spaceInPx / 2
    }
}