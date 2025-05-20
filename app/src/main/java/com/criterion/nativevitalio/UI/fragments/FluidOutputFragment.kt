package com.critetiontech.ctvitalio.UI.fragments

import DateUtils
import PrefsManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.adapter.FluidOutputHistoryAdapter
import com.critetiontech.ctvitalio.adapter.FluidOutputRangeAdapter
import com.critetiontech.ctvitalio.utils.LoaderUtils.hideLoading
import com.critetiontech.ctvitalio.utils.LoaderUtils.showLoading
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentFluidOutputBinding
import com.critetiontech.ctvitalio.viewmodel.FluidIntakeOuputViewModel


class FluidOutputFragment : Fragment() {

    private lateinit var binding: FragmentFluidOutputBinding
    private lateinit var viewModel: FluidIntakeOuputViewModel
    private lateinit var fluidOutputAdapter: FluidOutputHistoryAdapter
    private lateinit var fluidOutputRangeAdapter: FluidOutputRangeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentFluidOutputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.backIcon.setOnClickListener {
            findNavController().popBackStack()
        }
        updateToggleStyles(binding.fluidToggleGroup.checkedButtonId)


        viewModel = ViewModelProvider(this)[FluidIntakeOuputViewModel::class.java]
        PrefsManager().getPatient()?.let { viewModel.fetchFluidOutputDaily(it.uhID) }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) showLoading() else hideLoading()
        }

        viewModel.outputList.observe(viewLifecycleOwner) { list ->
            binding.fluidGraph.post {
                binding.fluidGraph.setData(list)
            }
            fluidOutputAdapter = FluidOutputHistoryAdapter(list)
            binding.recyclerViewFluidLogs.adapter = fluidOutputAdapter
        }
        binding.btnList.setOnClickListener {
            binding.recyclerViewFluidLogs.visibility = View.VISIBLE
            binding.fluidGraph.visibility= View.GONE
            binding.graphscroll.visibility= View.GONE
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
            binding.recyclerViewFluidLogs.visibility = View.GONE
            binding.fluidGraph.visibility= View.VISIBLE
            binding.graphscroll.visibility= View.VISIBLE
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

                        binding.btnGraphToggleLayout.visibility= View.VISIBLE
                        binding.tvSelectedDate.setText("Today")
                        viewModel.outputList.observe(viewLifecycleOwner) { list ->
                            fluidOutputAdapter = FluidOutputHistoryAdapter(list)
                            binding.recyclerViewFluidLogs.adapter = fluidOutputAdapter
                        }

                    }
                    R.id.btnWeekly -> {

                        binding.btnGraphToggleLayout.visibility= View.GONE
                        val (from, to) = DateUtils.getLastWeekRange()
                        binding.tvSelectedDate.setText("$from--$to")
                        viewModel.fetchManualFluidOutPutByRange(PrefsManager().getPatient()!!.uhID,from,to)
                        viewModel.outputListRangeWise.observe(viewLifecycleOwner) { list ->
                            Log.d("TAG", "onViewCreated: "+list.size.toString())
                            fluidOutputRangeAdapter = FluidOutputRangeAdapter(list)
                            binding.recyclerViewFluidLogs.adapter = fluidOutputRangeAdapter
                            fluidOutputRangeAdapter.notifyDataSetChanged()
                        }
                        Log.d("Toggle", "Weekly: From $from to $to")
                    }
                    R.id.btnMonthly -> {

                        binding.btnGraphToggleLayout.visibility= View.GONE
                        val (from, to) = DateUtils.getLastMonthRange()
                        binding.tvSelectedDate.setText("$from--$to")
                        viewModel.fetchManualFluidOutPutByRange(PrefsManager().getPatient()!!.uhID,from,to)
                       viewModel.outputListRangeWise.observe(viewLifecycleOwner) { list ->
                            Log.d("TAG", "onViewCreated: "+list.size.toString())
                            fluidOutputRangeAdapter = FluidOutputRangeAdapter(list)
                            binding.recyclerViewFluidLogs.adapter = fluidOutputRangeAdapter
                            fluidOutputRangeAdapter.notifyDataSetChanged()
                        }
                        Log.d("Toggle", "Weekly: From $from to $to")
                    }
                }
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


}