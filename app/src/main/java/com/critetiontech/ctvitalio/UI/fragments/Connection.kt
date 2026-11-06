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
import com.critetiontech.ctvitalio.utils.showSafe
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


        when (vitalType) {
            "Blood Pressure" -> binding.toolbar.title = "Add Blood Pressure "

            "Glucose" -> binding.toolbar.title = "Add Glucose Level "
        }
        binding.vitalType = vitalType
        viewModel = ViewModelProvider(this)[ConnectionViewModel::class.java]
        lifecycleScope.launchWhenStarted {
            viewModel.events.collect { event ->
                when (event) {
                    is UiEvent.ShowBottomSheet -> {
                        GlobalMessageBottomSheet(
                            iconRes = event.icon,
                            title = if (vitalType == "Glucose") {
                                "Glucose Logged Successfully!"
                            } else {
                                "Blood Pressure Logged Successfully!"
                            },
                            message = event.message,
                            buttonText = event.buttonText,
                            haptic = event.hapticType
                        ).showSafe(parentFragmentManager, "msg")

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

}