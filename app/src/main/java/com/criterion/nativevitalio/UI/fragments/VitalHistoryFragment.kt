package com.criterion.nativevitalio.UI.fragments

import DateUtils
import PrefsManager
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.adapter.BPReadingAdapter
import com.criterion.nativevitalio.databinding.FragmentVitalHistoryBinding
import com.criterion.nativevitalio.utils.LoaderUtils.hideLoading
import com.criterion.nativevitalio.utils.LoaderUtils.showLoading
import com.criterion.nativevitalio.viewmodel.VitalHistoryViewModel
import java.text.SimpleDateFormat
import java.util.Locale


class VitalHistoryFragment : Fragment() {

    private lateinit var binding: FragmentVitalHistoryBinding
    private val viewModel: VitalHistoryViewModel by viewModels()
    private lateinit var bloodPressureAdapter: BPReadingAdapter
    private lateinit var vitalId: String
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


if(vitalType=="Blood Pressure"){
    vitalId="6,4"
    }else{
    vitalId="5"
   }

        PrefsManager().getPatient()?.let { viewModel.getBloodPressureRangeHistory(it.uhID,DateUtils.getTodayDate(),DateUtils.getTodayDate(),vitalId) }


        viewModel.bpList.observe(viewLifecycleOwner) { list ->
            bloodPressureAdapter = BPReadingAdapter(list.reversed())
            binding.recyclerViewFluidLogs.adapter = bloodPressureAdapter
        }

        binding.backIcon.setOnClickListener {
            findNavController().popBackStack()
        }
        updateToggleStyles(binding.fluidToggleGroup.checkedButtonId)
        binding.headerTitle.text=vitalType
        binding.tvTitle.text= "$vitalType Log"
        binding.bpText.text=value
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) showLoading() else hideLoading()
        }


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
                        binding.heartImg.visibility= View.VISIBLE
                        PrefsManager().getPatient()?.let {
                            viewModel.getBloodPressureRangeHistory(it.uhID,DateUtils.getTodayDate(),DateUtils.getTodayDate(),vitalId) }


                    }
                    R.id.btnWeekly -> {
                        binding.btnGraphToggleLayout.visibility= View.GONE
                        val (from, to) = DateUtils.getLastWeekRange()
                        binding.tvSelectedDate.setText("${formatDateString(from)}--${formatDateString(to)}")
                        binding.heartImg.visibility= View.GONE
                        PrefsManager().getPatient()?.let { viewModel.getBloodPressureRangeHistory(it.uhID,from,to,vitalId) }

                    }
                    R.id.btnMonthly -> {

                        binding.btnGraphToggleLayout.visibility= View.GONE
                        val (from, to) = DateUtils.getLastMonthRange()
                        binding.heartImg.visibility= View.GONE
                        binding.tvSelectedDate.setText("${formatDateString(from)}--${formatDateString(to)}")
                        PrefsManager().getPatient()?.let { viewModel.getBloodPressureRangeHistory(it.uhID,from,to,vitalId) }
                    }
                }
            }
        }

    }

    private fun formatDateString(inputDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val date = inputFormat.parse(inputDate)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            "-"
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