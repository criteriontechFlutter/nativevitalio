package com.critetiontech.ctvitalio.UI.fragments

import DateUtils
import PrefsManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.FluidIntakeLogAdapter
import com.critetiontech.ctvitalio.adapter.FluidIntakeRangeAdapter
import com.critetiontech.ctvitalio.databinding.FragmentFluidInputHistoryBinding
import com.critetiontech.ctvitalio.model.FluidPointGraph
import com.critetiontech.ctvitalio.viewmodel.FluidIntakeOuputViewModel

class FluidInputHistoryFragment : Fragment() {

    private lateinit var binding: FragmentFluidInputHistoryBinding
    private lateinit var fluidIntakeAdapter: FluidIntakeLogAdapter
    private lateinit var fluidIntakeAdapterRange: FluidIntakeRangeAdapter
    private lateinit var viewModel: FluidIntakeOuputViewModel




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFluidInputHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backIcon.setOnClickListener {
            findNavController().popBackStack()
        }
        updateToggleStyles(binding.fluidToggleGroup.checkedButtonId)

        viewModel = ViewModelProvider(this)[FluidIntakeOuputViewModel::class.java]
        PrefsManager().getPatient()?.let { viewModel.fetchManualFluidIntake(it.empId) }

        viewModel.intakeList.observe(viewLifecycleOwner) { list ->
            fluidIntakeAdapter = FluidIntakeLogAdapter(list)
            binding.recyclerViewFluidLogs.adapter = fluidIntakeAdapter
        }

        binding.fluidGraph.dataPoints = listOf(
            FluidPointGraph(2f, 200f, "#3DA5F5", "Water"),
            FluidPointGraph(6f, 300f, "#3DA5F5", "Water"),
            FluidPointGraph(8f, 350f, "#FFD700", "Milk"),
            FluidPointGraph(11f, 400f, "#3DA5F5", "Water"),
            FluidPointGraph(14f, 200f, "#FF7F50", "Juice"),
            FluidPointGraph(17f, 200f, "#3DA5F5", "Water"),
            FluidPointGraph(18f, 50f, "#CD853F", "Tea")
        )

