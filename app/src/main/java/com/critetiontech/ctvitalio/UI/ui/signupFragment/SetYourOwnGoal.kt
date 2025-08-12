package com.critetiontech.ctvitalio.UI.ui.signupFragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager

import com.critetiontech.ctvitalio.viewmodel.RegistrationViewModel
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.SetYourOwnGoalAdapter
import com.critetiontech.ctvitalio.databinding.FragmentSetYourOwnGoalBinding
import com.critetiontech.ctvitalio.model.SetGoalModel
import com.google.android.material.bottomsheet.BottomSheetDialog

class SetYourOwnGoal : Fragment() {
    private lateinit var binding: FragmentSetYourOwnGoalBinding
    private lateinit var progressViewModel: ProgressViewModel
    private lateinit var viewModel: RegistrationViewModel

    val metrics = listOf(
        SetGoalModel(R.drawable.rstep, "Steps"),
        SetGoalModel(R.drawable.rcalories, "Calories"),
        SetGoalModel(R.drawable.rwater, "Water"),
        SetGoalModel(R.drawable.rspo2, "Spo2"),
        SetGoalModel(R.drawable.rhr, "Heart Rate"),
        SetGoalModel(R.drawable.rbp, "Blood Pressure"),
        SetGoalModel(R.drawable.rsugar, "Sugar"),
        SetGoalModel(R.drawable.rsleep, "Sleep")
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
        val adapter = SetYourOwnGoalAdapter(metrics) { selectedGoal ->
            context?.let { showHeightPicker(it,selectedGoal.label.toString()) }
         }

         val recyclerView = binding.metricsRecyclerView
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 4) // Corrected this line as well
        recyclerView.adapter = adapter


    }


    fun showHeightPicker(context: Context, onSelected: String) {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.set_goal_bootm_sheet, null)
        dialog.setContentView(view)

        val imperialContainer = view.findViewById<LinearLayout>(R.id.containerImperial)
        val btnDone = view.findViewById<Button>(R.id.btnDone)


        btnDone.setOnClickListener {


            dialog.dismiss()
        }

        dialog.show()
    }

}