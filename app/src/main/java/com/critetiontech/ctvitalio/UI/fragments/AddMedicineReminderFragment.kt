package com.example.vitalio_pragya.fragment

import Medicine
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentAddMedicineReminderBinding
import com.critetiontech.ctvitalio.databinding.ItemMedicineBinding
import com.critetiontech.ctvitalio.databinding.MedicineReminderScheduleDialogueBinding
import com.critetiontech.ctvitalio.viewmodel.AddMedicineReminderViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.*
class AddMedicineReminderFragment : Fragment() {

    private var _binding: FragmentAddMedicineReminderBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddMedicineReminderViewModel by viewModels()

    private val Min_Days = 1
    private val Max_Days = 30
    private val maxSlots = 5

    private var isUpdating = false
    private lateinit var daysList: List<TextView>
    private var selectedDay: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddMedicineReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeMedicineList()
        /** ADD TIME SLOT event **/
        binding.btnAddSlot.setOnClickListener {
            addTimeSlotField()
        }

        /** SPECIFIC DATES UI CLICK **/
        binding.containerSpecificDates.setOnClickListener {
openSpecificDatePicker() }
        binding.ivCalendar.setOnClickListener { openSpecificDatePicker() }
        binding.etSpecificDates.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener { openSpecificDatePicker() }
        }
    }

    // 🕒 Add Time Slot System
    private fun addTimeSlotField() {

        if (viewModel.timeSlots.size >= maxSlots) {
            Toast.makeText(requireContext(), "Max 5 time slots allowed", Toast.LENGTH_SHORT).show()
            return
        }

        val index = viewModel.timeSlots.size
        viewModel.addTimeSlot("08:00")

        val newSlot = layoutInflater.inflate(R.layout.item_time_slot, binding.slotContainer, false)
        val tvTime = newSlot.findViewById<TextView>(R.id.tvTimeDynamic)
        val deleteBtn = newSlot.findViewById<ImageView>(R.id.btnDeleteSlot)

        tvTime.text = viewModel.timeSlots[index]

        tvTime.setOnClickListener {
            showTimePicker(tvTime) { t -> viewModel.updateTimeSlot(index, t) }
        }

        deleteBtn.setOnClickListener {
            binding.slotContainer.removeView(newSlot)
            viewModel.removeTimeSlot(index)
            refreshTimeSlots()
        }

        binding.slotContainer.addView(newSlot)
    }

    private fun refreshTimeSlots() {
        viewModel.timeSlots.clear()
        for (i in 0 until binding.slotContainer.childCount) {
            val slotView = binding.slotContainer.getChildAt(i)
            val tv = slotView.findViewById<TextView>(R.id.tvTimeDynamic)
            viewModel.timeSlots.add(tv.text.toString())
        }
    }

    private fun showTimePicker(textView: TextView, result: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, h, m ->
                val time = "%02d:%02d".format(h, m)
                textView.text = time
                result(time)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    // 📅 Specific Month Date picker
    private fun openSpecificDatePicker() {
        val calendar = Calendar.getInstance()
        val listener = DatePickerDialog.OnDateSetListener { _, _, _, d ->
            val formatted = formatDay(d)
            viewModel.addSpecificDate(formatted)
            updateSpecificDatesUI()
        }

        DatePickerDialog(
            requireContext(), listener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateSpecificDatesUI() {
        binding.etSpecificDates.setText(viewModel.selectedMonthDays.joinToString(", "))
    }

    private fun formatDay(day: Int): String =
        when {
            day % 10 == 1 && day != 11 -> "${day}st"
            day % 10 == 2 && day != 12 -> "${day}nd"
            day % 10 == 3 && day != 13 -> "${day}rd"
            else -> "${day}th"
        }

    // 🔍 Medicine Search DropDown
    private fun observeMedicineList() {
        viewModel.medicineLiveData.observe(viewLifecycleOwner) { showMedicineDropDown(it) }
    }

    private fun showMedicineDropDown(list: List<Medicine>) {
        val scroll = binding.medicineScroll
        val container = binding.medicineContainer
        container.removeAllViews()

        if (list.isEmpty()) {
            collapse(scroll)
            return
        }

        expand(scroll)

        list.forEach { medicine ->
            val itemBinding = ItemMedicineBinding.inflate(layoutInflater)
            itemBinding.tvMedicineName.text = medicine.name

            itemBinding.root.setOnClickListener {
                binding.etMedicineName.setText(medicine.name)
                viewModel.updateSelectedMedicine(medicine  )
                collapse(scroll)
            }

            container.addView(itemBinding.root)
        }
    }

    // 🧠 UI Setup
    private fun setupUI() {
        setupFrequencyDropdown()
        setupDaySelection()
        setupDatePickers()
        setupTimePicker()
        setDaysValue(1)

        binding.btnUp.setOnClickListener { increaseDays() }
        binding.btnDown.setOnClickListener { decreaseDays() }

        binding.etMedicineName.addTextChangedListener { text ->
            if (!text.isNullOrEmpty()) {
                val currentName = viewModel.selectedMedicine.value?.medicineName

                if (!text.isNullOrEmpty() && text.toString() != currentName) {
 viewModel.getBrandList(text.toString())
                }
            } else collapse(binding.medicineScroll)
        }

        binding.addMedicine.setOnClickListener { showMedicineDialog() }



        binding.addMedicine.setOnClickListener() {

            viewModel.selectedMedicine.value?.id?.toInt()?.let { startdate ->
                viewModel.addMedicine(

                    medicineId = startdate,
                    dosageType = viewModel.selectedMedicine.value?.dosageFormName ,
                    dosageStrength = viewModel.selectedMedicine.value!!.doseStrength.toInt(),
                    frequency = viewModel.selectedMedicineName .value.toString(),
                    instructions =  binding.etInstructions.text.toString()  ,
                    timeSlotsJson = """[{"timeSlot":"09:30"}]""",
                    startdate =  binding.etStartDate.text.toString()  ,
                    enddate =  binding.etEndDate.text.toString()  ,

                    )
            }

        }
    }
    private fun setupFrequencyDropdown() {

        val frequencies = arrayOf(
            "Every day",
            "Every x day",
            "Every week",
            "Every month",
            "As Needed"
        )

        val adapter = ArrayAdapter(requireContext(), R.layout.item_spinner, frequencies)
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        binding.spFrequency.adapter = adapter

        binding.spFrequency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
val selectedName = parent?.getItemAtPosition(position).toString()
                viewModel.updateSelectedMedicineName(selectedName)    // save to v
                clearDaySelection()
                hideAllFrequencyContainers()

                when (position) {
                    0 -> { // Every day
                        binding.containerEveryDay.visibility = View.VISIBLE
                    }
                    1 -> { // Every x day
                        binding.containerEveryXDay.visibility = View.VISIBLE
                    }
                    2 -> { // Every week
                        binding.containerDaysOfWeek.visibility = View.VISIBLE
                    }
                    3 -> { // Every month
                        binding.containerSpecificDates.visibility = View.VISIBLE
                    }
                    4 -> { // As Needed — no UI inputs required
                        // all containers hidden
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    private fun clearDaySelection() {
        selectedDay = null

        daysList.forEach { tv ->
            tv.setBackgroundResource(R.drawable.bg_day_unselected)

            tv.setTextColor(
                if (tv.id == R.id.daySun)
                    Color.RED
                else
                    Color.GRAY
            )
        }
    }
    private fun hideAllFrequencyContainers() {
        binding.containerEveryDay.visibility = View.GONE
        binding.containerEveryXDay.visibility = View.GONE
        binding.containerDaysOfWeek.visibility = View.GONE
        binding.containerSpecificDates.visibility = View.GONE
    }
    // 🔔 Expand Collapse animation
    private fun expand(view: View) {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(binding.root.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.UNSPECIFIED
        )
        val targetHeight = view.measuredHeight
        view.layoutParams.height = 0
        view.alpha = 0f
        view.visibility = View.VISIBLE

        val animation = object : Animation() {
            override fun applyTransformation(interpolated: Float, t: Transformation?) {
                view.layoutParams.height =
                    if (interpolated == 1f) ViewGroup.LayoutParams.WRAP_CONTENT
                    else (targetHeight * interpolated).toInt()
                view.alpha = interpolated
                view.requestLayout()
            }
        }
        animation.duration = 200
        view.startAnimation(animation)
    }

    private fun collapse(view: View) {
        val initialHeight = view.measuredHeight
        val animation = object : Animation() {
            override fun applyTransformation(interpolated: Float, t: Transformation?) {
                if (interpolated == 1f) view.visibility = View.GONE
                else {
                    view.layoutParams.height =
                        initialHeight - (initialHeight * interpolated).toInt()
                    view.alpha = 1 - interpolated
                    view.requestLayout()
                }
            }
        }
        animation.duration = 200
        view.startAnimation(animation)
    }

    // 📆 Start/End Date Picker
    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()
        val listener = DatePickerDialog.OnDateSetListener { _, y, m, d ->
            val date = "%02d/%02d/%04d".format(d, m + 1, y)
            if (binding.etStartDate.isFocused)
                binding.etStartDate.setText(date)
            else
                binding.etEndDate.setText(date)
        }

        binding.etStartDate.setOnClickListener {
            DatePickerDialog(requireContext(), listener,
                calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DAY_OF_MONTH]
            ).show()
        }
        binding.etEndDate.setOnClickListener {
            DatePickerDialog(requireContext(), listener,
                calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DAY_OF_MONTH]
            ).show()
        }
    }

    private fun setupTimePicker() {
        binding.tvTime.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(
                requireContext(),
                { _, h, m -> binding.tvTime.text = "%02d:%02d".format(h, m) },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        }
    }

    // 📅 Weekly Selection
    private fun setupDaySelection() {
        daysList = listOf(
            binding.daySun, binding.dayMon, binding.dayTue,
            binding.dayWed, binding.dayThu, binding.dayFri, binding.daySat
        )

        daysList.forEach { tv ->
            tv.setOnClickListener {
                selectedDay?.apply {
                    setBackgroundResource(R.drawable.bg_day_unselected)
                    setTextColor(if (id == R.id.daySun) Color.RED else Color.GRAY)
                }
                tv.setBackgroundResource(R.drawable.bg_day_selected)
                tv.setTextColor(Color.WHITE)
                selectedDay = tv
            }
        }
    }

    private fun increaseDays() =
        setDaysValue((binding.etDays.text.toString().toIntOrNull() ?: 1) + 1)

    private fun decreaseDays() =
        setDaysValue((binding.etDays.text.toString().toIntOrNull() ?: 1) - 1)

    private fun setDaysValue(value: Int) {
        val safeValue = value.coerceIn(Min_Days, Max_Days)
        binding.etDays.setText(safeValue.toString())
        binding.etDays.setSelection(binding.etDays.length())
        binding.tvEveryXTitle.text = "Every $safeValue day${if (safeValue > 1) "s" else ""}"
    }

    // Bottom sheet dialog after Add Medicine
    private fun showMedicineDialog() {
        val dialogBinding = MedicineReminderScheduleDialogueBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(false)

        Glide.with(this)
            .asGif()
            .load(R.drawable.medicine_reminder)
            .into(dialogBinding.medicineReminder)

        dialogBinding.closeButton.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnAddMore.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}