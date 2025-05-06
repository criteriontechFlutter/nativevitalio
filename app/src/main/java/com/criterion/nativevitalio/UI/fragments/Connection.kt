package com.criterion.nativevitalio.UI.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.criterion.nativevitalio.Omron.Activities.OmronConnectedDeviceList
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.adapter.ConnectionAdapter
import com.criterion.nativevitalio.databinding.FragmentConnectionBinding
import com.criterion.nativevitalio.model.VitalDevice
import com.criterion.nativevitalio.model.VitalPosition
import com.criterion.nativevitalio.utils.LoaderUtils.hideLoading
import com.criterion.nativevitalio.utils.LoaderUtils.showLoading
import com.criterion.nativevitalio.utils.MyApplication
import com.criterion.nativevitalio.viewmodel.ConnectionViewModel

class Connection: Fragment() {

    private lateinit var adapter: ConnectionAdapter
    private lateinit var binding: FragmentConnectionBinding
    private lateinit var viewModel: ConnectionViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentConnectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vitalType = arguments?.getString("vitalType") ?: "Blood Pressure"
        viewModel = ViewModelProvider(this)[ConnectionViewModel::class.java]
        val showContainer = shouldShowContainerFor(vitalType)


        binding.deviceContainer.visibility = if (showContainer) View.VISIBLE else View.GONE


        val devices = getFilteredDevices(vitalType)
        adapter = ConnectionAdapter(devices) { selectedDevice ->
            val bundle = Bundle().apply {
                putString("deviceName", selectedDevice.name)
            }
            findNavController().navigate(R.id.action_connection_to_devicesConnectivity, bundle)
        }
        binding.recyclerViewDevices.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewDevices.adapter = adapter


        binding.btnAddVitalManually.setOnClickListener {
            showManualVitalDialog(vitalType)
        }
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) showLoading() else hideLoading()
        }

        binding.deviceContainer.setOnClickListener(){
            val intent = Intent(MyApplication.appContext, OmronConnectedDeviceList::class.java)
            startActivity(intent)


        }


        binding.backButton.setOnClickListener(){

            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

    }
    private fun shouldShowContainerFor(vitalType: String?): Boolean {
        val typeKey = when (vitalType) {
            "Blood Pressure" -> "bp"
            "Blood Oxygen (SpO2)" -> "spo2"
            "Body Weight" -> "weight"
            else -> null
        }
        return typeKey != null
    }
    private fun getFilteredDevices(vitalType: String?): List<VitalDevice> {
        val allDevices = listOf(
            VitalDevice(0, true, "OxySmart", "YK-81C",
                "Oximeter", "yonker_oxi", "YK-81C",
                "cdeacd80-...", "cdeacd81-...", false, listOf("spo2", "bp")),
            VitalDevice(1, true, "device2", "YK-81C", "Oximeter", "yonker_oxi", "YK-81C", "cdeacd80-...", "cdeacd81-...", false, listOf("rr", "bp"))
        )
        val typeKey = when (vitalType) {
            "Blood Pressure" -> "bp"
            "Heart Rate" -> "hr"
            "Blood Oxygen (spo2)" -> "spo2"
            "Body Temperature" -> "temp"
            "Respiratory Rate" -> "rr"
            "Pulse" -> "/min"
            "RBS" -> "rbs"
            "Body Weight" -> "weight"
            else -> null
        }
        return if (typeKey != null) allDevices.filter { it.dataType.contains(typeKey) } else allDevices
    }

    private fun showManualVitalDialog(vitalType: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.manual_vital_dialog, null)

//        Toast.makeText(requireContext(), vitalType, Toast.LENGTH_SHORT).show()
        val title = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val sysInput = dialogView.findViewById<EditText>(R.id.sysInput)
        val diaInput = dialogView.findViewById<EditText>(R.id.diaInput)
        val rrInput = dialogView.findViewById<EditText>(R.id.rrInput)
        val spo2Input = dialogView.findViewById<EditText>(R.id.spo2Input)
        val heartRateInput = dialogView.findViewById<EditText>(R.id.heartRateInput)
        val tempInput = dialogView.findViewById<EditText>(R.id.tempInput)
        val rbsInput = dialogView.findViewById<EditText>(R.id.rbsInput)
        val weightInput = dialogView.findViewById<EditText>(R.id.weightInput)
        val pulseInput = dialogView.findViewById<EditText>(R.id.pulseInput)
        val layoutBP = dialogView.findViewById<LinearLayout>(R.id.layoutBP)
        val saveButton = dialogView.findViewById<Button>(R.id.saveVitalsBtn)
