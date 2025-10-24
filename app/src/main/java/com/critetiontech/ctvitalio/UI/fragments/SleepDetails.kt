package com.critetiontech.ctvitalio.UI.fragments

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.GestureDetector
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentMoodBinding
import com.critetiontech.ctvitalio.databinding.FragmentSleepDetailsBinding
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.utils.ToastUtils
import com.critetiontech.ctvitalio.viewmodel.MoodViewModel
data class SleepEntry(val day: Int, val value: Int)

class SleepDetails : Fragment() {
    private lateinit var barsContainer: LinearLayout
    private lateinit var scoreText: TextView
    private lateinit var labelText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sleep_details, container, false)
        barsContainer = view.findViewById(R.id.barsContainer)
        scoreText = view.findViewById(R.id.tvScore)
        labelText = view.findViewById(R.id.tvLabel)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Example dataset
        val data = listOf(
            SleepEntry(12, 69),
            SleepEntry(13, 56),
            SleepEntry(14, 54),
            SleepEntry(15, 62)
        )
        setData(data)
    }

    private fun setData(entries: List<SleepEntry>) {
        barsContainer.removeAllViews()
        if (entries.isEmpty()) return

        val maxValue = entries.maxOf { it.value }
        val avg = entries.map { it.value }.average().toInt()

        scoreText.text = avg.toString()
        labelText.text = "Sleep"

        entries.forEach { entry ->
            val barLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            }

            val barContainer = FrameLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(24.dp, LinearLayout.LayoutParams.MATCH_PARENT)
                background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_bar_track)
            }

            val fillHeightRatio = entry.value / maxValue.toFloat()
            val fillView = View(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    ((150 * fillHeightRatio).toInt()) ,
                    Gravity.BOTTOM
                )
                background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_bar_fill)
            }

            val bubble = TextView(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(32.dp, 32.dp, Gravity.TOP or Gravity.CENTER_HORIZONTAL)
                text = entry.value.toString()
                setTextColor(Color.parseColor("#0A84FF"))
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_circle_value)
            }

            barContainer.addView(fillView)
            barContainer.addView(bubble)

            val dayLabel = TextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = entry.day.toString()
                setTextColor(Color.parseColor("#E0E6F8"))
                textSize = 12f
                gravity = Gravity.CENTER
                setPadding(0, 4.dp, 0, 0)
            }

            barLayout.addView(barContainer)
            barLayout.addView(dayLabel)
            barsContainer.addView(barLayout)
        }
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}