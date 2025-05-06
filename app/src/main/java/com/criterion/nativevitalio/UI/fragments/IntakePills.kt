package com.criterion.nativevitalio.UI.fragments

import PillReminderModel
import PillTime
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.criterion.nativevitalio.databinding.FragmentIntakePillsBinding
import com.criterion.nativevitalio.viewmodel.IntakePillsViewModel

class IntakePills : Fragment() {

    private var pill: PillReminderModel? = null
    private lateinit var _binding: FragmentIntakePillsBinding
    private lateinit var viewModel: IntakePillsViewModel

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pill = arguments?.getSerializable("PILL_DATA") as? PillReminderModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIntakePillsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvMedName.text = pill?.drugName ?: "No pill info"
//        binding.tvMedDosage.text = pill?.dosageForm
        binding.tvInstruction.text = pill?.remark


        viewModel = ViewModelProvider(this)[IntakePillsViewModel::class.java]
        val timeObj: PillTime? = pill?.jsonTime?.firstOrNull()
        binding.imgCheck.setOnClickListener {
            timeObj?.let {
                viewModel.insertPatientMedication(
                    pmID = pill?.pmId.toString()  ,
                    prescriptionID = pill?.prescriptionRowID.toString(),
                    durationType = it.durationType,
                    compareTime = it.time
                )
            }
        }

        }


}