package com.critetiontech.ctvitalio.UI.fragments

import FluidAmountBottomSheet
import PrefsManager
import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.criterion.nativevitalio.utils.LoaderUtils.hideLoading
import com.criterion.nativevitalio.utils.LoaderUtils.showLoading
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.FluidOptionAdapter
import com.critetiontech.ctvitalio.adapter.GlassSizeAdapter
import com.critetiontech.ctvitalio.databinding.FragmentFluidBinding
import com.critetiontech.ctvitalio.viewmodel.FluidIntakeOuputViewModel


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



    @SuppressLint("ResourceAsColor", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        updateToggleStyles(binding.fluidToggleGroup.checkedButtonId)


        binding.historyBtn.setOnClickListener {
            viewModel.selectedIntakeButton.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) findNavController().navigate(R.id.action_fluidFragment_to_fluidOutputFragment) else findNavController().navigate(R.id.action_fluidFragment_to_fluidInputHistoryFragment)
            }

        }

        binding.backIcon.setOnClickListener {
            findNavController().popBackStack()
        }

//        populateScale()
//        setupSeekBar()


        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) showLoading() else hideLoading()
        }


        val ovalMeterView = binding.ovalMeterView
        val scaleLayout = binding.scaleLayout
        val labelBox = binding.labelBox
        val thumbLabel = binding.thumbLabel
        val thumbLine = binding.thumbLine

        val divisionCount = 10
        val labelHeight = 300.dp / divisionCount
        for (i in 0..divisionCount) {
            val label = TextView(context ).apply {
                text = "${1000 - i * 100} ml"
                setTextColor(Color.GRAY)
                textSize = 12f
                height = labelHeight
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    labelHeight
                )
            }
            scaleLayout.addView(label)
        }


        ovalMeterView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                viewModel.setSelectedOutputProgress(ovalMeterView.getProgressValueInDouble())
                val relativeY = event.y
                val height = ovalMeterView.height
                val newProgress = ((height - relativeY) / height).coerceIn(0f, 1f)

                ovalMeterView.setProgress(newProgress)
                val progressValue = (newProgress * 1000).toInt()
                val y = ovalMeterView.top + height * (1 - newProgress)

                thumbLabel.text = "%.1f".format(progressValue / 100f)
                labelBox.x = ovalMeterView.right + 20f
                labelBox.y = y - labelBox.height / 2

                val lineWidth = ovalMeterView.x - scaleLayout.width.toFloat() - 24f
                thumbLine.layoutParams.width = lineWidth.toInt()
                thumbLine.requestLayout()
                thumbLine.y = y

                for (i in 0 until scaleLayout.childCount) {
                    val label = scaleLayout.getChildAt(i) as TextView
                    val value = 1000 - (i * 100)
                    label.setTextColor(if (value <= progressValue) Color.parseColor("#1564ED") else Color.GRAY)
                }
            }
            true
        }





        // viewModel.selectedVolume.value?.let { binding.glassView.setGlassSize(it) } // Set max to 500ml

        binding.waterGlassView.setOnFillChangedListener { percent, ml ->
            Log.d("TAG", "onViewCreated: "+percent.toString()+"Mlll"+ml.toString())
            viewModel.setSelectedFluidIntakeVolume(ml.toDouble())

        }


        val colorMap = mapOf(
            R.id.colorLightYellow to "#FFFDE7",
            R.id.colorYellow to "#FFF176",
            R.id.colorDarkYellow to "#FFEB3B",
            R.id.colorAmber to "#FFC107",
            R.id.colorBrown to "#A1887F",
            R.id.colorRed to "#D32F2F"
        )
        for ((viewId, colorHex) in colorMap) {
            view.findViewById<View>(viewId).setOnClickListener {
                viewModel.setSelectedColor(colorHex)
                highlightSelectedColor(viewId)
            }
        }


        // Listen for changes
        binding.fluidToggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                updateToggleStyles(checkedId)

                when (checkedId) {
                    R.id.btnIntake -> {
                        viewModel.setSelectedIntakeButton(false)
                        binding.fluidIntakeLayout.visibility=VISIBLE
                        binding.outPutLayout.visibility=GONE
                        binding.btnAddIntake.text="Add Intake"
                        // Handle Fluid Intake click
                    }

                    R.id.btnOutput -> {
                        viewModel.setSelectedIntakeButton(true)
                        binding.fluidIntakeLayout.visibility=GONE
                        binding.outPutLayout.visibility=VISIBLE
                        binding.btnAddIntake.text="Update"
                        // Handle Fluid Output click
                    }
                }
            }


        }




        PrefsManager().getPatient()?.let { viewModel.fetchManualFluidIntake(it.uhID) }