        binding.btnList.setOnClickListener {
            binding.recyclerViewFluidLogs.visibility = VISIBLE
            binding.fluidGraph.visibility = GONE

            binding.btnChart.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.gray),
                PorterDuff.Mode.SRC_IN
            )
            binding.btnList.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.primaryBlue),
                PorterDuff.Mode.SRC_IN
            )
        }

        binding.btnChart.setOnClickListener {
            binding.recyclerViewFluidLogs.visibility = GONE
            binding.fluidGraph.visibility = VISIBLE
            binding.btnChart.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.primaryBlue),
                PorterDuff.Mode.SRC_IN
            )
            binding.btnList.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.gray),
                PorterDuff.Mode.SRC_IN
            )
        }


        binding.fluidToggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                updateToggleStyles(checkedId)
                when (checkedId) {
                    R.id.btnDaily -> {
                        binding.progressBarLayout.visibility = VISIBLE
                        binding.btnGraphToggleLayout.visibility = VISIBLE
                        binding.tvSelectedDate.text = "Today"
                        viewModel.intakeList.observe(viewLifecycleOwner) { list ->
                            fluidIntakeAdapter = FluidIntakeLogAdapter(list)
                            binding.recyclerViewFluidLogs.adapter = fluidIntakeAdapter
                        }

                    }

                    R.id.btnWeekly -> {
                        binding.progressBarLayout.visibility = GONE
                        binding.btnGraphToggleLayout.visibility = GONE
                        val (from, to) = DateUtils.getLastWeekRange()
                        binding.tvSelectedDate.text = "$from--$to"
                        viewModel.fetchManualFluidIntakeByRange(
                            PrefsManager().getPatient()!!.uhID,
                            from,
                            to
                        )
                        viewModel.intakeListRangeWise.observe(viewLifecycleOwner) { list ->
                            Log.d("TAG", "onViewCreated: " + list.size.toString())
                            fluidIntakeAdapterRange = FluidIntakeRangeAdapter(list)
                            binding.recyclerViewFluidLogs.adapter = fluidIntakeAdapterRange
                            fluidIntakeAdapterRange.notifyDataSetChanged()
                        }
                        Log.d("Toggle", "Weekly: From $from to $to")
                    }

                    R.id.btnMonthly -> {
                        binding.progressBarLayout.visibility = GONE
                        binding.btnGraphToggleLayout.visibility = GONE
                        val (from, to) = DateUtils.getLastMonthRange()
                        binding.tvSelectedDate.text = "$from--$to"
                        viewModel.fetchManualFluidIntakeByRange(
                            PrefsManager().getPatient()!!.uhID,
                            from,
                            to
                        )
                        viewModel.intakeListRangeWise.observe(viewLifecycleOwner) { list ->
                            Log.d("TAG", "onViewCreated: " + list.size.toString())
                            fluidIntakeAdapterRange = FluidIntakeRangeAdapter(list)
                            binding.recyclerViewFluidLogs.adapter = fluidIntakeAdapterRange
                            fluidIntakeAdapterRange.notifyDataSetChanged()
                        }
                        Log.d("Toggle", "Weekly: From $from to $to")
                    }
                }
            }
        }






        viewModel.recommended.observe(viewLifecycleOwner) { recommended ->
            binding.recommendedText.text = "Recommended Fluid: $recommended ml"
        }

        viewModel.totalIntake.observe(viewLifecycleOwner) { intake ->
            binding.intakeText.text = "Intake: $intake ml"
        }

        viewModel.remaining.observe(viewLifecycleOwner) { remaining ->
            binding.remainingText.text = "Remaining: $remaining ml"
        }

        viewModel.fluidList.observe(viewLifecycleOwner) { list ->
            binding.stackedBar.removeAllViews()
            val total = viewModel.recommended.value ?: 2000

            list.forEach {
                val weight = it.amount.toFloat() / total
                val segment = View(requireContext()).apply {
                    layoutParams =
                        LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, weight)
                    setBackgroundColor(it.color)
                }
                binding.stackedBar.addView(segment)
            }

            binding.legendLayout.removeAllViews()
            list.forEach {
                val itemLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, 0, 32, 0)
                }
                val colorDot = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(20, 20).apply { rightMargin = 8 }

                    // Create circular shape with color
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(it.color) // Set your dynamic color
                    }
                }

                val label = TextView(requireContext()).apply {
                    text = it.name
                    setTextColor(Color.DKGRAY)
                    textSize = 10f
                }

                itemLayout.addView(colorDot)
                itemLayout.addView(label)
                binding.legendLayout.addView(itemLayout)
            }


        }


    }







    private fun updateToggleStyles(checkedId: Int) {
        val buttons = listOf(binding.btnDaily, binding.btnWeekly,binding.btnMonthly)
        for (button in buttons) {
            if (button.id == checkedId) {
                button.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.blue))
                button.setTextColor(Color.WHITE)
            } else {
                button.setBackgroundColor(Color.WHITE)
                button.setTextColor(ContextCompat.getColor(requireActivity(), R.color.gray))
            }
        }
    }

//        binding.fluidChartContent.fluidData = listOf(
//            FluidPoint(2f, 200f, "#3DA5F5"),
//            FluidPoint(6f, 300f, "#3DA5F5"),
//            FluidPoint(8f, 350f, "#FFD700"),
//            FluidPoint(11f, 400f, "#3DA5F5"),
//            FluidPoint(14f, 200f, "#FF7F50"),
//            FluidPoint(17f, 200f, "#3DA5F5"),
//            FluidPoint(18f, 50f, "#CD853F")
//        )


}