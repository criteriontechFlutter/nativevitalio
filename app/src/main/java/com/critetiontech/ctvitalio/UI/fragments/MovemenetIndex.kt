package com.critetiontech.ctvitalio.UI.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentMovemenetIndexBinding
import com.critetiontech.ctvitalio.model.MovementIndexViewModel
import java.util.Calendar


class MovemenetIndex : Fragment() {

    private var _binding: FragmentMovemenetIndexBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MovementIndexViewModel
    private val weekOrder = mapOf(
        "Sunday" to 0,
        "Monday" to 1,
        "Tuesday" to 2,
        "Wednesday" to 3,
        "Thursday" to 4,
        "Friday" to 5,
        "Saturday" to 6
    )
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovemenetIndexBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[MovementIndexViewModel::class.java]

        viewModel.wellnessMetrics.observe(viewLifecycleOwner) { response ->

            val entries = response.movementIndexWeek.map {
                MovementEntrys(
                    date = it.date,
                    dayName = it.dayName,
                    value = it.vmValue.toInt()
                )
            }

            val sortedEntries = entries.sortedBy { weekOrder[it.dayName] ?: 0 }

            setData(sortedEntries)



            binding.totalSleepIds.title.text="Steps"
            binding.totalSleepIds.value.text=" "
            binding.totalSleepIds.status.text=""



            val movementIndexVital = response.vitals.firstOrNull {
                it.vitalName.equals("MovementIndex", ignoreCase = true)
            }
            binding.inactiveTimeId.title.text="Inactive Time"
            binding.inactiveTimeId.value.text= movementIndexVital?.vmValue?.toSafeIntFromDecimal().toString()
            binding.inactiveTimeId.status.text=  ""


            val activeHours = response.vitals.firstOrNull {
                it.vitalName.equals("ActiveHours", ignoreCase = true)
            }
            binding.activeHrId.title.text="Active Hours  "
            binding.activeHrId.value.text= activeHours?.vmValue?.toSafeIntFromDecimal().toString()
            binding.activeHrId.status.text=""


            val activeMinutes = response.vitals.firstOrNull {
                it.vitalName.equals("ActiveMinutes", ignoreCase = true)
            }
            binding.activeMinId.title.text="Active Mins"
            binding.activeMinId.value.text= activeMinutes?.vmValue?.toSafeIntFromDecimal().toString()
            binding.activeMinId.status.text=""


        }


        // Sample data (last 7 days)


  binding.wellnessImageArrow.setOnClickListener {

            findNavController().popBackStack()
        }
    }

    fun String?.toSafeIntFromDecimal(default: Int = 0): Int {
        return this?.toDoubleOrNull()?.toInt() ?: default
    }
    fun renderHourlyStressChart(
        context: Context,
        barContainer: LinearLayout,
        hourlyStress: List<Int>
    ) {
        barContainer.removeAllViews()
        if (hourlyStress.isEmpty()) return

        val maxValue = hourlyStress.maxOrNull() ?: 1
        val maxHeightPx = barContainer.height.takeIf { it > 0 }
            ?: (50 * context.resources.displayMetrics.density).toInt()

        hourlyStress.forEach { value ->
            val barHeight =
                ((value.toFloat() / maxValue) * maxHeightPx)
                    .toInt()
                    .coerceAtLeast(2)

            val barView = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    (6 * context.resources.displayMetrics.density).toInt(),
                    barHeight
                ).apply {
                    marginEnd = (3 * context.resources.displayMetrics.density).toInt()
                }
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#546788")) // stress color
                    cornerRadius = 3f
                }
            }

            barContainer.addView(barView)
        }




    }

    // --------------------------------------------------
    // Chart Builder
    // --------------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun setData(entries: List<MovementEntrys>) {

        binding.barsContainer.removeAllViews()
        if (entries.isEmpty()) return

        val maxValue = entries.maxOf { it.value }
        val avg = entries.map { it.value }.average().toInt()

        binding.tvScore.text = avg.toString()
        binding.tvLabel.text = "Movement Index"

        binding.barsContainer.post {

            val containerHeight = binding.barsContainer.height
            val maxBarHeight = containerHeight - 60.dp

            entries.forEachIndexed { _, entry ->

                val fillRatio = entry.value.toFloat() / maxValue.toFloat()
                val fillHeight = (maxBarHeight * fillRatio).toInt()

                val barLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1f
                    )
                }

                val spacer = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1f
                    )
                }

                val barContainer = FrameLayout(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        24.dp,
                        fillHeight.coerceAtLeast(30.dp)
                    )
                }

                val trackView = View(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        24.dp,
                        fillHeight,
                        Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    )
                    background = GradientDrawable().apply {
                        setColor(Color.parseColor("#40FFFFFF"))
                        cornerRadius = 12.dp.toFloat()
                    }
                }

                val fillView = View(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        4.dp,
                        fillHeight,
                        Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    )
                    background = GradientDrawable().apply {
                        setColor(Color.WHITE)
                        cornerRadius = 2.dp.toFloat()
                    }
                }

                val bubble = TextView(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        24.dp,
                        24.dp,
                        Gravity.TOP or Gravity.CENTER_HORIZONTAL
                    )
                    text = entry.value.toString()
                    setTextColor(Color.parseColor("#0A84FF"))
                    textSize = 11f
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    background = GradientDrawable().apply {
                        setColor(Color.WHITE)
                        shape = GradientDrawable.OVAL
                    }
                    elevation = 2.dp.toFloat()
                }

                // --- weekday initial from API (S,M,T,W,T,F,S)
                val weekdayInitial = entry.dayName.first().toString()

                val weekdayBubble = TextView(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        20.dp,
                        20.dp,
                        Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    )
                    text = weekdayInitial
                    setTextColor(Color.parseColor("#0A84FF"))
                    textSize = 10f
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    background = GradientDrawable().apply {
                        setColor(Color.WHITE)
                        shape = GradientDrawable.OVAL
                    }
                    elevation = 1.dp.toFloat()
                }

                // --- convert YYYY-MM-DD into dd/MM
                val parts = entry.date.split("-")
                val dayLabel = TextView(requireContext()).apply {
                    text = "${parts[2]}/${parts[1]}"
                    setTextColor(Color.parseColor("#80FFFFFF"))
                    textSize = 10f
                    gravity = Gravity.CENTER
                }

                val baseLine = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2.dp
                    ).apply {
                        topMargin = 6.dp
                        bottomMargin = 6.dp
                    }
                    setBackgroundColor(Color.parseColor("#40FFFFFF"))
                }

                barContainer.addView(trackView)
                barContainer.addView(fillView)
                barContainer.addView(bubble)
                barContainer.addView(weekdayBubble)

                barLayout.addView(spacer)
                barLayout.addView(barContainer)
                barLayout.addView(baseLine)
                barLayout.addView(dayLabel)

                binding.barsContainer.addView(barLayout)
            }
        }
    }

    data class MovementEntrys(
        val date: String,       // "2026-01-25"
        val dayName: String,    // "Sunday"
        val value: Int          // vmValue
    )
val Int.dp: Int
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()
}