//        viewModel.selectedVolume.observe(viewLifecycleOwner) { volume ->
//            if (volume == 0) {
//                FluidAmountBottomSheet { selectedAmount ->
//                    viewModel.setSelectedGlassSize(selectedAmount)
//
//                }.show(parentFragmentManager, "FluidAmountBottomSheet")
//            }
//        }


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
                if(selected?.foodName.equals("Milk")){
                    binding.milkGlassView.visibility= VISIBLE
                    binding.waterGlassView.visibility= GONE
                    binding.juiceGlassView.visibility= GONE
                    binding.cupglassView.visibility= GONE
                    binding.noFluid.visibility= GONE
                }else if(selected?.foodName.equals("water")){
                    binding.milkGlassView.visibility= GONE
                    binding.waterGlassView.visibility= VISIBLE
                    binding.juiceGlassView.visibility= GONE
                    binding.cupglassView.visibility= GONE
                    binding.noFluid.visibility= GONE
                }
                else if(selected?.foodName.equals("Green Tea")){
                    binding.milkGlassView.visibility= GONE
                    binding.waterGlassView.visibility= GONE
                    binding.juiceGlassView.visibility= GONE
                    binding.cupglassView.visibility= VISIBLE
                    binding.noFluid.visibility= GONE
                }
                else if(selected?.foodName.equals("Coffee")){
                    binding.milkGlassView.visibility= GONE
                    binding.waterGlassView.visibility= GONE
                    binding.juiceGlassView.visibility= GONE
                    binding.cupglassView.visibility= VISIBLE
                    binding.noFluid.visibility= GONE
                }
                else if(selected?.foodName.equals("Fruit Juice")){
                    binding.milkGlassView.visibility= GONE
                    binding.waterGlassView.visibility= GONE
                    binding.juiceGlassView.visibility= VISIBLE
                    binding.cupglassView.visibility= GONE
                    binding.noFluid.visibility= GONE
                }


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

        binding.btnAddIntake.setOnClickListener {

            if(viewModel.selectedIntakeButton.value == true){
                viewModel.insertFluidOutPut()
            }else{
                viewModel.insertFluidIntake()
            }


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
                    gravity=Gravity.CENTER_VERTICAL
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

            loadGlassSizeAdapter()
        }



    }

    private fun highlightSelectedColor(selectedId: Int) {
        val colorViews = listOf(
            R.id.colorLightYellow,
            R.id.colorYellow,
            R.id.colorDarkYellow,
            R.id.colorAmber,
            R.id.colorBrown,
            R.id.colorRed
        )

        for (id in colorViews) {
            val view = requireView().findViewById<View>(id)
            view.alpha = if (id == selectedId) 1f else 0.4f // visual selection
        }
    }

    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    @SuppressLint("SetTextI18n")
    private fun loadGlassSizeAdapter() {

        binding.recyclerViewGlassSize.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }

        viewModel.glassSizeList.observe(viewLifecycleOwner) { sizes ->
            adapterGlassSize = GlassSizeAdapter(sizes) { selected ->
                viewModel.setSelectedGlassSize(selected.volume)
                binding.waterGlassView.setGlassSize(selected.volume)
                binding.cupglassView.setGlassSize(selected.volume)
                binding.milkGlassView.setGlassSize(selected.volume)
                binding.juiceGlassView.setGlassSize(selected.volume)
                if(selected.volume==0 && selected.isSelected){
                    FluidAmountBottomSheet { selectedAmount ->
                        viewModel.setSelectedGlassSize(selectedAmount)

                    }.show(parentFragmentManager, "FluidAmountBottomSheet")
                }
            }
            binding.recyclerViewGlassSize.adapter = adapterGlassSize
        }


        viewModel.selectedVolume.observe(viewLifecycleOwner) { volume ->
            //binding.selectedSizeText.text = "Selected: ${volume} ml"
        }
    }



//    private fun populateScale() {
//        val step = 100
//        for (i in 1000 downTo 0 step step) {
//            val label = TextView(activity).apply {
//                text = "$i ml"
//                setTextColor(Color.GRAY)
//                textSize = 12f
//                layoutParams = LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.WRAP_CONTENT,
//                    0,
//                    1f
//                )
//            }
//           labelViews.add(label)
//            binding.scaleLabels.addView(label)
//        }
//    }
//
//    private fun setupSeekBar() {
//        binding.fluidSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                viewModel.setSelectedOutputProgress(progress)
//                binding.fluidLevelLabel.text = "$progress ml"
//
//                // Position label vertically
//                val seekBarHeight =   binding.fluidSeekBar.height
//                val percent = progress.toFloat() /   binding.fluidSeekBar.max
//                val yPos =   binding.fluidSeekBar.top + seekBarHeight * (1 - percent)
//                binding.fluidLevelLabel.y = yPos -   binding.fluidLevelLabel.height / 2
//
//                // Animate fill view height
//                val fillHeight = (seekBarHeight * percent).toInt()
//                val params =   binding.fillView.layoutParams
//                params.height = fillHeight
//                binding.fillView.layoutParams = params
//
//                // Color scale labels
//                for ((index, label) in labelViews.withIndex()) {
//                    val labelValue = 1000 - index * 100
//                    label.setTextColor(
//                        if (progress >= labelValue) Color.parseColor("#FF6F00") else Color.GRAY
//                    )
//                }
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
//        })
//    }

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

