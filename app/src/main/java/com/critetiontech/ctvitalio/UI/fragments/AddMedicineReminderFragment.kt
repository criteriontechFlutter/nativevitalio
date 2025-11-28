package com.example.vitalio_pragya.fragment

import Medicine
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentAddMedicineReminderBinding
import com.critetiontech.ctvitalio.databinding.ItemMedicineBinding
import com.critetiontech.ctvitalio.databinding.MedicineReminderScheduleDialogueBinding
import com.critetiontech.ctvitalio.viewmodel.AddMedicineReminderViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.*


class AddMedicineReminderFragment : Fragment() {

    private var _binding: FragmentAddMedicineReminderBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddMedicineReminderViewModel by viewModels()

    private val maxSlots = 5
    private val weekIDs = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")  // SUN=7, MON=1 ...

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddMedicineReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFrequencyUI()
        setupDatePickers()
        observeMedicine()

        // Default first slot
        addTimeSlotView("08:00")

        binding.btnAddSlot.setOnClickListener { addTimeSlotView("08:00") }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.addMedicine.setOnClickListener {

            if (binding.etMedicineName.text.isNullOrEmpty()) {
                Toast.makeText(requireContext(),"Select Medicine First",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (binding.etStartDate.text.isNullOrEmpty()) {
                Toast.makeText(requireContext(),"Please select Start Date",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (binding.etEndDate.text.isNullOrEmpty()) {
                Toast.makeText(requireContext(),"Please select End Date",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔥 Validate Start Date must be <= End date
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            val start = formatter.parse(binding.etStartDate.text.toString())
            val end = formatter.parse(binding.etEndDate.text.toString())

            if (start.after(end)) {
                Toast.makeText(requireContext(),"End Date must be greater than Start Date",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔥 If validation success → API Call
            viewModel.addMedicineFinal(
                startDate = binding.etStartDate.text.toString(),
                endDate   = binding.etEndDate.text.toString(),
                instructions = binding.etInstructions.text.toString()
            )

            clearUI()       // after API reset fields
            showSuccessDialog()
        }

    }

    // --------------------------------------------------------------------
    // 🔥 Time Slot Handling
    // --------------------------------------------------------------------
    private fun addTimeSlotView(time: String) {

        if (viewModel.timeSlots.size >= maxSlots) return

        viewModel.addTimeSlot(time)

        val slot = layoutInflater.inflate(R.layout.item_time_slot, binding.slotContainer, false)
        val tv = slot.findViewById<TextView>(R.id.tvTimeDynamic)
        val deleteBtn = slot.findViewById<ImageView>(R.id.btnDeleteSlot)

        tv.text = time

        /** Click to open Time Picker */
        tv.setOnClickListener {
            showTimePicker(tv) { newTime ->
                tv.text = newTime
                val idx = binding.slotContainer.indexOfChild(slot)
                viewModel.updateTimeSlot(idx, newTime)
            }
        }

        /** Delete Slot (only if more than 1 slot exists) */
        deleteBtn.setOnClickListener {
            if (viewModel.timeSlots.size > 1) {       // <-- IMPORTANT
                binding.slotContainer.removeView(slot)
                val idx = binding.slotContainer.indexOfChild(slot)
                viewModel.removeTimeSlot(idx)
                refreshSlots()
            } else {
                Toast.makeText(requireContext(),"At least one time is required!",Toast.LENGTH_SHORT).show()
            }
        }

        /** 🔥 Hide delete if only one slot exists */
        deleteBtn.visibility = if (viewModel.timeSlots.size <= 1) View.GONE else View.VISIBLE

        binding.slotContainer.addView(slot)
    }
    private fun clearUI() {

        binding.etMedicineName.text.clear()
        binding.etInstructions.text.clear()
        binding.etStartDate.text.clear()
        binding.etEndDate.text.clear()
        binding.etSpecificDates.text.clear()
        binding.etDays.setText("1")     // reset x day input

        // reset dropdown
        binding.spFrequency.setSelection(0)

        // Reset Week buttons
        val weekViews = listOf(binding.daySun,binding.dayMon,binding.dayTue,binding.dayWed,
            binding.dayThu,binding.dayFri,binding.daySat)

        weekViews.forEach {
            it.isSelected = false
            it.setBackgroundResource(R.drawable.bg_day_unselected)
            it.setTextColor(Color.BLACK)
        }

        // reset time slot view → keep only one default
        binding.slotContainer.removeAllViews()
        addTimeSlotView("08:00")

        Log.e("RESET_UI", "All visible UI fields reset successfully!")
    }

    private fun refreshSlots() {

        viewModel.timeSlots.clear()

        // ⛔ Rebuild slot list
        for (i in 0 until binding.slotContainer.childCount) {
            val slotView = binding.slotContainer.getChildAt(i)
            val timeStr = slotView.findViewById<TextView>(R.id.tvTimeDynamic).text.toString()
            viewModel.timeSlots.add(timeStr)
        }

        // 🔥 Reassign listeners with new index map
        for (i in 0 until binding.slotContainer.childCount) {

            val slotView = binding.slotContainer.getChildAt(i)
            val tv  = slotView.findViewById<TextView>(R.id.tvTimeDynamic)
            val del = slotView.findViewById<ImageView>(R.id.btnDeleteSlot)

            /** ⏱ Update time slot on click */
            tv.setOnClickListener {
                showTimePicker(tv) { newTime ->
                    tv.text = newTime
                    viewModel.updateTimeSlot(i, newTime)
                }
            }

            /** ❌ Remove slot safely */
            del.setOnClickListener {
                binding.slotContainer.removeViewAt(i)  // <-- FIX
                viewModel.removeTimeSlot(i)            // <-- FIX
                refreshSlots()                         // <-- Refresh indexes again
            }

            /** 🔵 Show delete only if NOT "Every day" */
            del.visibility = if ( viewModel.selectedFrequency== "Every day") View.GONE else View.VISIBLE
        }
    }


    private fun showTimePicker(tv:TextView, cb:(String)->Unit){
        val c=Calendar.getInstance()
        TimePickerDialog(requireContext(),{_,h,m->
            val t="%02d:%02d".format(h,m)
            cb(t)
        },
            c.get(Calendar.HOUR_OF_DAY),
            c.get(Calendar.MINUTE), true).show()
    }

    // --------------------------------------------------------------------
    // 🔥 Frequency Selection — FINAL WORKING LOGIC
    // --------------------------------------------------------------------
    private fun setupFrequencyUI() {

        val freqList = listOf("Every day","Every x day","Every week","Every month","As Needed")
        binding.spFrequency.adapter = ArrayAdapter(requireContext(), R.layout.item_spinner, freqList)

        binding.spFrequency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                val selected = parent?.getItemAtPosition(position).toString()
                viewModel.setFrequency(selected)

                binding.containerEveryXDay.visibility = View.GONE
                binding.weekLayout.visibility = View.GONE
                binding.containerSpecificDates.visibility = View.GONE

                // Always keep one time field visible
                resetToSingleTimeSlot("08:00")

                when (selected) {

                    /** 🔵 EVERY DAY → one slot only, delete hidden */
                    "Every day" -> {
                        binding.containerEveryDay.visibility = View.VISIBLE
                        binding.slotContainer.visibility = View.VISIBLE
                        binding.btnAddSlot.visibility = View.GONE
                    }

                    /** 🟢 EVERY X DAY */
                    "Every x day" -> {

                        binding.containerEveryXDay.visibility = View.VISIBLE
                        binding.containerEveryDay.visibility = View.VISIBLE
                        binding.slotContainer.visibility = View.VISIBLE
                        binding.btnAddSlot.visibility = View.GONE

                        // Set initial value
                        val current = viewModel.everyXDayValue
                        binding.etDays.setText(current.toString())
                        binding.tvEveryXTitle.text = "Every $current day(s)"

                        binding.btnUp.setOnClickListener {
                            val x = (binding.etDays.text.toString().toIntOrNull() ?: 1) + 1
                            binding.etDays.setText(x.toString())
                            viewModel.updateEveryXDay(x)
                            binding.tvEveryXTitle.text = "Every $x day(s)"
                        }

                        binding.btnDown.setOnClickListener {
                            val x = (binding.etDays.text.toString().toIntOrNull() ?: 1) - 1
                            if(x >= 1) {
                                binding.etDays.setText(x.toString())
                                viewModel.updateEveryXDay(x)
                                binding.tvEveryXTitle.text = "Every $x day(s)"
                            }
                        }

                        binding.etDays.addTextChangedListener {
                            val x = it.toString().toIntOrNull() ?: 1
                            viewModel.updateEveryXDay(x)
                            binding.tvEveryXTitle.text = "Every $x day(s)"
                        }
                    }

                    /** 🟡 EVERY WEEK */
                    "Every week" -> {
                        binding.containerEveryDay.visibility = View.VISIBLE
                        binding.slotContainer.visibility = View.VISIBLE
                        binding.btnAddSlot.visibility = View.GONE
                        setupWeekSelection()
                    }

                    /** 🟣 EVERY MONTH */
                    "Every month" -> {
                        binding.containerEveryDay.visibility = View.VISIBLE
                        binding.slotContainer.visibility = View.VISIBLE
                        binding.btnAddSlot.visibility = View.GONE
                        showMonthPicker()
                    }

                    /** ⚪ AS NEEDED → COMPLETELY HIDE SLOT UI */
                    "As Needed" -> {
                        binding.containerEveryDay.visibility = View.GONE
                        binding.slotContainer.visibility = View.GONE
                        binding.btnAddSlot.visibility = View.GONE
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    private fun resetToSingleTimeSlot(defaultTime:String) {
        binding.slotContainer.removeAllViews()
        viewModel.timeSlots.clear()

        addTimeSlotView(defaultTime)  // always one visible
        binding.btnAddSlot.visibility = View.GONE   // remove add button for all frequencies
    }

    private fun setupWeekSelection() {

        binding.weekLayout.visibility = View.VISIBLE

        val days = listOf(binding.daySun,binding.dayMon,binding.dayTue,binding.dayWed,
            binding.dayThu,binding.dayFri,binding.daySat)

        days.forEachIndexed { i, tv ->

            tv.setOnClickListener {

                // 🔹 First unselect all
                days.forEach { d ->
                    d.isSelected = false
                    d.setBackgroundResource(R.drawable.bg_day_unselected)
                    d.setTextColor(Color.BLACK)
                }

                // 🔹 Select only clicked one
                tv.isSelected = true
                tv.setBackgroundResource(R.drawable.bg_day_selected)
                tv.setTextColor(Color.WHITE)

                // 🔹 Store only ONE weekId
                viewModel.selectedWeekDays.clear()
                viewModel.setWeekDay(weekIDs[i])
            }
        }
    }

    private fun showMonthPicker(){
        binding.containerSpecificDates.visibility = View.VISIBLE
        binding.ivCalendar.setOnClickListener {
            DatePickerDialog(requireContext(),{_,_,_,day->
                val s = when { day%10==1&&day!=11->"st"; day%10==2&&day!=12->"nd"; day%10==3&&day!=13->"rd"; else->"th" }
                viewModel.addSpecificDate("$day$s")
                binding.etSpecificDates.setText(viewModel.selectedMonthDays.joinToString(", "))
            },2025,0,1).show()
        }
    }

    // --------------------------------------------------------------------
    // 🔥 Medicine Search Dropdown
    // --------------------------------------------------------------------
    private fun observeMedicine(){
        binding.etMedicineName.addTextChangedListener {
            if(it.isNullOrEmpty()) binding.medicineScroll.visibility=View.GONE
            else viewModel.getBrandList(it.toString())
        }

        viewModel.medicineLiveData.observe(viewLifecycleOwner){ list ->
            binding.medicineContainer.removeAllViews()
            if(list.isEmpty()) return@observe

            binding.medicineScroll.visibility=View.VISIBLE
            list.forEach { med->
                val item = ItemMedicineBinding.inflate(layoutInflater)
                item.tvMedicineName.text = med.name
                item.root.setOnClickListener{
                    binding.etMedicineName.setText(med.name)
                    viewModel.updateSelectedMedicine(med)
                    binding.medicineScroll.visibility = View.GONE
                }
                binding.medicineContainer.addView(item.root)
            }
        }
    }

    // --------------------------------------------------------------------
    // 🔥 Date Pickers
    // --------------------------------------------------------------------
    private fun setupDatePickers(){
        val dp = DatePickerDialog.OnDateSetListener { _,y,m,d ->
            val date="%02d/%02d/%04d".format(d,m+1,y)
            if(binding.etStartDate.isFocused) binding.etStartDate.setText(date)
            else binding.etEndDate.setText(date)
        }

        binding.etStartDate.setOnClickListener{
            val c=Calendar.getInstance()
            DatePickerDialog(requireContext(),dp,c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show()
        }
        binding.etEndDate.setOnClickListener{
            val c=Calendar.getInstance()
            DatePickerDialog(requireContext(),dp,c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    // --------------------------------------------------------------------
    // SUCCESS POPUP
    // --------------------------------------------------------------------
    private fun showSuccessDialog(){
        val d = MedicineReminderScheduleDialogueBinding.inflate(layoutInflater)
        val dialog=BottomSheetDialog(requireContext(),R.style.BottomSheetDialogTheme)
        dialog.setContentView(d.root)
        dialog.setCancelable(false)
        Glide.with(this).asGif().load(R.drawable.medicine_reminder).into(d.medicineReminder)
        d.closeButton.setOnClickListener { dialog.dismiss() }
        d.btnAddMore.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onDestroyView() {
        _binding=null
        super.onDestroyView()
    }
}