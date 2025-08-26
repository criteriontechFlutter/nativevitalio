package com.critetiontech.ctvitalio.UI.ui.signupFragment

import PrefsManager
import android.annotation.SuppressLint
import android.app.FragmentManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager

import com.critetiontech.ctvitalio.viewmodel.RegistrationViewModel
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.Home
import com.critetiontech.ctvitalio.UI.ResetPassword
import com.critetiontech.ctvitalio.UI.ui.ConfirmUpdateDialog
import com.critetiontech.ctvitalio.adapter.SetYourOwnGoalAdapter
import com.critetiontech.ctvitalio.databinding.FragmentSetYourOwnGoalBinding
import com.critetiontech.ctvitalio.model.SetGoalModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson

class SetYourOwnGoal : Fragment() {
    private lateinit var binding: FragmentSetYourOwnGoalBinding
    private lateinit var progressViewModel: ProgressViewModel
    private lateinit var viewModel: RegistrationViewModel
    private lateinit var adapter:  SetYourOwnGoalAdapter

    val metrics = mutableListOf(
        SetGoalModel(
            R.drawable.rstep,
            "Steps",
            234,
            "steps/day",
            "Tracks the total number of steps you walk daily.",
            selectedGoal = 4000,   // default value
            normalValue  = 4000,   // default value
            isSelected = false      // initially selected
        ),
        SetGoalModel(
            R.drawable.rcalories,
            "Calories",
            244,
            "kcal/day",
            "Monitors daily calories burned through activities.",
            selectedGoal = 2000,
            normalValue  = 2000,   // default value
            isSelected = false
        ),
        SetGoalModel(
            R.drawable.rwater,
            "Water",
            245,
            "liters/day",
            "Helps track daily water intake for hydration.",
            selectedGoal = 4,
            normalValue  = 4,   // default value
            isSelected = false
        ),
        SetGoalModel(
            R.drawable.rspo2,
            "Spo2",
            6,
            "%",
            "Measures blood oxygen saturation levels.",
            selectedGoal = 98,
            normalValue  = 98,
            isSelected = false
        ),
        SetGoalModel(
            R.drawable.rhr,
            "Heart Rate",
            74,
            "bpm",
            "Tracks resting and active heart rate.",
            selectedGoal = 72,
            normalValue  = 72,
            isSelected = false
        ),
        SetGoalModel(
            R.drawable.rbp,
            "Blood Pressure",
            4,
            "mmHg",
            "Monitors systolic blood pressure values.",
            selectedGoal = 120,
            normalValue  = 120,
            isSelected = false
        ),
        SetGoalModel(
            R.drawable.rsugar,
            "Sugar",
            246,
            "mg/dL",
            "Tracks blood sugar (glucose) levels.",
            selectedGoal = 95,
            normalValue  = 95,
            isSelected = false
        ),
        SetGoalModel(
            R.drawable.rsleep,
            "Sleep",
            243,
            "hours/night",
            "Analyzes sleep duration and quality.",
            selectedGoal = 8,
            normalValue  = 8,
            isSelected = false
        )
    )


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSetYourOwnGoalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the Spinner
//        val items = listOf("Kg" )  // Units list
//        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, items)
//        adapter.setDropDownViewResource(R.layout.spinner_item)
//        binding.spinnerUnit.adapter = adapter  // Bind the adapter to the spinner

        // Get the ViewModel instances

        viewModel = ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]
        progressViewModel = ViewModelProvider(requireActivity())[ProgressViewModel::class.java]
        // Restore previously entered weight if any

         adapter = SetYourOwnGoalAdapter(metrics) { selectedGoal ->
             val index = metrics.indexOfFirst { it.vmId == selectedGoal.vmId }
             if (index != -1) {
                 if (selectedGoal.isSelected) {
                     // ✅ Unselect and reset to normalValue
                     metrics[index] = selectedGoal.copy(
                         isSelected = false,
                         selectedGoal = selectedGoal.normalValue
                     )
                     adapter.notifyItemChanged(index)
                 } else {
                     // ✅ Open BottomSheet to update value
                     showGoalPicker(requireContext(), selectedGoal) { updatedGoal ->
                         metrics[index] = updatedGoal.copy(isSelected = true)
                         adapter.notifyItemChanged(index)
                     }
                 }
             }
             if(metrics.any{
                     it.isSelected
                 }){
                 binding.btnNext.text="Finsh"
             }
        }
         val recyclerView = binding.metricsRecyclerView
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 4) // Corrected this line as well
        recyclerView.adapter = adapter

        binding.btnNext.setOnClickListener(){
            ConfirmUpdateDialog(
                title = "All Done! Your Profile is Ready",
                message = "Welcome aboard!\nLet's take you to your dashboard.",
                btnText = "Continue to Dashboard",
                onConfirm = {
                    viewModel.updateUserData(
                        requireContext(),
                        filePath = viewModel.selectedImageUri.value,

                        chronicData = Gson().toJson(
                            viewModel.selectedDiseaseList.value ?: emptyList<Map<String, String>>()
                        ),
                        street = viewModel.streetAddress.value?.toString().orEmpty(),
                        zipCode = viewModel.pinCode.value?.toString().orEmpty(),
                        countryId = viewModel.selectedCountryId.value?.toString().orEmpty(),
                        stateId = viewModel.selectedStateId.value?.toString().orEmpty(),
                        cityId = viewModel.selectedCityId.value?.toString().orEmpty(),
                        weight = viewModel.wt.value?.toString().orEmpty(),
                        height = viewModel.htInCm.value?.toString().orEmpty(),
                        bgId = viewModel.bgId.value?.toString().orEmpty(),
                        familyDiseaseJson = Gson().toJson( viewModel.familyDiseaseMap.value ?: emptyMap<String, List<String>>()),

                    )
                },

                ).show(childFragmentManager, ConfirmUpdateDialog.TAG)
        }



    }


    @SuppressLint("MissingInflatedId")
    fun  showGoalPicker(
        context: Context,
        goal: SetGoalModel,
        onGoalSelected: (SetGoalModel) -> Unit
    ) {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.set_goal_bootm_sheet, null)
        dialog.setContentView(view)

        val imgId = view.findViewById<ImageView>(R.id.imgId)
        val di = view.findViewById<TextView>(R.id.discriptionId)
        val unitId = view.findViewById<TextView>(R.id.unitId)
        val valueText = view.findViewById<TextView>(R.id.valueText)
        val btnAdd = view.findViewById<ImageView>(R.id.addarrow)
        val btnSub = view.findViewById<ImageView>(R.id.subarrow)
        val btnDone = view.findViewById<Button>(R.id.btnDone)

        // Set metric info
        imgId.setImageResource(goal.icon)
        di.text = goal.description
        unitId.text = goal.unit
        valueText.text = goal.selectedGoal.toString() // pre-populated

        // Increment / decrement
        btnAdd.setOnClickListener {
            goal.selectedGoal += 1
            valueText.text = goal.selectedGoal.toString()
        }

        btnSub.setOnClickListener {
            goal.selectedGoal = (goal.selectedGoal - 1).coerceAtLeast(0)
            valueText.text = goal.selectedGoal.toString()
        }

        // Set Goal button
        btnDone.setOnClickListener {
            goal.isSelected = true
            onGoalSelected(goal) // return updated goal to fragment/adapter
            dialog.dismiss()
        }

        dialog.show()
    }
}