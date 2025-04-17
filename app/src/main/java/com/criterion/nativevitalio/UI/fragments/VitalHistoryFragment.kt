package com.criterion.nativevitalio.UI.fragments

import DateUtils
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.FragmentVitalHistoryBinding


class VitalHistoryFragment : Fragment() {

    private lateinit var binding: FragmentVitalHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVitalHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vitalType = arguments?.getString("vitalType")
        val json = arguments?.getString("itemData")
        val (type, value, timestamp) = json.toString()
            .removeSurrounding("(", ")")
            .split(",")
            .map { it.trim() }

        Log.d("Vital", "Type: $type, Value: $value, Time: $timestamp")


        Log.d("ReceivedVital", "Type: $vitalType,")

        binding.backIcon.setOnClickListener {
            findNavController().popBackStack()
        }
        updateToggleStyles(binding.fluidToggleGroup.checkedButtonId)
        binding.headerTitle.text=vitalType
        binding.tvTitle.text= "$vitalType Log"
        binding.bpText.text=value


        binding.btnChart.setOnClickListener {
            binding.recyclerViewFluidLogs.visibility = View.GONE
            binding.fluidGraph.visibility= View.VISIBLE
            binding.btnChart.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.primaryBlue),
                PorterDuff.Mode.SRC_IN
            )
            binding.btnList.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.gray),
                PorterDuff.Mode.SRC_IN
            )
        }


        binding.btnList.setOnClickListener {
            binding.recyclerViewFluidLogs.visibility = View.VISIBLE
            binding.fluidGraph.visibility= View.GONE


            binding.btnChart.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.gray),
                PorterDuff.Mode.SRC_IN
            )
            binding.btnList.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.primaryBlue),
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

                    }
                    R.id.btnWeekly -> {
                        binding.btnGraphToggleLayout.visibility= View.GONE
                        val (from, to) = DateUtils.getCurrentWeekRange()
                        binding.tvSelectedDate.setText("$from--$to")

                    }
                    R.id.btnMonthly -> {

                        binding.btnGraphToggleLayout.visibility= View.GONE
                        val (from, to) = DateUtils.getCurrentMonthRange()
                        binding.tvSelectedDate.setText("$from--$to")
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