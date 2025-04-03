package com.criterion.nativevitalio.UI.fragments

import PillReminderAdapter
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.UI.customviews.SyncedHorizontalScrollView
import com.criterion.nativevitalio.databinding.FragmentPillsReminderBinding
import com.criterion.nativevitalio.viewmodel.PillsReminderViewModal

class PillsReminder : Fragment() {

    private lateinit var binding: FragmentPillsReminderBinding
    private lateinit var viewModel: PillsReminderViewModal
    private lateinit var adapter: PillReminderAdapter
    private val LEFT_COLUMN_WIDTH_DP = 200

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPillsReminderBinding.inflate(inflater, container, false)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[PillsReminderViewModal::class.java]

        val allScrollViews = mutableListOf<SyncedHorizontalScrollView>()
        allScrollViews.add(binding.headerScrollView)


        binding.backButton.setOnClickListener(){
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        viewModel.pillList.observe(viewLifecycleOwner) { list ->
            val sortedTimes = list
                .flatMap { it.jsonTime.map { timeObj -> timeObj.time } }
                .toSet()
                .sorted()

//            adapter = PillReminderAdapter(list, binding.headerScrollView, allScrollViews,
//                (){
//
//                })
            adapter = PillReminderAdapter(
                list,
                binding.headerScrollView,
                allScrollViews
            ) { pill, timeObj, iconView ->

                val newState = when (timeObj.icon?.lowercase()) {
                    "taken" -> "missed"
                    "missed", "upcoming", "exclamation", null -> "taken"
                    else -> "taken"
                }
//
//                timeObj.icon = newState
//                iconView.setImageResource(getIconRes(newState))

                // 💉 Call API via ViewModel
                viewModel.insertPatientMedication(
                      pill.pmId.toString(),
                   pill.prescriptionRowID.toString(),
                    timeObj.durationType.toString(),
                 timeObj.time.toString(),
                )
            }
            binding.recyclerView.adapter = adapter

            adapter.setHeaderTimes(sortedTimes)
            binding.headerScrollView.linkedScrollViews = allScrollViews

            buildDynamicHeader(sortedTimes)
        }

        viewModel.getAllPatientMedication()
    }
    private fun getIconRes(icon: String?): Int {
        return when (icon?.lowercase()) {
            "taken" -> com.google.android.material.R.drawable.ic_mtrl_checked_circle
            "missed" -> com.google.android.material.R.drawable.mtrl_ic_error
            "upcoming" -> com.google.android.material.R.drawable.ic_clock_black_24dp
            "exclamation" -> com.google.android.material.R.drawable.ic_clock_black_24dp
            else -> R.drawable.ic_launcher_foreground
        }
    }
    private fun buildDynamicHeader(sortedTimes: List<String>) {
        val headerLayout = binding.dynamicHeaderLayout
        headerLayout.removeAllViews()

        val medicineBlock = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(LEFT_COLUMN_WIDTH_DP.dpToPx(), LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        val medicineTitle = TextView(requireContext()).apply {
            text = "MEDICINE"
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setTextColor(Color.BLACK)
        }

//        val medicineSubText = TextView(requireContext()).apply {
//            text = "Drug Name"
//            textSize = 12f
//            gravity = Gravity.CENTER
//            setTextColor(Color.DKGRAY)
//        }

        medicineBlock.addView(medicineTitle)
//        medicineBlock.addView(medicineSubText)
        headerLayout.addView(medicineBlock)

        sortedTimes.forEach { time ->
            val timeBlock = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(100.dpToPx(), LinearLayout.LayoutParams.WRAP_CONTENT)
            }

            val timeText = TextView(requireContext()).apply {
                text = time
                textSize = 12f
                gravity = Gravity.CENTER
                setTextColor(Color.DKGRAY)
            }

//            val icon = ImageView(requireContext()).apply {
//                layoutParams = LinearLayout.LayoutParams(32.dpToPx(), 32.dpToPx())
//                setImageResource(R.drawable.fluidmanagement)
//            }

            timeBlock.addView(timeText)
//            timeBlock.addView(icon)
            headerLayout.addView(timeBlock)
        }
    }

    fun Int.dpToPx(): Int {
        val density = Resources.getSystem().displayMetrics.density
        return (this * density).toInt()
    }
}