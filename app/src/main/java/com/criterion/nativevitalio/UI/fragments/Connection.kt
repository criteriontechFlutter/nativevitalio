package com.criterion.nativevitalio.UI.fragments

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.adapter.ConnectionAdapter
import com.criterion.nativevitalio.databinding.FragmentConnectionBinding
import com.criterion.nativevitalio.model.VitalDevice
import com.criterion.nativevitalio.model.VitalPosition
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

        val devices = getFilteredDevices(vitalType)
        adapter = ConnectionAdapter(devices)
        binding.recyclerViewDevices.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewDevices.adapter = adapter

        binding.btnAddVitalManually.setOnClickListener {
            showManualVitalDialog(vitalType)
        }

        binding.backButton.setOnClickListener(){

            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun getFilteredDevices(vitalType: String?): List<VitalDevice> {
        val allDevices = listOf(
            VitalDevice(0, true, "yonker", "YK-81C", "Oximeter", "yonker_oxi", "YK-81C", "cdeacd80-...", "cdeacd81-...", false, listOf("spo2", "bp")),
            VitalDevice(1, true, "device2", "YK-81C", "Oximeter", "yonker_oxi", "YK-81C", "cdeacd80-...", "cdeacd81-...", false, listOf("rr", "bp"))
        )
        val typeKey = when (vitalType) {
            "Blood Pressure" -> "bp"
            "Heart Rate" -> "hr"
            "Blood Oxygen (SpO2)" -> "spo2"
            "Body Temperature" -> "temp"
            "Respiratory Rate" -> "rr"
            "RBS" -> "rbs"
            "Body Weight" -> "weight"
            else -> null
        }
        return if (typeKey != null) allDevices.filter { it.dataType.contains(typeKey) } else allDevices
    }

    private fun showManualVitalDialog(vitalType: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.manual_vital_dialog, null)

        Toast.makeText(requireContext(), vitalType, Toast.LENGTH_SHORT).show()
        val title = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val sysInput = dialogView.findViewById<EditText>(R.id.sysInput)
        val diaInput = dialogView.findViewById<EditText>(R.id.diaInput)
        val rrInput = dialogView.findViewById<EditText>(R.id.rrInput)
        val spo2Input = dialogView.findViewById<EditText>(R.id.spo2Input)
        val heartRateInput = dialogView.findViewById<EditText>(R.id.heartRateInput)
        val tempInput = dialogView.findViewById<EditText>(R.id.tempInput)
        val rbsInput = dialogView.findViewById<EditText>(R.id.rbsInput)
        val weightInput = dialogView.findViewById<EditText>(R.id.weightInput)
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
        }

        hideAllFields()


        when (vitalType) {
            "Blood Pressure" -> {
                title.text = "Enter BP Values"
                layoutBP.visibility = View.VISIBLE
            }
            "Respiratory Rate" -> rrInput.visibility = View.VISIBLE
            "Blood Oxygen (SpO2)" -> spo2Input.visibility = View.VISIBLE
            "Heart Rate" -> heartRateInput.visibility = View.VISIBLE
            "Body Temperature" -> tempInput.visibility = View.VISIBLE
            "RBS" -> rbsInput.visibility = View.VISIBLE
            "Body Weight" -> weightInput.visibility = View.VISIBLE


            "SpO2" -> spo2Input.visibility = View.VISIBLE
            "RespRate" -> rrInput.visibility = View.VISIBLE
            "Temperature" -> tempInput.visibility = View.VISIBLE
            "RBS" -> rbsInput.visibility = View.VISIBLE
            "Weight" -> weightInput.visibility = View.VISIBLE
        }

        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).setCancelable(true).create()

        saveButton.setOnClickListener {
            val positions = listOf(
                VitalPosition(129, "Sitting", "VitalPosition", true, "2024-08-07T12:28:13"),
                VitalPosition(130, "Standing", "VitalPosition", true, "2024-08-07T12:28:13"),
                VitalPosition(131, "Walking", "VitalPosition", true, "2024-08-07T12:28:13"),
                VitalPosition(133, "Lying", "VitalPosition", true, "2024-08-07T12:28:13")
            )

            val isValid = when (vitalType) {
                "Blood Pressure" -> sysInput.text.isNotBlank() && diaInput.text.isNotBlank()
                "Respiratory Rate" -> rrInput.text.isNotBlank()
                "Blood Oxygen (SpO2)" -> spo2Input.text.isNotBlank()
                "Heart Rate" -> heartRateInput.text.isNotBlank()
                "Body Temperature" -> tempInput.text.isNotBlank()
                "RBS" -> rbsInput.text.isNotBlank()
                "Body Weight" -> weightInput.text.isNotBlank()
                else -> false
            }

            if (!isValid) {
                Toast.makeText(requireContext(), "Please enter valid input", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dialog.dismiss()
            showPositionDialog(positions) { positionId, remark ->
//                selectedPositionText.text = "Position: $remark"

                when (vitalType) {
                    "Blood Pressure" -> viewModel.insertPatientVital(sysInput.text.toString(), diaInput.text.toString(), positionId.toString())
                    "Respiratory Rate" -> viewModel.insertPatientVital(rr = rrInput.text.toString(), positionId = positionId.toString())
                    "Blood Oxygen (SpO2)" -> viewModel.insertPatientVital(spo2 = spo2Input.text.toString(), positionId = positionId.toString())
                    "Heart Rate" -> viewModel.insertPatientVital(hr = heartRateInput.text.toString(), positionId = positionId.toString())
                    "Body Temperature" -> viewModel.insertPatientVital(tmp = tempInput.text.toString(), positionId = positionId.toString())
                    "RBS" -> viewModel.insertPatientVital(rbs = rbsInput.text.toString(), positionId = positionId.toString())
                    "Body Weight" -> viewModel.insertPatientVital(weight = weightInput.text.toString(), positionId = positionId.toString())
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
}