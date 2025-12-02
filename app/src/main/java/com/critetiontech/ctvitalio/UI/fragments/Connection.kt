package com.critetiontech.ctvitalio.UI.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.BaseActivity
import com.critetiontech.ctvitalio.UI.OmronActivity.OmronConnectedDeviceList
import com.critetiontech.ctvitalio.adapter.ConnectionAdapter
import com.critetiontech.ctvitalio.common.GlobalMessageBottomSheet
import com.critetiontech.ctvitalio.databinding.FragmentConnectionBinding
import com.critetiontech.ctvitalio.model.VitalDevice
import com.critetiontech.ctvitalio.model.VitalPosition
import com.critetiontech.ctvitalio.utils.LoaderUtils.hideLoading
import com.critetiontech.ctvitalio.utils.LoaderUtils.showLoading
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.viewmodel.ConnectionViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import sealedClass.UiEvent

class Connection : Fragment() {

    private lateinit var adapter: ConnectionAdapter
    private lateinit var binding: FragmentConnectionBinding
    private lateinit var viewModel: ConnectionViewModel
    private var glucoseValue = 125   // default
    private var systolicValue = 120   // default
    private var dystolicValue = 80   // default
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentConnectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as? BaseActivity)?.setSystemBarsColor(
            statusBarColor = R.color.white,
            navBarColor = R.color.white,
            lightIcons = true
        )
        val vitalType = arguments?.getString("vitalType") ?: "Blood Pressure"
        binding.vitalType = vitalType;
        viewModel = ViewModelProvider(this)[ConnectionViewModel::class.java]
        lifecycleScope.launchWhenStarted {
            viewModel.events.collect { event ->
                when (event) {
                    is UiEvent.ShowBottomSheet -> {
                        GlobalMessageBottomSheet(
                            iconRes = event.icon,
                            title = event.title,
                            message = event.message,
                            buttonText = event.buttonText,
                            haptic = event.hapticType
                        ).show(parentFragmentManager, "msg")
                    }
                }
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        updateUI()

        // Glucose
        setupCounter(
            binding.btnPlus, binding.btnMinus,
            { glucoseValue }, { glucoseValue = it })
         // Systolic
        setupCounter(
            binding.btnPlusSystolic, binding.btnMinusSystolic,
            { systolicValue }, { systolicValue = it })

        // Diastolic
        setupCounter(
            binding.btnPlusDystolic, binding.btnMinusDystolic,
            { dystolicValue }, { dystolicValue = it })
// Inline edit for glucose value

// Add Reading
        binding.btnAddReading.setOnClickListener {
            when (vitalType) {
                "Blood Pressure" -> viewModel.insertPatientVital(
                    navController = findNavController(),
                    requireContext = requireContext(),
                    BPSys = binding.tvSystolicValue.text.toString(),
                    BPDias = binding.tvDystolicValue.text.toString(),
                    positionId = "0"
                )

                "Glucose" -> viewModel.insertPatientVital(
                    navController = findNavController(),
                    requireContext = requireContext(),
                    glucose = binding.tvGlucoseValue.text.toString(),
                    positionId = "0"
                )
            }
        }

        binding.etHiddenInput.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {

                val value = v.text.toString()
                if (value.isNotEmpty()) {
                    binding.tvGlucoseValue.text = value
                }

                // Hide keyboard
                val imm =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.etHiddenInput.windowToken, 0)

                binding.etHiddenInput.visibility = View.INVISIBLE
                true
            } else false
        }


    }

    private fun setupCounter(
        plusBtn: View,
        minusBtn: View,
        getter: () -> Int,
        setter: (Int) -> Unit,
        min: Int = 0
    ) {
        plusBtn.setOnClickListener {
            setter(getter() + 1)
            updateUI()
        }
        minusBtn.setOnClickListener {
            if (getter() > min) {
                setter(getter() - 1)
                updateUI()
            }
        }
    }


    private fun updateUI() {
        binding.tvGlucoseValue.text = glucoseValue.toString()
        binding.tvDystolicValue.text = dystolicValue.toString()
        binding.tvSystolicValue.text = systolicValue.toString()
    }

