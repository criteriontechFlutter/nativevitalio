package com.critetiontech.ctvitalio.UI.fragments

import PillReminderAdapter
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentPillsReminderBinding
import com.critetiontech.ctvitalio.utils.SyncedHorizontalScrollView
import com.critetiontech.ctvitalio.viewmodel.PillsReminderViewModal
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PillsReminder : Fragment() {
    private lateinit var binding: FragmentPillsReminderBinding
    private lateinit var viewModel: PillsReminderViewModal
    private lateinit var adapter: PillReminderAdapter
    private val LEFT_COLUMN_WIDTH_DP = 200

    private val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val filterFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var selectedFilterDate: String = ""
    private var isTimelineView = false
    private val syncedScrollViews = mutableListOf<SyncedHorizontalScrollView>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPillsReminderBinding.inflate(inflater, container, false)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        syncedScrollViews.clear()
        syncedScrollViews.add(binding.headerScrollView)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[PillsReminderViewModal::class.java]

        setupBackButton()
        setupDatePicker()

        binding.seeTimeline.setOnClickListener {
            isTimelineView = !isTimelineView
            if (isTimelineView) {
                binding.headerScrollView.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
                binding.timelineScrollView.visibility = View.VISIBLE
                binding.seeTimeline.text = "See Checklist"
                showTimelineChecklist()
            } else {
                binding.headerScrollView.visibility = View.VISIBLE
                binding.timelineScrollView.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                binding.seeTimeline.text = "See Timeline"
                filterPillsByDate(selectedFilterDate)
            }
        }

        viewModel.pillList.observe(viewLifecycleOwner) {
            if (isTimelineView) showTimelineChecklist() else filterPillsByDate(selectedFilterDate)
        }

        viewModel.getAllPatientMedication()
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupDatePicker() {
        val today = Date()
        selectedFilterDate = filterFormat.format(today)
        binding.dateText.text = displayFormat.format(today)

        binding.datePickerField.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selected = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }.time
                    selectedFilterDate = filterFormat.format(selected)
                    binding.dateText.text = displayFormat.format(selected)
                    if (isTimelineView) showTimelineChecklist() else filterPillsByDate(selectedFilterDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun filterPillsByDate(selectedDate: String) {
        val list = viewModel.pillList.value ?: return
        val filteredList = list.filter { it.date == selectedDate }

        if (filteredList.isEmpty()) {
            binding.recyclerView.adapter = null
            binding.dynamicHeaderLayout.removeAllViews()
            Toast.makeText(requireContext(), "No pills scheduled for $selectedDate", Toast.LENGTH_SHORT).show()
            return
        }

        val sortedTimes = filteredList.flatMap { it.jsonTime.map { it.time } }.toSet().sorted()
        val syncedScrollViews = mutableListOf(binding.headerScrollView) // shared list
        adapter = PillReminderAdapter(
            fragment = this,
            items = filteredList,
            headerScrollView = binding.headerScrollView,
            scrollViewsList = syncedScrollViews
        ) { pill, timeObj, iconView ->
                    viewModel.insertPatientMedication(
                        pill.pmId.toString(),
                        pill.prescriptionRowID.toString(),
                        timeObj.durationType.toString(),
                        timeObj.time.toString()
                    )
        }

        binding.recyclerView.adapter = adapter
        adapter.setHeaderTimes(sortedTimes)
        buildDynamicHeader(sortedTimes)
    }

    private fun buildDynamicHeader(sortedTimes: List<String>) {
        val headerLayout = binding.dynamicHeaderLayout
        headerLayout.removeAllViews()

        val medicineBlock = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LEFT_COLUMN_WIDTH_DP.dpToPx(),
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val medicineTitle = TextView(requireContext()).apply {
            text = "MEDICINE"
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.START
            setTextColor(Color.BLACK)
        }

        medicineBlock.addView(medicineTitle)
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

            timeBlock.addView(timeText)
            headerLayout.addView(timeBlock)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showTimelineChecklist() {
        val list = viewModel.pillList.value ?: return
        val todayList = list.filter { it.date == selectedFilterDate }

        if (todayList.isEmpty()) {
            Toast.makeText(requireContext(), "No data for $selectedFilterDate", Toast.LENGTH_SHORT).show()
            return
        }

        binding.timelineContainer.removeAllViews()

        val grouped = todayList.flatMap { pill ->
            pill.jsonTime.map { timeObj -> Triple(timeObj.time, pill, timeObj) }
        }.groupBy { it.first }.toSortedMap()

        val now = LocalTime.now()

        val pastelColors = listOf(
            "#E0F7FA", "#F3E5F5", "#E1F5FE", "#FFEBEE",
            "#FFFDE7", "#F9FBE7", "#E8F5E9", "#ECEFF1"
        )

        grouped.forEach { (time, items) ->
            val rowView = LayoutInflater.from(requireContext()).inflate(R.layout.item_time_pill_row, binding.timelineContainer, false)
            val tvTime = rowView.findViewById<TextView>(R.id.tvTime)
            val cardContainer = rowView.findViewById<LinearLayout>(R.id.medicineContainer)

            // Highlight upcoming/current time
            val itemTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("hh:mm a"))
            if (!itemTime.isBefore(now)) {
                tvTime.setBackgroundResource(R.drawable.bg_time_highlight)
                tvTime.setTextColor(Color.WHITE)
            }

            tvTime.text = time

            items.forEach { (_, pill, timeObj) ->
                val card = LayoutInflater.from(requireContext()).inflate(R.layout.item_medicine_card, cardContainer, false)

                card.findViewById<TextView>(R.id.tvDrugName).text = pill.drugName
                card.findViewById<TextView>(R.id.tvInstruction).text = timeObj.instruction ?: "after meal"
                card.findViewById<TextView>(R.id.tvDose).text = "${timeObj.dose ?: 1} ${timeObj.instruction ?: "unit"}"

                // Set random pastel background
                val color = Color.parseColor(pastelColors.random())
                card.backgroundTintList = ColorStateList.valueOf(color)

                val imgIcon = card.findViewById<ImageView>(R.id.imgIcon)
                val iconValue = "pill"
                val iconRes = when {
                    iconValue.contains("syrup") -> R.drawable.pill
                    iconValue.contains("capsule") -> R.drawable.pill
                    else -> R.drawable.pill
                }
                imgIcon.setImageResource(iconRes)

                val actionIcon = card.findViewById<ImageView>(R.id.imgAction)
                val isTaken = timeObj.icon?.lowercase() in listOf("taken", "check", "late")


                val cardColor = (card.backgroundTintList as? ColorDrawable)?.color ?: Color.WHITE
                val darkerColor = ColorUtils.blendARGB(cardColor, Color.BLACK, 0.2f)

                actionIcon.setImageResource(R.drawable.ic_checkbox_square)

                if (isTaken) {
                    actionIcon.setImageResource(R.drawable.checkbox)

                    actionIcon.setColorFilter(darkerColor, PorterDuff.Mode.SRC_IN)
                    actionIcon.isEnabled = false
                } else {
                    actionIcon.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN)
                    actionIcon.setOnClickListener {
                        val cal = Calendar.getInstance()
                        TimePickerDialog(
                            requireContext(),
                            { _, hourOfDay, minute ->
                                val selectedTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
                                    .format(Calendar.getInstance().apply {
                                        set(Calendar.HOUR_OF_DAY, hourOfDay)
                                        set(Calendar.MINUTE, minute)
                                    }.time)

                                AlertDialog.Builder(requireContext())
                                    .setTitle("Confirm Intake")
                                    .setMessage("Mark as taken at $selectedTime?")
                                    .setPositiveButton("Yes") { _, _ ->
                                        viewModel.insertPatientMedication(
                                            pill.pmId.toString(),
                                            pill.prescriptionRowID.toString(),
                                            timeObj.durationType.toString(),
                                            selectedTime
                                        )
                                        showTimelineChecklist()
                                    }
                                    .setNegativeButton("Cancel", null)
                                    .show()
                            },
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE),
                            false
                        ).show()
                    }
                }

//                actionIcon.setImageResource(
//                    if (isTaken) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_square
//                )
//                actionIcon.isEnabled = !isTaken



                cardContainer.addView(card)
            }

            binding.timelineContainer.addView(rowView)
        }
    }

    private fun Int.dpToPx(): Int {
        val density = Resources.getSystem().displayMetrics.density
        return (this * density).toInt()
    }
}
