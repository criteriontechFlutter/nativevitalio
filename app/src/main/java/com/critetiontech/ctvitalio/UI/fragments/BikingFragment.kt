package com.example.vitalio_pragya.fragment

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentBikingBinding
import com.critetiontech.ctvitalio.viewmodel.BinkingViewModel
import com.example.vitalio_pragya.viewmodel.AddActivityViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.*

class BikingFragment : Fragment() {

    private var _binding: FragmentBikingBinding? = null
    private val binding get() = _binding!!

    private var startTime = ""
    private var endTime = ""
    private var activityId = 1
    private var activityName = "Biking"

    private val viewModel: BinkingViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBikingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityId = arguments?.getInt("activityId") ?: 1
        activityName = arguments?.getString("activityName") ?: "Biking"

        binding.title.text = activityName
        binding.titleToolbar.text = "Add Activity"

        startTime = getCurrentTime()
        endTime = getCurrentTime()

        binding.startTimeButton.text = startTime
        binding.endTimeButton.text = endTime

        binding.backButton.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        binding.startTimeContainer.setOnClickListener { pickTime { t -> startTime = t; binding.startTimeButton.text=t } }
        binding.endTimeContainer.setOnClickListener { pickTime { t -> endTime = t; binding.endTimeButton.text=t } }

        binding.saveButton.setOnClickListener { viewModel.insertEmployeeActivity(activityId,startTime,endTime) }

        viewModel.activityInsertSuccess.observe(viewLifecycleOwner) {
            if (it) showSuccessDialog()
            else Toast.makeText(requireContext(),"Failed! Try again.",Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickTime(callback: (String) -> Unit) {
        val c = Calendar.getInstance()
        TimePickerDialog(requireContext(),{_,h,m->
            val f = if (h < 12) "AM" else "PM"
            val d = if (h % 12 == 0) 12 else h % 12
            callback(String.format("%d:%02d %s",d,m,f))
        },c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),false).show()
    }

    private fun getCurrentTime() =
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())

    private fun showSuccessDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_success_bottom,null)
        val dialog = BottomSheetDialog(requireContext(),R.style.BottomSheetDialogTheme)
        dialog.setContentView(view)
        dialog.setCancelable(false)

        Glide.with(this).asGif().load(R.drawable.sucesses_dia).into(view.findViewById(R.id.successIcon))
        view.findViewById<Button>(R.id.okButton).setOnClickListener { dialog.dismiss() }
        view.findViewById<ImageView>(R.id.closeButton).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    override fun onDestroyView() { _binding = null; super.onDestroyView() }
}