//        val selectedPositionText = dialogView.findViewById<TextView>(R.id.tvSelectedPosition)

        fun hideAllFields() {
            layoutBP.visibility = View.GONE
            rrInput.visibility = View.GONE
            spo2Input.visibility = View.GONE
            heartRateInput.visibility = View.GONE
            tempInput.visibility = View.GONE
            rbsInput.visibility = View.GONE
            weightInput.visibility = View.GONE
            pulseInput.visibility = View.GONE
        }

        hideAllFields()


        when (vitalType) {
            "Blood Pressure" -> {
                title.text = "Enter BP Values"
                layoutBP.visibility = View.VISIBLE
            }
            "Respiratory Rate" -> rrInput.visibility = View.VISIBLE
            "Blood Oxygen (spo2)" -> spo2Input.visibility = View.VISIBLE
            "Heart Rate" -> heartRateInput.visibility = View.VISIBLE
            "Body Temperature" -> tempInput.visibility = View.VISIBLE
            "RBS" -> rbsInput.visibility = View.VISIBLE
            "Body Weight" -> weightInput.visibility = View.VISIBLE
            "Pulse Rate" -> pulseInput.visibility = View.VISIBLE


        }

        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).setCancelable(true).create()

        saveButton.setOnClickListener {
            val positions = listOf(
                VitalPosition(129, "Sitting", "VitalPosition", true, "2024-08-07T12:28:13"),
                VitalPosition(130, "Standing", "VitalPosition", true, "2024-08-07T12:28:13"),
                VitalPosition(131, "Walking", "VitalPosition", true, "2024-08-07T12:28:13"),
                VitalPosition(133, "Lying", "VitalPosition", true, "2024-08-07T12:28:13")
            )

//            val isValid = when (vitalType) {
//                "Blood Pressure" -> sysInput.text.isNotBlank() && diaInput.text.isNotBlank()
//                "Respiratory Rate" -> rrInput.text.isNotBlank()
//                "Blood Oxygen (SpO2)" -> spo2Input.text.isNotBlank()
//                "Heart Rate" -> heartRateInput.text.isNotBlank()
//                "Body Temperature" -> tempInput.text.isNotBlank()
//                "RBS" -> rbsInput.text.isNotBlank()
//                "Body Weight" -> weightInput.text.isNotBlank()
//                "Pulse" -> pulseInput.text.isNotBlank()
//                else -> false
//            }
            Toast.makeText(requireContext(), vitalType, Toast.LENGTH_SHORT).show()
            val isValid = when (vitalType) {
                "Blood Pressure" -> {
                    validateField(sysInput, "Systolic", 60.0, 250.0) &&
                            validateField(diaInput, "Diastolic", 40.0, 150.0)
                }
                "Respiratory Rate" -> validateField(rrInput, "Respiratory Rate", 5.0, 50.0)
                "Blood Oxygen (spo2)" -> validateField(spo2Input, "SpO2", 50.0, 100.0)
                "Heart Rate" -> validateField(heartRateInput, "Heart Rate", 30.0, 200.0)
                "Body Temperature" -> validateField(tempInput, "Temperature", 30.0, 45.0)
                "RBS" -> validateField(rbsInput, "RBS", 40.0, 600.0)
                "Body Weight" -> validateField(weightInput, "Weight", 1.0, 300.0)
                "Pulse" -> validateField(pulseInput, "Pulse", 30.0, 200.0)
                "Pulse Rate" -> validateField(pulseInput, "Pulse", 30.0, 200.0)
                else -> false
            }
            if (!isValid) {
                Toast.makeText(requireContext(), "Please enter valid input", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dialog.dismiss()
            showPositionDialog(positions) { positionId, remark ->
//                selectedPositionText.text = "Position: $remark"
                Toast.makeText(requireContext(), vitalType, Toast.LENGTH_SHORT).show()
                when (vitalType) {
                    "Blood Pressure" -> viewModel.insertPatientVital(findNavController(),requireContext(),sysInput.text.toString(), diaInput.text.toString(), positionId.toString())
                    "Respiratory Rate" -> viewModel.insertPatientVital(findNavController(),requireContext(),rr = rrInput.text.toString(), positionId = positionId.toString())
                    "Blood Oxygen (spo2)" -> viewModel.insertPatientVital(findNavController(),requireContext(),spo2 = spo2Input.text.toString(), positionId = positionId.toString())
                    "Heart Rate" -> viewModel.insertPatientVital(findNavController(),requireContext(),hr = heartRateInput.text.toString(), positionId = positionId.toString())
                    "Body Temperature" -> viewModel.insertPatientVital(findNavController(),requireContext(),tmp = tempInput.text.toString(), positionId = positionId.toString())
                    "RBS" -> viewModel.insertPatientVital(findNavController(),requireContext(),rbs = rbsInput.text.toString(), positionId = positionId.toString())
                    "Pulse" -> viewModel.insertPatientVital(findNavController(),requireContext(),pr = pulseInput.text.toString(), positionId = positionId.toString())
                    "Pulse Rate" -> viewModel.insertPatientVital(findNavController(),requireContext(),pr = pulseInput.text.toString(), positionId = positionId.toString())
                    "Body Weight" -> viewModel.insertPatientVital(findNavController(),requireContext(),weight = weightInput.text.toString(), positionId = positionId.toString())
                }

            }
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun showPositionDialog(
        positions: List<VitalPosition>,
        onPositionSelected: (positionId: Int, remark: String) -> Unit
    ) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_position_selector, null)
        val listView = dialogView.findViewById<ListView>(R.id.positionListView)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveSelectedPosition)

        val remarks = positions.map { it.remark }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_single_choice, remarks)
        listView.adapter = adapter

        var selectedIndex = -1
        listView.setOnItemClickListener { _, _, position, _ -> selectedIndex = position }

        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).setCancelable(true).create()

        btnSave.setOnClickListener {
            if (selectedIndex != -1) {
                val selected = positions[selectedIndex]
                onPositionSelected(selected.id, selected.remark)
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please select a position", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }


    fun validateField(field: EditText, fieldName: String, min: Double = 0.0, max: Double = 300.0): Boolean {
        val text = field.text.toString()
        val value = text.toDoubleOrNull()

        return when {
            text.isBlank() -> {
                field.error = "$fieldName is required"
                false
            }
            value == null -> {
                field.error = "$fieldName must be a number"
                false
            }
            value < min || value > max -> {
                field.error = "$fieldName must be between $min and $max"
                false
            }
            else -> true
        }
    }
}