//    private fun showValueEditDialog() {
//        val editText = EditText(requireContext())
//        editText.inputType = InputType.TYPE_CLASS_NUMBER
//        editText.setText(glucoseValue.toString())
//
//        AlertDialog.Builder(requireContext())
//            .setTitle("Enter Glucose Value")
//            .setView(editText)
//            .setPositiveButton("OK") { _, _ ->
//                val newValue = editText.text.toString()
//                if (newValue.isNotEmpty()) {
//                    glucoseValue = newValue.toInt()
//                    updateUI()
//                }
//            }
//            .setNegativeButton("Cancel", null)
//            .show()
//    }
//
//    private fun shouldShowContainerFor(vitalType: String?): Boolean {
//        val typeKey = when (vitalType) {
//            "Blood Pressure" -> "bp"
//            "Blood Oxygen (SpO2)" -> "spo2"
//            "Body Weight" -> "weight"
//            else -> null
//        }
//        return typeKey != null
//    }
//    private fun getFilteredDevices(vitalType: String?): List<VitalDevice> {
//        val allDevices = listOf(
//            VitalDevice(0, true, "OxySmart", "YK-81C",
//                "Oximeter", "yonker_oxi", "YK-81C",
//                "cdeacd80-...", "cdeacd81-...", false, listOf("spo2", "bp")),
//            VitalDevice(1, true, "device2", "YK-81C", "Oximeter", "yonker_oxi", "YK-81C", "cdeacd80-...", "cdeacd81-...", false, listOf("rr", "bp"))
//        )
//        val typeKey = when (vitalType) {
//            "Blood Pressure" -> "bp"
//            "Heart Rate" -> "hr"
//            "Blood Oxygen (spo2)" -> "spo2"
//            "Body Temperature" -> "temp"
//            "Respiratory Rate" -> "rr"
//            "Pulse" -> "/min"
//            "RBS" -> "rbs"
//            "Body Weight" -> "weight"
//
//            "Glucose" -> "mg/dL"
//            else -> null
//        }
//        return if (typeKey != null) allDevices.filter { it.dataType.contains(typeKey) } else allDevices
//    }
//
//
//
//    @SuppressLint("MissingInflatedId")
//    private fun showManualVitalDialog(vitalType: String) {
//        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.manual_bp_dialog, null)
//        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
//        bottomSheetDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        bottomSheetDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
//
//        bottomSheetDialog.setContentView(dialogView)
//
//        // Find views
//        val title = dialogView.findViewById<TextView>(R.id.dialogTitle)
//        title.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black)) // Set text color to black
//
//        val sysInput = dialogView.findViewById<EditText>(R.id.sysInput)
//        val diaInput = dialogView.findViewById<EditText>(R.id.diaInput)
//        val rrInput = dialogView.findViewById<EditText>(R.id.rrInput)
//        val spo2Input = dialogView.findViewById<EditText>(R.id.spo2Input)
//        val heartRateInput = dialogView.findViewById<EditText>(R.id.heartRateInput)
//        val tempInput = dialogView.findViewById<EditText>(R.id.tempInput)
//        val rbsInput = dialogView.findViewById<EditText>(R.id.rbsInput)
//        val weightInput = dialogView.findViewById<EditText>(R.id.weightInput)
//        val pulseInput = dialogView.findViewById<EditText>(R.id.pulseInput)
//        val glucoseInput = dialogView.findViewById<EditText>(R.id.glucoseInput)
//        val saveButton = dialogView.findViewById<Button>(R.id.saveVitalsBtn)
//
//        val closeButton = dialogView.findViewById<ImageView>(R.id.closeButton)
//        val sysContainer = dialogView.findViewById<LinearLayout>(R.id.sysContainer)
//        val diaContainer = dialogView.findViewById<LinearLayout>(R.id.diaContainer)
//        val pulseContainer = dialogView.findViewById<LinearLayout>(R.id.pulseContainer)
//        val rrContainer = dialogView.findViewById<LinearLayout>(R.id.rrContainer)
//        val spo2Container = dialogView.findViewById<LinearLayout>(R.id.spo2Container)
//        val heartRateContainer = dialogView.findViewById<LinearLayout>(R.id.heartRateContainer)
//        val tempContainer = dialogView.findViewById<LinearLayout>(R.id.tempContainer)
//        val glucose = dialogView.findViewById<LinearLayout>(R.id.glucoseContainer)
//        val rbsContainer = dialogView.findViewById<LinearLayout>(R.id.rbsContainer)
//        val weightContainer = dialogView.findViewById<LinearLayout>(R.id.weightContainer)
//
//        // Helper function to hide all input fields initially
//        fun hideAllFields() {
//            sysContainer.visibility = View.GONE
//            diaContainer.visibility = View.GONE
//            pulseContainer.visibility = View.GONE
//            rrContainer.visibility = View.GONE
//            spo2Container.visibility = View.GONE
//            heartRateContainer.visibility = View.GONE
//            tempContainer.visibility = View.GONE
//            glucose.visibility = View.GONE
//            rbsContainer.visibility = View.GONE
//            weightContainer.visibility = View.GONE
//        }
//
//        closeButton.setOnClickListener {
//            bottomSheetDialog.dismiss()
//        }
//
//        hideAllFields()
//
//        // Show only the relevant fields depending on vitalType
//        when (vitalType) {
//            "Blood Pressure" -> {
//                title.text = "Enter BP Values"
//                sysContainer.visibility = View.VISIBLE
//                diaContainer.visibility = View.VISIBLE
//            }
//            "Respiratory Rate" -> {
//                title.text = "Enter RR Value"
//                rrContainer.visibility = View.VISIBLE
//            }
//            "Blood Oxygen (spo2)" -> {
//                title.text = "Enter SpO2 Value"
//                spo2Container.visibility = View.VISIBLE
//            }
//            "Heart Rate" -> {
//                title.text = "Enter Heart Rate Value"
//                heartRateContainer.visibility = View.VISIBLE
//            }
//            "Body Temperature" -> {
//                title.text = "Enter Temperature Value"
//                tempContainer.visibility = View.VISIBLE
//            }
//            "RBS" -> {
//                title.text = "Enter RBS Value"
//                rbsContainer.visibility = View.VISIBLE
//            }
//            "Body Weight" -> {
//                title.text = "Enter Weight Value"
//                weightContainer.visibility = View.VISIBLE
//            }
//            "Pulse Rate" -> {
//                title.text = "Enter Pulse Rate Value"
//                pulseContainer.visibility = View.VISIBLE
//            }
//            "Glucose" -> {
//                title.text = "Enter Glucose Value"
//                glucose.visibility = View.VISIBLE
//            }
//        }
//
//        // Validation function to check input
//        fun isInputValid(): Boolean {
//            return when (vitalType) {
//                "Blood Pressure" -> {
//                    validateField(sysInput, "Systolic", 60.0, 250.0) &&
//                            validateField(diaInput, "Diastolic", 40.0, 150.0)
//                }
//                "Respiratory Rate" -> validateField(rrInput, "Respiratory Rate", 5.0, 50.0)
//                "Blood Oxygen (spo2)" -> validateField(spo2Input, "SpO2", 50.0, 100.0)
//                "Heart Rate" -> validateField(heartRateInput, "Heart Rate", 30.0, 200.0)
//                "Body Temperature" -> validateField(tempInput, "Temperature", 30.0, 45.0)
//                "RBS" -> validateField(rbsInput, "RBS", 40.0, 600.0)
//                "Body Weight" -> validateField(weightInput, "Weight", 1.0, 300.0)
//                "Pulse Rate" -> validateField(pulseInput, "Pulse", 30.0, 200.0)
//                "Glucose" -> validateField(glucoseInput, "Glucose", 10.0, 550.0)
//                else -> false
//            }
//        }
//        val fontSizeIncrement = 50f
//        fun adjustFontSize(editText: EditText) {
//            var fontSize = editText.textSize / resources.displayMetrics.scaledDensity // Convert to SP
//            fontSize  = fontSizeIncrement // Increase the font size
//            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)
//            editText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
//        }
//
//        // Add TextWatcher to all fields
//        val fields = listOf(sysInput, diaInput, rrInput, spo2Input, heartRateInput, tempInput,
//            rbsInput, weightInput, pulseInput, glucoseInput)
//        fields.forEach { field ->
//            field.addTextChangedListener(object : TextWatcher {
//                override fun afterTextChanged(s: Editable?) {
//                    adjustFontSize(field)
//                    saveButton.isEnabled = isInputValid()
//                }
//                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//            })
//        }
//
//        // Save Button click
//        saveButton.setOnClickListener {
//            if (isInputValid()) {
//                bottomSheetDialog.dismiss()
//
//                // Show position dialog after input validation passes
//                val positions = listOf(
//                    VitalPosition(129, "Sitting", "VitalPosition", true, "2024-08-07T12:28:13"),
//                    VitalPosition(130, "Standing", "VitalPosition", true, "2024-08-07T12:28:13"),
//                    VitalPosition(131, "Walking", "VitalPosition", true, "2024-08-07T12:28:13"),
//                    VitalPosition(133, "Lying", "VitalPosition", true, "2024-08-07T12:28:13")
//                )
//
//
//
//                showPositionDialog(positions) { positionId, remark ->
//                    when (vitalType) {
//                        "Blood Pressure" -> viewModel.insertPatientVital(
//                            navController = findNavController(),
//                            requireContext = requireContext(),
//                            BPSys = sysInput.text.toString(),
//                            BPDias = diaInput.text.toString(),
//                            positionId = positionId.toString()
//                        )
//                        "Respiratory Rate" -> viewModel.insertPatientVital(findNavController(), requireContext(), rr = rrInput.text.toString(), positionId = positionId.toString())
//                        "Blood Oxygen (spo2)" -> viewModel.insertPatientVital(findNavController(), requireContext(), spo2 = spo2Input.text.toString(), positionId = positionId.toString())
//                        "Heart Rate" -> viewModel.insertPatientVital(findNavController(), requireContext(), hr = heartRateInput.text.toString(), positionId = positionId.toString())
//                        "Body Temperature" -> viewModel.insertPatientVital(findNavController(), requireContext(), tmp = tempInput.text.toString(), positionId = positionId.toString())
//                        "RBS" -> viewModel.insertPatientVital(findNavController(), requireContext(), rbs = rbsInput.text.toString(), positionId = positionId.toString())
//                        "Pulse Rate" -> viewModel.insertPatientVital(findNavController(), requireContext(), pr = pulseInput.text.toString(), positionId = positionId.toString())
//                        "Body Weight" -> viewModel.insertPatientVital(findNavController(), requireContext(), weight = weightInput.text.toString(), positionId = positionId.toString())
//                        "Glucose" -> viewModel.insertPatientVital(findNavController(), requireContext(),
//                            glucose = glucoseInput.text.toString(),
//                            positionId = positionId.toString())
//
//                    }
//                }
//            } else {
//                Toast.makeText(requireContext(), "Please enter valid input", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        bottomSheetDialog.setOnShowListener { dialog ->
//            val bottomSheet = (dialog as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
//            bottomSheet?.let {
//                val behavior = BottomSheetBehavior.from(it)
//                behavior.state = BottomSheetBehavior.STATE_EXPANDED
//                behavior.skipCollapsed = true
//                behavior.isDraggable = false
//                behavior.peekHeight = it.height
//            }
//        }
//
//        bottomSheetDialog.show()
//    }
//    private fun showPositionDialog(
//        positions: List<VitalPosition>,
//        onPositionSelected: (positionId: Int, remark: String) -> Unit
//    ) {
//        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_position_selector, null)
//        val listView = dialogView.findViewById<ListView>(R.id.positionListView)
//        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveSelectedPosition)
//
//        val remarks = positions.map { it.remark }
//        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_single_choice, remarks)
//        listView.adapter = adapter
//
//        var selectedIndex = -1
//        listView.setOnItemClickListener { _, _, position, _ -> selectedIndex = position }
//
//        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).setCancelable(true).create()
//
//        btnSave.setOnClickListener {
//            if (selectedIndex != -1) {
//                val selected = positions[selectedIndex]
//                onPositionSelected(selected.id, selected.remark)
//                dialog.dismiss()
//            } else {
//                Toast.makeText(requireContext(), "Please select a position", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        dialog.show()
//    }
//
//
//    fun validateField(field: EditText, fieldName: String, min: Double = 0.0, max: Double = 300.0): Boolean {
//        val text = field.text.toString()
//        val value = text.toDoubleOrNull()
//        return when {
//            text.isBlank() -> {
//                field.error = "$fieldName is required"
//                false
//            }
//            value == null -> {
//                field.error = "$fieldName must be a number"
//                false
//            }
//            value < min || value > max -> {
//                field.error = "$fieldName must be between $min and $max"
//                false
//            }
//            else -> true
//        }
//    }
}