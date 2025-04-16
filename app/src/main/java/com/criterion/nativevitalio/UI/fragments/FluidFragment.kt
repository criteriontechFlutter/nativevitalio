package com.criterion.nativevitalio.UI.fragments

import FluidAmountBottomSheet
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.adapter.FluidOptionAdapter
import com.criterion.nativevitalio.adapter.GlassSizeAdapter
import com.criterion.nativevitalio.databinding.FragmentFluidBinding
import com.criterion.nativevitalio.viewmodel.FluidIntakeOuputViewModel


class FluidFragment : Fragment() {

    private val viewModel: FluidIntakeOuputViewModel by viewModels()

    private lateinit var binding: FragmentFluidBinding
    private lateinit var adapter: FluidOptionAdapter
    private lateinit var adapterGlassSize: GlassSizeAdapter
    private val labelViews = mutableListOf<TextView>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFluidBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        updateToggleStyles(binding.fluidToggleGroup.checkedButtonId)


        binding.historyBtn.setOnClickListener {
            findNavController().navigate(R.id.action_fluidFragment_to_fluidInputHistoryFragment)
        }

        populateScale()
        setupSeekBar()






        // Listen for changes
        binding.fluidToggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                updateToggleStyles(checkedId)

                when (checkedId) {
                    R.id.btnIntake -> {
                        binding.fluidIntakeLayout.visibility=VISIBLE
                        binding.outPutLayout.visibility=GONE
                        // Handle Fluid Intake click
                    }

                    R.id.btnOutput -> {
                        binding.fluidIntakeLayout.visibility=GONE
                        binding.outPutLayout.visibility=VISIBLE
                        // Handle Fluid Output click
                    }
                }
            }


        }




        viewModel.fetchManualFluidIntake("UHID01235")




        viewModel.selectedVolume.observe(viewLifecycleOwner) { volume ->
            if (volume == 0) {
                FluidAmountBottomSheet { selectedAmount ->
                    viewModel.setSelectedGlassSize(selectedAmount)
                }.show(parentFragmentManager, "FluidAmountBottomSheet")
            }
        }


        binding.fluidRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        // Observe fluid list
        viewModel.intakeList.observe(viewLifecycleOwner) { list ->
            val selectedId = viewModel.selectedFluid.value?.foodID
            adapter = FluidOptionAdapter(list, selectedId) { selectedItem ->
                viewModel.setSelectedFluid(selectedItem)
            }
            binding.fluidRecyclerView.adapter = adapter
        }

        // Observe selection to refresh UI if needed
        viewModel.selectedFluid.observe(viewLifecycleOwner) { selected ->
            adapter = FluidOptionAdapter(viewModel.intakeList.value ?: emptyList(), selected?.foodID) {
                viewModel.setSelectedFluid(it)
            }
            binding.fluidRecyclerView.adapter = adapter
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
                    setPadding(0, 0, 32, 0)
                }

                val colorDot = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(20, 20).apply { rightMargin = 8 }
                    setBackgroundColor(it.color)
                }

                val label = TextView(requireContext()).apply {
                    text = it.name
                    setTextColor(Color.DKGRAY)
                    textSize = 14f
                }

                itemLayout.addView(colorDot)
                itemLayout.addView(label)
                binding.legendLayout.addView(itemLayout)
            }

            loadGlassSizeAdapter()
        }}

    @SuppressLint("SetTextI18n")
    private fun loadGlassSizeAdapter() {

        binding.recyclerViewGlassSize.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }

        viewModel.glassSizeList.observe(viewLifecycleOwner) { sizes ->
            adapterGlassSize = GlassSizeAdapter(sizes) { selected ->
                viewModel.setSelectedGlassSize(selected.volume)
            }
            binding.recyclerViewGlassSize.adapter = adapterGlassSize
        }


        viewModel.selectedVolume.observe(viewLifecycleOwner) { volume ->
            binding.selectedSizeText.text = "Selected: ${volume} ml"
        }
    }



    private fun populateScale() {
        val step = 100
        for (i in 1000 downTo 0 step step) {
            val label = TextView(activity).apply {
                text = "$i ml"
                setTextColor(Color.GRAY)
                textSize = 12f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    0,
                    1f
                )
            }
           labelViews.add(label)
            binding.scaleLabels.addView(label)
        }
    }

    private fun setupSeekBar() {
        binding.fluidSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.fluidLevelLabel.text = "$progress ml"

                // Position label vertically
                val seekBarHeight =   binding.fluidSeekBar.height
                val percent = progress.toFloat() /   binding.fluidSeekBar.max
                val yPos =   binding.fluidSeekBar.top + seekBarHeight * (1 - percent)
                binding.fluidLevelLabel.y = yPos -   binding.fluidLevelLabel.height / 2

                // Animate fill view height
                val fillHeight = (seekBarHeight * percent).toInt()
                val params =   binding.fillView.layoutParams
                params.height = fillHeight
                binding.fillView.layoutParams = params

                // Color scale labels
                for ((index, label) in labelViews.withIndex()) {
                    val labelValue = 1000 - index * 100
                    label.setTextColor(
                        if (progress >= labelValue) Color.parseColor("#FF6F00") else Color.GRAY
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun updateToggleStyles(checkedId: Int) {
        val buttons = listOf(binding.btnIntake, binding.btnOutput)
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

}

